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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.settings.views.SettingJoystickSize
import ink.moling.mocklocation.activity.settings.views.SettingRandomOffset
import ink.moling.mocklocation.activity.settings.views.SettingSpeedPreset
import ink.moling.mocklocation.activity.settings.views.SettingThemeMode
import ink.moling.mocklocation.ui.components.DebugOnly
import ink.moling.mocklocation.utils.logger.LoggerFile
import kotlinx.coroutines.launch

enum class SettingOption {
    SPEED_PRESETS,
    THEME_MODE,
    JOYSTICK_SIZE,
    RANDOM_OFFSET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strOpenLog = stringResource(R.string.settings_chooser_open_log)

    var settingOption by remember { mutableStateOf(SettingOption.SPEED_PRESETS) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetState = scaffoldState.bottomSheetState
    val scaffoldExpanded by remember {
        derivedStateOf {
            sheetState.targetValue == SheetValue.Expanded ||
                    sheetState.currentValue == SheetValue.Expanded
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        containerColor = MaterialTheme.colorScheme.background,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            when (settingOption) {
                SettingOption.SPEED_PRESETS -> SettingSpeedPreset(scaffoldState)
                SettingOption.THEME_MODE    -> SettingThemeMode(scaffoldState)
                SettingOption.JOYSTICK_SIZE -> SettingJoystickSize(scaffoldState)
                SettingOption.RANDOM_OFFSET -> SettingRandomOffset(scaffoldState)
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
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                item {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = stringResource(R.string.settings_section_general),
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
                                    stringResource(R.string.settings_item_speed_presets),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        settingOption = SettingOption.THEME_MODE
                                        scope.launch {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Palette,
                                    contentDescription = null
                                )
                                Text(
                                    stringResource(R.string.settings_item_theme_mode),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        settingOption = SettingOption.JOYSTICK_SIZE
                                        scope.launch {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Gamepad,
                                    contentDescription = null
                                )
                                Text(
                                    stringResource(R.string.settings_item_joystick_size),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = stringResource(R.string.settings_section_mock_options),
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
                                        settingOption = SettingOption.RANDOM_OFFSET
                                        scope.launch {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Shuffle,
                                    contentDescription = null
                                )
                                Text(
                                    stringResource(R.string.settings_item_random_offset),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = stringResource(R.string.settings_section_about),
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
                                        val intent = Intent(Intent.ACTION_VIEW,
                                            "https://github.com/Malpl3naInk/MockLocation-releases"
                                                .toUri()
                                        )
                                        context.startActivity(intent)
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
                                    stringResource(R.string.settings_item_release_repo),
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
                                    stringResource(R.string.settings_item_oss_license),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = stringResource(R.string.settings_section_debug),
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
                                            Intent.createChooser(intent, strOpenLog)
                                        )
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Description,
                                    contentDescription = null
                                )
                                Text(
                                    stringResource(R.string.settings_item_export_log),
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
                                            stringResource(R.string.settings_item_crash_test),
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