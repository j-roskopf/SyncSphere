import androidx.compose.ui.window.ComposeUIViewController
import com.joetr.sync.sphere.Main
import com.joetr.sync.sphere.initKoin

@Suppress("Unused")
fun MainViewController(): platform.UIKit.UIViewController {
    initKoin()
    return ComposeUIViewController {
        Main()
    }
}
