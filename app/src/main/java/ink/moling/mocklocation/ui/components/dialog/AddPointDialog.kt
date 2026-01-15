package ink.moling.mocklocation.ui.components.dialog

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.data.repository.MockServiceState
import ink.moling.mocklocation.utils.LatLngInputType
import ink.moling.mocklocation.utils.isValidLatLngInput
import ink.moling.mocklocation.viewmodel.MainViewModel

/**
 * 添加点位对话框
 */
@Composable
fun AddPointDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pointLatitude       by remember { mutableStateOf("") }
    var isLatitudeError     by remember { mutableStateOf(false) }
    var pointLongitude      by remember { mutableStateOf("") }
    var isLongitudeError    by remember { mutableStateOf(false) }
    var pointAltitude       by remember { mutableStateOf("") }
    var isAltitudeError     by remember { mutableStateOf(false) }
    var pointName   by remember { mutableStateOf("") }
    var isNameError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add point") },
        text = {
            Column {
                GPSTextField(
                    label = "Latitude",
                    value = pointLatitude,
                    isError = isLatitudeError,
                    onValueChange = {
                        pointLatitude = it
                        isLatitudeError = !isValidLatLngInput(it, LatLngInputType.LAT)
                    }
                )
                GPSTextField(
                    label = "Longitude",
                    value = pointLongitude,
                    isError = isLongitudeError,
                    onValueChange = {
                        pointLongitude = it
                        isLongitudeError = !isValidLatLngInput(it, LatLngInputType.LNG)
                    }
                )
                GPSTextField(
                    label = "Altitude",
                    value = pointAltitude,
                    isError = isAltitudeError,
                    onValueChange = {
                        pointAltitude = it
                        isAltitudeError = !isValidLatLngInput(it, LatLngInputType.ALT)
                    }
                )
                if (viewModel.mockStatus.value == MockServiceState.Enabled) {
                    Text(
                        "Mock service running, current location unavailable",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        pointLatitude   = viewModel.location.value?.lat.toString()
                        pointLongitude  = viewModel.location.value?.lng.toString()
                        pointAltitude   = viewModel.location.value?.alt.toString()
                    },
                    enabled = viewModel.mockStatus.value != MockServiceState.Enabled
                ) {
                    Text("Current location")
                }
                OutlinedTextField(
                    label = { Text("Name") },
                    value = pointName,
                    isError = isNameError,
                    onValueChange = {
                        pointName = it
                        isNameError = pointName.isEmpty()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    isNameError = pointName.isEmpty()
                    if (!isLatitudeError && !isLongitudeError && isNameError) {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addPoint(
                            pointName,
                            pointLatitude.toDouble(),
                            pointLongitude.toDouble(),
                            pointAltitude.toDouble()
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GPSTextField(
    label: String,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        label = { Text(label) },
        value = value,
        isError = isError,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true
    )
}
