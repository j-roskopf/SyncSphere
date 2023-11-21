package com.joetr.sync.sphere.crash

actual class CrashReportingImpl : CrashReporting {
    override fun recordException(throwable: Throwable) {
        // no-op on desktop
    }
}
