package com.vyro.player.domain.model

data class AudioTrackInfo(
    val id: String,
    val language: String,
    val label: String,
    val mimeType: String,
    val channelCount: Int = 2,
    val sampleRate: Int = 44100,
    val bitrate: Int = 0,
    val groupIndex: Int = 0,
    val trackIndex: Int = 0,
    val isSelected: Boolean = false
) {
    val channelLayoutLabel: String
        get() = when (channelCount) {
            1 -> "Mono (1.0)"
            2 -> "Stereo (2.0)"
            6 -> "5.1 Surround"
            8 -> "7.1 Surround"
            else -> "$channelCount Channels"
        }
}
