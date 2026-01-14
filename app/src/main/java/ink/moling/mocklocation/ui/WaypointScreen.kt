package ink.moling.mocklocation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.data.models.RouteObject
import ink.moling.mocklocation.ui.components.LatLngScatter

@Composable
fun WaypointScreen(
    selectedRoute: RouteObject
) {
    var selectedRouteName       by remember { mutableStateOf("") }
    var highlightedRoutePointId by remember { mutableStateOf<Int?>(null) }
    var routePointRadius        by remember { mutableStateOf(3.dp) }
    var selectedTab             by remember { mutableIntStateOf(0) }
    val tabs = listOf("Route Points", "Waypoints")
    selectedRouteName = selectedRoute.name

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 64.dp)
        ) {
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    value = selectedRouteName,
                    onValueChange = {
                        selectedRouteName = it
                    },
                    label = {
                        Text("Route name")
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    LatLngScatter(
                        modifier = Modifier.fillMaxSize(),
                        routeObject = selectedRoute,
                        pointRadius = routePointRadius,
                        highlightPointId = highlightedRoutePointId
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                routePointRadius = when (index) {
                                    0 -> 3.dp
                                    1 -> 0.dp
                                    else -> 3.dp
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> LazyColumn {
                        items(selectedRoute.points) {
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        highlightedRoutePointId =
                                            if (highlightedRoutePointId != it.id) it.id else null
                                    },
                                    onLongClick = { /* TODO */ }
                                )
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "#${it.id}",
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(45.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Column(
                                            modifier = Modifier.weight(1.0f)
                                        ) {
                                            Text(it.lat.toString())
                                            Text(it.lng.toString())
                                        }
                                        Text(
                                            it.type,
                                            modifier = Modifier.width(40.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                    1 -> LazyColumn {
                        item {
                            Text("Waypoint")
                        }
                    }
                }
            }
        }
    }
}