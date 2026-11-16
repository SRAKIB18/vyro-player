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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSettingsScreen(
    viewModel: SubtitleSettingsViewModel,
    onBackClick: () -> Unit
) {
    val subtitleSettings by viewModel.subtitleSettingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subtitle Settings") },
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
            Text(text = "Preferred Language", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subtitleSettings.language,
                onValueChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(language = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Font Size (${subtitleSettings.fontSizeSp} sp)", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = subtitleSettings.fontSizeSp.toFloat(),
                onValueChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(fontSizeSp = it.toInt())) },
                valueRange = 12f..36f,
                steps = 11,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Background Opacity (${(subtitleSettings.backgroundOpacity * 100).toInt()}%)", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = subtitleSettings.backgroundOpacity,
                onValueChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(backgroundOpacity = it)) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
