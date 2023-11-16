package com.joetr.sync.sphere.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomDocument(
    @SerialName("name") val name: String,
    @SerialName("createTime") val createTime: String,
    @SerialName("updateTime") val updateTime: String,
    @SerialName("fields") val fields: RoomField,
    @SerialName("error") val error: RoomDocumentError?,
)

@Serializable
data class RoomDocumentField(
    @SerialName("fields") val fields: RoomField,
)

@Serializable
data class RoomDocumentError(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("status") val status: String,
)
