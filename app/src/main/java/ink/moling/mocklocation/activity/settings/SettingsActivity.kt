package ink.moling.mocklocation.activity.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.ui.theme.MockLocationTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize()
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}