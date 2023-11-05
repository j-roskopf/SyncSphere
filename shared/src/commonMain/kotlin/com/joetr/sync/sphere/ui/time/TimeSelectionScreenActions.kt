package com.joetr.sync.sphere.ui.time

sealed interface TimeSelectionScreenActions {
    data object ErrorOccurred : TimeSelectionScreenActions
    data class NavigateToResults(val roomCode: String) : TimeSelectionScreenActions
}
