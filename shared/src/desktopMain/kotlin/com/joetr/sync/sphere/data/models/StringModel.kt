package com.joetr.sync.sphere.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StringModel(
    @SerialName("stringValue") val stringValue: String,
)
