package com.vyro.player

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vyro.player.player.VyroPlayerManager
import com.vyro.player.presentation.navigation.VyroNavHost
import com.vyro.player.presentation.theme.VYROPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playerManager: VyroPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VYROPlayerTheme {
                VyroNavHost()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (playerManager.playerState.value.isPlaying && playerManager.playerState.value.mediaType == com.vyro.player.player.MediaType.VIDEO) {
            playerManager.enterPipMode(this)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        playerManager.setInPipMode(isInPictureInPictureMode)
    }
}
