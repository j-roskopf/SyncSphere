import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.joetr.sync.sphere.initKoin
import java.awt.Dimension

private const val WINDOW_MIN_WIDTH = 700
private const val WINDOW_MIN_HEIGHT = 1000

fun main() = application {
    initKoin()

    Window(
        title = "Sync Sphere",
        icon = painterResource("desktop_icon.png"),
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
