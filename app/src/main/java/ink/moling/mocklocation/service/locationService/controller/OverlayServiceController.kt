package ink.moling.mocklocation.service.locationService.controller

import android.content.Context
import android.content.Intent
import ink.moling.mocklocation.service.overlayService.ACTION_TOGGLE_JOYSTICK_VISIBILITY
import ink.moling.mocklocation.service.overlayService.EXTRA_DISPLAY_MODE
import ink.moling.mocklocation.service.overlayService.EXTRA_VISIBILITY
import ink.moling.mocklocation.service.overlayService.OverlayService
import ink.moling.mocklocation.utils.MockMode

class OverlayServiceController(
    private val context: Context
) {
    private var isServiceStarted = false
    private var displayMode = 0 // Point = 0, Route = 1
    
    fun start() {
        val intent = Intent(context, OverlayService::class.java).apply {
            putExtra(EXTRA_DISPLAY_MODE, displayMode)
        }
        context.startService(intent)
        isServiceStarted = true
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
        context.startService(intent)
    }

    fun setMode(mode: Int) {
        displayMode = mode
    }
}