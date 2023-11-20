package com.joetr.sync.sphere.data

private const val DEBUG_ROOM_COLLECTION = "RoomsTesting"
private const val ROOM_COLLECTION = "RoomsAuthenticated"

class RoomConstants(private val buildConfig: BuildConfig) {

    companion object {
        const val OLD_ROOM_COLLECTION = "Rooms"

        // amount of digits to use for anonymous users
        const val MAX_RANDOM_NUMBERS = 5

        const val ROOM_CODE_KEY = "lastRoomCode"
        const val NAME_KEY = "name"
        const val USER_ID_KEY = "userId"
    }

    fun roomCollection(): String {
        return if (buildConfig.isDebug()) {
            DEBUG_ROOM_COLLECTION
        } else {
            ROOM_COLLECTION
        }
    }
}
interface BuildConfig {
    fun isDebug(): Boolean
}

expect class BuildConfigImpl : BuildConfig
