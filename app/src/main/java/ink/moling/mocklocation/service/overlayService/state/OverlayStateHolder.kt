package ink.moling.mocklocation.service.overlayService.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 摇杆状态单例
 * 用于在 FloatingUI 和 JoystickSimulator 之间共享摇杆状态
 */
object OverlayStateHolder {
    data class OverlayState(
        val mockMode: Int = 0,  // 0 = POINT, 1 = ROUTE
        /* 点位模拟相关 */
        val direction: Float = 0f,      // 方向角度（弧度），0为北，顺时针增加
        val speed: Float = 0f,          // 速度（0-1），0为静止，1为最大速度
        val maxSpeed: Double = 5.0      // 最大速度（米/秒），默认为 5 m/s (约18 km/h，步行速度)
    )

    private val _state = MutableStateFlow(OverlayState())
    val state: StateFlow<OverlayState> = _state.asStateFlow()

    fun setMode(mode: Int) {
        _state.value = OverlayState(
            mode,
            _state.value.direction,
            _state.value.speed,
            _state.value.maxSpeed
        )
    }

    /**
     * 更新全局摇杆状态
     * @param direction 方向角度（弧度），0为北，顺时针增加
     * @param speed 速度（0-1），0为静止，1为最大速度
     */
    fun update(direction: Float, speed: Float, maxSpeed: Double = 5.0) {
        _state.value = OverlayState(
            0,  // POINT mode
            direction,
            speed,
            maxSpeed.coerceAtLeast(0.1) // 最小速度 0.1 m/s
        )
    }

    /**
     * 更新全局路径模拟状态
     * @param speed 速度（0-1），0为静止，1为最大速度
     */
    fun update(speed: Float, maxSpeed: Double = 5.0) {
        _state.value = OverlayState(
            1,  // ROUTE mode
            0.0f,
            speed,
            maxSpeed.coerceAtLeast(0.1) // 最小速度 0.1 m/s
        )
    }

    /**
     * 重置摇杆状态
     */
    fun reset() {
        _state.value = OverlayState()
    }
}