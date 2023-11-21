package com.joetr.sync.sphere.ui.pre

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
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
private const val MAX_ANONYMOUS_USERNAME_RANDOM_NUMBERS = 3

class PreScreenModel(
    private val dispatcher: CoroutineDispatcher,
    private val roomRepository: RoomRepository,
) : ScreenModel {

    private val _state = MutableStateFlow<PreScreenViewState>(PreScreenViewState.Loading)
    val state: StateFlow<PreScreenViewState> = _state

    private val _action = MutableSharedFlow<PreScreenActions>()
    val action: SharedFlow<PreScreenActions> = _action

    private var userIconPreference: String? = null
    private lateinit var lastKnownName: String

    fun init() {
        coroutineScope.launch(dispatcher) {
            roomRepository.signInAnonymouslyIfNeeded()

            userIconPreference = roomRepository.getLocalIcon()

            lastKnownName = roomRepository.getLocalName()

            _state.emit(
                PreScreenViewState.Content(
                    lastKnownName = lastKnownName,
                    userIconPreference = userIconPreference,
                ),
            )
        }
    }

    fun validateRoomCode(roomCode: String, name: String) {
        coroutineScope.launch(dispatcher) {
            _state.value = PreScreenViewState.Loading
            val roomExists = roomRepository.roomExists(roomCode = roomCode)
            if (roomExists) {
                _action.emit(PreScreenActions.RoomExists(roomCode = roomCode, name = name))
            } else {
                _state.value = PreScreenViewState.Content(lastKnownName, userIconPreference)
                _action.emit(PreScreenActions.RoomDoesNotExist)
            }
        }
    }

    fun joinRoom(roomCode: String, name: String) {
        coroutineScope.launch(dispatcher) {
            val room = roomRepository.getRoom(roomCode = roomCode)
            // check if the user is already in the room
            var localUserId = roomRepository.getUserId()
            val userId = if (localUserId == null) {
                // this user isn't in the room already or has cleared their data
                val tempId = randomUUID()
                roomRepository.saveUserIdLocally(tempId)
                tempId
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
                userName = name,
                userId = userId,

            )
            _action.emit(PreScreenActions.NavigateToRoom(newRoom, userId, name))
        }
    }

    internal fun tryAgain(roomCode: String) {
        val lastState = state.value
        if (lastState is PreScreenViewState.Content) {
            validateRoomCode(
                roomCode = roomCode,
                name = lastState.lastKnownName,
            )
        } else {
            // fall back to default state
            init()
        }
    }

    internal fun getAnonymousUsername(): String {
        return "Anon".plus(displayPlatformName()).plus(
            Clock.System.now().toEpochMilliseconds().toString().takeLast(
                MAX_ANONYMOUS_USERNAME_RANDOM_NUMBERS,
            ),
        )
    }
}
