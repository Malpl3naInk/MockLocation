package ink.moling.mocklocation.service.locationService.state

sealed class LocationMode {
    object Idle : LocationMode()
    data class Point(
        val name: String,
        val lat: Double,
        val lng: Double
    ) : LocationMode()

    data class Route(
        val name: String,
        val speedMps: Double
    ) : LocationMode()
}