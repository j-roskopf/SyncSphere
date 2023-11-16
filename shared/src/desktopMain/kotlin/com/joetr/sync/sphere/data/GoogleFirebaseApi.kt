package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.data.RoomConstants.ROOM_COLLECTION
import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.data.models.ArrayValue
import com.joetr.sync.sphere.data.models.AvailabilityArrayValues
import com.joetr.sync.sphere.data.models.AvailabilityFieldValue
import com.joetr.sync.sphere.data.models.AvailabilityMapValue
import com.joetr.sync.sphere.data.models.AvailabilityTimeFieldsValue
import com.joetr.sync.sphere.data.models.AvailabilityTimeMapValue
import com.joetr.sync.sphere.data.models.AvailabilityTimeModel
import com.joetr.sync.sphere.data.models.AvailabilityValues
import com.joetr.sync.sphere.data.models.FirebaseSignIn
import com.joetr.sync.sphere.data.models.IntegerModel
import com.joetr.sync.sphere.data.models.PeopleArrayModel
import com.joetr.sync.sphere.data.models.PeopleMapValueFields
import com.joetr.sync.sphere.data.models.PeopleMapValues
import com.joetr.sync.sphere.data.models.PeopleValues
import com.joetr.sync.sphere.data.models.RemoteAvailability
import com.joetr.sync.sphere.data.models.RoomDocument
import com.joetr.sync.sphere.data.models.RoomDocumentField
import com.joetr.sync.sphere.data.models.RoomField
import com.joetr.sync.sphere.data.models.StringModel
import com.joetr.sync.sphere.ui.time.DayTime
import com.joetr.sync.sphere.util.randomUUID
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

private const val BASE_URL =
    "https://firestore.googleapis.com/v1/projects/syncsphere-37dca/databases/(default)/documents/"

private const val IOS_FIREBASE_API_KEY = "AIzaSyCPlNmTVYxBOG7kTriSr74pKqK8cyvVJLo"

@Suppress("RethrowCaughtException")
class GoogleFirebaseApi {

    @OptIn(ExperimentalSerializationApi::class)
    private val client by lazy {
        HttpClient {
            install(Logging)
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
    }

    suspend fun updateRoom(localRoom: Room, idToken: String) {
        val room = localRoom.toRoomField()
        val roomDocumentField = RoomDocumentField(
            fields = room,
        )

        try {
            client.patch("$ROOM_COLLECTION/${room.roomCode.stringValue}") {
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

    suspend fun createRoom(name: String, roomCode: String, idToken: String): Room {
        val room = RoomField(
            numberOfPeople = IntegerModel("1"),
            lastUpdatedTimestamp = IntegerModel(
                System.currentTimeMillis().toString(),
            ),
            roomCode = StringModel(
                roomCode,
            ),
            people = PeopleArrayModel(
                arrayValue = ArrayValue(
                    values = listOf(
                        PeopleValues(
                            mapValue = PeopleMapValues(
                                fields = PeopleMapValueFields(
                                    name = StringModel(
                                        name,
                                    ),
                                    id = StringModel(
                                        randomUUID(),
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
        )
        val roomDocumentField = RoomDocumentField(
            fields = room,
        )

        try {
            client.post("$ROOM_COLLECTION?documentId=$roomCode") {
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

    suspend fun getRoom(roomCode: String, idToken: String): Room {
        return try {
            val response: RoomDocument = client.get("$ROOM_COLLECTION/$roomCode") {
                header("Authorization", "Bearer $idToken")
            }.body()
            response.fields.toRoom()
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    @Suppress("SwallowedException")
    suspend fun roomExists(roomCode: String, idToken: String): Boolean {
        return try {
            val response: RoomDocument = client.get("$ROOM_COLLECTION/$roomCode") {
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
                arrayValue = ArrayValue(
                    values = this.people.map { localPerson ->
                        PeopleValues(
                            mapValue = PeopleMapValues(
                                fields = PeopleMapValueFields(
                                    name = StringModel(
                                        localPerson.name,
                                    ),
                                    id = StringModel(
                                        localPerson.id,
                                    ),
                                    availability = RemoteAvailability(
                                        values = AvailabilityArrayValues(
                                            values = localPerson.availability.map { localAvailability ->
                                                AvailabilityValues(
                                                    mapValue = AvailabilityMapValue(
                                                        fields = AvailabilityFieldValue(
                                                            display = StringModel(
                                                                localAvailability.display,
                                                            ),
                                                            time = AvailabilityTimeModel(
                                                                AvailabilityTimeMapValue(
                                                                    fields = AvailabilityTimeFieldsValue(
                                                                        type = StringModel(
                                                                            localAvailability.time::class.qualifiedName!!,
                                                                        ),
                                                                        endTimeHour = if (localAvailability.time is DayTime.Range) {
                                                                            IntegerModel(
                                                                                localAvailability.time.endTimeHour.toString(),
                                                                            )
                                                                        } else {
                                                                            null
                                                                        },
                                                                        endTimeMinute = if (localAvailability.time is DayTime.Range) {
                                                                            IntegerModel(
                                                                                localAvailability.time.endTimeMinute.toString(),
                                                                            )
                                                                        } else {
                                                                            null
                                                                        },
                                                                        startTimeMinute = if (localAvailability.time is DayTime.Range) {
                                                                            IntegerModel(
                                                                                localAvailability.time.startTimeMinute.toString(),
                                                                            )
                                                                        } else {
                                                                            null
                                                                        },
                                                                        startTimeHour = if (localAvailability.time is DayTime.Range) {
                                                                            IntegerModel(
                                                                                localAvailability.time.startTimeHour.toString(),
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
                                            },
                                        ),
                                    ),
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
                People(
                    availability = people.mapValue.fields.availability.values.values?.map { availability ->
                        Availability(
                            display = availability.mapValue.fields.display.stringValue,
                            time = when (
                                availability.mapValue.fields.time.mapValue.fields.type.stringValue.substringAfterLast(
                                    ".",
                                )
                            ) {
                                DayTime.AllDay::class.simpleName -> DayTime.AllDay
                                DayTime.NotSelected::class.simpleName -> DayTime.NotSelected
                                DayTime.Range::class.simpleName -> DayTime.Range(
                                    startTimeHour = availability.mapValue.fields.time.mapValue.fields.startTimeHour!!.integerValue.toInt(),
                                    startTimeMinute = availability.mapValue.fields.time.mapValue.fields.startTimeMinute!!.integerValue.toInt(),
                                    endTimeHour = availability.mapValue.fields.time.mapValue.fields.endTimeHour!!.integerValue.toInt(),
                                    endTimeMinute = availability.mapValue.fields.time.mapValue.fields.endTimeMinute!!.integerValue.toInt(),
                                )

                                else -> DayTime.NotSelected
                            },
                        )
                    } ?: emptyList(),
                    name = people.mapValue.fields.name.stringValue,
                    id = people.mapValue.fields.id.stringValue,
                )
            },
            lastUpdatedTimestamp = this.lastUpdatedTimestamp.integerValue.toLong(),
        )
    }

    suspend fun signInAnonymously(): String {
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$IOS_FIREBASE_API_KEY"
        try {
            val response: FirebaseSignIn = client.post(url).body()
            return response.idToken
        } catch (throwable: Throwable) {
            throw throwable
        }
    }
}
