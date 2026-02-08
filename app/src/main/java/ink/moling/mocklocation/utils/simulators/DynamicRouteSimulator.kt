package ink.moling.mocklocation.utils.simulators

import ink.moling.mocklocation.data.models.RouteObject

class DynamicRouteSimulator(
    route: RouteObject
) : LocationSimulator {
    override fun next(deltaTimeMs: Long): SimulatedLocation {

        return SimulatedLocation(
            lat = 0.0,
            lng = 0.0,
            alt = 0.0,
            bearing = 0.0f,  // 始终返回当前方向（移动时更新，静止时保持）
            speed = 0.0
        )
    }
}