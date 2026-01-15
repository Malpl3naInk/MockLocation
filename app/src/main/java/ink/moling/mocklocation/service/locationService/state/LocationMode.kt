package ink.moling.mocklocation.service.locationService.state

sealed class LocationMode {
    object Idle : LocationMode()
    data class Point(
        val lat: Double,
        val lng: Double
    ) : LocationMode()

    data class Route(
        val speedMps: Double
    ) : LocationMode()
}