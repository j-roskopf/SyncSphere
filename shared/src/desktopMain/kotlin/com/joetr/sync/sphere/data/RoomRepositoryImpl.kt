package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.data.RoomConstants.Companion.ICON_KEY
import com.joetr.sync.sphere.data.RoomConstants.Companion.MAX_RANDOM_NUMBERS
import com.joetr.sync.sphere.data.RoomConstants.Companion.NAME_KEY
import com.joetr.sync.sphere.data.RoomConstants.Companion.ROOM_CODE_KEY
import com.joetr.sync.sphere.data.RoomConstants.Companion.USER_ID_KEY
import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.Finalization
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.previous.data.PreviousRoom
import com.joetr.sync.sphere.ui.time.DayTime
import com.joetr.sync.sphere.ui.time.DayTimeItem
import com.joetr.sync.sphere.util.randomUUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

private const val ID_TOKEN_KEY = "idToken"

@Suppress("TooManyFunctions", "RethrowCaughtException")
actual class RoomRepositoryImpl actual constructor(
    dictionary: Dictionary,
    crashReporting: CrashReporting,
    private val roomConstants: RoomConstants,
    private val syncSphereRoomDatabase: SyncSphereRoomDatabase,
    private val settings: Settings,
) : RoomRepository {

    private val roomFlow = MutableSharedFlow<Room>(
        replay = 1,
    )

    private val dictionary = dictionary

    private val firebaseApi = GoogleFirebaseApi()

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

    @Suppress("MaxLineLength")
    override suspend fun signInAnonymouslyIfNeeded() {
        idToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVkNjE3N2E5Mjg2ZDI1Njg0NTI2OWEzMTM2ZDNmNjY0MjZhNGQ2NDIiLCJ0eXAiOiJKV1QifQ.eyJwcm92aWRlcl9pZCI6ImFub255bW91cyIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9zeW5jc3BoZXJlLTM3ZGNhIiwiYXVkIjoic3luY3NwaGVyZS0zN2RjYSIsImF1dGhfdGltZSI6MTcxNzA5NjU2OSwidXNlcl9pZCI6Ik05QjdFT0JWOGdSVnlpYm5HNHZPTkN6T01qRzIiLCJzdWIiOiJNOUI3RU9CVjhnUlZ5aWJuRzR2T05Dek9NakcyIiwiaWF0IjoxNzE3MDk2NTY5LCJleHAiOjE3MTcxMDAxNjksImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnt9LCJzaWduX2luX3Byb3ZpZGVyIjoiYW5vbnltb3VzIn19.m1CcK_aaiZzqg0-jzjl-1IUl-DhBAR_UhsDMIQlobl0SNzDE1Whlrrve0sTXA3GefQbxFRI4b3p3JOHpCGk60Xk7OabLrf9kL148UFfrMjLuHZ--JvDwJH9oAiWVXBfYe79-NSAnBQigS5pPcyn30bGRqdNLzTl6BifIR6hpTGK9GAXpo5mqhfNITlqgMjTTBpraJb0xh0LEAXhoPB_VMzpFXHyqOjqgxP-oALQVLB52FNv4kzvHwoI3jW7LZxqd99F1KBkfGhBaH0IlEIWlNjwLtbWmJrLDiMroj8BJCkLrw7Esi7AyUQOzAxJwmHMEvha0v3x_Bvud5jSMNG7kzA"
    }

    override suspend fun createRoom(name: String): Room {
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

        // insert room locally so it can be viewed later
        insertRoomIfNecessary(roomCode = roomCode, userId = localUserId, userName = name)

        return firebaseApi.createRoom(
            name = name,
            roomCode = roomCode,
            idToken = idToken,
            roomCollection = roomConstants.roomCollection(),
            localUserId = localUserId,
        )
    }

    override suspend fun getRoom(roomCode: String): Room {
        try {
            return firebaseApi.getRoom(
                roomCode = roomCode,
                idToken = idToken,
                roomCollection = roomConstants.roomCollection(),
            )
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    override suspend fun updateRoom(room: Room, userName: String, userId: String) {
        try {
            // insert room locally so it can be viewed later
            insertRoomIfNecessary(roomCode = room.roomCode, userName = userName, userId = userId)

            return firebaseApi.updateRoom(
                localRoom = room,
                idToken = idToken,
                roomCollection = roomConstants.roomCollection(),
            )
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    override suspend fun roomUpdates(roomCode: String): Flow<Room> {
        emitRoom(roomCode)
        return roomFlow
    }

    private suspend fun emitRoom(roomCode: String) {
        val room = getRoom(roomCode)
        roomFlow.emit(room)
    }

    override fun saveRoomCodeLocally(roomCode: String) {
        settings.putString(ROOM_CODE_KEY, roomCode)
    }

    override fun saveNameLocally(name: String) {
        settings.putString(NAME_KEY, name)
    }

    override fun getLocalName(): String {
        return settings[NAME_KEY] ?: ""
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
        return firebaseApi.roomExists(
            roomCode = roomCode,
            idToken = idToken,
            roomCollection = roomConstants.roomCollection(),
        )
    }

    override suspend fun oldRoomExists(roomCode: String): Boolean {
        return firebaseApi.oldRoomExists(roomCode, idToken)
    }

    override suspend fun submitAvailability(
        roomCode: String,
        availability: List<DayTimeItem>,
        personId: String,
    ) {
        try {
            val room = firebaseApi.getRoom(
                roomCode = roomCode,
                idToken = idToken,
                roomCollection = roomConstants.roomCollection(),
            )
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
            firebaseApi.updateRoom(
                localRoom = updatedRoom,
                idToken = idToken,
                roomCollection = roomConstants.roomCollection(),
            )
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    override suspend fun getLocalRoomCodes(): List<PreviousRoom> {
        return syncSphereRoomDatabase.roomQueries.SelectAll().executeAsList().map {
            PreviousRoom(
                userName = it.userName,
                userId = it.userId,
                roomCode = it.roomCode,
            )
        }.reversed()
    }

    override fun saveIconLocally(image: String) {
        settings[ICON_KEY] = image
    }

    override fun getLocalIcon(): String? = settings[ICON_KEY]

    override suspend fun finalize(
        person: People,
        roomCode: String,
        localDate: LocalDate,
        dayTime: DayTime,
    ) {
        // get the current room
        val room = getRoom(roomCode)

        // update room
        firebaseApi.updateRoom(
            localRoom = room.copy(
                finalizations = room.finalizations + Finalization(
                    person = person,
                    availability = Availability(
                        time = dayTime,
                        display = localDate.toString(),
                    ),
                ),
                lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
            ),
            idToken = idToken,
            roomCollection = roomConstants.roomCollection(),
        )

        emitRoom(roomCode)
    }

    override suspend fun undoFinalization(person: People, roomCode: String) {
        // get the current room
        val room = getRoom(roomCode)

        // update room
        firebaseApi.updateRoom(
            localRoom = room.copy(
                finalizations = room.finalizations.filter {
                    it.person.id != person.id
                },
                lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds(),
            ),
            idToken = idToken,
            roomCollection = roomConstants.roomCollection(),
        )

        emitRoom(roomCode)
    }

    override suspend fun deleteRoomLocally(roomCode: String) {
        syncSphereRoomDatabase.roomQueries.DeleteRoom(
            roomCode = roomCode,
        )
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
