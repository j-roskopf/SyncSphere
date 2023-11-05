package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.time.DayTimeItem
import com.joetr.sync.sphere.util.randomUUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock

private const val ROOMS_COLLECTION = "Rooms"
private const val ROOM_CODE = "lastRoomCode"
private const val NUMBER_ROOM_WORDS = 3
private const val DEFAULT_TIMEOUT_MILLIS = 10_000L

class RoomRepositoryImpl(val dictionary: Dictionary) : RoomRepository {

    val settings = Settings()

    private val firestore = Firebase.firestore

    private fun getRoomCode(): String {
        return dictionary.numberOfRandomWords(NUMBER_ROOM_WORDS).joinToString(" ").trim()
    }

    override suspend fun createRoom(name: String): Room {
        return runCatching {
            withTimeout(
                DEFAULT_TIMEOUT_MILLIS,
            ) {
                var roomCode = getRoomCode()
                if (roomExists(roomCode)) {
                    while (roomExists(roomCode)) {
                        roomCode = getRoomCode()
                    }
                }

                val room = Room(
                    roomCode = roomCode,
                    numberOfPeople = 1,
                    people = listOf(
                        People(
                            name = name,
                            availability = listOf(),
                            id = randomUUID(),
                        ),
                    ),
                    lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
                )

                firestore.collection(ROOMS_COLLECTION).document(roomCode).set(room)
                room
            }
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                throw it
            },
        )
    }

    override suspend fun getRoom(roomCode: String): Room {
        return firestore.collection(ROOMS_COLLECTION).document(roomCode).get().data()
    }

    override suspend fun updateRoom(room: Room) {
        firestore.collection(ROOMS_COLLECTION).document(room.roomCode).set(
            room.copy(
                lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }

    override fun roomUpdates(roomCode: String): Flow<Room> {
        return firestore
            .collection(ROOMS_COLLECTION)
            .document(roomCode)
            .snapshots
            .map {
                it.data() as Room
            }
    }

    override fun saveRoomCodeLocally(roomCode: String) {
        settings.putString(ROOM_CODE, roomCode)
    }

    override suspend fun getLocalRoomCode(): String? {
        return settings[ROOM_CODE]
    }

    override suspend fun roomExists(roomCode: String): Boolean {
        return runCatching {
            withTimeout(
                DEFAULT_TIMEOUT_MILLIS,
            ) {
                firestore.collection(ROOMS_COLLECTION).get()
                val roomCollection = firestore.collection(ROOMS_COLLECTION).get()
                roomCollection.documents.any {
                    it.id == roomCode
                }
            }
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                throw it
            },
        )
    }

    override suspend fun submitAvailability(
        roomCode: String,
        availability: List<DayTimeItem>,
        personId: String,
    ) {
        runCatching {
            withTimeout(DEFAULT_TIMEOUT_MILLIS) {
                val room = firestore.collection(ROOMS_COLLECTION).document(roomCode).get().data<Room>()

                firestore.collection(ROOMS_COLLECTION).document(room.roomCode).set(
                    room.copy(
                        people = room.people.map {
                            if (it.id == personId) {
                                it.copy(
                                    availability = availability.map { daytimeItem ->
                                        Availability(
                                            time = daytimeItem.dayTime,
                                            display = daytimeItem.display,
                                        )
                                    },
                                )
                            } else {
                                it
                            }
                        },
                        lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }.fold(
            onSuccess = {
                // nothing to do
            },
            onFailure = {
                throw it
            },
        )
    }
}
