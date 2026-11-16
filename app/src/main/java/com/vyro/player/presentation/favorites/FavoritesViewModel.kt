package com.vyro.player.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.usecase.GetFavoriteVideosUseCase
import com.vyro.player.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteVideosUseCase: GetFavoriteVideosUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState(isLoading = true)
            getFavoriteVideosUseCase()
                .catch { _uiState.value = FavoritesUiState(isLoading = false) }
                .collect { list ->
                    _uiState.value = FavoritesUiState(favorites = list, isLoading = false)
                }
        }
    }

    fun toggleFavorite(videoId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(videoId)
        }
    }
}
