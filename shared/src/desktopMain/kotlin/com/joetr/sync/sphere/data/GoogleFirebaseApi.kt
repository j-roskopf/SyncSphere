package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.data.RoomConstants.Companion.OLD_ROOM_COLLECTION
import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.Finalization
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.data.models.AvailabilityArrayValues
import com.joetr.sync.sphere.data.models.AvailabilityFieldValue
import com.joetr.sync.sphere.data.models.AvailabilityMapValue
import com.joetr.sync.sphere.data.models.AvailabilityTimeFieldsValue
import com.joetr.sync.sphere.data.models.AvailabilityTimeMapValue
import com.joetr.sync.sphere.data.models.AvailabilityTimeModel
import com.joetr.sync.sphere.data.models.AvailabilityValues
import com.joetr.sync.sphere.data.models.FinalizationFields
import com.joetr.sync.sphere.data.models.FinalizationMapValue
import com.joetr.sync.sphere.data.models.FinalizationValue
import com.joetr.sync.sphere.data.models.FinalizationsArrayModel
import com.joetr.sync.sphere.data.models.FinalizationsArrayValue
import com.joetr.sync.sphere.data.models.FirebaseSignIn
import com.joetr.sync.sphere.data.models.IntegerModel
import com.joetr.sync.sphere.data.models.PeopleArrayModel
import com.joetr.sync.sphere.data.models.PeopleArrayValues
import com.joetr.sync.sphere.data.models.PeopleMapValueFields
import com.joetr.sync.sphere.data.models.PeopleMapValues
import com.joetr.sync.sphere.data.models.PeopleValues
import com.joetr.sync.sphere.data.models.RemoteAvailability
import com.joetr.sync.sphere.data.models.RoomDocument
import com.joetr.sync.sphere.data.models.RoomDocumentField
import com.joetr.sync.sphere.data.models.RoomField
import com.joetr.sync.sphere.data.models.StringModel
import com.joetr.sync.sphere.ui.time.DayTime
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

private const val BASE_URL =
    "https://firestore.googleapis.com/v1/projects/syncsphere-37dca/databases/(default)/documents/"

private const val IOS_FIREBASE_API_KEY = "AIzaSyCPlNmTVYxBOG7kTriSr74pKqK8cyvVJLo"

@OptIn(ExperimentalSerializationApi::class)
private val client = HttpClient {
    defaultRequest {
        url(BASE_URL)
        contentType(ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = false
            },
        )
    }
}

@Suppress("RethrowCaughtException", "TooManyFunctions")
class GoogleFirebaseApi {

