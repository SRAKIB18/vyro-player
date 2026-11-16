package com.vyro.player.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vyro.player.presentation.decoder.DecoderInfoScreen
import com.vyro.player.presentation.decoder.DecoderInfoViewModel
import com.vyro.player.presentation.favorites.FavoritesScreen
import com.vyro.player.presentation.favorites.FavoritesViewModel
import com.vyro.player.presentation.folders.FolderDetailScreen
import com.vyro.player.presentation.folders.FoldersScreen
import com.vyro.player.presentation.folders.FoldersViewModel
import com.vyro.player.presentation.history.RecentVideosScreen
import com.vyro.player.presentation.history.RecentVideosViewModel
import com.vyro.player.presentation.home.HomeScreen
import com.vyro.player.presentation.home.HomeViewModel
import com.vyro.player.presentation.library.AllVideosScreen
import com.vyro.player.presentation.library.AllVideosViewModel
import com.vyro.player.presentation.player.PlayerScreen
import com.vyro.player.presentation.player.PlayerViewModel
import com.vyro.player.presentation.settings.AppSettingsScreen
import com.vyro.player.presentation.settings.AppSettingsViewModel
import com.vyro.player.presentation.settings.PlaybackSettingsScreen
import com.vyro.player.presentation.settings.SettingsViewModel
import com.vyro.player.presentation.settings.SubtitleSettingsScreen
import com.vyro.player.presentation.settings.SubtitleSettingsViewModel
import com.vyro.player.presentation.theme.VYROPlayerTheme

@Composable
fun VyroNavHost(
    navController: NavHostController = rememberNavController(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    VYROPlayerTheme(themeMode = settingsState.themeMode) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // 1. Home Dashboard Screen
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) },
                    onVideoClick = { videoId -> navController.navigate(Screen.VideoPlayer.createRoute(videoId)) },
                    onFolderClick = { folderName -> navController.navigate(Screen.FolderDetail.createRoute(folderName)) }
                )
            }

            // 2. All Videos Screen
            composable(Screen.AllVideos.route) {
                val viewModel: AllVideosViewModel = hiltViewModel()
                AllVideosScreen(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) },
                    onVideoClick = { videoId -> navController.navigate(Screen.VideoPlayer.createRoute(videoId)) }
                )
            }

            // 3. Folders Screen
            composable(Screen.Folders.route) {
                val viewModel: FoldersViewModel = hiltViewModel()
                FoldersScreen(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) },
                    onFolderClick = { folderName -> navController.navigate(Screen.FolderDetail.createRoute(folderName)) }
                )
            }

            // 4. Folder Detail Screen
            composable(Screen.FolderDetail.route) { backStackEntry ->
                val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                val viewModel: FoldersViewModel = hiltViewModel()
                FolderDetailScreen(
                    folderName = folderName,
                    viewModel = viewModel,
                    onVideoClick = { videoId -> navController.navigate(Screen.VideoPlayer.createRoute(videoId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 5. Recent Videos / History Screen
            composable(Screen.RecentVideos.route) {
                val viewModel: RecentVideosViewModel = hiltViewModel()
                RecentVideosScreen(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) },
                    onVideoClick = { videoId -> navController.navigate(Screen.VideoPlayer.createRoute(videoId)) }
                )
            }

            // 6. Favorites Screen
            composable(Screen.Favorites.route) {
                val viewModel: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(
                    viewModel = viewModel,
                    onVideoClick = { videoId -> navController.navigate(Screen.VideoPlayer.createRoute(videoId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 7. Video Player Screen
            composable(Screen.VideoPlayer.route) {
                val viewModel: PlayerViewModel = hiltViewModel()
                PlayerScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 8. Playback Settings Screen
            composable(Screen.PlaybackSettings.route) {
                PlaybackSettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 9. Subtitle Settings Screen
            composable(Screen.SubtitleSettings.route) {
                val viewModel: SubtitleSettingsViewModel = hiltViewModel()
                SubtitleSettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 10. Hardware Decoder Info Screen
            composable(Screen.DecoderInfo.route) {
                val viewModel: DecoderInfoViewModel = hiltViewModel()
                DecoderInfoScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 11. App Settings Screen
            composable(Screen.AppSettings.route) {
                val viewModel: AppSettingsViewModel = hiltViewModel()
                AppSettingsScreen(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToPlaybackSettings = { navController.navigate(Screen.PlaybackSettings.route) },
                    onNavigateToSubtitleSettings = { navController.navigate(Screen.SubtitleSettings.route) },
                    onNavigateToDecoders = { navController.navigate(Screen.DecoderInfo.route) }
                )
            }
        }
    }
}
