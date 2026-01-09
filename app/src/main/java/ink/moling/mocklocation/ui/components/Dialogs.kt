package ink.moling.mocklocation.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.utils.FileHelper
import ink.moling.mocklocation.utils.LatLngInputType
import ink.moling.mocklocation.utils.isValidLatLngInput
import ink.moling.mocklocation.viewmodel.MainViewModel
import org.json.JSONObject
import java.io.File

/**
 * 错误对话框
 */
@Composable
fun ErrorDialog(
    onDismiss: () -> Unit,
    title: String,
    text: String,
    stackTrace: String
) {
    val context = LocalContext.current
    val stackTraceScrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(text)
                Text(
                    stackTrace,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 20.dp)
                        .verticalScroll(state = stackTraceScrollState)
                        .combinedClickable(
                            onClick = { /* 可选 */ },
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(stackTrace))
                                Toast.makeText(
                                    context,
                                    "Stack trace copied",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * 导入/导出路径对话框
 */
@Composable
fun ImportExportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Import", "Export")
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            // 用户没有选择文件，或者按了返回
            Toast.makeText(context, "User canceled", Toast.LENGTH_SHORT).show()
        } else {
            // 用户选择了文件
            val importedFile = FileHelper.importToRoutesDir(context, uri)
            Toast.makeText( context, "Route file imported", Toast.LENGTH_SHORT).show()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage routes") },
        text = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTab) {
                    0 -> Button(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    ) {
                        Text("Select file (.json)")
                    }
                    1 -> RouteFileList()
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * 权限未授予警告对话框
 */
@Composable
fun AddPointDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pointLatitude by remember { mutableStateOf("") }
    var isLatitudeError by remember { mutableStateOf(false) }
    var pointLongitude by remember { mutableStateOf("") }
    var isLongitudeError by remember { mutableStateOf(false) }
    var pointName by remember { mutableStateOf("") }
    var isNameError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add point") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        label = { Text("Latitude") },
                        value = pointLatitude,
                        isError = isLatitudeError,
                        onValueChange = {
                            pointLatitude = it
                            isLatitudeError = !isValidLatLngInput(it, LatLngInputType.LAT)
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        label = { Text("Longitude") },
                        value = pointLongitude,
                        isError = isLongitudeError,
                        onValueChange = {
                            pointLongitude = it
                            isLongitudeError = !isValidLatLngInput(it, LatLngInputType.LNG)
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )
                }
                Button(
                    onClick = {
                        pointLatitude = viewModel.gpsLatitude.value.toString()
                        pointLongitude = viewModel.gpsLongitude.value.toString()
                    }
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
                            pointLongitude.toDouble()
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun RouteFileList() {
    val context = LocalContext.current
    val routeFiles by remember { mutableStateOf(FileHelper.listRouteFiles(context)) }

    val fileNameToFileMap = remember { mutableStateMapOf<String, File>() }
    LaunchedEffect(routeFiles) {
        fileNameToFileMap.clear()
        routeFiles.forEach { file ->
            try {
                val json = FileHelper.readText(file)
                val name = JSONObject(json).optString("name", file.name)
                fileNameToFileMap[name] = file
            } catch (_: Exception) {
                fileNameToFileMap[file.name] = file
            }
        }
    }

    var exportFile by remember { mutableStateOf<File?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        exportFile?.let { file ->
            FileHelper.exportFromFilesDir(context, file, uri)
            Toast.makeText(context, "Exported ${file.name}", Toast.LENGTH_SHORT).show()
        }
        exportFile = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = fileNameToFileMap.keys.toList(),
            key = { it }
        ) { displayName ->
            Text(
                text = displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                exportFile = fileNameToFileMap[displayName]
                                exportFile?.let {
                                    exportLauncher.launch("$displayName.json")
                                }
                            }
                        )
                    }
            )

            Divider(thickness = 0.5.dp)
        }
    }
}
