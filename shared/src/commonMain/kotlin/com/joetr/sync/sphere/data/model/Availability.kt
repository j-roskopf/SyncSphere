package com.joetr.sync.sphere.data.model

import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.serialization.Serializable
import com.joetr.sync.sphere.util.Serializable as JvmSerialization

@Serializable
data class Availability(
    val time: DayTime,
    val display: String,
) : JvmSerialization
