package com.joetr.sync.sphere.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

actual fun LocalDateTime.format(format: String): String {
    return DateTimeFormatter.ofPattern(format).format(this.toJavaLocalDateTime())
}

actual fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return timeFormat.format(calendar.time)
}
