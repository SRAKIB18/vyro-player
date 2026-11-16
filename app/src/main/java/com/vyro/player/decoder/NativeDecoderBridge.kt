package com.vyro.player.decoder

/**
 * Isolated bridge interface for optional native decoder extensions (e.g. FFmpeg / libVLC).
 * Keeps native decoder dependencies isolated from standard Media3 ExoPlayer builds.
 */
interface NativeDecoderBridge {
    fun isNativeDecoderAvailable(): Boolean
    fun getSupportedNativeContainers(): List<String>
    fun getSupportedNativeCodecs(): List<String>
    fun supportsContainer(containerExtension: String): Boolean
    fun supportsCodec(mimeType: String): Boolean
}

class DefaultNativeDecoderBridge : NativeDecoderBridge {
    override fun isNativeDecoderAvailable(): Boolean = false

    override fun getSupportedNativeContainers(): List<String> = listOf(
        "MP4", "MKV", "WebM", "MOV", "AVI", "TS", "MTS", "M2TS", "3GP", "MPG", "MPEG", "FLV"
    )

    override fun getSupportedNativeCodecs(): List<String> = listOf(
        "video/avc", "video/hevc", "video/x-vnd.on2.vp8", "video/x-vnd.on2.vp9", "video/av01",
        "audio/mp4a-latm", "audio/mpeg", "audio/ac3", "audio/eac3", "audio/flac", "audio/opus", "audio/vorbis"
    )

    override fun supportsContainer(containerExtension: String): Boolean {
        val ext = containerExtension.uppercase()
        return getSupportedNativeContainers().contains(ext)
    }

    override fun supportsCodec(mimeType: String): Boolean {
        return getSupportedNativeCodecs().contains(mimeType)
    }
}
