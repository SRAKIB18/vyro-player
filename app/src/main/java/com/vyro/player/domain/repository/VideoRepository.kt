package com.vyro.player.domain.repository

import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.VideoItem
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getAllVideos(): Flow<List<VideoItem>>
    fun getFavoriteVideos(): Flow<List<VideoItem>>
    fun getVideosInFolder(folderName: String): Flow<List<VideoItem>>
    fun getFolders(): Flow<List<MediaFolder>>
    suspend fun getVideoById(id: Long): VideoItem?
    suspend fun updatePlaybackPosition(id: Long, positionMs: Long)
    suspend fun toggleFavorite(id: Long)
    suspend fun scanMedia(): Int
}
