package ink.moling.mocklocation.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.utils.FileHelper
import org.json.JSONObject
import java.io.File

/**
 * 权限未授予警告对话框
 */
@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Not Granted") },
        text = { Text("Program is not allowed to perform MOCK_LOCATION") },
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
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            items = fileNameToFileMap.keys.toList(),
            key = { it }
        ) { displayName ->

            LongPressDeleteRow(
                text = displayName,
                onClick = {
                    exportFile = fileNameToFileMap[displayName]
                    exportFile?.let {
                        exportLauncher.launch("$displayName.json")
                    }
                },
                onDelete = {
                    FileHelper.deleteFile(fileNameToFileMap[displayName]!!)
                    fileNameToFileMap.remove(displayName)
                }
            )

            Divider(thickness = 0.5.dp)
        }
    }
}

@Composable
fun LongPressDeleteRow(
    text: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    deleteDuration: Int = 1200
) {
    val progress = remember { Animatable(0f) }
    var pressing by remember { mutableStateOf(false) }

    LaunchedEffect(pressing) {
        if (pressing) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(deleteDuration)
            )
            onDelete()
        } else {
            progress.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            // 手势处理
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        pressing = true
                    },
                    onPress = {
                        try {
                            // 等待手指松开或取消
                            tryAwaitRelease()
                        } finally {
                            // 松手取消
                            pressing = false
                        }
                    }
                )
            }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(8.dp),
            maxLines = 1
        )

        // 删除进度覆盖层
        Box(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(progress.value)
                .background(
                    Color(0xFFFF5252).copy(alpha = progress.value)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            )
        }
    }
}

