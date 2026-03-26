@file:Suppress("DEPRECATION")

package ink.moling.mocklocation.service.locationService

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.annotation.RequiresPermission
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.data.local.repository.MockServiceState
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.service.locationService.controller.MockLocationController
import ink.moling.mocklocation.service.locationService.controller.NotificationController
import ink.moling.mocklocation.service.locationService.controller.OverlayServiceController
import ink.moling.mocklocation.service.locationService.controller.RealLocationController
import ink.moling.mocklocation.service.locationService.controller.TestProviderManager
import ink.moling.mocklocation.service.locationService.state.LocationMode
import ink.moling.mocklocation.service.locationService.state.LocationStateHolder
import ink.moling.mocklocation.service.overlayService.EXTRA_POINT_ALT
import ink.moling.mocklocation.service.overlayService.EXTRA_POINT_ID
import ink.moling.mocklocation.service.overlayService.EXTRA_POINT_LAT
import ink.moling.mocklocation.service.overlayService.EXTRA_POINT_LNG
import ink.moling.mocklocation.service.overlayService.EXTRA_POINT_NAME
import ink.moling.mocklocation.service.overlayService.ACTION_SELECT_POINT
import ink.moling.mocklocation.service.overlayService.state.OverlayStateHolder
import ink.moling.mocklocation.utils.KalmanFilter
import ink.moling.mocklocation.utils.MockMode
import ink.moling.mocklocation.utils.simulators.DynamicRouteSimulator
import ink.moling.mocklocation.utils.simulators.StaticPointSimulator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationService : Service() {
    // 控制器
    private var providerMgr: TestProviderManager? = null
    private lateinit var realCtrl: RealLocationController
    private lateinit var mockCtrl: MockLocationController
    private lateinit var notifyCtrl: NotificationController
    private lateinit var overlayCtrl: OverlayServiceController

    // 状态管理
    private lateinit var locationStateHolder: LocationStateHolder
    private val kf = KalmanFilter()
    
    // 系统服务
    private lateinit var locationManager: LocationManager
    
    // 错误状态
    data class ErrorInfo(
        val title: String,
        val message: String,
        val stackTrace: String
    )
    
    private val _errorState = MutableStateFlow<ErrorInfo?>(null)
    
    // 数据绑定
    private val binder = MockLocationServiceBinder()
    override fun onBind(intent: Intent?): IBinder = binder

    // 广播接收器
    private val selectPointReceiver = object : BroadcastReceiver() {
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_SELECT_POINT -> {
                    val name = intent.getStringExtra(EXTRA_POINT_NAME) ?: return
                    val lat = intent.getDoubleExtra(EXTRA_POINT_LAT, 0.0)
                    val lng = intent.getDoubleExtra(EXTRA_POINT_LNG, 0.0)
                    val alt = intent.getDoubleExtra(EXTRA_POINT_ALT, 0.0)
                    if (lat != 0.0 || lng != 0.0) {
                        binder.setStaticPoint(name, lat, lng, alt)
                    }
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        locationStateHolder = LocationStateHolder()

        // 初始化系统服务
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // 初始化 TestProviderManager 并清理可能残留的 TestProvider
        providerMgr = TestProviderManager(
            locationManager = locationManager,
            onError = { title, message, stackTrace ->
                _errorState.value = ErrorInfo(title, message, stackTrace)
            }
        )
        // 清理可能由于上次应用异常退出而残留的 TestProvider
        providerMgr?.teardown()

        // 初始化控制器
        mockCtrl = MockLocationController(
            locationManager = locationManager
        ) { locationStateHolder.updateFromMock(it) }
        realCtrl = RealLocationController(
            context = this,
            kf = kf,
            isMocking = { mockCtrl.isRunning() }
        ) { loc ->
            locationStateHolder.updateFromRealLocation(loc)
            notifyCtrl.updateLocation(loc.lat, loc.lng)
        }
        overlayCtrl = OverlayServiceController(this)
        notifyCtrl = NotificationController(
            service = this,
            onToggleOverlay = { isVisible -> toggleJoystick(isVisible) }
        )

        // 只启动真实位置监听，模拟控制器在需要时才启动
        realCtrl.start()
        notifyCtrl.startForeground()

        // Service 启动但未开始模拟，状态为 Disabled
        MockServiceStatusRepository.state.value = MockServiceState.Disabled

        // 注册广播接收器
        registerReceiver(selectPointReceiver, IntentFilter(ACTION_SELECT_POINT),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        unregisterReceiver(selectPointReceiver)
        mockCtrl.stop()
        realCtrl.stop()
        notifyCtrl.stopForeground()
        providerMgr?.teardown()
        MockServiceStatusRepository.state.value = MockServiceState.Disabled
        super.onDestroy()
    }

    /**
     * 切换摇杆可见性
     * 
     * @param isVisible 新的可见性状态
     */
    private fun toggleJoystick(isVisible: Boolean) {
        // 只在模拟运行时才允许切换
        if (!mockCtrl.isRunning()) return
        
        // 应用新的可见性状态到 JoystickService
        overlayCtrl.setVisibility(isVisible)
    }
    
    inner class MockLocationServiceBinder : Binder() {
        fun locationFlow(): StateFlow<CandidateLocation> =
            locationStateHolder.state
        
        fun errorFlow(): StateFlow<ErrorInfo?> = _errorState
        
        fun clearError() {
            _errorState.value = null
        }

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        fun setStaticPoint(name: String, lat: Double, lng: Double, alt: Double) {
            // 尝试设置 TestProvider
            val setupSuccess = providerMgr?.setup() ?: false
            
            if (!setupSuccess) {
                // 设置失败，清理并保持在真实定位模式
                providerMgr?.teardown()
                // 确保真实位置监听正在运行
                realCtrl.start()
                // 更新通知显示为空闲状态
                notifyCtrl.updateMode(LocationMode.Idle)
                // 保持状态为已禁用
                MockServiceStatusRepository.state.value = MockServiceState.Disabled
                return
            }

            // 重置StateHolder状态
            OverlayStateHolder.reset()
            
            // 设置模拟器并启动模拟控制器（支持摇杆动态移动）
            mockCtrl.setSimulator(StaticPointSimulator(lat, lng, alt))
            mockCtrl.start()

            // 设置显示模式
            overlayCtrl.setMode(0)
            // 启动悬浮控件
            overlayCtrl.start()
            
            // 停止真实位置监听，重置卡尔曼滤波器
            realCtrl.stop()
            kf.reset()

            // 更新通知显示
            notifyCtrl.setOverlayVisibility(true)
            notifyCtrl.updateMode(
                LocationMode.Point(name, lat, lng)
            )

            // 更新状态为已启用
            MockServiceStatusRepository.state.value = MockServiceState.Enabled
        }

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        fun setDynamicRoute(
            route: RouteObject
        ) {
            // 尝试设置 TestProvider
            val setupSuccess = providerMgr?.setup() ?: false

            if (!setupSuccess) {
                // 设置失败，清理并保持在真实定位模式
                providerMgr?.teardown()
                // 确保真实位置监听正在运行
                realCtrl.start()
                // 更新通知显示为空闲状态
                notifyCtrl.updateMode(LocationMode.Idle)
                // 保持状态为已禁用
                MockServiceStatusRepository.state.value = MockServiceState.Disabled
                return
            }

            // 重置StateHolder状态
            OverlayStateHolder.reset()
            OverlayStateHolder.setRandomOffset(PrefsHelper.getRandomOffsetEnabled(this@LocationService))
            OverlayStateHolder.setMaxRandomOffset(PrefsHelper.getMaxRandomOffset(this@LocationService))

            // 设置模拟器并启动模拟控制器（支持摇杆动态移动）
            mockCtrl.setSimulator(DynamicRouteSimulator(route))
            mockCtrl.start()

            // 设置显示模式
            overlayCtrl.setMode(1)
            // 启动悬浮控件
            overlayCtrl.start()

            // 停止真实位置监听，重置卡尔曼滤波器
            realCtrl.stop()
            kf.reset()

            // 更新通知显示
            notifyCtrl.setOverlayVisibility(true)
            notifyCtrl.updateMode(
                LocationMode.Route(route.name, 0.0)
            )

            // 更新状态为已启用
            MockServiceStatusRepository.state.value = MockServiceState.Enabled
        }

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        fun stopSimulation() {
            // 重置摇杆状态
            OverlayStateHolder.reset()
            
            // 停止模拟控制器
            mockCtrl.stop()

            // 关闭悬浮控件
            overlayCtrl.stop()

            providerMgr?.teardown()
            
            // 重置位置状态，允许真实位置更新 UI
            locationStateHolder.stopMock()
            
            // 重启真实位置监听，重置卡尔曼滤波器
            realCtrl.start()
            kf.reset()

            // 更新通知显示
            notifyCtrl.setOverlayVisibility(false)
            notifyCtrl.updateMode(LocationMode.Idle)

            // 更新状态为已禁用
            MockServiceStatusRepository.state.value = MockServiceState.Disabled
        }
    }
}