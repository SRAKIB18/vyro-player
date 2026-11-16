package com.vyro.player.domain.model

data class VideoItem(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val resolution: String = "",
    val mimeType: String = "",
    val dateAdded: Long = 0L,
    val folderName: String = "",
    val lastPlayedPosition: Long = 0L,
    val isFavorite: Boolean = false,
    val thumbnailPath: String? = null
)
