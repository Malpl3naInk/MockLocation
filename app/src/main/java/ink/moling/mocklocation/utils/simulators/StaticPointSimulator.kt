package ink.moling.mocklocation.utils.simulators

import ink.moling.mocklocation.service.overlayService.state.OverlayStateHolder
import ink.moling.mocklocation.utils.logger.Logger
import kotlin.math.cos
import kotlin.math.sin

/**
 * 静态点位模拟器，支持摇杆控制动态移动
 * 
 * @param lat 初始纬度
 * @param lng 初始经度
 * @param alt 初始海拔
 */
class StaticPointSimulator(
    private var lat: Double,
    private var lng: Double,
    private var alt: Double,
) : LocationSimulator {
    
    private var currentBearing: Float = 0f
    private var currentSpeed: Double = 0.0
    
    // 地球半径（米）
    private val earthRadiusM = 6371000.0

    override fun next(deltaTimeMs: Long): SimulatedLocation {
        val overlayState = OverlayStateHolder.state.value
        
        // 如果摇杆有移动
        if (overlayState.speed > 0.01f) {
            // 计算实际速度（米/秒）
            currentSpeed = overlayState.maxSpeed * overlayState.speed
            
            // 更新方位角（转换为度数），当摇杆移动时同步方向
            currentBearing = Math.toDegrees(overlayState.direction.toDouble()).toFloat()
            Logger.d("StaticPointSimulator", "direction(rad)=${overlayState.direction}, bearing(deg)=$currentBearing, speed=$currentSpeed")
            
            // 计算移动距离（米）
            val deltaTimeS = deltaTimeMs / 1000.0
            val distanceM = currentSpeed * deltaTimeS
            
            // 根据方向和距离计算新的经纬度
            // 方向角：0为北，顺时针增加
            val direction = overlayState.direction
            
            // 计算纬度变化
            // 北方向为负Y轴，所以使用 cos
            val deltaLat = distanceM * cos(direction.toDouble()) / earthRadiusM
            lat += Math.toDegrees(deltaLat)
            
            // 计算经度变化
            // 东方向为正X轴，所以使用 sin
            val deltaLng = distanceM * sin(direction.toDouble()) / 
                          (earthRadiusM * cos(Math.toRadians(lat)))
            lng += Math.toDegrees(deltaLng)
            
            // 限制经纬度范围
            lat = lat.coerceIn(-90.0, 90.0)
            lng = when {
                lng > 180.0 -> lng - 360.0
                lng < -180.0 -> lng + 360.0
                else -> lng
            }
        } else {
            currentSpeed = 0.0
        }
        
        return SimulatedLocation(
            lat = lat,
            lng = lng,
            alt = alt,
            bearing = currentBearing,  // 始终返回当前方向（移动时更新，静止时保持）
            speed = currentSpeed
        )
    }

    fun updatePoint(lat: Double, lng: Double, alt: Double) {
        this.lat = lat
        this.lng = lng
        this.alt = alt
    }
    
    /**
     * 获取当前位置
     */
    fun getCurrentPosition() = Triple(lat, lng, alt)
}
