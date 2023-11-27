package com.joetr.sync.sphere.ui.previous

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.data.RoomRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreviousScreenModel(
    private val dispatcher: CoroutineDispatcher,
    private val roomRepository: RoomRepository,
    private val crashReporting: CrashReporting,
) : ScreenModel {

    private val _state = MutableStateFlow<PreviousScreenViewState>(PreviousScreenViewState.Loading)
    val state: StateFlow<PreviousScreenViewState> = _state

    fun init() {
        screenModelScope.launch(dispatcher) {
            runCatching {
                roomRepository.getLocalRoomCodes()
            }.fold(
                onSuccess = {
                    if (it.isEmpty()) {
                        _state.value = PreviousScreenViewState.Empty
                    } else {
                        _state.value = PreviousScreenViewState.Content(it)
                    }
                },
                onFailure = {
                    crashReporting.recordException(it)
                    _state.value = PreviousScreenViewState.Error
                },
            )
        }
    }

    fun deleteRoom(roomCode: String) {
        screenModelScope.launch(dispatcher) {
            runCatching {
                roomRepository.deleteRoomLocally(roomCode)

                roomRepository.getLocalRoomCodes()
            }.fold(
                onSuccess = {
                    if (it.isEmpty()) {
                        _state.value = PreviousScreenViewState.Empty
                    } else {
                        _state.value = PreviousScreenViewState.Content(it)
                    }
                },
                onFailure = {
                    crashReporting.recordException(it)
                    _state.value = PreviousScreenViewState.Error
                },
            )
        }
    }
}
