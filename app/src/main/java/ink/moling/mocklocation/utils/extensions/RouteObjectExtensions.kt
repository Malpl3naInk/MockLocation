package ink.moling.mocklocation.utils.extensions

import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.RoutePoint

/**
 * Convert route points to a map indexed by point ID
 * 
 * @return A map where the key is the point ID and the value is the RoutePoint object
 */
fun RouteObject.toPointMap(): Map<Int, RoutePoint> =
    points.associateBy { it.id }

/**
 * Convert route to a graph structure for path analysis
 * 
 * @return A map where the key is the point ID and the value is a list of connected point IDs
 */
fun RouteObject.toGraph(): Map<Int, List<Int>> =
    points.associate { it.id to it.connects }

fun RouteObject.isValid(): Boolean {
    // 验证必要字段
    if (this.name.isBlank())    return false
    if (this.points.isEmpty())  return false

    // 验证点的数据
    this.points.forEachIndexed { _, point ->
        // 验证经纬度范围
        if (point.lat !in -90.0..90.0)      return false
        if (point.lng !in -180.0..180.0)    return false
    }

    return true
}