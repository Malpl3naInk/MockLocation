package ink.moling.mocklocation.activity.main.views

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.main.MainViewModel
import ink.moling.mocklocation.activity.mappicker.MapPickerActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsPointView(
    viewModel: MainViewModel,
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    // 地图选点结果回调
    val mapPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { data ->
                val lat = data.getDoubleExtra(MapPickerActivity.EXTRA_RESULT_LATITUDE, 0.0)
                val lng = data.getDoubleExtra(MapPickerActivity.EXTRA_RESULT_LONGITUDE, 0.0)
                if (lat != 0.0 || lng != 0.0) {
                    viewModel.updatePointLat(lat.toString())
                    viewModel.updatePointLng(lng.toString())
                }
            }
        }
    }

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

        // 操作按钮组（仅当选中项目时显示）
        if (viewModel.selectedMockPoint != null) {
            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 主操作按钮：编辑/保存
                val isSaveMode = uiState.editingSimPoint
                val primaryColor = if (isSaveMode && uiState.isPointModified)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary

                OutlinedButton(
                    onClick = {
                        if (isSaveMode) viewModel.savePoint()
                        else viewModel.startEditPoint()
                    },
                    modifier = Modifier.padding(horizontal = 6.dp),
                    border = BorderStroke(2.dp, primaryColor),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 20.dp, top = 8.dp, bottom = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = if (isSaveMode) Icons.Outlined.Save else Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = primaryColor
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(
                            if (isSaveMode) R.string.button_save else R.string.button_edit
                        ),
                        color = primaryColor
                    )
                }

                // 编辑模式下的辅助按钮组
                if (isSaveMode) {
                    // 取消编辑（仅编辑已有项目时显示）
                    if (!uiState.isCreatingPoint) {
                        CircleIconButton(
                            onClick = { viewModel.cancelEditPoint() },
                            icon = Icons.AutoMirrored.Outlined.Undo,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // 删除按钮
                    CircleIconButton(
                        onClick = { viewModel.showDeletePointConfirmDialog() },
                        icon = Icons.Outlined.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )

                    // 填充当前位置
                    IconButton(onClick = { viewModel.fillCurrentLocation() }) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = null)
                    }

                    // 地图选点
                    IconButton(onClick = {
                        val intent = Intent(context, MapPickerActivity::class.java).apply {
                            val currentLat = uiState.pointLat.toDoubleOrNull() ?: 0.0
                            val currentLng = uiState.pointLng.toDoubleOrNull() ?: 0.0
                            if (currentLat != 0.0 || currentLng != 0.0) {
                                putExtra(MapPickerActivity.EXTRA_INITIAL_LATITUDE, currentLat)
                                putExtra(MapPickerActivity.EXTRA_INITIAL_LONGITUDE, currentLng)
                            }
                        }
                        mapPickerLauncher.launch(intent)
                    }) {
                        Icon(Icons.Outlined.Map, contentDescription = null)
                    }
                } else {
                    // 非编辑模式：仅显示删除按钮
                    CircleIconButton(
                        onClick = { viewModel.showDeletePointConfirmDialog() },
                        icon = Icons.Outlined.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )
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

/**
 * 圆形边框图标按钮 - 用于操作按钮组中的辅助操作
 */
@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(42.dp)
            .border(2.dp, tint, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
    }
}