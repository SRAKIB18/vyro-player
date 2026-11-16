package com.vyro.player.domain.repository

import com.vyro.player.domain.model.PlayerSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<PlayerSettings>
    suspend fun updateSettings(settings: PlayerSettings)
}
