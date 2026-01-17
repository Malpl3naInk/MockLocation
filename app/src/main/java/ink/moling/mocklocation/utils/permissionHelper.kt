package ink.moling.mocklocation.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat

/**
 * 权限信息数据类
 */
data class PermissionInfo(
    val permission: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val isRequired: Boolean = true,
    val isSpecialPermission: Boolean = false,  // 是否是特殊权限（需要跳转设置页面）
    val minSdkVersion: Int = Build.VERSION_CODES.BASE
)

/**
 * 权限帮助类
 */
object PermissionHelper {
    
    /**
     * 应用所需的所有权限
     */
    val REQUIRED_PERMISSIONS = listOf(
        PermissionInfo(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            name = "Fine Location",
            description = "Required to access and mock precise location information",
            icon = Icons.Default.LocationOn,
            isRequired = true
        ),
        PermissionInfo(
            permission = Manifest.permission.ACCESS_COARSE_LOCATION,
            name = "Coarse Location",
            description = "Required to access and mock approximate location information",
            icon = Icons.Default.MyLocation,
            isRequired = true
        ),
        PermissionInfo(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            name = "Notifications",
            description = "Required to display foreground service notifications",
            icon = Icons.Default.Notifications,
            isRequired = true,
            minSdkVersion = Build.VERSION_CODES.TIRAMISU
        ),
        PermissionInfo(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            name = "Background Location",
            description = "Required to continue mocking location when app is in background",
            icon = Icons.Default.LocationSearching,
            isRequired = true,
            minSdkVersion = Build.VERSION_CODES.Q
        ),
        PermissionInfo(
            permission = Manifest.permission.SYSTEM_ALERT_WINDOW,
            name = "Display Over Other Apps",
            description = "Required to display floating joystick for quick location adjustments",
            icon = Icons.Default.Layers,
            isRequired = true,
            isSpecialPermission = true
        )
    )
    
    /**
     * 检查单个权限是否已授予
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        // 悬浮窗权限需要特殊检查
        if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
            return Settings.canDrawOverlays(context)
        }
        return ActivityCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否拥有所有必需的权限
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return getRequiredPermissionsForCurrentDevice().all { 
            hasPermission(context, it.permission)
        }
    }
    
    /**
     * 获取当前设备需要的权限（排除不适用于当前系统版本的权限）
     */
    fun getRequiredPermissionsForCurrentDevice(): List<PermissionInfo> {
        return REQUIRED_PERMISSIONS.filter { 
            Build.VERSION.SDK_INT >= it.minSdkVersion 
        }
    }
    
    /**
     * 获取所有未授予的权限
     */
    fun getMissingPermissions(context: Context): List<PermissionInfo> {
        return getRequiredPermissionsForCurrentDevice().filter { 
            !hasPermission(context, it.permission)
        }
    }
    
    /**
     * 获取需要请求的前台权限（不包括后台定位和特殊权限）
     */
    fun getForegroundPermissionsToRequest(context: Context): List<String> {
        return getMissingPermissions(context)
            .filter { 
                it.permission != Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
                !it.isSpecialPermission
            }
            .map { it.permission }
    }
    
    /**
     * 检查是否需要请求后台定位权限
     */
    fun needsBackgroundLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }
        return !hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    
    /**
     * 检查是否有前台定位权限
     */
    fun hasForegroundLocationPermissions(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    
    /**
     * 检查是否需要请求悬浮窗权限
     */
    fun needsOverlayPermission(context: Context): Boolean {
        return !Settings.canDrawOverlays(context)
    }
    
    /**
     * 获取缺失的特殊权限
     */
    fun getMissingSpecialPermissions(context: Context): List<PermissionInfo> {
        return getMissingPermissions(context).filter { it.isSpecialPermission }
    }
}