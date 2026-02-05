package ink.moling.mocklocation.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShareLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.activity.SettingsActivity
import ink.moling.mocklocation.activity.WaypointActivity
import ink.moling.mocklocation.data.local.repository.MockServiceState
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.ui.components.LatLngScatter
import ink.moling.mocklocation.ui.components.PillSelection
import ink.moling.mocklocation.ui.components.PillSelector
import ink.moling.mocklocation.ui.dialog.ErrorDialog
import ink.moling.mocklocation.viewmodel.MainUiEvent
import ink.moling.mocklocation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 从 ViewModel 收集状态
    val mockStatus by viewModel.mockStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val savedPoints by viewModel.savedPoints.collectAsState()
    
    // Scaffold 和 Pager 状态（纯 UI 状态，保留在 Composable 中）
    val pagerState = rememberPagerState { 2 }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )
    val sheetState = scaffoldState.bottomSheetState
    val scaffoldExpanded by remember {
        derivedStateOf {
            sheetState.targetValue == SheetValue.Expanded ||
                    sheetState.currentValue == SheetValue.Expanded
        }
    }
    
    // Activity Launcher
    val waypointActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            WaypointActivity.RESULT_EDIT_OK -> { }
            WaypointActivity.RESULT_NEW_OK -> { }
            else -> { }
        }
    }
    
    // 收集 UI 事件
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MainUiEvent.CollapseBottomSheet -> {
                    scaffoldState.bottomSheetState.expand()
                }
                is MainUiEvent.HideBottomSheet -> {
                    scaffoldState.bottomSheetState.hide()
                }
            }
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

    // 确认删除对话框
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Confirm delete",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Delete point \"${uiState.pointName}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeletePoint() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.78f)
                    .padding(vertical = 16.dp, horizontal = 32.dp)
            ) {
                when (uiState.selectedSimulation) {
                    0 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Saved points",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { viewModel.startCreatePoint() }) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = null
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            items(savedPoints) { point ->
                                Card(
                                    onClick = { viewModel.selectPoint(point.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            point.name,
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp)
                                        )
                                        Text(
                                            "@%.6f,%.6f#%.2f".format(point.lat, point.lng, point.alt),
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Saved routes",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = {
                                waypointActivityLauncher.launch(
                                    Intent(context, WaypointActivity::class.java)
                                )
                            }) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = null
                                )
                            }

                            IconButton(onClick = { }) {
                                Icon(
                                    Icons.Outlined.Download,
                                    contentDescription = null
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            repeat(50) {
                                item {
                                    Card(
                                        onClick = { },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text(
                                                "Map #$it",
                                                modifier = Modifier
                                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !scaffoldExpanded && !uiState.editingSimPoint
            ) { page ->
                when (page) {
                    0 -> {
                        // 首页
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 88.dp, start = 24.dp, end = 24.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 32.dp, horizontal = 24.dp),
                                text = appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )

                            Card(
                                modifier = Modifier
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.MyLocation,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(6.dp)
                                        )
                                        Text(
                                            text = uiState.displayedLocation
                                        )
                                    }
                                    Text(
                                        text = uiState.displayedOpenCode,
                                        modifier = Modifier
                                            .padding(start = 36.dp),
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 18.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp),
                                    onClick = {
                                        if (mockStatus == MockServiceState.Enabled || mockStatus is MockServiceState.Error) {
                                            onStopMockLocation()
                                        } else if (mockStatus == MockServiceState.Disabled) {
                                            if (!viewModel.hasSelectedPoint()) {
                                                Toast.makeText(
                                                    context,
                                                    "Please select point",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@Button
                                            }
                                            onStartMockLocation()
                                        }
                                    }
                                ) {
                                    Text(
                                        when (mockStatus) {
                                            MockServiceState.Disabled -> "Start"
                                            MockServiceState.Enabled -> "Stop"
                                            else -> "Wait..."
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(42.dp)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.secondary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    SettingsActivity::class.java
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        Icon(
                                            Icons.Outlined.Settings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Current selection",
                                            modifier = Modifier,
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                        Text(
                                            text = when(uiState.selectedSimulation) {
                                                0    -> "Point > ${uiState.pointName}"
                                                else -> "Route > ${uiState.routeName}"
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Icon(
                                        imageVector = (
                                            when(uiState.selectedSimulation) {
                                                0    -> Icons.Outlined.LocationOn
                                                else -> Icons.Outlined.Route
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(6.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Status",
                                            modifier = Modifier,
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                        Text(
                                            when (mockStatus) {
                                                MockServiceState.Enabled -> "Mocking"
                                                MockServiceState.Initializing -> "Initializing"
                                                else -> "Idle"
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Icon(
                                        when (mockStatus) {
                                            MockServiceState.Enabled -> Icons.Outlined.ShareLocation
                                            MockServiceState.Initializing -> Icons.Outlined.Build
                                            else -> Icons.Outlined.LocationSearching
                                        },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(6.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(0.4f))
                        }
                    }

                    1 -> {
                        // 模拟配置页
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 88.dp, start = 24.dp, end = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 32.dp, horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Simulation",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                PillSelector(
                                    items = listOf(
                                        PillSelection(Icons.Outlined.LocationOn),
                                        PillSelection(Icons.Outlined.Route)
                                    ),
                                    enabled = !uiState.editingSimPoint,
                                    selectedIndex = uiState.selectedSimulation,
                                    onSelectedChange = { viewModel.setSimulationMode(it) }
                                )
                            }

                            when (uiState.selectedSimulation) {
                                0 -> {
                                    // Point 模式
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Point name",
                                                    modifier = Modifier,
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                                if (uiState.editingSimPoint) {
                                                    TextField(
                                                        value = uiState.pointName,
                                                        onValueChange = { viewModel.updatePointName(it) },
                                                        singleLine = true,
                                                        modifier = Modifier
                                                            .padding(
                                                                start = 6.dp,
                                                                end = 6.dp,
                                                                bottom = 6.dp
                                                            )
                                                            .fillMaxWidth(),
                                                        colors = TextFieldDefaults.colors(
                                                            unfocusedContainerColor = Color.Transparent,
                                                            focusedContainerColor = Color.Transparent
                                                        )
                                                    )
                                                } else {
                                                    Text(uiState.pointName)
                                                }
                                            }
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Location",
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                                if (uiState.editingSimPoint) {
                                                    Column(
                                                        modifier = Modifier.padding(
                                                            start = 8.dp,
                                                            end = 2.dp,
                                                            top = 2.dp,
                                                            bottom = 2.dp
                                                        )
                                                    ) {
                                                        Text(
                                                            "Latitude",
                                                            color = MaterialTheme.colorScheme.onSecondary
                                                        )
                                                        TextField(
                                                            value = uiState.pointLat,
                                                            onValueChange = { viewModel.updatePointLat(it) },
                                                            singleLine = true,
                                                            isError = uiState.pointLatError,
                                                            modifier = Modifier
                                                                .padding(
                                                                    start = 6.dp,
                                                                    end = 6.dp,
                                                                    bottom = 6.dp
                                                                )
                                                                .fillMaxWidth(),
                                                            colors = TextFieldDefaults.colors(
                                                                unfocusedContainerColor = Color.Transparent,
                                                                focusedContainerColor = Color.Transparent
                                                            ),
                                                            keyboardOptions = KeyboardOptions(
                                                                keyboardType = KeyboardType.Decimal
                                                            )
                                                        )
                                                        Text(
                                                            "Longitude",
                                                            color = MaterialTheme.colorScheme.onSecondary
                                                        )
                                                        TextField(
                                                            value = uiState.pointLng,
                                                            onValueChange = { viewModel.updatePointLng(it) },
                                                            singleLine = true,
                                                            isError = uiState.pointLngError,
                                                            modifier = Modifier
                                                                .padding(
                                                                    start = 6.dp,
                                                                    end = 6.dp,
                                                                    bottom = 6.dp
                                                                )
                                                                .fillMaxWidth(),
                                                            colors = TextFieldDefaults.colors(
                                                                unfocusedContainerColor = Color.Transparent,
                                                                focusedContainerColor = Color.Transparent
                                                            ),
                                                            keyboardOptions = KeyboardOptions(
                                                                keyboardType = KeyboardType.Decimal
                                                            )
                                                        )
                                                        Text(
                                                            "Altitude",
                                                            color = MaterialTheme.colorScheme.onSecondary
                                                        )
                                                        TextField(
                                                            value = uiState.pointAlt,
                                                            onValueChange = { viewModel.updatePointAlt(it) },
                                                            singleLine = true,
                                                            isError = uiState.pointAltError,
                                                            modifier = Modifier
                                                                .padding(
                                                                    start = 6.dp,
                                                                    end = 6.dp,
                                                                    bottom = 6.dp
                                                                )
                                                                .fillMaxWidth(),
                                                            colors = TextFieldDefaults.colors(
                                                                unfocusedContainerColor = Color.Transparent,
                                                                focusedContainerColor = Color.Transparent
                                                            ),
                                                            keyboardOptions = KeyboardOptions(
                                                                keyboardType = KeyboardType.Decimal
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        "@%s,%s#%s".format(
                                                            uiState.pointLat,
                                                            uiState.pointLng,
                                                            uiState.pointAlt
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 只有在选中点时才显示操作按钮
                                    if (viewModel.hasSelectedPoint()) {
                                        Row(
                                            modifier = Modifier
                                                .padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (uiState.editingSimPoint) {
                                                        viewModel.savePoint()
                                                    } else {
                                                        viewModel.startEditPoint()
                                                    }
                                                },
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp),
                                            border = BorderStroke(
                                                2.dp,
                                                color = (
                                                    if (uiState.editingSimPoint && uiState.isPointModified)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.secondary
                                                )
                                            ),
                                            contentPadding = PaddingValues(
                                                start = 16.dp,
                                                end = 20.dp,
                                                top = 8.dp,
                                                bottom = 8.dp
                                            )
                                        ) {
                                            Icon(
                                                imageVector = (
                                                    if (uiState.editingSimPoint)
                                                        Icons.Outlined.Save
                                                    else
                                                        Icons.Outlined.Edit
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = (
                                                    if (uiState.editingSimPoint && uiState.isPointModified)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = (
                                                    if (uiState.editingSimPoint)
                                                        "Save"
                                                    else
                                                        "Edit"
                                                ),
                                                color = (
                                                    if (uiState.editingSimPoint && uiState.isPointModified)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        }

                                        if (uiState.editingSimPoint && !uiState.isCreatingPoint) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp)
                                                    .size(42.dp)
                                                    .border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.secondary,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.cancelEditPoint() },
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Outlined.Undo,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp)
                                                .size(42.dp)
                                                .border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.error,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.showDeleteConfirmDialog() },
                                                modifier = Modifier
                                                    .fillMaxSize()
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        if (uiState.editingSimPoint) {
                                            // Fill with current location
                                            IconButton(
                                                onClick = { viewModel.fillCurrentLocation() }
                                            ) {
                                                Icon(
                                                    Icons.Outlined.MyLocation,
                                                    contentDescription = null
                                                )
                                            }

                                            // Select from map
                                            IconButton(
                                                onClick = { }
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Map,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    if (!uiState.editingSimPoint) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            onClick = {
                                                scope.launch {
                                                    scaffoldState.bottomSheetState.expand()
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(uiState.pointName)
                                                    Text(
                                                        "Select target",
                                                        color = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                Icon(
                                                    Icons.Outlined.ArrowDropUp,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }

                                1 -> {
                                    // Route 模式
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Route name",
                                                    modifier = Modifier,
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                                Text(uiState.routeName)
                                            }
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Map",
                                                    modifier = Modifier,
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                                if (true/* TODO: If LatLngScatter displayed a route */) {
                                                    LatLngScatter(
                                                        modifier = Modifier.fillMaxHeight(0.24f)
                                                    )
                                                } else {
                                                    Text("")
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .padding(vertical = 6.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                waypointActivityLauncher.launch(
                                                    Intent(context, WaypointActivity::class.java).apply {
                                                        putExtra("selectedRoute", uiState.routeName)
                                                    }
                                                )
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp),
                                            border = BorderStroke(
                                                2.dp,
                                                MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(
                                                start = 16.dp,
                                                end = 20.dp,
                                                top = 8.dp,
                                                bottom = 8.dp
                                            )
                                        ) {
                                            Icon(
                                                Icons.Outlined.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "Edit",
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp)
                                                .size(42.dp)
                                                .border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.error,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconButton(
                                                onClick = { },
                                                modifier = Modifier
                                                    .fillMaxSize()
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        // Export route button
                                        IconButton(
                                            onClick = { }
                                        ) {
                                            Icon(
                                                Icons.Outlined.FileUpload,
                                                contentDescription = null
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        onClick = {
                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(vertical = 8.dp, horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("<Placeholder>")
                                                Text(
                                                    "Select target",
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                            }

                                            Spacer(modifier = Modifier.weight(1f))

                                            Icon(
                                                Icons.Outlined.ArrowDropUp,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column {
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            Modifier
                                .size(18.dp)
                                .padding(4.dp)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary,
                                    CircleShape
                                )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = scaffoldExpanded,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                scaffoldState.bottomSheetState.hide()
                            }
                        }
                )
            }
        }
    }
}