    suspend fun updateRoom(localRoom: Room, idToken: String, roomCollection: String) {
        val room = localRoom.toRoomField()
        val roomDocumentField = RoomDocumentField(
            fields = room,
        )

        try {
            client.patch("$roomCollection/${room.roomCode.stringValue}") {
                header("Authorization", "Bearer $idToken")
                contentType(ContentType.Application.Json)
                setBody(
                    roomDocumentField,
                )
            }
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    suspend fun createRoom(
        name: String,
        roomCode: String,
        idToken: String,
        roomCollection: String,
        localUserId: String,
    ): Room {
        val room = RoomField(
            numberOfPeople = IntegerModel("1"),
            lastUpdatedTimestamp = IntegerModel(
                System.currentTimeMillis().toString(),
            ),
            roomCode = StringModel(
                roomCode,
            ),
            people = PeopleArrayModel(
                arrayValue = PeopleArrayValues(
                    values = listOf(
                        PeopleValues(
                            mapValue = PeopleMapValues(
                                fields = PeopleMapValueFields(
                                    name = StringModel(
                                        name,
                                    ),
                                    id = StringModel(
                                        localUserId,
                                    ),
                                    availability = RemoteAvailability(
                                        values = AvailabilityArrayValues(
                                            values = emptyList(),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            finalizations = FinalizationsArrayModel(
                finalizationsArrayValue = FinalizationsArrayValue(
                    values = emptyList(),
                ),
            ),
        )

        val roomDocumentField = RoomDocumentField(
            fields = room,
        )

        try {
            client.post("$roomCollection?documentId=$roomCode") {
                header("Authorization", "Bearer $idToken")
                contentType(ContentType.Application.Json)
                setBody(
                    roomDocumentField,
                )
            }

            return room.toRoom()
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    suspend fun getRoom(roomCode: String, idToken: String, roomCollection: String): Room {
        return try {
            val response: RoomDocument = client.get("$roomCollection/$roomCode") {
                header("Authorization", "Bearer $idToken")
            }.body()
            response.fields.toRoom()
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    @Suppress("SwallowedException")
    suspend fun roomExists(roomCode: String, idToken: String, roomCollection: String): Boolean {
        return try {
            val response: RoomDocument = client.get("$roomCollection/$roomCode") {
                header("Authorization", "Bearer $idToken")
            }.body()
            response.error == null
        } catch (e: Throwable) {
            return false
        }
    }

    @Suppress("SwallowedException")
    suspend fun oldRoomExists(roomCode: String, idToken: String): Boolean {
        return try {
            val response: RoomDocument = client.get("$OLD_ROOM_COLLECTION/$roomCode") {
                header("Authorization", "Bearer $idToken")
            }.body()
            response.error == null
        } catch (e: Throwable) {
            return false
        }
    }

    @Suppress("MaxLineLength")
    private fun Room.toRoomField(): RoomField {
        return RoomField(
            numberOfPeople = IntegerModel(
                this.numberOfPeople.toString(),
            ),
            lastUpdatedTimestamp = IntegerModel(
                System.currentTimeMillis().toString(),
            ),
            roomCode = StringModel(
                this.roomCode,
            ),
            people = PeopleArrayModel(
                arrayValue = PeopleArrayValues(
                    values = this.people.map { localPerson ->
                        localPerson.toPeopleValues()
                    },
                ),
            ),
            finalizations = FinalizationsArrayModel(
                finalizationsArrayValue = FinalizationsArrayValue(
                    values = this.finalizations.map {
                        FinalizationValue(
                            mapValue = FinalizationMapValue(
                                fields = FinalizationFields(
                                    person = it.person.toPeopleValues(),
                                    availabilityValues = it.availability.toAvailabilityValues(),
                                ),
                            ),
                        )
                    },
                ),
            ),
        )
    }

    @Suppress("MaxLineLength")
    private fun RoomField.toRoom(): Room {
        return Room(
            roomCode = this.roomCode.stringValue,
            numberOfPeople = this.numberOfPeople.integerValue.toInt(),
            people = this.people.arrayValue.values.map { people ->
                people.toPeople()
            },
            lastUpdatedTimestamp = this.lastUpdatedTimestamp.integerValue.toLong(),
            finalizations = this.finalizations.finalizationsArrayValue.values.map {
                Finalization(
                    person = it.mapValue.fields.person.toPeople(),
                    availability = it.mapValue.fields.availabilityValues.toAvailability(),
                )
            },
        )
    }

    @Suppress("MagicNumber")
    suspend fun signInAnonymously(): String {
        return withTimeout(10000L) {
            val url =
                "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$IOS_FIREBASE_API_KEY"
            try {
                val response: FirebaseSignIn = client.post(url).body()
                response.idToken
            } catch (throwable: Throwable) {
                throw throwable
            }
        }
    }

    private fun Availability.toAvailabilityValues(): AvailabilityValues {
        return AvailabilityValues(
            mapValue = AvailabilityMapValue(
                fields = AvailabilityFieldValue(
                    display = StringModel(
                        this.display,
                    ),
                    time = AvailabilityTimeModel(
                        AvailabilityTimeMapValue(
                            fields = AvailabilityTimeFieldsValue(
                                type = StringModel(
                                    this.time::class.qualifiedName!!,
                                ),
                                endTimeHour = if (this.time is DayTime.Range) {
                                    IntegerModel(
                                        this.time.endTimeHour.toString(),
                                    )
                                } else {
                                    null
                                },
                                endTimeMinute = if (this.time is DayTime.Range) {
                                    IntegerModel(
                                        this.time.endTimeMinute.toString(),
                                    )
                                } else {
                                    null
                                },
                                startTimeMinute = if (this.time is DayTime.Range) {
                                    IntegerModel(
                                        this.time.startTimeMinute.toString(),
                                    )
                                } else {
                                    null
                                },
                                startTimeHour = if (this.time is DayTime.Range) {
                                    IntegerModel(
                                        this.time.startTimeHour.toString(),
                                    )
                                } else {
                                    null
                                },
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun People.toPeopleValues(): PeopleValues {
        return PeopleValues(
            mapValue = PeopleMapValues(
                fields = PeopleMapValueFields(
                    name = StringModel(
                        this.name,
                    ),
                    id = StringModel(
                        this.id,
                    ),
                    availability = RemoteAvailability(
                        values = AvailabilityArrayValues(
                            values = this.availability.map { localAvailability ->
                                localAvailability.toAvailabilityValues()
                            },
                        ),
                    ),
                ),
            ),
        )
    }

    private fun AvailabilityValues.toAvailability(): Availability {
        return Availability(
            display = this.mapValue.fields.display.stringValue,
            time = when (
                this.mapValue.fields.time.mapValue.fields.type.stringValue.substringAfterLast(
                    ".",
                )
            ) {
                DayTime.AllDay::class.simpleName -> DayTime.AllDay
                DayTime.NotSelected::class.simpleName -> DayTime.NotSelected
                DayTime.Range::class.simpleName -> DayTime.Range(
                    startTimeHour = this.mapValue.fields.time.mapValue.fields.startTimeHour!!.integerValue.toInt(),
                    startTimeMinute = this.mapValue.fields.time.mapValue.fields.startTimeMinute!!.integerValue.toInt(),
                    endTimeHour = this.mapValue.fields.time.mapValue.fields.endTimeHour!!.integerValue.toInt(),
                    endTimeMinute = this.mapValue.fields.time.mapValue.fields.endTimeMinute!!.integerValue.toInt(),
                )

                else -> DayTime.NotSelected
            },
        )
    }

    private fun PeopleValues.toPeople(): People {
        return People(
            availability = this.mapValue.fields.availability.values.values?.map { availability ->
                availability.toAvailability()
            } ?: emptyList(),
            name = this.mapValue.fields.name.stringValue,
            id = this.mapValue.fields.id.stringValue,
        )
    }
}
