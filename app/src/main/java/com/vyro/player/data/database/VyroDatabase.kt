package com.vyro.player.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vyro.player.data.database.dao.AudioDao
import com.vyro.player.data.database.dao.PlaybackHistoryDao
import com.vyro.player.data.database.dao.VideoDao
import com.vyro.player.data.database.entity.AudioEntity
import com.vyro.player.data.database.entity.PlaybackHistoryEntity
import com.vyro.player.data.database.entity.VideoEntity

@Database(
    entities = [VideoEntity::class, PlaybackHistoryEntity::class, AudioEntity::class],
    version = 4,
    exportSchema = false
)
abstract class VyroDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun audioDao(): AudioDao
}
