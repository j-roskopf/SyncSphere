package com.joetr.sync.sphere.data.model

import com.joetr.sync.sphere.util.Serializable

@Suppress("SerialVersionUIDInSerializableClass")
data class JoinedRoom(
    val room: Room,
    val id: String,
) : Serializable
