package ink.moling.mocklocation.service.overlayService

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.service.overlayService.state.OverlayStateHolder
import ink.moling.mocklocation.service.overlayService.views.OverlayPointView
import ink.moling.mocklocation.service.overlayService.views.OverlayRouteView
import ink.moling.mocklocation.utils.MockMode
import kotlin.math.roundToInt

@Composable
fun OverlayScreen(
    windowManager: WindowManager,
    composeView: ComposeView,
    params: WindowManager.LayoutParams
) {
    val context = LocalContext.current
    val overlayState = OverlayStateHolder.state.collectAsState().value

    // 累积浮点数偏移量，避免丢失小数部分
    val offsetX = remember { mutableListOf(0f) }
    val offsetY = remember { mutableListOf(0f) }

    // 悬浮窗最小化
    var isOverlayMinimized  by remember { mutableStateOf(false) }
    // 悬浮窗菜单展开
    var isOverlayMenuExpanded by remember { mutableStateOf(false) }

    // 模拟位置移动速度预设
    val currentSpeedPresets: List<Double> = PrefsHelper.getMaxSpeedPresets(context)
    var selectedSpeedPreset   by remember { mutableIntStateOf(0) }
    // 当前速度百分比
    var currentSpeedPer       by remember { mutableFloatStateOf(0f) }
    // 摇杆状态
    var joystickDirection   by remember { mutableFloatStateOf(0f) }
    var joystickLocked      by remember { mutableStateOf(true) }
    // 随 Selected preset 修改 State holder 中的最大速度
    LaunchedEffect(selectedSpeedPreset) {
        if (overlayState.mockMode == MockMode.MOCK_MODE_POINT) {
            OverlayStateHolder.update(
                overlayState.direction,
                overlayState.speed,
                currentSpeedPresets[selectedSpeedPreset]
            )
        } else {
            OverlayStateHolder.update(
                overlayState.speed,
                currentSpeedPresets[selectedSpeedPreset]
            )
        }
    }
    
    Row(
        modifier = Modifier
            .background(
                Color.Black.copy(
                    alpha = if (isOverlayMinimized) 0.1f else 0.7f
                ),
                shape = if (isOverlayMinimized) RoundedCornerShape(24.dp) else RectangleShape
            )
    ) {
        Column {
            // 拖动区域
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    }
                    .clickable(
                        onClick = { isOverlayMinimized = !isOverlayMinimized },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isOverlayMinimized) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Drag handle",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(72.dp)
                    )
                } else {
                    Icon(
                        Icons.Outlined.OpenWith,
                        contentDescription = "Drag handle",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (!isOverlayMinimized) {
                // 摇杆锁定
                if (overlayState.mockMode == MockMode.MOCK_MODE_POINT) {
                    // 模式为点位模拟时显示摇杆锁定按钮
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                }
                // 菜单
                Box {
                    // 菜单按钮
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                onClick = { isOverlayMenuExpanded = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Overlay menu",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = isOverlayMenuExpanded,
                        onDismissRequest = { isOverlayMenuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        offset = DpOffset(x = 10.dp, y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hide Overlay") },
                            onClick =  {
                                isOverlayMenuExpanded = false
                                val intent = android.content.Intent(
                                    ink.moling.mocklocation.service.locationService.controller.ACTION_TOGGLE_OVERLAY
                                ).apply {
                                    setPackage(context.packageName)
                                }
                                context.sendBroadcast(intent)
                            }
                        )
                    }
                }
            }
        }

        if (!isOverlayMinimized) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                when (overlayState.mockMode) {
                    MockMode.MOCK_MODE_POINT -> OverlayPointView(
                        currentSpeedPer,
                        joystickDirection,
                        currentSpeedPresets[selectedSpeedPreset],
                        joystickLocked
                    ) { direction, speed ->
                        joystickDirection = direction
                        currentSpeedPer = speed
                        // 更新全局摇杆状态，供 StaticPointSimulator 使用
                        // 注意：direction 是弧度，直接传递，不要转换成度数
                        OverlayStateHolder.update(
                            direction,
                            speed,
                            currentSpeedPresets[selectedSpeedPreset]
                        )
                    }
                    MockMode.MOCK_MODE_ROUTE -> OverlayRouteView(
                        currentSpeedPer,
                        currentSpeedPresets[selectedSpeedPreset]
                    ) { speed ->
                        currentSpeedPer = speed
                        // 更新全局摇杆状态，供 DynamicRouteSimulator 使用
                        OverlayStateHolder.update(
                            speed,
                            currentSpeedPresets[selectedSpeedPreset]
                        )
                    }
                }

                Row {
                    IconButton(onClick = {
                        if (overlayState.mockMode == MockMode.MOCK_MODE_ROUTE) {
                            val currentSpeed =
                                currentSpeedPer * currentSpeedPresets[selectedSpeedPreset]
                            val newPreset = currentSpeedPresets[0]
                            currentSpeedPer = if (currentSpeed >= newPreset) {
                                1f
                            } else {
                                (currentSpeed / newPreset).toFloat()
                            }
                        }
                        selectedSpeedPreset = 0
                    }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.DirectionsWalk,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = {
                        if (overlayState.mockMode == MockMode.MOCK_MODE_ROUTE) {
                            val currentSpeed =
                                currentSpeedPer * currentSpeedPresets[selectedSpeedPreset]
                            val newPreset = currentSpeedPresets[1]
                            currentSpeedPer = if (currentSpeed >= newPreset) {
                                1f
                            } else {
                                (currentSpeed / newPreset).toFloat()
                            }
                        }
                        selectedSpeedPreset = 1
                    }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.DirectionsRun,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = {
                        if (overlayState.mockMode == MockMode.MOCK_MODE_ROUTE) {
                            val currentSpeed =
                                currentSpeedPer * currentSpeedPresets[selectedSpeedPreset]
                            val newPreset = currentSpeedPresets[2]
                            currentSpeedPer = if (currentSpeed >= newPreset) {
                                1f
                            } else {
                                (currentSpeed / newPreset).toFloat()
                            }
                        }
                        selectedSpeedPreset = 2
                    }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.DirectionsBike,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}