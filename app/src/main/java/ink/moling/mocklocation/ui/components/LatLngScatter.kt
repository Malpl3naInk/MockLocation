package ink.moling.mocklocation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import ink.moling.mocklocation.data.models.LatLng
import kotlin.math.*

/**
 * 点类型枚举，用于区分不同路径类型
 */
enum class PointType(val color: Color) {
    RUNNING(Color(0xFF2196F3)),      // 蓝色 - 普通路径
    WALKING(Color(0xFFFF9800)),      // 绿色 - 步行路径
    LOOPING(Color(0xFF4CAF50))       // 橙色 - 场地路径
}

/**
 * 根据 label 值推断点类型
 * 可根据实际需求自定义映射规则
 */
fun typeToPointType(type: String?): PointType {
    return when {
        type == null -> PointType.RUNNING
        // 根据 label 的值或特征判断类型：
        type.startsWith("R") || type.startsWith("r") -> PointType.RUNNING
        type.startsWith("W") || type.startsWith("w") -> PointType.WALKING
        type.startsWith("L") || type.startsWith("l") -> PointType.LOOPING
        else -> PointType.RUNNING
    }
}

/**
 * LatLng 扩展函数：根据 label 获取点类型
 */
fun LatLng.getPointType(): PointType = typeToPointType(type)

/* ---------------- Web Mercator 投影 ---------------- */

private data class MercatorPoint(val x: Double, val y: Double)

private fun latLngToMercator(lat: Double, lng: Double): MercatorPoint {
    val r = 6378137.0
    val maxLat = 85.05112878
    val clampedLat = lat.coerceIn(-maxLat, maxLat)

    val x = Math.toRadians(lng) * r
    val y = ln(tan(Math.PI / 4 + Math.toRadians(clampedLat) / 2)) * r

    return MercatorPoint(x, y)
}

/* ---------------- Bounds 以 Mercator 为单位 ---------------- */
private data class Bounds(
    val minLat: Double, val maxLat: Double,
    val minLng: Double, val maxLng: Double
)

private fun computeBounds(points: List<LatLng>): Bounds {
    if (points.isEmpty()) return Bounds(0.0, 0.0, 0.0, 0.0)

    var minX = Double.POSITIVE_INFINITY
    var maxX = Double.NEGATIVE_INFINITY
    var minY = Double.POSITIVE_INFINITY
    var maxY = Double.NEGATIVE_INFINITY

    for (p in points) {
        val m = latLngToMercator(p.lat, p.lng)
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
        minLng = minX, maxLng = maxX
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

    val xRange = (bounds.maxLng - bounds.minLng).toFloat().coerceAtLeast(1e-12f)
    val yRange = (bounds.maxLat - bounds.minLat).toFloat().coerceAtLeast(1e-12f)

    // 等比缩放（保证不拉伸）
    val scale = min(availW / xRange, availH / yRange)

    val drawW = xRange * scale
    val drawH = yRange * scale

    // 居中偏移（墨卡托）
    val offsetX = paddingPx + (availW - drawW) / 2f
    val offsetY = paddingPx + (availH - drawH) / 2f

    return points.map { p ->
        val m = latLngToMercator(p.lat, p.lng)

        val x = ((m.x - bounds.minLng).toFloat() * scale) + offsetX
        val y = (drawH - (m.y - bounds.minLat).toFloat() * scale) + offsetY

        Offset(x, y)
    }
}

/* ---------------- 主控件 ---------------- */
@Composable
fun LatLngScatter(
    modifier: Modifier = Modifier,
    points: SnapshotStateList<LatLng> = mutableStateListOf(),
    pointRadius: Dp = 6.dp,
    pointColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    strokeWidthDp: Dp = 1.dp,
    paddingDp: Dp = 8.dp,            // 绘图 Padding
    cardPadding: Dp = 16.dp,         // Card 内部 Padding
    onPointClick: ((index: Int, point: LatLng) -> Unit)? = null
) {
    val density = LocalDensity.current
    val prPx = with(density) { pointRadius.toPx() }
    val strokePx = with(density) { strokeWidthDp.toPx() }
    val paddingPx = with(density) { paddingDp.toPx() }

    val bounds by remember { derivedStateOf { computeBounds(points) } }


    Box(
        modifier = modifier
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

            // 连接线（只有两端点类型相同时才使用类型颜色，否则使用默认颜色）
            for (i in points.indices) {
                val startPoint = points[i]
                val start = mapped[i]
                val startType = startPoint.getPointType()
                
                for (target in startPoint.connections) {
                    if (target !in mapped.indices) continue
                    val endPoint = points[target]
                    val end = mapped[target]
                    val endType = endPoint.getPointType()
                    
                    // 只有两端点类型相同时使用类型颜色，否则使用默认颜色（RUNNING）
                    val lineColor = if (startType == endType) {
                        startType.color
                    } else {
                        PointType.RUNNING.color
                    }
                    
                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        lineTo(end.x, end.y)
                    }
                    drawPath(path, lineColor, style = Stroke(strokePx))
                }
            }

            // 点
            for (pt in mapped) {
                drawCircle(pointColor, prPx, pt)
            }
        }
    }
}
