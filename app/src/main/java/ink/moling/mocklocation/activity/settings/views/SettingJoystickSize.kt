package ink.moling.mocklocation.activity.settings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.ui.components.JoystickControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingJoystickSize(
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current

    var joystickSize by remember { mutableFloatStateOf(PrefsHelper.getJoystickSize(context)) }
    LaunchedEffect(joystickSize) {
        PrefsHelper.setJoystickSize(context, joystickSize)
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
    ) {
        Text(
            stringResource(R.string.settings_joystick_size_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                JoystickControl(
                    joystickSize = joystickSize
                ) { _, _ -> /* Ignore */ }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                modifier = Modifier.padding(12.dp).weight(1f),
                value = joystickSize,
                onValueChange = {
                    joystickSize = it
                },
                valueRange = 60f..240f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(42.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.secondary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        joystickSize = 120f
                    },
                    modifier = Modifier
                        .fillMaxSize()
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