package com.joetr.sync.sphere.data

interface CrashReporting {
    fun recordException(
        throwable: Throwable,
    )
}
