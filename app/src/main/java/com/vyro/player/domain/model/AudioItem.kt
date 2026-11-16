package com.vyro.player.domain.model

data class AudioItem(
    val id: Long,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val duration: Long,
    val size: Long,
    val path: String,
    val albumArtUri: String? = null,
    val isFavorite: Boolean = false,
    val dateAdded: Long = 0L
)
