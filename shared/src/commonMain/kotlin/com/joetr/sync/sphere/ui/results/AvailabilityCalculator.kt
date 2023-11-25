package com.joetr.sync.sphere.ui.results

import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.ui.results.data.ALL_DAY
import com.joetr.sync.sphere.ui.results.data.NONE
import com.joetr.sync.sphere.ui.results.data.Time
import com.joetr.sync.sphere.ui.results.data.TimeRange
import com.joetr.sync.sphere.ui.time.DayTime

class AvailabilityCalculator {

    fun findOverlappingTime(people: List<People>): Map<String, List<Pair<String, DayTime>>> {
        // Flatten and map each availability to a comparable time range)
        val availabilities = people.flatMap { person ->
            person.availability.mapNotNull { availability ->
                when (availability.time) {
                    is DayTime.AllDay -> Pair(availability.display, ALL_DAY)
                    is DayTime.NotSelected -> Pair(availability.display, NONE)
                    is DayTime.Range -> Pair(
                        availability.display,
                        TimeRange(
                            startTime = Time(
                                availability.time.startTimeHour,
                                availability.time.startTimeMinute,
                            ),
                            endTime = Time(
                                availability.time.endTimeHour,
                                availability.time.endTimeMinute,
                            ),
                        ),
                    )
                }
            }
        }

        // Group by day
        val groupedByDay = availabilities.groupBy { it.first }

        // Find overlapping times for each day
        return groupedByDay.mapValues { (_, availabilities) ->
            val accumulatedAvailabilities = availabilities.fold(availabilities) { acc, range ->
                acc.map {
                    val intersection = it.intersect(range)
                    intersection ?: Pair(it.first, NONE)
                }
            }.map {
                val dayTime = when (it.second) {
                    ALL_DAY -> DayTime.AllDay
                    NONE -> DayTime.NotSelected
                    else -> {
                        DayTime.Range(
                            it.second.startTime.hour,
                            it.second.startTime.minute,
                            it.second.endTime.hour,
                            it.second.endTime.minute,
                        )
                    }
                } as DayTime
                Pair(it.first, dayTime)
            }

            if (accumulatedAvailabilities.size != people.size) {
                // no shared availability for all people
                listOf(
                    Pair(
                        accumulatedAvailabilities.first().first,
                        DayTime.NotSelected,
                    ),
                )
            } else {
                accumulatedAvailabilities
            }
        }
    }

    private fun Pair<String, TimeRange>.intersect(other: Pair<String, TimeRange>): Pair<String, TimeRange>? {
        if (this.second == ALL_DAY && other.second == ALL_DAY) return Pair(this.first, ALL_DAY)

        val maxStart = maxOf(this.second.startTime, other.second.startTime)
        val minEnd = minOf(this.second.endTime, other.second.endTime)
        return if (maxStart < minEnd) Pair(this.first, TimeRange(maxStart, minEnd)) else null
    }

    private fun minOf(a: Time, b: Time): Time {
        return if (a >= b) {
            b
        } else {
            a
        }
    }

    private fun maxOf(a: Time, b: Time): Time {
        return if (a >= b) {
            a
        } else {
            b
        }
    }
}
