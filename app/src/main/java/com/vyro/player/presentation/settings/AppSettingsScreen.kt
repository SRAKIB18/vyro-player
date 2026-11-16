package com.vyro.player.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vyro.player.domain.model.AspectRatioMode
import com.vyro.player.domain.model.ThemeMode
import com.vyro.player.presentation.components.VyroBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPlaybackSettings: () -> Unit,
    onNavigateToSubtitleSettings: () -> Unit,
    onNavigateToDecoders: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showAspectDialog by remember { mutableStateOf(false) }
    var showSeekDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCacheClearedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            VyroBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("General & Look", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingNavigationCard(
                title = "App Theme Mode",
                subtitle = "Currently: ${settings.themeMode.name}",
                icon = Icons.Default.ColorLens,
                onClick = { showThemeDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "App Interface Language",
                subtitle = "Currently: ${settings.appLanguage}",
                icon = Icons.Default.Language,
                onClick = { showLangDialog = true }
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("Playback & Controls", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingNavigationCard(
                title = "Default Playback Speed",
                subtitle = "Currently: ${settings.defaultSpeed}x",
                icon = Icons.Default.Speed,
                onClick = { showSpeedDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Default Aspect Ratio",
                subtitle = "Currently: ${settings.defaultAspectRatio.name}",
                icon = Icons.Default.PlayCircle,
                onClick = { showAspectDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Double Tap Seek Step",
                subtitle = "Currently: ${settings.doubleTapSeekSeconds} seconds",
                icon = Icons.Default.Gesture,
                onClick = { showSeekDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Playback Settings",
                subtitle = "Hardware acceleration & background play",
                icon = Icons.Default.PlayCircle,
                onClick = onNavigateToPlaybackSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Subtitle Customization",
                subtitle = "Font size, family, color & background opacity",
                icon = Icons.Default.Subtitles,
                onClick = onNavigateToSubtitleSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Hardware Decoder Info",
                subtitle = "View video decoders & maximum HDR capabilities",
                icon = Icons.Default.DeveloperBoard,
                onClick = onNavigateToDecoders
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("Storage & Privacy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingNavigationCard(
                title = "Favorite Bookmarks",
                subtitle = "View and manage favorite videos",
                icon = Icons.Default.Star,
                onClick = onNavigateToFavorites
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Rescan Storage",
                subtitle = "Refresh video & audio tracks",
                icon = Icons.Default.Refresh,
                onClick = { viewModel.scanMedia() }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Clear Thumbnail Cache",
                subtitle = "Free disk space by clearing cached thumbnail images",
                icon = Icons.Default.CleaningServices,
                onClick = {
                    viewModel.clearThumbnailCache()
                    showCacheClearedMessage = true
                }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Clear Playback History",
                subtitle = "Delete all recent playback history records",
                icon = Icons.Default.Delete,
                onClick = { viewModel.clearHistory() }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "About VYRO Player",
                subtitle = "Version 1.0.0 • Production Release",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )
        }

        // Theme Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("App Theme Mode") },
                text = {
                    Column {
                        ThemeMode.entries.forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.themeMode == mode,
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = mode.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Close") } }
            )
        }

        // Speed Dialog
        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Default Playback Speed") },
                text = {
                    Column {
                        listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(defaultSpeed = speed))
                                        showSpeedDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.defaultSpeed == speed,
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(defaultSpeed = speed))
                                        showSpeedDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "${speed}x", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showSpeedDialog = false }) { Text("Close") } }
            )
        }

        // Aspect Ratio Dialog
        if (showAspectDialog) {
            AlertDialog(
                onDismissRequest = { showAspectDialog = false },
                title = { Text("Default Aspect Ratio") },
                text = {
                    Column {
                        AspectRatioMode.entries.forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(defaultAspectRatio = mode))
                                        showAspectDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.defaultAspectRatio == mode,
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(defaultAspectRatio = mode))
                                        showAspectDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = mode.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showAspectDialog = false }) { Text("Close") } }
            )
        }

        // Seek Dialog
        if (showSeekDialog) {
            AlertDialog(
                onDismissRequest = { showSeekDialog = false },
                title = { Text("Double Tap Seek Interval") },
                text = {
                    Column {
                        listOf(5, 10, 15, 30).forEach { sec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(doubleTapSeekSeconds = sec, seekIntervalSeconds = sec))
                                        showSeekDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.doubleTapSeekSeconds == sec,
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(doubleTapSeekSeconds = sec, seekIntervalSeconds = sec))
                                        showSeekDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "$sec Seconds", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showSeekDialog = false }) { Text("Close") } }
            )
        }

        // Language Dialog
        if (showLangDialog) {
            AlertDialog(
                onDismissRequest = { showLangDialog = false },
                title = { Text("App Language") },
                text = {
                    Column {
                        listOf("System Default", "English", "Spanish", "French", "German", "Japanese").forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(appLanguage = lang))
                                        showLangDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.appLanguage == lang,
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(appLanguage = lang))
                                        showLangDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = lang, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showLangDialog = false }) { Text("Close") } }
            )
        }

        if (showCacheClearedMessage) {
            AlertDialog(
                onDismissRequest = { showCacheClearedMessage = false },
                title = { Text("Cache Cleared") },
                text = { Text("Disk cached thumbnail images have been successfully cleared.") },
                confirmButton = { TextButton(onClick = { showCacheClearedMessage = false }) { Text("OK") } }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("VYRO Video Player") },
                text = {
                    Column {
                        Text(text = "Professional Android Video & Audio Player built with Jetpack Compose, Material 3, Clean Architecture, and AndroidX Media3 ExoPlayer 1.11.1.")
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Package: com.vyro.player\nMinimum SDK: 24 (Android 7.0)\nTarget SDK: 37 (Android 15+)", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("OK") } }
            )
        }
    }
}

@Composable
fun SettingNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 14.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
