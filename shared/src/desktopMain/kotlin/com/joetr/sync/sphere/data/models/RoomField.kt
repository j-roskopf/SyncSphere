package com.joetr.sync.sphere.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomField(
    @SerialName("numberOfPeople") val numberOfPeople: IntegerModel,
    @SerialName("lastUpdatedTimestamp") val lastUpdatedTimestamp: IntegerModel,
    @SerialName("roomCode") val roomCode: StringModel,
    @SerialName("people") val people: PeopleArrayModel,
    @SerialName("finalizations") val finalizations: FinalizationsArrayModel,
)
