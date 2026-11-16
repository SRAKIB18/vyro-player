package com.vyro.player.presentation.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.usecase.GetVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoldersUiState(
    val folders: List<MediaFolder> = emptyList(),
    val allVideos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoldersUiState())
    val uiState: StateFlow<FoldersUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getVideosUseCase()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
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
                        folders = folders,
                        allVideos = videos,
                        isLoading = false
                    )
                }
        }
    }

    fun getVideosForFolder(folderName: String): List<VideoItem> {
        return _uiState.value.allVideos.filter { it.folderName == folderName }
    }
}
