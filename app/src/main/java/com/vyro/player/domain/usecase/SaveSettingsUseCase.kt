package com.vyro.player.domain.usecase

import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: PlayerSettings) {
        settingsRepository.updateSettings(settings)
    }
}
