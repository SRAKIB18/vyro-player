package com.vyro.player.domain.model

data class AudioAlbum(
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: String? = null
)
