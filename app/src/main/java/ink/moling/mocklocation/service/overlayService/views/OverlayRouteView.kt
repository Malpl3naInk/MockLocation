package ink.moling.mocklocation.service.overlayService.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverlayRouteView(
    currentSpeedPer: Float,
    currentSpeedPreset: Double,
    onCurrentSpeedChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
    ) {
        Row {
            Text(
                "Movement speed",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                "%.2f m/s".format(
                    currentSpeedPer * currentSpeedPreset
                ),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Slider(
            value = currentSpeedPer,
            onValueChange = onCurrentSpeedChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSecondary
            )
        )

        Spacer(Modifier.height(8.dp))


        Row {
            Text(
                "Maximum speed preset",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                "%.2f m/s (Max)".format(
                    currentSpeedPreset
                ),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}