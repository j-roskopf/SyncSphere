package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.data.RoomConstants.MAX_RANDOM_NUMBERS
import com.joetr.sync.sphere.data.RoomConstants.ROOM_CODE_KEY
import com.joetr.sync.sphere.data.RoomConstants.USER_ID_KEY
import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.time.DayTimeItem
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

private const val ID_TOKEN_KEY = "idToken"

@Suppress("TooManyFunctions", "RethrowCaughtException")
actual class RoomRepositoryImpl actual constructor(
    dictionary: Dictionary,
    crashReporting: CrashReporting,
) : RoomRepository {

    private val dictionary = dictionary

    private val firebaseApi = GoogleFirebaseApi()

    private val settings = Settings()

    private val jwtParser = JwtParser()

    private var idToken: String

    init {
        idToken = settings[ID_TOKEN_KEY] ?: ""
    }

    private fun getRoomCode(): String {
        val numberOfWords = 1
        return dictionary.numberOfRandomWords(numberOfWords).first().plus(
            Clock.System.now().toEpochMilliseconds().toString().takeLast(
                MAX_RANDOM_NUMBERS,
            ),
        )
    }

    override suspend fun signInAnonymouslyIfNeeded() {
        if (jwtParser.isTokenExpired(idToken)) {
            idToken = firebaseApi.signInAnonymously()
            settings[ID_TOKEN_KEY] = idToken
        }
    }

    override suspend fun createRoom(name: String): Room {
        var roomCode = getRoomCode()
        if (roomExists(roomCode)) {
            while (roomExists(roomCode)) {
                roomCode = getRoomCode()
            }
        }

        return firebaseApi.createRoom(name, roomCode, idToken)
    }

    override suspend fun getRoom(roomCode: String): Room {
        try {
            return firebaseApi.getRoom(roomCode, idToken)
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    override suspend fun updateRoom(room: Room) {
        try {
            return firebaseApi.updateRoom(room, idToken)
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    override fun roomUpdates(roomCode: String): Flow<Room> {
        return flow {
            emit(
                firebaseApi.getRoom(roomCode, idToken),
            )
        }
    }

    override fun saveRoomCodeLocally(roomCode: String) {
        settings.putString(ROOM_CODE_KEY, roomCode)
    }

    override suspend fun getLocalRoomCode(): String? {
        return settings[ROOM_CODE_KEY]
    }

    override fun saveUserIdLocally(userId: String) {
        settings.putString(USER_ID_KEY, userId)
    }

    override suspend fun getUserId(): String? {
        return settings[USER_ID_KEY]
    }

    override suspend fun roomExists(roomCode: String): Boolean {
        return firebaseApi.roomExists(roomCode, idToken)
    }

    override suspend fun submitAvailability(
        roomCode: String,
        availability: List<DayTimeItem>,
        personId: String,
    ) {
        try {
            val room = firebaseApi.getRoom(roomCode, idToken)
            val updatedRoom = room.copy(
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
            )
            firebaseApi.updateRoom(updatedRoom, idToken)
        } catch (throwable: Throwable) {
            throw throwable
        }
    }
}
