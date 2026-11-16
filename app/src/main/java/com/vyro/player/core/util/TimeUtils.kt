package com.vyro.player.core.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    /**
     * Formats duration in milliseconds to HH:MM:SS or MM:SS format.
     */
    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"

        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
