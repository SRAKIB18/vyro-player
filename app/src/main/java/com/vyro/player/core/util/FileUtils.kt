package com.vyro.player.core.util

import java.util.Locale

object FileUtils {

    /**
     * Formats file size in bytes to human-readable format (KB, MB, GB).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return String.format(Locale.getDefault(), "%.1f %s", value, units[index])
    }

    /**
     * Helper to get extension from path or filename.
     */
    fun getExtension(path: String): String {
        val lastDotIndex = path.lastIndexOf('.')
        return if (lastDotIndex != -1 && lastDotIndex < path.length - 1) {
            path.substring(lastDotIndex + 1).lowercase(Locale.getDefault())
        } else ""
    }
}
