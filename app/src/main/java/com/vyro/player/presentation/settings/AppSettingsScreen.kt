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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
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
    var showAboutDialog by remember { mutableStateOf(false) }

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
        ) {
            SettingNavigationCard(
                title = "Favorite Videos",
                subtitle = "Manage your bookmarked videos",
                icon = Icons.Default.Star,
                onClick = onNavigateToFavorites
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Playback Settings",
                subtitle = "Hardware acceleration, background play & controls",
                icon = Icons.Default.PlayCircle,
                onClick = onNavigateToPlaybackSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Subtitle Settings",
                subtitle = "Subtitles font size, language & background",
                icon = Icons.Default.Subtitles,
                onClick = onNavigateToSubtitleSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Hardware Decoders",
                subtitle = "View video decoders & HDR capabilities",
                icon = Icons.Default.DeveloperBoard,
                onClick = onNavigateToDecoders
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "App Theme Mode",
                subtitle = "Currently: ${settings.themeMode.name}",
                icon = Icons.Default.ColorLens,
                onClick = { showThemeDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationCard(
                title = "Rescan Media Storage",
                subtitle = "Refresh videos in internal storage",
                icon = Icons.Default.Refresh,
                onClick = { viewModel.scanMedia() }
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
                subtitle = "Version 1.0.0 • Production Build",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Choose Theme Mode") },
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
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("VYRO Video Player") },
                text = {
                    Column {
                        Text(text = "Professional Android Video Player built with Jetpack Compose, Material 3, Clean Architecture, and AndroidX Media3 ExoPlayer 1.11.1.")
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Package: com.vyro.player", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
                }
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
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
