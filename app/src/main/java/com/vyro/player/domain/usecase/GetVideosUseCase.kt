package com.vyro.player.domain.usecase

import com.vyro.player.domain.model.VideoItem
import com.vyro.player.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(): Flow<List<VideoItem>> = videoRepository.getAllVideos()
}
