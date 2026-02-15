package ink.moling.mocklocation.activity.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.openlocationcode.OpenLocationCode
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.data.local.db.AppDatabase
import ink.moling.mocklocation.data.local.db.MockPointEntity
import ink.moling.mocklocation.data.local.db.MockRouteEntity
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.Source
import ink.moling.mocklocation.service.locationService.LocationService
import ink.moling.mocklocation.utils.MockMode
import ink.moling.mocklocation.utils.logger.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 错误信息数据类
 */
data class ErrorState(
    val title: String,
    val message: String,
    val stackTrace: String? = null
)

/**
 * MainScreen 的 UI 状态
 */
data class MainUiState(
    // 模拟模式：0=Point, 1=Route
    val selectedSimulation: Int = MockMode.MOCK_MODE_POINT,
    // 点编辑状态
    val editingSimPoint: Boolean = false,
    val isPointModified: Boolean = false,
    val isCreatingPoint: Boolean = false,
    // 当前选中点的详情
    val pointName: String = "<Unselected>",
    val pointLat: String = "0.000000",
    val pointLng: String = "0.000000",
    val pointAlt: String = "0.00",
    // 输入验证
    val pointLatError: Boolean = false,
    val pointLngError: Boolean = false,
    val pointAltError: Boolean = false,
    // 对话框
    val showDeleteConfirmDialog: Boolean = false,
    val showDeleteRouteConfirmDialog: Boolean = false,
    val errorState: ErrorState? = null,
    // 显示位置
    val displayedLocation: String = "@51.476900,0.000500#46.0",
    val displayedOpenCode: String = "9C3XFXGX+PQ",
    // 路线名称
    val routeName: String = "<Unselected>",
    val routeObject: RouteObject? = null
)

/**
 * 一次性 UI 事件
 */
