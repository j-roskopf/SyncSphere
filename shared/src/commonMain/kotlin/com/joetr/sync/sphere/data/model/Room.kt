package com.joetr.sync.sphere.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Room(
    val roomCode: String,
    val numberOfPeople: Int,
    val people: List<People>,
    val lastUpdatedTimestamp: Long,
)
