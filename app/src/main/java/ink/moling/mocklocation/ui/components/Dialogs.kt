package ink.moling.mocklocation.ui.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
            .padding(horizontal = 0.dp)
    ) {
        items(fileNameToFileMap.keys.toList()) { displayName ->
            Text(
                text = displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        exportFile = fileNameToFileMap[displayName]
                        exportFile?.let {
                            exportLauncher.launch("${displayName}.json")
                        }
                    }
                    .padding(8.dp),
                maxLines = 1
            )
            Divider(thickness = 0.5.dp)
        }
    }
}
