package ink.moling.mocklocation

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.LocationKalmanFilter
import ink.moling.mocklocation.widgets.ExpandableCard
import ink.moling.mocklocation.widgets.LatLng
import ink.moling.mocklocation.widgets.LatLonScatter
import ink.moling.mocklocation.widgets.RectangleFloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    lateinit var mServiceBinder: MockLocationService.MockLocationServiceBinder
    lateinit var mService: MockLocationService
    lateinit var mConnection: ServiceConnection
    lateinit var mLocationManager: LocationManager
    lateinit var mLocationListener: LocationListener
    val kf = LocationKalmanFilter()
    var mockStatus by mutableIntStateOf(MOCK_STATUS_DISABLED)
    var gpsLatitude by mutableDoubleStateOf(0.0)
    var gpsLongitude by mutableDoubleStateOf(0.0)
    var gpsAltitude by mutableDoubleStateOf(0.0)
    var gpsProvider by mutableStateOf("")

    // 权限申请相关
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // 前台定位
            val fine        = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarse      = results[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            // 后台定位（Android 10+）
            val background  = results[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
            // 通知（Android 13+）
            val notif       = results[Manifest.permission.POST_NOTIFICATIONS] ?: false
        }

    private fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        val perms = mutableListOf<String>()
        // 前台定位
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            perms += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            perms += Manifest.permission.ACCESS_COARSE_LOCATION
        }
        // 通知（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                perms += Manifest.permission.POST_NOTIFICATIONS
            }
        }
        // 后台定位（Android 10+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                // 注意：系统要求先授权前台定位，然后才能申请后台定位
                perms += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }
        }
        if (perms.isNotEmpty()) {
            requestPermissions.launch(perms.toTypedArray())
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onResume() {
        super.onResume()
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,     // 1秒
                0f,
                mLocationListener
            )
            mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000L,
                0f,
                mLocationListener
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mLocationManager.removeUpdates(mLocationListener)
    }

    override fun onDestroy() {
        unbindService(mConnection)
        stopMockLocation()
        super.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationListener = LocationListener { location ->
            val (lat, lon) = kf.update(
                location.latitude,
                location.longitude,
                location.accuracy,
                location.time
            )
            gpsProvider = (location.provider?:"*")[0].toString().uppercase()
            gpsLatitude = lat
            gpsLongitude = lon
            if (gpsProvider == "G") gpsAltitude = location.altitude
        }

        mConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?,service: IBinder?) {
                mServiceBinder = service as MockLocationService.MockLocationServiceBinder
                mService = mServiceBinder.getService()
                lifecycleScope.launch {
                    mockStatus = mService.stateFlow.value
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }

        }

        setContent {
            val context = LocalContext.current
            DisposableEffect(Unit) {
                // 权限检查
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // GPS 实时监听
                    mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,     // 1秒
                        0f,
                        mLocationListener
                    )

                    // 网络定位监听（可选，提高首次定位速度）
                    mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        0f,
                        mLocationListener
                    )
                }

                // 清理
                onDispose {
                    mLocationManager.removeUpdates(mLocationListener)
                }
            }
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (mockStatus == MOCK_STATUS_ERR_NO_PERM) {
                        AlertDialog(
                            onDismissRequest = {
                                stopMockLocation()
                                mockStatus = MOCK_STATUS_DISABLED
                            },
                            title = { Text("Permission Not Granted") },
                            text = { Text("Program is not allowed to perform MOCK_LOCATION") },
                            confirmButton = {
                                TextButton({
                                    stopMockLocation()
                                    mockStatus = MOCK_STATUS_DISABLED
                                }) { Text("OK") }
                            }
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
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 32.dp),
                                text = resources.getText(R.string.app_name).toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Column(
                                modifier = Modifier.padding(all = 8.dp)
                            ) {
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
                            // Testing data
                            val points = listOf(
                                LatLng(30.31278782, 120.37452974, "R", listOf(1, 36)),
                                LatLng(30.31270001, 120.37486252, "R", listOf(0, 2)),
                                LatLng(30.31255188, 120.37488464, "R", listOf(1, 3, 5)),
                                LatLng(30.31176745, 120.37448947, "R", listOf(2, 4)),
                                LatLng(30.31151951, 120.37454468, "W", listOf(3, 9, 14, 37)),
                                LatLng(30.31254331, 120.37556238, "R", listOf(2, 6, 28)),
                                LatLng(30.31215474, 120.37559495, "R", listOf(5, 15)),
                                LatLng(30.31089503, 120.37400104, "R", listOf(8, 29)),
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
                            ExpandableCard(
                                modifier = Modifier
                                    .height(260.dp),
                                title = "Service mode [ Route ]"
                            ) {
                                val options = listOf("Option A", "Option B", "Option C")
                                var expanded by remember { mutableStateOf(false) }
                                var selected by remember { mutableStateOf(options[0]) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded },
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    TextField(
                                        value = selected,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select route") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    selected = option
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Row {
                                    LatLonScatter(
                                        modifier = Modifier.weight(0.8f),
                                        paddingDp = 0.dp,
                                        cardPadding = 8.dp,
                                        points = points,
                                        pointRadius = 1.dp
                                    )
                                    Column {
                                        IconButton(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(vertical = 5.dp),
                                            onClick = {

                                            }
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
                                            onClick = {

                                            }
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
                                            onClick = {

                                            }
                                        ) {
                                            Icon(
                                                Icons.Outlined.Add,
                                                contentDescription = "New",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            ExpandableCard(title = "Altitude noise [ Disabled ]") {
                                Text(text = "ExpandableCard")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()           // 避开底部手势栏
                                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, // 或 SpaceEvenly
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // 开始/停止按钮
                            RectangleFloatingActionButton(
                                modifier = Modifier.weight(1f), // 关键：占满剩余空间
                                onClick = {
                                    if (mockStatus == MOCK_STATUS_ENABLED || mockStatus == MOCK_STATUS_ERR_NO_PERM) {
                                        stopMockLocation()
                                        mockStatus = MOCK_STATUS_DISABLED
                                    } else if (mockStatus == MOCK_STATUS_DISABLED) {
                                        startMockLocation()
                                        mockStatus = MOCK_STATUS_INITIALIZING
                                    }
                                }
                            ) {
                                val buttonLabel = when (mockStatus) {
                                    MOCK_STATUS_ENABLED         -> "Stop"
                                    MOCK_STATUS_DISABLED        -> "Start"
                                    MOCK_STATUS_INITIALIZING    -> "Initializing"
                                    else                        -> "Error"
                                }
                                val buttonIcon = when (mockStatus) {
                                    MOCK_STATUS_ENABLED         -> Icons.Filled.LocationOn
                                    MOCK_STATUS_DISABLED        -> Icons.Outlined.LocationOn
                                    MOCK_STATUS_INITIALIZING    -> Icons.Outlined.Build
                                    else                        -> Icons.Filled.Warning
                                }
                                Icon(buttonIcon, contentDescription = buttonLabel)
                                Text(text = buttonLabel, modifier = Modifier.padding(start = 8.dp))
                            }

                            // 间隔
                            Spacer(modifier = Modifier.width(16.dp))

                            // 设置 FAB
                            FloatingActionButton(
                                onClick = {
                                    mServiceBinder.setPosition(53.4519076,-3.0029668,48.0)
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMockLocation() {
        val serviceMockLocation = Intent(this, MockLocationService::class.java)
        startForegroundService(serviceMockLocation)
        bindService(serviceMockLocation, mConnection, BIND_AUTO_CREATE) // 绑定服务与活动
    }

    private fun stopMockLocation() {
        unbindService(mConnection)
        val serviceMockLocation = Intent(this, MockLocationService::class.java)
        stopService(serviceMockLocation)
    }
}