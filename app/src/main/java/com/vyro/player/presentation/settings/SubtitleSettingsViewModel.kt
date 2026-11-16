package com.vyro.player.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.SubtitleSettings
import com.vyro.player.domain.usecase.GetSubtitleSettingsUseCase
import com.vyro.player.domain.usecase.SaveSubtitleSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubtitleSettingsViewModel @Inject constructor(
    getSubtitleSettingsUseCase: GetSubtitleSettingsUseCase,
    private val saveSubtitleSettingsUseCase: SaveSubtitleSettingsUseCase
) : ViewModel() {

    val subtitleSettingsState: StateFlow<SubtitleSettings> = getSubtitleSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubtitleSettings()
        )

    fun updateSubtitleSettings(settings: SubtitleSettings) {
        viewModelScope.launch {
            saveSubtitleSettingsUseCase(settings)
        }
    }
}
