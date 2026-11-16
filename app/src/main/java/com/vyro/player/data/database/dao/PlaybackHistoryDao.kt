package com.vyro.player.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vyro.player.data.database.entity.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC")
    fun getRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
