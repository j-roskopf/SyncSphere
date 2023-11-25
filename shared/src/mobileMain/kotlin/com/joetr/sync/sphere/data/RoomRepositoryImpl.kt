package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.previous.data.PreviousRoom
import com.joetr.sync.sphere.ui.time.DayTimeItem
import com.joetr.sync.sphere.util.randomUUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock

private const val DEFAULT_TIMEOUT_MILLIS = 10_000L

@Suppress("TooManyFunctions")
actual class RoomRepositoryImpl actual constructor(
    private val dictionary: Dictionary,
    private val crashReporting: CrashReporting,
    private val roomConstants: RoomConstants,
    private val syncSphereRoomDatabase: SyncSphereRoomDatabase,
    private val settings: Settings,
) : RoomRepository {

    private val firestore = Firebase.firestore

    private val auth = Firebase.auth

    private fun getRoomCode(): String {
        val numberOfWords = 1
        return dictionary.numberOfRandomWords(numberOfWords).first().plus(
            Clock.System.now().toEpochMilliseconds().toString().takeLast(
                RoomConstants.MAX_RANDOM_NUMBERS,
            ),
        )
    }

    override suspend fun signInAnonymouslyIfNeeded() {
        if (auth.currentUser == null) {
            val user = auth.signInAnonymously().user
            if (user == null) {
                crashReporting.recordException(
                    Throwable("Error - after signing in anonymously, there was no user present"),
                )
            }
        }
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

                var localUserId = getUserId()
                if (localUserId == null) {
                    localUserId = randomUUID()
                    saveUserIdLocally(localUserId)
                }

                val room = Room(
                    roomCode = roomCode,
                    numberOfPeople = 1,
                    people = listOf(
                        People(
                            name = name,
                            availability = listOf(),
                            id = localUserId,
                        ),
                    ),
                    lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
                )

                insertRoomIfNecessary(
                    roomCode = room.roomCode,
                    userId = localUserId,
                    userName = name,
                )

                firestore.collection(roomConstants.roomCollection()).document(roomCode).set(room)
                room
            }
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                crashReporting.recordException(it)
                throw it
            },
        )
    }

    override suspend fun getRoom(roomCode: String): Room {
        return if (oldRoomExists(roomCode)) {
            firestore.collection(RoomConstants.OLD_ROOM_COLLECTION).document(roomCode).get().data()
        } else {
            firestore.collection(roomConstants.roomCollection()).document(roomCode).get().data()
        }
    }

    override suspend fun updateRoom(room: Room, userName: String, userId: String) {
        insertRoomIfNecessary(roomCode = room.roomCode, userName = userName, userId = userId)

        firestore.collection(roomConstants.roomCollection()).document(room.roomCode).set(
            room.copy(
                lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }

    override suspend fun roomUpdates(roomCode: String): Flow<Room> {
        return if (roomExists(roomCode)) {
            firestore
                .collection(roomConstants.roomCollection())
                .document(roomCode)
                .snapshots
                .map {
                    it.data() as Room
                }
        } else if (oldRoomExists(roomCode)) {
            firestore
                .collection(RoomConstants.OLD_ROOM_COLLECTION)
                .document(roomCode)
                .snapshots
                .map {
                    it.data() as Room
                }
        } else {
            throw IllegalArgumentException("Room not found")
        }
    }

    override fun saveRoomCodeLocally(roomCode: String) {
        settings.putString(RoomConstants.ROOM_CODE_KEY, roomCode)
    }

    override suspend fun getLocalRoomCode(): String? {
        return settings[RoomConstants.ROOM_CODE_KEY]
    }

    override fun saveNameLocally(name: String) {
        settings[RoomConstants.NAME_KEY] = name
    }

    override fun getLocalName(): String {
        return settings[RoomConstants.NAME_KEY] ?: ""
    }

    override fun saveUserIdLocally(userId: String) {
        settings.putString(RoomConstants.USER_ID_KEY, userId)
    }

    override suspend fun getUserId(): String? {
        return settings[RoomConstants.USER_ID_KEY]
    }

    override suspend fun roomExists(roomCode: String): Boolean {
        return runCatching {
            withTimeout(
                DEFAULT_TIMEOUT_MILLIS,
            ) {
                val roomCollection = if (oldRoomExists(roomCode)) {
                    firestore.collection(RoomConstants.OLD_ROOM_COLLECTION).get()
                } else {
                    firestore.collection(roomConstants.roomCollection()).get()
                }
                roomCollection.documents.any {
                    it.id == roomCode
                }
            }
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                crashReporting.recordException(it)
                throw it
            },
        )
    }

    override suspend fun oldRoomExists(roomCode: String): Boolean {
        return runCatching {
            withTimeout(
                DEFAULT_TIMEOUT_MILLIS,
            ) {
                val roomCollection = firestore.collection(RoomConstants.OLD_ROOM_COLLECTION).get()
                roomCollection.documents.any {
                    it.id == roomCode
                }
            }
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                crashReporting.recordException(it)
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
                val room =
                    firestore.collection(roomConstants.roomCollection()).document(roomCode).get()
                        .data<Room>()

                firestore.collection(roomConstants.roomCollection()).document(room.roomCode).set(
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
                crashReporting.recordException(it)
                throw it
            },
        )
    }

    override fun saveIconLocally(image: String) {
        settings[RoomConstants.ICON_KEY] = image
    }

    override fun getLocalIcon(): String? = settings[RoomConstants.ICON_KEY]

    override suspend fun getLocalRoomCodes(): List<PreviousRoom> {
        return syncSphereRoomDatabase.roomQueries.SelectAll().executeAsList().map {
            PreviousRoom(
                userName = it.userName,
                userId = it.userId,
                roomCode = it.roomCode,
            )
        }.reversed()
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun insertRoomIfNecessary(roomCode: String, userName: String, userId: String) {
        syncSphereRoomDatabase.roomQueries.InsertRoom(
            roomCode = roomCode,
            userName = userName,
            userId = userId,
        )
    }
}
