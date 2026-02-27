package ink.moling.mocklocation.activity.waypoint

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.waypoint.views.SheetDetailView
import ink.moling.mocklocation.activity.waypoint.views.SheetPointView
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.ui.components.LatLngScatter
import ink.moling.mocklocation.ui.components.PillSelection
import ink.moling.mocklocation.ui.components.PillSelector
import ink.moling.mocklocation.ui.dialog.AddWaypointDialog
import ink.moling.mocklocation.ui.dialog.UnsavedChangesDialog
import ink.moling.mocklocation.utils.WaypointGraph
import ink.moling.mocklocation.utils.WaypointSheet
import ink.moling.mocklocation.utils.extensions.centerPoint
import ink.moling.mocklocation.utils.extensions.isConnected
import ink.moling.mocklocation.utils.extensions.isEmpty
import ink.moling.mocklocation.utils.extensions.toLineString
import ink.moling.mocklocation.utils.logger.Logger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, MapboxDelicateApi::class)
@Composable
fun WaypointScreen(
    viewModel: WaypointViewModel,
    onSaveSuccess: () -> Unit = {},
    onCloseActivity: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // LocationService 绑定
    var locationBinder by remember { mutableStateOf<LocationService.MockLocationServiceBinder?>(null) }
    var currentLocation by remember { mutableStateOf<CandidateLocation?>(null) }
    
    // 添加路点对话框状态
    var showAddWaypointDialog by remember { mutableStateOf(false) }
    
    // 编辑路点对话框状态
    var showEditWaypointDialog by remember { mutableStateOf(false) }

    // 是否正在编辑连接点
    var isEditingConnectionMode by remember { mutableStateOf(false) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(17.0)
            center(Point.fromLngLat(0.0005, 51.4769))
            pitch(0.0)
            bearing(0.0)
        }
    }
    
    DisposableEffect(context) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                locationBinder = service as? LocationService.MockLocationServiceBinder
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                locationBinder = null
            }
        }
        
        // 绑定服务
        val intent = Intent(context, LocationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        
        onDispose {
            context.unbindService(connection)
            locationBinder = null
        }
    }
    
    // 监听位置更新
    LaunchedEffect(locationBinder) {
        locationBinder?.locationFlow()?.collect { location ->
            currentLocation = location
        }
    }
    
    val routeNameState = rememberTextFieldState(uiState.routeName)
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    
    // 同步 ViewModel 状态到 TextFieldState
    LaunchedEffect(uiState.routeName) {
        if (routeNameState.text.toString() != uiState.routeName) {
            routeNameState.setTextAndPlaceCursorAtEnd(uiState.routeName)
        }
    }
    
    // 监听 UI 事件
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is WaypointUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is WaypointUiEvent.ShowUnsavedChangesDialog -> {
                    showUnsavedChangesDialog = true
                }
                is WaypointUiEvent.SaveSuccess -> onSaveSuccess()
                is WaypointUiEvent.ClosActivity -> onCloseActivity()
            }
        }
    }

    var selectedWaypoint by remember { mutableIntStateOf(-1) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 384.dp,
        containerColor = MaterialTheme.colorScheme.background,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.78f)
                    .padding(
                        bottom = 16.dp,
                        start = 32.dp,
                        end = 32.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillSelector(
                        items = listOf(
                            PillSelection(Icons.Outlined.LocationOn, stringResource(R.string.waypoint_pill_waypoints)),
                            PillSelection(Icons.Outlined.Description, stringResource(R.string.waypoint_pill_details))
                        ),
                        selectedIndex = uiState.selectedSheetDetail,
                        onSelectedChange = { viewModel.setSheetDetail(it) },
                        selectedBackground = MaterialTheme.colorScheme.secondaryContainer
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (uiState.selectedSheetDetail == WaypointSheet.WAYPOINT_SHEET_POINTS) {
                        IconButton(onClick = {
                            showAddWaypointDialog = true
                        }) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null
                            )
                        }
                    }
                }

                when (uiState.selectedSheetDetail) {
                    WaypointSheet.WAYPOINT_SHEET_POINTS -> SheetPointView(
                        viewModel,
                        selectedWaypoint
                    ) { index ->
                        selectedWaypoint = index
                        viewModel.selectWaypoint(if (index == -1) null else index)
                    }
                    WaypointSheet.WAYPOINT_SHEET_DETAIL -> SheetDetailView(
                        viewModel,
                        selectedWaypoint,
                        onWaypointEdit = { showEditWaypointDialog = true },
                        onConnectsEdit = {
                            isEditingConnectionMode = true
                            scope.launch {
                                scaffoldState.bottomSheetState.hide()
                            }
                        },
                        onConnectsDelete = {
                            viewModel.deleteWaypoint(selectedWaypoint)
                        }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (uiState.selectedDisplayMode) {
                WaypointGraph.WAYPOINT_GRAPH_CANVAS -> {
                    Column {
                        Spacer(modifier = Modifier.weight(0.2f))

                        LatLngScatter(
                            modifier = Modifier.fillMaxHeight(0.7f),
                            routeObject = uiState.routeObject,
                            highlightPointId = uiState.selectedWaypointIndex?.let {
                                uiState.routeObject.points.getOrNull(it)?.id
                            },
                            currentLocation = Pair(
                                currentLocation?.lat ?: 0.0,
                                currentLocation?.lng ?: 0.0
                            ),
                            onPointClick = { index, _ ->
                                if (isEditingConnectionMode) {
                                    Logger.d("WaypointScreen", "Clicked: $index, Selected: $selectedWaypoint")
                                    if (uiState.routeObject.isConnected(selectedWaypoint, index)) {
                                        viewModel.removeWaypointConnections(selectedWaypoint, index)
                                    } else {
                                        viewModel.addWaypointConnections(selectedWaypoint, index)
                                    }
                                } else {
                                    val isWaypointSelected = index == selectedWaypoint
                                    selectedWaypoint = if (isWaypointSelected) -1 else index
                                    viewModel.selectWaypoint(if (isWaypointSelected) null else index)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.weight(0.1f))
                    }
                }
                WaypointGraph.WAYPOINT_GRAPH_MAP -> {
                    MapboxMap(
                        Modifier.fillMaxSize(),
                        mapViewportState = mapViewportState,
                        compass = { Compass(Modifier.padding(top = 120.dp)) },
                        scaleBar = { ScaleBar(Modifier.padding(top = 120.dp)) },
                        logo = { Logo(Modifier.padding(bottom = 40.dp)) },
                        attribution = { Attribution(Modifier.padding(bottom = 40.dp)) }
                    ) {
                        // 1. Location puck + follow-puck
                        MapEffect(Unit) { mapView ->
                            mapView.location.updateSettings {
                                locationPuck = createDefault2DPuck()
                                enabled = true
                            }
                        }

                        // 2. GeoJSON source for your route
                        val routeSource = rememberGeoJsonSourceState {
                            // optional: lineMetrics if you want lineTrimOffset/lineProgress
                            // lineMetrics = BooleanValue(true)
                        }

                        // 3. Update source data whenever route changes
                        LaunchedEffect(uiState.routeObject) {
                            uiState.routeObject.let {
                                val lineString = it.toLineString()
                                routeSource.data = GeoJSONData(lineString)
                            }
                        }

                        // 4. Line layer that draws the route
                        LineLayer(
                            sourceState = routeSource
                        ) {
                            lineWidth = DoubleValue(4.0)
                            lineColor = ColorValue(Color(0xFF2F7AC6)) // example color
                            lineCap = LineCapValue.ROUND
                            lineJoin = LineJoinValue.ROUND
                        }

                        if (uiState.routeObject.isEmpty()) {
                            mapViewportState.setCameraOptions(
                                cameraOptions {
                                    center(
                                        Point.fromLngLat(
                                            currentLocation?.lng ?: 0.0005,
                                            currentLocation?.lat ?: 51.4769
                                        )
                                    )
                                }
                            )
                        } else {
                            mapViewportState.setCameraOptions(
                                cameraOptions {
                                    center(uiState.routeObject.centerPoint())
                                }
                            )
                        }
                    }
                }
            }

            Column {
                if (isEditingConnectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.6f), // 顶部 60% 黑
                                        Color.Transparent               // 底部完全透明
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.waypoint_hint_connect_mode),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        IconButton(
                            modifier = Modifier
                                .padding(start = 6.dp),
                            onClick = {
                                viewModel.saveRoute()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Save,
                                contentDescription = null,
                                tint = when (uiState.selectedDisplayMode) {
                                    WaypointGraph.WAYPOINT_GRAPH_MAP -> Color.Black
                                    else -> LocalContentColor.current
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            PillSelector(
                                items = listOf(
                                    PillSelection(Icons.Outlined.Route, stringResource(R.string.waypoint_pill_route)),
                                    PillSelection(Icons.Outlined.Map, stringResource(R.string.waypoint_pill_map))
                                ),
                                selectedIndex = uiState.selectedDisplayMode,
                                onSelectedChange = { viewModel.setDisplayMode(it) }
                            )
                        }
                    }
                }
            }
            
            // FAB：当 BottomSheet 收起时显示，用于展开 BottomSheet
            if (scaffoldState.bottomSheetState.targetValue == SheetValue.Hidden) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (isEditingConnectionMode) {
                        FloatingActionButton(
                            onClick = {
                                isEditingConnectionMode = false
                                scope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            },
                            modifier = Modifier
                                .padding(bottom = 24.dp, end = 24.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.waypoint_cd_ok)
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            },
                            modifier = Modifier
                                .padding(bottom = 24.dp, end = 24.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.waypoint_cd_expand_sheet)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 未保存更改对话框
    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedChangesDialog = false
                viewModel.saveRoute()
            },
            onDiscard = {
                showUnsavedChangesDialog = false
                viewModel.discardChangesAndClose()
            },
            onDismiss = {
                showUnsavedChangesDialog = false
            }
        )
    }
    
    // 添加路点对话框
    if (showAddWaypointDialog) {
        AddWaypointDialog(
            currentLatitude = currentLocation?.lat,
            currentLongitude = currentLocation?.lng,
            onConfirm = { lat, lng ->
                if (showAddWaypointDialog) {
                    uiState.routeObject.points.lastOrNull()?.let {
                        val newPoint = viewModel.addWaypoint(lat, lng, connects = setOf(it.id))
                        viewModel.updateWaypoint(it.id, connects = it.connects.plus(newPoint))
                    } ?: viewModel.addWaypoint(lat, lng)
                    showAddWaypointDialog = false
                }
            },
            onDismiss = {
                showAddWaypointDialog = false
            }
        )
    }
    
    // 编辑路点对话框
    if (showEditWaypointDialog && selectedWaypoint != -1) {
        val currentWaypoint = uiState.routeObject.points.getOrNull(selectedWaypoint)
        currentWaypoint?.let { waypoint ->
            AddWaypointDialog(
                currentLatitude = currentLocation?.lat,
                currentLongitude = currentLocation?.lng,
                initialLatitude = waypoint.lat,
                initialLongitude = waypoint.lng,
                isEditMode = true,
                onConfirm = { lat, lng ->
                    if (showEditWaypointDialog) {
                        viewModel.updateWaypoint(selectedWaypoint, lat, lng)
                        showEditWaypointDialog = false
                    }
                },
                onDismiss = {
                    showEditWaypointDialog = false
                }
            )
        }
    }
}