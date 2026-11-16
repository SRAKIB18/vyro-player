package com.vyro.player.data.repository

import com.vyro.player.data.database.dao.VideoDao
import com.vyro.player.data.database.entity.VideoEntity
import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.repository.VideoRepository
import com.vyro.player.scanner.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
    private val mediaScanner: MediaScanner
) : VideoRepository {

    override fun getAllVideos(): Flow<List<VideoItem>> {
        return videoDao.getAllVideos().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getVideosInFolder(folderName: String): Flow<List<VideoItem>> {
        return videoDao.getVideosInFolder(folderName).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFolders(): Flow<List<MediaFolder>> {
        return videoDao.getAllVideos().map { entities ->
            entities.groupBy { it.folderName }.map { (folderName, videos) ->
                MediaFolder(
                    name = folderName,
                    path = videos.firstOrNull()?.path?.substringBeforeLast('/') ?: "",
                    videoCount = videos.size,
                    totalSize = videos.sumOf { it.size }
                )
            }
        }
    }

    override suspend fun getVideoById(id: Long): VideoItem? {
        return videoDao.getVideoById(id)?.toDomainModel()
    }

    override suspend fun updatePlaybackPosition(id: Long, positionMs: Long) {
        videoDao.updatePlaybackPosition(id, positionMs)
    }

    override suspend fun toggleFavorite(id: Long) {
        videoDao.toggleFavorite(id)
    }

    override suspend fun scanMedia(): Int = withContext(Dispatchers.IO) {
        val scannedVideos = mediaScanner.scanVideos()
        if (scannedVideos.isNotEmpty()) {
            val entities = scannedVideos.map { item ->
                val existing = videoDao.getVideoById(item.id)
                VideoEntity(
                    id = item.id,
                    title = item.title,
                    path = item.path,
                    duration = item.duration,
                    size = item.size,
                    resolution = item.resolution,
                    mimeType = item.mimeType,
                    dateAdded = item.dateAdded,
                    folderName = item.folderName,
                    lastPlayedPosition = existing?.lastPlayedPosition ?: 0L,
                    isFavorite = existing?.isFavorite ?: false,
                    thumbnailPath = item.thumbnailPath
                )
            }
            videoDao.insertVideos(entities)
            videoDao.deleteStaleVideos(scannedVideos.map { it.id })
        }
        scannedVideos.size
    }

    private fun VideoEntity.toDomainModel() = VideoItem(
        id = id,
        title = title,
        path = path,
        duration = duration,
        size = size,
        resolution = resolution,
        mimeType = mimeType,
        dateAdded = dateAdded,
        folderName = folderName,
        lastPlayedPosition = lastPlayedPosition,
        isFavorite = isFavorite,
        thumbnailPath = thumbnailPath
    )
}
