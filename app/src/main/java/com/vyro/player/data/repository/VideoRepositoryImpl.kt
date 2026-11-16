package com.vyro.player.data.repository

import android.net.Uri
import com.vyro.player.data.database.dao.PlaybackHistoryDao
import com.vyro.player.data.database.dao.VideoDao
import com.vyro.player.data.database.entity.PlaybackHistoryEntity
import com.vyro.player.data.database.entity.VideoEntity
import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.repository.VideoRepository
import com.vyro.player.scanner.MediaScanner
import com.vyro.player.scanner.MediaStoreObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val mediaScanner: MediaScanner,
    private val mediaStoreObserver: MediaStoreObserver
) : VideoRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        mediaStoreObserver.startObserving()
        scope.launch {
            mediaStoreObserver.mediaChangeEvent.collect {
                scanMedia()
            }
        }
    }

    override fun getAllVideos(): Flow<List<VideoItem>> {
        return videoDao.getAllVideos().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFavoriteVideos(): Flow<List<VideoItem>> {
        return videoDao.getFavoriteVideos().map { entities ->
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
        val video = videoDao.getVideoById(id)
        if (video != null) {
            playbackHistoryDao.insertHistory(
                PlaybackHistoryEntity(
                    videoId = video.id,
                    videoTitle = video.title,
                    videoPath = video.path,
                    lastPosition = positionMs,
                    totalDuration = video.duration,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
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
                    width = item.width,
                    height = item.height,
                    resolution = item.resolution,
                    bitrate = item.bitrate,
                    mimeType = item.mimeType,
                    dateAdded = item.dateAdded,
                    dateModified = item.dateModified,
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

    override suspend fun scanSafUri(treeUri: Uri): Int = withContext(Dispatchers.IO) {
        val safVideos = mediaScanner.scanSafUriTree(treeUri)
        if (safVideos.isNotEmpty()) {
            val entities = safVideos.map { item ->
                val existing = videoDao.getVideoById(item.id)
                VideoEntity(
                    id = item.id,
                    title = item.title,
                    path = item.path,
                    duration = item.duration,
                    size = item.size,
                    width = item.width,
                    height = item.height,
                    resolution = item.resolution,
                    bitrate = item.bitrate,
                    mimeType = item.mimeType,
                    dateAdded = item.dateAdded,
                    dateModified = item.dateModified,
                    folderName = item.folderName,
                    lastPlayedPosition = existing?.lastPlayedPosition ?: 0L,
                    isFavorite = existing?.isFavorite ?: false,
                    thumbnailPath = item.thumbnailPath
                )
            }
            videoDao.insertVideos(entities)
        }
        safVideos.size
    }

    private fun VideoEntity.toDomainModel() = VideoItem(
        id = id,
        title = title,
        path = path,
        duration = duration,
        size = size,
        width = width,
        height = height,
        resolution = resolution,
        bitrate = bitrate,
        mimeType = mimeType,
        dateAdded = dateAdded,
        dateModified = dateModified,
        folderName = folderName,
        lastPlayedPosition = lastPlayedPosition,
        isFavorite = isFavorite,
        thumbnailPath = thumbnailPath
    )
}
