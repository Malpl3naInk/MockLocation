package ink.moling.mocklocation.activity

import android.content.Context
import androidx.activity.ComponentActivity
import ink.moling.mocklocation.utils.LocaleHelper

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        newBase?.let { LocaleHelper.initSystemLocale(it) }
        super.attachBaseContext(newBase?.let { LocaleHelper.setLocale(it) })
    }

    override fun onResume() {
        super.onResume()
        if (LocaleHelper.hasLanguageChanged(this)) {
            recreate()
        }
    }
}
