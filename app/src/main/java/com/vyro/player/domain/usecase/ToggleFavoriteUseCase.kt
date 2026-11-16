package com.vyro.player.domain.usecase

import com.vyro.player.domain.repository.VideoRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long) = videoRepository.toggleFavorite(videoId)
}
