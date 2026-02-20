package ink.moling.mocklocation.activity.waypoint.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.waypoint.WaypointViewModel
import ink.moling.mocklocation.data.models.PointType

@Composable
fun SheetPointView(
    viewModel: WaypointViewModel,
    selectedWaypoint: Int,
    onSelectionChange: (index: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .padding(bottom = 12.dp)
    ) {
        items(uiState.routeObject.points.size) { index ->
            val waypoint = uiState.routeObject.points[index]
            val isWaypointCardExpanded = index == selectedWaypoint

            Card(
                onClick = { onSelectionChange(if (isWaypointCardExpanded) -1 else index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = (
                            if (isWaypointCardExpanded)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                Color.Transparent
                            )
                )
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = (
                                if (isWaypointCardExpanded)
                                    "#${waypoint.id}"
                                else
                                    "#${waypoint.id} - @%.6f,%.6f".format(waypoint.lat, waypoint.lng)
                                ),
                        modifier = Modifier
                            .padding(
                                horizontal = 6.dp,
                                vertical = (
                                        if (isWaypointCardExpanded)
                                            6.dp
                                        else
                                            0.dp
                                        )
                            )
                    )
                    if (isWaypointCardExpanded) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(horizontal = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(
                            modifier = Modifier
                                .padding(
                                    start = 18.dp,
                                    end = 6.dp
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "@%.6f,%.6f".format(waypoint.lat, waypoint.lng),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(onClick = {
                                    viewModel.setSheetDetail(1)
                                }) {
                                    Icon(
                                        Icons.Outlined.EditLocationAlt,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            when (waypoint.type) {
                                PointType.R -> stringResource(R.string.waypoint_point_type_road)
                                PointType.L -> stringResource(R.string.waypoint_point_type_loop)
                                PointType.W -> stringResource(R.string.waypoint_point_type_walk)
                            },
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }
}