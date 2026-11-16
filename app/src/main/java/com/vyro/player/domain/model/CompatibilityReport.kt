package com.vyro.player.domain.model

enum class CompatibilityStatus {
    FULLY_SUPPORTED_HW,
    SUPPORTED_SW_FALLBACK,
    UNSUPPORTED_VIDEO_CODEC,
    UNSUPPORTED_AUDIO_CODEC,
    UNSUPPORTED_CONTAINER,
    UNREADABLE_FILE
}

data class CompatibilityReport(
    val filePath: String,
    val containerFormat: String,
    val videoMimeType: String?,
    val videoCodecName: String,
    val audioMimeType: String?,
    val audioCodecName: String,
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val bitrate: Long,
    val isContainerSupported: Boolean,
    val isVideoSupported: Boolean,
    val isAudioSupported: Boolean,
    val hasHardwareVideoDecoder: Boolean,
    val hasSoftwareVideoDecoder: Boolean,
    val hasHardwareAudioDecoder: Boolean,
    val hasSoftwareAudioDecoder: Boolean,
    val compatibilityStatus: CompatibilityStatus,
    val statusMessage: String
)
