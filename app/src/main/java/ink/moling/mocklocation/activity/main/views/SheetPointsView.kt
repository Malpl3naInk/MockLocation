package ink.moling.mocklocation.activity.main.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.main.MainViewModel

@Composable
fun SheetPointsView(
    viewModel: MainViewModel
) {
    val savedPoints by viewModel.savedPoints.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.sheet_points_title),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { viewModel.startCreatePoint() }) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null
            )
        }
    }

    LazyColumn(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(savedPoints) { point ->
            Card(
                onClick = { viewModel.selectPoint(point.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        point.name,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                    )
                    Text(
                        "@%.6f,%.6f#%.2f".format(point.lat, point.lng, point.alt),
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}