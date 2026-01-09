package ink.moling.mocklocation.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.repository.MockServiceState
import ink.moling.mocklocation.data.repository.MockServiceStatusRepository
import ink.moling.mocklocation.utils.LocationSimulator
import ink.moling.mocklocation.utils.StaticPointSimulator

/* 定位相关 */
const val DEFAULT_LAT = 51.476853
const val DEFAULT_LNG = 0.0
const val DEFAULT_ALT = 694.0
const val DEFAULT_BEA = 0.0f
const val HANDLER_MSG_ID = 0
const val SERVICE_MOCK_LOC_HANDLER_NAME = "ServiceMockLocation"
/* 通知相关 */
const val SERVICE_MOCK_LOC_NOTE_ID = 1
const val SERVICE_MOCK_LOC_NOTE_CHANNEL_ID = "SERVICE_MOCK_LOC_NOTE"
const val SERVICE_MOCK_LOC_NOTE_CHANNEL_NAME = "SERVICE_MOCK_LOC_NOTE"

class MockLocationService : Service() {
    /* 定位相关 */
    var mCurLat = DEFAULT_LAT
    var mCurLng = DEFAULT_LNG
    var mCurAlt = DEFAULT_ALT
    var mCurBea = DEFAULT_BEA
    val mSpeed = 1.2
    lateinit var mLocationManager: LocationManager
    lateinit var mLocHandlerThread: HandlerThread
    lateinit var mLocHandler: Handler

    private val mBinder = MockLocationServiceBinder()

    override fun onBind(intent: Intent?): IBinder = mBinder

    @Volatile
    private var simulator: LocationSimulator? = null

    override fun onCreate() {
        super.onCreate()

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        initNotification()

        removeTestProviderNetwork()
        if (!addTestProviderNetwork()) return
        Log.d("MockLoc_Service", "NETWORK_PROVIDER initialized")

        removeTestProviderGPS()
        if (!addTestProviderGPS()) return
        Log.d("MockLoc_Service", "GPS_PROVIDER initialized")

        initMockLocation()

        MockServiceStatusRepository.state.value = MockServiceState.Enabled
    }

    override fun onDestroy() {
        removeTestProviderNetwork()
        removeTestProviderGPS()

        stopForeground(STOP_FOREGROUND_REMOVE)

        super.onDestroy()
    }

    // ----------------
    // 初始化位置模拟
    // ----------------
    private fun initMockLocation() {
        mLocHandlerThread = HandlerThread(
            SERVICE_MOCK_LOC_HANDLER_NAME,
            Process.THREAD_PRIORITY_FOREGROUND
        )
        mLocHandlerThread.start()

        mLocHandler = object : Handler(mLocHandlerThread.looper) {

            private val tickMs = 100L

            override fun handleMessage(msg: Message) {

                // 由模拟器推进位置
                simulator?.let {
                    val loc = it.next(tickMs)
                    // Log.d("MockLoc_Service", "Next tick=$mCurLat,$mCurLng,$mCurAlt")
                    mCurLat = loc.lat
                    mCurLng = loc.lng
                    mCurAlt = loc.alt
                    mCurBea = loc.bearing
                    // speed 如需可同步
                }

                // 注入系统定位
                setLocationNetwork()
                setLocationGPS()

                // 安排下一帧
                sendEmptyMessageDelayed(HANDLER_MSG_ID, tickMs)
            }
        }

        mLocHandler.sendEmptyMessage(HANDLER_MSG_ID)
    }


