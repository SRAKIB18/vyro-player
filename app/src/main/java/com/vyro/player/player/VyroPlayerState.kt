package com.vyro.player.player

import com.vyro.player.domain.model.AspectRatioMode

data class VyroPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val title: String = "",
    val error: String? = null
)
