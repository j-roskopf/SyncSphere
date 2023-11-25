package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.ui.time.DayTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

private const val JAN_1_2000_OFFSET_SECONDS = 978307200

actual class Calendar {
    /**
     * TODO joer referenve this for creating event
     *
     * https://stackoverflow.com/a/53151074
     */
    actual fun addToCalendar(localDate: LocalDate, dayTime: DayTime) {
        val localDateSeconds = localDate.atStartOfDayIn(
            TimeZone.currentSystemDefault(),
        ).epochSeconds - JAN_1_2000_OFFSET_SECONDS
        // "calshow: wants the number of seconds since jan 1st 2000
        UIApplication.sharedApplication().openURL(NSURL(string = "calshow:$localDateSeconds"))
    }

    /**
     * https://github.com/line/abc-kmm-analytics-tools/blob/7025ded85c8a8a215f8ed3ae4d08162377808b91/src/iosMain/kotlin/com/linecorp/abc/analytics/utils/UIViewControllerUtil.kt#L12
     */
    @Suppress("UnusedPrivateMember", "Unused")
    private fun topMostViewController(): UIViewController? {
        val windows = UIApplication.sharedApplication.windows.map { it as UIWindow }
        return windows
            .first { it.rootViewController != null && it.isKeyWindow() }
            .rootViewController
            ?: return null
    }
}
