package com.joetr.sync.sphere.ui.pre

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.joetr.sync.sphere.data.CrashReporting
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.util.displayPlatformName
import com.joetr.sync.sphere.util.randomUUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

// amount of digits to use for anonymous users
private const val MAX_RANDOM_NUMBERS = 3

class PreScreenModel(
    private val dispatcher: CoroutineDispatcher,
    private val roomRepository: RoomRepository,
    private val crashReporting: CrashReporting,
) : ScreenModel {

    private val _state = MutableStateFlow<PreScreenViewState>(PreScreenViewState.Loading)
    val state: StateFlow<PreScreenViewState> = _state

    private val _action = MutableSharedFlow<PreScreenActions>()
    val action: SharedFlow<PreScreenActions> = _action

    private var lastKnownRoomCode: String? = null

    fun init() {
        coroutineScope.launch(dispatcher) {
            lastKnownRoomCode = roomRepository.getLocalRoomCode()

            _state.emit(PreScreenViewState.Content(lastKnownRoomCode))
        }
    }

    fun validateRoomCode(roomCode: String, name: String) {
        coroutineScope.launch(dispatcher) {
            _state.value = PreScreenViewState.Loading
            val roomExists = roomRepository.roomExists(roomCode = roomCode)
            if (roomExists) {
                _action.emit(PreScreenActions.RoomExists(roomCode = roomCode, name = name))
            } else {
                _state.value = PreScreenViewState.Content(lastKnownRoomCode)
                _action.emit(PreScreenActions.RoomDoesNotExist)
            }
        }
    }

    fun joinRoom(roomCode: String, name: String) {
        coroutineScope.launch(dispatcher) {
            val room = roomRepository.getRoom(roomCode = roomCode)
            // check if the user is already in the room
            val localUserId = roomRepository.getUserId()
            val userId = if (localUserId == null) {
                // this user isn't in the room already or has cleared their data
                val userId = randomUUID()
                roomRepository.saveUserIdLocally(userId)
                userId
            } else {
                localUserId
            }

            val newRoom = room.copy(
                numberOfPeople = room.numberOfPeople + 1,
                people = if (localUserId == null) {
                    // if the user hasn't been there before, add them
                    room.people.toMutableList().plus(
                        People(
                            name = name,
                            availability = emptyList(),
                            id = userId,
                        ),
                    )
                } else {
                    // update existing user to have new name
                    val people = room.people.toMutableList()
                    val personIndex = people.indexOfFirst {
                        it.id == localUserId
                    }
                    if (personIndex != -1) {
                        // update with new name
                        people[personIndex] = people[personIndex].copy(
                            name = name,
                        )

                        people
                    } else {
                        // could not find user in list, ideally shouldn't happen, but if so, just add them as new user
                        room.people.toMutableList().plus(
                            People(
                                name = name,
                                availability = emptyList(),
                                id = userId,
                            ),
                        )
                    }
                },
            )
            roomRepository.updateRoom(
                room = newRoom,
            )
            _action.emit(PreScreenActions.NavigateToRoom(newRoom, userId, name))
        }
    }

    internal fun getAnonymousUsername(): String {
        return "Anon".plus(displayPlatformName()).plus(
            Clock.System.now().toEpochMilliseconds().toString().takeLast(
                MAX_RANDOM_NUMBERS,
            ),
        )
    }
}