    // ----------------
    // 初始化保活通知
    // ----------------
    @SuppressLint("ForegroundServiceType")
    private fun initNotification() {
        val mChannel = NotificationChannel(
            SERVICE_MOCK_LOC_NOTE_CHANNEL_ID,
            SERVICE_MOCK_LOC_NOTE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(mChannel)

        val notification = NotificationCompat.Builder(this, SERVICE_MOCK_LOC_NOTE_CHANNEL_ID)
            .setChannelId(SERVICE_MOCK_LOC_NOTE_CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("MockLocation Service Running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(SERVICE_MOCK_LOC_NOTE_ID, notification)
    }

    // ----------------
    // 移除当前 GPS Test Provider
    // ----------------
    private fun removeTestProviderGPS(): Boolean {
        try {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
                mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            }
        } catch (e: Exception) {
            Log.e("MockLoc_Service", "removeTestProviderGPS ${e.message}")
        }
        return true
    }

    // ----------------
    // 添加 GPS Test Provider
    // ----------------
    @SuppressLint("WrongConstant")
    private fun addTestProviderGPS(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                mLocationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    Criteria.POWER_HIGH,
                    Criteria.ACCURACY_FINE
                )
            }
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            }
            return true
        } catch (e: Exception) {
            Log.e("MockLoc_Service", "addTestProviderGPS ${e.message}")
            MockServiceStatusRepository.state.value =
                MockServiceState.Error(
                    type = "Permission Not Granted",
                    msg = "Program is not allowed to perform MOCK_LOCATION",
                    stackTrace = e.stackTraceToString()
                )
            return false
        }
    }

    // ----------------
    // 修改 GPS 定位位置
    // ----------------
    private fun setLocationGPS() {
        try {
            val loc = Location(LocationManager.GPS_PROVIDER)
            loc.accuracy = Criteria.ACCURACY_FINE.toFloat()
            loc.altitude = mCurAlt  // 高度
            loc.bearing = mCurBea   // 方向角度
            loc.latitude = mCurLat  // 纬度
            loc.longitude = mCurLng // 经度
            loc.time = System.currentTimeMillis()
            loc.speed = mSpeed.toFloat()
            loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            val bundle = Bundle()   // 卫星数量
            bundle.putInt("satellites", 19)
            loc.extras = bundle

            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)
        } catch (e: Exception) {
            Log.e("MockLoc_Service", "setLocationGPS ${e.message}")
        }
    }

    // ----------------
    // 移除当前 Network Test Provider
    // ----------------
    private fun removeTestProviderNetwork(): Boolean {
        try {
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
                mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            Log.e("MockLoc_Service", "removeTestProviderNetwork ${e.message}")
        }
        return true
    }

    // ----------------
    // 添加 Network Test Provider
    // ----------------
    @SuppressLint("WrongConstant")
    private fun addTestProviderNetwork(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocationManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER,
                    true,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                mLocationManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER,
                    true,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    Criteria.POWER_HIGH,
                    Criteria.ACCURACY_FINE
                )
            }
            if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
            }
            return true
        } catch (e: Exception) {
            Log.e("MockLoc_Service", "addTestProviderNetwork ${e.message}")
            MockServiceStatusRepository.state.value =
                MockServiceState.Error(
                    type = "Permission Not Granted",
                    msg = "Program is not allowed to perform MOCK_LOCATION",
                    stackTrace = e.stackTraceToString()
                )
            return false
        }
    }

    // ----------------
    // 修改网络定位位置
    // ----------------
    private fun setLocationNetwork() {
        try {
            val loc = Location(LocationManager.NETWORK_PROVIDER)
            loc.accuracy = Criteria.ACCURACY_COARSE.toFloat()
            loc.altitude = mCurAlt  // 高度
            loc.bearing = mCurBea   // 方向角度
            loc.latitude = mCurLat  // 纬度
            loc.longitude = mCurLng // 经度
            loc.time = System.currentTimeMillis()
            loc.speed = mSpeed.toFloat()
            loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

            mLocationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, loc)
        } catch (e: Exception) {
            Log.e("MockLoc_Service", "setLocationNetwork ${e.message}")
        }
    }

    inner class MockLocationServiceBinder : Binder() {
        fun getService() = this@MockLocationService

        fun setStaticPoint(lat: Double, lng: Double, alt: Double) {
            Log.d("MockLoc_Service", "setStaticPoint $lat,$lng,$alt")
            simulator = StaticPointSimulator(lat, lng, alt)
        }

        /*fun startPathSimulation(
            path: List<PathPoint>,
            speedMps: Double
        ) {
            simulator = PathSimulator(path, speedMps)
        }*/

        fun stopSimulation() {
            simulator = null
        }
    }
}