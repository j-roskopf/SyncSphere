package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.ui.time.DayTime
import com.sun.jna.Platform
import kotlinx.datetime.LocalDate
import java.awt.Desktop
import java.io.File

actual class Calendar {
    // todo joer - look into using JNA to better interface with native APIs
    actual fun addToCalendar(localDate: LocalDate, dayTime: DayTime): Boolean {
        return if (Platform.isMac()) {
            // todo joer figure out how to add parameters
            val calendarApp = File("/System/Applications/Calendar.app")
            if (calendarApp.exists()) {
                Desktop.getDesktop().open(calendarApp)
                true
            } else {
                false
            }
        } else if (Platform.isWindows()) {
            // todo joer figure out how to open calendar on windows
            false
        } else if (Platform.isLinux()) {
            // todo joer figure out how to open calendar on linux
            false
        } else {
            throw IllegalArgumentException("Unsupported OS ${Platform.getOSType()}")
        }
    }
}
