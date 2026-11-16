@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)

package com.vyro.player.presentation.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.vyro.player.core.util.FileUtils
import com.vyro.player.core.util.TimeUtils
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.AudioTrackInfo
import com.vyro.player.domain.model.OrientationLock
import com.vyro.player.domain.model.SubtitleTrack
import com.vyro.player.player.VyroPlayerState
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val playerState by viewModel.playerState.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showAbRepeatDialog by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }

    // SAF Subtitle File Picker Launcher
    val safSubtitleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val label = uri.lastPathSegment?.substringAfterLast('/') ?: "External Subtitle"
            viewModel.loadExternalSubtitle(uri, label)
        }
    }

    // Double tap ripple indicators
    var doubleTapSeekLeft by remember { mutableStateOf(false) }
    var doubleTapSeekRight by remember { mutableStateOf(false) }

    // Gesture indicator states
    var isAdjustingVolume by remember { mutableStateOf(false) }
    var gestureVolumeVal by remember { mutableFloatStateOf(playerState.volumeLevel) }

    var isAdjustingBrightness by remember { mutableStateOf(false) }
    var gestureBrightnessVal by remember { mutableFloatStateOf(playerState.brightnessLevel) }

    var pinchZoomScale by remember { mutableFloatStateOf(playerState.zoomScale) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto hide controls after 3.5 seconds of inactivity
    LaunchedEffect(showControls, playerState.isPlaying) {
        if (showControls && playerState.isPlaying && !playerState.isScreenLocked) {
            delay(3500)
            showControls = false
        }
    }

    // Lifecycle Observer for Activity/Compose ON_PAUSE, ON_RESUME, ON_STOP
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (!playerState.isInPipMode) {
                        viewModel.pause()
                        viewModel.saveCurrentPosition()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.play()
                }
                Lifecycle.Event.ON_STOP -> {
                    if (!playerState.isInPipMode) {
                        viewModel.saveCurrentPosition()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.saveCurrentPosition()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(playerState.isScreenLocked || playerState.isInPipMode) {
                if (!playerState.isScreenLocked && !playerState.isInPipMode) {
                    detectTransformGestures { _, _, zoom, _ ->
                        pinchZoomScale = (pinchZoomScale * zoom).coerceIn(1.0f, 4.0f)
                        viewModel.setZoomScale(pinchZoomScale)
                    }
                }
            }
            .pointerInput(playerState.isScreenLocked || playerState.isInPipMode) {
                if (!playerState.isScreenLocked && !playerState.isInPipMode) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        },
                        onDoubleTap = { offset ->
                            val screenWidth = size.width
                            when {
                                offset.x < screenWidth * 0.35f -> {
                                    viewModel.rewindBy(10)
                                    doubleTapSeekLeft = true
                                }
                                offset.x > screenWidth * 0.65f -> {
                                    viewModel.forwardBy(10)
                                    doubleTapSeekRight = true
                                }
                                else -> {
                                    viewModel.togglePlayPause()
                                }
                            }
                        },
                        onLongPress = {
                            viewModel.setSpeedBoosted(true, 2.0f)
                        },
                        onPress = {
                            try {
                                awaitRelease()
                            } finally {
                                viewModel.setSpeedBoosted(false)
                            }
                        }
                    )
                }
            }
            .pointerInput(playerState.isScreenLocked || playerState.isInPipMode) {
                if (!playerState.isScreenLocked && !playerState.isInPipMode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth / 2) {
                                isAdjustingBrightness = true
                            } else {
                                isAdjustingVolume = true
                            }
                        },
                        onDragEnd = {
                            isAdjustingBrightness = false
                            isAdjustingVolume = false
                        },
                        onDragCancel = {
                            isAdjustingBrightness = false
                            isAdjustingVolume = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount.y / 800f
                            if (isAdjustingBrightness) {
                                val nextVal = (gestureBrightnessVal + delta).coerceIn(0.05f, 1.0f)
                                gestureBrightnessVal = nextVal
                                viewModel.setBrightnessLevel(nextVal)
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    lp.screenBrightness = nextVal
                                    act.window.attributes = lp
                                }
                            } else if (isAdjustingVolume) {
                                val nextVal = (gestureVolumeVal + delta).coerceIn(0.0f, 1.0f)
                                gestureVolumeVal = nextVal
                                viewModel.setVolumeLevel(nextVal)
                            }
                        }
                    )
                }
            }
    ) {
        // Media3 ExoPlayer AndroidView Surface with Graphics Zoom & Rotation
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = when (playerState.aspectRatio) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                }

                // Apply Subtitle Caption Styling
                val captionStyle = CaptionStyleCompat(
                    Color.White.toArgb(),
                    Color.Black.copy(alpha = 0.6f).toArgb(),
                    Color.Transparent.toArgb(),
                    CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                    Color.Black.toArgb(),
                    Typeface.DEFAULT_BOLD
                )
                playerView.subtitleView?.setStyle(captionStyle)
                playerView.subtitleView?.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = pinchZoomScale,
                    scaleY = pinchZoomScale,
                    rotationZ = playerState.rotationAngle.toFloat()
                )
        )

        // Buffering Indicator
        if (playerState.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Speed Boost Top Badge Indicator
        if (playerState.isSpeedBoosted && !playerState.isInPipMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FastForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "2.0x Fast Forwarding", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Double Tap Seek Left Ripple Indicator (-10s)
        LaunchedEffect(doubleTapSeekLeft) {
            if (doubleTapSeekLeft) {
                delay(800)
                doubleTapSeekLeft = false
            }
        }
        if (doubleTapSeekLeft && !playerState.isInPipMode) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Replay10, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Text(text = "-10s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Double Tap Seek Right Ripple Indicator (+10s)
        LaunchedEffect(doubleTapSeekRight) {
            if (doubleTapSeekRight) {
                delay(800)
                doubleTapSeekRight = false
            }
        }
        if (doubleTapSeekRight && !playerState.isInPipMode) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "+10s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(Icons.Default.Forward10, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Screen Lock Unlock Floating Button
        if (playerState.isScreenLocked && !playerState.isInPipMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleScreenLock() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.75f))
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Unlock Screen", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Gesture Brightness Indicator (Left Overlay)
        AnimatedVisibility(
            visible = isAdjustingBrightness && !playerState.isScreenLocked && !playerState.isInPipMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .width(48.dp)
                    .height(160.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.Brightness6, contentDescription = null, tint = Color.White, modifier = Modifier.padding(top = 12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .fillMaxHeight(gestureBrightnessVal)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(text = "${(gestureBrightnessVal * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }

        // Gesture Volume Indicator (Right Overlay)
        AnimatedVisibility(
            visible = isAdjustingVolume && !playerState.isScreenLocked && !playerState.isInPipMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .width(48.dp)
                    .height(160.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.padding(top = 12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .fillMaxHeight(gestureVolumeVal)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(text = "${(gestureVolumeVal * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }

        // Error Retry Overlay
        if (playerState.error != null && !playerState.isInPipMode) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Playback Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = playerState.error!!, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retry Playback")
                    }
                }
            }
        }

        // Custom Overlay Controls (Visible when not screen locked and not in PiP mode)
        if (showControls && !playerState.isScreenLocked && !playerState.isInPipMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Top Bar with Status Bar Padding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.saveCurrentPosition()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = playerState.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    // Picture-in-Picture Button
                    IconButton(onClick = {
                        activity?.let { act -> viewModel.enterPipMode(act) }
                    }) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Picture-in-Picture",
                            tint = Color.White
                        )
                    }

                    // Audio Tracks & Device Output Picker Button
                    IconButton(onClick = { showAudioSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Audio Tracks",
                            tint = if (playerState.selectedAudioTrack != null) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    // Subtitle Tracks Picker Button
                    IconButton(onClick = { showSubtitleSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitles",
                            tint = if (playerState.selectedSubtitle != null) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    // Screen Lock Toggle Button
                    IconButton(onClick = { viewModel.toggleScreenLock() }) {
                        Icon(Icons.Default.LockOpen, contentDescription = "Lock Screen", tint = Color.White)
                    }

                    // Rotation Angle Button
                    IconButton(onClick = {
                        val nextAngle = (playerState.rotationAngle + 90) % 360
                        viewModel.setRotationAngle(nextAngle)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate Screen", tint = Color.White)
                    }

                    // Auto Orientation Lock Button
                    IconButton(onClick = {
                        val nextLock = when (playerState.orientationLock) {
                            OrientationLock.AUTO -> {
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                OrientationLock.PORTRAIT
                            }
                            OrientationLock.PORTRAIT -> {
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                OrientationLock.LANDSCAPE
                            }
                            else -> {
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                OrientationLock.AUTO
                            }
                        }
                        viewModel.setOrientationLock(nextLock)
                    }) {
                        Icon(Icons.Default.ScreenRotation, contentDescription = "Orientation Lock", tint = Color.White)
                    }

                    // A-B Repeat Button
                    IconButton(onClick = { showAbRepeatDialog = true }) {
                        Icon(Icons.Default.Loop, contentDescription = "A-B Repeat", tint = if (playerState.abRepeatA != null) MaterialTheme.colorScheme.primary else Color.White)
                    }

                    // Stats for Nerds Toggle
                    IconButton(onClick = { viewModel.toggleStatsOverlay() }) {
                        Icon(Icons.Default.Info, contentDescription = "Stats for Nerds", tint = Color.White)
                    }

                    // Sleep Timer Button
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(Icons.Default.Timer, contentDescription = "Sleep Timer", tint = if (playerState.sleepTimerMinutes != null) MaterialTheme.colorScheme.primary else Color.White)
                    }

                    // Playback Speed Menu Button
                    Box {
                        IconButton(onClick = { showSpeedMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback Speed",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}x") },
                                    onClick = {
                                        viewModel.setPlaybackSpeed(speed)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Aspect Ratio Toggle Button
                    IconButton(onClick = {
                        val nextMode = when (playerState.aspectRatio) {
                            AspectRatioMode.FIT -> AspectRatioMode.FILL
                            AspectRatioMode.FILL -> AspectRatioMode.ZOOM
                            AspectRatioMode.ZOOM -> AspectRatioMode.STRETCH
                            else -> AspectRatioMode.FIT
                        }
                        viewModel.setAspectRatio(nextMode)
                    }) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Aspect Ratio",
                            tint = Color.White
                        )
                    }
                }

                // Center Controls (Prev, Rewind 10s, Play/Pause, Forward 10s, Next)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    IconButton(
                        onClick = { viewModel.rewindBy(10) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { viewModel.forwardBy(10) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                // Bottom Control Bar with Navigation Bar Padding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    // Frame step and AB Repeat Pills Row
                    if (playerState.abRepeatA != null || playerState.abRepeatB != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AssistChip(
                                onClick = { showAbRepeatDialog = true },
                                label = {
                                    Text(
                                        text = "A-B Loop [${TimeUtils.formatDuration(playerState.abRepeatA ?: 0L)} - ${if (playerState.abRepeatB != null) TimeUtils.formatDuration(playerState.abRepeatB!!) else "End"}]",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${TimeUtils.formatDuration(playerState.currentPosition)} / ${TimeUtils.formatDuration(playerState.duration)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Zoom: ${(pinchZoomScale * 100).toInt()}% • Speed: ${String.format(Locale.getDefault(), "%.2fx", playerState.playbackSpeed)} • ${playerState.aspectRatio.name}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Slider(
                        value = playerState.currentPosition.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..(playerState.duration.coerceAtLeast(1L).toFloat()),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Subtitles Picker Sheet
        if (showSubtitleSheet && !playerState.isInPipMode) {
            SubtitlePickerModalSheet(
                playerState = playerState,
                onSelectSubtitle = { viewModel.selectSubtitleTrack(it) },
                onLoadExternalSubtitle = {
                    showSubtitleSheet = false
                    safSubtitleLauncher.launch(arrayOf("*/*"))
                },
                onDismiss = { showSubtitleSheet = false }
            )
        }

        // Audio Tracks & Device Capabilities Sheet
        if (showAudioSheet && !playerState.isInPipMode) {
            AudioTrackPickerModalSheet(
                playerState = playerState,
                onSelectAudioTrack = { viewModel.selectAudioTrack(it) },
                onDismiss = { showAudioSheet = false }
            )
        }

        // Stats for Nerds Dialog Overlay
        if (playerState.showStatsOverlay && !playerState.isInPipMode) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 16.dp)
                    .width(300.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Stats for Nerds",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Resolution: ${playerState.videoWidth} x ${playerState.videoHeight}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(text = "Max HW Cap: ${playerState.maxSupportedResolution}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(text = "Bitrate: ${if (playerState.bitrate > 0) FileUtils.formatFileSize(playerState.bitrate) + "/s" else "Variable"}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(text = "Dropped Frames: ${playerState.droppedFrames}", style = MaterialTheme.typography.bodySmall, color = if (playerState.droppedFrames > 30) Color(0xFFFF5252) else Color.White)
                    Text(text = "Codec: ${playerState.compatibilityReport?.videoCodecName ?: "Hardware Codec"}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
        }

        // A-B Repeat Setup Dialog
        if (showAbRepeatDialog && !playerState.isInPipMode) {
            AlertDialog(
                onDismissRequest = { showAbRepeatDialog = false },
                title = { Text("A-B Repeat Loop") },
                text = {
                    Column {
                        Text("Loop playback continuously between Point A and Point B.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = { viewModel.setAbRepeatA() }) {
                                Text("Set Point A (${TimeUtils.formatDuration(playerState.currentPosition)})")
                            }
                            Button(onClick = { viewModel.setAbRepeatB() }) {
                                Text("Set Point B (${TimeUtils.formatDuration(playerState.currentPosition)})")
                            }
                        }
                        if (playerState.abRepeatA != null || playerState.abRepeatB != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    viewModel.clearAbRepeat()
                                    showAbRepeatDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear A-B Repeat Loop", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAbRepeatDialog = false }) { Text("Close") }
                }
            )
        }

        // Sleep Timer Dialog
        if (showTimerDialog && !playerState.isInPipMode) {
            AlertDialog(
                onDismissRequest = { showTimerDialog = false },
                title = { Text("Sleep Timer") },
                text = {
                    Column {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            Button(
                                onClick = {
                                    viewModel.startSleepTimer(mins)
                                    showTimerDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                Text("$mins Minutes")
                            }
                        }
                        if (playerState.sleepTimerMinutes != null) {
                            TextButton(
                                onClick = {
                                    viewModel.cancelSleepTimer()
                                    showTimerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Turn Off Timer", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTimerDialog = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
private fun AudioTrackPickerModalSheet(
    playerState: VyroPlayerState,
    onSelectAudioTrack: (AudioTrackInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = "Audio Tracks & Device Output", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Output Device: ${playerState.deviceAudioCapabilities?.connectedOutputDevice ?: "Internal Speaker"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (playerState.availableAudioTracks.isEmpty()) {
                Text(text = "Default Audio Track Active", style = MaterialTheme.typography.bodyLarge)
            } else {
                playerState.availableAudioTracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectAudioTrack(track)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = playerState.selectedAudioTrack?.id == track.id,
                            onClick = {
                                onSelectAudioTrack(track)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${track.label} (${track.language})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${track.channelLayoutLabel} • ${if (track.sampleRate > 0) "${track.sampleRate} Hz" else "Default Sample Rate"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitlePickerModalSheet(
    playerState: VyroPlayerState,
    onSelectSubtitle: (SubtitleTrack?) -> Unit,
    onLoadExternalSubtitle: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = "Subtitle Tracks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Off Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onSelectSubtitle(null)
                        onDismiss()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = playerState.selectedSubtitle == null,
                    onClick = {
                        onSelectSubtitle(null)
                        onDismiss()
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subtitles Off", style = MaterialTheme.typography.bodyLarge)
            }

            // Available Subtitle Tracks (Embedded & External)
            playerState.availableSubtitles.forEach { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onSelectSubtitle(track)
                            onDismiss()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = playerState.selectedSubtitle?.id == track.id,
                        onClick = {
                            onSelectSubtitle(track)
                            onDismiss()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${track.label} (${track.language})${if (track.isExternal) " [External]" else " [Embedded]"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLoadExternalSubtitle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Load External Subtitle File (SAF .srt / .vtt / .ass)")
            }
        }
    }
}
