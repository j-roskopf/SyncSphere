package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.time.DayTimeItem
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface RoomRepository {
    suspend fun signInAnonymouslyIfNeeded()
    suspend fun createRoom(name: String): Room
    suspend fun getRoom(roomCode: String): Room
    suspend fun updateRoom(room: Room)
    suspend fun roomExists(roomCode: String): Boolean
    suspend fun oldRoomExists(roomCode: String): Boolean
    suspend fun submitAvailability(
        roomCode: String,
        availability: List<DayTimeItem>,
        personId: String,
    )
    suspend fun roomUpdates(roomCode: String): Flow<Room>

    fun saveRoomCodeLocally(roomCode: String)
    suspend fun getLocalRoomCode(): String?

    fun saveNameLocally(name: String)
    fun getLocalName(): String

    fun saveUserIdLocally(userId: String)
    suspend fun getUserId(): String?
}
