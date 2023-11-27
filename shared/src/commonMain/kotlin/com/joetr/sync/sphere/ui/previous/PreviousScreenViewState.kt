package com.joetr.sync.sphere.ui.previous

import com.joetr.sync.sphere.ui.previous.data.PreviousRoom

@Suppress("MagicNumber")
sealed interface PreviousScreenViewState {
    val key: Int

    data object Loading : PreviousScreenViewState {
        override val key: Int
            get() = 1
    }

    data object Error : PreviousScreenViewState {
        override val key: Int
            get() = 2
    }

    data object Empty : PreviousScreenViewState {
        override val key: Int
            get() = 3
    }

    data class Content(
        val previousRooms: List<PreviousRoom>,
        override val key: Int = 4,
    ) : PreviousScreenViewState
}
