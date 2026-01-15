package ink.moling.mocklocation

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.ui.MainScreen
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var connection: ServiceConnection

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()

        setContent {
            val viewModel: MainViewModel = viewModel()

            // 初始化服务连接（在 Composable 内部，以便访问 ViewModel）
            connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocationService.MockLocationServiceBinder
                    viewModel.onServiceBinderReady(binder)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    // Service 断开连接
                }
            }

            // 启动并绑定服务
            LaunchedEffect(Unit) {
                startAndBindService()
            }

            // 监听 ViewModel 的服务启动请求
            LaunchedEffect(Unit) {
                viewModel.needStartService.collect {
                    startAndBindService()
                }
            }

            MockLocationTheme {
                Surface {
                    MainScreen(
                        viewModel = viewModel,
                        appName = getString(R.string.app_name),
                        onStartMockLocation = {
                            // 通过 ViewModel 启动模拟（保持架构清晰）
                            viewModel.selectedMockPoint?.let { point ->
                                viewModel.setMockPosition(
                                    point.latitude,
                                    point.longitude,
                                    point.altitude
                                )
                            }
                        },
                        onStopMockLocation = {
                            // 通过 ViewModel 停止模拟
                            viewModel.stopMockPosition()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            unbindService(connection)
        } catch (_: Exception) {
            // Service might not be bound
        }
        super.onDestroy()
    }

    /**
     * 启动并绑定位置服务
     * - 启动为前台服务，可以持续运行
     * - 绑定以获取 Binder 接口
     */
    private fun startAndBindService() {
        val intent = Intent(this, LocationService::class.java)
        // 先启动服务（前台服务）
        startForegroundService(intent)
        // 再绑定服务
        bindService(intent, connection, BIND_AUTO_CREATE)
    }
}
