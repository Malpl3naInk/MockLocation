package ink.moling.mocklocation.utils

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import ink.moling.mocklocation.utils.logger.Logger

/**
 * 权限处理管理类
 * 封装权限请求逻辑，简化 Activity 代码
 */
class PermissionHandler(private val activity: ComponentActivity) {

    private val tag = "PermissionHandler"

    // 权限状态
    var hasRequiredPermissions = mutableStateOf(false)
        private set

    // 对话框显示状态
    var showPermissionDialog = mutableStateOf(false)
        private set
    var showOverlayPermissionDialog = mutableStateOf(false)
        private set
    var pendingPermissions = mutableStateOf<List<PermissionInfo>>(emptyList())
        private set

    // 权限请求回调
    private var onPermissionsGranted: (() -> Unit)? = null

    // 前台权限请求器
    private val requestForegroundPermissions: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            Logger.d(tag, "Permission results: $results")

            hasRequiredPermissions.value = PermissionHelper.hasForegroundLocationPermissions(activity)

            if (hasRequiredPermissions.value) {
                Logger.d(tag, "Foreground permissions granted")
                requestBackgroundLocationIfNeeded()
                onPermissionsGranted?.invoke()
            } else {
                Logger.w(tag, "Foreground permissions denied")
            }
        }

    // 后台权限请求器
    private val requestBackgroundPermission: ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            Logger.d(tag, "Background location permission: $granted")
        }

    /**
     * 检查并请求权限
     */
    fun checkAndRequestPermissions(onGranted: (() -> Unit)? = null) {
        onPermissionsGranted = onGranted
        Logger.d(tag, "Checking permissions...")

        if (PermissionHelper.hasForegroundLocationPermissions(activity)) {
            Logger.d(tag, "Already have required permissions")
            hasRequiredPermissions.value = true
            requestBackgroundLocationIfNeeded()
            onPermissionsGranted?.invoke()
        } else {
            val missingPermissions = PermissionHelper.getMissingPermissions(activity)
                .filter { it.permission != Manifest.permission.ACCESS_BACKGROUND_LOCATION }

            if (missingPermissions.isNotEmpty()) {
                Logger.d(tag, "Missing permissions: ${missingPermissions.map { it.name }}")
                pendingPermissions.value = missingPermissions
                showPermissionDialog.value = true
            }
        }
    }

    /**
     * 执行权限请求
     */
    fun performPermissionRequest() {
        val permissionsToRequest = PermissionHelper.getForegroundPermissionsToRequest(activity)
        if (permissionsToRequest.isNotEmpty()) {
            Logger.d(tag, "Requesting permissions: ${permissionsToRequest.joinToString()}")
            requestForegroundPermissions.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * 请求后台定位权限（如果需要）
     */
    private fun requestBackgroundLocationIfNeeded() {
        // TODO: 严格需要时才请求后台权限
        if (PermissionHelper.needsBackgroundLocationPermission(activity) && false) {
            Logger.d(tag, "Requesting background location permission")
            requestBackgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        requestOverlayPermissionIfNeeded()
    }

    /**
     * 请求悬浮窗权限（如果需要）
     */
    private fun requestOverlayPermissionIfNeeded() {
        if (PermissionHelper.needsOverlayPermission(activity)) {
            Logger.d(tag, "Need overlay permission")
            val missingSpecialPermissions = PermissionHelper.getMissingSpecialPermissions(activity)
            if (missingSpecialPermissions.isNotEmpty()) {
                pendingPermissions.value = missingSpecialPermissions
                showOverlayPermissionDialog.value = true
            }
        }
    }

    /**
     * 跳转到悬浮窗权限设置页面
     */
    fun requestOverlayPermission() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivity(intent)
        } catch (e: Exception) {
            Logger.e(tag, "Failed to open overlay permission settings", e)
        }
    }

    /**
     * 隐藏权限对话框
     */
    fun dismissPermissionDialog() {
        showPermissionDialog.value = false
    }

    /**
     * 隐藏悬浮窗权限对话框
     */
    fun dismissOverlayPermissionDialog() {
        showOverlayPermissionDialog.value = false
    }
}
