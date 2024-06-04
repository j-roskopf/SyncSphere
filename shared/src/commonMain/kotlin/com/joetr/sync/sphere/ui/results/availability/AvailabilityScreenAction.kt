package com.joetr.sync.sphere.ui.results.availability

sealed interface AvailabilityScreenAction {
    data object AddToCalendarError : AvailabilityScreenAction
}
