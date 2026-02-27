package ink.moling.mocklocation.activity.main.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.main.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsPointView(
    viewModel: MainViewModel,
    scaffoldState: BottomSheetScaffoldState
) {
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    Column {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.options_point_label_name),
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    if (uiState.editingSimPoint) {
                        TextField(
                            value = uiState.pointName,
                            onValueChange = { viewModel.updatePointName(it) },
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
                    } else {
                        Text(uiState.pointName)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.options_point_label_location),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    if (uiState.editingSimPoint) {
                        Column(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 2.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            )
                        ) {
                            Text(
                                stringResource(R.string.options_point_label_latitude),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = uiState.pointLat,
                                onValueChange = { viewModel.updatePointLat(it) },
                                singleLine = true,
                                isError = uiState.pointLatError,
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
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                            Text(
                                stringResource(R.string.options_point_label_longitude),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = uiState.pointLng,
                                onValueChange = { viewModel.updatePointLng(it) },
                                singleLine = true,
                                isError = uiState.pointLngError,
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
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                            Text(
                                stringResource(R.string.options_point_label_altitude),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = uiState.pointAlt,
                                onValueChange = { viewModel.updatePointAlt(it) },
                                singleLine = true,
                                isError = uiState.pointAltError,
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
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                        }
                    } else {
                        Text(
                            "@%s,%s#%s".format(
                                uiState.pointLat,
                                uiState.pointLng,
                                uiState.pointAlt
                            )
                        )
                    }
                }
            }
        }

        // 只有在选中点时才显示操作按钮
        if (viewModel.hasSelectedPoint()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (uiState.editingSimPoint) {
                            viewModel.savePoint()
                        } else {
                            viewModel.startEditPoint()
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 6.dp),
                    border = BorderStroke(
                        2.dp,
                        color = (
                                if (uiState.editingSimPoint && uiState.isPointModified)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                                )
                    ),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = (
                                if (uiState.editingSimPoint)
                                    Icons.Outlined.Save
                                else
                                    Icons.Outlined.Edit
                                ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = (
                                if (uiState.editingSimPoint && uiState.isPointModified)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                                )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = (
                                if (uiState.editingSimPoint)
                                    stringResource(R.string.button_save)
                                else
                                    stringResource(R.string.button_edit)
                                ),
                        color = (
                                if (uiState.editingSimPoint && uiState.isPointModified)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                                )
                    )
                }

                if (uiState.editingSimPoint && !uiState.isCreatingPoint) {
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
                            onClick = { viewModel.cancelEditPoint() },
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

                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(42.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.error,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.showDeletePointConfirmDialog() },
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (uiState.editingSimPoint) {
                    // Fill with current location
                    IconButton(
                        onClick = { viewModel.fillCurrentLocation() }
                    ) {
                        Icon(
                            Icons.Outlined.MyLocation,
                            contentDescription = null
                        )
                    }

                    // Select from map
                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            Icons.Outlined.Map,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (!uiState.editingSimPoint) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                onClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(uiState.pointName)
                        Text(
                            stringResource(R.string.label_select_target),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Icon(
                        Icons.Outlined.ArrowDropUp,
                        contentDescription = null
                    )
                }
            }
        }
    }
}