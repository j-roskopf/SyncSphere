package com.joetr.sync.sphere.data

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.nio.charset.StandardCharsets

private const val PAYLOAD = 1
private const val JWT_PARTS = 3

class JwtParser {

    fun isTokenExpired(jwtToken: String): Boolean {
        if (jwtToken.isEmpty()) return true

        val payload = getPayload(jwtToken)
        val exp = payload?.get("exp")?.toString()?.toLong()
        return if (exp == null) {
            true
        } else {
            return exp <= Clock.System.now().epochSeconds
        }
    }

    /**
     * Returns payload of a JWT as a JSON object.
     *
     * @param jwt REQUIRED: valid JSON Web Token as String.
     * @return payload as a JSONObject.
     */
    @Suppress("SwallowedException")
    private fun getPayload(jwt: String): JsonObject? {
        try {
            validateJWT(jwt)
            val payload = jwt.split("\\.".toRegex()).toTypedArray()[PAYLOAD]
            val sectionDecoded = java.util.Base64.getDecoder().decode(payload)
            val jwtSection = String(sectionDecoded, StandardCharsets.UTF_8)
            return Json.decodeFromString<JsonObject>(jwtSection)
        } catch (e: Exception) {
            // swallowed for now
        }
        return null
    }

    /**
     * Checks if `JWT` is a valid JSON Web Token.
     *
     * @param jwt REQUIRED: The JWT as a [String].
     */
    @Throws(IllegalArgumentException::class)
    fun validateJWT(jwt: String) {
        // Check if the the JWT has the three parts
        val jwtParts = jwt.split("\\.".toRegex()).toTypedArray()
        if (jwtParts.size != JWT_PARTS) {
            throw IllegalArgumentException("Bad JWT format")
        }
    }
}
