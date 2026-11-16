package com.vyro.player.domain.model

data class PlaybackState(
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val currentVideoId: Long? = null,
    val currentVideoTitle: String = ""
)

enum class AspectRatioMode {
    FIT,
    FILL,
    ZOOM,
    RATIO_16_9,
    RATIO_4_3
}
