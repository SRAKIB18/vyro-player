package com.vyro.player.presentation.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.AudioAlbum
import com.vyro.player.domain.model.AudioArtist
import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.usecase.GetAudioTracksUseCase
import com.vyro.player.domain.usecase.ScanAudioMediaUseCase
import com.vyro.player.domain.usecase.ToggleAudioFavoriteUseCase
import com.vyro.player.player.VyroPlayerManager
import com.vyro.player.player.VyroPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicUiState(
    val songs: List<AudioItem> = emptyList(),
    val albums: List<AudioAlbum> = emptyList(),
    val artists: List<AudioArtist> = emptyList(),
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val getAudioTracksUseCase: GetAudioTracksUseCase,
    private val scanAudioMediaUseCase: ScanAudioMediaUseCase,
    private val toggleAudioFavoriteUseCase: ToggleAudioFavoriteUseCase,
    private val playerManager: VyroPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<VyroPlayerState> = playerManager.playerState

    init {
        loadMusicData()
    }

    fun loadMusicData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getAudioTracksUseCase()
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage) }
                .collect { tracks ->
                    val albums = tracks.groupBy { it.album }.map { (name, songs) ->
                        AudioAlbum(
                            name = name,
                            artist = songs.firstOrNull()?.artist ?: "Unknown Artist",
                            songCount = songs.size,
                            albumArtUri = songs.firstOrNull()?.albumArtUri
                        )
                    }
                    val artists = tracks.groupBy { it.artist }.map { (name, songs) ->
                        AudioArtist(
                            name = name,
                            songCount = songs.size,
                            albumCount = songs.map { it.album }.distinct().size
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        songs = tracks,
                        albums = albums,
                        artists = artists,
                        isLoading = false
                    )
                }
        }
    }

    fun playAudio(audioItem: AudioItem) {
        playerManager.prepareAndPlayAudio(audioItem)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun scanAudioMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                scanAudioMediaUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            } finally {
                _uiState.value = _uiState.value.copy(isScanning = false)
            }
        }
    }

    fun toggleFavorite(audioId: Long) {
        viewModelScope.launch {
            toggleAudioFavoriteUseCase(audioId)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
