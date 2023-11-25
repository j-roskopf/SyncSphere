package com.joetr.sync.sphere.ui.results

import com.joetr.sync.sphere.data.model.Availability
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.ui.time.DayTime
import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityCalculatorTest {

    private val availabilityCalculator = AvailabilityCalculator()

    @Test
    fun `mix of all day partial and no availability`() {
        val allDayDate = "2023-11-23"
        val partialAvailabilityDate = "2023-11-24"
        val noAvailabilityDate = "2023-11-25"

        val people = getMixedAvailabilityForThreeDays(
            allDayDate = allDayDate,
            partialAvailabilityDate = partialAvailabilityDate,
            noAvailabilityDate = noAvailabilityDate,
        )

        val availability = availabilityCalculator.findOverlappingTime(people)
        assertEquals(3, availability.size)
        assertEquals(
            DayTime.AllDay,
            availability[allDayDate]!!.first().second,
        )
        assertEquals(
            DayTime.Range(5, 0, 5, 30),
            availability[partialAvailabilityDate]!!.first().second,
        )
        assertEquals(
            DayTime.NotSelected,
            availability[noAvailabilityDate]!!.first().second,
        )
    }

    @Test
    fun `all day between multiple pvveople is marked as available all day`() {
        val date = "2023-11-23"
        val people = listOf(
            personWithAllDayAvailabilityForDay(date),
            personWithAllDayAvailabilityForDay(date),
            personWithAllDayAvailabilityForDay(date),
            personWithAllDayAvailabilityForDay(date),
            personWithAllDayAvailabilityForDay(date),
        )

        val availability = availabilityCalculator.findOverlappingTime(
            people,
        )

        assertEquals(1, availability.size)
        assertEquals(
            DayTime.AllDay,
            availability[date]!!.first().second,
        )
    }

    @Test
    fun `all day between multiple days is marked as no availability`() {
        val dateOne = "2023-11-23"
        val dateTwo = "2023-11-24"
        val people = listOf(
            personWithAllDayAvailabilityForDay(dateOne),
            personWithAllDayAvailabilityForDay(dateTwo),
        )

        val availability = availabilityCalculator.findOverlappingTime(
            people,
        )

        assertEquals(2, availability.size)
        assertEquals(
            DayTime.NotSelected,
            availability[dateOne]!!.first().second,
        )
        assertEquals(
            DayTime.NotSelected,
            availability[dateTwo]!!.first().second,
        )
    }

    @Test
    fun `no overlapping time between multiple people is marked as no availability`() {
        val date = "2023-11-23"
        val people = listOf(
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 1,
                    startTimeMinute = 0,
                    endTimeHour = 3,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 3,
                    startTimeMinute = 1,
                    endTimeHour = 4,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 4,
                    startTimeMinute = 1,
                    endTimeHour = 6,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 6,
                    startTimeMinute = 1,
                    endTimeHour = 10,
                    endTimeMinute = 0,
                ),
            ),
        )

        val availability = availabilityCalculator.findOverlappingTime(
            people,
        )

        assertEquals(1, availability.size)
        assertEquals(
            DayTime.NotSelected,
            availability[date]!!.first().second,
        )
    }

    @Test
    fun `overlapping time between multiple people is marked as availability with range`() {
        val date = "2023-11-23"
        val people = listOf(
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 1,
                    startTimeMinute = 0,
                    endTimeHour = 8,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 3,
                    startTimeMinute = 0,
                    endTimeHour = 7,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 4,
                    startTimeMinute = 0,
                    endTimeHour = 7,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 5,
                    startTimeMinute = 0,
                    endTimeHour = 6,
                    endTimeMinute = 0,
                ),
            ),
        )

        val availability = availabilityCalculator.findOverlappingTime(
            people,
        )

        assertEquals(1, availability.size)
        assertEquals(
            DayTime.Range(5, 0, 6, 0),
            availability[date]!!.first().second,
        )
    }

    @Test
    fun `overlapping time of one minute between multiple people is marked as availability with range`() {
        val date = "2023-11-23"
        val people = listOf(
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 1,
                    startTimeMinute = 0,
                    endTimeHour = 4,
                    endTimeMinute = 0,
                ),
            ),
            personWithAvailabilityForDay(
                displayDate = date,
                dayTime = DayTime.Range(
                    startTimeHour = 3,
                    startTimeMinute = 59,
                    endTimeHour = 4,
                    endTimeMinute = 1,
                ),
            ),
        )

        val availability = availabilityCalculator.findOverlappingTime(
            people,
        )

        assertEquals(1, availability.size)
        assertEquals(
            DayTime.Range(3, 59, 4, 0),
            availability[date]!!.first().second,
        )
    }

    private fun personWithAllDayAvailabilityForDay(displayDate: String): People {
        return People(
            name = "Person",
            id = "ID",
            availability = listOf(
                Availability(
                    display = displayDate,
                    time = DayTime.AllDay,
                ),
            ),
        )
    }

    private fun personWithAvailabilityForDay(displayDate: String, dayTime: DayTime): People {
        return People(
            name = "Person",
            id = "ID",
            availability = listOf(
                Availability(
                    display = displayDate,
                    time = dayTime,
                ),
            ),
        )
    }

    private fun getMixedAvailabilityForThreeDays(
        allDayDate: String,
        partialAvailabilityDate: String,
        noAvailabilityDate: String,
    ): List<People> {
        return listOf(
            People(
                name = "person",
                id = "id",
                availability = listOf(
                    Availability(
                        display = allDayDate,
                        time = DayTime.AllDay,
                    ),
                    Availability(
                        display = partialAvailabilityDate,
                        time = DayTime.Range(
                            3,
                            0,
                            7,
                            0,
                        ),
                    ),
                    Availability(
                        display = noAvailabilityDate,
                        time = DayTime.Range(
                            3,
                            0,
                            4,
                            0,
                        ),
                    ),
                ),
            ),
            People(
                name = "person",
                id = "id",
                availability = listOf(
                    Availability(
                        display = allDayDate,
                        time = DayTime.AllDay,
                    ),
                    Availability(
                        display = partialAvailabilityDate,
                        time = DayTime.Range(
                            4,
                            0,
                            6,
                            0,
                        ),
                    ),
                    Availability(
                        display = noAvailabilityDate,
                        time = DayTime.Range(
                            5,
                            0,
                            6,
                            0,
                        ),
                    ),
                ),
            ),
            People(
                name = "person",
                id = "id",
                availability = listOf(
                    Availability(
                        display = allDayDate,
                        time = DayTime.AllDay,
                    ),
                    Availability(
                        display = partialAvailabilityDate,
                        time = DayTime.Range(
                            5,
                            0,
                            5,
                            30,
                        ),
                    ),
                    Availability(
                        display = noAvailabilityDate,
                        time = DayTime.Range(
                            7,
                            0,
                            8,
                            0,
                        ),
                    ),
                ),
            ),
        )
    }
}
