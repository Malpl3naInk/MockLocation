package ink.moling.mocklocation.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PillSelection (
    val icon: ImageVector,
    val text: String? = null
)

@Composable
fun PillSelector(
    items: List<PillSelection>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    enabled: Boolean = true,
    selectedBackground: Color = MaterialTheme.colorScheme.primary,
    iconSize: Dp = 24.dp,
    contentSpacing: Dp = 16.dp,
    contentPadding: Dp = 4.dp
) {
    val itemWidths = remember(items) { mutableStateListOf<Int>() }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .wrapContentWidth()  // 紧凑包裹内容
            .height(iconSize + contentPadding * 4)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(contentPadding)
    ) {
        val animatedOffset by animateDpAsState(
            targetValue = with(density) {
                itemWidths.take(selectedIndex).sum().toDp()
            },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = ""
        )

        val indicatorWidth = with(density) {
            itemWidths.getOrNull(selectedIndex)?.toDp() ?: 0.dp
        }

        // 选中背景
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(selectedBackground)
        )

        Row {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .onSizeChanged { size ->
                            if (itemWidths.size <= index) {
                                itemWidths.add(size.width)
                            } else {
                                itemWidths[index] = size.width
                            }
                        }
                        .clickable(
                            enabled = enabled,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSelectedChange(index) }
                        .padding(horizontal = contentSpacing, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize),
                            tint = if (index == selectedIndex)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.text != null) {
                            Text(
                                text = item.text,
                                color = if (index == selectedIndex)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

