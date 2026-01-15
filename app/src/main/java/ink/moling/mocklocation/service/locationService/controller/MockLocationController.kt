@file:Suppress("DEPRECATION")

package ink.moling.mocklocation.service.locationService.controller

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Process
import android.os.SystemClock
import android.util.Log
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.Source
import ink.moling.mocklocation.utils.LocationSimulator
import ink.moling.mocklocation.utils.SimulatedLocation

const val HANDLER_MSG_ID = 0
const val SERVICE_MOCK_LOC_HANDLER_NAME = "ServiceMockLocation"
class MockLocationController(
    private val locationManager: LocationManager,
    private val output: (CandidateLocation) -> Unit
) {
    // ===== 线程 & 调度 =====
    private val tickMs = 100L
    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    
    // ===== 状态 =====
    private var simulator: LocationSimulator? = null

    fun start() {
        // 如果已经在运行，直接返回
        if (thread?.isAlive == true) return

        // 创建新的线程（支持重复启动）
        thread = HandlerThread(
            SERVICE_MOCK_LOC_HANDLER_NAME,
            Process.THREAD_PRIORITY_FOREGROUND
        ).apply {
            start()
        }
        
        handler = object : Handler(thread!!.looper) {
            override fun handleMessage(msg: Message) {
                tick()
                sendEmptyMessageDelayed(HANDLER_MSG_ID, tickMs)
            }
        }
        handler?.sendEmptyMessage(HANDLER_MSG_ID)
    }


    fun stop() {
        simulator = null
        handler?.removeCallbacksAndMessages(null)
        thread?.quitSafely()
        handler = null
        thread = null
    }

    private fun tick() {
        val sim = simulator ?: return

        val loc = sim.next(tickMs)

        injectToSystem(loc)
        emitState(loc)
    }

    private fun injectGps(loc: SimulatedLocation) {
        val l = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = loc.lat
            longitude = loc.lng
            altitude = loc.alt
            bearing = loc.bearing
            speed = loc.speed.toFloat()
            accuracy = 1.5f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            extras = Bundle().apply {
                putInt("satellites", 19)
            }
        }

        locationManager.setTestProviderLocation(
            LocationManager.GPS_PROVIDER, l
        )
    }

    private fun injectNetwork(loc: SimulatedLocation) {
        val l = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = loc.lat
            longitude = loc.lng
            altitude = loc.alt
            bearing = loc.bearing
            speed = loc.speed.toFloat()
            accuracy = 5f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }

        locationManager.setTestProviderLocation(
            LocationManager.NETWORK_PROVIDER, l
        )
    }

    private fun injectToSystem(loc: SimulatedLocation) {
        try {
            injectGps(loc)
            injectNetwork(loc)
        } catch (_: IllegalArgumentException) {
            // is not a test provider → 忽略
        } catch (e: Exception) {
            Log.e("MockLoc_Ctrl", "inject failed", e)
        }
    }

    private fun emitState(loc: SimulatedLocation) {
        output(
            CandidateLocation(
                lat = loc.lat,
                lng = loc.lng,
                accuracy = 1.5f,
                time = System.currentTimeMillis(),
                alt = loc.alt,
                source = Source.MOCK
            )
        )
    }

    fun setSimulator(sim: LocationSimulator?) {
        simulator = sim
    }

    fun isRunning(): Boolean = simulator != null
}
