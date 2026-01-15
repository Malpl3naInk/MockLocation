package ink.moling.mocklocation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingUI() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(Color.Black.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("悬浮", color = Color.White, fontSize = 12.sp)
    }
}