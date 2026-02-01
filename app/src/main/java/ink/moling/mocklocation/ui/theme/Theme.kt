package ink.moling.mocklocation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary         = Color(0xFFD0BCFF),
    secondary       = Color(0xFFCCC2DC),
    tertiary        = Color(0xFFEFB8C8),
)

private val LightColorScheme = lightColorScheme(
    primary             = Color(0xFF2962FF),
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFE3F2FD),
    onPrimaryContainer  = Color(0xFF0D47A1),
    inversePrimary      = Color(0xFF82B1FF),

    secondary           = Color(0xFF455A64),
    onSecondary         = Color.Gray,
    secondaryContainer  = Color(0xFFCFD8DC),
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
    surface             = Color(0xFFF5F5F5),
    onSurface           = Color(0xFF1A1C1E),
    surfaceVariant      = Color(0xFFE3E8EE),
    onSurfaceVariant    = Color(0xFF42474E),

    outline             = Color(0xFF72777F),
    outlineVariant      = Color(0xFFC2C7CF),

    inverseSurface      = Color(0xFF2F3033),
    inverseOnSurface    = Color(0xFFF1F3F4),

    scrim = Color.Black,
)

@Composable
fun MockLocationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}