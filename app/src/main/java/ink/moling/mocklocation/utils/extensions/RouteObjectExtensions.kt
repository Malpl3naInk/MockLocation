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