package com.vyro.player.presentation.player

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.AudioTrackInfo
import com.vyro.player.domain.model.OrientationLock
import com.vyro.player.domain.model.SubtitleTrack
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

    fun play() {
        playerManager.play()
    }

    fun pause() {
        playerManager.pause()
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun forwardBy(seconds: Int = 10) {
        playerManager.forwardBy(seconds)
    }

    fun rewindBy(seconds: Int = 10) {
        playerManager.rewindBy(seconds)
    }

    fun stepFrameForward() {
        playerManager.stepFrameForward()
    }

    fun stepFrameBackward() {
        playerManager.stepFrameBackward()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun setSpeedBoosted(boosted: Boolean, boostSpeed: Float = 2.0f) {
        playerManager.setSpeedBoosted(boosted, boostSpeed)
    }

    fun enterPipMode(activity: Activity) {
        playerManager.enterPipMode(activity)
    }

    fun setInPipMode(inPip: Boolean) {
        playerManager.setInPipMode(inPip)
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        playerManager.setAspectRatio(mode)
    }

    fun setZoomScale(scale: Float) {
        playerManager.setZoomScale(scale)
    }

    fun setRotationAngle(angle: Int) {
        playerManager.setRotationAngle(angle)
    }

    fun setOrientationLock(lock: OrientationLock) {
        playerManager.setOrientationLock(lock)
    }

    fun toggleScreenLock() {
        playerManager.toggleScreenLock()
    }

    fun selectAudioTrack(track: AudioTrackInfo) {
        playerManager.selectAudioTrack(track)
    }

    fun setAudioDelay(delayMs: Long) {
        playerManager.setAudioDelay(delayMs)
    }

    fun loadExternalSubtitle(uri: Uri, label: String = "External Subtitle") {
        playerManager.loadExternalSubtitle(uri, label)
    }

    fun selectSubtitleTrack(track: SubtitleTrack?) {
        playerManager.selectSubtitleTrack(track)
    }

    fun setSubtitleDelay(delayMs: Long) {
        playerManager.setSubtitleDelay(delayMs)
    }

    fun setAbRepeatA() {
        playerManager.setAbRepeatA()
    }

    fun setAbRepeatB() {
        playerManager.setAbRepeatB()
    }

    fun clearAbRepeat() {
        playerManager.clearAbRepeat()
    }

    fun setVolumeLevel(level: Float) {
        playerManager.setVolumeLevel(level)
    }

    fun setBrightnessLevel(level: Float) {
        playerManager.setBrightnessLevel(level)
    }

    fun toggleStatsOverlay() {
        playerManager.toggleStatsOverlay()
    }

    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    fun retry() {
        playerManager.retry()
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
