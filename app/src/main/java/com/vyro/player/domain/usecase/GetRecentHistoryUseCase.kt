package com.vyro.player.domain.usecase

import com.vyro.player.data.database.dao.PlaybackHistoryDao
import com.vyro.player.data.database.entity.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentHistoryUseCase @Inject constructor(
    private val playbackHistoryDao: PlaybackHistoryDao
) {
    operator fun invoke(): Flow<List<PlaybackHistoryEntity>> = playbackHistoryDao.getRecentHistory()
}
