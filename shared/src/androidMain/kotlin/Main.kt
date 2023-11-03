import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.joetr.sync.sphere.Main
import com.joetr.sync.sphere.design.theme.AppTheme

@Composable fun MainView() {
    AppTheme {
        Surface {
            Main()
        }
    }
}
