package com.joetr.sync.sphere.ui.icon

import com.joetr.sync.sphere.ui.icon.data.IconSelection

sealed interface IconSelectionViewState {
    val key: Int

    data object Loading : IconSelectionViewState {
        override val key: Int
            get() = 1
    }

    data class Content(val images: List<IconSelection>, override val key: Int = 2) : IconSelectionViewState
}
