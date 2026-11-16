package com.vyro.player.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AllVideos : Screen("all_videos")
    object Folders : Screen("folders")
    object FolderDetail : Screen("folder_detail/{folderName}") {
        fun createRoute(folderName: String) = "folder_detail/$folderName"
    }
    object Music : Screen("music")
    object RecentVideos : Screen("recent_videos")
    object Favorites : Screen("favorites")
    object VideoPlayer : Screen("video_player/{videoId}") {
        fun createRoute(videoId: Long) = "video_player/$videoId"
    }
    object PlaybackSettings : Screen("playback_settings")
    object SubtitleSettings : Screen("subtitle_settings")
    object DecoderInfo : Screen("decoder_info")
    object AppSettings : Screen("app_settings")
}
