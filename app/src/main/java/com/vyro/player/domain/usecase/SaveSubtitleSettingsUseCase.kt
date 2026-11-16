package com.vyro.player.domain.usecase

import com.vyro.player.domain.model.SubtitleSettings
import com.vyro.player.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveSubtitleSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: SubtitleSettings) = settingsRepository.updateSubtitleSettings(settings)
}
