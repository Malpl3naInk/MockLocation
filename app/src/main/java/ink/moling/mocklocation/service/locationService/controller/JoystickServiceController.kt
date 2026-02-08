package ink.moling.mocklocation.service.locationService.controller

import android.content.Context
import android.content.Intent
import ink.moling.mocklocation.service.joystickService.ACTION_TOGGLE_JOYSTICK_VISIBILITY
import ink.moling.mocklocation.service.joystickService.EXTRA_VISIBILITY
import ink.moling.mocklocation.service.joystickService.JoystickService

class JoystickServiceController(
    private val context: Context
) {
    private var isServiceStarted = false
    
    fun start() {
        val intent = Intent(context, JoystickService::class.java)
        context.startService(intent)
        isServiceStarted = true
    }

    fun stop() {
        context.stopService(
            Intent(context, JoystickService::class.java)
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
        
        val intent = Intent(context, JoystickService::class.java).apply {
            action = ACTION_TOGGLE_JOYSTICK_VISIBILITY
            putExtra(EXTRA_VISIBILITY, visible)
        }
        context.startService(intent)
    }
}