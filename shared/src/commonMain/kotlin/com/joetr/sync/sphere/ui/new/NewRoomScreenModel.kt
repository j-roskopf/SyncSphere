package com.joetr.sync.sphere.ui.new

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.data.model.Room
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class NewRoomScreenModel(
    private val roomRepository: RoomRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ScreenModel {

    var room: Room? = null
    private var userPreferenceIcon: String? = null
    private var selectedDates = emptyList<LocalDate>()
    lateinit var personId: String

    private val _state = MutableStateFlow<NewRoomState>(NewRoomState.Loading)
    val state: StateFlow<NewRoomState> = _state

    fun init(joinedRoom: JoinedRoom?, name: String) {
        screenModelScope.launch(coroutineDispatcher) {
            _state.value = NewRoomState.Loading

            runCatching {
                if (room == null) {
                    room = if (joinedRoom != null) {
                        personId = joinedRoom.id

                        joinedRoom.room
                    } else {
                        val room = roomRepository.createRoom(name)
                        // if we are creating the room, there is only 1 person in it, store their ID
                        personId = room.people.first().id
                        room
                    }
                }

                val roomCode = room!!.roomCode

                roomRepository.saveRoomCodeLocally(roomCode)

                roomRepository.saveNameLocally(name)

                userPreferenceIcon = roomRepository.getLocalIcon()

                roomRepository.roomUpdates(
                    roomCode = roomCode,
                )
            }.fold(
                onSuccess = { flow ->
                    flow.collect { room ->
                        // check if the person has dates already
                        val personDates = room.people.firstOrNull { person ->
                            person.id == personId
                        }?.availability?.map {
                            LocalDate.parse(it.display)
                        }
                        if (!personDates.isNullOrEmpty()) {
                            selectedDates = personDates
                        }
                        _state.value = NewRoomState.Content(
                            roomCode = room.roomCode,
                            dates = selectedDates,
                            names = room.people.map { person ->
                                person.name
                            },
                            userPreferenceIcon = userPreferenceIcon,
                        )
                    }
                },
                onFailure = {
                    _state.value = NewRoomState.Error
                },
            )
        }
    }

    fun addDates(dates: List<LocalDate>) {
        val state = _state.value
        if (state is NewRoomState.Content) {
            selectedDates = dates
            _state.value = state.copy(
                dates = dates,
            )
        }
    }
}
