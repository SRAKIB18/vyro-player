package com.vyro.player.domain.usecase

import com.vyro.player.domain.repository.AudioRepository
import javax.inject.Inject

class ScanAudioMediaUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): Int = audioRepository.scanAudioMedia()
}
