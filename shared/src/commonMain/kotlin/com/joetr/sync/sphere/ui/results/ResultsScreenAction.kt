package com.joetr.sync.sphere.ui.results

import com.joetr.sync.sphere.ui.time.DayTime

sealed interface ResultsScreenAction {
    data class NavigateToResults(val timeRanges: Map<String, DayTime>) : ResultsScreenAction
}
