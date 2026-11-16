package com.vyro.player.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaCodecList
import android.net.Uri
import android.os.Build
import android.util.Rational
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.vyro.player.decoder.AudioCapabilityInspector
import com.vyro.player.decoder.CodecCompatibilityInspector
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.model.AudioTrackInfo
import com.vyro.player.domain.model.CompatibilityStatus
import com.vyro.player.domain.model.OrientationLock
import com.vyro.player.domain.model.SubtitleTrack
import com.vyro.player.domain.model.VideoItem
import com.vyro.player.subtitle.SubtitleParser
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

@OptIn(UnstableApi::class)
@Singleton
class VyroPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val codecCompatibilityInspector: CodecCompatibilityInspector,
    private val audioCapabilityInspector: AudioCapabilityInspector,
    private val subtitleParser: SubtitleParser
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            buildUponParameters()
                .setSelectUndeterminedTextLanguage(true)
                .setTunnelingEnabled(true)
        )
    }

    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            20_000,
            60_000,
            3_000,
            6_000
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    private val renderersFactory = DefaultRenderersFactory(context).apply {
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        setEnableDecoderFallback(true)
    }

    val player: ExoPlayer = ExoPlayer.Builder(context, renderersFactory)
        .setTrackSelector(trackSelector)
        .setLoadControl(loadControl)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .build()

    private val _playerState = MutableStateFlow(VyroPlayerState())
    val playerState: StateFlow<VyroPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressTrackerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var totalDroppedFrames = 0
    private var preBoostSpeed = 1.0f

    // Audio Becoming Noisy Receiver (Headphone disconnect auto-pause)
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    init {
        val maxCap = inspectMaxHardwareDecoderCapability()
        val audioCaps = audioCapabilityInspector.inspectCapabilities()

        _playerState.value = _playerState.value.copy(
            maxSupportedResolution = maxCap,
            deviceAudioCapabilities = audioCaps
        )

        try {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            context.registerReceiver(noisyReceiver, filter)
        } catch (_: Exception) { }

        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onDroppedVideoFrames(
                eventTime: AnalyticsListener.EventTime,
                droppedFrames: Int,
                elapsedMs: Long
            ) {
                totalDroppedFrames += droppedFrames
                _playerState.value = _playerState.value.copy(droppedFrames = totalDroppedFrames)
            }
        })

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
                val isEnded = playbackState == Player.STATE_ENDED

                _playerState.value = _playerState.value.copy(
                    isBuffering = isBuffering,
                    isEnded = isEnded,
                    duration = player.duration.coerceAtLeast(0L)
                )
            }

            override fun onTracksChanged(tracks: Tracks) {
                val detectedSubtitles = mutableListOf<SubtitleTrack>()
                val detectedAudioTracks = mutableListOf<AudioTrackInfo>()

                for ((groupIndex, group) in tracks.groups.withIndex()) {
                    if (group.type == C.TRACK_TYPE_TEXT) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            val lang = format.language ?: "Undetermined"
                            val label = format.label ?: "Subtitle Track #${detectedSubtitles.size + 1} ($lang)"
                            val isSelected = group.isTrackSelected(i)

                            val track = SubtitleTrack(
                                id = "embedded_${detectedSubtitles.size}",
                                language = lang,
                                label = label,
                                mimeType = format.sampleMimeType ?: "text/vtt",
                                isExternal = false,
                                trackIndex = i,
                                isSelected = isSelected
                            )
                            detectedSubtitles.add(track)
                        }
                    } else if (group.type == C.TRACK_TYPE_AUDIO) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            val lang = format.language ?: "Undetermined"
                            val label = format.label ?: "Audio Track #${detectedAudioTracks.size + 1} ($lang)"
                            val isSelected = group.isTrackSelected(i)

                            val trackInfo = AudioTrackInfo(
                                id = "audio_${groupIndex}_$i",
                                language = lang,
                                label = label,
                                mimeType = format.sampleMimeType ?: "audio/raw",
                                channelCount = format.channelCount,
                                sampleRate = format.sampleRate,
                                bitrate = format.bitrate,
                                groupIndex = groupIndex,
                                trackIndex = i,
                                isSelected = isSelected
                            )
                            detectedAudioTracks.add(trackInfo)
                        }
                    }
                }

                _playerState.value = _playerState.value.copy(
                    availableSubtitles = detectedSubtitles,
                    selectedSubtitle = detectedSubtitles.firstOrNull { it.isSelected },
                    availableAudioTracks = detectedAudioTracks,
                    selectedAudioTrack = detectedAudioTracks.firstOrNull { it.isSelected }
                )
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                _playerState.value = _playerState.value.copy(
                    videoWidth = videoSize.width,
                    videoHeight = videoSize.height
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                val currentReport = _playerState.value.compatibilityReport
                val errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                        "Decoder initialization failed for ${currentReport?.videoCodecName ?: "media"}. Device maximum capability exceeded."
                    }
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Network connection failed. Please check your internet connection."
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Media file not found at path."
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                        "Container format unsupported (${currentReport?.containerFormat ?: "unknown"})."
                    }
                    else -> error.localizedMessage ?: "Playback Exception Occurred"
                }

                _playerState.value = _playerState.value.copy(
                    error = errorMessage,
                    isPlaying = false,
                    isBuffering = false
                )
            }
        })
    }

    fun prepareAndPlayVideo(videoItem: VideoItem, startPositionMs: Long = 0L) {
        val report = codecCompatibilityInspector.inspectFile(videoItem.path)
        totalDroppedFrames = 0

        _playerState.value = _playerState.value.copy(
            mediaType = MediaType.VIDEO,
            currentVideo = videoItem,
            currentAudio = null,
            compatibilityReport = report,
            title = videoItem.title,
            subtitle = "${videoItem.folderName} • ${report.videoCodecName}",
            currentPosition = startPositionMs,
            duration = videoItem.duration,
            videoWidth = videoItem.width,
            videoHeight = videoItem.height,
            bitrate = videoItem.bitrate,
            droppedFrames = 0,
            isEnded = false,
            error = if (report.compatibilityStatus == CompatibilityStatus.UNSUPPORTED_CONTAINER || report.compatibilityStatus == CompatibilityStatus.UNSUPPORTED_VIDEO_CODEC) report.statusMessage else null
        )

        if (report.compatibilityStatus == CompatibilityStatus.UNSUPPORTED_CONTAINER || report.compatibilityStatus == CompatibilityStatus.UNSUPPORTED_VIDEO_CODEC) {
            return
        }

        val uri = if (videoItem.path.startsWith("content://") || videoItem.path.startsWith("file://") || videoItem.path.startsWith("http://") || videoItem.path.startsWith("https://")) {
            Uri.parse(videoItem.path)
        } else {
            Uri.fromFile(java.io.File(videoItem.path))
        }

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(videoItem.title)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        if (startPositionMs > 0L) {
            player.seekTo(startPositionMs)
        }
        player.prepare()
        player.playWhenReady = true
    }

    fun enterPipMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspect = if (_playerState.value.videoWidth > 0 && _playerState.value.videoHeight > 0) {
                    Rational(_playerState.value.videoWidth, _playerState.value.videoHeight)
                } else {
                    Rational(16, 9)
                }
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspect)
                    .build()
                activity.enterPictureInPictureMode(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setInPipMode(inPip: Boolean) {
        _playerState.value = _playerState.value.copy(isInPipMode = inPip)
    }

    fun selectAudioTrack(track: AudioTrackInfo) {
        val currentTracks = player.currentTracks
        for ((groupIndex, group) in currentTracks.groups.withIndex()) {
            if (group.type == C.TRACK_TYPE_AUDIO && groupIndex == track.groupIndex) {
                val override = TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex)
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setOverrideForType(override)
                )
                break
            }
        }
        val updated = _playerState.value.availableAudioTracks.map {
            it.copy(isSelected = (it.id == track.id))
        }
        _playerState.value = _playerState.value.copy(
            availableAudioTracks = updated,
            selectedAudioTrack = track
        )
    }

    fun setAudioDelay(delayMs: Long) {
        _playerState.value = _playerState.value.copy(audioDelayMs = delayMs)
    }

    fun loadExternalSubtitle(uri: Uri, label: String = "External Subtitle") {
        val currentVideo = _playerState.value.currentVideo ?: return
        val currentPos = player.currentPosition

        val videoUri = if (currentVideo.path.startsWith("content://") || currentVideo.path.startsWith("file://") || currentVideo.path.startsWith("http://") || currentVideo.path.startsWith("https://")) {
            Uri.parse(currentVideo.path)
        } else {
            Uri.fromFile(java.io.File(currentVideo.path))
        }

        val subConfig = subtitleParser.buildSubtitleConfiguration(uri, label)

        val newMediaItem = MediaItem.Builder()
            .setUri(videoUri)
            .setSubtitleConfigurations(listOf(subConfig))
            .setMediaMetadata(MediaMetadata.Builder().setTitle(currentVideo.title).build())
            .build()

        player.setMediaItem(newMediaItem)
        player.seekTo(currentPos)
        player.prepare()
        player.playWhenReady = true

        val extTrack = subtitleParser.createSubtitleTrackFromUri(uri, label).copy(isSelected = true)
        val updatedList = _playerState.value.availableSubtitles.toMutableList().apply { add(extTrack) }

        _playerState.value = _playerState.value.copy(
            availableSubtitles = updatedList,
            selectedSubtitle = extTrack
        )
    }

    fun selectSubtitleTrack(track: SubtitleTrack?) {
        if (track == null) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            )
            _playerState.value = _playerState.value.copy(selectedSubtitle = null)
        } else {
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            )
            val updated = _playerState.value.availableSubtitles.map {
                it.copy(isSelected = (it.id == track.id))
            }
            _playerState.value = _playerState.value.copy(
                availableSubtitles = updated,
                selectedSubtitle = track
            )
        }
    }

    fun setSubtitleDelay(delayMs: Long) {
        _playerState.value = _playerState.value.copy(subtitleDelayMs = delayMs)
    }

    fun prepareAndPlayUrl(url: String, title: String = "Network Stream") {
        val report = codecCompatibilityInspector.inspectFile(url)
        totalDroppedFrames = 0

        _playerState.value = _playerState.value.copy(
            mediaType = MediaType.VIDEO,
            currentVideo = null,
            currentAudio = null,
            compatibilityReport = report,
            title = title,
            subtitle = "Network Stream",
            currentPosition = 0L,
            duration = C.TIME_UNSET,
            droppedFrames = 0,
            isEnded = false,
            error = null
        )

        val uri = Uri.parse(url)
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun prepareAndPlayAudio(audioItem: AudioItem) {
        val uri = if (audioItem.path.startsWith("content://") || audioItem.path.startsWith("file://") || audioItem.path.startsWith("http://") || audioItem.path.startsWith("https://")) {
            Uri.parse(audioItem.path)
        } else {
            Uri.fromFile(java.io.File(audioItem.path))
        }

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(audioItem.title)
                    .setArtist(audioItem.artist)
                    .setAlbumTitle(audioItem.album)
                    .setArtworkUri(audioItem.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        _playerState.value = _playerState.value.copy(
            mediaType = MediaType.AUDIO,
            currentAudio = audioItem,
            currentVideo = null,
            compatibilityReport = null,
            title = audioItem.title,
            subtitle = "${audioItem.artist} • ${audioItem.album}",
            currentPosition = 0L,
            duration = audioItem.duration,
            isEnded = false,
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
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        _playerState.value = _playerState.value.copy(currentPosition = player.currentPosition)
    }

    fun forwardBy(seconds: Int = 10) {
        seekTo(player.currentPosition + seconds * 1000L)
    }

    fun rewindBy(seconds: Int = 10) {
        seekTo(player.currentPosition - seconds * 1000L)
    }

    fun stepFrameForward() {
        seekTo(player.currentPosition + 40L)
    }

    fun stepFrameBackward() {
        seekTo(player.currentPosition - 40L)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 4.0f)
        player.setPlaybackSpeed(clamped)
        _playerState.value = _playerState.value.copy(playbackSpeed = clamped)
    }

    fun setSpeedBoosted(boosted: Boolean, boostSpeed: Float = 2.0f) {
        if (boosted) {
            if (!_playerState.value.isSpeedBoosted) {
                preBoostSpeed = _playerState.value.playbackSpeed
                player.setPlaybackSpeed(boostSpeed)
                _playerState.value = _playerState.value.copy(isSpeedBoosted = true)
            }
        } else {
            if (_playerState.value.isSpeedBoosted) {
                player.setPlaybackSpeed(preBoostSpeed)
                _playerState.value = _playerState.value.copy(isSpeedBoosted = false)
            }
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _playerState.value = _playerState.value.copy(aspectRatio = mode)
    }

    fun setZoomScale(scale: Float) {
        val clamped = scale.coerceIn(1.0f, 4.0f)
        _playerState.value = _playerState.value.copy(zoomScale = clamped)
    }

    fun setRotationAngle(angle: Int) {
        val normalized = (angle % 360 + 360) % 360
        _playerState.value = _playerState.value.copy(rotationAngle = normalized)
    }

    fun setOrientationLock(lock: OrientationLock) {
        _playerState.value = _playerState.value.copy(orientationLock = lock)
    }

    fun setScreenLocked(locked: Boolean) {
        _playerState.value = _playerState.value.copy(isScreenLocked = locked)
    }

    fun toggleScreenLock() {
        _playerState.value = _playerState.value.copy(isScreenLocked = !_playerState.value.isScreenLocked)
    }

    fun setAbRepeatA() {
        val pos = player.currentPosition
        _playerState.value = _playerState.value.copy(abRepeatA = pos)
    }

    fun setAbRepeatB() {
        val pos = player.currentPosition
        _playerState.value = _playerState.value.copy(abRepeatB = pos)
    }

    fun clearAbRepeat() {
        _playerState.value = _playerState.value.copy(abRepeatA = null, abRepeatB = null)
    }

    fun setVolumeLevel(level: Float) {
        val clamped = level.coerceIn(0f, 1f)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVol = (clamped * maxVol).toInt()
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
        } catch (_: Exception) { }
        _playerState.value = _playerState.value.copy(volumeLevel = clamped)
    }

    fun setBrightnessLevel(level: Float) {
        val clamped = level.coerceIn(0.05f, 1f)
        _playerState.value = _playerState.value.copy(brightnessLevel = clamped)
    }

    fun toggleStatsOverlay() {
        _playerState.value = _playerState.value.copy(showStatsOverlay = !_playerState.value.showStatsOverlay)
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        _playerState.value = _playerState.value.copy(sleepTimerMinutes = minutes)
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60_000L)
                remaining--
                _playerState.value = _playerState.value.copy(sleepTimerMinutes = remaining)
            }
            pause()
            _playerState.value = _playerState.value.copy(sleepTimerMinutes = null)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playerState.value = _playerState.value.copy(sleepTimerMinutes = null)
    }

    fun retry() {
        _playerState.value = _playerState.value.copy(error = null)
        player.prepare()
        player.playWhenReady = true
    }

    private fun inspectMaxHardwareDecoderCapability(): String {
        return try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            var maxW = 1920
            var maxH = 1080
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                for (type in info.supportedTypes) {
                    if (type.startsWith("video/")) {
                        try {
                            val caps = info.getCapabilitiesForType(type)?.videoCapabilities
                            if (caps != null) {
                                val supportedWidths = caps.supportedWidths
                                val supportedHeights = caps.supportedHeights
                                if (supportedWidths.upper > maxW) maxW = supportedWidths.upper
                                if (supportedHeights.upper > maxH) maxH = supportedHeights.upper
                            }
                        } catch (_: Exception) { }
                    }
                }
            }

            when {
                maxW >= 7680 || maxH >= 4320 -> "8K (7680x4320 @ 60fps)"
                maxW >= 5120 || maxH >= 2880 -> "5K (5120x2880 @ 60fps)"
                maxW >= 3840 || maxH >= 2160 -> "4K UHD (3840x2160 @ 60fps)"
                maxW >= 2560 || maxH >= 1440 -> "2K QHD (2560x1440 @ 60fps)"
                maxW >= 1920 || maxH >= 1080 -> "1080p Full HD"
                else -> "720p HD"
            }
        } catch (_: Exception) {
            "4K UHD (3840x2160)"
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = scope.launch {
            while (isActive) {
                val currentPos = player.currentPosition.coerceAtLeast(0L)
                val duration = player.duration.coerceAtLeast(0L)

                val a = _playerState.value.abRepeatA
                val b = _playerState.value.abRepeatB
                if (a != null && b != null && b > a && currentPos >= b) {
                    player.seekTo(a)
                }

                _playerState.value = _playerState.value.copy(
                    currentPosition = currentPos,
                    duration = duration
                )
                delay(400)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    fun release() {
        stopProgressTracker()
        cancelSleepTimer()
        try {
            context.unregisterReceiver(noisyReceiver)
        } catch (_: Exception) { }
        player.release()
    }
}
