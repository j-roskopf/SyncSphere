package com.joetr.sync.sphere.ui.results.availability

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.sync.sphere.data.Calendar
import com.joetr.sync.sphere.ui.results.availability.data.DayStatus
import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class AvailabilityScreenModel(
    private val calendar: Calendar,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ScreenModel {

    private val _state = MutableStateFlow<AvailabilityScreenState>(AvailabilityScreenState.Loading)
    val state: StateFlow<AvailabilityScreenState> = _state

    /**
     * @param inputData map of display date to availability
     */
    fun init(inputData: Map<String, DayTime>) {
        screenModelScope.launch(coroutineDispatcher) {
            // group input data into days that work (partial, full) and days that do not work
            val uiData = mutableMapOf<DayStatus, List<Pair<String, DayTime>>>()
            uiData[DayStatus.DAY_WORKS] = inputData.filter {
                it.value is DayTime.AllDay || it.value is DayTime.Range
            }.toList().sortedBy {
                it.first
            }

            uiData[DayStatus.DAY_DOES_NOT_WORK] = inputData.filter {
                it.value is DayTime.NotSelected
            }.toList().sortedBy {
                it.first
            }

            _state.emit(
                AvailabilityScreenState.Content(
                    uiData,
                ),
            )
        }
    }

    fun addToCalendar(localDate: LocalDate, dayTime: DayTime) {
        calendar.addToCalendar(localDate, dayTime)
    }
}
