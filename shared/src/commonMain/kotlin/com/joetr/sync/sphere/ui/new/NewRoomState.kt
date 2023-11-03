package com.joetr.sync.sphere.ui.new

sealed interface NewRoomState {
    data object Loading : NewRoomState
    data class Content(
        val roomCode: String,
        val dates: List<String>,
        val names: List<String>,
    ) : NewRoomState
}
