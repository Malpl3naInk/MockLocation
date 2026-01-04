package ink.moling.mocklocation.utils

data class KalmanState(
    var lat: Double,
    var lon: Double,
    var vLat: Double,
    var vLon: Double,
    var covariance: Array<DoubleArray>
)

class LocationKalmanFilter(
    private val processNoise: Double = 1e-5
) {

    private var state: KalmanState? = null
    private var lastTimestamp: Long = 0L

    fun update(
        lat: Double,
        lon: Double,
        accuracy: Float,
        timestamp: Long
    ): Pair<Double, Double> {

        if (state == null) {
            state = KalmanState(
                lat, lon, 0.0, 0.0,
                Array(4) { i -> DoubleArray(4) { if (i == it) 1.0 else 0.0 } }
            )
            lastTimestamp = timestamp
            return lat to lon
        }

        val dt = (timestamp - lastTimestamp) / 1000.0
        lastTimestamp = timestamp

        predict(dt)
        correct(lat, lon, accuracy.toDouble())

        return state!!.lat to state!!.lon
    }

    private fun predict(dt: Double) {
        val s = state!!

        // 状态预测
        s.lat += s.vLat * dt
        s.lon += s.vLon * dt

        val F = arrayOf(
            doubleArrayOf(1.0, 0.0, dt, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, dt),
            doubleArrayOf(0.0, 0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0)
        )

        val Q = Array(4) { i ->
            DoubleArray(4) { j ->
                if (i == j) processNoise else 0.0
            }
        }

        s.covariance = matAdd(matMul(matMul(F, s.covariance), transpose(F)), Q)
    }

    private fun correct(measLat: Double, measLon: Double, accuracy: Double) {
        val s = state!!

        val H = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0)
        )

        val R = arrayOf(
            doubleArrayOf(accuracy * accuracy, 0.0),
            doubleArrayOf(0.0, accuracy * accuracy)
        )

        val z = doubleArrayOf(measLat, measLon)
        val x = doubleArrayOf(s.lat, s.lon, s.vLat, s.vLon)

        val y = vecSub(z, matVecMul(H, x))
        val S = matAdd(matMul(matMul(H, s.covariance), transpose(H)), R)
        val K = matMul(matMul(s.covariance, transpose(H)), inverse2x2(S))

        val xNew = vecAdd(x, matVecMul(K, y))
        s.lat = xNew[0]
        s.lon = xNew[1]
        s.vLat = xNew[2]
        s.vLon = xNew[3]

        val I = Array(4) { i -> DoubleArray(4) { if (i == it) 1.0 else 0.0 } }
        s.covariance = matMul(matSub(I, matMul(K, H)), s.covariance)
    }
    private fun matMul(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val res = Array(a.size) { DoubleArray(b[0].size) }
        for (i in a.indices)
            for (j in b[0].indices)
                for (k in b.indices)
                    res[i][j] += a[i][k] * b[k][j]
        return res
    }

    private fun matVecMul(a: Array<DoubleArray>, x: DoubleArray): DoubleArray {
        return DoubleArray(a.size) { i ->
            a[i].indices.sumOf { j -> a[i][j] * x[j] }
        }
    }

    private fun transpose(a: Array<DoubleArray>): Array<DoubleArray> {
        return Array(a[0].size) { i ->
            DoubleArray(a.size) { j -> a[j][i] }
        }
    }

    private fun matAdd(a: Array<DoubleArray>, b: Array<DoubleArray>) =
        Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] + b[i][j] } }

    private fun matSub(a: Array<DoubleArray>, b: Array<DoubleArray>) =
        Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] - b[i][j] } }

    private fun vecAdd(a: DoubleArray, b: DoubleArray) =
        DoubleArray(a.size) { i -> a[i] + b[i] }

    private fun vecSub(a: DoubleArray, b: DoubleArray) =
        DoubleArray(a.size) { i -> a[i] - b[i] }

    private fun inverse2x2(m: Array<DoubleArray>): Array<DoubleArray> {
        val det = m[0][0] * m[1][1] - m[0][1] * m[1][0]
        return arrayOf(
            doubleArrayOf(m[1][1] / det, -m[0][1] / det),
            doubleArrayOf(-m[1][0] / det, m[0][0] / det)
        )
    }
}

