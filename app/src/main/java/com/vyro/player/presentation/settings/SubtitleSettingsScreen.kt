package com.vyro.player.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyro.player.domain.model.SubtitleSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubtitleSettingsScreen(
    viewModel: SubtitleSettingsViewModel,
    onBackClick: () -> Unit
) {
    val subtitleSettings by viewModel.subtitleSettingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subtitle Customization") },
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
            // Live Subtitle Preview Card
            Text(
                text = "Live Subtitle Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F101A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = when (subtitleSettings.verticalPosition) {
                        "TOP" -> Alignment.TopCenter
                        "MIDDLE" -> Alignment.Center
                        else -> Alignment.BottomCenter
                    }
                ) {
                    val parsedFontColor = try {
                        Color(android.graphics.Color.parseColor(subtitleSettings.fontColorHex))
                    } catch (_: Exception) {
                        Color.White
                    }

                    val fontFam = when (subtitleSettings.fontFamily) {
                        "MONOSPACE" -> FontFamily.Monospace
                        "SERIF" -> FontFamily.Serif
                        "SANS_SERIF" -> FontFamily.SansSerif
                        else -> FontFamily.Default
                    }

                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = subtitleSettings.backgroundOpacity))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "VYRO Subtitles Live Preview Sample Text",
                            color = parsedFontColor,
                            fontSize = subtitleSettings.fontSizeSp.sp,
                            fontFamily = fontFam,
                            fontWeight = if (subtitleSettings.isBold) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selector
            Text(text = "Preferred Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subtitleSettings.language,
                onValueChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(language = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Font Size
            Text(text = "Font Size (${subtitleSettings.fontSizeSp} sp)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Slider(
                value = subtitleSettings.fontSizeSp.toFloat(),
                onValueChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(fontSizeSp = it.toInt())) },
                valueRange = 12f..36f,
                steps = 11,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Font Family
            Text(text = "Font Family", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DEFAULT", "MONOSPACE", "SANS_SERIF", "SERIF").forEach { family ->
                    AssistChip(
                        onClick = { viewModel.updateSubtitleSettings(subtitleSettings.copy(fontFamily = family)) },
                        label = { Text(family) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (subtitleSettings.fontFamily == family) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font Color
            Text(text = "Font Color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("#FFFFFF", "#FFEB3B", "#00E5FF", "#00E676", "#FF5252").forEach { hex ->
                    val colorVal = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.White }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorVal)
                            .border(
                                width = if (subtitleSettings.fontColorHex == hex) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updateSubtitleSettings(subtitleSettings.copy(fontColorHex = hex)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Background Opacity
            Text(text = "Background Opacity (${(subtitleSettings.backgroundOpacity * 100).toInt()}%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Slider(
                value = subtitleSettings.backgroundOpacity,
                onValueChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(backgroundOpacity = it)) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Positioning
            Text(text = "Vertical Alignment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BOTTOM", "MIDDLE", "TOP").forEach { pos ->
                    AssistChip(
                        onClick = { viewModel.updateSubtitleSettings(subtitleSettings.copy(verticalPosition = pos)) },
                        label = { Text(pos) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (subtitleSettings.verticalPosition == pos) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bold & Outline Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Bold Text", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = subtitleSettings.isBold,
                    onCheckedChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(isBold = it)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Text Outline / Shadow", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = subtitleSettings.hasOutline,
                    onCheckedChange = { viewModel.updateSubtitleSettings(subtitleSettings.copy(hasOutline = it)) }
                )
            }
        }
    }
}
