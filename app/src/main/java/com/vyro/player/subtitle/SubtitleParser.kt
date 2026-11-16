package com.vyro.player.subtitle

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.vyro.player.domain.model.SubtitleTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class SubtitleParser @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getSubtitleMimeType(pathOrUri: String): String {
        val lower = pathOrUri.lowercase(Locale.ROOT)
        return when {
            lower.contains(".vtt") -> MimeTypes.TEXT_VTT
            lower.contains(".ass") || lower.contains(".ssa") -> MimeTypes.TEXT_SSA
            lower.contains(".ttml") || lower.contains(".xml") -> MimeTypes.APPLICATION_TTML
            else -> MimeTypes.APPLICATION_SUBRIP
        }
    }

    fun buildSubtitleConfiguration(uri: Uri, label: String = "External Subtitle", language: String = "en"): MediaItem.SubtitleConfiguration {
        val mimeType = getSubtitleMimeType(uri.toString())
        return MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mimeType)
            .setLanguage(language)
            .setLabel(label)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
    }

    fun createSubtitleTrackFromFile(file: File): SubtitleTrack {
        val mimeType = getSubtitleMimeType(file.name)
        val nameWithoutExtension = file.nameWithoutExtension
        return SubtitleTrack(
            id = file.absolutePath,
            language = "en",
            label = nameWithoutExtension,
            mimeType = mimeType,
            uri = Uri.fromFile(file).toString(),
            isExternal = true
        )
    }

    fun createSubtitleTrackFromUri(uri: Uri, label: String): SubtitleTrack {
        val mimeType = getSubtitleMimeType(uri.toString())
        return SubtitleTrack(
            id = uri.toString(),
            language = "en",
            label = label,
            mimeType = mimeType,
            uri = uri.toString(),
            isExternal = true
        )
    }
}
