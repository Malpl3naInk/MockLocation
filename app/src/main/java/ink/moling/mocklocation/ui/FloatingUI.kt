package ink.moling.mocklocation.ui

import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.service.joystickService.state.JoystickStateHolder
import ink.moling.mocklocation.utils.azimuthToDirection
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val PI: Float = Math.PI.toFloat()

@Composable
fun JoystickControl(
    modifier: Modifier = Modifier,
    joystickSize: Float = 120f,
    onMove: (direction: Float, speed: Float) -> Unit = { _, _ -> }
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    Canvas(
        modifier = modifier
            .size(joystickSize.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // 松手后回到中心
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
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

@Composable
fun FloatingUI(
    windowManager: WindowManager,
    composeView: ComposeView,
    params: WindowManager.LayoutParams
) {
    // 累积浮点数偏移量，避免丢失小数部分
    val offsetX = remember { mutableListOf(0f) }
    val offsetY = remember { mutableListOf(0f) }
    
    // 摇杆状态
    var joystickDirection by remember { mutableFloatStateOf(0f) }
    var joystickSpeed by remember { mutableFloatStateOf(0f) }

    var angle = 0
    var speedPercent = 0
    
    Row(
        modifier = Modifier
            .width(168.dp)
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        Column {
            // 拖动区域
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()  // 消费事件，防止穿透

                            // 累积浮点数偏移量
                            offsetX[0] += dragAmount.x
                            offsetY[0] += dragAmount.y

                            // 计算整数偏移量
                            val deltaX = offsetX[0].roundToInt()
                            val deltaY = offsetY[0].roundToInt()

                            // 只有当累积的偏移量达到整数像素时才更新
                            if (deltaX != 0 || deltaY != 0) {
                                params.x += deltaX
                                params.y += deltaY
                                windowManager.updateViewLayout(composeView, params)

                                // 减去已经应用的整数偏移量，保留小数部分
                                offsetX[0] -= deltaX
                                offsetY[0] -= deltaY
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.OpenWith,
                    contentDescription = "Drag handle",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 菜单
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
                    .clickable(
                        onClick = {

                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Joystick menu",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 信息显示区域
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (joystickSpeed > 0) {
                    angle = Math.toDegrees(joystickDirection.toDouble()).toInt()
                    speedPercent = (joystickSpeed * 100).toInt()
                    Log.d("FloatingUI", "angle=$angle")
                }
                Text(
                    "${azimuthToDirection(angle)} ${angle}° Spd: ${speedPercent}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 摇杆控制
            JoystickControl(
                onMove = { direction, speed ->
                    joystickDirection = direction
                    joystickSpeed = speed
                    // 更新全局摇杆状态，供 StaticPointSimulator 使用
                    // 注意：direction 是弧度，直接传递，不要转换成度数
                    JoystickStateHolder.update(direction, speed)
                    Log.d("FloatingUI", "direction(rad)=$direction, speed=$speed")
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}