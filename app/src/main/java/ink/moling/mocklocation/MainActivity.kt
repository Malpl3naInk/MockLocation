package ink.moling.mocklocation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.widgets.ExpandableCard

const val MOCK_STATUS_DISABLED      = -1
const val MOCK_STATUS_INITIALIZING  = 0
const val MOCK_STATUS_ENABLED       = 1

class MainActivity : ComponentActivity() {
    lateinit var mServiceBinder: MockLocationService.MockLocationServiceBinder
    lateinit var mConnection: ServiceConnection
    var mockStatus by mutableIntStateOf(MOCK_STATUS_DISABLED)
    var gpsLatitude by mutableDoubleStateOf(0.0)
    var gpsLongitude by mutableDoubleStateOf(0.0)
    var gpsAltitude by mutableDoubleStateOf(0.0)
    var gpsProvider by mutableStateOf("")

    override fun onDestroy() {
        unbindService(mConnection)
        stopMockLocation()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?,service: IBinder?) {
                mServiceBinder = service as MockLocationService.MockLocationServiceBinder
                mockStatus = MOCK_STATUS_ENABLED
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }

        }

        setContent {
            val context = LocalContext.current
            val locationManager = remember {
                context.getSystemService(LOCATION_SERVICE) as LocationManager
            }
            DisposableEffect(Unit) {
                val listener = LocationListener { location ->
                    gpsLatitude = location.latitude
                    gpsLongitude = location.longitude
                    gpsAltitude = location.altitude
                    gpsProvider = (location.provider?:"*")[0].toString().uppercase()
                    Log.d("MockLoc_Main", "$gpsProvider@$gpsLatitude,$gpsLongitude,$gpsAltitude")
                }
                // 权限检查
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // GPS 实时监听
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,     // 1秒
                        0f,
                        listener
                    )

                    // 网络定位监听（可选，提高首次定位速度）
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        0f,
                        listener
                    )
                }

                // 清理
                onDispose {
                    locationManager.removeUpdates(listener)
                }
            }
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                            Column {
                                Text("Location Service")
                                Text(
                                    "$gpsProvider@$gpsLatitude,$gpsLongitude,$gpsAltitude",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            ExpandableCard(title = "type") {
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
                            Surface(
                                onClick = {
                                    if (mockStatus == MOCK_STATUS_ENABLED) {
                                        stopMockLocation()
                                        mockStatus = MOCK_STATUS_DISABLED
                                    } else if (mockStatus == MOCK_STATUS_DISABLED) {
                                        startMockLocation()
                                        mockStatus = MOCK_STATUS_INITIALIZING
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)                     // 关键：占满剩余空间
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shadowElevation = 6.dp,             // 悬浮阴影
                                tonalElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val buttonLabel = when (mockStatus) {
                                        MOCK_STATUS_ENABLED -> "Stop"
                                        MOCK_STATUS_DISABLED -> "Start"
                                        MOCK_STATUS_INITIALIZING -> "Initializing"
                                        else -> "wtf"
                                    }
                                    val buttonIcon = when (mockStatus) {
                                        MOCK_STATUS_ENABLED -> Icons.Filled.LocationOn
                                        MOCK_STATUS_DISABLED -> Icons.Outlined.LocationOn
                                        MOCK_STATUS_INITIALIZING -> Icons.Outlined.Build
                                        else -> Icons.Filled.Warning
                                    }
                                    Icon(buttonIcon, contentDescription = buttonLabel)
                                    Text(text = buttonLabel)
                                }
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