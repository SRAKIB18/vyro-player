package com.vyro.player.settings

import android.content.Context
import android.content.SharedPreferences
import com.vyro.player.domain.model.PlayerSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vyro_player_prefs", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<PlayerSettings> = _settingsState.asStateFlow()

    private fun loadSettings(): PlayerSettings {
        return PlayerSettings(
            useHardwareAcceleration = prefs.getBoolean("hw_accel", true),
            autoPlayNext = prefs.getBoolean("auto_play_next", true),
            savePlaybackPosition = prefs.getBoolean("save_position", true),
            allowBackgroundPlay = prefs.getBoolean("bg_play", false),
            gestureVolumeEnabled = prefs.getBoolean("gesture_vol", true),
            gestureBrightnessEnabled = prefs.getBoolean("gesture_bright", true),
            defaultSpeed = prefs.getFloat("default_speed", 1.0f)
        )
    }

    fun updateSettings(newSettings: PlayerSettings) {
        prefs.edit()
            .putBoolean("hw_accel", newSettings.useHardwareAcceleration)
            .putBoolean("auto_play_next", newSettings.autoPlayNext)
            .putBoolean("save_position", newSettings.savePlaybackPosition)
            .putBoolean("bg_play", newSettings.allowBackgroundPlay)
            .putBoolean("gesture_vol", newSettings.gestureVolumeEnabled)
            .putBoolean("gesture_bright", newSettings.gestureBrightnessEnabled)
            .putFloat("default_speed", newSettings.defaultSpeed)
            .apply()
        _settingsState.value = newSettings
    }
}
