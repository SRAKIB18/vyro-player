package com.vyro.player.domain.model

data class DecoderCapability(
    val codecName: String,
    val mimeType: String,
    val isHardwareAccelerated: Boolean,
    val isSoftwareOnly: Boolean,
    val isEncoder: Boolean,
    val maxSupportedResolution: String,
    val isHdrSupported: Boolean
)
