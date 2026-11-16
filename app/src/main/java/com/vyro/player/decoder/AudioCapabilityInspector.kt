package com.vyro.player.decoder

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaCodecList
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceAudioCapabilities(
    val maxOutputChannels: Int = 2,
    val isDolbyDigitalSupported: Boolean = false,
    val isDolbyDigitalPlusSupported: Boolean = false,
    val isDtsSupported: Boolean = false,
    val isFlacSupported: Boolean = true,
    val isOpusSupported: Boolean = true,
    val isSpatialAudioCapable: Boolean = false,
    val connectedOutputDevice: String = "Internal Speaker"
)

@Singleton
class AudioCapabilityInspector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun inspectCapabilities(): DeviceAudioCapabilities {
        var isAc3 = false
        var isEac3 = false
        var isDts = false
        var isFlac = false
        var isOpus = false

        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                for (type in info.supportedTypes) {
                    val lower = type.lowercase(Locale.ROOT)
                    if (lower.contains("ac3")) isAc3 = true
                    if (lower.contains("eac3")) isEac3 = true
                    if (lower.contains("dts")) isDts = true
                    if (lower.contains("flac")) isFlac = true
                    if (lower.contains("opus")) isOpus = true
                }
            }
        } catch (_: Exception) { }

        val outputDevice = detectConnectedOutputDevice()
        val maxChannels = if (outputDevice.contains("HDMI") || outputDevice.contains("Surround")) 6 else 2

        val isSpatial = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val spatializer = audioManager.spatializer
                spatializer.isAvailable
            } catch (_: Exception) {
                false
            }
        } else false

        return DeviceAudioCapabilities(
            maxOutputChannels = maxChannels,
            isDolbyDigitalSupported = isAc3,
            isDolbyDigitalPlusSupported = isEac3,
            isDtsSupported = isDts,
            isFlacSupported = isFlac,
            isOpusSupported = isOpus,
            isSpatialAudioCapable = isSpatial,
            connectedOutputDevice = outputDevice
        )
    }

    private fun detectConnectedOutputDevice(): String {
        return try {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (dev in devices) {
                when (dev.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> return "Bluetooth Headset (${dev.productName})"
                    AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> return "Wired Headphones"
                    AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_DEVICE -> return "USB Audio Device"
                    AudioDeviceInfo.TYPE_HDMI, AudioDeviceInfo.TYPE_HDMI_ARC -> return "HDMI Surround Device"
                }
            }
            "Internal Device Speaker"
        } catch (_: Exception) {
            "Internal Device Speaker"
        }
    }
}
