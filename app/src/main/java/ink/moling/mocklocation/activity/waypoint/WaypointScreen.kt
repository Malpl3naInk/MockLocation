package ink.moling.mocklocation.activity.waypoint

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.data.models.PointType
import ink.moling.mocklocation.data.models.RouteMeta
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.RouteType
import ink.moling.mocklocation.ui.components.LatLngScatter
import ink.moling.mocklocation.ui.components.PillSelection
import ink.moling.mocklocation.ui.components.PillSelector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaypointScreen(
    viewModel: WaypointViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    
    val routeNameState = rememberTextFieldState(uiState.routeName)
    
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
                else -> { /* 其他事件在 Activity 中处理 */ }
            }
        }
    }
    
    // 创建 RouteObject 用于显示
    val routeObject = remember(uiState.waypoints, uiState.routeName, uiState.isWaypointMap) {
        if (uiState.waypoints.isNotEmpty()) {
            RouteObject(
                name = uiState.routeName,
                meta = RouteMeta(
                    type = if (uiState.isWaypointMap)
                        RouteType.WAYPOINTS
                    else
                        RouteType.ROUTE,
                    version = 1
                ),
                points = uiState.waypoints
            )
        } else {
            null
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 384.dp,
        sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
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
                            PillSelection(Icons.Outlined.LocationOn, "Waypoints"),
                            PillSelection(Icons.Outlined.Description, "Details")
                        ),
                        selectedIndex = uiState.selectedSheetDetail,
                        onSelectedChange = { viewModel.setSheetDetail(it) },
                        selectedBackground = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (uiState.selectedSheetDetail == 0) {
                        IconButton(onClick = {
                            // TODO: 显示添加路点对话框
                            // 示例：添加一个默认路点
                            viewModel.addWaypoint(0.0, 0.0)
                        }) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null
                            )
                        }
                    }
                }

                when (uiState.selectedSheetDetail) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                        ) {
                            items(uiState.waypoints.size) { index ->
                                val waypoint = uiState.waypoints[index]
                                var isWaypointCardExpanded by remember { mutableStateOf(false) }
                                val isLoopRing = waypoint.type == PointType.L
                                
                                Card(
                                    onClick = {
                                        isWaypointCardExpanded = !isWaypointCardExpanded
                                        viewModel.selectWaypoint(if (isWaypointCardExpanded) index else null)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = (
                                            if (isWaypointCardExpanded)
                                                MaterialTheme.colorScheme.surfaceVariant
                                            else
                                                Color.Transparent
                                        )
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            text = (
                                                if (isWaypointCardExpanded)
                                                    "#${waypoint.id}"
                                                else
                                                    "#${waypoint.id} - @%.6f,%.6f".format(waypoint.lat, waypoint.lng)
                                            ),
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 6.dp,
                                                    vertical = (
                                                        if (isWaypointCardExpanded)
                                                            6.dp
                                                        else
                                                            0.dp
                                                    )
                                                )
                                        )
                                        if (isWaypointCardExpanded) {
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .padding(
                                                        start = 18.dp,
                                                        end = 6.dp
                                                    )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "@%.6f,%.6f".format(waypoint.lat, waypoint.lng),
                                                        modifier = Modifier
                                                            .padding(horizontal = 6.dp)
                                                    )

                                                    Spacer(modifier = Modifier.weight(1f))

                                                    IconButton(onClick = {
                                                        // TODO: 显示编辑位置对话框
                                                    }) {
                                                        Icon(
                                                            Icons.Outlined.EditLocationAlt,
                                                            contentDescription = null
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier
                                                        .padding(bottom = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = isLoopRing,
                                                        onCheckedChange = { checked ->
                                                            viewModel.updateWaypoint(
                                                                index = index,
                                                                lat = waypoint.lat,
                                                                lng = waypoint.lng,
                                                                type = if (checked) 
                                                                    PointType.L
                                                                else 
                                                                    PointType.R
                                                            )
                                                        }
                                                    )
                                                    Text(
                                                        "Loop ring",
                                                        modifier = Modifier
                                                            .padding(horizontal = 3.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                when (waypoint.type) {
                                                    PointType.R -> "Common road"
                                                    PointType.L -> "Loop ring"
                                                    PointType.W -> "Walk way"
                                                },
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .padding(
                                    bottom = 12.dp,
                                    start = 8.dp,
                                    end = 8.dp
                                )
                        ) {
                            Text(
                                "Route name",
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                state = routeNameState,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            
                            // 监听 TextField 状态变化并更新 ViewModel
                            LaunchedEffect(routeNameState.text) {
                                val newName = routeNameState.text.toString()
                                if (newName != uiState.routeName) {
                                    viewModel.updateRouteName(newName)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.isWaypointMap,
                                    onCheckedChange = { viewModel.setMapMode(it) }
                                )
                                Text("Map mode")
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (uiState.selectedDisplayMode) {
                0 -> {
                    Column {
                        LatLngScatter(
                            routeObject = routeObject,
                            highlightPointId = uiState.selectedWaypointIndex?.let {
                                uiState.waypoints.getOrNull(it)?.id
                            }
                        )
                    }
                }
                1 -> {
                    // TODO: Map view
                }
            }


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
                        contentDescription = null
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PillSelector(
                        items = listOf(
                            PillSelection(Icons.Outlined.Route, "Route"),
                            PillSelection(Icons.Outlined.Map, "Map")
                        ),
                        selectedIndex = uiState.selectedDisplayMode,
                        onSelectedChange = { viewModel.setDisplayMode(it) }
                    )
                }
            }
            
            // FAB：当 BottomSheet 收起时显示，用于展开 BottomSheet
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd
                ) {
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
                            contentDescription = "Expand bottom sheet"
                        )
                    }
                }
            }
        }
    }
}