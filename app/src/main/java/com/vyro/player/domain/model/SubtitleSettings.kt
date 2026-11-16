package com.vyro.player.domain.model

data class SubtitleSettings(
    val language: String = "English",
    val fontSizeSp: Int = 18,
    val fontColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#80000000",
    val backgroundOpacity: Float = 0.5f,
    val delayMs: Long = 0L
)
