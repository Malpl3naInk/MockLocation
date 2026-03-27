package ink.moling.mocklocation.activity.osslicense

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.data.local.PrefsHelper
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.ui.theme.ThemeStateHolder
import ink.moling.mocklocation.utils.LocaleHelper

class OssLicenseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        // 尽早初始化系统语言（在 setLocale 修改 Locale.getDefault() 之前）
        newBase?.let { LocaleHelper.initSystemLocale(it) }
        super.attachBaseContext(newBase?.let { LocaleHelper.setLocale(it) })
    }

    override fun onResume() {
        super.onResume()
        // 检查语言是否变化，如果变化则重新创建 Activity
        if (LocaleHelper.hasLanguageChanged(this)) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeStateHolder.themeMode.value = PrefsHelper.getThemeMode(this)

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OssLicenseScreen()
                }
            }
        }
    }
}
