package com.vyro.player.presentation.navigation

sealed class Screen(val route: String) {
    object VideoLibrary : Screen("video_library")
    object VideoPlayer : Screen("video_player/{videoId}") {
        fun createRoute(videoId: Long) = "video_player/$videoId"
    }
    object DecoderInfo : Screen("decoder_info")
    object Settings : Screen("settings")
}
