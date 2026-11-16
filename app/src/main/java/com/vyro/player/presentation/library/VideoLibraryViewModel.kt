package com.vyro.player.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.usecase.GetVideosUseCase
import com.vyro.player.domain.usecase.ScanMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoLibraryUiState(
    val videos: List<VideoItem> = emptyList(),
    val folders: List<MediaFolder> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VideoLibraryViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val scanMediaUseCase: ScanMediaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoLibraryUiState())
    val uiState: StateFlow<VideoLibraryUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getVideosUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage
                    )
                }
                .collect { videos ->
                    val folders = videos.groupBy { it.folderName }.map { (name, folderVideos) ->
                        MediaFolder(
                            name = name,
                            path = folderVideos.firstOrNull()?.path?.substringBeforeLast('/') ?: "",
                            videoCount = folderVideos.size,
                            totalSize = folderVideos.sumOf { it.size }
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        videos = videos,
                        folders = folders,
                        isLoading = false
                    )
                }
        }
    }

    fun scanMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                scanMediaUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            } finally {
                _uiState.value = _uiState.value.copy(isScanning = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
