package com.joetr.sync.sphere.ui.results

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class ResultsScreenModel(
    private val roomRepository: RoomRepository,
    private val crashReporting: CrashReporting,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val availabilityCalculator: AvailabilityCalculator,
) : ScreenModel {

    private val _state = MutableStateFlow<ResultsScreenState>(ResultsScreenState.Loading)
    val state: StateFlow<ResultsScreenState> = _state

    private val _action = MutableSharedFlow<ResultsScreenAction>()
    val action: SharedFlow<ResultsScreenAction> = _action

    fun initializeData(roomCode: String) {
        screenModelScope.launch(coroutineDispatcher) {
            runCatching {
                flowOf(hasUserSubmittedAvailabilityForRoomCode(roomCode)).zip(
                    roomRepository.roomUpdates(
                        roomCode = roomCode,
                    ),
                ) { hasUserSubmittedAvailability, room ->
                    Pair(hasUserSubmittedAvailability, room)
                }
            }.fold(
                onSuccess = {
                    it.collect { pair ->
                        _state.value = ResultsScreenState.Content(
                            room = pair.second.copy(
                                people = pair.second.people.map { person ->
                                    person.copy(
                                        // sort by availability
                                        availability = person.availability.sortedBy { availability ->
                                            availability.display
                                        },
                                    )
                                },
                            ),
                            hasUserSubmittedAvailability = pair.first,
                        )
                    }
                },
                onFailure = {
                    crashReporting.recordException(it)
                    _state.value = ResultsScreenState.Error
                },
            )
        }
    }

    private suspend fun hasUserSubmittedAvailabilityForRoomCode(roomCode: String): Boolean {
        // default to true if we have no user ID
        val userId = roomRepository.getUserId() ?: return true
        val room = roomRepository.getRoom(roomCode)
        val person = room.people.firstOrNull {
            it.id == userId
        } ?: return true

        return if (person.availability.isEmpty()) {
            false
        } else {
            person.availability.all {
                it.time != DayTime.NotSelected
            }
        }
    }

    fun calculateAvailability(people: List<People>, roomCode: String) {
        screenModelScope.launch(coroutineDispatcher) {
            _state.emit(ResultsScreenState.Loading)
            val localId = roomRepository.getUserId()
            if (localId == null) {
                _state.emit(ResultsScreenState.Error)
            } else {
                val person = people.first {
                    it.id == localId
                }
                val timeRanges = availabilityCalculator.findOverlappingTime(people)
                val uiModel = timeRanges.mapValues {
                    it.value.first().second
                }
                _action.emit(
                    ResultsScreenAction.NavigateToResults(
                        timeRanges = uiModel,
                        person = person,
                        roomCode = roomCode,
                    ),
                )
            }
        }
    }
}
