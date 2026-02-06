package ink.moling.mocklocation.activity.waypoint

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.ui.components.LatLngScatter
import ink.moling.mocklocation.ui.components.PillSelection
import ink.moling.mocklocation.ui.components.PillSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaypointScreen(
    selectedRoute: String
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var selectedDisplayMode by remember { mutableIntStateOf(0) }
    var selectedSheetDetail by remember { mutableIntStateOf(0) }
    val routeNameState = rememberTextFieldState("<Placeholder>")
    var isWaypointMap by remember { mutableStateOf(false) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.78f)
                    .padding(
                        bottom = 16.dp,
                        start = 32.dp,
                        end = 32.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillSelector(
                        items = listOf(
                            PillSelection(Icons.Outlined.LocationOn, "Waypoints"),
                            PillSelection(Icons.Outlined.Description, "Details")
                        ),
                        selectedIndex = selectedSheetDetail,
                        onSelectedChange = { selectedSheetDetail = it },
                        selectedBackground = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (selectedSheetDetail == 0) {
                        IconButton(onClick = {

                        }) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null
                            )
                        }
                    }
                }

                when (selectedSheetDetail) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                        ) {
                            repeat(30) { times ->
                                item {
                                    var isWaypointCardExpanded by remember { mutableStateOf(false) }
                                    var isWaypointLoopable by remember { mutableStateOf(false) }
                                    Card(
                                        onClick = {
                                            isWaypointCardExpanded = !isWaypointCardExpanded
                                        },
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
                                                        "#$times"
                                                    else
                                                        "#$times - @0.000000,0.000000#0.00"
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
                                                            "@0.000000,0.000000#0.00",
                                                            modifier = Modifier
                                                                .padding(horizontal = 6.dp)
                                                        )

                                                        Spacer(modifier = Modifier.weight(1f))

                                                        IconButton(onClick = {

                                                        }) {
                                                            Icon(
                                                                Icons.Outlined.EditLocationAlt,
                                                                contentDescription = null
                                                            )
                                                        }
                                                    }

                                                    Row(
                                                        modifier = Modifier
                                                            .padding(bottom = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = isWaypointLoopable,
                                                            onCheckedChange = {
                                                                isWaypointLoopable = it
                                                            }
                                                        )
                                                        Text(
                                                            "Loop ring",
                                                            modifier = Modifier
                                                                .padding(horizontal = 3.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    "Common road",
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
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .padding(
                                    bottom = 12.dp,
                                    start = 8.dp,
                                    end = 8.dp
                                )
                        ) {
                            Text(
                                "Route name",
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                state = routeNameState,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isWaypointMap,
                                    onCheckedChange = { isWaypointMap = it }
                                )
                                Text("Map mode")
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (selectedDisplayMode) {
                0 -> {
                    LatLngScatter()
                }
                1 -> {

                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    modifier = Modifier
                        .padding(start = 6.dp),
                    onClick = {

                    }
                ) {
                    Icon(
                        Icons.Outlined.Save,
                        contentDescription = null
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PillSelector(
                        items = listOf(
                            PillSelection(Icons.Outlined.Route, "Route"),
                            PillSelection(Icons.Outlined.Map, "Map")
                        ),
                        selectedIndex = selectedDisplayMode,
                        onSelectedChange = { selectedDisplayMode = it }
                    )
                }
            }
        }
    }
}