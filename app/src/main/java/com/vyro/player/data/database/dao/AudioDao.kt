package com.vyro.player.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vyro.player.data.database.entity.AudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Query("SELECT * FROM audio_tracks ORDER BY title ASC")
    fun getAllAudioTracks(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteAudioTracks(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_tracks WHERE id = :id LIMIT 1")
    suspend fun getAudioById(id: Long): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioTracks(tracks: List<AudioEntity>)

    @Query("UPDATE audio_tracks SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleAudioFavorite(id: Long)

    @Query("DELETE FROM audio_tracks WHERE id NOT IN (:validIds)")
    suspend fun deleteStaleAudioTracks(validIds: List<Long>)
}
