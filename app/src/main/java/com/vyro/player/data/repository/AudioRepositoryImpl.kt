package com.vyro.player.data.repository

import com.vyro.player.data.database.dao.AudioDao
import com.vyro.player.data.database.entity.AudioEntity
import com.vyro.player.domain.model.AudioAlbum
import com.vyro.player.domain.model.AudioArtist
import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.repository.AudioRepository
import com.vyro.player.scanner.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val audioDao: AudioDao,
    private val mediaScanner: MediaScanner
) : AudioRepository {

    override fun getAllAudioTracks(): Flow<List<AudioItem>> {
        return audioDao.getAllAudioTracks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFavoriteAudioTracks(): Flow<List<AudioItem>> {
        return audioDao.getFavoriteAudioTracks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAudioAlbums(): Flow<List<AudioAlbum>> {
        return audioDao.getAllAudioTracks().map { entities ->
            entities.groupBy { it.album }.map { (albumName, songs) ->
                AudioAlbum(
                    name = albumName,
                    artist = songs.firstOrNull()?.artist ?: "Unknown Artist",
                    songCount = songs.size,
                    albumArtUri = songs.firstOrNull()?.albumArtUri
                )
            }
        }
    }

    override fun getAudioArtists(): Flow<List<AudioArtist>> {
        return audioDao.getAllAudioTracks().map { entities ->
            entities.groupBy { it.artist }.map { (artistName, songs) ->
                AudioArtist(
                    name = artistName,
                    songCount = songs.size,
                    albumCount = songs.map { it.album }.distinct().size
                )
            }
        }
    }

    override suspend fun getAudioById(id: Long): AudioItem? {
        return audioDao.getAudioById(id)?.toDomainModel()
    }

    override suspend fun toggleAudioFavorite(id: Long) {
        audioDao.toggleAudioFavorite(id)
    }

    override suspend fun scanAudioMedia(): Int = withContext(Dispatchers.IO) {
        val scannedAudio = mediaScanner.scanAudioTracks()
        if (scannedAudio.isNotEmpty()) {
            val entities = scannedAudio.map { item ->
                val existing = audioDao.getAudioById(item.id)
                AudioEntity(
                    id = item.id,
                    title = item.title,
                    artist = item.artist,
                    album = item.album,
                    duration = item.duration,
                    size = item.size,
                    path = item.path,
                    albumArtUri = item.albumArtUri,
                    isFavorite = existing?.isFavorite ?: false,
                    dateAdded = item.dateAdded
                )
            }
            audioDao.insertAudioTracks(entities)
            audioDao.deleteStaleAudioTracks(scannedAudio.map { it.id })
        }
        scannedAudio.size
    }

    private fun AudioEntity.toDomainModel() = AudioItem(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        size = size,
        path = path,
        albumArtUri = albumArtUri,
        isFavorite = isFavorite,
        dateAdded = dateAdded
    )
}
