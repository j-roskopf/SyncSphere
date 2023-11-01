package com.joetr.sync.sphere.util

import kotlinx.datetime.LocalDateTime

expect fun LocalDateTime.format(format: String): String

expect fun formatTime(hour: Int, minute: Int): String
