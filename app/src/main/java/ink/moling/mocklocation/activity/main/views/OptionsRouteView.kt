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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.activity.main.MainViewModel
import ink.moling.mocklocation.activity.waypoint.WaypointActivity
import ink.moling.mocklocation.ui.components.LatLngScatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsRouteView(
    viewModel: MainViewModel,
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    // Activity Launcher
    val waypointActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            WaypointActivity.RESULT_EDIT_OK -> { }
            WaypointActivity.RESULT_NEW_OK -> { }
            else -> { }
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
                        "Route name",
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(uiState.routeName)
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
                        "Map",
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    if (uiState.routeObject != null && uiState.routeObject!!.points.isNotEmpty()) {
                        LatLngScatter(
                            modifier = Modifier.fillMaxHeight(0.28f),
                            routeObject = uiState.routeObject,
                            pointRadius = 0.dp
                        )
                    } else {
                        Text(
                            "No route data",
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(vertical = 6.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    waypointActivityLauncher.launch(
                        Intent(context, WaypointActivity::class.java).apply {
                            putExtra("selectedRoute", uiState.routeName)
                        }
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 6.dp),
                border = BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.secondary
                ),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Edit",
                    color = MaterialTheme.colorScheme.secondary
                )
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
                    onClick = { viewModel.showDeleteRouteConfirmDialog() },
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

            // Export route button
            IconButton(
                onClick = { }
            ) {
                Icon(
                    Icons.Outlined.FileUpload,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                    Text(uiState.routeName)
                    Text(
                        "Select target",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    Icons.Outlined.ArrowDropUp,
                    contentDescription = null
                )
            }
        }
    }
}
