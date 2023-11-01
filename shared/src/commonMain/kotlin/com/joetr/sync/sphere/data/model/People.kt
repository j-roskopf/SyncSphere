package com.joetr.sync.sphere.data.model

import kotlinx.serialization.Serializable

@Serializable
data class People(
    val availability: List<Availability>,
    val name: String,
    val id: String,
)
