import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.joetr.sync.sphere.data.BuildConfig
import com.joetr.sync.sphere.data.BuildConfigImpl
import com.joetr.sync.sphere.data.Calendar
import com.joetr.sync.sphere.data.local.DriverFactory
import com.joetr.sync.sphere.initKoin
import org.koin.dsl.module
import java.awt.Dimension

private const val WINDOW_MIN_WIDTH = 700
private const val WINDOW_MIN_HEIGHT = 1000

fun main() = application {
    val isSandboxed = System.getenv("APP_SANDBOX_CONTAINER_ID") != null
    if (isSandboxed) {
        val resourcesPath = System.getProperty("compose.application.resources.dir")

        // jna
        System.setProperty("jna.nounpack", "true")
        System.setProperty("jna.boot.library.path", resourcesPath)

        // sqlite-jdbc
        System.setProperty("org.sqlite.lib.path", resourcesPath)
        System.setProperty("org.sqlite.lib.name", "libsqlitejdbc.dylib")
    }

    initKoin(modules = listOf(buildConfigModule, sqlDriverModule, calendarModule))

    Window(
        title = "Sync Sphere",
        icon = painterResource("icon/desktop_icon.png"),
        onCloseRequest = {
            exitApplication()
        },
    ) {
        window.minimumSize = Dimension(
            WINDOW_MIN_WIDTH,
            WINDOW_MIN_HEIGHT,
        )
        MainView()
    }
}

private val buildConfigModule = module {
    single<BuildConfig> { BuildConfigImpl() }
}

private val sqlDriverModule = module {
    single { DriverFactory().createDriver() }
}

private val calendarModule = module {
    single { Calendar() }
}
