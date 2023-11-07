package com.joetr.sync.sphere.data.model

import kotlinx.serialization.Serializable
import com.joetr.sync.sphere.util.Serializable as JvmSerialization

@Serializable
data class Room(
    val roomCode: String,
    val numberOfPeople: Int,
    val people: List<People>,
    val lastUpdatedTimestamp: Long,
) : JvmSerialization
