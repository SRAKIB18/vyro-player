package com.vyro.player.settings

import android.content.Context
import android.content.SharedPreferences
import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.model.SubtitleSettings
import com.vyro.player.domain.model.ThemeMode
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

    private val _subtitleSettingsState = MutableStateFlow(loadSubtitleSettings())
    val subtitleSettingsState: StateFlow<SubtitleSettings> = _subtitleSettingsState.asStateFlow()

    private fun loadSettings(): PlayerSettings {
        val themeOrdinal = prefs.getInt("theme_mode", ThemeMode.DARK.ordinal)
        return PlayerSettings(
            useHardwareAcceleration = prefs.getBoolean("hw_accel", true),
            autoPlayNext = prefs.getBoolean("auto_play_next", true),
            savePlaybackPosition = prefs.getBoolean("save_position", true),
            allowBackgroundPlay = prefs.getBoolean("bg_play", false),
            gestureVolumeEnabled = prefs.getBoolean("gesture_vol", true),
            gestureBrightnessEnabled = prefs.getBoolean("gesture_bright", true),
            defaultSpeed = prefs.getFloat("default_speed", 1.0f),
            themeMode = ThemeMode.values().getOrElse(themeOrdinal) { ThemeMode.DARK }
        )
    }

    private fun loadSubtitleSettings(): SubtitleSettings {
        return SubtitleSettings(
            language = prefs.getString("sub_lang", "English") ?: "English",
            fontSizeSp = prefs.getInt("sub_font_size", 18),
            fontColorHex = prefs.getString("sub_font_color", "#FFFFFF") ?: "#FFFFFF",
            backgroundColorHex = prefs.getString("sub_bg_color", "#80000000") ?: "#80000000",
            backgroundOpacity = prefs.getFloat("sub_bg_opacity", 0.5f),
            delayMs = prefs.getLong("sub_delay", 0L)
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
            .putInt("theme_mode", newSettings.themeMode.ordinal)
            .apply()
        _settingsState.value = newSettings
    }

    fun updateSubtitleSettings(newSettings: SubtitleSettings) {
        prefs.edit()
            .putString("sub_lang", newSettings.language)
            .putInt("sub_font_size", newSettings.fontSizeSp)
            .putString("sub_font_color", newSettings.fontColorHex)
            .putString("sub_bg_color", newSettings.backgroundColorHex)
            .putFloat("sub_bg_opacity", newSettings.backgroundOpacity)
            .putLong("sub_delay", newSettings.delayMs)
            .apply()
        _subtitleSettingsState.value = newSettings
    }
}
