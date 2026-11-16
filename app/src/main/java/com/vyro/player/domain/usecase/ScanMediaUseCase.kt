package com.vyro.player.domain.usecase

import com.vyro.player.domain.repository.VideoRepository
import javax.inject.Inject

class ScanMediaUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(): Int = videoRepository.scanMedia()
}
