package com.joetr.sync.sphere.ui.results.availability

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.data.Calendar
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.model.Finalization
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.results.availability.data.DayStatus
import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class AvailabilityScreenModel(
    private val calendar: Calendar,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val roomRepository: RoomRepository,
    private val crashReporting: CrashReporting,
) : ScreenModel {

    private val _state = MutableStateFlow<AvailabilityScreenState>(AvailabilityScreenState.Loading)
    val state: StateFlow<AvailabilityScreenState> = _state

    private val _action = MutableSharedFlow<AvailabilityScreenAction>()
    val action: SharedFlow<AvailabilityScreenAction> = _action

    /**
     * @param inputData map of display date to availability
     */
    fun init(inputData: Map<String, DayTime>, roomCode: String, person: People) {
        screenModelScope.launch(coroutineDispatcher) {
            runCatching {
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

                AvailabilityScreenModelData(
                    uiData = uiData,
                    roomFlow = roomRepository.roomUpdates(
                        roomCode = roomCode,
                    ),
                )
            }.fold(
                onSuccess = { availabilityScreenModelData ->
                    availabilityScreenModelData.roomFlow.collect { room ->
                        val finalizations = runCatching {
                            calculateFinalizations(
                                room = room,
                            )
                        }.fold(
                            onSuccess = {
                                it
                            },
                            onFailure = {
                                emptyMap()
                            },
                        )

                        val namesThatNeedToFinalize = runCatching {
                            calculateNameFinalizations(
                                room = room,
                                uiData = availabilityScreenModelData.uiData,
                                finalizations = finalizations,
                            )
                        }.fold(
                            onSuccess = {
                                it
                            },
                            onFailure = {
                                emptyMap()
                            },
                        )

                        val hasUserSubmittedFinalization = room.finalizations.any {
                            it.person.id == person.id
                        }

                        val finalizationsList = finalizations.toList()
                        val finalizedDateIndex = finalizationsList.indexOfFirst {
                            it.second.size == room.people.size
                        }
                        val finalDate = if (finalizedDateIndex >= 0) {
                            finalizationsList[finalizedDateIndex].first
                        } else {
                            null
                        }

                        _state.value = AvailabilityScreenState.Content(
                            data = availabilityScreenModelData.uiData,
                            finalizations = finalizations,
                            namesThatNeedToFinalize = namesThatNeedToFinalize,
                            hasUserSubmittedFinalization = hasUserSubmittedFinalization,
                            finalDate = finalDate,
                        )
                    }
                },
                onFailure = {
                    crashReporting.recordException(it)
                    _state.value = AvailabilityScreenState.Error
                },
            )
        }
    }

    private fun calculateFinalizations(room: Room): MutableMap<String, List<Finalization>> {
        val finalizations = mutableMapOf<String, List<Finalization>>()
        room.finalizations.forEach { finalization ->
            if (finalizations.containsKey(finalization.availability.display)) {
                finalizations[finalization.availability.display] =
                    finalizations[finalization.availability.display]!! + finalization
            } else {
                finalizations[finalization.availability.display] = listOf(finalization)
            }
        }

        return finalizations
    }

    @Suppress("NestedBlockDepth")
    private fun calculateNameFinalizations(
        room: Room,
        uiData: Map<DayStatus, List<Pair<String, DayTime>>>,
        finalizations: Map<String, List<Finalization>>,
    ): MutableMap<String, List<String>> {
        val namesThatNeedToFinalize = mutableMapOf<String, List<String>>()
        uiData[DayStatus.DAY_WORKS]!!.forEach { uiDataItem ->
            room.people.forEach { person ->
                if (finalizations.containsKey(uiDataItem.first).not()) {
                    // no finalizations exist for this day, so everyone needs to finalize
                    namesThatNeedToFinalize[uiDataItem.first] =
                        (namesThatNeedToFinalize[uiDataItem.first] ?: emptyList()) + listOf(
                            person.name,
                        )
                } else {
                    if (finalizations[uiDataItem.first]?.any {
                            it.person.id == person.id
                        } == false
                    ) {
                        // they have not finalized
                        namesThatNeedToFinalize[uiDataItem.first] =
                            (
                                namesThatNeedToFinalize[uiDataItem.first]
                                    ?: emptyList()
                                ) + listOf(person.name)
                    }
                }
            }

            if (namesThatNeedToFinalize.containsKey(uiDataItem.first).not()) {
                // everyone needs to finalize
                namesThatNeedToFinalize[uiDataItem.first] = emptyList()
            }
        }

        return namesThatNeedToFinalize
    }

    fun addToCalendar(localDate: LocalDate, dayTime: DayTime) {
        val result = calendar.addToCalendar(localDate, dayTime)
        if (result.not()) {
            screenModelScope.launch(coroutineDispatcher) {
                // an error occurred
                _action.emit(AvailabilityScreenAction.AddToCalendarError)
            }
        }
    }

    /**
     * Person finalizes their selection for a given room code on a given local date
     * for a given day time.
     */
    fun finalize(person: People, roomCode: String, localDate: LocalDate, dayTime: DayTime) {
        screenModelScope.launch(coroutineDispatcher) {
            _state.value = AvailabilityScreenState.Loading
            roomRepository.finalize(person, roomCode, localDate, dayTime)
        }
    }

    fun undoFinalization(person: People, roomCode: String) {
        screenModelScope.launch(coroutineDispatcher) {
            _state.value = AvailabilityScreenState.Loading
            roomRepository.undoFinalization(
                person = person,
                roomCode = roomCode,
            )
        }
    }
}

private data class AvailabilityScreenModelData(
    val uiData: Map<DayStatus, List<Pair<String, DayTime>>>,
    val roomFlow: Flow<Room>,
)
