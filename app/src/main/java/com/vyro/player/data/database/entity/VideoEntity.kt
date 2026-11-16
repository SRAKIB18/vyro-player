package com.vyro.player.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val resolution: String,
    val mimeType: String,
    val dateAdded: Long,
    val folderName: String,
    val lastPlayedPosition: Long = 0L,
    val isFavorite: Boolean = false,
    val thumbnailPath: String? = null
)
