package ink.moling.mocklocation.activity.waypoint

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.LocaleHelper
import ink.moling.mocklocation.utils.logger.Logger

class WaypointActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        // 尽早初始化系统语言（在 setLocale 修改 Locale.getDefault() 之前）
        newBase?.let { LocaleHelper.initSystemLocale(it) }
        super.attachBaseContext(newBase?.let { LocaleHelper.setLocale(it) })
    }
    companion object {
        const val RESULT_EDIT_OK        = 0xE0
        const val RESULT_EDIT_CANCELED  = 0xEC
        const val RESULT_NEW_OK         = 0xA0
        
        // Intent extras
        const val EXTRA_ROUTE_NAME = "selectedRoute"
        const val EXTRA_ROUTE_ID = "routeId"
    }
    
    private val viewModel: WaypointViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 获取路线信息
        val selectedRoute = intent.getStringExtra(EXTRA_ROUTE_NAME) ?: "<NEW_ROUTE>"
        val routeId = intent.getLongExtra(EXTRA_ROUTE_ID, -1L)

        // 注册返回事件处理器
        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.hasUnsavedChanges()) {
                viewModel.showUnsavedChangesDialog()
            } else {
                setResult(if (selectedRoute == "<NEW_ROUTE>") RESULT_NEW_OK else RESULT_EDIT_OK)
                finish()
            }
        }
        
        Logger.d("WaypointActivity", "selectedRoute=$selectedRoute, routeId=$routeId")

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 初始化加载路线
                    LaunchedEffect(Unit) {
                        if (routeId != -1L) {
                            viewModel.loadRouteById(routeId)
                        } else {
                            viewModel.loadRoute(selectedRoute)
                        }
                    }

                    WaypointScreen(
                        viewModel = viewModel,
                        onSaveSuccess = {
                            val state = viewModel.uiState.value
                            setResult(
                                if (state.isNewRoute) RESULT_NEW_OK else RESULT_EDIT_OK
                            )
                        },
                        onCloseActivity = {
                            setResult(RESULT_EDIT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 检查语言是否变化，如果变化则重新创建 Activity
        if (LocaleHelper.hasLanguageChanged(this)) {
            recreate()
        }
    }
}