package ink.moling.mocklocation.service.locationService.controller

import android.content.Context
import android.content.Intent
import android.provider.Settings
import ink.moling.mocklocation.service.overlayService.ACTION_TOGGLE_JOYSTICK_VISIBILITY
import ink.moling.mocklocation.service.overlayService.EXTRA_DISPLAY_MODE
import ink.moling.mocklocation.service.overlayService.EXTRA_VISIBILITY
import ink.moling.mocklocation.service.overlayService.OverlayService
import ink.moling.mocklocation.utils.MockMode
import ink.moling.mocklocation.utils.logger.Logger

private const val TAG = "OverlayServiceController"

class OverlayServiceController(
    private val context: Context
) {
    private var isServiceStarted = false
    private var displayMode = 0 // Point = 0, Route = 1
    
    /**
     * 启动悬浮窗服务
     *
     * @return 是否成功启动。缺少 SYSTEM_ALERT_WINDOW 权限时返回 false，
     *         因为即使启动了 OverlayService 也无法添加悬浮窗。
     */
    fun start(): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            Logger.w(TAG, "System alert window permission missing, skip starting OverlayService")
            isServiceStarted = false
            return false
        }

        val intent = Intent(context, OverlayService::class.java).apply {
            putExtra(EXTRA_DISPLAY_MODE, displayMode)
        }
        return try {
            context.startService(intent)
            isServiceStarted = true
            true
        } catch (e: IllegalStateException) {
            // Android 8+ 在后台不允许启动普通 Service，不能让异常传到调用方
            Logger.e(TAG, "Not allowed to start OverlayService", e)
            isServiceStarted = false
            false
        } catch (e: SecurityException) {
            Logger.e(TAG, "SecurityException while starting OverlayService", e)
            isServiceStarted = false
            false
        }
    }

    fun stop() {
        context.stopService(
            Intent(context, OverlayService::class.java)
        )
        isServiceStarted = false
    }
    
    /**
     * 设置 Joystick 的可见性（不停止服务）
     * 
     * @param visible true 显示，false 隐藏
     */
    fun setVisibility(visible: Boolean) {
        if (!isServiceStarted) {
            // 如果服务未启动，先启动服务
            if (visible) {
                start()
            }
            return
        }
        
        val intent = Intent(context, OverlayService::class.java).apply {
            action = ACTION_TOGGLE_JOYSTICK_VISIBILITY
            putExtra(EXTRA_VISIBILITY, visible)
            putExtra(EXTRA_DISPLAY_MODE, displayMode)
        }
        try {
            context.startService(intent)
        } catch (e: IllegalStateException) {
            Logger.e(TAG, "Not allowed to start OverlayService", e)
            isServiceStarted = false
        } catch (e: SecurityException) {
            Logger.e(TAG, "SecurityException while starting OverlayService", e)
            isServiceStarted = false
        }
    }

    fun setMode(mode: Int) {
        displayMode = mode
    }
}