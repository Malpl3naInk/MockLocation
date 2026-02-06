package ink.moling.mocklocation.activity.main.views

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.activity.waypoint.WaypointActivity
import ink.moling.mocklocation.activity.main.MainViewModel

@Composable
fun SheetRoutesView(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Saved routes",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

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

        IconButton(onClick = { }) {
            Icon(
                Icons.Outlined.Download,
                contentDescription = null
            )
        }
    }

    LazyColumn(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        repeat(50) {
            item {
                Card(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            "Map #$it",
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }
}