package ink.moling.mocklocation.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.ui.components.DebugOnly

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
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                }
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Widgets,
                                contentDescription = null
                            )
                            Text(
                                "Something",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                }
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Widgets,
                                contentDescription = null
                            )
                            Text(
                                "Something else",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    modifier = Modifier.padding(vertical = 6.dp),
                    text = "About",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    /* TODO: OSS license activity */
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_github),
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                "Release repo",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    /* TODO: OSS license activity */
                                }
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Code,
                                contentDescription = null
                            )
                            Text(
                                "Open Source License",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            item {
                DebugOnly {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = "Debug",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                DebugOnly {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Column {
                            val progress = remember { Animatable(0f) }
                            var pressing by remember { mutableStateOf(false) }
                            val pressDuration = 2000

                            LaunchedEffect(pressing) {
                                if (pressing) {
                                    progress.snapTo(0f)
                                    progress.animateTo(
                                        1f,
                                        animationSpec = tween(
                                            pressDuration,
                                            easing = LinearEasing
                                        )
                                    )
                                    // 长按完成，触发点击事件
                                    throw RuntimeException("Crash test")
                                } else {
                                    // 释放时重置进度
                                    progress.animateTo(
                                        0f,
                                        animationSpec = tween(200)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                pressing = true
                                            },
                                            onPress = {
                                                try {
                                                    tryAwaitRelease()
                                                } finally {
                                                    pressing = false
                                                }
                                            }
                                        )
                                    }
                            ) {
                                // 设置卡片内容（模仿 SettingCardNormal）
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.BugReport,
                                        contentDescription = null
                                    )
                                    Text(
                                        "Crash test (Long press)",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 长按进度覆盖层
                                Box(
                                    modifier = Modifier.matchParentSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress.value)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                MaterialTheme.colorScheme.error
                                                    .copy(alpha = 0.8f * progress.value)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}