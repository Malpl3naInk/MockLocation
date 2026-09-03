package ink.moling.mocklocation.utils.extensions

import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import com.google.gson.JsonObject
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

/**
 * 检查路径是否为有效的环（首尾相连，无其他分支）
 * 
 * 有效环的条件：
 * 1. 每个点最多有 2 个连接（入度和出度都不超过 2）
 * 2. 只有一个连通分量
 * 3. 如果是环，则首尾相连（起点和终点相同）
 * 4. 如果不是环，则是一条链（起点和终点不同，各只有一个连接）
 */
fun RouteObject.isValidLoopOrChain(): Boolean {
    if (points.isEmpty()) return true
    
    val pointMap = toPointMap()
    
    // 检查每个点的连接数不超过 2
    for (point in points) {
        if (point.connects.size > 2) return false
    }
    
    // 检查连通性
    if (!isFullyConnected()) return false
    
    // 统计端点（连接数为 1 的点）
    val endpoints = points.filter { it.connects.size == 1 }
    
    // 有效的情况：
    // 1. 没有端点 - 是一个环
    // 2. 有 2 个端点 - 是一条链
    return endpoints.size == 0 || endpoints.size == 2
}

/**
 * 检查所有点是否连通
 */
fun RouteObject.isFullyConnected(): Boolean {
    if (points.isEmpty()) return true

    val pointMap = toPointMap()
    val visited = mutableSetOf<Int>()
    val queue = ArrayDeque<Int>()

    // 从第一个点开始 BFS
    queue.add(points.first().id)

    while (queue.isNotEmpty()) {
        val currentId = queue.removeFirst()
        if (currentId in visited) continue
        visited.add(currentId)

        val point = pointMap[currentId] ?: continue
        for (neighborId in point.connects) {
            if (neighborId !in visited) {
                queue.add(neighborId)
            }
        }
    }

    // 所有点都应该被访问到
    return visited.size == points.size
}

/**
 * 检查路径是否为环形（首尾相连，没有端点）
 */
fun RouteObject.isLoop(): Boolean {
    if (points.isEmpty()) return false

    // 检查每个点的连接数
    // 环形：每个点都有且仅有 2 个连接
    // 链形：有 2 个端点（连接数为 1），其余点连接数为 2
    val endpoints = points.filter { it.connects.size == 1 }

    // 如果没有端点，说明是环形
    return endpoints.isEmpty() && points.all { it.connects.size == 2 }
}

/**
 * 将路线的每条 edge（连接关系）转换为 MultiLineString，支持分支拓扑。
 * 每个独立 edge 作为一条 2 点 LineString，避免单条折线无法表达分支的问题。
 */
fun RouteObject.toMultiLineString(): MultiLineString {
    val pointMap = toPointMap()
    val lines = mutableListOf<List<Point>>()
    val seenEdges = mutableSetOf<Pair<Int, Int>>()

    for (point in points) {
        for (neighborId in point.connects) {
            val edgeKey = minOf(point.id, neighborId) to maxOf(point.id, neighborId)
            if (seenEdges.add(edgeKey)) {
                val neighbor = pointMap[neighborId] ?: continue
                lines.add(
                    listOf(
                        Point.fromLngLat(point.lng, point.lat),
                        Point.fromLngLat(neighbor.lng, neighbor.lat)
                    )
                )
            }
        }
    }

    return MultiLineString.fromLngLats(lines)
}

/**
 * 将所有路点转换为 Feature 列表，用于在地图上绘制节点圆圈（包括孤立点）。
 */
fun RouteObject.toFeatureList(): List<Feature> =
    points.map { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)) }

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

/**
 * 将路线的每条 edge（连接关系）转换为带属性的独立 LineString Feature，供地图数据驱动渲染。
 *
 * 属性：
 * - "kind"：两端点类型相同时为对应类型(R/W/L)；不同时为 "X"（由图层 fallback 色处理）
 * - "dim"：选中某路点后，与该路点不直接相连的边为 true（用于淡化显示）
 */
fun RouteObject.toEdgeFeatures(selectedPointId: Int?): List<Feature> {
    val pointMap = toPointMap()
    val seenEdges = mutableSetOf<Pair<Int, Int>>()
    val features = mutableListOf<Feature>()

    for (point in points) {
        for (neighborId in point.connects) {
            val edgeKey = minOf(point.id, neighborId) to maxOf(point.id, neighborId)
            if (seenEdges.add(edgeKey)) {
                val neighbor = pointMap[neighborId] ?: continue

                val kind = if (point.type == neighbor.type) point.type.name else "X"
                val dim = selectedPointId != null &&
                    point.id != selectedPointId && neighborId != selectedPointId

                val properties = JsonObject().apply {
                    addProperty("kind", kind)
                    addProperty("dim", dim)
                }
                features.add(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            listOf(
                                Point.fromLngLat(point.lng, point.lat),
                                Point.fromLngLat(neighbor.lng, neighbor.lat)
                            )
                        ),
                        properties
                    )
                )
            }
        }
    }
    return features
}

/**
 * 将每个路点转换为带属性的 Point Feature，供地图数据驱动渲染。
 *
 * 属性：
 * - "selected"：该点是否为当前选中路点
 * - "dim"：选中某路点后，与该点及其直接相连点无关的点为 true（用于淡化显示）
 */
fun RouteObject.toPointFeatures(selectedPointId: Int?): List<Feature> {
    val pointMap = toPointMap()
    val selected = selectedPointId?.let { pointMap[it] }
    val relatedIds = selected?.connects ?: emptySet()

    return points.map { point ->
        val isSelected = point.id == selectedPointId
        val isRelated = selected != null && (isSelected || point.id in relatedIds)
        val dim = selected != null && !isRelated

        val properties = JsonObject().apply {
            addProperty("selected", isSelected)
            addProperty("dim", dim)
        }
        Feature.fromGeometry(
            Point.fromLngLat(point.lng, point.lat),
            properties
        )
    }
}
