package ink.moling.mocklocation.activity.main.views

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShareLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.activity.main.MainViewModel
import ink.moling.mocklocation.activity.settings.SettingsActivity
import ink.moling.mocklocation.data.local.repository.MockServiceState

@Composable
fun PageMainView(
    viewModel: MainViewModel,
    appName: String,
    onStartMockLocation: () -> Unit,
    onStopMockLocation: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val mockStatus by viewModel.mockStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 88.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 24.dp),
            text = appName,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Card(
            modifier = Modifier
                .padding(vertical = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(6.dp)
                    )
                    Text(
                        text = uiState.displayedLocation
                    )
                }
                Text(
                    text = uiState.displayedOpenCode,
                    modifier = Modifier
                        .padding(start = 36.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .padding(horizontal = 6.dp),
                onClick = {
                    if (mockStatus == MockServiceState.Enabled || mockStatus is MockServiceState.Error) {
                        onStopMockLocation()
                    } else if (mockStatus == MockServiceState.Disabled) {
                        if (!viewModel.hasSelectedPoint()) {
                            Toast.makeText(
                                context,
                                "Please select a point",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        onStartMockLocation()
                    }
                }
            ) {
                Text(
                    when (mockStatus) {
                        MockServiceState.Disabled -> "Start"
                        MockServiceState.Enabled -> "Stop"
                        else -> "Wait..."
                    }
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(42.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.secondary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                SettingsActivity::class.java
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                        "Current selection",
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = when(uiState.selectedSimulation) {
                            0    -> "Point > ${uiState.pointName}"
                            else -> "Route > ${uiState.routeName}"
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = (
                            when(uiState.selectedSimulation) {
                                0    -> Icons.Outlined.LocationOn
                                else -> Icons.Outlined.Route
                            }
                            ),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
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
                        "Status",
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        when (mockStatus) {
                            MockServiceState.Enabled -> "Mocking"
                            MockServiceState.Initializing -> "Initializing"
                            else -> "Idle"
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    when (mockStatus) {
                        MockServiceState.Enabled -> Icons.Outlined.ShareLocation
                        MockServiceState.Initializing -> Icons.Outlined.Build
                        else -> Icons.Outlined.LocationSearching
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.4f))
    }
}