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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.data.models.PointType
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.data.models.RoutePoint
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

/**
 * PointType 的显示颜色配置
 */
fun PointType.getColor(): Color = when (this) {
    PointType.R -> Color(0xFF2196F3)  // 蓝色 - Running
    PointType.W -> Color(0xFFFF9800)  // 橙色 - Walking
    PointType.L -> Color(0xFF4CAF50)  // 绿色 - Looping
}

/**
 * 根据字符串类型推断 PointType
 */
fun typeToPointType(type: String?): PointType {
    return when {
        type.isNullOrEmpty() -> PointType.R
        type.startsWith("R", ignoreCase = true) -> PointType.R
        type.startsWith("W", ignoreCase = true) -> PointType.W
        type.startsWith("L", ignoreCase = true) -> PointType.L
        else -> PointType.R
    }
}

/**
 * RoutePoint 扩展函数：根据 type 字符串获取点类型
 */
fun RoutePoint.getPointType(): PointType = typeToPointType(type)

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

private fun computeBounds(points: List<RoutePoint>): Bounds {
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
    points: List<RoutePoint>,
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
    routeObject: RouteObject? = null,
    pointRadius: Dp = 3.dp,
    pointColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    strokeWidthDp: Dp = 1.dp,
    paddingDp: Dp = 8.dp,                                               // 绘图 Padding
    cardPadding: Dp = 16.dp,                                            // Card 内部 Padding
    highlightPointId: Int? = null,                                      // 需要高亮的点的 ID（null 表示无高亮）
    highlightBorderColor: Color = MaterialTheme.colorScheme.outline,    // 高亮边框颜色
    highlightBorderWidth: Dp = 1.dp,                                    // 高亮边框宽度
    currentLocation: Pair<Double, Double>? = null,                      // 当前位置 (lat, lng)
    currentLocationColor: Color = Color(0xFFFF5722),             // 当前位置点颜色
    currentLocationRadius: Dp = 3.dp,                                   // 当前位置点半径
    onPointClick: ((index: Int, point: RoutePoint) -> Unit)? = null
) {
    val density = LocalDensity.current
    val prPx = with(density) { pointRadius.toPx() }
    val strokePx = with(density) { strokeWidthDp.toPx() }
    val paddingPx = with(density) { paddingDp.toPx() }
    val highlightBorderPx = with(density) { highlightBorderWidth.toPx() }
    val currentLocationPx = with(density) { currentLocationRadius.toPx() }

    // 从 RouteObject 获取点列表
    val points = routeObject?.points ?: emptyList()
    
    val bounds by remember(points) { derivedStateOf { computeBounds(points) } }
    
    // 创建 ID 到索引的映射，用于通过 ID 查找点
    val idToIndex by remember(points) { derivedStateOf { 
        points.mapIndexed { index, point -> point.id to index }.toMap()
    } }


    Box(
        modifier = modifier
            .padding(cardPadding)
            .clipToBounds()
            .pointerInput(routeObject) {
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
            val hasHighlight = highlightPointId != null
            
            // 获取高亮点的连接关系
            val highlightedConnections = if (hasHighlight) {
                points.find { it.id == highlightPointId }?.connects?.toSet() ?: emptySet()
            } else {
                emptySet()
            }
            
            for (i in points.indices) {
                val startPoint = points[i]
                val start = mapped[i]
                val startType = startPoint.getPointType()
                
                for (targetId in startPoint.connects) {
                    // 通过 ID 查找目标点的索引
                    val targetIndex = idToIndex[targetId] ?: continue
                    if (targetIndex !in mapped.indices) continue
                    
                    val endPoint = points[targetIndex]
                    val end = mapped[targetIndex]
                    val endType = endPoint.getPointType()
                    
                    // 只有两端点类型相同时使用类型颜色，否则使用默认颜色（R）
                    val baseLineColor = if (startType == endType) {
                        startType.getColor()
                    } else {
                        PointType.R.getColor()
                    }
                    
                    // 如果有高亮点，判断该连接线是否与高亮点相关
                    val lineColor = if (hasHighlight) {
                        val isRelatedToHighlight = startPoint.id == highlightPointId || 
                                                   endPoint.id == highlightPointId
                        if (isRelatedToHighlight) baseLineColor else baseLineColor.copy(alpha = 0.2f)
                    } else {
                        baseLineColor
                    }
                    
                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        lineTo(end.x, end.y)
                    }
                    drawPath(path, lineColor, style = Stroke(strokePx))
                }
            }

            // 点（先绘制高亮边框，再绘制点本身）
            for (i in mapped.indices) {
                val pt = mapped[i]
                val pointId = points[i].id
                // 判断该点是否应该高亮（是高亮点本身或其连接的点）
                val isHighlighted = pointId == highlightPointId || pointId in highlightedConnections
                
                // 绘制当前选择点边框
                if (isHighlighted && pointId == highlightPointId) {
                    drawCircle(
                        color = highlightBorderColor,
                        radius = prPx + highlightBorderPx + 3,
                        center = pt,
                        style = Stroke(width = highlightBorderPx)
                    )
                }
                
                // 绘制点本身，如果有高亮且当前点不需要高亮，降低亮度
                val finalPointColor = if (hasHighlight && !isHighlighted) {
                    pointColor.copy(alpha = 0.2f)
                } else {
                    pointColor
                }
                drawCircle(finalPointColor, prPx, pt)
            }

            // 绘制当前位置
            currentLocation?.let { (lat, lng) ->
                val m = latLngToMercator(lat, lng)
                
                // 计算当前位置在画布上的原始坐标
                val availW = (size.width - paddingPx * 2).coerceAtLeast(0f)
                val availH = (size.height - paddingPx * 2).coerceAtLeast(0f)
                
                val xRange = (bounds.maxLng - bounds.minLng).toFloat().coerceAtLeast(1e-12f)
                val yRange = (bounds.maxLat - bounds.minLat).toFloat().coerceAtLeast(1e-12f)
                
                val scale = min(availW / xRange, availH / yRange)
                val drawW = xRange * scale
                val drawH = yRange * scale
                
                val offsetX = paddingPx + (availW - drawW) / 2f
                val offsetY = paddingPx + (availH - drawH) / 2f
                
                var x = ((m.x - bounds.minLng).toFloat() * scale) + offsetX
                var y = (drawH - (m.y - bounds.minLat).toFloat() * scale) + offsetY
                
                // 计算绘制区域的边界
                val minX = paddingPx
                val maxX = size.width - paddingPx
                val minY = paddingPx
                val maxY = size.height - paddingPx
                
                // 如果当前位置超出边界，将其限制在边框上
                x = x.coerceIn(minX, maxX)
                y = y.coerceIn(minY, maxY)
                
                val currentPos = Offset(x, y)
                
                // 绘制当前位置点（外圈白色边框，内圈颜色）
                drawCircle(
                    color = Color.White,
                    radius = currentLocationPx + 2f,
                    center = currentPos
                )
                drawCircle(
                    color = currentLocationColor,
                    radius = currentLocationPx,
                    center = currentPos
                )
            }
        }
    }
}
