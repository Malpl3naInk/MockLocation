package ink.moling.mocklocation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        LazyColumn {
            item {
                Text(
                    modifier = Modifier.padding(top = 88.dp, bottom = 32.dp, start = 24.dp),
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            item {
                Text(
                    modifier = Modifier.padding(vertical = 6.dp),
                    text = "General",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    onClick = {  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Widgets,
                            contentDescription = "Something",
                            modifier = Modifier.padding(start = 20.dp, end = 16.dp)
                        )
                        Text("Something")
                    }
                }
            }
            item {
                Text(
                    modifier = Modifier.padding(vertical = 6.dp),
                    text = "About",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    onClick = {
                        /* TODO: OSS license activity */
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Code,
                            contentDescription = "Open Source License",
                            modifier = Modifier.padding(start = 20.dp, end = 16.dp)
                        )
                        Text("Open Source License")
                    }
                }
            }
        }
    }
}