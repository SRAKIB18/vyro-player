package com.vyro.player.domain.model

data class PlayerSettings(
    val useHardwareAcceleration: Boolean = true,
    val autoPlayNext: Boolean = true,
    val savePlaybackPosition: Boolean = true,
    val allowBackgroundPlay: Boolean = false,
    val gestureVolumeEnabled: Boolean = true,
    val gestureBrightnessEnabled: Boolean = true,
    val defaultSpeed: Float = 1.0f
)
