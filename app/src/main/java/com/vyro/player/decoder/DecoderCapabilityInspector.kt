package com.vyro.player.decoder

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import com.vyro.player.domain.model.DecoderCapability
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DecoderCapabilityInspector @Inject constructor() {

    fun getDecoderCapabilities(): List<DecoderCapability> {
        val capabilities = mutableListOf<DecoderCapability>()
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (codecInfo in codecList.codecInfos) {
                val supportedTypes = codecInfo.supportedTypes
                val isVideo = supportedTypes.any { it.startsWith("video/") }
                if (!isVideo) continue

                val isEncoder = codecInfo.isEncoder
                val isSoftwareOnly = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    codecInfo.isSoftwareOnly
                } else {
                    codecInfo.name.startsWith("OMX.google.") || codecInfo.name.startsWith("c2.android.")
                }
                val isHardware = !isSoftwareOnly

                for (type in supportedTypes) {
                    if (type.startsWith("video/")) {
                        val caps = try {
                            codecInfo.getCapabilitiesForType(type)
                        } catch (_: Exception) {
                            null
                        }

                        val isHdrSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && caps != null) {
                            caps.profileLevels?.any { profileLevel ->
                                profileLevel.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10 ||
                                        profileLevel.profile == MediaCodecInfo.CodecProfileLevel.VP9Profile2HDR
                            } ?: false
                        } else false

                        capabilities.add(
                            DecoderCapability(
                                codecName = codecInfo.name,
                                mimeType = type,
                                isHardwareAccelerated = isHardware,
                                isSoftwareOnly = isSoftwareOnly,
                                isEncoder = isEncoder,
                                maxSupportedResolution = "Supported",
                                isHdrSupported = isHdrSupported
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return capabilities
    }
}
