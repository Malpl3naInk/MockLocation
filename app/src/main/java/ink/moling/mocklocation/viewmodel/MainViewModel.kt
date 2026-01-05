package ink.moling.mocklocation.viewmodel

import androidx.lifecycle.ViewModel
import ink.moling.mocklocation.MOCK_STATUS_DISABLED
import ink.moling.mocklocation.MockLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MainViewModel 管理 MockLocation 应用的 UI 状态
 */
class MainViewModel : ViewModel() {

    // Mock 服务状态
    private val _mockStatus = MutableStateFlow(MOCK_STATUS_DISABLED)
    val mockStatus: StateFlow<Int> = _mockStatus.asStateFlow()

    // 实时 GPS 数据
    private val _gpsLatitude = MutableStateFlow(0.0)
    val gpsLatitude: StateFlow<Double> = _gpsLatitude.asStateFlow()

    private val _gpsLongitude = MutableStateFlow(0.0)
    val gpsLongitude: StateFlow<Double> = _gpsLongitude.asStateFlow()

    private val _gpsAltitude = MutableStateFlow(0.0)
    val gpsAltitude: StateFlow<Double> = _gpsAltitude.asStateFlow()

    private val _gpsProvider = MutableStateFlow("")
    val gpsProvider: StateFlow<String> = _gpsProvider.asStateFlow()

    // Dialog 状态
    private val _isImportExportDialogOpen = MutableStateFlow(false)
    val isImportExportDialogOpen: StateFlow<Boolean> = _isImportExportDialogOpen.asStateFlow()

    // 服务引用（由 Activity 设置）
    var serviceBinder: MockLocationService.MockLocationServiceBinder? = null

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
     * 更新 Mock 服务状态
     */
    fun updateMockStatus(status: Int) {
        _mockStatus.value = status
    }

    /**
     * 打开/关闭导入导出对话框
     */
    fun setImportExportDialogOpen(isOpen: Boolean) {
        _isImportExportDialogOpen.value = isOpen
    }

    /**
     * 设置模拟位置（通过服务）
     */
    fun setMockPosition(latitude: Double, longitude: Double, altitude: Double) {
        serviceBinder?.setPosition(latitude, longitude, altitude)
    }
}