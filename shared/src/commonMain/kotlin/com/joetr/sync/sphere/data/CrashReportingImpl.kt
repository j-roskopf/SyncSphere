package com.joetr.sync.sphere.data

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin

class CrashReportingImpl : CrashReporting {

    // todo joer release ci / cd pipeline

    override fun recordException(throwable: Throwable) {
        CrashlyticsKotlin.sendHandledException(
            throwable,
        )
    }
}
