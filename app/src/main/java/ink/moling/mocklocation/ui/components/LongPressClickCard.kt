package ink.moling.mocklocation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 支持长按触发的设置卡片组件（整合了 SettingCardNormal 的功能）
 * 只有长按到指定时长后才会触发 onClick 事件
 * 
 * @param imageVector 图标
 * @param contentDescription 图标描述
 * @param text 显示文本
 * @param onClick 长按完成后触发的回调
 * @param modifier 修饰符
 * @param pressDuration 需要按压的时长（毫秒）
 * @param progressColor 进度条颜色
 * @param enabled 是否启用长按功能
 */
@Composable
fun LongPressClickCard(
    imageVector: ImageVector,
    contentDescription: String,
    text: String,
    modifier: Modifier = Modifier,
    pressDuration: Int = 1000,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    var pressing by remember { mutableStateOf(false) }

    LaunchedEffect(pressing) {
        if (pressing && enabled) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(pressDuration)
            )
            // 长按完成，触发点击事件
            onClick()
        } else {
            // 释放时重置进度
            progress.animateTo(
                0f,
                animationSpec = tween(200)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures(
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
                }
        ) {
            // 设置卡片内容（模仿 SettingCardNormal）
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    modifier = Modifier.padding(start = 20.dp, end = 16.dp)
                )
                Text(text)
            }

            // 长按进度覆盖层
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            progressColor.copy(alpha = 0.8f * progress.value)
                        )
                )
            }
        }
    }
}
