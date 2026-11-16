package com.vyro.player.domain.usecase

import android.net.Uri
import com.vyro.player.domain.repository.VideoRepository
import javax.inject.Inject

class ScanSafUriUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(treeUri: Uri): Int = videoRepository.scanSafUri(treeUri)
}
