package com.joetr.sync.sphere.ui.results.availability

import com.joetr.sync.sphere.ui.results.availability.data.DayStatus
import com.joetr.sync.sphere.ui.time.DayTime

sealed interface AvailabilityScreenState {
    data object Loading : AvailabilityScreenState
    data class Content(val data: Map<DayStatus, List<Pair<String, DayTime>>>) : AvailabilityScreenState
}
