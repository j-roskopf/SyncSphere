package com.joetr.sync.sphere.ui.new

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.data.model.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class NewRoomScreenModel(
    private val roomRepository: RoomRepository,
) : ScreenModel {

    var room: Room? = null
    private var selectedDates = emptyList<LocalDate>()
    lateinit var personId: String

    private val _state = MutableStateFlow<NewRoomState>(NewRoomState.Loading)
    val state: StateFlow<NewRoomState> = _state

    fun init(joinedRoom: JoinedRoom?, name: String) {
        coroutineScope.launch(Dispatchers.IO) {
            _state.value = NewRoomState.Loading

            runCatching {
                room = if (joinedRoom != null) {
                    personId = joinedRoom.id

                    joinedRoom.room
                } else {
                    val room = roomRepository.createRoom(name)
                    // if we are creating the room, there is only 1 person in it, store their ID
                    personId = room.people.first().id
                    room
                }

                val roomCode = room!!.roomCode

                roomRepository.saveRoomCodeLocally(roomCode)

                roomRepository.roomUpdates(
                    roomCode = roomCode,
                )
            }.fold(
                onSuccess = { flow ->
                    flow.collect {
                        _state.value = NewRoomState.Content(
                            roomCode = it.roomCode,
                            dates = selectedDates,
                            names = it.people.map { person ->
                                person.name
                            },
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
