package ink.moling.mocklocation.ui

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun FloatingUI(
    windowManager: WindowManager,
    composeView: ComposeView,
    params: WindowManager.LayoutParams
) {
    // 累积浮点数偏移量，避免丢失小数部分
    val offsetX = remember { mutableListOf(0f) }
    val offsetY = remember { mutableListOf(0f) }
    
    Box(
        modifier = Modifier
            .height(112.dp)
            .width(224.dp)
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // 中间的文字
        Text("悬浮", color = Color.White, fontSize = 12.sp)
        
        // 左上角的拖动区域
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopStart)
                .background(Color.White.copy(alpha = 0.1f))
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
    }
}