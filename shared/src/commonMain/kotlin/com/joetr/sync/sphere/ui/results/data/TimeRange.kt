package com.joetr.sync.sphere.ui.results.data

data class TimeRange(val startTime: Time, val endTime: Time)

@Suppress("MagicNumber")
val ALL_DAY = TimeRange(
    startTime = Time(0, 0),
    endTime = Time(23, 59),
)

@Suppress("MagicNumber")
val NONE = TimeRange(
    startTime = Time(0, 0),
    endTime = Time(0, 0),
)
