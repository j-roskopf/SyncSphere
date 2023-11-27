package com.joetr.sync.sphere.data.model

import kotlinx.serialization.Serializable
import com.joetr.sync.sphere.util.Serializable as JvmSerialization

@Serializable
data class Finalization(
    val person: People,
    val availability: Availability,
) : JvmSerialization
