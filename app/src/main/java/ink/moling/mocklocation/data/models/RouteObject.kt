package ink.moling.mocklocation.data.models

import androidx.room.TypeConverter
import com.google.gson.Gson

enum class RouteType { ROUTE, WAYPOINTS }
enum class PointType { R, L, W }

data class RouteObject(
    val name: String,
    val meta: RouteMeta,
    val points: List<RoutePoint>
) {
    companion object {
        val Empty = RouteObject(
            name = "",
            meta = RouteMeta(
                type = RouteType.ROUTE,
                version = 1
            ),
            points = emptyList()
        )
    }
}

data class RouteMeta(
    val type: RouteType,
    val version: Int
)

data class RoutePoint(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val type: PointType,
    val connects: Set<Int>
)

object RouteObjectJson {
    fun toJson(value: RouteObject?): String? =
        value?.let { Gson().toJson(it) }

    fun fromJson(value: String?): RouteObject? =
        value?.let { Gson().fromJson(it, RouteObject::class.java) }
}

class RouteObjectConverter {
    @TypeConverter
    fun toJson(value: RouteObject?): String? =
        RouteObjectJson.toJson(value)

    @TypeConverter
    fun fromJson(value: String?): RouteObject? =
        RouteObjectJson.fromJson(value)
}
