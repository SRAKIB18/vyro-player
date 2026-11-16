package com.vyro.player.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SettingSwitchRow(
                title = "Hardware Acceleration",
                subtitle = "Use hardware decoders for video decoding when available",
                checked = settings.useHardwareAcceleration,
                onCheckedChange = { viewModel.updateSettings(settings.copy(useHardwareAcceleration = it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitchRow(
                title = "Auto-play Next Video",
                subtitle = "Automatically play the next video in queue",
                checked = settings.autoPlayNext,
                onCheckedChange = { viewModel.updateSettings(settings.copy(autoPlayNext = it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitchRow(
                title = "Resume Playback",
                subtitle = "Remember last played position of videos",
                checked = settings.savePlaybackPosition,
                onCheckedChange = { viewModel.updateSettings(settings.copy(savePlaybackPosition = it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitchRow(
                title = "Gesture Controls",
                subtitle = "Enable volume and brightness vertical drag gestures",
                checked = settings.gestureVolumeEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(gestureVolumeEnabled = it)) }
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
