package ink.moling.mocklocation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShareLocation
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.openlocationcode.OpenLocationCode
import ink.moling.mocklocation.data.db.MockPointEntity
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.repository.MockServiceState
import ink.moling.mocklocation.data.repository.MockServiceStatusRepository
import ink.moling.mocklocation.ui.components.dialog.AddPointDialog
import ink.moling.mocklocation.ui.components.dialog.ErrorDialog
import ink.moling.mocklocation.ui.components.ExpandableCard
import ink.moling.mocklocation.ui.components.dialog.ImportExportDialog
import ink.moling.mocklocation.ui.components.PointModeView
import ink.moling.mocklocation.ui.components.RectangleFloatingActionButton
import ink.moling.mocklocation.ui.components.RouteModeView
import ink.moling.mocklocation.utils.PrefsHelper
import ink.moling.mocklocation.viewmodel.MainViewModel

/**
 * 主屏幕 Composable
 * 
 * @param viewModel 主屏幕 ViewModel
 * @param appName 应用名称
 * @param onStartMockLocation 开始模拟位置回调
 * @param onStopMockLocation 停止模拟位置回调
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    appName: String,
    onStartMockLocation: () -> Unit,
    onStopMockLocation: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val mockStatus by viewModel.mockStatus.collectAsState()
    val fusedLocation by viewModel.fusedLocation.collectAsState()
    val isImportExportDialogOpen by viewModel.isImportExportDialogOpen.collectAsState()
    val isAddPointDialogOpen by viewModel.isAddPointDialogOpen.collectAsState()
    
    // 用于触发 RouteSelector 刷新的计数器
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    // 监听 Dialog 关闭事件，触发刷新
    LaunchedEffect(isImportExportDialogOpen) {
        if (!isImportExportDialogOpen) {
            // Dialog 关闭时触发刷新
            refreshTrigger++
        }
    }
    
    // 显示权限未授予对话框
    if (mockStatus is MockServiceState.Error) {
        ErrorDialog(
            title = (mockStatus as MockServiceState.Error).type,
            text = (mockStatus as MockServiceState.Error).msg,
            stackTrace = (mockStatus as MockServiceState.Error).stackTrace,
            onDismiss = {
                onStopMockLocation()
                MockServiceStatusRepository.state.value = MockServiceState.Disabled
            }
        )
    }
    
    // 显示导入/导出对话框
    if (isImportExportDialogOpen) {
        ImportExportDialog(
            onDismiss = { viewModel.setImportExportDialogOpen(false) }
        )
    }

    // 显示添加点对话框
    if (isAddPointDialogOpen) {
        AddPointDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setAddPointDialogOpen(false) }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 64.dp, start = 8.dp, end = 8.dp)
        ) {
            // 应用标题
            Text(
                modifier = Modifier.padding(vertical = 32.dp),
                text = appName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            
            // 位置服务信息显示
            LocationServiceInfo(
                mockStatus = mockStatus,
                fusedLocation = fusedLocation
            )
            
            // 模拟设置卡片（包含点位和路径模式）
            MockSettingsCard(
                viewModel = viewModel,
                refreshTrigger = refreshTrigger,
                onImportExportClick = { viewModel.setImportExportDialogOpen(true) },
                onAddPointClick = { viewModel.setAddPointDialogOpen(true) }
            )
            
            // TODO: 高度噪声卡片
            // ExpandableCard(title = "Altitude noise [ Disabled ]") {
            //     Text(text = "ExpandableCard")
            // }
        }
        
        // 底部操作按钮
        BottomActionButtons(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            mockStatus = mockStatus,
            onStartStop = {
                val mode = PrefsHelper.getMockMode(context)

                val selected = when (mode) {
                    "Point" -> viewModel.selectedMockPoint
                    "Route" -> viewModel.selectedMockRoute
                    else    -> null // ?
                }

                selected ?: run {
                    Toast.makeText(
                        context,
                        "Please select ${mode.lowercase()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@BottomActionButtons
                }

                if (mockStatus == MockServiceState.Enabled || mockStatus is MockServiceState.Error) {
                    onStopMockLocation()
                    MockServiceStatusRepository.state.value = MockServiceState.Disabled
                } else if (mockStatus == MockServiceState.Disabled) {
                    focusManager.clearFocus()
                    onStartMockLocation()
                    if (mode == "Point") {
                        val point = selected as MockPointEntity
                        viewModel.setMockPosition(point.latitude, point.longitude, 48.0)
                    } else {
                        /* TODO */
                    }
                    MockServiceStatusRepository.state.value = MockServiceState.Initializing
                }
            },
            onSettings = {
                // 测试设置模拟位置
                viewModel.setMockPosition(53.4519076, -3.0029668, 48.0)
            }
        )
    }
}

