package com.vyro.player.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vyro.player.domain.model.ViewMode
import com.vyro.player.presentation.components.ContinueWatchingCard
import com.vyro.player.presentation.components.FolderCardItem
import com.vyro.player.presentation.components.HistoryCarouselCard
import com.vyro.player.presentation.components.SortFilterDialog
import com.vyro.player.presentation.components.VideoThumbnailCard
import com.vyro.player.presentation.components.VyroBottomBar
import com.vyro.player.presentation.components.VyroHeaderBar
import com.vyro.player.presentation.navigation.Screen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onVideoClick: (Long) -> Unit,
    onFolderClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            VyroHeaderBar(
                title = "VYRO Player",
                searchQuery = uiState.searchQuery,
                isSearchActive = uiState.isSearchActive,
                viewMode = uiState.viewMode,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSearchActiveChange = { viewModel.setSearchActive(it) },
                onToggleViewMode = { viewModel.toggleViewMode() },
                onSortClick = { viewModel.setSortDialogVisible(true) },
                onRefreshClick = { viewModel.scanMedia() }
            )
        },
        bottomBar = {
            VyroBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading || uiState.isScanning) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val filteredVideos = if (uiState.searchQuery.isBlank()) {
                    uiState.videos
                } else {
                    uiState.videos.filter { it.title.contains(uiState.searchQuery, ignoreCase = true) }
                }

                if (uiState.viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // History Section Header
                        if (uiState.recentHistory.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Column {
                                    HomeSectionHeader(
                                        title = "Recently Played",
                                        icon = Icons.Default.History,
                                        onSeeAllClick = { onNavigate(Screen.RecentVideos.route) }
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        items(uiState.recentHistory, key = { it.historyId }) { historyItem ->
                                            HistoryCarouselCard(
                                                history = historyItem,
                                                onClick = { onVideoClick(historyItem.videoId) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }

                        // Continue Watching Banner Card
                        if (uiState.continueWatchingVideo != null) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                ContinueWatchingCard(
                                    video = uiState.continueWatchingVideo!!,
                                    onContinueClick = { onVideoClick(uiState.continueWatchingVideo!!.id) }
                                )
                            }
                        }

                        // Video Folders Preview Section
                        if (uiState.folders.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Column {
                                    HomeSectionHeader(
                                        title = "Video Folders",
                                        icon = Icons.Default.Folder,
                                        onSeeAllClick = { onNavigate(Screen.Folders.route) }
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        items(uiState.folders.take(6), key = { it.name }) { folder ->
                                            FolderCardItem(
                                                folder = folder,
                                                onClick = { onFolderClick(folder.name) },
                                                modifier = Modifier.width(220.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // All Videos Grid Section Header
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            HomeSectionHeader(
                                title = "All Videos (${filteredVideos.size})",
                                icon = Icons.Default.Movie,
                                onSeeAllClick = { onNavigate(Screen.AllVideos.route) }
                            )
                        }

                        // All Videos Grid Items
                        items(filteredVideos, key = { it.id }) { video ->
                            VideoThumbnailCard(
                                video = video,
                                viewMode = ViewMode.GRID,
                                onClick = { onVideoClick(video.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(video.id) },
                                onOptionsClick = { /* Options */ }
                            )
                        }
                    }
                } else {
                    // List View Mode
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.recentHistory.isNotEmpty()) {
                            item {
                                Column {
                                    HomeSectionHeader(
                                        title = "Recently Played",
                                        icon = Icons.Default.History,
                                        onSeeAllClick = { onNavigate(Screen.RecentVideos.route) }
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        items(uiState.recentHistory, key = { it.historyId }) { historyItem ->
                                            HistoryCarouselCard(
                                                history = historyItem,
                                                onClick = { onVideoClick(historyItem.videoId) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }

                        if (uiState.continueWatchingVideo != null) {
                            item {
                                ContinueWatchingCard(
                                    video = uiState.continueWatchingVideo!!,
                                    onContinueClick = { onVideoClick(uiState.continueWatchingVideo!!.id) }
                                )
                            }
                        }

                        item {
                            HomeSectionHeader(
                                title = "All Videos (${filteredVideos.size})",
                                icon = Icons.Default.Movie,
                                onSeeAllClick = { onNavigate(Screen.AllVideos.route) }
                            )
                        }

                        items(filteredVideos, key = { it.id }) { video ->
                            VideoThumbnailCard(
                                video = video,
                                viewMode = ViewMode.LIST,
                                onClick = { onVideoClick(video.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(video.id) },
                                onOptionsClick = { /* Options */ }
                            )
                        }
                    }
                }
            }

            if (uiState.showSortDialog) {
                SortFilterDialog(
                    currentOption = uiState.sortOption,
                    currentOrder = uiState.sortOrder,
                    onOptionSelected = { viewModel.setSortOption(it) },
                    onOrderSelected = { viewModel.setSortOrder(it) },
                    onDismiss = { viewModel.setSortDialogVisible(false) }
                )
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSeeAllClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "See All",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
