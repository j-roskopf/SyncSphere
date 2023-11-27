package com.joetr.sync.sphere.ui.results

import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.ui.time.DayTime

sealed interface ResultsScreenAction {
    data class NavigateToResults(
        val person: People,
        val timeRanges: Map<String, DayTime>,
        val roomCode: String,
    ) : ResultsScreenAction
}
