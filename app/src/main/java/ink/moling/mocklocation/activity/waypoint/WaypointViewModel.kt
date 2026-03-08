package ink.moling.mocklocation.activity.waypoint

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.db.AppDatabase
import ink.moling.mocklocation.data.local.db.MockRouteEntity
import ink.moling.mocklocation.data.models.PointType
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.RoutePoint
import ink.moling.mocklocation.data.models.RouteType
import ink.moling.mocklocation.utils.extensions.addConn
import ink.moling.mocklocation.utils.extensions.hasCycle
import ink.moling.mocklocation.utils.extensions.removeConn
import ink.moling.mocklocation.utils.logger.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Waypoint 编辑器的 UI 状态
 */
data class WaypointUiState(
    // 路线基本信息
    val routeName: String = "<NEW_ROUTE>",
    val isWaypointMap: Boolean = false,
    val isNewRoute: Boolean = true,
    val routeId: Long? = null,

    var routeObject: RouteObject = RouteObject.Empty,
    
    // 编辑状态
    val isModified: Boolean = false,
    val selectedWaypointIndex: Int = -1,
    
    // UI 控制
    val selectedDisplayMode: Int = 0,  // 0=Route, 1=Map
    val selectedSheetDetail: Int = 0,  // 0=Waypoints, 1=Details
    
    // 错误状态
    val errorMessage: String? = null
)

/**
 * 一次性 UI 事件
 */
sealed class WaypointUiEvent {
    data class ShowToast(val message: String) : WaypointUiEvent()
    object SaveSuccess : WaypointUiEvent()
    object ClosActivity : WaypointUiEvent()
    object ShowUnsavedChangesDialog : WaypointUiEvent()
}

class WaypointViewModel(application: Application) : AndroidViewModel(application) {
    
    // =====================================================
    // 数据库访问
    // =====================================================
    
    private val mockRouteDao by lazy {
        AppDatabase
            .getInstance(getApplication())
            .mockRouteDao()
    }
    
    // =====================================================
    // UI 状态
    // =====================================================
    
    private val _uiState = MutableStateFlow(WaypointUiState())
    val uiState: StateFlow<WaypointUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<WaypointUiEvent>(extraBufferCapacity = 64)
    val uiEvent = _uiEvent.asSharedFlow()
    
    // =====================================================
    // 初始化
    // =====================================================
    
    /**
     * 加载现有路线进行编辑
     * 
     * @param routeName 路线名称
     */
    fun loadRoute(routeName: String) {
        if (routeName == "<NEW_ROUTE>" || routeName.isEmpty()) {
            // 创建新路线模式
            _uiState.update { 
                it.copy(
                    routeName = "",
                    isNewRoute = true,
                    routeObject = RouteObject.Empty
                ) 
            }
            return
        }
        
        viewModelScope.launch {
            try {
                // 通过名称搜索路线
                val routes = mockRouteDao.searchByName(routeName)
                val route = routes.firstOrNull { it.name == routeName }
                
                if (route != null) {
                    _uiState.update { 
                        it.copy(
                            routeName = route.name,
                            isNewRoute = false,
                            routeId = route.id,
                            routeObject = route.details,
                            isWaypointMap = route.details.meta.type == RouteType.WAYPOINTS
                        ) 
                    }
                    Logger.d("WaypointViewModel", "Loaded route: ${route.name}, points: ${route.details.points.size}")
                } else {
                    // 路线不存在，创建新路线
                    _uiState.update { 
                        it.copy(
                            routeName = routeName,
                            isNewRoute = true,
                            routeObject = RouteObject.Empty
                        ) 
                    }
                }
            } catch (e: Exception) {
                Logger.e("WaypointViewModel", "Failed to load route", e)
                _uiState.update { it.copy(errorMessage = "Failed to load route: ${e.message}") }
            }
        }
    }
    
    /**
     * 通过 ID 加载路线
     * 
     * @param routeId 路线 ID
     */
    fun loadRouteById(routeId: Long) {
        viewModelScope.launch {
            try {
                val route = mockRouteDao.getById(routeId)
                if (route != null) {
                    _uiState.update { 
                        it.copy(
                            routeName = route.name,
                            isNewRoute = false,
                            routeId = route.id,
                            routeObject = route.details,
                            isWaypointMap = route.details.meta.type == RouteType.WAYPOINTS
                        ) 
                    }
                    Logger.d("WaypointViewModel", "Loaded route by ID: ${route.name}")
                } else {
                    _uiState.update { it.copy(errorMessage = "Route not found") }
                }
            } catch (e: Exception) {
                Logger.e("WaypointViewModel", "Failed to load route by ID", e)
                _uiState.update { it.copy(errorMessage = "Failed to load route: ${e.message}") }
            }
        }
    }
    
