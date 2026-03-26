package ink.moling.mocklocation.activity.settings.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.local.AppLanguage
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.utils.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingLanguage(
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    val currentLanguage = PrefsHelper.getLanguage(context)

    Column(
        modifier = Modifier
            .padding(24.dp)
    ) {
        Text(
            stringResource(R.string.settings_language_title),
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
                LanguageOption(
                    label = stringResource(R.string.settings_language_system),
                    isSelected = currentLanguage == AppLanguage.SYSTEM,
                    onClick = {
                        PrefsHelper.setLanguage(context, AppLanguage.SYSTEM)
                        updateLocaleAndRestart(context, AppLanguage.SYSTEM)
                    }
                )
                LanguageOption(
                    label = "English",
                    isSelected = currentLanguage == AppLanguage.ENGLISH,
                    onClick = {
                        PrefsHelper.setLanguage(context, AppLanguage.ENGLISH)
                        updateLocaleAndRestart(context, AppLanguage.ENGLISH)
                    }
                )
                LanguageOption(
                    label = "中文",
                    isSelected = currentLanguage == AppLanguage.CHINESE,
                    onClick = {
                        PrefsHelper.setLanguage(context, AppLanguage.CHINESE)
                        updateLocaleAndRestart(context, AppLanguage.CHINESE)
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            if (isSelected) Icons.Filled.Check else Icons.Outlined.Check,
            contentDescription = null,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                LocalContentColor.current.copy(alpha = 0.0f)
        )
        Text(
            label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun updateLocaleAndRestart(context: Context, language: AppLanguage) {
    // Recreate activity to apply language change
    val activity = context as? Activity
    activity?.recreate()
}
