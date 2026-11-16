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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vyro.player.domain.model.AspectRatioMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()

    var showAspectDialog by remember { mutableStateOf(false) }
    var showSeekDialog by remember { mutableStateOf(false) }
    var showDecoderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playback & Decoder Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            Text("Hardware Decoding & Performance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingRowSwitch(
                title = "Hardware Acceleration",
                subtitle = "Use hardware decoders for decoding videos when available",
                checked = settings.useHardwareAcceleration,
                onCheckedChange = { viewModel.updateSettings(settings.copy(useHardwareAcceleration = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDecoderDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Decoder Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Currently: ${settings.defaultDecoderPreference}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Gesture Controls", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingRowSwitch(
                title = "Volume Swipe Gesture",
                subtitle = "Swipe vertically on the right side of the video to control volume",
                checked = settings.gestureVolumeEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(gestureVolumeEnabled = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingRowSwitch(
                title = "Brightness Swipe Gesture",
                subtitle = "Swipe vertically on the left side of the video to control brightness",
                checked = settings.gestureBrightnessEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(gestureBrightnessEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Playback Defaults", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingRowSwitch(
                title = "Auto-play Next Video",
                subtitle = "Automatically play next item in folder when current video ends",
                checked = settings.autoPlayNext,
                onCheckedChange = { viewModel.updateSettings(settings.copy(autoPlayNext = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingRowSwitch(
                title = "Background Audio Playback",
                subtitle = "Continue playing video audio in background when screen is off or app minimized",
                checked = settings.allowBackgroundPlay,
                onCheckedChange = { viewModel.updateSettings(settings.copy(allowBackgroundPlay = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingRowSwitch(
                title = "Save Playback Position (Resume)",
                subtitle = "Remember last position and resume video playback where you left off",
                checked = settings.savePlaybackPosition,
                onCheckedChange = { viewModel.updateSettings(settings.copy(savePlaybackPosition = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingRowSwitch(
                title = "Hide File Size & Duration",
                subtitle = "Hide video file size and duration on thumbnail cards",
                checked = settings.hideFileSizeAndDuration,
                onCheckedChange = { viewModel.updateSettings(settings.copy(hideFileSizeAndDuration = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAspectDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Default Aspect Ratio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Currently: ${settings.defaultAspectRatio.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSeekDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Double Tap Seek Step", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Currently: ${settings.doubleTapSeekSeconds} seconds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = "Default Playback Speed (${String.format(Locale.getDefault(), "%.2fx", settings.defaultSpeed)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = settings.defaultSpeed,
                    onValueChange = { viewModel.updateSettings(settings.copy(defaultSpeed = it)) },
                    valueRange = 0.25f..3.0f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

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

        if (showSeekDialog) {
            AlertDialog(
                onDismissRequest = { showSeekDialog = false },
                title = { Text("Seek Step Duration") },
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

        if (showDecoderDialog) {
            AlertDialog(
                onDismissRequest = { showDecoderDialog = false },
                title = { Text("Decoder Preference") },
                text = {
                    Column {
                        listOf("HARDWARE_FALLBACK", "HARDWARE_ONLY", "SOFTWARE_ONLY").forEach { pref ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(defaultDecoderPreference = pref))
                                        showDecoderDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.defaultDecoderPreference == pref,
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(defaultDecoderPreference = pref))
                                        showDecoderDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = pref, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showDecoderDialog = false }) { Text("Close") } }
            )
        }
    }
}

@Composable
private fun SettingRowSwitch(
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
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
