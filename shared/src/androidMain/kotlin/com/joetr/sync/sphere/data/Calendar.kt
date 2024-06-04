package com.joetr.sync.sphere.data

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

actual class Calendar(private val context: Context) {

    actual fun addToCalendar(localDate: LocalDate, dayTime: DayTime): Boolean {
        val intent = Intent(Intent.ACTION_EDIT)
        intent.type = "vnd.android.cursor.item/event"
        if (dayTime is DayTime.AllDay) {
            val localStartDateTime = LocalDateTime(
                localDate.year,
                localDate.month,
                // when doing all day, seems to be off by one day
                localDate.dayOfMonth + 1,
                0,
                0,
            )

            intent.putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                localStartDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            )

            intent.putExtra(CalendarContract.Events.ALL_DAY, true)
        } else if (dayTime is DayTime.Range) {
            val localStartDateTime = LocalDateTime(
                localDate.year,
                localDate.month,
                localDate.dayOfMonth,
                dayTime.startTimeHour,
                dayTime.startTimeMinute,
            )

            val localEndDateTime = LocalDateTime(
                localDate.year,
                localDate.month,
                localDate.dayOfMonth,
                dayTime.endTimeHour,
                dayTime.endTimeMinute,
            )

            intent.putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                localStartDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            )
            intent.putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                localEndDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            )
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

        context.startActivity(intent)

        return true
    }
}
