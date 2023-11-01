package com.joetr.sync.sphere.ui.results.data

data class TimeRange(val startTime: Time, val endTime: Time) {
    override fun toString(): String {
        return "${startTime.hour}:${startTime.minute} - ${endTime.hour}:${endTime.minute}"
    }
}

val ALL_DAY = TimeRange(
    startTime = Time(0, 0),
    endTime = Time(23, 59),
)

val NONE = TimeRange(
    startTime = Time(0, 0),
    endTime = Time(0, 0),
)
