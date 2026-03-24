package ink.moling.mocklocation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 圆形边框图标按钮
 *
 * @param onClick 点击回调
 * @param icon 图标
 * @param tint 图标/边框颜色
 * @param modifier 额外修饰符
 * @param contentDescription 内容描述
 */
@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .size(42.dp)
            .border(BorderStroke(2.dp, tint), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint
            )
        }
    }
}

/**
 * 圆形边框图标按钮 - 使用 MaterialTheme 次要颜色
 */
@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    CircleIconButton(
        onClick = onClick,
        icon = icon,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = modifier,
        contentDescription = contentDescription
    )
}
