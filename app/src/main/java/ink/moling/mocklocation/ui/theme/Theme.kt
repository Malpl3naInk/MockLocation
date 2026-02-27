package ink.moling.mocklocation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ink.moling.mocklocation.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary             = Color(0xFF2962FF),
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF1A3A7A),
    onPrimaryContainer  = Color(0xFFD6E4FF),
    inversePrimary      = Color(0xFF2962FF),

    secondary           = Color(0xFFB0BEC5),
    onSecondary         = Color(0xFF8A9099),
    secondaryContainer  = Color(0xFF3A3F46),
    onSecondaryContainer = Color(0xFFCFD8DC),

    tertiary            = Color(0xFF4DD0E1),
    onTertiary          = Color(0xFF00363A),
    tertiaryContainer   = Color(0xFF004F56),
    onTertiaryContainer = Color(0xFFB2EBF2),

    error               = Color(0xFFFF5252),
    onError             = Color.White,
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6),

    background          = Color(0xFF212121),
    onBackground        = Color(0xFFE3E6EA),
    surface             = Color(0xFF424242),
    onSurface           = Color(0xFFE3E6EA),
    surfaceVariant      = Color(0xFF2A2F36),
    onSurfaceVariant    = Color(0xFFC2C7CF),

    outline             = Color(0xFF8A9099),
    outlineVariant      = Color(0xFF3A3F46),

    inverseSurface      = Color(0xFFE3E6EA),
    inverseOnSurface    = Color(0xFF2F3033),

    scrim = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary             = Color(0xFF2962FF),
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF0D47A1),
    onPrimaryContainer  = Color(0xFFE3F2FD),
    inversePrimary      = Color(0xFF82B1FF),

    secondary           = Color(0xFF455A64),
    onSecondary         = Color.Gray,
    secondaryContainer  = Color(0xFF53C1CD),
    onSecondaryContainer = Color(0xFF102027),

    tertiary            = Color(0xFF00838F),
    onTertiary          = Color.LightGray,
    tertiaryContainer   = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF00363A),

    error               = Color(0xFFFF5252),
    onError             = Color.White,
    errorContainer      = Color(0xFFF9DEDC),
    onErrorContainer    = Color(0xFF410E0B),

    background          = Color(0xFFF9FAFC),
    onBackground        = Color(0xFF1A1C1E),
    surface             = Color(0xFFCFD8DC),
    onSurface           = Color(0xFF1A1C1E),
    surfaceVariant      = Color(0xFFE3E8EE),
    onSurfaceVariant    = Color(0xFF42474E),

    outline             = Color(0xFF72777F),
    outlineVariant      = Color(0xFFC2C7CF),

    inverseSurface      = Color(0xFF2F3033),
    inverseOnSurface    = Color(0xFFF1F3F4),

    scrim = Color.Black,
)

object ThemeStateHolder {
    val themeMode = mutableStateOf(ThemeMode.SYSTEM)
}

@Composable
fun MockLocationTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeMode by ThemeStateHolder.themeMode
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}