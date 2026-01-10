package ink.moling.mocklocation.data.models

enum class Source { GPS, NETWORK }

data class CandidateLocation(
    val lat: Double,
    val lng: Double,
    val alt: Double?,
    val accuracy: Float,
    val time: Long,
    val source: Source
)

fun chooseBest(
    gps: CandidateLocation?,
    net: CandidateLocation?
): CandidateLocation? {

    val now = System.currentTimeMillis()
    val gpsFresh = gps != null && now - gps.time < 5_000

    return when {
        gpsFresh && gps.accuracy <= 20f -> gps
        gpsFresh && net == null -> gps
        gps == null -> net
        net == null -> gps
        else -> if (gps.accuracy <= net.accuracy) gps else net
    }
}

