package com.joetr.sync.sphere.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinalizationsArrayModel(
    @SerialName("arrayValue") val finalizationsArrayValue: FinalizationsArrayValue,
)

@Serializable
data class FinalizationsArrayValue(
    @SerialName("values") val values: List<FinalizationValue> = emptyList(),
)

@Serializable
data class FinalizationValue(
    @SerialName("mapValue") val mapValue: FinalizationMapValue,
)

@Serializable
data class FinalizationMapValue(
    @SerialName("fields") val fields: FinalizationFields,
)

@Serializable
data class FinalizationFields(
    @SerialName("person") val person: PeopleValues,
    @SerialName("availability") val availabilityValues: AvailabilityValues,
)

@Serializable
data class PeopleArrayModel(
    @SerialName("arrayValue") val arrayValue: PeopleArrayValues,
)

@Serializable
data class PeopleArrayValues(
    @SerialName("values") val values: List<PeopleValues>,
)

@Serializable
data class PeopleValues(
    @SerialName("mapValue") val mapValue: PeopleMapValues,
)

@Serializable
data class PeopleMapValues(
    @SerialName("fields") val fields: PeopleMapValueFields,
)

@Serializable
data class PeopleMapValueFields(
    @SerialName("name") val name: StringModel,
    @SerialName("id") val id: StringModel,
    @SerialName("availability") val availability: RemoteAvailability,
)

@Serializable
data class RemoteAvailability(
    @SerialName("arrayValue") val values: AvailabilityArrayValues,
)

@Serializable
data class AvailabilityArrayValues(
    @SerialName("values") val values: List<AvailabilityValues>?,
)

@Serializable
data class AvailabilityValues(
    @SerialName("mapValue") val mapValue: AvailabilityMapValue,
)

@Serializable
data class AvailabilityMapValue(
    @SerialName("fields") val fields: AvailabilityFieldValue,
)

@Serializable
data class AvailabilityFieldValue(
    @SerialName("display") val display: StringModel,
    @SerialName("time") val time: AvailabilityTimeModel,
)

@Serializable
data class AvailabilityTimeModel(
    @SerialName("mapValue") val mapValue: AvailabilityTimeMapValue,
)

@Serializable
data class AvailabilityTimeMapValue(
    @SerialName("fields") val fields: AvailabilityTimeFieldsValue,
)

@Serializable
data class AvailabilityTimeFieldsValue(
    @SerialName("type") val type: StringModel,
    @SerialName("endTimeHour") val endTimeHour: IntegerModel?,
    @SerialName("startTimeHour") val startTimeHour: IntegerModel?,
    @SerialName("startTimeMinute") val startTimeMinute: IntegerModel?,
    @SerialName("endTimeMinute") val endTimeMinute: IntegerModel?,
)
