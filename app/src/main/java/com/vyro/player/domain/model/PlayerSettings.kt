package com.vyro.player.domain.model

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

data class PlayerSettings(
    val useHardwareAcceleration: Boolean = true,
    val autoPlayNext: Boolean = true,
    val savePlaybackPosition: Boolean = true,
    val allowBackgroundPlay: Boolean = false,
    val gestureVolumeEnabled: Boolean = true,
    val gestureBrightnessEnabled: Boolean = true,
    val defaultSpeed: Float = 1.0f,
    val seekIntervalSeconds: Int = 10,
    val doubleTapSeekSeconds: Int = 10,
    val longPressSpeedBoost: Float = 2.0f,
    val hideFileSizeAndDuration: Boolean = false,
    val defaultRotation: Int = 0,
    val defaultAspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val defaultDecoderPreference: String = "HARDWARE_FALLBACK",
    val screenLockDefault: Boolean = false,
    val appLanguage: String = "System Default",
    val themeMode: ThemeMode = ThemeMode.DARK
)
