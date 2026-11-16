package com.vyro.player.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vyro.player.presentation.decoder.DecoderInfoScreen
import com.vyro.player.presentation.decoder.DecoderInfoViewModel
import com.vyro.player.presentation.library.VideoLibraryScreen
import com.vyro.player.presentation.library.VideoLibraryViewModel
import com.vyro.player.presentation.player.PlayerScreen
import com.vyro.player.presentation.player.PlayerViewModel
import com.vyro.player.presentation.settings.SettingsScreen
import com.vyro.player.presentation.settings.SettingsViewModel

@Composable
fun VyroNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.VideoLibrary.route
    ) {
        composable(Screen.VideoLibrary.route) {
            val viewModel: VideoLibraryViewModel = hiltViewModel()
            VideoLibraryScreen(
                viewModel = viewModel,
                onVideoClick = { videoId ->
                    navController.navigate(Screen.VideoPlayer.createRoute(videoId))
                },
                onNavigateToDecoders = {
                    navController.navigate(Screen.DecoderInfo.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.VideoPlayer.route) {
            val viewModel: PlayerViewModel = hiltViewModel()
            PlayerScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DecoderInfo.route) {
            val viewModel: DecoderInfoViewModel = hiltViewModel()
            DecoderInfoScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
