package ink.moling.mocklocation.data.models

enum class Source { GPS, NETWORK, MOCK }

data class CandidateLocation(
    val lat: Double,
    val lng: Double,
    val alt: Double?,
    val accuracy: Float,
    val time: Long,
    val source: Source
)
