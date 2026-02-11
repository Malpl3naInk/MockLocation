package ink.moling.mocklocation.activity.settings

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.ui.components.DebugOnly
import ink.moling.mocklocation.utils.extensions.isFloat
import ink.moling.mocklocation.utils.logger.LoggerFile
import kotlinx.coroutines.launch

enum class SettingOption {
    SPEED_PRESETS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var settingOption by remember { mutableStateOf(SettingOption.SPEED_PRESETS) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetState = scaffoldState.bottomSheetState
    val scaffoldExpanded by remember {
        derivedStateOf {
            sheetState.targetValue == SheetValue.Expanded ||
                    sheetState.currentValue == SheetValue.Expanded
        }
    }

    var speedPresetWalk by remember { mutableStateOf("5.0") }
    var speedPresetRun  by remember { mutableStateOf("12.0") }
    var speedPresetBike by remember { mutableStateOf("25.0") }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        sheetContent = {
            when (settingOption) {
                SettingOption.SPEED_PRESETS -> {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Text(
                            "Speed Presets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                        ) {
                            Text(
                                "Walking speed",
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = speedPresetWalk,
                                isError = !speedPresetWalk.isFloat(strict = true),
                                onValueChange = { if (it.isFloat()) speedPresetWalk = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .padding(
                                        start = 6.dp,
                                        end = 6.dp,
                                        bottom = 6.dp
                                    )
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            Text(
                                "Running speed",
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = speedPresetRun,
                                isError = !speedPresetRun.isFloat(strict = true),
                                onValueChange = { if (it.isFloat()) speedPresetRun = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .padding(
                                        start = 6.dp,
                                        end = 6.dp,
                                        bottom = 6.dp
                                    )
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            Text(
                                "Bicycling speed",
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = speedPresetBike,
                                isError = !speedPresetBike.isFloat(strict = true),
                                onValueChange = { if (it.isFloat()) speedPresetBike = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .padding(
                                        start = 6.dp,
                                        end = 6.dp,
                                        bottom = 6.dp
                                    )
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )

                            OutlinedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                onClick = {
                                    if (
                                        speedPresetWalk.isFloat(strict = true) &&
                                        speedPresetRun.isFloat(strict = true) &&
                                        speedPresetBike.isFloat(strict = true)
                                    ) {
                                        PrefsHelper.setMaxSpeedPresets(context, listOf(
                                            speedPresetWalk.toDouble(),
                                            speedPresetRun.toDouble(),
                                            speedPresetBike.toDouble()
                                        ))
                                        scope.launch {
                                            scaffoldState.bottomSheetState.partialExpand()
                                        }
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
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
                                        settingOption = SettingOption.SPEED_PRESETS
                                        PrefsHelper.getMaxSpeedPresets(context).let {
                                            speedPresetWalk = it[0].toString()
                                            speedPresetRun  = it[1].toString()
                                            speedPresetBike = it[2].toString()
                                        }
                                        scope.launch {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.DirectionsWalk,
                                    contentDescription = null
                                )
                                Text(
                                    "Speed presets",
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
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = "Debug",
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
                                        val logFile =
                                            LoggerFile.getCurrentLogFile() ?: return@clickable

                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            logFile
                                        )

                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "text/plain")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }

                                        context.startActivity(
                                            Intent.createChooser(intent, "Open log with...")
                                        )
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Description,
                                    contentDescription = null
                                )
                                Text(
                                    "Export log",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            DebugOnly {
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

            AnimatedVisibility(
                visible = scaffoldExpanded,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                )
            }
        }
    }
}