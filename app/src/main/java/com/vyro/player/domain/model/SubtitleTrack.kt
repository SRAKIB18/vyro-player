package com.vyro.player.domain.model

data class SubtitleTrack(
    val id: String,
    val language: String,
    val label: String,
    val mimeType: String,
    val uri: String? = null,
    val isExternal: Boolean = false,
    val trackIndex: Int = 0,
    val isSelected: Boolean = false
)
