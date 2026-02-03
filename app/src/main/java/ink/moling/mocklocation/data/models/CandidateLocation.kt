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
}
