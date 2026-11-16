package com.vyro.player.player

import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.model.VideoItem

enum class MediaType {
    NONE,
    VIDEO,
    AUDIO
}

data class VyroPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val mediaType: MediaType = MediaType.NONE,
    val currentVideo: VideoItem? = null,
    val currentAudio: AudioItem? = null,
    val title: String = "",
    val subtitle: String = "",
    val error: String? = null
)
