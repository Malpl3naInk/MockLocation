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
import ink.moling.mocklocation.service.MockLocationService
import ink.moling.mocklocation.ui.MainScreen
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.LocationKalmanFilter
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
    private lateinit var mServiceBinder: MockLocationService.MockLocationServiceBinder
    private lateinit var mService: MockLocationService
    private lateinit var mConnection: ServiceConnection
    
    /*
     * 位置管理
     */
    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener
    private val kf = LocationKalmanFilter()

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
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && ::mLocationListener.isInitialized) {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
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
        if (::mLocationListener.isInitialized) {
            mLocationManager.removeUpdates(mLocationListener)
        }
    }

    override fun onDestroy() {
        if (::mConnection.isInitialized) {
            try {
                unbindService(mConnection)
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
            mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // 初始化服务连接
            mConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    mServiceBinder = service as MockLocationService.MockLocationServiceBinder
                    viewModel.onServiceBinderReady(mServiceBinder)
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
            
            // 设置生命周期监听和位置更新
            DisposableEffect(Unit) {
                // 初始化 LocationListener（需要访问 ViewModel）
                mLocationListener = LocationListener { location ->
                    val (lat, lng) = kf.update(
                        location.latitude,
                        location.longitude,
                        location.accuracy,
                        location.time
                    )
                    val provider = (location.provider ?: "*")[0].toString().uppercase()
                    val altitude = if (provider == "G") location.altitude else viewModel.gpsAltitude.value
                    
                    viewModel.updateGpsLocation(lat, lng, altitude, provider)
                }
                
                // 设置服务 Binder 引用到 ViewModel
                val binderJob = lifecycleScope.launch {
                    // 等待服务连接
                    while (!::mServiceBinder.isInitialized) {
                        kotlinx.coroutines.delay(100)
                    }
                    viewModel.serviceBinder.value = mServiceBinder
                }

                // 权限检查并开启位置更新
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
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
                
                // 清理
                onDispose {
                    binderJob.cancel()
                    mLocationManager.removeUpdates(mLocationListener)
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
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    /**
     * 停止模拟位置服务
     */
    private fun stopMockLocation() {
        try {
            if (::mConnection.isInitialized) {
                unbindService(mConnection)
            }
            val serviceMockLocation = Intent(this, MockLocationService::class.java)
            stopService(serviceMockLocation)
        } catch (_: Exception) {
            // Service might not be running
        }
    }
}
