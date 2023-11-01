package com.joetr.sync.sphere.ui.results

import com.joetr.sync.sphere.ui.results.data.TimeRange

sealed interface ResultsScreenAction {
    data class NavigateToResults(val timeRanges: Map<String, TimeRange>) : ResultsScreenAction
}
