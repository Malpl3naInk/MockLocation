package ink.moling.mocklocation.ui.components

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.activity.WaypointActivity
import ink.moling.mocklocation.data.local.FileHelper
import ink.moling.mocklocation.data.local.repository.MockServiceState
import ink.moling.mocklocation.data.models.RouteItem
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.utils.loadRouteFromFile
import ink.moling.mocklocation.utils.logger.Logger
import ink.moling.mocklocation.viewmodel.MainViewModel
import org.json.JSONObject

/**
 * 路径模式视图组件
 */
@Composable
fun RouteModeView(
    viewModel: MainViewModel,
    refreshTrigger: Int,
    onImportExportClick: () -> Unit
) {
    val context = LocalContext.current
    var currentRouteObject by remember { mutableStateOf<RouteObject?>(null) }
    val waypointActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentRouteObject = viewModel.selectedMockRoute?.file?.let { loadRouteFromFile(it) }
        }
    }
    
    // 初始加载选中的路径
    LaunchedEffect(viewModel.selectedMockRoute) {
        currentRouteObject = viewModel.selectedMockRoute?.file?.let { loadRouteFromFile(it) }
    }
    
    RouteSelector(
        viewModel = viewModel,
        refreshTrigger = refreshTrigger,
        onFileSelected = { item ->
            currentRouteObject = loadRouteFromFile(item.file)
        },
        onFileDeleted = {
            currentRouteObject = null
        }
    )

    Column {
        Row(modifier = Modifier.weight(0.8f)) {
            // 路径散点图
            LatLngScatter(
                modifier = Modifier.weight(0.8f),
                paddingDp = 0.dp,
                cardPadding = 8.dp,
                routeObject = currentRouteObject,
                pointRadius = 1.dp,
                currentLocation = Pair(
                    viewModel.location.value?.lat ?: 51.476853,
                    viewModel.location.value?.lng ?: 0.0
                )
            )

            // 操作按钮列
            RouteActionButtons(
                onRouteClick = onImportExportClick
            )
        }
        Button(
            onClick = {
                if (currentRouteObject == null) {
                    Toast.makeText(context, "Select a route", Toast.LENGTH_SHORT).show()
                } else {
                    waypointActivityLauncher.launch(
                        Intent(context, WaypointActivity::class.java).apply {
                            putExtra("selectedRoute",
                                viewModel.selectedMockRoute?.file?.absolutePath
                            )
                        }
                    )
                }
            }
        ) {
            Text("Waypoints")
        }
    }
}

/**
 * 路径选择下拉框组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelector(
    viewModel: MainViewModel,
    refreshTrigger: Int = 0,
    onFileSelected: (RouteItem) -> Unit,
    onFileDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val routeItems = remember { mutableStateListOf<RouteItem>() }
    val selectedName = viewModel.selectedMockRoute?.displayName ?: ""

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
                Logger.d("RouteSelector", "Selected file deleted: $selectedName")
                onFileDeleted()
            }
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    val mockStatus by viewModel.mockStatus.collectAsState()
    val isMockDisabled = mockStatus != MockServiceState.Enabled

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            if (isMockDisabled) isExpanded = it
        },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        @Suppress("DEPRECATION")
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            enabled = (viewModel.mockStatus.value != MockServiceState.Enabled),
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
                                    Logger.e("RouteSelector", "File not found: ${item.file.absolutePath}")
                                    onFileDeleted()
                                    return@LongPressDeleteMenuItem
                                }

                                viewModel.selectedMockRoute = item
                                onFileSelected(item.copy())
                            } catch (e: Exception) {
                                Logger.e("RouteSelector", "Error reading file: ${e.message}")
                                onFileDeleted()
                            }
                        },
                        onDelete = {
                            FileHelper.deleteFile(item.file)
                            routeItems.remove(item)
                            if (selectedName == item.displayName) {
                                viewModel.selectedMockRoute = null
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
fun RouteActionButtons(
    onRouteClick: () -> Unit
) {
    Column {
        IconButton(
            modifier = Modifier
                .size(50.dp)
                .padding(vertical = 5.dp),
            onClick = onRouteClick
        ) {
            Icon(
                Icons.Outlined.Route,
                contentDescription = "Route",
                // tint = Color.White
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
                // tint = Color.White
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
                // tint = Color.White
            )
        }
    }
}
