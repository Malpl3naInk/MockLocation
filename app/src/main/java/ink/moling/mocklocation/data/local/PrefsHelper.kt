package ink.moling.mocklocation.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefsHelper {
    private const val PREFS_NAME = "MockLocation"
    private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_MOCK_MODE = "mock_mode"
    private const val KEY_SELECTED_POINT_ID = "selected_point_id"
    private const val KEY_SELECTED_ROUTE_ID = "selected_route_id"
    private const val KEY_MAX_SPEED_JOYSTICK_PRESET_0 = "max_speed_joystick_preset_0"
    private const val KEY_MAX_SPEED_JOYSTICK_PRESET_1 = "max_speed_joystick_preset_1"
    private const val KEY_MAX_SPEED_JOYSTICK_PRESET_2 = "max_speed_joystick_preset_2"
    private const val KEY_MAX_SPEED_ROUTE_PRESET_0 = "max_speed_route_preset_0"
    private const val KEY_MAX_SPEED_ROUTE_PRESET_1 = "max_speed_route_preset_1"
    private const val KEY_MAX_SPEED_ROUTE_PRESET_2 = "max_speed_route_preset_2"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getIsFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setIsFirstLaunch(context: Context) {
        getPrefs(context).edit { putBoolean(KEY_IS_FIRST_LAUNCH, false) }
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

    fun setMaxSpeedPresetsJoystick(context: Context, maxSpeed: List<Double>) {
        getPrefs(context).edit {
            putFloat(KEY_MAX_SPEED_JOYSTICK_PRESET_0, maxSpeed[0].toFloat())
            putFloat(KEY_MAX_SPEED_JOYSTICK_PRESET_1, maxSpeed[1].toFloat())
            putFloat(KEY_MAX_SPEED_JOYSTICK_PRESET_2, maxSpeed[2].toFloat())
        }
    }

    fun getMaxSpeedPresetsJoystick(context: Context): List<Double> {
        return listOf(
            getPrefs(context).getFloat(KEY_MAX_SPEED_JOYSTICK_PRESET_0, 5.0f).toDouble(),
            getPrefs(context).getFloat(KEY_MAX_SPEED_JOYSTICK_PRESET_1, 12.0f).toDouble(),
            getPrefs(context).getFloat(KEY_MAX_SPEED_JOYSTICK_PRESET_2, 25.0f).toDouble()
        )
    }

    fun setMaxSpeedPresetsRoute(context: Context, maxSpeed: List<Double>) {
        getPrefs(context).edit {
            putFloat(KEY_MAX_SPEED_ROUTE_PRESET_0, maxSpeed[0].toFloat())
            putFloat(KEY_MAX_SPEED_ROUTE_PRESET_1, maxSpeed[1].toFloat())
            putFloat(KEY_MAX_SPEED_ROUTE_PRESET_2, maxSpeed[2].toFloat())
        }
    }

    fun getMaxSpeedPresetsRoute(context: Context): List<Double> {
        return listOf(
            getPrefs(context).getFloat(KEY_MAX_SPEED_ROUTE_PRESET_0, 5.0f).toDouble(),
            getPrefs(context).getFloat(KEY_MAX_SPEED_ROUTE_PRESET_1, 12.0f).toDouble(),
            getPrefs(context).getFloat(KEY_MAX_SPEED_ROUTE_PRESET_2, 25.0f).toDouble()
        )
    }
}