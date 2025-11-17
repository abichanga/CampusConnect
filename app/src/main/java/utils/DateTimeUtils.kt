package utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    /**
     * Format: "Mon, Nov 10 | 2:00 PM - 4:00 PM"
     */
    fun formatEventDate(timestamp: Long): String {
        val date = Date(timestamp)
        val calendar = Calendar.getInstance().apply { time = date }

        val dayFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val dayPart = dayFormat.format(date)
        val timePart = timeFormat.format(date)

        // Assuming event duration is 2 hours (you can make this dynamic)
        calendar.add(Calendar.HOUR, 2)
        val endTime = timeFormat.format(calendar.time)

        return "$dayPart | $timePart - $endTime"
    }

    /**
     * Format: "Nov 10, 2024"
     */
    fun formatShortDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return format.format(date)
    }

    /**
     * Check if event is in the future
     */
    fun isUpcoming(timestamp: Long): Boolean {
        return timestamp > System.currentTimeMillis()
    }

    /**
     * Get relative time: "2 days from now", "3 hours ago"
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = timestamp - now

        return when {
            diff < 0 -> {
                // Past event
                val absDiff = kotlin.math.abs(diff)
                when {
                    absDiff < TimeUnit.HOURS.toMillis(1) -> "Just now"
                    absDiff < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(absDiff)
                        "$hours hours ago"
                    }
                    absDiff < TimeUnit.DAYS.toMillis(30) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(absDiff)
                        "$days days ago"
                    }
                    else -> formatShortDate(timestamp)
                }
            }
            diff < TimeUnit.HOURS.toMillis(1) -> "Starting soon"
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "In $hours hours"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "In $days days"
            }
            else -> formatShortDate(timestamp)
        }
    }
}