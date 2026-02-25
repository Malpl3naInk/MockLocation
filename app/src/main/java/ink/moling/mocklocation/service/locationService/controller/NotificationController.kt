package ink.moling.mocklocation.service.locationService.controller

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import ink.moling.mocklocation.R
import ink.moling.mocklocation.service.locationService.state.LocationMode

const val SERVICE_MOCK_LOC_NOTE_ID = 1
const val SERVICE_MOCK_LOC_NOTE_CHANNEL_ID = "SERVICE_MOCK_LOC_NOTE"
const val SERVICE_MOCK_LOC_NOTE_CHANNEL_NAME = "SERVICE_MOCK_LOC_NOTE"

// Notification Actions
const val ACTION_TOGGLE_OVERLAY = "ink.moling.mocklocation.ACTION_TOGGLE_OVERLAY"

class NotificationController(
    private val service: Service,
    private val onToggleOverlay: (Boolean) -> Unit
) {

    private val notificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

    private var currentMode: LocationMode = LocationMode.Idle
    
    private var isOverlayVisible: Boolean = false
    
    // BroadcastReceiver 处理通知按钮点击
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_TOGGLE_OVERLAY -> {
                    isOverlayVisible = !isOverlayVisible
                    onToggleOverlay(isOverlayVisible)
                    // 更新通知以反映新的状态
                    notificationManager.notify(
                        SERVICE_MOCK_LOC_NOTE_ID,
                        buildNotification(currentMode)
                    )
                }
            }
        }
    }

    fun startForeground() {
        createChannelIfNeeded()
        registerReceiver()
        service.startForeground(
            SERVICE_MOCK_LOC_NOTE_ID,
            buildNotification(currentMode)
        )
    }

    fun stopForeground() {
        unregisterReceiver()
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }
    
    fun setOverlayVisibility(isVisible: Boolean) {
        isOverlayVisible = isVisible
        // 更新通知
        notificationManager.notify(
            SERVICE_MOCK_LOC_NOTE_ID,
            buildNotification(currentMode)
        )
    }
    
    // 注册 BroadcastReceiver
    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_TOGGLE_OVERLAY)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            service.registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            service.registerReceiver(notificationReceiver, filter)
        }
    }
    
    // 注销 BroadcastReceiver
    private fun unregisterReceiver() {
        try {
            service.unregisterReceiver(notificationReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver 未注册，忽略
        }
    }

    fun updateMode(mode: LocationMode) {
        currentMode = mode
        notificationManager.notify(
            SERVICE_MOCK_LOC_NOTE_ID,
            buildNotification(mode)
        )
    }

    // ----------------------------
    // Channel
    // ----------------------------

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            SERVICE_MOCK_LOC_NOTE_CHANNEL_ID,
            SERVICE_MOCK_LOC_NOTE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mock location foreground service"
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
        }

        notificationManager.createNotificationChannel(channel)
    }

    // ----------------------------
    // Notification Builder
    // ----------------------------

    private fun buildNotification(mode: LocationMode): Notification {
        val toggleIntent = Intent(ACTION_TOGGLE_OVERLAY).apply {
            setPackage(service.packageName)
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            service,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val buttonText = if (isOverlayVisible) "Hide Overlay" else "Show Overlay"
        var builder = NotificationCompat.Builder(
            service,
            SERVICE_MOCK_LOC_NOTE_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentTitle(
                "MockLocation ${
                    when (mode) {
                        LocationMode.Idle -> "[Idle]"
                        is LocationMode.Point -> "[Point]"
                        is LocationMode.Route -> "[Route]"
                    }
                }"
            )
        if (mode != LocationMode.Idle) {
            builder = builder.addAction(
                0, // 无图标
                buttonText,
                togglePendingIntent
            )
        }
        
        return builder.build()
    }
}

