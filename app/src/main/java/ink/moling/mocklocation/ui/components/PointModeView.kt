package ink.moling.mocklocation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.data.local.repository.MockServiceState
import ink.moling.mocklocation.viewmodel.MainViewModel

/**
 * 点位模式视图组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointModeView(
    viewModel: MainViewModel,
    onAddPointClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val points by viewModel.points.collectAsState()
    val mockStatus by viewModel.mockStatus.collectAsState()
    val isMockDisabled = mockStatus != MockServiceState.Enabled

    LaunchedEffect(Unit) {
        viewModel.loadPoints()
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {
                if (isMockDisabled) isExpanded = it
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            @Suppress("DEPRECATION")
            OutlinedTextField(
                value = viewModel.selectedMockPoint?.name ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = isMockDisabled,
                label = { Text("Select point") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(isExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onDismissRequest = { isExpanded = false }
            ) {
                if (points.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No saved points") },
                        onClick = { isExpanded = false },
                        enabled = false
                    )
                } else {
                    points.forEach { point ->
                        LongPressDeleteMenuItem(
                            text = {
                                Column {
                                    Text(point.name)
                                    Text(
                                        text = "%.8f, %.8f".format(point.latitude, point.longitude),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectedMockPoint = point
                                isExpanded = false
                            },
                            onDelete = {
                                if (point.name == viewModel.selectedMockPoint?.name) {
                                    viewModel.selectedMockPoint = null
                                }
                                viewModel.deletePoint(point.id)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        // 添加点位按钮
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .padding(top = 4.dp),
            onClick = onAddPointClick
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Add point",
                // tint = Color.White
            )
        }
    }
}

