package com.vyro.player.domain.usecase

import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioTracksUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    operator fun invoke(): Flow<List<AudioItem>> = audioRepository.getAllAudioTracks()
}
