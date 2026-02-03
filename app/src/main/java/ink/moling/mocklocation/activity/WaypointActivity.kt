package ink.moling.mocklocation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.ui.screen.WaypointScreen
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.logger.Logger

class WaypointActivity : ComponentActivity() {
    companion object {
        const val RESULT_EDIT_OK        = 0xE0
        const val RESULT_EDIT_CANCELED  = 0xEC
        const val RESULT_NEW_OK         = 0xA0
        const val RESULT_NEW_CANCELLED  = 0xAC
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册返回事件处理器（推荐方式）
        onBackPressedDispatcher.addCallback(this) {
            setResult(RESULT_OK)
            finish()  // 手动 finish
        }

        val selectedRoute = intent.getStringExtra("selectedRoute") ?: "<NEW_ROUTE>"
        Logger.d("WaypointActivity", "selectedRoute=$selectedRoute")

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WaypointScreen(
                        selectedRoute = selectedRoute
                    )
                }
            }
        }
    }
}