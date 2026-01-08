package ink.moling.mocklocation.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.MOCK_STATUS_DISABLED
import ink.moling.mocklocation.MOCK_STATUS_ENABLED
import ink.moling.mocklocation.MOCK_STATUS_ERR_NO_PERM
import ink.moling.mocklocation.MOCK_STATUS_INITIALIZING
import ink.moling.mocklocation.ui.components.AddPointDialog
import ink.moling.mocklocation.ui.components.ExpandableCard
import ink.moling.mocklocation.ui.components.ImportExportDialog
import ink.moling.mocklocation.ui.components.LatLng
import ink.moling.mocklocation.ui.components.LatLngScatter
import ink.moling.mocklocation.ui.components.PermissionDeniedDialog
import ink.moling.mocklocation.ui.components.RectangleFloatingActionButton
import ink.moling.mocklocation.utils.FileHelper
import ink.moling.mocklocation.utils.PrefsHelper
import ink.moling.mocklocation.viewmodel.MainViewModel
import org.json.JSONObject
import java.io.File

/**
 * 主屏幕 Composable
 * 
 * @param viewModel 主屏幕 ViewModel
 * @param appName 应用名称
 * @param onStartMockLocation 开始模拟位置回调
 * @param onStopMockLocation 停止模拟位置回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    appName: String,
    onStartMockLocation: () -> Unit,
    onStopMockLocation: () -> Unit
) {
    val mockStatus by viewModel.mockStatus.collectAsState()
    val gpsLatitude by viewModel.gpsLatitude.collectAsState()
    val gpsLongitude by viewModel.gpsLongitude.collectAsState()
    val gpsAltitude by viewModel.gpsAltitude.collectAsState()
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
    if (mockStatus == MOCK_STATUS_ERR_NO_PERM) {
        PermissionDeniedDialog(
            onDismiss = {
                onStopMockLocation()
                viewModel.updateMockStatus(MOCK_STATUS_DISABLED)
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
                gpsLatitude = gpsLatitude,
                gpsLongitude = gpsLongitude,
                gpsAltitude = gpsAltitude
            )
            
            // 测试数据点 TODO: 应该从数据源获取
            // val points = getTestPoints()
            
            // 路径模式卡片
            RouteServiceCard(
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
                if (mockStatus == MOCK_STATUS_ENABLED || mockStatus == MOCK_STATUS_ERR_NO_PERM) {
                    onStopMockLocation()
                    viewModel.updateMockStatus(MOCK_STATUS_DISABLED)
                } else if (mockStatus == MOCK_STATUS_DISABLED) {
                    onStartMockLocation()
                    viewModel.updateMockStatus(MOCK_STATUS_INITIALIZING)
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
 * 位置服务信息显示
 */
