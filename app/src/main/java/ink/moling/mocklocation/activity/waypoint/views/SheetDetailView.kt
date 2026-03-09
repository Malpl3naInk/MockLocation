package ink.moling.mocklocation.activity.waypoint.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.waypoint.WaypointViewModel
import ink.moling.mocklocation.utils.WaypointGraph

@Composable
fun SheetDetailView(
    viewModel: WaypointViewModel,
    selectedWaypoint: Int,
    onWaypointEdit: () -> Unit,
    onConnectsEdit: () -> Unit,
    onConnectsDelete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .padding(
                bottom = 12.dp,
                start = 8.dp,
                end = 8.dp
            )
    ) {
        Text(
            stringResource(R.string.options_route_label_name),
            modifier = Modifier,
            color = MaterialTheme.colorScheme.onSecondary
        )
        TextField(
            value = uiState.routeName,
            onValueChange = {
                viewModel.updateRouteName(it)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                val tSelectedWaypoint = when (selectedWaypoint) {
                    -1      -> "/"
                    else    -> "${selectedWaypoint + 1}"
                }
                Text(
                    stringResource(R.string.waypoint_detail_waypoint_title, tSelectedWaypoint),
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                if (selectedWaypoint != -1) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        onClick = onWaypointEdit
                    ) {
                        Icon(
                            Icons.Outlined.EditLocationAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.waypoint_detail_button_change_location))
                    }

                    if (uiState.selectedDisplayMode == WaypointGraph.CANVAS) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            onClick = onConnectsEdit
                        ) {
                            Icon(
                                Icons.Outlined.Route,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(stringResource(R.string.waypoint_detail_button_connections))
                        }
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        onClick = onConnectsDelete
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.waypoint_detail_button_delete))
                    }
                }
            }
        }
    }
}