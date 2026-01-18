package ink.moling.mocklocation.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import ink.moling.mocklocation.data.models.RouteItem
import java.io.File

object PrefsHelper {
    private const val PREFS_NAME = "MockLocation"
    private const val KEY_MOCK_MODE = "mock_mode"
    private const val KEY_SELECTED_POINT_ID = "selected_point_id"
    private const val KEY_SELECTED_ROUTE_NAME = "selected_route_name"
    private const val KEY_SELECTED_ROUTE_PATH = "selected_route_path"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setMockMode(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_MOCK_MODE, value) }
    }

    fun getMockMode(context: Context): String {
        return getPrefs(context).getString(KEY_MOCK_MODE, "Point") ?: "Point"
    }

    /**
     * 保存选中的模拟点 ID
     */
    fun setSelectedPointId(context: Context, pointId: Long?) {
        getPrefs(context).edit {
            if (pointId != null) {
                putLong(KEY_SELECTED_POINT_ID, pointId)
            } else {
                remove(KEY_SELECTED_POINT_ID)
            }
        }
    }

    /**
     * 获取选中的模拟点 ID
     */
    fun getSelectedPointId(context: Context): Long? {
        val prefs = getPrefs(context)
        val id = prefs.getLong(KEY_SELECTED_POINT_ID, -1L)
        return if (id == -1L) null else id
    }

    /**
     * 保存选中的模拟路径
     */
    fun setSelectedRoute(context: Context, route: RouteItem?) {
        getPrefs(context).edit {
            if (route != null) {
                putString(KEY_SELECTED_ROUTE_NAME, route.displayName)
                putString(KEY_SELECTED_ROUTE_PATH, route.file.absolutePath)
            } else {
                remove(KEY_SELECTED_ROUTE_NAME)
                remove(KEY_SELECTED_ROUTE_PATH)
            }
        }
    }

    /**
     * 获取选中的模拟路径
     */
    fun getSelectedRoute(context: Context): RouteItem? {
        val prefs = getPrefs(context)
        val name = prefs.getString(KEY_SELECTED_ROUTE_NAME, null) ?: return null
        val path = prefs.getString(KEY_SELECTED_ROUTE_PATH, null) ?: return null

        val file = File(path)
        if (!file.exists()) return null

        return RouteItem(
            displayName = name,
            file = file
        )
    }
}