package com.vyro.player.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.data.database.entity.PlaybackHistoryEntity
import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.SortOption
import com.vyro.player.domain.model.SortOrder
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.model.ViewMode
import com.vyro.player.domain.usecase.GetRecentHistoryUseCase
import com.vyro.player.domain.usecase.GetVideosUseCase
import com.vyro.player.domain.usecase.ScanMediaUseCase
import com.vyro.player.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val videos: List<VideoItem> = emptyList(),
    val folders: List<MediaFolder> = emptyList(),
    val recentHistory: List<PlaybackHistoryEntity> = emptyList(),
    val continueWatchingVideo: VideoItem? = null,
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOption: SortOption = SortOption.DATE,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val showSortDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                getVideosUseCase()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage) }
                    .collect { rawVideos ->
                        val sorted = sortVideos(rawVideos, _uiState.value.sortOption, _uiState.value.sortOrder)
                        val folders = sorted.groupBy { it.folderName }.map { (name, items) ->
                            MediaFolder(
                                name = name,
                                path = items.firstOrNull()?.path?.substringBeforeLast('/') ?: "",
                                videoCount = items.size,
                                totalSize = items.sumOf { it.size }
                            )
                        }
                        val continueVideo = sorted.firstOrNull { it.lastPlayedPosition > 0L && it.lastPlayedPosition < it.duration }

                        _uiState.value = _uiState.value.copy(
                            videos = sorted,
                            folders = folders,
                            continueWatchingVideo = continueVideo,
                            isLoading = false
                        )
                    }
            }

            launch {
                getRecentHistoryUseCase()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage) }
                    .collect { history ->
                        _uiState.value = _uiState.value.copy(recentHistory = history.take(10))
                    }
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

    fun toggleFavorite(videoId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(videoId)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
        if (!active) _uiState.value = _uiState.value.copy(searchQuery = "")
    }

    fun toggleViewMode() {
        val nextMode = if (_uiState.value.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
        _uiState.value = _uiState.value.copy(viewMode = nextMode)
    }

    fun setSortDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showSortDialog = visible)
    }

    fun setSortOption(option: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = option)
        _uiState.value = _uiState.value.copy(videos = sortVideos(_uiState.value.videos, option, _uiState.value.sortOrder))
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
        _uiState.value = _uiState.value.copy(videos = sortVideos(_uiState.value.videos, _uiState.value.sortOption, order))
    }

    private fun sortVideos(list: List<VideoItem>, option: SortOption, order: SortOrder): List<VideoItem> {
        val comparator = when (option) {
            SortOption.NAME -> compareBy<VideoItem> { it.title }
            SortOption.DATE -> compareBy { it.dateAdded }
            SortOption.SIZE -> compareBy { it.size }
            SortOption.DURATION -> compareBy { it.duration }
        }
        return if (order == SortOrder.ASCENDING) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
    }
}
