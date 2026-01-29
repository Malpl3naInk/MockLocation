package ink.moling.mocklocation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

private const val PI: Float = Math.PI.toFloat()

@Composable
fun JoystickControl(
    modifier: Modifier = Modifier,
    joystickSize: Float = 120f,
    locked: Boolean = true,
    onMove: (direction: Float, speed: Float) -> Unit = { _, _ -> }
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // 当取消锁定时重置摇杆位置
    LaunchedEffect(locked) {
        if (!locked) {
            offsetX = 0f
            offsetY = 0f
            onMove(0f, 0f)
        }
    }

    Canvas(
        modifier = modifier
            .size(joystickSize.dp)
            .pointerInput(locked) {
                detectDragGestures(
                    onDragEnd = {
                        // 如果没有锁定则松手后回到中心
                        if (!locked) {
                            offsetX = 0f
                            offsetY = 0f
                            onMove(0f, 0f)
                        }
                    },
                    onDragCancel = {
                        // 如果没有锁定则拖动取消时也回到中心
                        if (!locked) {
                            offsetX = 0f
                            offsetY = 0f
                            onMove(0f, 0f)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // 更新偏移量
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        // 限制在圆形范围内（使用密度转换dp到像素）
                        val canvasRadiusPx = (joystickSize / 2 * density) - 40f // 留出内圆的空间
                        val distance = sqrt(offsetX * offsetX + offsetY * offsetY)

                        if (distance > canvasRadiusPx) {
                            val ratio = canvasRadiusPx / distance
                            offsetX *= ratio
                            offsetY *= ratio
                        }

                        // 计算方向（角度，单位：弧度）和速度（0-1）
                        // 角度按照指南针方位角计算：北=0, 东=π/2, 南=π, 西=3π/2
                        var direction = atan2(offsetX.toDouble(), -offsetY.toDouble()).toFloat()
                        if (direction < 0) direction += PI * 2  // 将负角度转换为 [0, 2π) 范围
                        val speed = min(distance / canvasRadiusPx, 1f)

                        onMove(direction, speed)
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val outerRadius = min(canvasWidth, canvasHeight) / 2 - 15f
        val innerRadius = 25f

        // 绘制外圆（背景）
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = outerRadius,
            center = Offset(centerX, centerY)
        )

        // 绘制外圆边框
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = outerRadius,
            center = Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )

        // 绘制中心点标记
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = 5f,
            center = Offset(centerX, centerY)
        )

        // 绘制十字定位线
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset( centerX - outerRadius, centerY),
            end = Offset(centerX + outerRadius, centerY),
            strokeWidth = 3f
        )

        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset( centerX, centerY - outerRadius),
            end = Offset(centerX, centerY + outerRadius),
            strokeWidth = 3f
        )

        // 绘制摇杆（内圆）
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = innerRadius,
            center = Offset(centerX + offsetX, centerY + offsetY)
        )

        // 绘制摇杆边框
        drawCircle(
            color = Color.White,
            radius = innerRadius,
            center = Offset(centerX + offsetX, centerY + offsetY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}