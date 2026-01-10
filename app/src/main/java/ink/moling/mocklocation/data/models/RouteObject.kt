package ink.moling.mocklocation.data.models

enum class PointType { R, L, W }

data class RouteObject(
    val name: String,
    val meta: RouteMeta,
    val points: List<RoutePoint>
)

data class RouteMeta(
    val type: PointType,
    val version: Int
)

data class RoutePoint(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val type: String,
    val connects: List<Int>
)
