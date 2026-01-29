package ink.moling.mocklocation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.ui.components.DebugOnly
import ink.moling.mocklocation.ui.components.LongPressClickCard
import ink.moling.mocklocation.ui.components.SettingCardNormal

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
                SettingCardNormal(
                    Icons.Outlined.Widgets,
                    contentDescription = "Something",
                    text = "Something"
                ) {

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
                SettingCardNormal(
                    Icons.Outlined.Code,
                    contentDescription = "Open Source License",
                    text = "Open Source License"
                ) {
                    /* TODO: OSS license activity */
                }
            }
            item {
                DebugOnly {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = "Debug",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                DebugOnly {
                    LongPressClickCard(
                        Icons.Outlined.BugReport,
                        contentDescription = "Crash test",
                        text = "Crash test",
                        progressColor = Color(0xFFFF5252)
                    ) {
                        throw RuntimeException("Crash test")
                    }
                }
            }
        }
    }
}