package ink.moling.mocklocation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ink.moling.mocklocation.data.local.db.AppDatabase
import ink.moling.mocklocation.data.local.db.MockPointEntity
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.RouteItem
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.data.local.PrefsHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // =====================================================
    // 数据库访问
    // =====================================================
    
    private val mockPointDao by lazy {
        AppDatabase
            .getInstance(getApplication())
            .mockPointDao()
    }
    
    // =====================================================
    // 选中的 Mock 目标（UI Selection）
    // =====================================================
    
    var selectedMockPoint: MockPointEntity? = null
        set(value) {
            field = value
            PrefsHelper.setSelectedPointId(getApplication(), value?.id)
        }
    
    var selectedMockRoute: RouteItem? = null
        set(value) {
            field = value
            PrefsHelper.setSelectedRoute(getApplication(), value)
        }
    
    init {
        // 从 SharedPreferences 恢复上次选择的模拟路径
        selectedMockRoute = PrefsHelper.getSelectedRoute(getApplication())
        
        // 从 SharedPreferences 恢复上次选择的模拟点 ID，然后从数据库查询
        val savedPointId = PrefsHelper.getSelectedPointId(getApplication())
        if (savedPointId != null) {
            viewModelScope.launch {
                selectedMockPoint = mockPointDao.getById(savedPointId)
            }
        }
    }

    // =====================================================
    // 1. Mock Service / 生命周期相关
    // =====================================================

    val mockStatus = MockServiceStatusRepository.state

    val serviceBinder =
        MutableStateFlow<LocationService.MockLocationServiceBinder?>(null)

    private val _needStartService = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val needStartService = _needStartService.asSharedFlow()

    private var pendingAction: (() -> Unit)? = null

    fun onServiceBinderReady(
        binder: LocationService.MockLocationServiceBinder
    ) {
        serviceBinder.value = binder

        // 订阅位置流
        viewModelScope.launch {
            binder.locationFlow().collect { location ->
                _location.value = location
            }
        }
        
        // 订阅错误流
        viewModelScope.launch {
            binder.errorFlow().collect { error ->
                _errorInfo.value = error
            }
        }

        pendingAction?.invoke()
        pendingAction = null
    }

    // =====================================================
    // 2. UI 状态（Dialog / 页面）
    // =====================================================

    private val _isImportExportDialogOpen = MutableStateFlow(false)
    val isImportExportDialogOpen = _isImportExportDialogOpen.asStateFlow()

    private val _isAddPointDialogOpen = MutableStateFlow(false)
    val isAddPointDialogOpen = _isAddPointDialogOpen.asStateFlow()
    
    private val _errorInfo = MutableStateFlow<LocationService.ErrorInfo?>(null)
    val errorInfo = _errorInfo.asStateFlow()

    fun setImportExportDialogOpen(open: Boolean) {
        _isImportExportDialogOpen.value = open
    }

    fun setAddPointDialogOpen(open: Boolean) {
        _isAddPointDialogOpen.value = open
    }
    
    fun clearError() {
        _errorInfo.value = null
        serviceBinder.value?.clearError()
    }

    // =====================================================
    // 3. 位置数据流（来自 MockLocationService）
    // =====================================================

    private val _location = MutableStateFlow<CandidateLocation?>(null)
    
    /**
     * 当前位置流，来自 MockLocationService
     * - 未模拟时：显示真实位置（GPS/Network 融合）
     * - 模拟时：显示模拟位置
     */
    val location: StateFlow<CandidateLocation?> = _location.asStateFlow()

    // =====================================================
    // 4. Mock 行为（命令 Service）
    // =====================================================

    /**
     * 设置模拟位置（静态点）
     */
    fun setMockPosition(lat: Double, lng: Double, alt: Double) {
        val binder = serviceBinder.value
        if (binder == null) {
            pendingAction = {
                serviceBinder.value?.setStaticPoint(lat, lng, alt)
            }
            _needStartService.tryEmit(Unit)
            return
        }
        binder.setStaticPoint(lat, lng, alt)
    }

    /**
     * 停止模拟位置，恢复真实位置
     */
    fun stopMockPosition() {
        serviceBinder.value?.stopSimulation()
    }

    // =====================================================
    // 5. 本地路径点存储
    // =====================================================

    private val _points = MutableStateFlow<List<MockPointEntity>>(emptyList())
    val points: StateFlow<List<MockPointEntity>> = _points.asStateFlow()

    fun loadPoints() {
        viewModelScope.launch {
            _points.value = mockPointDao.getAll()
        }
    }

    fun addPoint(
        name: String,
        latitude: Double,
        longitude: Double,
        altitude: Double
    ) {
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

    fun deletePoint(id: Long) {
        viewModelScope.launch {
            mockPointDao.deleteById(id)
            _points.value = mockPointDao.getAll()
        }
    }

//    fun clearAllPoints() {
//        viewModelScope.launch {
//            mockPointDao.clearAll()
//            _points.value = emptyList()
//        }
//    }
}