/**
 * 模拟设置卡片 - 支持点位模式和路径模式
 */
@Composable
fun MockSettingsCard(
    viewModel: MainViewModel,
    refreshTrigger: Int,
    onImportExportClick: () -> Unit,
    onAddPointClick: () -> Unit
) {
    val context = LocalContext.current
    var currentMode by rememberSaveable { mutableStateOf(PrefsHelper.getMockMode(context)) }
    val isRouteMode = currentMode == "Route"

    ExpandableCard(
        modifier = Modifier.height(if (isRouteMode) 300.dp else 150.dp),
        title = "Mock settings [ $currentMode ]"
    ) {
        // 模式切换开关
        ModeToggleSwitch(
            isRouteMode = isRouteMode,
            onModeChange = { isRoute ->
                currentMode = if (isRoute) "Route" else "Point"
                PrefsHelper.setMockMode(context, currentMode)
            }
        )

        // 根据模式显示不同的内容
        if (isRouteMode) {
            RouteModeView(
                viewModel = viewModel,
                refreshTrigger = refreshTrigger,
                onImportExportClick = onImportExportClick
            )
        } else {
            PointModeView(
                viewModel = viewModel,
                onAddPointClick = onAddPointClick
            )
        }
    }
}

/**
 * 底部操作按钮（开始/停止 + 设置）
 */
@Composable
fun BottomActionButtons(
    modifier: Modifier = Modifier,
    mockStatus: MockServiceState,
    onStartStop: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // 开始/停止按钮
        RectangleFloatingActionButton(
            modifier = Modifier.weight(1f),
            onClick = onStartStop
        ) {
            val buttonLabel = when (mockStatus) {
                MockServiceState.Enabled -> "Stop"
                MockServiceState.Disabled -> "Start"
                MockServiceState.Initializing -> "Initializing"
                else -> "Error"
            }
            val buttonIcon = when (mockStatus) {
                MockServiceState.Enabled -> Icons.Filled.LocationOn
                MockServiceState.Disabled -> Icons.Outlined.LocationOn
                MockServiceState.Initializing -> Icons.Outlined.Build
                else -> Icons.Filled.Warning
            }
            Icon(buttonIcon, contentDescription = buttonLabel)
            Text(text = buttonLabel, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 设置按钮
        FloatingActionButton(
            onClick = onSettings,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
        }
    }
}

/**
 * 模式切换开关组件（点位模式/路径模式）
 */
@Composable
fun ModeToggleSwitch(
    isRouteMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 10.dp, bottom = 8.dp)
    ) {
        Text("Point")
        Switch(
            modifier = Modifier.padding(horizontal = 10.dp),
            checked = isRouteMode,
            onCheckedChange = onModeChange
        )
        Text("Route")
    }
}

/**
 * 位置服务信息显示组件
 */
@Composable
fun LocationServiceInfo(
    mockStatus: MockServiceState,
    fusedLocation: CandidateLocation?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (mockStatus) {
                MockServiceState.Enabled -> {
                    Icon(
                        Icons.Outlined.ShareLocation,
                        contentDescription = "Mock Location",
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
                else -> {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = "My location",
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(all = 12.dp)) {
                Text(
                    text = fusedLocation?.let {
                        "@%.5f,%.5f#%.2f".format(
                            it.lat,
                            it.lng,
                            it.alt ?: 0.0f
                        )
                    } ?: "@0.00000,0.00000#0.00"
                )
                Text(
                    text = fusedLocation?.let {
                        OpenLocationCode.encode(it.lat, it.lng)
                    } ?: ""
                )
            }
        }
    }
}
