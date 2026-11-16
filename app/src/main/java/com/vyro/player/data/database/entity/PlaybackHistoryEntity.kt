package com.vyro.player.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val videoId: Long,
    val videoTitle: String,
    val videoPath: String,
    val lastPosition: Long,
    val totalDuration: Long,
    val timestamp: Long
)
