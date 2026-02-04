package ink.moling.mocklocation.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.data.local.db.AppDatabase
import ink.moling.mocklocation.data.local.db.MockPointEntity
import ink.moling.mocklocation.data.local.db.MockRouteEntity
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.utils.logger.Logger
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

    private val mockRouteDao by lazy {
        AppDatabase
            .getInstance(getApplication())
            .mockRouteDao()
    }
    
    // =====================================================
    // 选中的 Mock 目标（UI Selection）
    // =====================================================
    
    var selectedMockPoint: MockPointEntity? = null
        set(value) {
            field = value
            PrefsHelper.setSelectedPointId(getApplication(), value?.id)
        }
    
    var selectedMockRoute: MockRouteEntity? = null
        set(value) {
            field = value
            PrefsHelper.setSelectedRouteId(getApplication(), value?.id)
        }
    
    init {
        // 从 SharedPreferences 恢复上次选择的模拟点 ID，然后从数据库查询
        val savedPointId = PrefsHelper.getSelectedPointId(getApplication())
        // 从 SharedPreferences 恢复上次选择的模拟路径
        val savedRouteId = PrefsHelper.getSelectedRouteId(getApplication())
        viewModelScope.launch {
            if (savedPointId != null)
                selectedMockPoint = mockPointDao.getById(savedPointId)
            if (savedRouteId != null)
                selectedMockRoute = mockRouteDao.getById(savedRouteId)
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

    private val _location = MutableStateFlow(CandidateLocation.Default)
    
    /**
     * 当前位置流，来自 MockLocationService
     * - 未模拟时：显示真实位置（GPS/Network 融合）
     * - 模拟时：显示模拟位置
     */
    val location: StateFlow<CandidateLocation> = _location.asStateFlow()

    // =====================================================
    // 4. Mock 行为（命令 Service）
    // =====================================================

    /**
     * 设置模拟位置（静态点）
     */
    @SuppressLint("MissingPermission")
    fun setMockPosition(lat: Double, lng: Double, alt: Double) {
        val binder = serviceBinder.value
        if (binder == null) {
            pendingAction = @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) {
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
    @SuppressLint("MissingPermission")
    fun stopMockPosition() {
        serviceBinder.value?.stopSimulation()
    }

    // =====================================================
    // 5. 本地路径点存储(旧)
    // =====================================================

//    private val _points = MutableStateFlow<List<MockPointEntity>>(emptyList())
//    val points: StateFlow<List<MockPointEntity>> = _points.asStateFlow()
//
//    fun loadPoints() {
//        viewModelScope.launch {
//            _points.value = mockPointDao.getAll()
//        }
//    }
//
//    fun addPoint(
//        name: String,
//        latitude: Double,
//        longitude: Double,
//        altitude: Double
//    ) {
//        viewModelScope.launch {
//            mockPointDao.insert(
//                MockPointEntity(
//                    name = name,
//                    lat = latitude,
//                    longitude = longitude,
//                    altitude = altitude
//                )
//            )
//            _points.value = mockPointDao.getAll()
//        }
//    }
//
//    fun deletePoint(id: Long) {
//        viewModelScope.launch {
//            mockPointDao.deleteById(id)
//            _points.value = mockPointDao.getAll()
//        }
//    }



    private val _savedPoints = MutableStateFlow<List<MockPointEntity>>(emptyList())
    val savedPoints: StateFlow<List<MockPointEntity>> = _savedPoints.asStateFlow()

    suspend fun getPoints() {
        _savedPoints.value = mockPointDao.getAll()
        Logger.d("getPoints", "Exists IDs: [%s]".format(
            _savedPoints.value.joinToString(separator = ",") { it.id.toString() }
        ))
    }

    fun addPoint(
        name: String,
        lat: Double,
        lng: Double,
        alt: Double
    ) {
        viewModelScope.launch {
            val insertedId = mockPointDao.insert(
                MockPointEntity(name = name, lat = lat, lng = lng, alt = alt)
            )
            PrefsHelper.setSelectedPointId(application, insertedId)
            getPoints()
        }
    }

    fun updatePoint(
        id: Long,
        name: String,
        lat: Double,
        lng: Double,
        alt: Double
    ) {
        viewModelScope.launch {
            val insertedId = mockPointDao.insert(
                MockPointEntity(id, name, lat, lng, alt)
            )
            PrefsHelper.setSelectedPointId(application, insertedId)
            getPoints()
        }
    }

    fun deletePoint(id: Long) {
        viewModelScope.launch {
            mockPointDao.deleteById(id)
        }
    }
}
