package com.joetr.sync.sphere.crash

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin

actual class CrashReportingImpl : CrashReporting {
    // todo joer voyager 1.0
    // android mapping file
    // publish desktop binary

    override fun recordException(throwable: Throwable) {
        CrashlyticsKotlin.sendHandledException(
            throwable,
        )
    }
}
