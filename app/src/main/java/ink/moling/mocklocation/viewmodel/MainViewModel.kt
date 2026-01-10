package ink.moling.mocklocation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ink.moling.mocklocation.data.db.AppDatabase
import ink.moling.mocklocation.data.db.MockPointEntity
import ink.moling.mocklocation.data.repository.MockServiceStatusRepository
import ink.moling.mocklocation.service.MockLocationService
import ink.moling.mocklocation.data.models.RouteItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * MainViewModel 管理 MockLocation 应用的 UI 状态
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Mock 服务状态
    val mockStatus = MockServiceStatusRepository.state

    // MockService 启动参数
    var selectedMockPoint: MockPointEntity? = null
    var selectedMockRoute: RouteItem? = null

    // 实时 GPS 数据
    private val _gpsLatitude = MutableStateFlow(0.0)
    val gpsLatitude: StateFlow<Double> = _gpsLatitude.asStateFlow()

    private val _gpsLongitude = MutableStateFlow(0.0)
    val gpsLongitude: StateFlow<Double> = _gpsLongitude.asStateFlow()

    private val _gpsAltitude = MutableStateFlow(0.0)
    val gpsAltitude: StateFlow<Double> = _gpsAltitude.asStateFlow()

    private val _gpsProvider = MutableStateFlow("")
    val gpsProvider: StateFlow<String> = _gpsProvider.asStateFlow()

    // 导入导出路径 Dialog 状态
    private val _isImportExportDialogOpen = MutableStateFlow(false)
    val isImportExportDialogOpen: StateFlow<Boolean> = _isImportExportDialogOpen.asStateFlow()

    // 添加点 Dialog 状态
    private val _isAddPointDialogOpen = MutableStateFlow(false)
    val isAddPointDialogOpen: StateFlow<Boolean> = _isAddPointDialogOpen.asStateFlow()

    // Service Binder（由 Activity 注入）
    val serviceBinder =
        MutableStateFlow<MockLocationService.MockLocationServiceBinder?>(null)

    // 一次性事件：请求启动 Service
    private val _needStartService = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val needStartService = _needStartService.asSharedFlow()

    // 暂存“待执行的操作”
    private var pendingAction: (() -> Unit)? = null

    fun onServiceBinderReady(
        binder: MockLocationService.MockLocationServiceBinder
    ) {
        serviceBinder.value = binder

        // 自动执行之前没法执行的操作
        pendingAction?.invoke()
        pendingAction = null
    }

    /**
     * 更新 GPS 位置数据
     */
    fun updateGpsLocation(latitude: Double, longitude: Double, altitude: Double, provider: String) {
        _gpsLatitude.value = latitude
        _gpsLongitude.value = longitude
        _gpsAltitude.value = altitude
        _gpsProvider.value = provider
    }

    /**
     * 打开/关闭导入导出对话框
     */
    fun setImportExportDialogOpen(isOpen: Boolean) {
        _isImportExportDialogOpen.value = isOpen
    }

    /**
     * 打开/关闭添加点对话框
     */
    fun setAddPointDialogOpen(isOpen: Boolean) {
        _isAddPointDialogOpen.value = isOpen
    }

    /**
     * 设置模拟位置（通过服务）
     */
    fun setMockPosition(lat: Double, lng: Double, alt: Double) {
        val binder = serviceBinder.value
        if (binder == null) {
            // Service 还没启动 → 记录操作
            pendingAction = {
                serviceBinder.value?.setStaticPoint(lat, lng, alt)
            }
            _needStartService.tryEmit(Unit)
            return
        }

        // Service 已就绪 → 直接执行
        binder.setStaticPoint(lat, lng, alt)
    }



    // ================= 路径点本地存储 =================

    private val mockPointDao by lazy {
        AppDatabase
            .getInstance(getApplication())
            .mockPointDao()
    }

    private val _points = MutableStateFlow<List<MockPointEntity>>(emptyList())
    val points: StateFlow<List<MockPointEntity>> = _points.asStateFlow()

    /**
     * 加载所有点（页面初始化时调用一次）
     */
    fun loadPoints() {
        viewModelScope.launch {
            _points.value = mockPointDao.getAll()
        }
    }

    /**
     * 添加一个点
     */
    fun addPoint(name: String, latitude: Double, longitude: Double, altitude: Double) {
        viewModelScope.launch {
            mockPointDao.insert(
                MockPointEntity(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude
                )
            )
            _points.value = mockPointDao.getAll()
        }
    }

    /**
     * 删除单个点（按 id）
     */
    fun deletePoint(id: Long) {
        viewModelScope.launch {
            mockPointDao.deleteById(id)
            _points.value = mockPointDao.getAll()
        }
    }

    /**
     * 清空所有点
     */
    fun clearAllPoints() {
        viewModelScope.launch {
            mockPointDao.clearAll()
            _points.value = emptyList()
        }
    }

    // ==================================================
}