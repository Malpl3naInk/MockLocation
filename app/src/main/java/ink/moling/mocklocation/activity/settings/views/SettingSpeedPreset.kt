package ink.moling.mocklocation.activity.settings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.utils.extensions.isFloat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingSpeedPreset(
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var speedPresetWalk by remember { mutableStateOf("5.0") }
    var speedPresetRun  by remember { mutableStateOf("12.0") }
    var speedPresetBike by remember { mutableStateOf("25.0") }
    PrefsHelper.getMaxSpeedPresets(context).let {
        speedPresetWalk = it[0].toString()
        speedPresetRun  = it[1].toString()
        speedPresetBike = it[2].toString()
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
    ) {
        Text(
            stringResource(R.string.settings_speed_preset_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                stringResource(R.string.settings_speed_preset_walking),
                color = MaterialTheme.colorScheme.onSecondary
            )
            TextField(
                value = speedPresetWalk,
                isError = !speedPresetWalk.isFloat(strict = true),
                onValueChange = { if (it.isFloat()) speedPresetWalk = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 6.dp
                    )
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )
            Text(
                stringResource(R.string.settings_speed_preset_running),
                color = MaterialTheme.colorScheme.onSecondary
            )
            TextField(
                value = speedPresetRun,
                isError = !speedPresetRun.isFloat(strict = true),
                onValueChange = { if (it.isFloat()) speedPresetRun = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 6.dp
                    )
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )
            Text(
                stringResource(R.string.settings_speed_preset_bicycling),
                color = MaterialTheme.colorScheme.onSecondary
            )
            TextField(
                value = speedPresetBike,
                isError = !speedPresetBike.isFloat(strict = true),
                onValueChange = { if (it.isFloat()) speedPresetBike = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 6.dp
                    )
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                onClick = {
                    if (
                        speedPresetWalk.isFloat(strict = true) &&
                        speedPresetRun.isFloat(strict = true) &&
                        speedPresetBike.isFloat(strict = true)
                    ) {
                        PrefsHelper.setMaxSpeedPresets(context, listOf(
                            speedPresetWalk.toDouble(),
                            speedPresetRun.toDouble(),
                            speedPresetBike.toDouble()
                        ))
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.button_save))
            }
        }
    }
}