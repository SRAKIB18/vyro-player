package com.vyro.player.domain.repository

import com.vyro.player.domain.model.PlayerSettings
import com.vyro.player.domain.model.SubtitleSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<PlayerSettings>
    fun getSubtitleSettings(): Flow<SubtitleSettings>
    suspend fun updateSettings(settings: PlayerSettings)
    suspend fun updateSubtitleSettings(settings: SubtitleSettings)
}
