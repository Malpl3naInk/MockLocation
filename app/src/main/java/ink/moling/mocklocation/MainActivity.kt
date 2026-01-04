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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                                LatLng(30.31278782, 120.37452974, "00", listOf(1, 36)),
                                LatLng(30.31270001, 120.37486252, "01", listOf(0, 2)),
                                LatLng(30.31255188, 120.37488464, "02", listOf(1, 3, 5)),
                                LatLng(30.31176745, 120.37448947, "03", listOf(2, 4)),
                                LatLng(30.31151951, 120.37454468, "04", listOf(3, 14, 37)),
                                LatLng(30.31254331, 120.37556238, "05", listOf(2, 6, 28)),
                                LatLng(30.31215474, 120.37559495, "06", listOf(5, 15)),
                                LatLng(30.31089503, 120.37400104, "07", listOf(8, 29)),
                                LatLng(30.31099922, 120.37415835, "08", listOf(7, 9)),
                                LatLng(30.31107804, 120.37453341, "09", listOf(8, 10)),
                                LatLng(30.31109505, 120.37481889, "10", listOf(9, 11)),
                                LatLng(30.31099153, 120.37506155, "11", listOf(10, 12)),
                                LatLng(30.31101573, 120.37525065, "12", listOf(11, 13)),
                                LatLng(30.31107460, 120.37518060, "13", listOf(12, 14)),
                                LatLng(30.31148371, 120.37516653, "14", listOf(4, 13, 15)),
                                LatLng(30.31152987, 120.37561017, "15", listOf(6, 14, 16)),
                                LatLng(30.31159333, 120.37585899, "16", listOf(15, 17, 28)),
                                LatLng(30.31143965, 120.37597601, "17", listOf(16, 18)),
                                LatLng(30.31133080, 120.37615380, "18", listOf(17, 19)),
                                LatLng(30.31130983, 120.37634747, "19", listOf(18, 20)),
                                LatLng(30.31135689, 120.37654047, "20", listOf(19, 21)),
                                LatLng(30.31155897, 120.37677538, "21", listOf(20, 22)),
                                LatLng(30.31258439, 120.37678427, "22", listOf(21, 23)),
                                LatLng(30.31272915, 120.37669964, "23", listOf(22, 24)),
                                LatLng(30.31282787, 120.37658566, "24", listOf(23, 25)),
                                LatLng(30.31290106, 120.37630929, "25", listOf(24, 26)),
                                LatLng(30.31281503, 120.37600992, "26", listOf(25, 27)),
                                LatLng(30.31271835, 120.37591156, "27", listOf(26, 28)),
                                LatLng(30.31262546, 120.37586177, "28", listOf(5, 16, 27)),
                                LatLng(30.31101727, 120.37364696, "29", listOf(7, 30)),
                                LatLng(30.31146107, 120.37361030, "30", listOf(29, 31, 37)),
                                LatLng(30.31157536, 120.37357460, "31", listOf(30, 32, 38)),
                                LatLng(30.31163393, 120.37349810, "32", listOf(31, 33)),
                                LatLng(30.31195135, 120.37347551, "33", listOf(32, 34)),
                                LatLng(30.31261212, 120.37348310, "34", listOf(33, 35)),
                                LatLng(30.31272823, 120.37365519, "35", listOf(34, 36)),
                                LatLng(30.31274091, 120.37431647, "36", listOf(0, 35, 44)),
                                LatLng(30.31154134, 120.37368335, "37", listOf(4, 30, 38)),
                                LatLng(30.31165234, 120.37367430, "38", listOf(31, 37, 39)),
                                LatLng(30.31208896, 120.37388341, "39", listOf(38, 40)),
                                LatLng(30.31215410, 120.37396471, "40", listOf(39, 41)),
                                LatLng(30.31225064, 120.37398847, "41", listOf(40, 42)),
                                LatLng(30.31235995, 120.37402515, "42", listOf(41, 43)),
                                LatLng(30.31242018, 120.37407256, "43", listOf(42, 44)),
                                LatLng(30.31250551, 120.37406562, "44", listOf(36, 43)),
                            )
                            ExpandableCard(
                                modifier = Modifier
                                    .height(240.dp),
                                title = "Service mode [ Route ]"
                            ) {
                                LatLonScatter(
                                    points = points,
                                    pointRadius = 2.dp
                                )
                            }
                            ExpandableCard(title = "Altitude noise") {
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