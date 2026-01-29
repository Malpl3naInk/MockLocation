package ink.moling.mocklocation.ui.components

import androidx.compose.runtime.Composable
import ink.moling.mocklocation.BuildConfig

@Composable
fun DebugOnly(content: @Composable () -> Unit) {
    if (BuildConfig.DEBUG) {
        content()
    }
}
