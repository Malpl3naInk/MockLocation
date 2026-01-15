@file:Suppress("DEPRECATION")

package ink.moling.mocklocation.service.locationService

import android.Manifest
import android.app.Service
import android.content.Intent
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.annotation.RequiresPermission
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.repository.MockServiceState
import ink.moling.mocklocation.data.repository.MockServiceStatusRepository
import ink.moling.mocklocation.service.locationService.controller.JoystickServiceController
import ink.moling.mocklocation.service.locationService.controller.MockLocationController
import ink.moling.mocklocation.service.locationService.controller.NotificationController
import ink.moling.mocklocation.service.locationService.controller.RealLocationController
import ink.moling.mocklocation.service.locationService.controller.TestProviderManager
import ink.moling.mocklocation.service.locationService.state.LocationMode
import ink.moling.mocklocation.service.locationService.state.LocationStateHolder
import ink.moling.mocklocation.utils.KalmanFilter
import ink.moling.mocklocation.utils.StaticPointSimulator
import kotlinx.coroutines.flow.StateFlow

class LocationService : Service() {
    // 控制器
    private lateinit var providerMgr: TestProviderManager
    private lateinit var realCtrl: RealLocationController
    private lateinit var mockCtrl: MockLocationController
    private lateinit var notifyCtrl: NotificationController
    private lateinit var joystickCtrl: JoystickServiceController

    // 状态管理
    private lateinit var locationStateHolder: LocationStateHolder
    private val kf = KalmanFilter()
    
    // 系统服务
    private lateinit var locationManager: LocationManager
    
    // 数据绑定
    private val binder = MockLocationServiceBinder()
    override fun onBind(intent: Intent?): IBinder = binder

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        locationStateHolder = LocationStateHolder()

        // 初始化系统服务
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // 初始化控制器
        mockCtrl = MockLocationController(
            locationManager = locationManager
        ) { locationStateHolder.updateFromMock(it) }
        realCtrl = RealLocationController(
            context = this,
            kf = kf,
            isMocking = { mockCtrl.isRunning() }
        ) { locationStateHolder.updateFromRealLocation(it) }
        notifyCtrl = NotificationController(this)
        joystickCtrl = JoystickServiceController(this)

        // 只启动真实位置监听，模拟控制器在需要时才启动
        realCtrl.start()
        notifyCtrl.startForeground()

        // Service 启动但未开始模拟，状态为 Disabled
        MockServiceStatusRepository.state.value = MockServiceState.Disabled
    }

    override fun onDestroy() {
        mockCtrl.stop()
        realCtrl.stop()
        notifyCtrl.stopForeground()
        providerMgr.teardown()
        MockServiceStatusRepository.state.value = MockServiceState.Disabled
        super.onDestroy()
    }

    inner class MockLocationServiceBinder : Binder() {
        fun locationFlow(): StateFlow<CandidateLocation?> =
            locationStateHolder.state

        fun setStaticPoint(lat: Double, lng: Double, alt: Double) {
            // 创建 TestProvider
            providerMgr = TestProviderManager(locationManager)
            providerMgr.setup()

            // 设置模拟器并启动模拟控制器
            mockCtrl.setSimulator(StaticPointSimulator(lat, lng, alt))
            mockCtrl.start()

            // 启动悬浮摇杆
            joystickCtrl.start()
            
            // 停止真实位置监听，重置卡尔曼滤波器
            realCtrl.stop()
            kf.reset()

            // 更新通知显示
            notifyCtrl.updateMode(
                LocationMode.Point(lat, lng)
            )

            // 更新状态为已启用
            MockServiceStatusRepository.state.value = MockServiceState.Enabled
        }

        /* TODO: Path simulation
        fun startPathSimulation(
            path: List<PathPoint>,
            speedMps: Double
        ) {
            simulator = PathSimulator(path, speedMps)
        }*/

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        fun stopSimulation() {
            // 停止模拟控制器
            mockCtrl.stop()

            // 关闭悬浮摇杆
            joystickCtrl.stop()

            providerMgr.teardown()
            
            // 重启真实位置监听，重置卡尔曼滤波器
            realCtrl.start()
            kf.reset()

            // 更新通知显示
            notifyCtrl.updateMode(LocationMode.Idle)

            // 更新状态为已禁用
            MockServiceStatusRepository.state.value = MockServiceState.Disabled
        }
    }
}