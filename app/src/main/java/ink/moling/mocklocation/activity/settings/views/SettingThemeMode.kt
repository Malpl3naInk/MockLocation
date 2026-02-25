package ink.moling.mocklocation.activity.settings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.data.local.ThemeMode
import ink.moling.mocklocation.ui.theme.ThemeStateHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingThemeMode(
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    var currentTheme by ThemeStateHolder.themeMode

    Column(
        modifier = Modifier
            .padding(24.dp)
    ) {
        Text(
            stringResource(R.string.settings_theme_mode_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
                            currentTheme = ThemeMode.SYSTEM
                            PrefsHelper.setThemeMode(context, currentTheme)
                        }
                        .padding(12.dp)
                ) {
                    Icon(
                        if (currentTheme == ThemeMode.SYSTEM)
                            Icons.Filled.BrightnessAuto
                        else Icons.Outlined.BrightnessAuto,
                        contentDescription = null,
                        tint = if (currentTheme == ThemeMode.SYSTEM)
                            MaterialTheme.colorScheme.primary
                        else
                            LocalContentColor.current
                    )
                    Text(
                        stringResource(R.string.settings_theme_mode_system),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentTheme = ThemeMode.LIGHT
                            PrefsHelper.setThemeMode(context, currentTheme)
                        }
                        .padding(12.dp)
                ) {
                    Icon(
                        if (currentTheme == ThemeMode.LIGHT)
                            Icons.Filled.LightMode
                        else Icons.Outlined.LightMode,
                        contentDescription = null,
                        tint = if (currentTheme == ThemeMode.LIGHT)
                            MaterialTheme.colorScheme.primary
                        else
                            LocalContentColor.current
                    )
                    Text(
                        stringResource(R.string.settings_theme_mode_light),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentTheme = ThemeMode.DARK
                            PrefsHelper.setThemeMode(context, currentTheme)
                        }
                        .padding(12.dp)
                ) {
                    Icon(
                        if (currentTheme == ThemeMode.DARK)
                            Icons.Filled.DarkMode
                        else Icons.Outlined.DarkMode,
                        contentDescription = null,
                        tint = if (currentTheme == ThemeMode.DARK)
                            MaterialTheme.colorScheme.primary
                        else
                            LocalContentColor.current
                    )
                    Text(
                        stringResource(R.string.settings_theme_mode_dark),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}