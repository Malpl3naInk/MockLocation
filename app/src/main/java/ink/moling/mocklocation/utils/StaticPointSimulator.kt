package ink.moling.mocklocation.utils

class StaticPointSimulator(
    private var lat: Double,
    private var lng: Double,
    private var alt: Double
) : LocationSimulator {

    override fun next(deltaTimeMs: Long): SimulatedLocation {
        return SimulatedLocation(
            lat = lat,
            lng = lng,
            alt = alt,
            bearing = 0f,
            speed = 0.0
        )
    }

    fun updatePoint(lat: Double, lng: Double, alt: Double) {
        this.lat = lat
        this.lng = lng
        this.alt = alt
    }
}