    // =====================================================
    // 路线操作
    // =====================================================
    
    /**
     * 保存路线
     */
    fun saveRoute() {
        val state = _uiState.value
        
        // 验证路线名称
        if (state.routeName.isBlank()) {
            _uiEvent.tryEmit(WaypointUiEvent.ShowToast(getApplication<Application>().getString(R.string.waypoint_toast_enter_route_name)))
            return
        }
        
        // 验证路点
        if (state.routeObject.points.isEmpty()) {
            _uiEvent.tryEmit(WaypointUiEvent.ShowToast(getApplication<Application>().getString(R.string.waypoint_toast_add_waypoint)))
            return
        }

        // 验证路径中不存在环
        if (state.routeObject.hasCycle()) {
            _uiEvent.tryEmit(WaypointUiEvent.ShowToast(getApplication<Application>().getString(R.string.waypoint_toast_cycle_detected)))
            return
        }
        
        viewModelScope.launch {
            try {
                val routeObject = state.routeObject.copy(
                    name = state.routeName,
                    meta = state.routeObject.meta.copy(
                        type = if (state.isWaypointMap) RouteType.WAYPOINTS else RouteType.ROUTE
                    )
                )
                
                if (state.isNewRoute) {
                    // 创建新路线
                    mockRouteDao.insert(
                        MockRouteEntity(
                            name = state.routeName,
                            details = routeObject
                        )
                    )
                    Logger.d("WaypointViewModel", "Created new route: ${state.routeName}")
                } else {
                    // 更新现有路线
                    mockRouteDao.insert(
                        MockRouteEntity(
                            id = state.routeId ?: 0,
                            name = state.routeName,
                            details = routeObject
                        )
                    )
                    Logger.d("WaypointViewModel", "Updated route: ${state.routeName}")
                }
                
                _uiState.update { it.copy(isModified = false) }
                _uiEvent.tryEmit(WaypointUiEvent.SaveSuccess)
                _uiEvent.tryEmit(WaypointUiEvent.ShowToast(getApplication<Application>().getString(R.string.waypoint_toast_save_success)))
            } catch (e: Exception) {
                Logger.e("WaypointViewModel", "Failed to save route", e)
                _uiEvent.tryEmit(WaypointUiEvent.ShowToast(getApplication<Application>().getString(R.string.waypoint_toast_save_failed, e.message ?: "")))
            }
        }
    }
    
    /**
     * 更新路线名称
     * 
     * @param name 新名称
     */
    fun updateRouteName(name: String) {
        _uiState.update { 
            it.copy(
                routeName = name,
                isModified = true
            ) 
        }
    }
    
    /**
     * 切换地图模式
     * 
     * @param isMapMode 是否为地图模式
     */
    fun setMapMode(isMapMode: Boolean) {
        _uiState.update { 
            it.copy(
                isWaypointMap = isMapMode,
                isModified = true
            ) 
        }
    }
    
    // =====================================================
    // 路点操作
    // =====================================================
    
    /**
     * 添加新路点
     * 
     * @param lat 纬度
     * @param lng 经度
     * @param type 路点类型
     * @param connects 连接的路点 ID 列表
     */
    fun addWaypoint(
        lat: Double,
        lng: Double,
        type: PointType = PointType.R,
        connects: Set<Int> = emptySet()
    ): Int {
        val state = _uiState.value

        Logger.d("WaypointViewModel", "Exists points: [%s]".format(
            state.routeObject.points.map { it.id }.joinToString(",")
        ))

        val newId = state.routeObject.points.maxOfOrNull { it.id }?.let { it + 1 } ?: 0

        val newPoint = RoutePoint(
            id = newId,
            lat = lat,
            lng = lng,
            type = type,
            connects = connects
        )
        
        _uiState.update { 
            it.copy(
                routeObject = state.routeObject.copy(points = state.routeObject.points + newPoint),
                isModified = true
            ) 
        }
        Logger.d("WaypointViewModel", "Added waypoint #$newId at ($lat, $lng)")

        return newId
    }
    
