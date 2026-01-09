package ink.moling.mocklocation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 支持长按删除的 DropdownMenuItem 包装器
 */
@Composable
fun LongPressDeleteMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    deleteDuration: Int = 1200
) {
    val progress = remember { Animatable(0f) }
    var pressing by remember { mutableStateOf(false) }

    LaunchedEffect(pressing) {
        if (pressing) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(deleteDuration)
            )
            onDelete()
        } else {
            progress.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        pressing = true
                    },
                    onPress = {
                        try {
                            tryAwaitRelease()
                        } finally {
                            pressing = false
                        }
                    }
                )
            }
    ) {
        // 原始内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            text()
        }

        // 删除进度覆盖层（使用嵌套 Box 来正确处理高度和进度）
        Box(
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .fillMaxHeight()
                    .background(
                        Color(0xFFFF5252).copy(alpha = progress.value * 0.8f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White.copy(alpha = progress.value),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}

