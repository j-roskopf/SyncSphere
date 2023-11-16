package com.joetr.sync.sphere.data

actual class CrashReportingImpl : CrashReporting {
    override fun recordException(throwable: Throwable) {
        // no-op on desktop
    }
}
