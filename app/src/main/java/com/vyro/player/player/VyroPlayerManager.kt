package com.vyro.player.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.VideoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VyroPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playerState = MutableStateFlow(VyroPlayerState())
    val playerState: StateFlow<VyroPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressTrackerJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                _playerState.value = _playerState.value.copy(
                    isBuffering = isBuffering,
                    duration = player.duration.coerceAtLeast(0L)
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                _playerState.value = _playerState.value.copy(
                    error = error.localizedMessage ?: "Playback Error"
                )
            }
        })
    }

    fun prepareAndPlay(videoItem: VideoItem, startPositionMs: Long = 0L) {
        val uri = if (videoItem.path.startsWith("content://") || videoItem.path.startsWith("file://")) {
            Uri.parse(videoItem.path)
        } else {
            Uri.fromFile(java.io.File(videoItem.path))
        }

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        if (startPositionMs > 0L) {
            player.seekTo(startPositionMs)
        }
        player.prepare()
        player.playWhenReady = true

        _playerState.value = _playerState.value.copy(
            title = videoItem.title,
            currentPosition = startPositionMs,
            duration = videoItem.duration,
            error = null
        )
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPosition = positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _playerState.value = _playerState.value.copy(aspectRatio = mode)
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = scope.launch {
            while (isActive) {
                _playerState.value = _playerState.value.copy(
                    currentPosition = player.currentPosition.coerceAtLeast(0L),
                    duration = player.duration.coerceAtLeast(0L)
                )
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    fun release() {
        stopProgressTracker()
        player.release()
    }
}
