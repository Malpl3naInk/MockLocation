package ink.moling.mocklocation.ui.screen

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropUp
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.activity.SettingsActivity
import ink.moling.mocklocation.data.local.repository.MockServiceState
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.ui.components.IconPillSelector
import ink.moling.mocklocation.ui.components.LatLngScatter
import ink.moling.mocklocation.ui.dialog.ErrorDialog
import ink.moling.mocklocation.utils.extensions.isNumber
import ink.moling.mocklocation.utils.extensions.isValidAlt
import ink.moling.mocklocation.utils.extensions.isValidLat
import ink.moling.mocklocation.utils.extensions.isValidLng
import ink.moling.mocklocation.utils.extensions.replace
import ink.moling.mocklocation.utils.extensions.toDouble
import ink.moling.mocklocation.viewmodel.MainViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val focusManager = LocalFocusManager.current

    val mockStatus by viewModel.mockStatus.collectAsState()
    val location by viewModel.location.collectAsState()
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

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    var selectedSimulation by remember { mutableIntStateOf(0) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetState = scaffoldState.bottomSheetState
    val scaffoldExpanded by remember {
        derivedStateOf {
            sheetState.targetValue == SheetValue.Expanded ||
                    sheetState.currentValue == SheetValue.Expanded
        }
    }
    var editingSimPoint by remember { mutableStateOf(false) }
    var isPointModified by remember { mutableStateOf(false) }
    var isCreatingPoint by remember { mutableStateOf(false) }
    var cachedPointName = "<Placeholder>"
    var cachedPointLat = 0.0
    var cachedPointLng = 0.0
    var cachedPointAlt = 0.0
    val pointNameState = rememberTextFieldState(cachedPointName)
    val pointLatState = rememberTextFieldState("%.6f".format(cachedPointLat))
    val pointLngState = rememberTextFieldState("%.6f".format(cachedPointLng))
    val pointAltState = rememberTextFieldState("%.2f".format(cachedPointAlt))
    var pointLatVerified by remember { mutableStateOf(true) }
    var pointLngVerified by remember { mutableStateOf(true) }
    var pointAltVerified by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        snapshotFlow {
            listOf(
                pointNameState.text.toString(),
                pointLatState.text.toString(),
                pointLngState.text.toString(),
                pointAltState.text.toString()
            )
        }
            .distinctUntilChanged()
            .collect { values ->
                val (name, lat, lng, alt) = values
                isPointModified =
                    name != cachedPointName ||
                    lat != "%.6f".format(cachedPointLat) ||
                    lng != "%.6f".format(cachedPointLng) ||
                    alt != "%.2f".format(cachedPointAlt)
            }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp, // 半露高度
        sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.78f)
                    .padding(vertical = 16.dp, horizontal = 32.dp)
            ) {
                when (selectedSimulation) {
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

                            IconButton(onClick = {
                                isCreatingPoint = true
                                isPointModified = true
                                editingSimPoint = true
                                /* Cache current point details */
                                cachedPointName = pointNameState.text.toString()
                                cachedPointLat = pointLatState.toDouble()
                                cachedPointLng = pointLngState.toDouble()
                                cachedPointAlt = pointAltState.toDouble()
                                /* Clear text editor */
                                pointNameState.edit { delete(0, length) }
                                pointLatState.edit { delete(0, length) }
                                pointLngState.edit { delete(0, length) }
                                pointAltState.edit { delete(0, length) }
                                /* Collapse bottom sheet */
                                scope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            }) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = null
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            repeat(30) {
                                item {
                                    Card(
                                        onClick = {

                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text(
                                                "Point #$it",
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp)
                                            )
                                            Text(
                                                "@0.000000,0.000000#0.00",
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

                            }) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = null
                                )
                            }

                            IconButton(onClick = {

                            }) {
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
                                        onClick = {

                                        },
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
    ) { _ ->
        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !scaffoldExpanded && !editingSimPoint
            ) { page ->
                when (page) {
                    0 -> {
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
                                        Text("@0.000000,0.000000#0.00")
                                    }
                                    Text(
                                        "9C3XFXGX+PQ",
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
                                    onClick = { }
                                ) {
                                    /* Start / Stop */
                                    Text("Start")
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
                                            /* Point / Route -> <name> */
                                            text = when(selectedSimulation) {
                                                0    -> "${"Point"} > ${pointNameState.text}"
                                                else -> "${"Route"} > <Placeholder>"
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Icon(
                                        /*
                                     * Point: Icons.Outlined.LocationOn
                                     * Route: Icons.Outlined.Route
                                     * */
                                        imageVector = (
                                            when(selectedSimulation) {
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
                                            /* Idle / Mocking / Initializing */
                                            "Idle"
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Icon(
                                        /*
                                     * Idle: Icons.Outlined.LocationSearching
                                     * Mocking: Icons.Outlined.ShareLocation
                                     * Initializing: Icons.Outlined.Build
                                     * */
                                        Icons.Outlined.LocationSearching,
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

                                IconPillSelector(
                                    icons = listOf(
                                        Icons.Outlined.LocationOn,
                                        Icons.Outlined.Route
                                    ),
                                    enabled = !editingSimPoint,
                                    selectedIndex = selectedSimulation,
                                    onSelectedChange = { selectedSimulation = it }
                                )
                            }

                            when (selectedSimulation) {
                                0 -> {
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
                                                if (editingSimPoint) {
                                                    TextField(
                                                        state = pointNameState,
                                                        lineLimits = TextFieldLineLimits.SingleLine,
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
                                                    Text(
                                                        pointNameState.text.toString()
                                                    )
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
                                                if (editingSimPoint) {
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
                                                            state = pointLatState,
                                                            lineLimits = TextFieldLineLimits.SingleLine,
                                                            isError = !pointLatVerified,
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
                                                            state = pointLngState,
                                                            lineLimits = TextFieldLineLimits.SingleLine,
                                                            isError = !pointLngVerified,
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
                                                            state = pointAltState,
                                                            lineLimits = TextFieldLineLimits.SingleLine,
                                                            isError = !pointAltVerified,
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
                                                            pointLatState.text,
                                                            pointLngState.text,
                                                            pointAltState.text
                                                        )
                                                    )
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
                                                if (editingSimPoint) {
                                                    /* Verify input */
                                                    pointLatVerified = pointLatState.isNumber() && pointLatState.isValidLat()
                                                    pointLngVerified = pointLngState.isNumber() && pointLngState.isValidLng()
                                                    pointAltVerified = pointAltState.isNumber() && pointAltState.isValidAlt()
                                                    if (!pointLatVerified || !pointLngVerified || !pointAltVerified)
                                                        return@OutlinedButton
                                                    /* Reset UI */
                                                    isPointModified = false
                                                    isCreatingPoint = false
                                                    /* Standardize location detail */
                                                    val (lat, lng, alt) = listOf(
                                                        pointLatState.toDouble(),
                                                        pointLngState.toDouble(),
                                                        pointAltState.toDouble()
                                                    )
                                                    pointLatState.edit { replace("%.6f".format(lat)) }
                                                    pointLngState.edit { replace("%.6f".format(lng)) }
                                                    pointAltState.edit { replace("%.2f".format(alt)) }
                                                    /* TODO: Save point details to shared preferences */
                                                } else {
                                                    isPointModified = false
                                                    /* Cache current point details */
                                                    cachedPointName = pointNameState.text.toString()
                                                    cachedPointLat = pointLatState.toDouble()
                                                    cachedPointLng = pointLngState.toDouble()
                                                    cachedPointAlt = pointAltState.toDouble()
                                                }
                                                editingSimPoint = !editingSimPoint
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp),
                                            border = BorderStroke(
                                                2.dp,
                                                color = (
                                                    if (editingSimPoint && isPointModified)
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
                                                    if (editingSimPoint)
                                                        Icons.Outlined.Save
                                                    else
                                                        Icons.Outlined.Edit
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = (
                                                    if (editingSimPoint && isPointModified)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = (
                                                    if (editingSimPoint)
                                                        "Save"
                                                    else
                                                        "Edit"
                                                ),
                                                color = (
                                                    if (editingSimPoint && isPointModified)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        }

                                        if (editingSimPoint && !isCreatingPoint) {
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
                                                        editingSimPoint = false
                                                        /* Undo edit, restore cached details */
                                                        pointNameState.edit { replace(cachedPointName) }
                                                        pointLatState.edit { replace("%.6f".format(cachedPointLat)) }
                                                        pointLngState.edit { replace("%.6f".format(cachedPointLng)) }
                                                        pointAltState.edit { replace("%.2f".format(cachedPointAlt)) }
                                                    },
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
                                                onClick = {
                                                    if (isCreatingPoint) {
                                                        /* Undo edit, restore cached details */
                                                        pointNameState.edit { replace(cachedPointName) }
                                                        pointLatState.edit { replace("%.6f".format(cachedPointLat)) }
                                                        pointLngState.edit { replace("%.6f".format(cachedPointLng)) }
                                                        pointAltState.edit { replace("%.2f".format(cachedPointAlt)) }
                                                        /* Disable edit mode */
                                                        editingSimPoint = false
                                                        isCreatingPoint = false
                                                        isPointModified = false
                                                    }
                                                },
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

                                        if (editingSimPoint) {
                                            // Fill with current location
                                            IconButton(
                                                onClick = {

                                                }
                                            ) {
                                                Icon(
                                                    Icons.Outlined.MyLocation,
                                                    contentDescription = null
                                                )
                                            }

                                            // Select from map
                                            IconButton(
                                                onClick = {

                                                }
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Map,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    if (!editingSimPoint) {
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

                                1 -> {
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
                                                Text(
                                                    "<Placeholder>"
                                                )
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
                                            onClick = { },
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
                                            onClick = {

                                            }
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
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                )
            }
        }
    }

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(top = 64.dp, start = 8.dp, end = 8.dp)
//        ) {
//            // 应用标题
//            Text(
//                modifier = Modifier.padding(vertical = 32.dp),
//                text = appName,
//                fontWeight = FontWeight.Bold,
//                fontSize = 20.sp
//            )
//
//            // 位置服务信息显示
//            LocationServiceInfo(
//                mockStatus = mockStatus,
//                location = location
//            )
//
//            // 模拟设置卡片（包含点位和路径模式）
//            MockSettingsCard(
//                viewModel = viewModel,
//                refreshTrigger = refreshTrigger,
//                onImportExportClick = { viewModel.setImportExportDialogOpen(true) },
//                onAddPointClick = { viewModel.setAddPointDialogOpen(true) }
//            )
//
//            // TODO: 高度噪声卡片
//            // ExpandableCard(title = "Altitude noise [ Disabled ]") {
//            //     Text(text = "ExpandableCard")
//            // }
//        }
//
//        // 底部操作按钮
//        BottomActionButtons(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter)
//                .navigationBarsPadding()
//                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
//            mockStatus = mockStatus,
//            onStartStop = {
//                val mode = PrefsHelper.getMockMode(context)
//
//                val selected = when (mode) {
//                    "Point" -> viewModel.selectedMockPoint
//                    "Route" -> viewModel.selectedMockRoute
//                    else    -> null // ?
//                }
//
//                selected ?: run {
//                    Toast.makeText(
//                        context,
//                        "Please select ${mode.lowercase()}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@BottomActionButtons
//                }
//
//                if (mockStatus == MockServiceState.Enabled || mockStatus is MockServiceState.Error) {
//                    // 停止模拟
//                    focusManager.clearFocus()
//                    onStopMockLocation()
//                } else if (mockStatus == MockServiceState.Disabled) {
//                    // 启动模拟
//                    focusManager.clearFocus()
//                    onStartMockLocation()
//                }
//            },
//            onSettings = {
//                context.startActivity(
//                    Intent(context, SettingsActivity::class.java)
//                )
//            }
//        )
//    }
}

///**
// * 模拟设置卡片 - 支持点位模式和路径模式
// */
//@Composable
//fun MockSettingsCard(
//    viewModel: MainViewModel,
//    refreshTrigger: Int,
//    onImportExportClick: () -> Unit,
//    onAddPointClick: () -> Unit
//) {
//    val context = LocalContext.current
//    var currentMode by rememberSaveable { mutableStateOf(PrefsHelper.getMockMode(context)) }
//    val isRouteMode = currentMode == "Route"
//
//    ExpandableCard(
//        modifier = Modifier.height(if (isRouteMode) 350.dp else 150.dp),
//        title = "Mock settings [ $currentMode ]"
//    ) {
//        // 模式切换开关
//        ModeToggleSwitch(
//            isRouteMode = isRouteMode,
//            onModeChange = { isRoute ->
//                currentMode = if (isRoute) "Route" else "Point"
//                PrefsHelper.setMockMode(context, currentMode)
//            }
//        )
//
//        // 根据模式显示不同的内容
//        if (isRouteMode) {
//            RouteModeView(
//                viewModel = viewModel,
//                refreshTrigger = refreshTrigger,
//                onImportExportClick = onImportExportClick
//            )
//        } else {
//            PointModeView(
//                viewModel = viewModel,
//                onAddPointClick = onAddPointClick
//            )
//        }
//    }
//}
//
///**
// * 底部操作按钮（开始/停止 + 设置）
// */
//@Composable
//fun BottomActionButtons(
//    modifier: Modifier = Modifier,
//    mockStatus: MockServiceState,
//    onStartStop: () -> Unit,
//    onSettings: () -> Unit
//) {
//    Row(
//        modifier = modifier,
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.Bottom
//    ) {
//        // 开始/停止按钮
//        RectangleFloatingActionButton(
//            modifier = Modifier.weight(1f),
//            onClick = onStartStop
//        ) {
//            val buttonLabel = when (mockStatus) {
//                MockServiceState.Enabled -> "Stop"
//                MockServiceState.Disabled -> "Start"
//                MockServiceState.Initializing -> "Initializing"
//                else -> "Error"
//            }
//            val buttonIcon = when (mockStatus) {
//                MockServiceState.Enabled -> Icons.Filled.LocationOn
//                MockServiceState.Disabled -> Icons.Outlined.LocationOn
//                MockServiceState.Initializing -> Icons.Outlined.Build
//                else -> Icons.Filled.Warning
//            }
//            Icon(buttonIcon, contentDescription = buttonLabel)
//            Text(text = buttonLabel, modifier = Modifier.padding(start = 8.dp))
//        }
//
//        Spacer(modifier = Modifier.width(16.dp))
//
//        // 设置按钮
//        FloatingActionButton(
//            onClick = onSettings,
//            containerColor = MaterialTheme.colorScheme.primary,
//            contentColor = MaterialTheme.colorScheme.onPrimary,
//            elevation = FloatingActionButtonDefaults.elevation(8.dp),
//            modifier = Modifier.size(56.dp)
//        ) {
//            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
//        }
//    }
//}
//
///**
// * 模式切换开关组件（点位模式/路径模式）
// */
//@Composable
//fun ModeToggleSwitch(
//    isRouteMode: Boolean,
//    onModeChange: (Boolean) -> Unit
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.padding(start = 10.dp, bottom = 8.dp)
//    ) {
//        Text("Point")
//        Switch(
//            modifier = Modifier.padding(horizontal = 10.dp),
//            checked = isRouteMode,
//            onCheckedChange = onModeChange,
//            colors = SwitchDefaults.colors(
//                uncheckedTrackColor = Color.Transparent
//            )
//        )
//        Text("Route")
//    }
//}
//
///**
// * 位置服务信息显示组件
// */
//@Composable
//fun LocationServiceInfo(
//    mockStatus: MockServiceState,
//    location: CandidateLocation?
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant
//        )
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            when (mockStatus) {
//                MockServiceState.Enabled -> {
//                    Icon(
//                        Icons.Outlined.ShareLocation,
//                        contentDescription = "Mock Location",
//                        modifier = Modifier.padding(start = 20.dp)
//                    )
//                }
//                else -> {
//                    Icon(
//                        Icons.Outlined.MyLocation,
//                        contentDescription = "My location",
//                        modifier = Modifier.padding(start = 20.dp)
//                    )
//                }
//            }
//            Column(modifier = Modifier.padding(all = 12.dp)) {
//                Text(
//                    text = location?.let {
//                        "@%.6f,%.6f#%.2f".format(
//                            it.lat,
//                            it.lng,
//                            it.alt ?: 0.0f
//                        )
//                    } ?: "@0.000000,0.000000#0.00"
//                )
//                Text(
//                    text = location?.let {
//                        OpenLocationCode.encode(it.lat, it.lng, 11)
//                    } ?: ""
//                )
//            }
//        }
//    }
//}
