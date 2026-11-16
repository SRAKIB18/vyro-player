package com.vyro.player.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_tracks")
data class AudioEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val size: Long,
    val path: String,
    val albumArtUri: String? = null,
    val isFavorite: Boolean = false,
    val dateAdded: Long = 0L
)
