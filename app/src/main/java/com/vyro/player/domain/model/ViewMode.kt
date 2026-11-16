package com.vyro.player.domain.model

enum class ViewMode {
    GRID,
    LIST
}

enum class SortOption {
    NAME,
    DATE,
    SIZE,
    DURATION,
    RESOLUTION
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

enum class ResolutionFilter(val displayName: String) {
    ALL("All Resolutions"),
    RES_4K("4K Ultra HD"),
    RES_1080P("1080p Full HD"),
    RES_720P("720p HD"),
    RES_SD("SD Standard")
}
