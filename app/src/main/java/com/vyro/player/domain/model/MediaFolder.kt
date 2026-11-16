package com.vyro.player.domain.model

data class MediaFolder(
    val name: String,
    val path: String,
    val videoCount: Int,
    val totalSize: Long
)
