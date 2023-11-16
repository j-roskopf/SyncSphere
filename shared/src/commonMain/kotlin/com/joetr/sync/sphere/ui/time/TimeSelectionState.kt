package com.joetr.sync.sphere.ui.time

import com.joetr.sync.sphere.util.formatTime
import kotlinx.serialization.Serializable
import com.joetr.sync.sphere.util.Serializable as JvmSerialization

sealed interface TimeSelectionState {
    // used as animation key
    val key: Int

    data object Loading : TimeSelectionState {
        override val key: Int
            get() = 1
    }

    data class Content(val data: List<DayTimeItem>, override val key: Int = 1) : TimeSelectionState
    data class TimeSelection(val index: Int, override val key: Int = 3) : TimeSelectionState
}

data class DayTimeItem(
    val display: String,
    val dayTime: DayTime,
)

@Serializable
sealed class DayTime {

    @Serializable
    data object NotSelected : DayTime(), JvmSerialization

    @Serializable
    data object AllDay : DayTime(), JvmSerialization

    @Serializable
    data class Range(
        val startTimeHour: Int,
        val startTimeMinute: Int,
        val endTimeHour: Int,
        val endTimeMinute: Int,
    ) : DayTime(), JvmSerialization
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
