package com.joetr.sync.sphere.ui.time

import com.joetr.sync.sphere.util.formatTime
import kotlinx.serialization.Serializable

sealed interface TimeSelectionState {
    data object Loading : TimeSelectionState
    data class Content(val data: List<DayTimeItem>) : TimeSelectionState
    data class TimeSelection(val index: Int) : TimeSelectionState
}

data class DayTimeItem(
    val display: String,
    val dayTime: DayTime,
)

@Serializable
sealed class DayTime {
    @Serializable
    data object NotSelected : DayTime()

    @Serializable
    data object AllDay : DayTime()

    @Serializable
    data class Range(
        val startTimeHour: Int,
        val startTimeMinute: Int,
        val endTimeHour: Int,
        val endTimeMinute: Int,
    ) : DayTime()
}

fun DayTime.getDisplayText(): String {
    return when (this) {
        is DayTime.AllDay -> "All Day"
        is DayTime.NotSelected -> "Not Selected"
        is DayTime.Range -> {
            val formattedStartTime = formatTime(this.startTimeHour, this.startTimeMinute)
            val formattedEndTime = formatTime(this.endTimeHour, this.endTimeMinute)

            return formattedStartTime.plus(" - ").plus(formattedEndTime)
        }
    }
}
