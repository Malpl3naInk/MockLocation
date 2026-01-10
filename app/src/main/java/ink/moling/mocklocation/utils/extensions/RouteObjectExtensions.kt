package ink.moling.mocklocation.utils.extensions

import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.RoutePoint


fun RouteObject.toPointMap(): Map<Int, RoutePoint> =
    points.associateBy { it.id }

fun RouteObject.toGraph(): Map<Int, List<Int>> =
    points.associate { it.id to it.connects }