package ink.moling.mocklocation.utils

interface LocationSimulator {
    /** 每一次 tick 调用，返回下一帧的位置 */
    fun next(deltaTimeMs: Long): SimulatedLocation
}

data class SimulatedLocation(
    val lat: Double,
    val lng: Double,
    val alt: Double,
    val bearing: Float,
    val speed: Double
)