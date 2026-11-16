package com.vyro.player.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vyro.player.core.util.FileUtils
import com.vyro.player.core.util.TimeUtils
import com.vyro.player.domain.model.MediaFolder
import com.vyro.player.domain.model.VideoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLibraryScreen(
    viewModel: VideoLibraryViewModel,
    onVideoClick: (Long) -> Unit,
    onNavigateToDecoders: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VYRO Video Player", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.scanMedia() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh / Scan Media")
                    }
                    IconButton(onClick = onNavigateToDecoders) {
                        Icon(Icons.Default.DeveloperBoard, contentDescription = "Decoder Capabilities")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search videos...") },
                singleLine = true
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Videos (${uiState.videos.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Folders (${uiState.folders.size})") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading || uiState.isScanning) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val filteredVideos = if (uiState.searchQuery.isBlank()) {
                        uiState.videos
                    } else {
                        uiState.videos.filter { it.title.contains(uiState.searchQuery, ignoreCase = true) }
                    }

                    if (selectedTab == 0) {
                        VideoList(
                            videos = filteredVideos,
                            onVideoClick = onVideoClick
                        )
                    } else {
                        FolderList(folders = uiState.folders)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoList(
    videos: List<VideoItem>,
    onVideoClick: (Long) -> Unit
) {
    if (videos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No videos found. Tap refresh to scan media.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(videos, key = { it.id }) { video ->
                VideoItemRow(video = video, onClick = { onVideoClick(video.id) })
            }
        }
    }
}

@Composable
fun VideoItemRow(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = TimeUtils.formatDuration(video.duration),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = FileUtils.formatFileSize(video.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun FolderList(folders: List<MediaFolder>) {
    if (folders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No media folders detected.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(folders, key = { it.name }) { folder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(text = folder.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${folder.videoCount} videos • ${FileUtils.formatFileSize(folder.totalSize)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