sealed class MainUiEvent {
    data class ShowToast(val message: String) : MainUiEvent()
    object CollapseBottomSheet : MainUiEvent()
    object HideBottomSheet : MainUiEvent()
}

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
    
    // 选中的路线
    var selectedMockRoute: MockRouteEntity? = null
        set(value) {
            field = value
            PrefsHelper.setSelectedRouteId(getApplication(), value?.id)
        }
    
    // =====================================================
    // UI 状态（需要在 init 之前声明）
    // =====================================================
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<MainUiEvent>(extraBufferCapacity = 1)
    val uiEvent = _uiEvent.asSharedFlow()
    
    // 缓存编辑前的点信息（用于取消编辑时恢复）
    private var cachedPointName: String = "<Unselected>"
    private var cachedPointLat: Double = 0.0
    private var cachedPointLng: Double = 0.0
    private var cachedPointAlt: Double = 0.0
    
    // 位置数据流（需要在 init 之前声明，因为 init 中会 collect）
    private val _location = MutableStateFlow(CandidateLocation.Default)
    
    /**
     * 当前位置流，来自 MockLocationService
     * - 未模拟时：显示真实位置（GPS/Network 融合）
     * - 模拟时：显示模拟位置
     */
    val location: StateFlow<CandidateLocation> = _location.asStateFlow()
    
    // 保存的点列表（需要在 init 之前声明，因为 init 中会调用 getPoints）
    private val _savedPoints = MutableStateFlow<List<MockPointEntity>>(emptyList())
    val savedPoints: StateFlow<List<MockPointEntity>> = _savedPoints.asStateFlow()
    
    // 保存的路线列表
    private val _savedRoutes = MutableStateFlow<List<MockRouteEntity>>(emptyList())
    val savedRoutes: StateFlow<List<MockRouteEntity>> = _savedRoutes.asStateFlow()
    
    init {
        // 从 SharedPreferences 恢复模拟模式
        val savedSimulationMode = PrefsHelper.getMockMode(getApplication())
        _uiState.update { it.copy(selectedSimulation = savedSimulationMode) }
        
        // 从 SharedPreferences 恢复上次选择的模拟点 ID，然后从数据库查询
        val savedPointId = PrefsHelper.getSelectedPointId(getApplication())
        // 从 SharedPreferences 恢复上次选择的模拟路径
        val savedRouteId = PrefsHelper.getSelectedRouteId(getApplication())
        viewModelScope.launch {
            // 加载保存的点列表
            getPoints()
            // 加载保存的路线列表
            getRoutes()
            
            if (savedPointId != null) {
                selectedMockPoint = mockPointDao.getById(savedPointId)
                // 更新 UI 状态中的点信息
                selectedMockPoint?.let { point ->
                    _uiState.update { state ->
                        state.copy(
                            pointName = point.name,
                            pointLat = "%.6f".format(point.lat),
                            pointLng = "%.6f".format(point.lng),
                            pointAlt = "%.2f".format(point.alt)
                        )
                    }
                }
            }
            
            if (savedRouteId != null) {
                val route = mockRouteDao.getById(savedRouteId)
                selectedMockRoute = route
                // 更新 UI 状态中的路线名称
                route?.let {
                    _uiState.update { state ->
                        state.copy(
                            routeName = it.name,
                            routeObject = it.details
                        )
                    }
                }
            }
        }
        
        // 监听位置变化，更新显示的位置信息
        viewModelScope.launch {
            _location.collect { loc ->
                if (loc.source != Source.DEFAULT) {
                    val lat = loc.lat
                    val lng = loc.lng
                    val alt = loc.alt ?: 0.0
                    _uiState.update { state ->
                        state.copy(
                            displayedLocation = "@%.6f,%.6f#%.2f".format(lat, lng, alt),
                            displayedOpenCode = OpenLocationCode.encode(lat, lng, 11)
                        )
                    }
                    // Logger.d("MainViewModel", "Displayed: Lat=$lat, Lng=$lng")
                }
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
        
        // 订阅错误流并更新到 UI State
        viewModelScope.launch {
            binder.errorFlow().collect { error ->
                _errorInfo.value = error
                if (error != null) {
                    showError(
                        title = error.title,
                        message = error.message,
                        stackTrace = error.stackTrace
                    )
                }
            }
        }

        pendingAction?.invoke()
        pendingAction = null
    }

    // =====================================================
    // 2. 错误状态
    // =====================================================
    
    private val _errorInfo = MutableStateFlow<LocationService.ErrorInfo?>(null)
    val errorInfo = _errorInfo.asStateFlow()
    
    /**
     * 显示错误对话框
     * 
     * @param title 错误标题
     * @param message 错误消息
     * @param stackTrace 堆栈跟踪（可选）
     */
    fun showError(title: String, message: String, stackTrace: String? = null) {
        _uiState.update { 
            it.copy(errorState = ErrorState(title, message, stackTrace))
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.update { it.copy(errorState = null) }
        _errorInfo.value = null
        serviceBinder.value?.clearError()
    }

    // =====================================================
    // 3. Mock 行为（命令 Service）
    // =====================================================

    /**
     * 设置模拟位置（静态点）
     */
    @SuppressLint("MissingPermission")
    fun setMockLocation(lat: Double, lng: Double, alt: Double) {
        val binder = serviceBinder.value
        if (binder == null) {
            pendingAction = @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) {
                serviceBinder.value?.setStaticPoint(lat, lng, alt)
            }
            _needStartService.tryEmit(Unit)
            return
        }
        binder.setStaticPoint(lat, lng, alt)
    }

    fun setMockLocation(route: RouteObject) {
        val binder = serviceBinder.value
        if (binder == null) {
            serviceBinder.value?.setDynamicRoute(route)
            _needStartService.tryEmit(Unit)
            return
        }
        binder.setDynamicRoute(route)
    }

    /**
     * 停止模拟位置，恢复真实位置
     */
    @SuppressLint("MissingPermission")
    fun stopMockLocation() {
        serviceBinder.value?.stopSimulation()
    }

    // =====================================================
    // 4. 本地路径点存储
    // =====================================================

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
            selectedMockPoint = mockPointDao.getById(insertedId)
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
            selectedMockPoint = mockPointDao.getById(insertedId)
            getPoints()
        }
    }

    fun deletePoint(id: Long) {
        viewModelScope.launch {
            mockPointDao.deleteById(id)
        }
    }
    
    // =====================================================
    // 5. 本地路线存储
    // =====================================================
    
    /**
     * 获取所有保存的路线
     */
    suspend fun getRoutes() {
        _savedRoutes.value = mockRouteDao.getAll()
        Logger.d("getRoutes", "Exists IDs: [%s]".format(
            _savedRoutes.value.joinToString(separator = ",") { it.id.toString() }
        ))
    }
    
    /**
     * 添加新路线
     * 
     * @param name 路线名称
     * @param details 路线详情（RouteObject）
     */
    fun addRoute(name: String, details: RouteObject) {
        viewModelScope.launch {
            mockRouteDao.insert(
                MockRouteEntity(name = name, details = details)
            )
            getRoutes()
            // 查找新插入的路线（通过名称，因为 insert 没有返回 ID）
            val insertedRoute = _savedRoutes.value.lastOrNull { it.name == name }
            insertedRoute?.let {
                selectedMockRoute = it
                _uiState.update { state ->
                    state.copy(
                        routeName = it.name,
                        routeObject = it.details
                    )
                }
            }
        }
    }
    
    /**
     * 更新现有路线
     * 
     * @param id 路线 ID
     * @param name 路线名称
     * @param details 路线详情（RouteObject）
     */
    fun updateRoute(id: Long, name: String, details: RouteObject) {
        viewModelScope.launch {
            mockRouteDao.insert(
                MockRouteEntity(id = id, name = name, details = details)
            )
            val route = mockRouteDao.getById(id)
            selectedMockRoute = route
            getRoutes()
            _uiState.update { state ->
                state.copy(
                    routeName = name,
                    routeObject = null
                )
            }
        }
    }
    
    /**
     * 删除路线
     * 
     * @param id 路线 ID
     */
    fun deleteRoute(id: Long) {
        viewModelScope.launch {
            mockRouteDao.deleteById(id)
            getRoutes()
            // 如果删除的是当前选中的路线，清除选中状态
            if (selectedMockRoute?.id == id) {
                selectedMockRoute = null
                    _uiState.update { state ->
                    state.copy(
                        routeName = "<Unselected>",
                        routeObject = null
                    )
                }
            }
        }
    }
    
    /**
     * 选择一个保存的路线
     * 
     * @param id 路线 ID
     */
    fun selectRoute(id: Long) {
        val route = _savedRoutes.value.find { it.id == id } ?: return
        selectedMockRoute = route

        _uiState.update { state ->
            state.copy(
                routeName = route.name,
                routeObject = route.details
            )
        }
        _uiEvent.tryEmit(MainUiEvent.HideBottomSheet)
    }

    /**
     * 刷新当前路线数据
     */
    fun refreshRoute() {
        // 从 SharedPreferences 恢复上次选择的模拟路径
        val savedRouteId = PrefsHelper.getSelectedRouteId(getApplication())
        viewModelScope.launch {
            // 加载保存的路线列表
            getRoutes()

            if (savedRouteId != null) {
                val route = mockRouteDao.getById(savedRouteId)
                selectedMockRoute = route
                // 更新 UI 状态中的路线名称
                route?.let {
                    _uiState.update { state ->
                        state.copy(
                            routeName = it.name,
                            routeObject = it.details
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 按名称搜索路线
     * 
     * @param name 搜索关键词
     * @return 匹配的路线列表
     */
    suspend fun searchRoutesByName(name: String): List<MockRouteEntity> {
        return mockRouteDao.searchByName(name)
    }
    
    /**
     * 清除所有路线
     */
    fun clearAllRoutes() {
        viewModelScope.launch {
            mockRouteDao.clearAll()
            getRoutes()
            selectedMockRoute = null
            _uiState.update { state ->
                state.copy(routeName = "<Unselected>")
            }
        }
    }
    
    /**
     * 检查是否有选中的路线
     */
    fun hasSelectedRoute(): Boolean {
        val selectedId = PrefsHelper.getSelectedRouteId(getApplication())
        return selectedId != null && selectedId != -1L
    }
    
    // =====================================================
    // 6. UI 交互方法（MainScreen 调用）
    // =====================================================
    
    /**
     * 设置模拟模式：0=Point, 1=Route
     */
    fun setSimulationMode(mode: Int) {
        PrefsHelper.setMockMode(getApplication(), mode)
        _uiState.update { it.copy(selectedSimulation = mode) }
    }
    
    /**
     * 开始编辑当前选中的点
     */
    fun startEditPoint() {
        val selectedId = PrefsHelper.getSelectedPointId(getApplication())
        if (selectedId == null || selectedId == -1L) {
            _uiEvent.tryEmit(MainUiEvent.ShowToast("Please select a point"))
            return
        }
        // 缓存当前点信息
        val state = _uiState.value
        cachedPointName = state.pointName
        cachedPointLat = state.pointLat.toDoubleOrNull() ?: 0.0
        cachedPointLng = state.pointLng.toDoubleOrNull() ?: 0.0
        cachedPointAlt = state.pointAlt.toDoubleOrNull() ?: 0.0
        // 重置验证状态
        _uiState.update { 
            it.copy(
                editingSimPoint = true,
                isPointModified = false,
                pointLatError = false,
                pointLngError = false,
                pointAltError = false
            ) 
        }
    }
    
    /**
     * 开始创建新点
     */
    fun startCreatePoint() {
        // 缓存当前点信息
        val state = _uiState.value
        cachedPointName = state.pointName
        cachedPointLat = state.pointLat.toDoubleOrNull() ?: 0.0
        cachedPointLng = state.pointLng.toDoubleOrNull() ?: 0.0
        cachedPointAlt = state.pointAlt.toDoubleOrNull() ?: 0.0
        // 清空输入框，进入创建模式
        _uiState.update { 
            it.copy(
                isCreatingPoint = true,
                isPointModified = true,
                editingSimPoint = true,
                pointName = "",
                pointLat = "",
                pointLng = "",
                pointAlt = ""
            ) 
        }
        _uiEvent.tryEmit(MainUiEvent.HideBottomSheet)
    }
    
    /**
     * 保存点（创建或更新）
     */
    fun savePoint() {
        val state = _uiState.value
        
        // 验证输入
        val lat = state.pointLat.toDoubleOrNull()
        val lng = state.pointLng.toDoubleOrNull()
        val alt = state.pointAlt.toDoubleOrNull()
        
        val latValid = lat != null && lat >= -90.0 && lat <= 90.0
        val lngValid = lng != null && lng >= -180.0 && lng <= 180.0
        val altValid = alt != null && alt >= -1000.0 && alt <= 100000.0
        
        _uiState.update { 
            it.copy(
                pointLatError = !latValid,
                pointLngError = !lngValid,
                pointAltError = !altValid
            ) 
        }
        
        if (!latValid || !lngValid || !altValid) {
            return
        }
        
        // 标准化格式
        val formattedLat = "%.6f".format(lat)
        val formattedLng = "%.6f".format(lng)
        val formattedAlt = "%.2f".format(alt)
        
        if (state.isCreatingPoint) {
            // 创建新点
            viewModelScope.launch {
                val insertedId = mockPointDao.insert(
                    MockPointEntity(name = state.pointName, lat = lat, lng = lng, alt = alt)
                )
                selectedMockPoint = mockPointDao.getById(insertedId)
                PrefsHelper.setSelectedPointId(getApplication(), insertedId)
                getPoints()
                
                _uiState.update { 
                    it.copy(
                        editingSimPoint = false,
                        isPointModified = false,
                        isCreatingPoint = false,
                        pointLat = formattedLat,
                        pointLng = formattedLng,
                        pointAlt = formattedAlt
                    ) 
                }
            }
        } else {
            // 更新现有点
            val selectedId = PrefsHelper.getSelectedPointId(getApplication()) ?: return
            viewModelScope.launch {
                mockPointDao.insert(
                    MockPointEntity(selectedId, state.pointName, lat, lng, alt)
                )
                selectedMockPoint = mockPointDao.getById(selectedId)
                getPoints()
                
                _uiState.update { 
                    it.copy(
                        editingSimPoint = false,
                        isPointModified = false,
                        pointLat = formattedLat,
                        pointLng = formattedLng,
                        pointAlt = formattedAlt
                    ) 
                }
            }
        }
    }
    
    /**
     * 取消编辑，恢复缓存的值
     */
    fun cancelEditPoint() {
        _uiState.update { 
            it.copy(
                editingSimPoint = false,
                isPointModified = false,
                isCreatingPoint = false,
                pointName = cachedPointName,
                pointLat = "%.6f".format(cachedPointLat),
                pointLng = "%.6f".format(cachedPointLng),
                pointAlt = "%.2f".format(cachedPointAlt),
                pointLatError = false,
                pointLngError = false,
                pointAltError = false
            ) 
        }
    }
    
    /**
     * 显示删除点确认对话框
     */
    fun showDeletePointConfirmDialog() {
        val state = _uiState.value
        if (state.isCreatingPoint) {
            // 创建模式下，取消创建
            cancelEditPoint()
        } else {
            val selectedId = PrefsHelper.getSelectedPointId(getApplication())
            if (selectedId != null && selectedId != -1L) {
                _uiState.update { it.copy(showDeleteConfirmDialog = true) }
            }
        }
    }
    
    /**
     * 关闭删除点确认对话框
     */
    fun dismissDeletePointDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }
    
    /**
     * 确认删除点
     */
    fun confirmDeletePoint() {
        val selectedId = PrefsHelper.getSelectedPointId(getApplication()) ?: return
        viewModelScope.launch {
            mockPointDao.deleteById(selectedId)
            PrefsHelper.setSelectedPointId(getApplication(), -1L)
            selectedMockPoint = null
            getPoints()
            
            _uiState.update { 
                it.copy(
                    showDeleteConfirmDialog = false,
                    editingSimPoint = false,
                    pointName = "<Unselected>",
                    pointLat = "0.000000",
                    pointLng = "0.000000",
                    pointAlt = "0.00"
                ) 
            }
        }
    }
    
    /**
     * 显示删除路线确认对话框
     */
    fun showDeleteRouteConfirmDialog() {
        val selectedId = PrefsHelper.getSelectedRouteId(getApplication())
        if (selectedId != null && selectedId != -1L) {
            _uiState.update { it.copy(showDeleteRouteConfirmDialog = true) }
        } else {
            _uiEvent.tryEmit(MainUiEvent.ShowToast("Please select a route"))
        }
    }
    
    /**
     * 关闭删除路线确认对话框
     */
    fun dismissDeleteRouteDialog() {
        _uiState.update { it.copy(showDeleteRouteConfirmDialog = false) }
    }
    
    /**
     * 确认删除路线
     */
    fun confirmDeleteRoute() {
        val selectedId = PrefsHelper.getSelectedRouteId(getApplication()) ?: return
        viewModelScope.launch {
            mockRouteDao.deleteById(selectedId)
            selectedMockRoute = null
            getRoutes()
            
            _uiState.update { 
                it.copy(
                    showDeleteRouteConfirmDialog = false,
                    routeName = "<Unselected>",
                    routeObject = null
                ) 
            }
        }
    }
    
    /**
     * 选择一个保存的点
     */
    fun selectPoint(id: Long) {
        val point = _savedPoints.value.find { it.id == id } ?: return
        PrefsHelper.setSelectedPointId(getApplication(), id)
        selectedMockPoint = point
        
        _uiState.update { 
            it.copy(
                pointName = point.name,
                pointLat = "%.6f".format(point.lat),
                pointLng = "%.6f".format(point.lng),
                pointAlt = "%.2f".format(point.alt)
            ) 
        }
        _uiEvent.tryEmit(MainUiEvent.HideBottomSheet)
    }
    
    /**
     * 更新点名称
     */
    fun updatePointName(value: String) {
        _uiState.update { 
            it.copy(
                pointName = value,
                isPointModified = true
            ) 
        }
    }
    
    /**
     * 更新纬度
     */
    fun updatePointLat(value: String) {
        _uiState.update { 
            it.copy(
                pointLat = value,
                isPointModified = true,
                pointLatError = false
            ) 
        }
    }
    
    /**
     * 更新经度
     */
    fun updatePointLng(value: String) {
        _uiState.update { 
            it.copy(
                pointLng = value,
                isPointModified = true,
                pointLngError = false
            ) 
        }
    }
    
    /**
     * 更新海拔
     */
    fun updatePointAlt(value: String) {
        _uiState.update { 
            it.copy(
                pointAlt = value,
                isPointModified = true,
                pointAltError = false
            ) 
        }
    }
    
    /**
     * 用当前位置填充输入框
     */
    fun fillCurrentLocation() {
        val loc = _location.value
        _uiState.update { 
            it.copy(
                pointLat = "${loc.lat}",
                pointLng = "${loc.lng}",
                pointAlt = "${loc.alt ?: 0.0}",
                isPointModified = true
            ) 
        }
    }
    
    /**
     * 检查是否有选中的点
     */
    fun hasSelectedPoint(): Boolean {
        val selectedId = PrefsHelper.getSelectedPointId(getApplication())
        return selectedId != null && selectedId != -1L
    }
    
    /**
     * 展开 BottomSheet
     */
    fun expandBottomSheet() {
        _uiEvent.tryEmit(MainUiEvent.CollapseBottomSheet)
    }
}
