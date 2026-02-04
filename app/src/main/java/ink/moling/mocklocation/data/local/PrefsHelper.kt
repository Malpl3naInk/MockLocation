package ink.moling.mocklocation.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefsHelper {
    private const val PREFS_NAME = "MockLocation"
    private const val KEY_MOCK_MODE = "mock_mode"
    private const val KEY_SELECTED_POINT_ID = "selected_point_id"
    private const val KEY_SELECTED_ROUTE_ID = "selected_route_id"
    private const val KEY_SELECTED_ROUTE_PATH = "selected_route_path"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setMockMode(context: Context, value: Int) {
        getPrefs(context).edit { putInt(KEY_MOCK_MODE, value) }
    }

    fun getMockMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_MOCK_MODE, 0)
    }

    fun setSelectedPointId(context: Context, pointId: Long?) {
        getPrefs(context).edit {
            if (pointId != null) {
                putLong(KEY_SELECTED_POINT_ID, pointId)
            } else {
                remove(KEY_SELECTED_POINT_ID)
            }
        }
    }

    fun getSelectedPointId(context: Context): Long? {
        val prefs = getPrefs(context)
        val id = prefs.getLong(KEY_SELECTED_POINT_ID, -1L)
        return if (id == -1L) null else id
    }

    fun setSelectedRouteId(context: Context, routeId: Long?) {
        getPrefs(context).edit {
            if (routeId != null) {
                putLong(KEY_SELECTED_ROUTE_ID, routeId)
            } else {
                remove(KEY_SELECTED_ROUTE_ID)
            }
        }
    }

    fun getSelectedRouteId(context: Context): Long? {
        val prefs = getPrefs(context)
        val id = prefs.getLong(KEY_SELECTED_ROUTE_ID, -1L)
        return if (id == -1L) null else id
    }
}