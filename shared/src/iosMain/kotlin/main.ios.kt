// ktlint-disable filename

import androidx.compose.material3.Surface
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import com.joetr.sync.sphere.Main
import com.joetr.sync.sphere.data.BuildConfig
import com.joetr.sync.sphere.data.BuildConfigImpl
import com.joetr.sync.sphere.design.theme.AppTheme
import com.joetr.sync.sphere.initKoin
import org.koin.dsl.module

@Suppress("Unused", "FunctionName")
fun MainViewController() = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
) {
    initCrashlyticsApple()
    initKoin(modules = listOf(buildConfigModule))
    AppTheme {
        Surface {
            Main()
        }
    }
}

private fun initCrashlyticsApple() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}

private val buildConfigModule = module {
    single<BuildConfig> { BuildConfigImpl() }
}
