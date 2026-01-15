package ink.moling.mocklocation.service.locationService.controller

import android.content.Context
import android.content.Intent
import ink.moling.mocklocation.service.JoystickService

class JoystickServiceController(
    private val context: Context
) {
    fun start() {
        val intent = Intent(context, JoystickService::class.java)
        context.startService(intent)
    }

    fun stop() {
        context.stopService(
            Intent(context, JoystickService::class.java)
        )
    }
}