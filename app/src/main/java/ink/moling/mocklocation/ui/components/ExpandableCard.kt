package ink.moling.mocklocation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.KeyboardArrowDown,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        // elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            // ==================== 标题栏（可点击） ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                // 小箭头旋转动画
                Icon(
                    imageVector = icon,
                    contentDescription = "展开/收起",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            // ==================== 可展开内容 ====================
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(tween(250)),
                exit  = fadeOut() + shrinkVertically(tween(200))

            ) {
                // 这里用 ColumnScope 直接调用传入的 content
                Column(
                    modifier = modifier
                        .background(Color.Transparent)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}
