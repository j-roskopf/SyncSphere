package com.joetr.sync.sphere.data

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin

actual class CrashReportingImpl : CrashReporting {
    // todo joer release ci / cd pipeline
    // todo joer voyager 1.0
    // todo compose multiplatform 1.6 alpha
    // android mapping file
    // publish desktop binary

    override fun recordException(throwable: Throwable) {
        CrashlyticsKotlin.sendHandledException(
            throwable,
        )
    }
}