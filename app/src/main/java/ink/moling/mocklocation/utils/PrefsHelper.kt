package ink.moling.mocklocation.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefsHelper {
    private const val PREFS_NAME = "MockLocation"
    private const val KEY_MOCK_MODE = "mock_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setMockMode(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_MOCK_MODE, value) }
    }

    fun getMockMode(context: Context): String {
        return getPrefs(context).getString(KEY_MOCK_MODE, "Point") ?: "Point"
    }
}