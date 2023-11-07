package com.joetr.sync.sphere.ui.results

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.ui.results.data.ALL_DAY
import com.joetr.sync.sphere.ui.results.data.NONE
import com.joetr.sync.sphere.ui.results.data.Time
import com.joetr.sync.sphere.ui.results.data.TimeRange
import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResultsScreenModel(
    private val dispatcher: CoroutineDispatcher,
    private val roomRepository: RoomRepository,
) : ScreenModel {

    private val _state = MutableStateFlow<ResultsScreenState>(ResultsScreenState.Loading)
    val state: StateFlow<ResultsScreenState> = _state

    private val _action = MutableSharedFlow<ResultsScreenAction>()
    val action: SharedFlow<ResultsScreenAction> = _action

    fun initializeData(roomCode: String) {
        coroutineScope.launch(dispatcher) {
            roomRepository.roomUpdates(
                roomCode = roomCode,
            ).collect {
                _state.value = ResultsScreenState.Content(
                    room = it,
                )
            }
        }
    }

    fun calculateAvailability(people: List<People>) {
        coroutineScope.launch(dispatcher) {
            _state.emit(ResultsScreenState.Loading)
            val timeRanges = mutableMapOf<String, TimeRange>()
            people.forEach { currentPerson ->
                currentPerson.availability.forEach { availability ->
                    timeRanges[availability.display] = findOverlapTimeForDay(
                        day = availability.display,
                        people = people,
                    )
                }
            }
            _action.emit(
                ResultsScreenAction.NavigateToResults(
                    convertDataToUiModel(timeRanges),
                ),
            )
        }
    }

    private fun convertDataToUiModel(dataModel: Map<String, TimeRange>): Map<String, DayTime> {
        val toReturn = mutableMapOf<String, DayTime>()
        val sortedList: List<Map.Entry<String, TimeRange>> = dataModel.entries.toList().sortedBy {
            it.key
        }
        sortedList.forEach {
            val dayTime = when (it.value) {
                ALL_DAY -> DayTime.AllDay
                NONE -> DayTime.NotSelected
                else -> {
                    DayTime.Range(
                        it.value.startTime.hour,
                        it.value.startTime.minute,
                        it.value.endTime.hour,
                        it.value.endTime.minute,
                    )
                }
            }
            toReturn[it.key] = dayTime
        }
        return toReturn
    }

    private fun findOverlapTimeForDay(day: String, people: List<People>): TimeRange {
        val dayTimeRanges = people.map { person ->
            person.availableTimeRangesForDay(day)
        }

        return findOverlapTimeRanges(dayTimeRanges)
    }

    private fun findOverlapTimeRanges(timeRanges: List<TimeRange>): TimeRange {
        if (timeRanges.isEmpty()) return NONE

        val sortedRanges =
            timeRanges.sortedWith(compareBy<TimeRange> { it.startTime.hour }.thenBy { it.startTime.minute })
        var overlapResult: TimeRange

        var currentStart = sortedRanges[0].startTime
        var currentEnd = sortedRanges[0].endTime

        for (i in 1 until sortedRanges.size) {
            val nextRange = sortedRanges[i]

            if (nextRange.startTime <= currentEnd) {
                currentEnd = minOf(currentEnd, nextRange.endTime)
                currentStart = maxOf(currentStart, nextRange.startTime)
            } else {
                currentStart = nextRange.startTime
                currentEnd = nextRange.endTime
            }
        }

        overlapResult = TimeRange(currentStart, currentEnd)

        return overlapResult
    }

    private fun minOf(a: Time, b: Time): Time {
        return if (a >= b) {
            b
        } else {
            a
        }
    }

    private fun maxOf(a: Time, b: Time): Time {
        return if (a >= b) {
            a
        } else {
            b
        }
    }

    private fun People.availableTimeRangesForDay(day: String): TimeRange {
        val timeForDay = this.availability.firstOrNull {
            day == it.display
        }?.time

        return when (timeForDay) {
            is DayTime.AllDay -> ALL_DAY
            is DayTime.NotSelected -> NONE
            is DayTime.Range ->
                TimeRange(
                    Time(
                        timeForDay.startTimeHour,
                        timeForDay.startTimeMinute,
                    ),
                    Time(timeForDay.endTimeHour, timeForDay.endTimeMinute),
                )

            null -> NONE
        }
    }
}
