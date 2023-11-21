package com.joetr.sync.sphere.ui.results

import com.joetr.sync.sphere.data.model.Room

sealed interface ResultsScreenState {
    data object Loading : ResultsScreenState
    data object Error : ResultsScreenState
    data class Content(val room: Room, val hasUserSubmittedAvailability: Boolean) : ResultsScreenState
}
