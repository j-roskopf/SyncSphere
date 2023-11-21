package com.joetr.sync.sphere.ui.time

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.joetr.sync.sphere.data.RoomRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimeSelectionScreenModel(
    private val roomRepository: RoomRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ScreenModel {
    private val _state = MutableStateFlow<TimeSelectionState>(TimeSelectionState.Loading)
    val state: StateFlow<TimeSelectionState> = _state

    private val _action = MutableSharedFlow<TimeSelectionScreenActions>()
    val action: SharedFlow<TimeSelectionScreenActions> = _action

    private var isInitialized = false
    private lateinit var uiData: List<DayTimeItem>
    private lateinit var roomCode: String

    fun initializeData(times: List<String>, roomCode: String) {
        this.roomCode = roomCode

        if (isInitialized.not()) {
            uiData = times.map {
                DayTimeItem(
                    display = it,
                    dayTime = DayTime.NotSelected,
                )
            }
        }

        _state.value = TimeSelectionState.Content(uiData)

        isInitialized = true
    }

    fun noPreference() {
        uiData = uiData.map { dayTimeItem ->
            dayTimeItem.copy(
                dayTime = DayTime.AllDay,
            )
        }
        _state.value = TimeSelectionState.Content(
            data = uiData,
        )
    }

    fun allDayClickedForItem(index: Int) {
        val currentState = _state.value
        if (currentState is TimeSelectionState.Content) {
            val dayTime = currentState.data[index].dayTime
            if (dayTime is DayTime.AllDay) {
                // if all day is already there and it's selected again, deselect it
                updateTimeAtIndex(index, DayTime.NotSelected)
            } else {
                updateTimeAtIndex(index, DayTime.AllDay)
            }
        } else {
            updateTimeAtIndex(index, DayTime.AllDay)
        }
    }

    fun rangeClickedForItem(index: Int, range: DayTime.Range) {
        updateTimeAtIndex(index, range)
    }

    fun goBackToContentState() {
        _state.value = TimeSelectionState.Content(
            data = uiData,
        )
    }

    fun timeRangeClickedForItem(index: Int) {
        val currentState = _state.value
        if (currentState is TimeSelectionState.Content) {
            val dayTime = currentState.data[index].dayTime
            if (dayTime is DayTime.Range) {
                // if range is already there and it's selected again, deselect it
                updateTimeAtIndex(index, DayTime.NotSelected)
            } else {
                // otherwise go to time selection screen
                _state.value = TimeSelectionState.TimeSelection(
                    index = index,
                )
            }
        } else {
            _state.value = TimeSelectionState.TimeSelection(
                index = index,
            )
        }
    }

    private fun updateTimeAtIndex(index: Int, dayTime: DayTime) {
        uiData = uiData.mapIndexed { mapIndex, dayTimeItem ->
            if (index == mapIndex) {
                dayTimeItem.copy(
                    dayTime = dayTime,
                )
            } else {
                dayTimeItem
            }
        }
        _state.value = TimeSelectionState.Content(
            data = uiData,
        )
    }

    fun submitAvailability(roomCode: String, personId: String) {
        coroutineScope.launch(coroutineDispatcher) {
            _state.value = TimeSelectionState.Loading
            runCatching {
                roomRepository.submitAvailability(
                    roomCode = roomCode,
                    availability = uiData,
                    personId = personId,
                )
            }.fold(
                onSuccess = {
                    _action.emit(TimeSelectionScreenActions.NavigateToResults(roomCode))
                },
                onFailure = {
                    _action.emit(TimeSelectionScreenActions.ErrorOccurred)
                },
            )
        }
    }
}
