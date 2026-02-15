package ink.moling.mocklocation.utils.extensions

import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.RoutePoint
import ink.moling.mocklocation.data.models.RouteType

fun RouteType.label(): String = when (this) {
    RouteType.ROUTE -> "Route"
    RouteType.WAYPOINTS -> "Map"
}

/**
 * Convert route points to a map indexed by point ID
 * 
 * @return A map where the key is the point ID and the value is the RoutePoint object
 */
fun RouteObject.toPointMap(): Map<Int, RoutePoint> =
    points.associateBy { it.id }

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

fun RouteObject.isConnected(a: Int, b: Int): Boolean =
    toPointMap()[a]?.connects?.contains(b) == true

fun RouteObject.addConn(from: Int, to: Int): RouteObject {
    if (from == to) return this

    val map = this.toPointMap().toMutableMap()

    val pFrom = map[from] ?: return this
    val pTo = map[to] ?: return this

    map[from] = pFrom.copy(connects = pFrom.connects + to)
    map[to] = pTo.copy(connects = pTo.connects + from)

    return copy(points = map.values.toList())
}

fun RouteObject.removeConn(from: Int, to: Int): RouteObject {
    val map = points.associateBy { it.id }.toMutableMap()

    val pFrom = map[from] ?: return this
    val pTo = map[to] ?: return this

    map[from] = pFrom.copy(connects = pFrom.connects - to)
    map[to] = pTo.copy(connects = pTo.connects - from)

    return copy(points = map.values.toList())
}
