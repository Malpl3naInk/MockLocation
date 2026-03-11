package ink.moling.mocklocation.activity.settings.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import kotlin.math.sin

private const val PREVIEW_POINTS = 120
private const val MAX_SLIDER = 20.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingRandomOffset(
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current

    var maxOffset by remember { mutableDoubleStateOf(PrefsHelper.getMaxRandomOffset(context)) }
    LaunchedEffect(maxOffset) {
        PrefsHelper.setMaxRandomOffset(context, maxOffset)
    }

    // 动画进度：驱动偏移路径的平移，使预览看起来像在移动
    val animOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            animOffset.animateTo(
                targetValue = animOffset.value + PREVIEW_POINTS,
                animationSpec = tween(durationMillis = 6000)
            )
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            stringResource(R.string.settings_random_offset_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, outlineColor),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                val anim = animOffset.value
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f
                    // 原始路径（水平直线）
                    drawLine(
                        color = primaryColor,
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // 偏移路径：平滑随机游走，振幅映射到 canvas 高度
                    val maxPx = (maxOffset / MAX_SLIDER * (h / 2f - 8.dp.toPx())).toFloat()
                    val offsetPath = Path()
                    var smoothY = 0f
                    val step = (maxOffset * 0.15).toFloat().coerceAtLeast(0.05f)

                    for (i in 0..PREVIEW_POINTS) {
                        val phase = (i + anim) * 0.18f
                        // 用多个正弦叠加模拟平滑随机漂移
                        val drift = (sin(phase) * 0.6f +
                                sin(phase * 1.7f + 1.2f) * 0.3f +
                                sin(phase * 3.1f + 2.4f) * 0.1f)
                        smoothY = drift * maxPx
                        val x = i / PREVIEW_POINTS.toFloat() * w
                        if (i == 0) offsetPath.moveTo(x, midY + smoothY)
                        else offsetPath.lineTo(x, midY + smoothY)
                    }
                    drawPath(
                        path = offsetPath,
                        color = secondaryColor,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 起点终点标记
                    drawCircle(primaryColor, radius = 5.dp.toPx(), center = Offset(0f, midY))
                    drawCircle(primaryColor, radius = 5.dp.toPx(), center = Offset(w, midY))
                }
            }
        }

        // 图例
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(24.dp, 12.dp)) {
                drawLine(primaryColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 3.dp.toPx())
            }
            Text(
                stringResource(R.string.settings_random_offset_legend_original),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp),
                color = primaryColor
            )
            Canvas(modifier = Modifier.size(24.dp, 12.dp)) {
                drawLine(secondaryColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2.dp.toPx())
            }
            Text(
                stringResource(R.string.settings_random_offset_legend_offset),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp),
                color = secondaryColor
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                modifier = Modifier.padding(12.dp).weight(1f),
                value = maxOffset.toFloat(),
                onValueChange = { maxOffset = it.toDouble() },
                valueRange = 0f..MAX_SLIDER.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            Text(
                text = "%.1fm".format(maxOffset),
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(42.dp)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { maxOffset = 8.0 },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Undo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
