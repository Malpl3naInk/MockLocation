package ink.moling.mocklocation.activity.main.views

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonSyntaxException
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.main.MainViewModel
import ink.moling.mocklocation.activity.waypoint.WaypointActivity
import ink.moling.mocklocation.data.models.RouteObjectJson
import ink.moling.mocklocation.data.models.RouteType
import ink.moling.mocklocation.ui.dialog.HelpButtonConfig
import ink.moling.mocklocation.utils.extensions.isValid
import ink.moling.mocklocation.utils.extensions.label
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun SheetRoutesView(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importedUri by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(importedUri) {
        val uri = importedUri ?: return@LaunchedEffect

        try {
            // 读取文件内容
            val jsonText = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw CancellationException("Cannot read file")
            if (jsonText.isBlank()) throw CancellationException("File is empty")

            try {
                val route = RouteObjectJson.fromJson(jsonText)
                    ?: throw CancellationException("Parsing route error")

                if (route.isValid()) {
                    viewModel.addRoute(route.name, route)
                } else {
                    throw CancellationException("Illegal waypoints data")
                }
            } catch (_: JsonSyntaxException) {
                throw CancellationException("Invalid JSON format")
            }
        } catch (e: CancellationException) {
            viewModel.showError(
                title = "Failed to import",
                message = e.message.orEmpty(),
                helpButton = HelpButtonConfig(context.getString(R.string.error_help_import_failed))
            )
        } finally {
            importedUri = null
        }
    }
    // Activity Launcher
    val waypointActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            in intArrayOf(WaypointActivity.RESULT_EDIT_OK, WaypointActivity.RESULT_NEW_OK) -> {
                scope.launch { viewModel.getRoutes() }
            }
            else -> { }
        }
    }
    val importSelectorLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            Toast.makeText(context, context.getString(R.string.sheet_routes_toast_canceled), Toast.LENGTH_SHORT).show()
        } else {
            // 触发异步验证
            importedUri = uri
        }
    }

    val savedRoutes by viewModel.savedRoutes.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.sheet_routes_title),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = {
            waypointActivityLauncher.launch(
                Intent(context, WaypointActivity::class.java)
            )
        }) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null
            )
        }

        IconButton(onClick = {
            importSelectorLauncher.launch(
                arrayOf("application/json")
            )
        }) {
            Icon(
                Icons.Outlined.Download,
                contentDescription = null
            )
        }
    }

    LazyColumn(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(savedRoutes) { route ->
            Card(
                onClick = {
                    if (route.details.meta.type == RouteType.ROUTE) {
                        viewModel.selectRoute(route.id)
                    } else {
                        /*
                         * TODO: Select start / stop / waypoint on map
                         *  then generating route automatically
                         */
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        route.name,
                        modifier = Modifier
                            .padding(
                                top = 4.dp,
                                start = 6.dp,
                                end = 6.dp
                            )
                    )
                    Text(
                        route.details.meta.type.label(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}