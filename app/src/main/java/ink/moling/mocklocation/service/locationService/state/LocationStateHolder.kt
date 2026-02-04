package ink.moling.mocklocation.service.locationService.state

import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/* 默认位置常量 */
const val DEFAULT_LAT = 51.476853
const val DEFAULT_LNG = 0.0 // 默认经纬度(格林尼治天文台)
const val DEFAULT_ALT = 694.0
const val DEFAULT_BEA = 0.0f

/**
 * 负责管理位置状态
 */
class LocationStateHolder {
    // 当前位置坐标
    var curLat = DEFAULT_LAT
    var curLng = DEFAULT_LNG
    var curAlt = DEFAULT_ALT
    var curBea = DEFAULT_BEA

    private val _state = MutableStateFlow(CandidateLocation.Default)
    val state: StateFlow<CandidateLocation> = _state

    private var currentSource: Source? = null
    private var mockEnabled: Boolean = false

    fun updateFromRealLocation(location: CandidateLocation) {
        if (mockEnabled) return   // mock 优先级最高

        // 简单策略：GPS > NETWORK，同源则允许更新
        val canOverride =
            currentSource == null ||
                    currentSource == location.source ||
                    (currentSource == Source.NETWORK &&
                    location.source == Source.GPS)

        if (!canOverride) return

        currentSource = location.source
        _state.value = location
    }

    fun updateFromMock(location: CandidateLocation) {
        mockEnabled = true
        currentSource = Source.MOCK
        _state.value = location
    }

    fun stopMock() {
        mockEnabled = false
        currentSource = null
        _state.value = CandidateLocation.Default
    }
}
