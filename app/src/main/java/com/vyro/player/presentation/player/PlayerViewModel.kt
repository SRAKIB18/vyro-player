package com.vyro.player.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.repository.VideoRepository
import com.vyro.player.domain.usecase.UpdatePlaybackPositionUseCase
import com.vyro.player.player.VyroPlayerManager
import com.vyro.player.player.VyroPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: VyroPlayerManager,
    private val videoRepository: VideoRepository,
    private val updatePlaybackPositionUseCase: UpdatePlaybackPositionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val playerState: StateFlow<VyroPlayerState> = playerManager.playerState
    val exoPlayer = playerManager.player

    private var currentVideo: VideoItem? = null

    init {
        val videoIdStr: String? = savedStateHandle.get<String>("videoId")
        val videoId = videoIdStr?.toLongOrNull()
        if (videoId != null) {
            loadAndPlayVideo(videoId)
        }
    }

    private fun loadAndPlayVideo(videoId: Long) {
        viewModelScope.launch {
            val video = videoRepository.getVideoById(videoId)
            if (video != null) {
                currentVideo = video
                playerManager.prepareAndPlayVideo(video, video.lastPlayedPosition)
            }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        playerManager.setAspectRatio(mode)
    }

    fun saveCurrentPosition() {
        val video = currentVideo ?: return
        val currentPos = playerState.value.currentPosition
        viewModelScope.launch {
            updatePlaybackPositionUseCase(video.id, currentPos)
        }
    }

    override fun onCleared() {
        saveCurrentPosition()
        super.onCleared()
    }
}
