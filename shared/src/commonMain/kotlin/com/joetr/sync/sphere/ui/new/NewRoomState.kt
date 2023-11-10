package com.joetr.sync.sphere.ui.new

import kotlinx.datetime.LocalDate

sealed interface NewRoomState {
    data object Loading : NewRoomState
    data object Error : NewRoomState
    data class Content(
        val roomCode: String,
        val dates: List<LocalDate>,
        val names: List<String>,
    ) : NewRoomState
}