    /**
     * 更新路点
     * 
     * @param index 路点索引
     * @param lat 纬度
     * @param lng 经度
     * @param type 路点类型
     * @param connects 连接的路点 ID 列表
     */
    fun updateWaypoint(
        index: Int,
        lat: Double? = null,
        lng: Double? = null,
        type: PointType? = null,
        connects: Set<Int>? = null
    ) {
        val state = _uiState.value
        if (index !in state.routeObject.points.indices) return
        
        val oldPoint = state.routeObject.points[index]
        val updatedPoint = oldPoint.copy(
            lat = lat ?: oldPoint.lat,
            lng = lng ?: oldPoint.lng,
            type = type ?: oldPoint.type,
            connects = connects ?: oldPoint.connects
        )
        
        val updatedWaypoints = state.routeObject.points.toMutableList()
        updatedWaypoints[index] = updatedPoint
        
        _uiState.update { 
            it.copy(
                routeObject = state.routeObject.copy(points = updatedWaypoints),
                isModified = true
            ) 
        }
        Logger.d("WaypointViewModel", "Updated waypoint #${oldPoint.id} at index $index")
    }
    
    /**
     * 删除路点
     * 
     * @param index 路点索引
     */
    fun deleteWaypoint(index: Int) {
        val state = _uiState.value
        if (index !in state.routeObject.points.indices) return
        
        val deletedPoint = state.routeObject.points[index]
        val deletedId = deletedPoint.id
        val neighborIds = deletedPoint.connects.toList()

        // 对被删除节点的所有邻居两两之间添加直连，桥接断开的路径
        var updatedRoute = state.routeObject
        for (i in neighborIds.indices) {
            for (j in i + 1 until neighborIds.size) {
                updatedRoute = updatedRoute.addConn(neighborIds[i], neighborIds[j])
            }
        }

        // 移除被删除的路点及所有对它的引用
        val updatedPoints = updatedRoute.points
            .filter { it.id != deletedId }
            .map { point -> point.copy(connects = point.connects - deletedId) }

        _uiState.update { 
            it.copy(
                routeObject = updatedRoute.copy(points = updatedPoints),
                isModified = true,
                selectedWaypointIndex = -1
            ) 
        }
        Logger.d("WaypointViewModel", "Deleted waypoint #$deletedId at index $index")
    }
    
    /**
     * 添加路点的连接
     * 
     * @param from 起始路点索引
     * @param to 中止路点索引
     */
    fun addWaypointConnections(from: Int, to: Int) {
        _uiState.update { 
            it.copy(
                isModified = true,
                routeObject = it.routeObject.addConn(from, to)
            ) 
        }
    }

    /**
     * 添加路点的连接
     *
     * @param from 起始路点索引
     * @param to 中止路点索引
     */
    fun removeWaypointConnections(from: Int, to: Int) {
        _uiState.update {
            it.copy(
                isModified = true,
                routeObject = it.routeObject.removeConn(from, to)
            )
        }
    }
    
    /**
     * 选择路点
     * 
     * @param index 路点索引，null 表示取消选择
     */
    fun selectWaypoint(index: Int) {
        _uiState.update { it.copy(selectedWaypointIndex = index) }
    }
    
    // =====================================================
    // UI 控制
    // =====================================================
    
    /**
     * 设置显示模式
     * 
     * @param mode 0=Route, 1=Map
     */
    fun setDisplayMode(mode: Int) {
        _uiState.update { it.copy(selectedDisplayMode = mode) }
    }
    
    /**
     * 设置底部表单详情
     * 
     * @param detail 0=Waypoints, 1=Details
     */
    fun setSheetDetail(detail: Int) {
        _uiState.update { it.copy(selectedSheetDetail = detail) }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 检查是否有未保存的修改
     * 
     * @return 是否有未保存的修改
     */
    fun hasUnsavedChanges(): Boolean {
        return _uiState.value.isModified
    }
    
    /**
     * 请求显示未保存更改对话框
     */
    fun showUnsavedChangesDialog() {
        _uiEvent.tryEmit(WaypointUiEvent.ShowUnsavedChangesDialog)
    }
    
    /**
     * 放弃更改并关闭
     */
    fun discardChangesAndClose() {
        _uiState.update { it.copy(isModified = false) }
        _uiEvent.tryEmit(WaypointUiEvent.ClosActivity)
    }
}
