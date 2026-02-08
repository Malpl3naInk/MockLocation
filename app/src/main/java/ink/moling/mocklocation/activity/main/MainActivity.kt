package ink.moling.mocklocation.activity.main

import android.Manifest
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.viewmodel.compose.viewModel
import ink.moling.mocklocation.NativeLib
import ink.moling.mocklocation.R
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.ui.dialog.ErrorDialog
import ink.moling.mocklocation.ui.dialog.RequestPermissionDialog
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.PermissionHelper
import ink.moling.mocklocation.utils.PermissionInfo
import ink.moling.mocklocation.utils.logger.CrashHandler
import ink.moling.mocklocation.utils.logger.Logger
import ink.moling.mocklocation.utils.logger.LoggerFile

class MainActivity : ComponentActivity() {
    private lateinit var connection: ServiceConnection

    // 权限状态标志（使用 mutableStateOf 以便 Compose 可以观察）
    private var hasRequiredPermissions by mutableStateOf(false)

    // 显示权限说明对话框的状态
    private var showPermissionDialog by mutableStateOf(false)
    private var showOverlayPermissionDialog by mutableStateOf(false)
    private var pendingPermissions by mutableStateOf<List<PermissionInfo>>(emptyList())

    companion object {
        private const val TAG = "MainActivity"
    }

    // 前台权限和通知权限申请（第一步）
    private val requestForegroundPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            Logger.d(TAG, "Permission results: $results")

            // 检查是否获得了必需的定位权限
            hasRequiredPermissions = PermissionHelper.hasForegroundLocationPermissions(this)

            // 如果前台权限已授予，请求后台权限
            if (hasRequiredPermissions) {
                Logger.d(TAG, "Foreground permissions granted, requesting background permission")
                requestBackgroundLocationIfNeeded()
            } else {
                Logger.w(TAG, "Foreground permissions denied")
            }
        }

    // 后台定位权限申请（第二步，仅 Android 10+）
    private val requestBackgroundPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Logger.d(TAG, "Background location permission: $granted")
        }

    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermissions() {
        Logger.d(TAG, "Checking permissions...")

        // 检查是否已有所有必需的前台权限
        if (PermissionHelper.hasForegroundLocationPermissions(this)) {
            Logger.d(TAG, "Already have required permissions")
            hasRequiredPermissions = true
            // 检查并请求后台定位权限
            requestBackgroundLocationIfNeeded()
        } else {
            // 获取缺失的权限
            val missingPermissions = PermissionHelper.getMissingPermissions(this)
                .filter { it.permission != Manifest.permission.ACCESS_BACKGROUND_LOCATION }

            if (missingPermissions.isNotEmpty()) {
                Logger.d(TAG, "Missing permissions: ${missingPermissions.map { it.name }}")
                // 显示权限说明对话框
                pendingPermissions = missingPermissions
                showPermissionDialog = true
            }
        }
    }

    /**
     * 执行权限请求
     */
    private fun performPermissionRequest() {
        val permissionsToRequest = PermissionHelper.getForegroundPermissionsToRequest(this)
        if (permissionsToRequest.isNotEmpty()) {
            Logger.d(TAG, "Requesting permissions: ${permissionsToRequest.joinToString()}")
            requestForegroundPermissions.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * 请求后台定位权限（如果需要）
     */
    private fun requestBackgroundLocationIfNeeded() {
        /* TODO: Check background location if strictly needed */
        if (PermissionHelper.needsBackgroundLocationPermission(this) && false) {
            Logger.d(TAG, "Requesting background location permission")
            requestBackgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        // 请求悬浮窗权限
        requestOverlayPermissionIfNeeded()
    }

    /**
     * 请求悬浮窗权限（如果需要）
     */
    private fun requestOverlayPermissionIfNeeded() {
        if (PermissionHelper.needsOverlayPermission(this)) {
            Logger.d(TAG, "Need overlay permission")
            val missingSpecialPermissions = PermissionHelper.getMissingSpecialPermissions(this)
            if (missingSpecialPermissions.isNotEmpty()) {
                // 显示悬浮窗权限说明对话框
                pendingPermissions = missingSpecialPermissions
                showOverlayPermissionDialog = true
            }
        }
    }

    /**
     * 跳转到悬浮窗权限设置页面
     */
    private fun requestOverlayPermission() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open overlay permission settings", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 检查权限
        checkAndRequestPermissions()

        // 初始化 logger 本地文件
        LoggerFile.init(this)

        // 设置自定义 Crash handler
        val default =
            Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(default)
        )

        // 测试 JNI 调用
        val native = NativeLib()
        Logger.d(TAG, native.stringFromJNI())

        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

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

            // 启动并绑定服务 - 当权限状态变为已授予时自动启动
            LaunchedEffect(hasRequiredPermissions) {
                Logger.d(
                    TAG,
                    "LaunchedEffect triggered, hasRequiredPermissions: $hasRequiredPermissions"
                )
                if (hasRequiredPermissions) {
                    Logger.d(TAG, "Starting and binding service")
                    startAndBindService()
                }
            }

            // 监听 ViewModel 的服务启动请求
            LaunchedEffect(Unit) {
                viewModel.needStartService.collect {
                    if (hasRequiredPermissions) {
                        startAndBindService()
                    }
                }
            }

            MockLocationTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize()
                ) {
                    // 显示常规权限说明对话框
                    if (showPermissionDialog) {
                        RequestPermissionDialog(
                            permissions = pendingPermissions,
                            onConfirm = {
                                showPermissionDialog = false
                                performPermissionRequest()
                            },
                            onDismiss = {
                                showPermissionDialog = false
                                // 用户取消，不请求权限
                            }
                        )
                    }

                    // 显示悬浮窗权限说明对话框
                    if (showOverlayPermissionDialog) {
                        RequestPermissionDialog(
                            permissions = pendingPermissions,
                            onConfirm = {
                                showOverlayPermissionDialog = false
                                requestOverlayPermission()
                            },
                            onDismiss = {
                                showOverlayPermissionDialog = false
                                // 用户取消，不请求悬浮窗权限
                            }
                        )
                    }

                    // 显示错误对话框
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
                            // 通过 ViewModel 启动模拟（保持架构清晰）
                            if (uiState.selectedSimulation == 0) {
                                // 启动点位模拟
                                viewModel.selectedMockPoint?.let { point ->
                                    viewModel.setMockLocation(
                                        point.lat,
                                        point.lng,
                                        point.alt
                                    )
                                }
                            } else {
                                // 启动路径模拟
                            }
                        },
                        onStopMockLocation = {
                            // 通过 ViewModel 停止模拟
                            viewModel.stopMockLocation()
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