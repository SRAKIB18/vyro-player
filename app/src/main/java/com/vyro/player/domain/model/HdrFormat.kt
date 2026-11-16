package com.vyro.player.domain.model

enum class HdrFormat(val displayName: String) {
    SDR("SDR Standard"),
    HDR10("HDR10"),
    HDR10_PLUS("HDR10+"),
    HLG("HLG"),
    DOLBY_VISION("Dolby Vision"),
    TEN_BIT("10-bit Color")
}

data class HdrDisplayInfo(
    val isHdrSupported: Boolean,
    val supportsHdr10: Boolean,
    val supportsHdr10Plus: Boolean,
    val supportsHlg: Boolean,
    val supportsDolbyVision: Boolean,
    val maxLuminance: Float = 0f
)

data class HdrMetadataInfo(
    val hdrFormat: HdrFormat,
    val bitDepth: Int = 8,
    val colorTransfer: String = "SDR (BT.709)",
    val colorStandard: String = "BT.709",
    val isNativeHdrPlayable: Boolean = false,
    val isDolbyVisionNative: Boolean = false,
    val fallbackMessage: String? = null
)
