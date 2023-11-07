package com.joetr.sync.sphere.data.model

import kotlinx.serialization.Serializable
import com.joetr.sync.sphere.util.Serializable as JvmSerialization

@Serializable
data class People(
    val availability: List<Availability>,
    val name: String,
    val id: String,
) : JvmSerialization
