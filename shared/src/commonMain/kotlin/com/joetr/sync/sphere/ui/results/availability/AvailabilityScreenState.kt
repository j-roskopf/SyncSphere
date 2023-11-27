package com.joetr.sync.sphere.ui.results.availability

import com.joetr.sync.sphere.data.model.Finalization
import com.joetr.sync.sphere.ui.results.availability.data.DayStatus
import com.joetr.sync.sphere.ui.time.DayTime

sealed interface AvailabilityScreenState {
    val key: Int

    data object Loading : AvailabilityScreenState {
        override val key: Int
            get() = 1
    }

    data object Error : AvailabilityScreenState {
        override val key: Int
            get() = 2
    }

    data class Content(
        val data: Map<DayStatus, List<Pair<String, DayTime>>>,
        val finalizations: Map<String, List<Finalization>>,
        override val key: Int = 3,
        val namesThatNeedToFinalize: Map<String, List<String>>,
        val hasUserSubmittedFinalization: Boolean,
        val finalDate: String?,
    ) : AvailabilityScreenState
}
