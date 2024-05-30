package com.joetr.sync.sphere.ui.new

import kotlinx.datetime.LocalDate

sealed interface NewRoomState {
    // used as animation key
    val key: Int

    data object Loading : NewRoomState {
        override val key: Int
            get() = 1
    }

    data class Error(val message: String) : NewRoomState {
        override val key: Int
            get() = 2
    }

    data class Content(
        val roomCode: String,
        val dates: List<LocalDate>,
        val names: List<String>,
        val userPreferenceIcon: String?,
        override val key: Int = 3,
    ) : NewRoomState
}
