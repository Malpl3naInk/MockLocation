package ink.moling.mocklocation.utils

fun normalizeAzimuth(deg: Int): Int {
    return ((deg % 360) + 360) % 360
}


fun azimuthToDirection(azimuthDeg: Int): String {
    val a = normalizeAzimuth(azimuthDeg)

    return when {
        a !in 23..<337   -> "N"
        a < 68              -> "NE"
        a < 113             -> "E"
        a < 158             -> "SE"
        a < 203             -> "S"
        a < 248             -> "SW"
        a < 293             -> "W"
        else                -> "NW"
    }
}
