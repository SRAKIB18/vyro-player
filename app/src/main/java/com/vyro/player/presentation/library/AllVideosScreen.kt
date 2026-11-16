package com.vyro.player.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vyro.player.domain.model.ViewMode
import com.vyro.player.presentation.components.SortFilterDialog
import com.vyro.player.presentation.components.VideoThumbnailCard
import com.vyro.player.presentation.components.VyroBottomBar
import com.vyro.player.presentation.components.VyroHeaderBar

@Composable
fun AllVideosScreen(
    viewModel: AllVideosViewModel,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onVideoClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            VyroHeaderBar(
                title = "All Videos (${uiState.videos.size})",
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

                if (filteredVideos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No videos found.")
                    }
                } else if (uiState.viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredVideos, key = { it.id }) { video ->
                            VideoThumbnailCard(
                                video = video,
                                viewMode = ViewMode.GRID,
                                onClick = { onVideoClick(video.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(video.id) },
                                onOptionsClick = { }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredVideos, key = { it.id }) { video ->
                            VideoThumbnailCard(
                                video = video,
                                viewMode = ViewMode.LIST,
                                onClick = { onVideoClick(video.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(video.id) },
                                onOptionsClick = { }
                            )
                        }
                    }
                }
            }

            if (uiState.showSortDialog) {
                SortFilterDialog(
                    currentOption = uiState.sortOption,
                    currentOrder = uiState.sortOrder,
                    currentResolutionFilter = uiState.resolutionFilter,
                    onOptionSelected = { viewModel.setSortOption(it) },
                    onOrderSelected = { viewModel.setSortOrder(it) },
                    onResolutionSelected = { viewModel.setResolutionFilter(it) },
                    onDismiss = { viewModel.setSortDialogVisible(false) }
                )
            }
        }
    }
}
