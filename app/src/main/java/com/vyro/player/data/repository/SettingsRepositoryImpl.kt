package com.vyro.player.data.repository

import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.repository.SettingsRepository
import com.vyro.player.settings.SettingsManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsManager: SettingsManager
) : SettingsRepository {

    override fun getSettings(): Flow<PlayerSettings> = settingsManager.settingsState

    override suspend fun updateSettings(settings: PlayerSettings) {
        settingsManager.updateSettings(settings)
    }
}
