package ink.moling.mocklocation.service.joystickService.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 摇杆状态单例
 * 用于在 FloatingUI 和 JoystickSimulator 之间共享摇杆状态
 */
object JoystickStateHolder {
    data class State(
        val direction: Float = 0f,      // 方向角度（弧度），0为北，顺时针增加
        val speed: Float = 0f,          // 速度（0-1），0为静止，1为最大速度
        val maxSpeed: Double = 5.0      // 最大速度（米/秒），默认为 5 m/s (约18 km/h，步行速度)
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * 更新摇杆状态
     * @param direction 方向角度（弧度），0为北，顺时针增加
     * @param speed 速度（0-1），0为静止，1为最大速度
     */
    fun update(direction: Float, speed: Float, maxSpeed: Double = 5.0) {
        _state.value = State(
            direction,
            speed,
            maxSpeed.coerceAtLeast(0.1) // 最小速度 0.1 m/s
        )
    }

    /**
     * 重置摇杆状态
     */
    fun reset() {
        _state.value = State()
    }
}