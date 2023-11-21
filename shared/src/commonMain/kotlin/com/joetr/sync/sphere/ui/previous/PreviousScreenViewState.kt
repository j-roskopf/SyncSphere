package com.joetr.sync.sphere.ui.previous

import com.joetr.sync.sphere.ui.previous.data.PreviousRoom

sealed interface PreviousScreenViewState {
    data object Loading : PreviousScreenViewState
    data object Error : PreviousScreenViewState
    data object Empty : PreviousScreenViewState
    data class Content(
        val previousRooms: List<PreviousRoom>,
    ) : PreviousScreenViewState
}
