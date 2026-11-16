package com.vyro.player.domain.usecase

import com.vyro.player.domain.model.SubtitleSettings
import com.vyro.player.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubtitleSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<SubtitleSettings> = settingsRepository.getSubtitleSettings()
}
