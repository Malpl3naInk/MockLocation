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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.Source
import ink.moling.mocklocation.data.repository.MockServiceState
import ink.moling.mocklocation.service.MockLocationService
import ink.moling.mocklocation.ui.MainScreen
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.KalmanFilter
import ink.moling.mocklocation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * MainActivity
 * 
 * 职责：
 * - 管理 Activity 生命周期
 * - 权限申请和管理
 * - LocationManager 集成
 * - MockLocationService 绑定和管理
 * - 将系统级事件桥接到 ViewModel
 */
class MainActivity : ComponentActivity() {
    /*
     * MockLocation 服务
     */
    private lateinit var serviceBinder: MockLocationService.MockLocationServiceBinder
    private lateinit var connection: ServiceConnection
    
    /*
     * 位置管理
     */
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var kf = KalmanFilter()

    // 权限申请相关
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // 前台定位
            val fine = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarse = results[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            // 后台定位（Android 10+）
            val background = results[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
            // 通知（Android 13+）
            val notif = results[Manifest.permission.POST_NOTIFICATIONS] ?: false
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
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && ::locationListener.isInitialized) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000L,
                0f,
                locationListener
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (::locationListener.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }

    override fun onDestroy() {
        if (::connection.isInitialized) {
            try {
                unbindService(connection)
            } catch (_: Exception) {
                // Service might not be bound
            }
        }
        stopMockLocation()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()

        setContent {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel()

            // 初始化 LocationManager
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // 初始化服务连接
            connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    serviceBinder = service as MockLocationService.MockLocationServiceBinder
                    viewModel.onServiceBinderReady(serviceBinder)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    // 服务意外断开
                }
            }

            LaunchedEffect(Unit) {
                viewModel.needStartService.collect {
                    startMockLocation()
                }
            }
            
            // 监听 Mock 状态变化，在停用时重置 Kalman 滤波器
            LaunchedEffect(Unit) {
                viewModel.mockStatus.collect { status ->
                    if (status == MockServiceState.Disabled) {
                        kf.reset()
                    }
                }
            }
            
            // 设置生命周期监听和位置更新
            DisposableEffect(Unit) {
                // 初始化 LocationListener（需要访问 ViewModel）
                locationListener = LocationListener { location ->
                    if (viewModel.mockStatus.value == MockServiceState.Enabled) return@LocationListener

                    val source = when (location.provider) {
                        LocationManager.GPS_PROVIDER -> Source.GPS
                        LocationManager.NETWORK_PROVIDER -> Source.NETWORK
                        else -> return@LocationListener
                    }

                    val c = if (source == Source.GPS) {
                        val (lat, lng) = kf.update(
                            location.latitude,
                            location.longitude,
                            location.accuracy,
                            location.time
                        )
                        CandidateLocation(
                            lat = lat,
                            lng = lng,
                            accuracy = location.accuracy,
                            time = location.time,
                            alt = location.altitude,
                            source = Source.GPS
                        )
                    } else {
                        CandidateLocation(
                            lat = location.latitude,
                            lng = location.longitude,
                            accuracy = location.accuracy,
                            time = location.time,
                            alt = null,
                            source = Source.NETWORK
                        )
                    }

                    when (source) {
                        Source.GPS -> viewModel.updateGpsCandidate(c)
                        Source.NETWORK -> viewModel.updateNetCandidate(c)
                    }
                }
                
                // 设置服务 Binder 引用到 ViewModel
                val binderJob = lifecycleScope.launch {
                    // 等待服务连接
                    while (!::serviceBinder.isInitialized) {
                        kotlinx.coroutines.delay(100)
                    }
                    viewModel.serviceBinder.value = serviceBinder
                }

                // 权限检查并开启位置更新
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        0f,
                        locationListener
                    )
                    
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        0f,
                        locationListener
                    )
                }
                
                // 清理
                onDispose {
                    binderJob.cancel()
                    locationManager.removeUpdates(locationListener)
                }
            }
            
            MockLocationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        appName = resources.getString(R.string.app_name),
                        onStartMockLocation = { startMockLocation() },
                        onStopMockLocation = { stopMockLocation() }
                    )
                }
            }
        }
    }

    /**
     * 启动模拟位置服务
     */
    private fun startMockLocation() {
        val intent = Intent(this, MockLocationService::class.java)
        startForegroundService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    /**
     * 停止模拟位置服务
     */
    private fun stopMockLocation() {
        try {
            if (::connection.isInitialized) {
                unbindService(connection)
            }
            val serviceMockLocation = Intent(this, MockLocationService::class.java)
            stopService(serviceMockLocation)
        } catch (_: Exception) {
            // Service might not be running
        }
    }
}
