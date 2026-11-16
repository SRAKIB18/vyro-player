package com.vyro.player.decoder

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import com.vyro.player.domain.model.CompatibilityReport
import com.vyro.player.domain.model.CompatibilityStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodecCompatibilityInspector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val nativeBridge: NativeDecoderBridge = DefaultNativeDecoderBridge()

    private val supportedContainers = setOf(
        "MP4", "MKV", "WEBM", "MOV", "AVI", "TS", "MTS", "M2TS", "3GP", "MPG", "MPEG", "FLV"
    )

    fun inspectFile(pathOrUri: String): CompatibilityReport {
        val containerExt = getContainerExtension(pathOrUri)

        var videoMime: String? = null
        var audioMime: String? = null
        var width = 0
        var height = 0
        var frameRate = 0f
        var bitrate = 0L

        try {
            val retriever = MediaMetadataRetriever()
            if (pathOrUri.startsWith("content://")) {
                retriever.setDataSource(context, Uri.parse(pathOrUri))
            } else {
                retriever.setDataSource(pathOrUri)
            }

            videoMime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (_: Exception) { }

        // Use MediaExtractor to get precise track MIME types
        try {
            val extractor = MediaExtractor()
            if (pathOrUri.startsWith("content://")) {
                extractor.setDataSource(context, Uri.parse(pathOrUri), null)
            } else {
                extractor.setDataSource(pathOrUri)
            }

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/") && videoMime.isNullOrEmpty()) {
                    videoMime = mime
                    if (format.containsKey(MediaFormat.KEY_WIDTH)) width = format.getInteger(MediaFormat.KEY_WIDTH)
                    if (format.containsKey(MediaFormat.KEY_HEIGHT)) height = format.getInteger(MediaFormat.KEY_HEIGHT)
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
                } else if (mime.startsWith("audio/") && audioMime.isNullOrEmpty()) {
                    audioMime = mime
                }
            }
            extractor.release()
        } catch (_: Exception) { }

        val isContainerSupported = supportedContainers.contains(containerExt) || nativeBridge.supportsContainer(containerExt)

        val videoDecoders = if (videoMime != null) findDecodersForMime(videoMime) else emptyList()
        val audioDecoders = if (audioMime != null) findDecodersForMime(audioMime) else emptyList()

        val hasHwVideo = videoDecoders.any { it.isHardware }
        val hasSwVideo = videoDecoders.any { it.isSoftware }
        val isVideoSupported = videoMime == null || hasHwVideo || hasSwVideo || nativeBridge.supportsCodec(videoMime)

        val hasHwAudio = audioDecoders.any { it.isHardware }
        val hasSwAudio = audioDecoders.any { it.isSoftware }
        val isAudioSupported = audioMime == null || hasHwAudio || hasSwAudio || nativeBridge.supportsCodec(audioMime)

        val videoCodecName = getFriendlyCodecName(videoMime)
        val audioCodecName = getFriendlyCodecName(audioMime)

        val (status, message) = when {
            !isContainerSupported -> {
                CompatibilityStatus.UNSUPPORTED_CONTAINER to "Unsupported container format: .$containerExt. Media3 hardware decoders cannot demux this file."
            }
            !isVideoSupported -> {
                CompatibilityStatus.UNSUPPORTED_VIDEO_CODEC to "Unsupported video codec: $videoCodecName ($videoMime). No hardware or software decoders available on device."
            }
            !isAudioSupported -> {
                CompatibilityStatus.UNSUPPORTED_AUDIO_CODEC to "Unsupported audio codec: $audioCodecName ($audioMime). Audio playback will be muted or fail."
            }
            hasHwVideo -> {
                CompatibilityStatus.FULLY_SUPPORTED_HW to "Fully supported with hardware acceleration ($videoCodecName)."
            }
            hasSwVideo -> {
                CompatibilityStatus.SUPPORTED_SW_FALLBACK to "Supported via software decoder fallback ($videoCodecName)."
            }
            else -> {
                CompatibilityStatus.UNREADABLE_FILE to "Unable to decode file metadata or header."
            }
        }

        return CompatibilityReport(
            filePath = pathOrUri,
            containerFormat = containerExt.ifEmpty { "Unknown" },
            videoMimeType = videoMime,
            videoCodecName = videoCodecName,
            audioMimeType = audioMime,
            audioCodecName = audioCodecName,
            width = width,
            height = height,
            frameRate = frameRate,
            bitrate = bitrate,
            isContainerSupported = isContainerSupported,
            isVideoSupported = isVideoSupported,
            isAudioSupported = isAudioSupported,
            hasHardwareVideoDecoder = hasHwVideo,
            hasSoftwareVideoDecoder = hasSwVideo,
            hasHardwareAudioDecoder = hasHwAudio,
            hasSoftwareAudioDecoder = hasSwAudio,
            compatibilityStatus = status,
            statusMessage = message
        )
    }

    private data class DecoderMatch(val name: String, val isHardware: Boolean, val isSoftware: Boolean)

    private fun findDecodersForMime(mimeType: String): List<DecoderMatch> {
        val matches = mutableListOf<DecoderMatch>()
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                if (info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }) {
                    val isSoftware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        info.isSoftwareOnly
                    } else {
                        info.name.startsWith("OMX.google.") || info.name.startsWith("c2.android.")
                    }
                    matches.add(DecoderMatch(name = info.name, isHardware = !isSoftware, isSoftware = isSoftware))
                }
            }
        } catch (_: Exception) { }
        return matches
    }

    private fun getContainerExtension(pathOrUri: String): String {
        val lastDot = pathOrUri.lastIndexOf('.')
        return if (lastDot != -1 && lastDot < pathOrUri.length - 1) {
            pathOrUri.substring(lastDot + 1).uppercase(Locale.ROOT)
        } else ""
    }

    private fun getFriendlyCodecName(mime: String?): String {
        if (mime == null) return "Unknown Codec"
        return when (mime.lowercase(Locale.ROOT)) {
            "video/avc" -> "H.264 / AVC"
            "video/hevc" -> "H.265 / HEVC"
            "video/x-vnd.on2.vp8" -> "VP8"
            "video/x-vnd.on2.vp9" -> "VP9"
            "video/av01" -> "AV1"
            "video/mp4v-es" -> "MPEG-4 Part 2"
            "video/3gpp" -> "H.263"
            "video/x-ms-wmv", "video/x-ms-asf" -> "WMV3 / VC-1"
            "audio/mp4a-latm" -> "AAC"
            "audio/mpeg" -> "MP3"
            "audio/ac3" -> "AC-3 (Dolby Digital)"
            "audio/eac3" -> "E-AC-3 (Dolby Digital Plus)"
            "audio/flac" -> "FLAC"
            "audio/opus" -> "Opus"
            "audio/vorbis" -> "Vorbis"
            "audio/vnd.dts" -> "DTS Audio"
            else -> mime.substringAfter('/')
        }
    }
}
