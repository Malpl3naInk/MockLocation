package ink.moling.mocklocation.activity.main.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.activity.main.MainViewModel
import ink.moling.mocklocation.ui.components.PillSelection
import ink.moling.mocklocation.ui.components.PillSelector
import ink.moling.mocklocation.utils.MockMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageSimulationView(
    viewModel: MainViewModel,
    scaffoldState: BottomSheetScaffoldState,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 88.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Simulation",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            PillSelector(
                items = listOf(
                    PillSelection(Icons.Outlined.LocationOn),
                    PillSelection(Icons.Outlined.Route)
                ),
                enabled = !uiState.editingSimPoint,
                selectedIndex = uiState.selectedSimulation,
                onSelectedChange = { viewModel.setSimulationMode(it) }
            )
        }

        when (uiState.selectedSimulation) {
            MockMode.MOCK_MODE_POINT -> OptionsPointView(viewModel, scaffoldState)
            MockMode.MOCK_MODE_ROUTE -> OptionsRouteView(viewModel, scaffoldState)
        }
    }
}