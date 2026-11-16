package com.vyro.player.presentation.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.ResolutionFilter
import com.vyro.player.domain.model.SortOption
import com.vyro.player.domain.model.SortOrder
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.model.ViewMode
import com.vyro.player.domain.usecase.GetVideosUseCase
import com.vyro.player.domain.usecase.ScanMediaUseCase
import com.vyro.player.domain.usecase.ScanSafUriUseCase
import com.vyro.player.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllVideosUiState(
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOption: SortOption = SortOption.DATE,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val resolutionFilter: ResolutionFilter = ResolutionFilter.ALL,
    val showSortDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AllVideosViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val scanSafUriUseCase: ScanSafUriUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllVideosUiState())
    val uiState: StateFlow<AllVideosUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getVideosUseCase()
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage) }
                .collect { videos ->
                    val filteredByRes = filterVideosByResolution(videos, _uiState.value.resolutionFilter)
                    val sorted = sortVideos(filteredByRes, _uiState.value.sortOption, _uiState.value.sortOrder)
                    _uiState.value = _uiState.value.copy(videos = sorted, isLoading = false)
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

    fun scanSafUri(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                scanSafUriUseCase(treeUri)
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

    fun setResolutionFilter(filter: ResolutionFilter) {
        _uiState.value = _uiState.value.copy(resolutionFilter = filter)
        loadVideos()
    }

    private fun filterVideosByResolution(list: List<VideoItem>, filter: ResolutionFilter): List<VideoItem> {
        return when (filter) {
            ResolutionFilter.ALL -> list
            ResolutionFilter.RES_4K -> list.filter { it.resolution == "4K" || it.resolution == "2K" }
            ResolutionFilter.RES_1080P -> list.filter { it.resolution == "1080p" }
            ResolutionFilter.RES_720P -> list.filter { it.resolution == "720p" }
            ResolutionFilter.RES_SD -> list.filter { it.resolution == "SD" || it.resolution == "480p" }
        }
    }

    private fun sortVideos(list: List<VideoItem>, option: SortOption, order: SortOrder): List<VideoItem> {
        val comparator = when (option) {
            SortOption.NAME -> compareBy<VideoItem> { it.title }
            SortOption.DATE -> compareBy { it.dateAdded }
            SortOption.SIZE -> compareBy { it.size }
            SortOption.DURATION -> compareBy { it.duration }
            SortOption.RESOLUTION -> compareBy { maxOf(it.width, it.height) }
        }
        return if (order == SortOrder.ASCENDING) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
    }
}
