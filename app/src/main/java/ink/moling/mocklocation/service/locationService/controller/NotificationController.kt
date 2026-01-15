package ink.moling.mocklocation.service.locationService.controller

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import ink.moling.mocklocation.R
import ink.moling.mocklocation.service.locationService.state.LocationMode

const val SERVICE_MOCK_LOC_NOTE_ID = 1
const val SERVICE_MOCK_LOC_NOTE_CHANNEL_ID = "SERVICE_MOCK_LOC_NOTE"
const val SERVICE_MOCK_LOC_NOTE_CHANNEL_NAME = "SERVICE_MOCK_LOC_NOTE"

class NotificationController(
    private val service: Service
) {

    private val notificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

    private var currentMode: LocationMode = LocationMode.Idle

    fun startForeground() {
        createChannelIfNeeded()
        service.startForeground(
            SERVICE_MOCK_LOC_NOTE_ID,
            buildNotification(currentMode)
        )
    }

    fun stopForeground() {
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
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

    private fun Double.format() =
        String.format("%.5f", this)

    private fun buildNotification(mode: LocationMode): Notification {
        return NotificationCompat.Builder(
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
            .setContentText(
                when (mode) {
                    LocationMode.Idle ->
                        "I'm a teapot!"
                    is LocationMode.Point ->
                        "@${mode.lat.format()}, ${mode.lng.format()}"
                    is LocationMode.Route ->
                        "${mode.speedMps} m/s"
                }
            )
            .build()
    }
}

