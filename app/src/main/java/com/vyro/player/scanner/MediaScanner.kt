package com.vyro.player.scanner

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.documentfile.provider.DocumentFile
import com.vyro.player.domain.model.AudioItem
import com.vyro.player.domain.model.VideoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val thumbnailDir: File by lazy {
        File(context.cacheDir, "vyro_thumbnails").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Scans MediaStore video files across internal and external storage.
     * Extracts full metadata: filename, duration, width/height, bitrate, resolution label, size, date, MIME type, folder, and cached thumbnail.
     * Performs duplicate entry detection.
     */
    fun scanVideos(): List<VideoItem> {
        val videoList = mutableListOf<VideoItem>()
        val uniqueSignatures = HashSet<String>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection.add(MediaStore.Video.Media.BITRATE)
        }

        val selection = "${MediaStore.Video.Media.SIZE} > 0"
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val displayNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val mimeColumn = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dateAddedColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val bucketColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val bitrateColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Video.Media.BITRATE)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val rawTitle = if (titleColumn != -1) cursor.getString(titleColumn) else null
                    val displayName = if (displayNameColumn != -1) cursor.getString(displayNameColumn) else null
                    val title = displayName ?: rawTitle ?: "Video_$id"
                    val path = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""
                    val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                    val size = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L
                    val mimeType = if (mimeColumn != -1) cursor.getString(mimeColumn) ?: "video/*" else "video/*"
                    val dateAdded = if (dateAddedColumn != -1) cursor.getLong(dateAddedColumn) else 0L
                    val dateModified = if (dateModifiedColumn != -1) cursor.getLong(dateModifiedColumn) else 0L
                    val folderName = if (bucketColumn != -1) cursor.getString(bucketColumn) ?: "Internal Storage" else "Internal Storage"

                    var width = if (widthColumn != -1) cursor.getInt(widthColumn) else 0
                    var height = if (heightColumn != -1) cursor.getInt(heightColumn) else 0
                    var bitrate = if (bitrateColumn != -1) cursor.getLong(bitrateColumn) else 0L

                    // Deduplication check based on signature (size + duration + title)
                    val signature = "${size}_${duration}_${title.lowercase(Locale.ROOT)}"
                    if (uniqueSignatures.contains(signature)) continue
                    uniqueSignatures.add(signature)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    val effectivePath = path.ifEmpty { contentUri.toString() }

                    // Fallback metadata extraction if width/height missing
                    if (width <= 0 || height <= 0) {
                        val meta = extractMetadataFromFileOrUri(effectivePath)
                        width = meta.first
                        height = meta.second
                        if (bitrate <= 0L) bitrate = meta.third
                    }

                    val resolutionLabel = formatResolutionLabel(width, height)
                    val thumbnailPath = generateAndCacheThumbnail(id, effectivePath)

                    videoList.add(
                        VideoItem(
                            id = id,
                            title = title,
                            path = effectivePath,
                            duration = duration,
                            size = size,
                            width = width,
                            height = height,
                            resolution = resolutionLabel,
                            bitrate = bitrate,
                            mimeType = mimeType,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            folderName = folderName,
                            thumbnailPath = thumbnailPath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videoList
    }

    /**
     * Scans user-selected folder via Storage Access Framework (SAF) tree URI.
     */
    fun scanSafUriTree(treeUri: Uri): List<VideoItem> {
        val safVideos = mutableListOf<VideoItem>()
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return safVideos

        fun traverseDirectory(doc: DocumentFile) {
            doc.listFiles().forEach { file ->
                if (file.isDirectory) {
                    traverseDirectory(file)
                } else if (file.isFile && (file.type?.startsWith("video/") == true || isVideoFileName(file.name ?: ""))) {
                    val id = file.uri.toString().hashCode().toLong()
                    val title = file.name ?: "SAF_Video_$id"
                    val uriString = file.uri.toString()
                    val duration = 0L
                    val size = file.length()
                    val mimeType = file.type ?: "video/*"
                    val dateModified = file.lastModified()
                    val folderName = doc.name ?: "SAF Storage"

                    val meta = extractMetadataFromFileOrUri(uriString)
                    val width = meta.first
                    val height = meta.second
                    val bitrate = meta.third
                    val resolutionLabel = formatResolutionLabel(width, height)
                    val thumbnailPath = generateAndCacheThumbnail(id, uriString)

                    safVideos.add(
                        VideoItem(
                            id = id,
                            title = title,
                            path = uriString,
                            duration = duration,
                            size = size,
                            width = width,
                            height = height,
                            resolution = resolutionLabel,
                            bitrate = bitrate,
                            mimeType = mimeType,
                            dateAdded = dateModified / 1000,
                            dateModified = dateModified,
                            folderName = folderName,
                            thumbnailPath = thumbnailPath
                        )
                    )
                }
            }
        }

        traverseDirectory(rootDoc)
        return safVideos
    }

    /**
     * Generates and caches thumbnail image file on disk.
     */
    private fun generateAndCacheThumbnail(id: Long, pathOrUri: String): String? {
        val thumbFile = File(thumbnailDir, "thumb_$id.jpg")
        if (thumbFile.exists() && thumbFile.length() > 0) {
            return thumbFile.absolutePath
        }

        var bitmap: Bitmap? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (pathOrUri.startsWith("content://")) {
                    bitmap = context.contentResolver.loadThumbnail(Uri.parse(pathOrUri), Size(320, 240), null)
                } else {
                    val file = File(pathOrUri)
                    if (file.exists()) {
                        bitmap = ThumbnailUtils.createVideoThumbnail(file, Size(320, 240), null)
                    }
                }
            }
        } catch (_: Exception) { }

        if (bitmap == null) {
            try {
                val retriever = MediaMetadataRetriever()
                if (pathOrUri.startsWith("content://")) {
                    retriever.setDataSource(context, Uri.parse(pathOrUri))
                } else {
                    retriever.setDataSource(pathOrUri)
                }
                bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
                retriever.release()
            } catch (_: Exception) { }
        }

        if (bitmap != null) {
            try {
                FileOutputStream(thumbFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                return thumbFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun extractMetadataFromFileOrUri(pathOrUri: String): Triple<Int, Int, Long> {
        var w = 0
        var h = 0
        var br = 0L
        try {
            val retriever = MediaMetadataRetriever()
            if (pathOrUri.startsWith("content://")) {
                retriever.setDataSource(context, Uri.parse(pathOrUri))
            } else {
                retriever.setDataSource(pathOrUri)
            }
            w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            br = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (_: Exception) { }
        return Triple(w, h, br)
    }

    private fun formatResolutionLabel(width: Int, height: Int): String {
        if (width <= 0 || height <= 0) return "SD"
        val minDim = minOf(width, height)
        val maxDim = maxOf(width, height)

        return when {
            maxDim >= 3840 || minDim >= 2160 -> "4K"
            maxDim >= 2560 || minDim >= 1440 -> "2K"
            maxDim >= 1920 || minDim >= 1080 -> "1080p"
            maxDim >= 1280 || minDim >= 720 -> "720p"
            maxDim >= 854 || minDim >= 480 -> "480p"
            else -> "SD"
        }
    }

    private fun isVideoFileName(fileName: String): Boolean {
        val lower = fileName.lowercase(Locale.ROOT)
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") ||
                lower.endsWith(".webm") || lower.endsWith(".mov") || lower.endsWith(".3gp") ||
                lower.endsWith(".flv") || lower.endsWith(".ts") || lower.endsWith(".m4v")
    }

    fun scanAudioTracks(): List<AudioItem> {
        val audioList = mutableListOf<AudioItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "Track_$id" else "Track_$id"
                    val artist = if (artistColumn != -1) cursor.getString(artistColumn) ?: "Unknown Artist" else "Unknown Artist"
                    val album = if (albumColumn != -1) cursor.getString(albumColumn) ?: "Unknown Album" else "Unknown Album"
                    val path = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""
                    val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                    val size = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L
                    val dateAdded = if (dateColumn != -1) cursor.getLong(dateColumn) else 0L
                    val albumId = if (albumIdColumn != -1) cursor.getLong(albumIdColumn) else 0L

                    val albumArtUri = ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()
                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )

                    audioList.add(
                        AudioItem(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            size = size,
                            path = path.ifEmpty { contentUri.toString() },
                            albumArtUri = albumArtUri,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return audioList
    }
}
