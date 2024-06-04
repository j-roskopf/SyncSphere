package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.datetime.LocalDate

expect class Calendar {
    fun addToCalendar(localDate: LocalDate, dayTime: DayTime): Boolean
}
