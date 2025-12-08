package ink.moling.mocklocation.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.*

/**
 * connections: 要连接的点索引
 */
data class LatLng(
    val lat: Double,
    val lon: Double,
    val label: String? = null,
    val connections: List<Int> = emptyList()
)

/* ---------------- Web Mercator 投影 ---------------- */

private data class MercatorPoint(val x: Double, val y: Double)

private fun latLngToMercator(lat: Double, lon: Double): MercatorPoint {
    val r = 6378137.0
    val maxLat = 85.05112878
    val clampedLat = lat.coerceIn(-maxLat, maxLat)

    val x = Math.toRadians(lon) * r
    val y = ln(tan(Math.PI / 4 + Math.toRadians(clampedLat) / 2)) * r

    return MercatorPoint(x, y)
}


/* ---------------- Bounds 以 Mercator 为单位 ---------------- */
private data class Bounds(
    val minLat: Double, val maxLat: Double,
    val minLon: Double, val maxLon: Double
)

private fun computeBounds(points: List<LatLng>): Bounds {
    if (points.isEmpty()) return Bounds(0.0, 0.0, 0.0, 0.0)

    var minX = Double.POSITIVE_INFINITY
    var maxX = Double.NEGATIVE_INFINITY
    var minY = Double.POSITIVE_INFINITY
    var maxY = Double.NEGATIVE_INFINITY

    for (p in points) {
        val m = latLngToMercator(p.lat, p.lon)
        minX = min(minX, m.x)
        maxX = max(maxX, m.x)
        minY = min(minY, m.y)
        maxY = max(maxY, m.y)
    }

    if (minX == maxX) {
        minX -= 1.0
        maxX += 1.0
    }
    if (minY == maxY) {
        minY -= 1.0
        maxY += 1.0
    }

    // 注意 Bounds 的字段名沿用原来的，但值是墨卡托坐标
    return Bounds(
        minLat = minY, maxLat = maxY,
        minLon = minX, maxLon = maxX
    )
}



/* ---------------- Mercator 映射至画布 ---------------- */
private fun mapPointsToCanvasInternal(
    points: List<LatLng>,
    width: Float,
    height: Float,
    bounds: Bounds,
    paddingPx: Float
): List<Offset> {
    val availW = (width - paddingPx * 2).coerceAtLeast(0f)
    val availH = (height - paddingPx * 2).coerceAtLeast(0f)

    val xRange = (bounds.maxLon - bounds.minLon).toFloat().coerceAtLeast(1e-12f)
    val yRange = (bounds.maxLat - bounds.minLat).toFloat().coerceAtLeast(1e-12f)

    // 等比缩放（保证不拉伸）
    val scale = min(availW / xRange, availH / yRange)

    val drawW = xRange * scale
    val drawH = yRange * scale

    // 居中偏移（墨卡托）
    val offsetX = paddingPx + (availW - drawW) / 2f
    val offsetY = paddingPx + (availH - drawH) / 2f

    return points.map { p ->
        val m = latLngToMercator(p.lat, p.lon)

        val x = ((m.x - bounds.minLon).toFloat() * scale) + offsetX
        val y = (drawH - (m.y - bounds.minLat).toFloat() * scale) + offsetY

        Offset(x, y)
    }
}

/* ---------------- 主控件（名称保持 LatLonScatter 不变） ---------------- */
@Composable
fun LatLonScatter(
    points: List<LatLng>,
    pointRadius: Dp = 6.dp,
    pointColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    strokeColor: Color = Color.Black,
    strokeWidthDp: Dp = 1.dp,
    paddingDp: Dp = 8.dp,            // 绘图 Padding
    cardPadding: Dp = 16.dp,         // Card 内部 Padding
    onPointClick: ((index: Int, point: LatLng) -> Unit)? = null
) {
    val density = LocalDensity.current
    val prPx = with(density) { pointRadius.toPx() }
    val strokePx = with(density) { strokeWidthDp.toPx() }
    val paddingPx = with(density) { paddingDp.toPx() }

    val bounds = remember(points) { computeBounds(points) }

    Box(
        modifier = Modifier
            .padding(cardPadding)
            .clipToBounds()
            .pointerInput(points) {
                detectTapGestures { tapOffset ->
                    val mapped = mapPointsToCanvasInternal(
                        points, size.width.toFloat(), size.height.toFloat(), bounds, paddingPx
                    )
                    var idx = -1
                    var nearest = Float.MAX_VALUE

                    for (i in mapped.indices) {
                        val dx = mapped[i].x - tapOffset.x
                        val dy = mapped[i].y - tapOffset.y
                        val d = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        if (d < nearest && d <= prPx * 1.5f) {
                            nearest = d
                            idx = i
                        }
                    }

                    if (idx >= 0) onPointClick?.invoke(idx, points[idx])
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas

            val mapped = mapPointsToCanvasInternal(
                points, size.width, size.height, bounds, paddingPx
            )

            // 连接线
            for (i in points.indices) {
                val p = points[i]
                val start = mapped[i]
                for (target in p.connections) {
                    if (target !in mapped.indices) continue
                    val end = mapped[target]
                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        lineTo(end.x, end.y)
                    }
                    drawPath(path, strokeColor, style = Stroke(strokePx))
                }
            }

            // 点
            for (pt in mapped) {
                drawCircle(pointColor, prPx, pt)
            }
        }
    }
}
