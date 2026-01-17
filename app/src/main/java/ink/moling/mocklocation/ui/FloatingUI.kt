package ink.moling.mocklocation.ui

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingUI(
    windowManager: WindowManager,
    composeView: ComposeView,
    params: WindowManager.LayoutParams
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()  // 消费事件，防止穿透
                    params.x += dragAmount.x.toInt()
                    params.y += dragAmount.y.toInt()
                    windowManager.updateViewLayout(composeView, params)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text("悬浮", color = Color.White, fontSize = 12.sp)
    }
}