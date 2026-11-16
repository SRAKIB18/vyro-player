package com.vyro.player.domain.model

data class SubtitleSettings(
    val language: String = "English",
    val fontSizeSp: Int = 18,
    val fontFamily: String = "DEFAULT",
    val fontColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#80000000",
    val backgroundOpacity: Float = 0.5f,
    val isBold: Boolean = false,
    val hasOutline: Boolean = true,
    val verticalPosition: String = "BOTTOM",
    val delayMs: Long = 0L
)
