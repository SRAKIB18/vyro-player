package com.vyro.player.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.model.ThemeMode
import com.vyro.player.domain.usecase.ClearRecentHistoryUseCase
import com.vyro.player.domain.usecase.GetSettingsUseCase
import com.vyro.player.domain.usecase.SaveSettingsUseCase
import com.vyro.player.domain.usecase.ScanMediaUseCase
import com.vyro.player.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val clearRecentHistoryUseCase: ClearRecentHistoryUseCase,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val settingsState: StateFlow<PlayerSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerSettings()
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            saveSettingsUseCase(settingsState.value.copy(themeMode = themeMode))
        }
    }

    fun updateSettings(newSettings: PlayerSettings) {
        viewModelScope.launch {
            saveSettingsUseCase(newSettings)
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            settingsManager.clearThumbnailCache()
        }
    }

    fun scanMedia() {
        viewModelScope.launch {
            scanMediaUseCase()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clearRecentHistoryUseCase()
        }
    }
}
