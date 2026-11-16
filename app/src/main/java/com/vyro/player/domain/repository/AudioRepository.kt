package com.vyro.player.domain.repository

import com.vyro.player.domain.model.AudioAlbum
import com.vyro.player.domain.model.AudioArtist
import com.vyro.player.domain.model.AudioItem
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun getAllAudioTracks(): Flow<List<AudioItem>>
    fun getFavoriteAudioTracks(): Flow<List<AudioItem>>
    fun getAudioAlbums(): Flow<List<AudioAlbum>>
    fun getAudioArtists(): Flow<List<AudioArtist>>
    suspend fun getAudioById(id: Long): AudioItem?
    suspend fun toggleAudioFavorite(id: Long)
    suspend fun scanAudioMedia(): Int
}
