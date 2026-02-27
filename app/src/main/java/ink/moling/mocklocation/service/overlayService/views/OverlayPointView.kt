package ink.moling.mocklocation.service.overlayService.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.ui.components.JoystickControl
import ink.moling.mocklocation.utils.azimuthToDirection

@Composable
fun OverlayPointView(
    currentSpeedPer: Float,
    joystickDirection: Float,
    currentSpeedPreset: Double,
    joystickLocked: Boolean,
    onJoystickMove: (direction: Float, speed: Float) -> Unit
) {
    val context = LocalContext.current
    // 信息显示区域
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val angle = Math.toDegrees(joystickDirection.toDouble()).toInt()
        val realSpeed = (currentSpeedPreset * currentSpeedPer)
        Text(
            "${azimuthToDirection(angle)} ${angle}° ${"%.2f".format(realSpeed)}m/s",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }

    Spacer(Modifier.height(8.dp))

    // 摇杆控制
    JoystickControl(
        joystickSize = PrefsHelper.getJoystickSize(context),
        locked = joystickLocked,
        onMove = onJoystickMove
    )
}