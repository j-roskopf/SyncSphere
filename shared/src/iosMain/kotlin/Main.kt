import androidx.compose.material3.Surface
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import com.joetr.sync.sphere.Main
import com.joetr.sync.sphere.design.theme.AppTheme
import com.joetr.sync.sphere.initKoin

@Suppress("Unused", "FunctionNaming")
fun MainViewController(): platform.UIKit.UIViewController {
    initCrashlyticsApple()
    initKoin()
    return ComposeUIViewController(
        configure = {
            onFocusBehavior = OnFocusBehavior.DoNothing
        },
    ) {
        AppTheme {
            Surface {
                Main()
            }
        }
    }
}

fun initCrashlyticsApple() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}
