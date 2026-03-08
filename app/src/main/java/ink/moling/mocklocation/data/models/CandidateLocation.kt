package ink.moling.mocklocation.data.models

enum class Source { GPS, NETWORK, MOCK, DEFAULT }

data class CandidateLocation(
    val lat: Double,
    val lng: Double,
    val alt: Double?,
    val accuracy: Float,
    val time: Long,
    val source: Source
) {
    companion object {
        val Default = CandidateLocation(
            lat = 51.4769,
            lng = 0.0005,
            alt = 46.0,
            accuracy = 1f,
            time = 1721506680,
            source = Source.DEFAULT
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CandidateLocation

        if (lat != other.lat) return false
        if (lng != other.lng) return false
        if (alt != other.alt) return false
        if (accuracy != other.accuracy) return false
        if (time != other.time) return false
        if (source != other.source) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lat.hashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + (alt?.hashCode() ?: 0)
        result = 31 * result + accuracy.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + source.hashCode()
        return result
    }


}
