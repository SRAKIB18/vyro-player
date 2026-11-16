package com.vyro.player.player

import com.vyro.player.decoder.DeviceAudioCapabilities
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.model.AudioTrackInfo
import com.vyro.player.domain.model.CompatibilityReport
import com.vyro.player.domain.model.OrientationLock
import com.vyro.player.domain.model.SubtitleTrack
import com.vyro.player.domain.model.VideoItem

enum class MediaType {
    NONE,
    VIDEO,
    AUDIO
}

data class VyroPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val isScreenLocked: Boolean = false,
    val isSpeedBoosted: Boolean = false,
    val isInPipMode: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val rotationAngle: Int = 0, // 0, 90, 180, 270 degrees
    val orientationLock: OrientationLock = OrientationLock.AUTO,
    val zoomScale: Float = 1.0f,
    val abRepeatA: Long? = null,
    val abRepeatB: Long? = null,
    val mediaType: MediaType = MediaType.NONE,
    val currentVideo: VideoItem? = null,
    val currentAudio: AudioItem? = null,
    val availableSubtitles: List<SubtitleTrack> = emptyList(),
    val selectedSubtitle: SubtitleTrack? = null,
    val subtitleDelayMs: Long = 0L,
    val availableAudioTracks: List<AudioTrackInfo> = emptyList(),
    val selectedAudioTrack: AudioTrackInfo? = null,
    val audioDelayMs: Long = 0L,
    val deviceAudioCapabilities: DeviceAudioCapabilities? = null,
    val compatibilityReport: CompatibilityReport? = null,
    val title: String = "",
    val subtitle: String = "",
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val frameRate: Float = 0f,
    val bitrate: Long = 0L,
    val droppedFrames: Int = 0,
    val maxSupportedResolution: String = "4K 60fps",
    val activeDecoderName: String = "Hardware Decoder",
    val sleepTimerMinutes: Int? = null,
    val volumeLevel: Float = 0.8f,
    val brightnessLevel: Float = 0.8f,
    val showStatsOverlay: Boolean = false,
    val error: String? = null
)
