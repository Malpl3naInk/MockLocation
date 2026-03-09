package ink.moling.mocklocation.activity.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.ui.theme.ThemeStateHolder

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeStateHolder.themeMode.value = PrefsHelper.getThemeMode(this)

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}