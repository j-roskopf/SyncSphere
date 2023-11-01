package com.joetr.sync.sphere.data.model

import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.serialization.Serializable

@Serializable
data class Availability(
    val time: DayTime,
    val display: String,
)
