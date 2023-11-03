import androidx.compose.material3.Surface
import androidx.compose.ui.window.ComposeUIViewController
import com.joetr.sync.sphere.Main
import com.joetr.sync.sphere.design.theme.AppTheme
import com.joetr.sync.sphere.initKoin

@Suppress("Unused", "FunctionNaming")
fun MainViewController(): platform.UIKit.UIViewController {
    initKoin()
    return ComposeUIViewController {
        AppTheme {
            Surface {
                Main()
            }
        }
    }
}
