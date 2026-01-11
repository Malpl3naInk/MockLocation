package ink.moling.mocklocation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.ui.WaypointScreen
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.loadRouteFromFile
import java.io.File

class WaypointActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册返回事件处理器（推荐方式）
        onBackPressedDispatcher.addCallback(this) {
            setResult(RESULT_OK)
            finish()  // 手动 finish
        }

        val selectedRoute = intent.getStringExtra("selectedRoute")
        Log.d("WaypointActivity", "selectedRoute=$selectedRoute")
        val route = loadRouteFromFile(File(selectedRoute!!))

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WaypointScreen(
                        selectedRoute = route!!
                    )
                }
            }
        }
    }
}