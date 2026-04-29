package com.dailypulse.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * 🕒 DATE UTILS
 * Specifically tuned for the NewsData.io UTC format.
 */
object DateUtils {

    fun getTimeAgo(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Recently"

        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val naiveDateTime = LocalDateTime.parse(dateString, formatter)

            // 1. Convert UTC to the phone's Local Timezone
            val utcZoned = naiveDateTime.atZone(ZoneId.of("UTC"))
            val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())

            val publishedAt = localZoned.toLocalDateTime()
            val now = LocalDateTime.now()

            // 2. Calculate the relative difference
            val seconds = ChronoUnit.SECONDS.between(publishedAt, now)
            val minutes = ChronoUnit.MINUTES.between(publishedAt, now)
            val hours = ChronoUnit.HOURS.between(publishedAt, now)
            val days = ChronoUnit.DAYS.between(publishedAt, now)

            // 3. Logic for friendly display
            when {
                // Handle slight clock drifts (future dates)
                seconds < 0 -> "Just now"

                seconds < 60 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days == 1L -> "Yesterday"
                days < 7 -> "${days}d ago"
                else -> {
                    // For news older than a week, use a clean date format
                    publishedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
                }
            }
        } catch (e: Exception) {
            "Recently"
        }
    }
}