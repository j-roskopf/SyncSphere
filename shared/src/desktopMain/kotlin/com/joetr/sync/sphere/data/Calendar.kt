package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.ui.time.DayTime
import com.sun.jna.Platform
import kotlinx.datetime.LocalDate

actual class Calendar {
    // todo joer - look into using JNA to better interface with native APIs
    actual fun addToCalendar(localDate: LocalDate, dayTime: DayTime) {
        if (Platform.isMac()) {
            // todo joer figure out how to add parameters
            Runtime.getRuntime().exec("open -a Calendar.app")
        } else if (Platform.isWindows()) {
            // todo joer figure out how to open calendar on windows
        } else if (Platform.isLinux()) {
            // todo joer figure out how to open calendar on linux
        } else {
            throw IllegalArgumentException("Unsupported OS ${Platform.getOSType()}")
        }
    }
}
