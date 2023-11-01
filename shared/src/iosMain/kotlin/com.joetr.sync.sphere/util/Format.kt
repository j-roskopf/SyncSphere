package com.joetr.sync.sphere.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter

actual fun LocalDateTime.format(format: String): String {
    val components = toNSDateComponents()
    val calendar = NSCalendar(NSCalendarIdentifierGregorian)
    val nsDate: NSDate = calendar.dateFromComponents(components)
        ?: throw IllegalStateException("Could not convert Kotlin LocalDateTime to NSDate $this")

    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format

    return dateFormatter.stringFromDate(nsDate)
}

actual fun formatTime(hour: Int, minute: Int): String {
    val calendar = NSCalendar(NSCalendarIdentifierGregorian)
    val components = NSDateComponents()
    components.hour = hour.toLong()
    components.minute = minute.toLong()
    val nsDate: NSDate = calendar.dateFromComponents(components)
        ?: throw IllegalStateException("Could not convert Kotlin LocalDateTime to NSDate $hour $minute")
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = "h:mm a"

    return dateFormatter.stringFromDate(nsDate)
}
