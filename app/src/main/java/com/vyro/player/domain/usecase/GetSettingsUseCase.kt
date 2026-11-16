package com.vyro.player.domain.usecase

import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<PlayerSettings> = settingsRepository.getSettings()
}
