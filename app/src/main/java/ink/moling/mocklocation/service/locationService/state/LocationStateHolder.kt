package ink.moling.mocklocation.service.locationService.state

import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 负责管理位置状态
 */
class LocationStateHolder {

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
