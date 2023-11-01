package com.joetr.sync.sphere.ui.results.data

data class Time(val hour: Int, val minute: Int) {
    operator fun compareTo(other: Time): Int {
        if (this.hour == other.hour) {
            // If hours are the same, compare minutes
            return this.minute.compareTo(other.minute)
        }
        // Compare hours if they are not the same
        return this.hour.compareTo(other.hour)
    }
}
