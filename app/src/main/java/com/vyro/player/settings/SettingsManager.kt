package com.vyro.player.settings

import android.content.Context
import android.content.SharedPreferences
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.model.SubtitleSettings
import com.vyro.player.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
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
        val aspectOrdinal = prefs.getInt("default_aspect", AspectRatioMode.FIT.ordinal)

        return PlayerSettings(
            useHardwareAcceleration = prefs.getBoolean("hw_accel", true),
            autoPlayNext = prefs.getBoolean("auto_play_next", true),
            savePlaybackPosition = prefs.getBoolean("save_position", true),
            allowBackgroundPlay = prefs.getBoolean("bg_play", false),
            gestureVolumeEnabled = prefs.getBoolean("gesture_vol", true),
            gestureBrightnessEnabled = prefs.getBoolean("gesture_bright", true),
            defaultSpeed = prefs.getFloat("default_speed", 1.0f),
            seekIntervalSeconds = prefs.getInt("seek_interval", 10),
            doubleTapSeekSeconds = prefs.getInt("double_tap_seek", 10),
            longPressSpeedBoost = prefs.getFloat("long_press_boost", 2.0f),
            hideFileSizeAndDuration = prefs.getBoolean("hide_meta", false),
            defaultRotation = prefs.getInt("default_rotation", 0),
            defaultAspectRatio = AspectRatioMode.entries.getOrElse(aspectOrdinal) { AspectRatioMode.FIT },
            defaultDecoderPreference = prefs.getString("decoder_pref", "HARDWARE_FALLBACK") ?: "HARDWARE_FALLBACK",
            screenLockDefault = prefs.getBoolean("lock_default", false),
            appLanguage = prefs.getString("app_lang", "System Default") ?: "System Default",
            themeMode = ThemeMode.entries.getOrElse(themeOrdinal) { ThemeMode.DARK }
        )
    }

    private fun loadSubtitleSettings(): SubtitleSettings {
        return SubtitleSettings(
            language = prefs.getString("sub_lang", "English") ?: "English",
            fontSizeSp = prefs.getInt("sub_font_size", 18),
            fontFamily = prefs.getString("sub_font_family", "DEFAULT") ?: "DEFAULT",
            fontColorHex = prefs.getString("sub_font_color", "#FFFFFF") ?: "#FFFFFF",
            backgroundColorHex = prefs.getString("sub_bg_color", "#80000000") ?: "#80000000",
            backgroundOpacity = prefs.getFloat("sub_bg_opacity", 0.5f),
            isBold = prefs.getBoolean("sub_is_bold", false),
            hasOutline = prefs.getBoolean("sub_has_outline", true),
            verticalPosition = prefs.getString("sub_vert_pos", "BOTTOM") ?: "BOTTOM",
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
            .putInt("seek_interval", newSettings.seekIntervalSeconds)
            .putInt("double_tap_seek", newSettings.doubleTapSeekSeconds)
            .putFloat("long_press_boost", newSettings.longPressSpeedBoost)
            .putBoolean("hide_meta", newSettings.hideFileSizeAndDuration)
            .putInt("default_rotation", newSettings.defaultRotation)
            .putInt("default_aspect", newSettings.defaultAspectRatio.ordinal)
            .putString("decoder_pref", newSettings.defaultDecoderPreference)
            .putBoolean("lock_default", newSettings.screenLockDefault)
            .putString("app_lang", newSettings.appLanguage)
            .putInt("theme_mode", newSettings.themeMode.ordinal)
            .apply()
        _settingsState.value = newSettings
    }

    fun updateSubtitleSettings(newSettings: SubtitleSettings) {
        prefs.edit()
            .putString("sub_lang", newSettings.language)
            .putInt("sub_font_size", newSettings.fontSizeSp)
            .putString("sub_font_family", newSettings.fontFamily)
            .putString("sub_font_color", newSettings.fontColorHex)
            .putString("sub_bg_color", newSettings.backgroundColorHex)
            .putFloat("sub_bg_opacity", newSettings.backgroundOpacity)
            .putBoolean("sub_is_bold", newSettings.isBold)
            .putBoolean("sub_has_outline", newSettings.hasOutline)
            .putString("sub_vert_pos", newSettings.verticalPosition)
            .putLong("sub_delay", newSettings.delayMs)
            .apply()
        _subtitleSettingsState.value = newSettings
    }

    suspend fun clearThumbnailCache(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val thumbDir = File(context.cacheDir, "vyro_thumbnails")
            if (thumbDir.exists()) {
                thumbDir.deleteRecursively()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
