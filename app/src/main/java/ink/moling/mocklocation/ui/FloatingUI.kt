package ink.moling.mocklocation.ui

import android.view.WindowManager
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.service.joystickService.state.JoystickStateHolder
import ink.moling.mocklocation.ui.components.JoystickControl
import ink.moling.mocklocation.utils.azimuthToDirection
import kotlin.math.roundToInt

@Composable
fun FloatingUI(
    windowManager: WindowManager,
    composeView: ComposeView,
    params: WindowManager.LayoutParams
) {
    val joystickState = JoystickStateHolder.state.collectAsState().value

    // 累积浮点数偏移量，避免丢失小数部分
    val offsetX = remember { mutableListOf(0f) }
    val offsetY = remember { mutableListOf(0f) }
    
    // 摇杆状态
    var joystickDirection by remember { mutableFloatStateOf(0f) }
    var joystickSpeed by remember { mutableFloatStateOf(0f) }
    var joystickLocked by remember { mutableStateOf(true) }

    var angle = 0
    var speedPercent = 0
    var realSpeed = 0.0
    
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
                    .clickable(onClick = { joystickLocked = !joystickLocked }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (joystickLocked) Icons.Filled.Lock else Icons.Outlined.LockOpen,
                    contentDescription = "Lock joystick",
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
                    realSpeed = (joystickState.maxSpeed * joystickSpeed)
                }
                Text(
                    "${azimuthToDirection(angle)} ${angle}° ${"%.2f".format(realSpeed)}m/s",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 摇杆控制
            JoystickControl(
                locked = joystickLocked,
                onMove = { direction, speed ->
                    joystickDirection = direction
                    joystickSpeed = speed
                    // 更新全局摇杆状态，供 StaticPointSimulator 使用
                    // 注意：direction 是弧度，直接传递，不要转换成度数
                    JoystickStateHolder.update(direction, speed)
                }
            )
        }
    }
}