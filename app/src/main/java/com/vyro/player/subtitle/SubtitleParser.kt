package com.vyro.player.subtitle

import android.net.Uri
import androidx.media3.common.MimeTypes
import com.vyro.player.domain.model.SubtitleTrack
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitleParser @Inject constructor() {

    fun getSubtitleMimeType(filePath: String): String {
        return when {
            filePath.endsWith(".srt", ignoreCase = true) -> MimeTypes.APPLICATION_SUBRIP
            filePath.endsWith(".vtt", ignoreCase = true) -> MimeTypes.TEXT_VTT
            filePath.endsWith(".ass", ignoreCase = true) || filePath.endsWith(".ssa", ignoreCase = true) -> MimeTypes.TEXT_SSA
            else -> MimeTypes.APPLICATION_SUBRIP
        }
    }

    fun createSubtitleTrackFromFile(file: File): SubtitleTrack {
        val mimeType = getSubtitleMimeType(file.name)
        val nameWithoutExtension = file.nameWithoutExtension
        return SubtitleTrack(
            id = file.absolutePath,
            language = "en",
            label = nameWithoutExtension,
            mimeType = mimeType,
            uri = Uri.fromFile(file).toString()
        )
    }
}