@Composable
private fun LocationServiceInfo(
    mockStatus: Int,
    gpsLatitude: Double,
    gpsLongitude: Double,
    gpsAltitude: Double
) {
    Column(modifier = Modifier.padding(all = 8.dp)) {
        Text("Location Service ${if (mockStatus == MOCK_STATUS_ENABLED) "*" else ""}")
        Text(
            "@%.8f,%.8f\n#%.2f".format(
                gpsLatitude,
                gpsLongitude,
                gpsAltitude
            ),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * 模拟设置卡片 - 支持点位模式和路径模式
 */
@Composable
private fun RouteServiceCard(
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
 * 模式切换开关
 */
@Composable
private fun ModeToggleSwitch(
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
 * 点位模式视图
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointModeView(
    viewModel: MainViewModel,
    onAddPointClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedPointName by rememberSaveable { mutableStateOf("") }
    val points by viewModel.points.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPoints()
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedPointName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select point") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(isExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                if (points.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No saved points") },
                        onClick = { isExpanded = false },
                        enabled = false
                    )
                } else {
                    points.forEach { point ->
                        LongPressDeleteMenuItem(
                            text = {
                                Column {
                                    Text(point.name)
                                    Text(
                                        text = "%.8f, %.8f".format(point.latitude, point.longitude),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                selectedPointName = point.name
                                isExpanded = false
                            },
                            onDelete = {
                                viewModel.deletePoint(point.id)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        // 添加点位按钮
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .padding(top = 4.dp),
            onClick = onAddPointClick
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Add point",
                tint = Color.White
            )
        }
    }
}

/**
 * 路径模式视图
 */
@Composable
private fun RouteModeView(
    refreshTrigger: Int,
    onImportExportClick: () -> Unit
) {
    var selectedRouteName by rememberSaveable { mutableStateOf("") }
    val routePoints = remember { mutableStateListOf<LatLng>() }
    
    RouteSelector(
        selectedName = selectedRouteName,
        refreshTrigger = refreshTrigger,
        onSelectionChange = { name ->
            selectedRouteName = name
        },
        onFileSelected = { file, json ->
            loadRouteFromJson(file, json, routePoints)
        },
        onFileDeleted = {
            selectedRouteName = ""
            routePoints.clear()
        }
    )

    Row {
        // 路径散点图
        LatLngScatter(
            modifier = Modifier.weight(0.8f),
            paddingDp = 0.dp,
            cardPadding = 8.dp,
            points = routePoints,
            pointRadius = 1.dp
        )

        // 操作按钮列
        RouteActionButtons(
            onImportExportClick = onImportExportClick
        )
    }
}

/**
 * 从 JSON 加载路径数据
 */
private fun loadRouteFromJson(
    file: File,
    json: JSONObject,
    targetList: MutableList<LatLng>
) {
    val name = json.optString("name")
    Log.d("MainScreen", "Selected file: $name, path: ${file.absolutePath}")
    
    targetList.clear()
    
    val jsonPoints = json.getJSONArray("points")
    val newPoints = (0 until jsonPoints.length()).map { i ->
        val jsonPoint = jsonPoints.getJSONObject(i)
        val connections = jsonPoint.getJSONArray("connects")
            .let { arr -> (0 until arr.length()).map { arr.getInt(it) } }
        
        LatLng(
            lat = jsonPoint.getDouble("lat"),
            lng = jsonPoint.getDouble("lng"),
            type = jsonPoint.getString("type"),
            connections = connections
        ).also { point ->
            Log.d("MainScreen", "Point loaded: lat=${point.lat}, lng=${point.lng}, type=${point.type}")
        }
    }
    
    targetList.addAll(newPoints)
}

/**
 * 路径选择下拉框
 */
data class RouteItem(val displayName: String, val file: File)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelector(
    selectedName: String,
    refreshTrigger: Int = 0,
    onSelectionChange: (String) -> Unit,
    onFileSelected: (File, JSONObject) -> Unit,
    onFileDeleted: () -> Unit = {}
) {
    val context = LocalContext.current

    val routeItems = remember { mutableStateListOf<RouteItem>() }
    
    // 监听 refreshTrigger，当它变化时重新加载文件列表
    LaunchedEffect(refreshTrigger) {
        routeItems.clear()
        val files = FileHelper.listRouteFiles(context)
        files.forEach { file ->
            val displayName = try {
                JSONObject(FileHelper.readText(file)).optString("name", file.name)
            } catch (_: Exception) {
                file.name
            }
            routeItems.add(RouteItem(displayName, file))
        }
        
        // 检查当前选中的文件是否还存在
        if (selectedName.isNotEmpty()) {
            val stillExists = routeItems.any { it.displayName == selectedName }
            if (!stillExists) {
                Log.d("RouteSelector", "Selected file deleted: $selectedName")
                onFileDeleted()
            }
        }
    }

    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select route") },
            trailingIcon = {
                // 点击图标切换下拉展开
                ExposedDropdownMenuDefaults.TrailingIcon(isExpanded)
            },
            modifier = Modifier
                .menuAnchor()  // 这是关键：让 TextField 能响应点击事件
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            if (routeItems.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No saved routes") },
                    onClick = { isExpanded = false },
                    enabled = false
                )
            } else {
                routeItems.forEach { item ->
                    LongPressDeleteMenuItem(
                        text = { Text(item.displayName) },
                        onClick = {
                            isExpanded = false
                            try {
                                // 检查文件是否存在
                                if (!item.file.exists()) {
                                    Log.e("RouteSelector", "File not found: ${item.file.absolutePath}")
                                    onFileDeleted()
                                    return@LongPressDeleteMenuItem
                                }
                                
                                val json = JSONObject(FileHelper.readText(item.file))
                                onSelectionChange(item.displayName)
                                onFileSelected(item.file, json)
                            } catch (e: Exception) {
                                Log.e("RouteSelector", "Error reading file: ${e.message}")
                                onFileDeleted()
                            }
                        },
                        onDelete = {
                            FileHelper.deleteFile(item.file)
                            routeItems.remove(item)
                            if (selectedName == item.displayName) {
                                onFileDeleted()
                            }
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}


/**
 * 路径操作按钮组
 */
@Composable
private fun RouteActionButtons(
    onImportExportClick: () -> Unit
) {
    Column {
        IconButton(
            modifier = Modifier
                .size(50.dp)
                .padding(vertical = 5.dp),
            onClick = onImportExportClick
        ) {
            Icon(
                Icons.Outlined.ImportExport,
                contentDescription = "ImportExport",
                tint = Color.White
            )
        }
        
        IconButton(
            modifier = Modifier
                .size(50.dp)
                .padding(vertical = 5.dp),
            onClick = { /* TODO: Edit */ }
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Edit",
                tint = Color.White
            )
        }
        
        IconButton(
            modifier = Modifier
                .size(50.dp)
                .padding(vertical = 5.dp),
            onClick = { /* TODO: Add new */ }
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "New",
                tint = Color.White
            )
        }
    }
}

/**
 * 底部操作按钮（开始/停止 + 设置）
 */
@Composable
private fun BottomActionButtons(
    modifier: Modifier = Modifier,
    mockStatus: Int,
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
                MOCK_STATUS_ENABLED -> "Stop"
                MOCK_STATUS_DISABLED -> "Start"
                MOCK_STATUS_INITIALIZING -> "Initializing"
                else -> "Error"
            }
            val buttonIcon = when (mockStatus) {
                MOCK_STATUS_ENABLED -> Icons.Filled.LocationOn
                MOCK_STATUS_DISABLED -> Icons.Outlined.LocationOn
                MOCK_STATUS_INITIALIZING -> Icons.Outlined.Build
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
 * 获取测试数据点
 * TODO: 应该从数据源或存储中获取
 */
private fun getTestPoints(): List<LatLng> {
    return listOf(
        LatLng(30.31278782, 120.37452974, "R", listOf(1, 36)),
        LatLng(30.31270001, 120.37486252, "R", listOf(0, 2)),
        LatLng(30.31255188, 120.37488464, "R", listOf(1, 3, 5)),
        LatLng(30.31176745, 120.37448947, "R", listOf(2, 4)),
        LatLng(30.31151951, 120.37454468, "W", listOf(3, 9, 14, 37)),
        LatLng(30.31254331, 120.37556238, "R", listOf(2, 6, 28)),
        LatLng(30.31215474, 120.37559495, "R", listOf(5, 15)),
        LatLng(30.31089503, 120.37400104, "R", listOf(7, 29)),
        LatLng(30.31099922, 120.37415835, "R", listOf(7, 9)),
        LatLng(30.31107804, 120.37453341, "W", listOf(4, 8, 10)),
        LatLng(30.31109505, 120.37481889, "R", listOf(9, 11)),
        LatLng(30.31099153, 120.37506155, "R", listOf(10, 12)),
        LatLng(30.31101573, 120.37525065, "R", listOf(11, 13)),
        LatLng(30.31107460, 120.37518060, "R", listOf(12, 14)),
        LatLng(30.31148371, 120.37516653, "R", listOf(4, 13, 15)),
        LatLng(30.31152987, 120.37561017, "R", listOf(6, 14, 16)),
        LatLng(30.31159333, 120.37585899, "L", listOf(15, 17, 28)),
        LatLng(30.31143965, 120.37597601, "L", listOf(16, 18)),
        LatLng(30.31133080, 120.37615380, "L", listOf(17, 19)),
        LatLng(30.31130983, 120.37634747, "L", listOf(18, 20)),
        LatLng(30.31135689, 120.37654047, "L", listOf(19, 21)),
        LatLng(30.31155897, 120.37677538, "L", listOf(20, 22)),
        LatLng(30.31258439, 120.37678427, "L", listOf(21, 23)),
        LatLng(30.31272915, 120.37669964, "L", listOf(22, 24)),
        LatLng(30.31282787, 120.37658566, "L", listOf(23, 25)),
        LatLng(30.31290106, 120.37630929, "L", listOf(24, 26)),
        LatLng(30.31281503, 120.37600992, "L", listOf(25, 27)),
        LatLng(30.31271835, 120.37591156, "L", listOf(26, 28)),
        LatLng(30.31262546, 120.37586177, "L", listOf(5, 16, 27)),
        LatLng(30.31101727, 120.37364696, "R", listOf(7, 30)),
        LatLng(30.31146107, 120.37361030, "R", listOf(29, 31, 37)),
        LatLng(30.31157536, 120.37357460, "R", listOf(30, 32, 38)),
        LatLng(30.31163393, 120.37349810, "R", listOf(31, 33)),
        LatLng(30.31195135, 120.37347551, "R", listOf(32, 34)),
        LatLng(30.31261212, 120.37348310, "R", listOf(33, 35)),
        LatLng(30.31272823, 120.37365519, "R", listOf(34, 36)),
        LatLng(30.31274091, 120.37431647, "W", listOf(0, 35, 44)),
        LatLng(30.31154134, 120.37368335, "R", listOf(4, 30, 38)),
        LatLng(30.31165234, 120.37367430, "W", listOf(31, 37, 39)),
        LatLng(30.31208896, 120.37388341, "W", listOf(38, 40)),
        LatLng(30.31215410, 120.37396471, "W", listOf(39, 41)),
        LatLng(30.31225064, 120.37398847, "W", listOf(40, 42)),
        LatLng(30.31235995, 120.37402515, "W", listOf(41, 43)),
        LatLng(30.31242018, 120.37407256, "W", listOf(42, 44)),
        LatLng(30.31250551, 120.37406562, "W", listOf(36, 43)),
    )
}

/**
 * 支持长按删除的 DropdownMenuItem 包装器
 */
@Composable
fun LongPressDeleteMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    deleteDuration: Int = 1200
) {
    val progress = remember { Animatable(0f) }
    var pressing by remember { mutableStateOf(false) }

    LaunchedEffect(pressing) {
        if (pressing) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(deleteDuration)
            )
            onDelete()
        } else {
            progress.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        pressing = true
                    },
                    onPress = {
                        try {
                            tryAwaitRelease()
                        } finally {
                            pressing = false
                        }
                    }
                )
            }
    ) {
        // 原始内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            text()
        }

        // 删除进度覆盖层（使用嵌套 Box 来正确处理高度和进度）
        Box(
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .fillMaxHeight()
                    .background(
                        Color(0xFFFF5252).copy(alpha = progress.value * 0.8f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White.copy(alpha = progress.value),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}
