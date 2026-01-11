package ink.moling.mocklocation.utils

import android.util.Log
import com.google.gson.Gson
import ink.moling.mocklocation.data.models.RouteObject
import java.io.File

/**
 * 从文件加载路径数据（使用 RouteObject 数据模型）
 */
fun loadRouteFromFile(file: File): RouteObject? {
    return try {
        val jsonText = FileHelper.readText(file)
        val gson = Gson()
        val routeObject = gson.fromJson(jsonText, RouteObject::class.java)

        Log.d("RouteModeView", "Loaded route: ${routeObject.name}, type: ${routeObject.meta.type}, version: ${routeObject.meta.version}")
        Log.d("RouteModeView", "Loaded ${routeObject.points.size} points")

        routeObject
    } catch (e: Exception) {
        Log.e("RouteModeView", "Error loading route from JSON: ${e.message}", e)
        null
    }
}