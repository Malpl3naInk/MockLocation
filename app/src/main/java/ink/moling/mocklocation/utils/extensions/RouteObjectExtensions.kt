package ink.moling.mocklocation.utils.extensions

import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
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

fun RouteObject.hasCycle(): Boolean {
    val pointMap = toPointMap()
    val visited = mutableSetOf<Int>()

    fun dfs(nodeId: Int, parentId: Int?): Boolean {
        visited.add(nodeId)
        val node = pointMap[nodeId] ?: return false
        for (neighborId in node.connects) {
            if (neighborId !in visited) {
                if (dfs(neighborId, nodeId)) return true
            } else if (neighborId != parentId) {
                return true
            }
        }
        return false
    }

    for (point in points) {
        if (point.id !in visited) {
            if (dfs(point.id, null)) return true
        }
    }
    return false
}

fun RouteObject.toLineString(): LineString {
    return LineString.fromLngLats(
        points
            .sortedBy { it.id }   // 如果需要按顺序
            .map { Point.fromLngLat(it.lng, it.lat) }
    )
}

fun RouteObject.isEmpty(): Boolean {
    return this.points.isEmpty()
}

fun RouteObject.centerPoint(): Point {

    val minLat = points.minOf { it.lat }
    val maxLat = points.maxOf { it.lat }
    val minLng = points.minOf { it.lng }
    val maxLng = points.maxOf { it.lng }

    val centerLat = (minLat + maxLat) / 2.0
    val centerLng = (minLng + maxLng) / 2.0

    return Point.fromLngLat(centerLng, centerLat)
}
