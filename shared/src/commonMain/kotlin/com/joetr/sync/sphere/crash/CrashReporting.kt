package com.joetr.sync.sphere.crash

interface CrashReporting {
    fun recordException(
        throwable: Throwable,
    )
}
