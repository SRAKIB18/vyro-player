package com.vyro.player.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun VideoThumbnailImage(
    videoPath: String,
    modifier: Modifier = Modifier,
    thumbnailPath: String? = null,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = videoPath, key2 = thumbnailPath) {
        value = withContext(Dispatchers.IO) {
            var bitmap: Bitmap? = null

            // 1. Check cached thumbnail path on disk first
            if (!thumbnailPath.isNullOrEmpty()) {
                val cachedFile = File(thumbnailPath)
                if (cachedFile.exists() && cachedFile.length() > 0) {
                    try {
                        bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
                    } catch (_: Exception) { }
                }
            }

            // 2. Android MediaStore Thumbnail API
            if (bitmap == null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (videoPath.startsWith("content://")) {
                            bitmap = context.contentResolver.loadThumbnail(Uri.parse(videoPath), Size(320, 240), null)
                        } else {
                            val file = File(videoPath)
                            if (file.exists()) {
                                bitmap = ThumbnailUtils.createVideoThumbnail(file, Size(320, 240), null)
                            }
                        }
                    } else {
                        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND)
                    }
                } catch (_: Exception) { }
            }

            // 3. MediaMetadataRetriever Frame Fallback
            if (bitmap == null) {
                try {
                    val retriever = MediaMetadataRetriever()
                    if (videoPath.startsWith("content://")) {
                        retriever.setDataSource(context, Uri.parse(videoPath))
                    } else {
                        retriever.setDataSource(videoPath)
                    }
                    bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                    retriever.release()
                } catch (_: Exception) { }
            }

            bitmap?.asImageBitmap()
        }
    }

    val imageBitmap = bitmapState.value
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF161824)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
