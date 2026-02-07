package ink.moling.mocklocation.activity.waypoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.logger.Logger

class WaypointActivity : ComponentActivity() {
    companion object {
        const val RESULT_EDIT_OK        = 0xE0
        const val RESULT_EDIT_CANCELED  = 0xEC
        const val RESULT_NEW_OK         = 0xA0
        const val RESULT_NEW_CANCELLED  = 0xAC
        
        // Intent extras
        const val EXTRA_ROUTE_NAME = "selectedRoute"
        const val EXTRA_ROUTE_ID = "routeId"
    }
    
    private val viewModel: WaypointViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册返回事件处理器
        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.hasUnsavedChanges()) {
                // TODO: 显示未保存更改的提示对话框
                setResult(RESULT_EDIT_CANCELED)
            } else {
                setResult(RESULT_OK)
            }
            finish()
        }

        // 获取路线信息
        val selectedRoute = intent.getStringExtra(EXTRA_ROUTE_NAME) ?: "<NEW_ROUTE>"
        val routeId = intent.getLongExtra(EXTRA_ROUTE_ID, -1L)
        
        Logger.d("WaypointActivity", "selectedRoute=$selectedRoute, routeId=$routeId")

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 初始化加载路线
                    LaunchedEffect(Unit) {
                        if (routeId != -1L) {
                            viewModel.loadRouteById(routeId)
                        } else {
                            viewModel.loadRoute(selectedRoute)
                        }
                        
                        // 监听 UI 事件
                        viewModel.uiEvent.collect { event ->
                            when (event) {
                                is WaypointUiEvent.SaveSuccess -> {
                                    val state = viewModel.uiState.value
                                    setResult(
                                        if (state.isNewRoute) RESULT_NEW_OK else RESULT_EDIT_OK
                                    )
                                }
                                is WaypointUiEvent.ClosActivity -> {
                                    finish()
                                }
                                is WaypointUiEvent.ShowToast -> {
                                    // Toast 在 Screen 中处理
                                }
                            }
                        }
                    }
                    
                    WaypointScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}