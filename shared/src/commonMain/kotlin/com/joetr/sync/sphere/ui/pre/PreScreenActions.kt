package com.joetr.sync.sphere.ui.pre

import com.joetr.sync.sphere.data.model.Room

sealed interface PreScreenActions {
    data object RoomDoesNotExist : PreScreenActions
    data class NavigateToRoom(
        val room: Room,
        val userId: String,
        val name: String,
    ) : PreScreenActions
    data class RoomExists(val roomCode: String, val name: String) : PreScreenActions
}
