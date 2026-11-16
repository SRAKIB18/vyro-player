package com.vyro.player.decoder

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.view.Display
import com.vyro.player.domain.model.HdrDisplayInfo
import com.vyro.player.domain.model.HdrFormat
import com.vyro.player.domain.model.HdrMetadataInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HdrCapabilityInspector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun inspectDisplayHdrCapabilities(): HdrDisplayInfo {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)

        if (display == null) {
            return HdrDisplayInfo(
                isHdrSupported = false,
                supportsHdr10 = false,
                supportsHdr10Plus = false,
                supportsHlg = false,
                supportsDolbyVision = false
            )
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val caps = try {
                display.hdrCapabilities
            } catch (_: Exception) {
                null
            }

            if (caps != null) {
                val types = caps.supportedHdrTypes
                val hasHdr10 = types.contains(Display.HdrCapabilities.HDR_TYPE_HDR10)
                val hasHdr10Plus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    types.contains(Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS)
                } else false
                val hasHlg = types.contains(Display.HdrCapabilities.HDR_TYPE_HLG)
                val hasDolbyVision = types.contains(Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION)

                HdrDisplayInfo(
                    isHdrSupported = types.isNotEmpty(),
                    supportsHdr10 = hasHdr10,
                    supportsHdr10Plus = hasHdr10Plus,
                    supportsHlg = hasHlg,
                    supportsDolbyVision = hasDolbyVision
                )
            } else {
                HdrDisplayInfo(false, false, false, false, false)
            }
        } else {
            HdrDisplayInfo(false, false, false, false, false)
        }
    }

    fun inspectFullHdrMetadata(pathOrUri: String): HdrMetadataInfo {
        var mimeType: String? = null
        var colorTransfer = -1
        var colorStandard = -1

        try {
            val retriever = MediaMetadataRetriever()
            if (pathOrUri.startsWith("content://")) {
                retriever.setDataSource(context, Uri.parse(pathOrUri))
            } else {
                retriever.setDataSource(pathOrUri)
            }

            mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                colorTransfer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_TRANSFER)?.toIntOrNull() ?: -1
                colorStandard = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_STANDARD)?.toIntOrNull() ?: -1
            }
            retriever.release()
        } catch (_: Exception) { }

        val displayInfo = inspectDisplayHdrCapabilities()
        val hasDvDecoder = inspectDolbyVisionDecoderSupport()
        val hasTenBitDecoder = inspectTenBitDecoderSupport()

        var format = HdrFormat.SDR
        var bitDepth = 8
        var transferLabel = "SDR (BT.709)"
        var standardLabel = "BT.709"

        if (mimeType != null && mimeType.equals("video/dolby-vision", ignoreCase = true)) {
            bitDepth = 10
            standardLabel = "BT.2020"
            transferLabel = "SMPTE ST 2084 (Dolby Vision)"
            format = if (displayInfo.supportsDolbyVision && hasDvDecoder) HdrFormat.DOLBY_VISION else HdrFormat.HDR10
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && colorTransfer == MediaFormat.COLOR_TRANSFER_ST2084) {
            bitDepth = 10
            standardLabel = "BT.2020"
            transferLabel = "SMPTE ST 2084 (PQ / HDR10)"
            format = if (displayInfo.supportsHdr10Plus) HdrFormat.HDR10_PLUS else HdrFormat.HDR10
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && colorTransfer == MediaFormat.COLOR_TRANSFER_HLG) {
            bitDepth = 10
            standardLabel = "BT.2020"
            transferLabel = "ARIB STD-B67 (HLG)"
            format = HdrFormat.HLG
        } else if (colorStandard == MediaFormat.COLOR_STANDARD_BT2020) {
            bitDepth = 10
            standardLabel = "BT.2020"
            transferLabel = "10-bit Wide Color"
            format = HdrFormat.TEN_BIT
        }

        val isNativePlayable = displayInfo.isHdrSupported && (hasTenBitDecoder || hasDvDecoder) && format != HdrFormat.SDR
        val fallbackNote = if (!displayInfo.isHdrSupported && format != HdrFormat.SDR) {
            "Display does not support hardware HDR. Falling back to SDR tone mapping."
        } else null

        return HdrMetadataInfo(
            hdrFormat = format,
            bitDepth = bitDepth,
            colorTransfer = transferLabel,
            colorStandard = standardLabel,
            isNativeHdrPlayable = isNativePlayable,
            isDolbyVisionNative = (format == HdrFormat.DOLBY_VISION),
            fallbackMessage = fallbackNote
        )
    }

    fun detectHdrFormat(pathOrUri: String): HdrFormat {
        return inspectFullHdrMetadata(pathOrUri).hdrFormat
    }

    fun inspectDolbyVisionDecoderSupport(): Boolean {
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                if (info.supportedTypes.any { it.equals("video/dolby-vision", ignoreCase = true) }) {
                    val isSoftware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        info.isSoftwareOnly
                    } else {
                        info.name.startsWith("OMX.google.") || info.name.startsWith("c2.android.")
                    }
                    if (!isSoftware) return true
                }
            }
        } catch (_: Exception) { }
        return false
    }

    fun inspectTenBitDecoderSupport(): Boolean {
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                for (type in info.supportedTypes) {
                    if (type.startsWith("video/")) {
                        val caps = try {
                            info.getCapabilitiesForType(type)
                        } catch (_: Exception) {
                            null
                        }

                        if (caps?.profileLevels != null) {
                            for (profileLevel in caps.profileLevels) {
                                if (profileLevel.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10 ||
                                    profileLevel.profile == MediaCodecInfo.CodecProfileLevel.VP9Profile2HDR
                                ) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        return false
    }
}
