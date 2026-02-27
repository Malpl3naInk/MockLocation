package ink.moling.mocklocation.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R

@Composable
fun AddWaypointDialog(
    currentLatitude: Double?,
    currentLongitude: Double?,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    isEditMode: Boolean = false,
    onConfirm: (latitude: Double, longitude: Double) -> Unit,
    onDismiss: () -> Unit
) {

    var latitudeText by remember { mutableStateOf(initialLatitude?.toString() ?: "") }
    var longitudeText by remember { mutableStateOf(initialLongitude?.toString() ?: "") }
    var latitudeError by remember { mutableStateOf(false) }
    var longitudeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = {
            Text(
                text = (
                    if (isEditMode) stringResource(R.string.waypoint_dialog_title_edit)
                    else            stringResource(R.string.waypoint_dialog_title_add)
                ),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Latitude input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = latitudeText,
                        onValueChange = {
                            latitudeText = it
                            latitudeError = false
                        },
                        label = { Text(stringResource(R.string.waypoint_dialog_text_latitude)) },
                        placeholder = { Text(stringResource(R.string.waypoint_dialog_example_latitude)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = latitudeError,
                        supportingText = if (latitudeError) {
                            { Text(stringResource(R.string.waypoint_dialog_text_latitude_invalid)) }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Longitude input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = longitudeText,
                        onValueChange = {
                            longitudeText = it
                            longitudeError = false
                        },
                        label = {
                            Text(stringResource(R.string.waypoint_dialog_text_longitude))
                        },
                        placeholder = {
                            Text(stringResource(R.string.waypoint_dialog_example_longitude))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = longitudeError,
                        supportingText = if (longitudeError) {{
                            Text(stringResource(R.string.waypoint_dialog_text_longitude_invalid))
                        }} else null,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Use current location button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = currentLatitude != null && currentLongitude != null,
                            onClick = {
                                currentLatitude?.let { latitudeText = it.toString() }
                                currentLongitude?.let { longitudeText = it.toString() }
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = "Use current location",
                        modifier = Modifier.padding(12.dp)
                    )
                    Text(
                        text = if (currentLatitude != null && currentLongitude != null) {
                            stringResource(R.string.waypoint_dialog_text_current_location)
                        } else {
                            stringResource(R.string.waypoint_dialog_text_waiting_location)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentLatitude != null && currentLongitude != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latitudeText.toDoubleOrNull()
                    val lng = longitudeText.toDoubleOrNull()

                    when {
                        lat == null || lat < -90 || lat > 90 -> {
                            latitudeError = true
                        }
                        lng == null || lng < -180 || lng > 180 -> {
                            longitudeError = true
                        }
                        else -> {
                            onConfirm(lat, lng)
                        }
                    }
                }
            ) {
                Text(
                    if (isEditMode) stringResource(R.string.waypoint_dialog_button_save)
                    else            stringResource(R.string.waypoint_dialog_button_add)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}
