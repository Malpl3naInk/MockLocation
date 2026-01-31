package ink.moling.mocklocation.ui.dialog

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import ink.moling.mocklocation.data.local.FileHelper
import ink.moling.mocklocation.utils.logger.Logger
import ink.moling.mocklocation.utils.validateRouteFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

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
    
    // 验证状态
    var isValidating by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    
    // 异步验证和导入
    LaunchedEffect(pendingUri) {
        val uri = pendingUri ?: return@LaunchedEffect
        
        isValidating = true
        
        try {
            // 在 IO 线程执行验证
            val validationResult = withContext(Dispatchers.IO) {
                validateRouteFile(context, uri)
            }
            
            // 回到主线程更新 UI
            withContext(Dispatchers.Main) {
                if (validationResult.isValid) {
                    // 在 IO 线程执行文件导入
                    withContext(Dispatchers.IO) {
                        FileHelper.importToRoutesDir(context, uri)
                    }
                    Toast.makeText(
                        context,
                        "Route imported: ${validationResult.routeName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Logger.d("ImportExportDialog", "Route imported successfully: ${validationResult.routeName}")
                } else {
                    Toast.makeText(
                        context,
                        "Invalid route file: ${validationResult.errorMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    Logger.e("ImportExportDialog", "Validation failed: ${validationResult.errorMessage}")
                }
            }
        } catch (e: Exception) {
            Logger.e("ImportExportDialog", "Error during validation/import", e)
            Toast.makeText(
                context,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            isValidating = false
            pendingUri = null
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            Toast.makeText(context, "User canceled", Toast.LENGTH_SHORT).show()
        } else {
            // 触发异步验证
            pendingUri = uri
        }
    }
    
    AlertDialog(
        onDismissRequest = {
            // 验证时不允许关闭对话框
            if (!isValidating) {
                onDismiss()
            }
        },
        title = { Text("Manage routes") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        text = {
            Column {
                @Suppress("DEPRECATION")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { 
                                if (!isValidating) {
                                    selectedTab = index
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTab) {
                    0 -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 加载指示器
                        if (isValidating) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Text("Validating route file...")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // 选择文件按钮
                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                            enabled = !isValidating
                        ) {
                            Text("Select file (.json)")
                        }
                    }
                    1 -> RouteFileList()
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * 路径文件列表（用于导出）
 */
@Composable
private fun RouteFileList() {
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
            @Suppress("DEPRECATION")
            Divider(thickness = 0.5.dp)
        }
    }
}
