package com.joetr.sync.sphere.ui.pre

sealed interface PreScreenViewState {
    data object Loading : PreScreenViewState
    data class Content(
        val lastKnownRoomCode: String?,
        val lastKnownName: String,
    ) : PreScreenViewState
}
