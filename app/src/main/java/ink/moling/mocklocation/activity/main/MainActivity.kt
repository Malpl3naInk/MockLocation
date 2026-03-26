package ink.moling.mocklocation.activity.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.nativelib.NativeLib
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.ui.dialog.ErrorDialog
import ink.moling.mocklocation.ui.dialog.RequestPermissionDialog
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.ui.theme.ThemeStateHolder
import ink.moling.mocklocation.utils.LocaleHelper
import ink.moling.mocklocation.utils.MockMode
import ink.moling.mocklocation.utils.PermissionHandler
import ink.moling.mocklocation.utils.logger.CrashHandler
import ink.moling.mocklocation.utils.logger.Logger
import ink.moling.mocklocation.utils.logger.LoggerFile

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.setLocale(it) })
    }
    private lateinit var connection: ServiceConnection
    private lateinit var permissionHandler: PermissionHandler

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeStateHolder.themeMode.value = PrefsHelper.getThemeMode(this)

        // 初始化权限处理器
        permissionHandler = PermissionHandler(this)
        permissionHandler.checkAndRequestPermissions()

        // 初始化 logger
        LoggerFile.init(this)

        // 设置 Crash handler
        val default = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(default))

        // 测试 JNI
        val native = NativeLib()
        Logger.d(TAG, native.stringFromJNI())

        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            // 服务连接
            connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocationService.MockLocationServiceBinder
                    viewModel.onServiceBinderReady(binder)
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            }

            // 权限获取后启动服务
            LaunchedEffect(permissionHandler.hasRequiredPermissions.value) {
                if (permissionHandler.hasRequiredPermissions.value) {
                    startAndBindService()
                }
            }

            // 监听 ViewModel 的服务启动请求
            LaunchedEffect(Unit) {
                viewModel.needStartService.collect {
                    if (permissionHandler.hasRequiredPermissions.value) {
                        startAndBindService()
                    }
                }
            }

            MockLocationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    // 权限对话框
                    if (permissionHandler.showPermissionDialog.value) {
                        RequestPermissionDialog(
                            permissions = permissionHandler.pendingPermissions.value,
                            onConfirm = {
                                permissionHandler.dismissPermissionDialog()
                                permissionHandler.performPermissionRequest()
                            },
                            onDismiss = { permissionHandler.dismissPermissionDialog() }
                        )
                    }

                    // 悬浮窗权限对话框
                    if (permissionHandler.showOverlayPermissionDialog.value) {
                        RequestPermissionDialog(
                            permissions = permissionHandler.pendingPermissions.value,
                            onConfirm = {
                                permissionHandler.dismissOverlayPermissionDialog()
                                permissionHandler.requestOverlayPermission()
                            },
                            onDismiss = { permissionHandler.dismissOverlayPermissionDialog() }
                        )
                    }

                    // 错误对话框
                    val errorInfo by viewModel.errorInfo.collectAsState()
                    errorInfo?.let { error ->
                        ErrorDialog(
                            onDismiss = { viewModel.clearError() },
                            title = error.title,
                            text = error.message,
                            stackTrace = error.stackTrace
                        )
                    }

                    MainScreen(
                        viewModel = viewModel,
                        appName = getString(R.string.app_name),
                        onStartMockLocation = {
                            if (uiState.selectedSimulation == MockMode.POINT) {
                                viewModel.selectedMockPoint?.let { point ->
                                    viewModel.setMockLocation(point.name, point.lat, point.lng, point.alt)
                                }
                            } else {
                                viewModel.selectedMockRoute?.let { route ->
                                    viewModel.setMockLocation(route.details)
                                }
                            }
                        },
                        onStopMockLocation = { viewModel.stopMockLocation() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 检查语言是否变化，如果变化则重新创建 Activity
        if (LocaleHelper.hasLanguageChanged(this)) {
            recreate()
        }
    }

    override fun onDestroy() {
        try {
            unbindService(connection)
        } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun startAndBindService() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }
}
