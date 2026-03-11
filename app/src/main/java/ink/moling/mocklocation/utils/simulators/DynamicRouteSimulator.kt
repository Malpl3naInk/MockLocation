package ink.moling.mocklocation.utils.simulators

import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.service.overlayService.state.OverlayStateHolder
import ink.moling.mocklocation.utils.extensions.isLoop
import ink.moling.mocklocation.utils.logger.Logger
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 动态路线模拟器，沿指定路线移动
 *
 * @param route 路线对象，包含路线点列表
 */
class DynamicRouteSimulator(
    private val route: RouteObject
) : LocationSimulator {

    // 当前位置（经纬度）
    private var currentLat: Double
    private var currentLng: Double
    private var currentAlt: Double = 0.0

    // 当前路线状态
    private var currentPointIndex: Int = 0
    private var nextPointIndex: Int = 0
    private var previousPointIndex: Int = -1 // 记录上一个点，防止往返
    private var segmentProgress: Double = 0.0 // 当前线段的进度 (0.0 ~ 1.0)
    private var routeCompleted: Boolean = false // 路线是否已完成

    // 是否为环形路线
    private val isLoop: Boolean = route.isLoop()

    // 运动状态
    private var currentBearing: Float = 0f
    private var currentSpeed: Double = 0.0

    // 随机偏移状态（垂直路径方向，单位：米）
    private var smoothOffsetM: Double = 0.0

    // 地球半径（米）
    private val earthRadiusM = 6371000.0

    init {
        require(route.points.isNotEmpty()) { "Route must have at least one point" }

        // 初始化为第一个点的位置
        currentLat = route.points[0].lat
        currentLng = route.points[0].lng

        // 如果有多个点，设置下一个目标点
        if (route.points.size > 1) {
            nextPointIndex = 1
            // 计算初始方位角
            currentBearing = calculateBearing(
                route.points[0].lat, route.points[0].lng,
                route.points[1].lat, route.points[1].lng
            )
        }
    }

    override fun next(deltaTimeMs: Long): SimulatedLocation {
        // 如果只有一个点，保持静止
        if (route.points.size <= 1) {
            return SimulatedLocation(
                lat = currentLat,
                lng = currentLng,
                alt = currentAlt,
                bearing = currentBearing,
                speed = 0.0
            )
        }

        // 如果是环形路线且已完成，重置继续循环
        if (routeCompleted && isLoop) {
            resetToStart()
        }

        // 如果路线已完成（非环形），保持静止
        if (routeCompleted) {
            return SimulatedLocation(
                lat = currentLat,
                lng = currentLng,
                alt = currentAlt,
                bearing = currentBearing,
                speed = 0.0
            )
        }

        val overlayState = OverlayStateHolder.state.value

        // 计算实际速度（米/秒）
        currentSpeed = if (overlayState.speed > 0.01f) {
            overlayState.maxSpeed * overlayState.speed
        } else {
            0.0
        }

        // 如果速度为0，保持当前位置
        if (currentSpeed <= 0.01) {
            return SimulatedLocation(
                lat = currentLat,
                lng = currentLng,
                alt = currentAlt,
                bearing = currentBearing,
                speed = currentSpeed
            )
        }

        // 计算移动距离（米）
        val deltaTimeS = deltaTimeMs / 1000.0
        val distanceM = currentSpeed * deltaTimeS

        // 沿路线移动
        moveAlongRoute(distanceM)

        Logger.d("DynamicRouteSimulator",
            "Point: $currentPointIndex->$nextPointIndex, Progress: %.2f%%, Bearing: %.1f°, Speed: %.1f m/s, Loop: $isLoop, Completed: $routeCompleted"
                .format(segmentProgress * 100, currentBearing, currentSpeed))

        if (overlayState.randomOffset) {
            val maxOffsetM = overlayState.maxRandomOffset
            smoothOffsetM = (smoothOffsetM + (Math.random() - 0.5) * (maxOffsetM * 0.15)).coerceIn(-maxOffsetM, maxOffsetM)
            val perpBearingRad = Math.toRadians((currentBearing + 90.0) % 360.0)
            val offsetLat = (smoothOffsetM * cos(perpBearingRad)) / 111111.0
            val offsetLng = (smoothOffsetM * sin(perpBearingRad)) / (111111.0 * cos(Math.toRadians(currentLat)))
            return SimulatedLocation(
                lat = currentLat + offsetLat,
                lng = currentLng + offsetLng,
                alt = currentAlt,
                bearing = currentBearing,
                speed = currentSpeed
            )
        }

        return SimulatedLocation(
            lat = currentLat,
            lng = currentLng,
            alt = currentAlt,
            bearing = currentBearing,
            speed = currentSpeed
        )
    }

    /**
     * 沿路线移动指定距离
     */
    private fun moveAlongRoute(distanceM: Double) {
        var remainingDistance = distanceM

        while (remainingDistance > 0.001 && !routeCompleted) {
            val currentPoint = route.points[currentPointIndex]
            val nextPoint = route.points[nextPointIndex]

            // 计算当前线段的总长度
            val segmentLength = calculateDistance(
                currentPoint.lat, currentPoint.lng,
                nextPoint.lat, nextPoint.lng
            )

            // 计算当前位置到下一个点的剩余距离
            val remainingSegmentLength = segmentLength * (1.0 - segmentProgress)

            if (remainingDistance >= remainingSegmentLength) {
                // 可以到达下一个点
                remainingDistance -= remainingSegmentLength
                previousPointIndex = currentPointIndex // 记录上一个点
                currentPointIndex = nextPointIndex
                segmentProgress = 0.0

                // 更新当前位置为下一个点
                currentLat = nextPoint.lat
                currentLng = nextPoint.lng

                // 查找下一个目标点
                if (!findNextPoint()) {
                    if (isLoop) {
                        // 环形路线，标记完成，下次会重置
                        routeCompleted = true
                        Logger.d("DynamicRouteSimulator", "Loop completed, will restart")
                    } else {
                        // 非环形路线，停在最后一个点
                        routeCompleted = true
                        currentSpeed = 0.0
                        Logger.d("DynamicRouteSimulator", "Route completed at point $currentPointIndex")
                    }
                    remainingDistance = 0.0
                    break
                }

                // 更新方位角
                currentBearing = calculateBearing(
                    route.points[currentPointIndex].lat, route.points[currentPointIndex].lng,
                    route.points[nextPointIndex].lat, route.points[nextPointIndex].lng
                )
            } else {
                // 在当前线段内移动
                val progressIncrease = remainingDistance / segmentLength
                segmentProgress += progressIncrease
                segmentProgress = segmentProgress.coerceIn(0.0, 1.0)

                // 插值计算当前位置
                currentLat = currentPoint.lat + (nextPoint.lat - currentPoint.lat) * segmentProgress
                currentLng = currentPoint.lng + (nextPoint.lng - currentPoint.lng) * segmentProgress

                remainingDistance = 0.0
            }
        }
    }

    /**
     * 查找下一个路线点
     * @return 是否找到下一个点
     */
    private fun findNextPoint(): Boolean {
        val currentPoint = route.points[currentPointIndex]

        // 如果有连接信息，使用连接信息
        if (currentPoint.connects.isNotEmpty()) {
            // 尝试找到一个不是上一个点的连接点（防止往返）
            for (nextId in currentPoint.connects) {
                val nextIndex = route.points.indexOfFirst { it.id == nextId }
                if (nextIndex >= 0 && nextIndex != previousPointIndex) {
                    nextPointIndex = nextIndex
                    return true
                }
            }

            // 如果所有连接点都是上一个点，说明到达终点或环的闭合点
            return false
        }

        // 否则按顺序取下一个点
        if (currentPointIndex + 1 < route.points.size) {
            nextPointIndex = currentPointIndex + 1
            return true
        }

        return false
    }

    /**
     * 重置到路线起点（用于环形路线循环）
     */
    private fun resetToStart() {
        if (route.points.isNotEmpty()) {
            currentPointIndex = 0
            nextPointIndex = if (route.points.size > 1) 1 else 0
            previousPointIndex = -1
            segmentProgress = 0.0
            routeCompleted = false
            currentLat = route.points[0].lat
            currentLng = route.points[0].lng

            if (route.points.size > 1) {
                currentBearing = calculateBearing(
                    route.points[0].lat, route.points[0].lng,
                    route.points[1].lat, route.points[1].lng
                )
            }
            Logger.d("DynamicRouteSimulator", "Loop restarted from point 0")
        }
    }

    /**
     * 计算两点之间的距离（米）
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLng = Math.toRadians(lng2 - lng1)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusM * c
    }

    /**
     * 计算从点1到点2的方位角（度数，0为北，顺时针）
     */
    private fun calculateBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLng = Math.toRadians(lng2 - lng1)

        val y = sin(deltaLng) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(deltaLng)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)

        // 转换为0-360度
        return ((bearingDeg + 360) % 360).toFloat()
    }

    /**
     * 获取当前位置
     */
    fun getCurrentPosition() = Triple(currentLat, currentLng, currentAlt)

    /**
     * 重置到路线起点
     */
    fun reset() {
        if (route.points.isNotEmpty()) {
            currentPointIndex = 0
            nextPointIndex = if (route.points.size > 1) 1 else 0
            previousPointIndex = -1
            segmentProgress = 0.0
            routeCompleted = false
            smoothOffsetM = 0.0
            currentLat = route.points[0].lat
            currentLng = route.points[0].lng

            if (route.points.size > 1) {
                currentBearing = calculateBearing(
                    route.points[0].lat, route.points[0].lng,
                    route.points[1].lat, route.points[1].lng
                )
            }
        }
    }
}
