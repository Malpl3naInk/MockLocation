package ink.moling.mocklocation.utils

data class KalmanState(
    var lat: Double,
    var lng: Double,
    var vLat: Double,
    var vLng: Double,
    var covariance: Array<DoubleArray>
)

class KalmanFilter(
    private val processNoise: Double = 1e-5
) {

    private var state: KalmanState? = null
    private var lastTimestamp: Long = 0L

    /**
     * Reset the filter state
     * Should be called when location tracking is interrupted or restarted
     */
    fun reset() {
        state = null
        lastTimestamp = 0L
    }

    fun update(
        lat: Double,
        lng: Double,
        accuracy: Float,
        timestamp: Long
    ): Pair<Double, Double> {

        if (state == null) {
            state = KalmanState(
                lat, lng, 0.0, 0.0,
                Array(4) { i -> DoubleArray(4) { if (i == it) 1.0 else 0.0 } }
            )
            lastTimestamp = timestamp
            return lat to lng
        }

        val dt = (timestamp - lastTimestamp) / 1000.0
        
        // Reset if time gap is too large (e.g., location tracking was paused)
        if (dt > 10.0 || dt < 0) {
            reset()
            return update(lat, lng, accuracy, timestamp)
        }
        
        lastTimestamp = timestamp

        predict(dt)
        correct(lat, lng, accuracy.toDouble())

        return state!!.lat to state!!.lng
    }

    private fun predict(dt: Double) {
        val s = state!!

        // 状态预测
        s.lat += s.vLat * dt
        s.lng += s.vLng * dt

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

    private fun correct(
        measLat: Double,
        measLng: Double,
        accuracy: Double
    ) {
        val s = state!!

        val H = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0)
        )

        val R = arrayOf(
            doubleArrayOf(accuracy * accuracy, 0.0),
            doubleArrayOf(0.0, accuracy * accuracy)
        )

        val z = doubleArrayOf(measLat, measLng)
        val x = doubleArrayOf(s.lat, s.lng, s.vLat, s.vLng)

        val y = vecSub(z, matVecMul(H, x))
        val S = matAdd(matMul(matMul(H, s.covariance), transpose(H)), R)
        val K = matMul(matMul(s.covariance, transpose(H)), inverse2x2(S))

        val xNew = vecAdd(x, matVecMul(K, y))
        s.lat = xNew[0]
        s.lng = xNew[1]
        s.vLat = xNew[2]
        s.vLng = xNew[3]

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

