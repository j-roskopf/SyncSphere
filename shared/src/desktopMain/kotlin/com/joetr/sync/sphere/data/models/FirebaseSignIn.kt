package com.joetr.sync.sphere.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseSignIn(
    @SerialName("kind") val kind: String,
    @SerialName("idToken") val idToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("expiresIn") val expiresIn: String,
    @SerialName("localId") val localId: String,
)
