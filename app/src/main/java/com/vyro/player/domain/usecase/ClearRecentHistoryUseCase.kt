package com.vyro.player.domain.usecase

import com.vyro.player.data.database.dao.PlaybackHistoryDao
import javax.inject.Inject

class ClearRecentHistoryUseCase @Inject constructor(
    private val playbackHistoryDao: PlaybackHistoryDao
) {
    suspend operator fun invoke() = playbackHistoryDao.clearHistory()
}
