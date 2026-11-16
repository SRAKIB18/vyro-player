package com.vyro.player.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.data.database.entity.PlaybackHistoryEntity
import com.vyro.player.domain.usecase.ClearRecentHistoryUseCase
import com.vyro.player.domain.usecase.GetRecentHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecentVideosUiState(
    val history: List<PlaybackHistoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RecentVideosViewModel @Inject constructor(
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase,
    private val clearRecentHistoryUseCase: ClearRecentHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentVideosUiState())
    val uiState: StateFlow<RecentVideosUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = RecentVideosUiState(isLoading = true)
            getRecentHistoryUseCase()
                .catch { _uiState.value = RecentVideosUiState(isLoading = false) }
                .collect { history ->
                    _uiState.value = RecentVideosUiState(history = history, isLoading = false)
                }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clearRecentHistoryUseCase()
        }
    }
}
