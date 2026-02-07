package ink.moling.mocklocation.utils

///**
// * 从文件加载路径数据（使用 RouteObject 数据模型）
// */
//fun loadRouteFromFile(file: File): RouteObject? {
//    return try {
//        val jsonText = FileHelper.readText(file)
//        val gson = Gson()
//        val routeObject = gson.fromJson(jsonText, RouteObject::class.java)
//
//        Logger.d("RouteModeView", "Loaded route: ${routeObject.name}, type: ${routeObject.meta.type}, version: ${routeObject.meta.version}")
//        Logger.d("RouteModeView", "Loaded ${routeObject.points.size} points")
//
//        routeObject
//    } catch (e: Exception) {
//        Logger.e("RouteModeView", "Error loading route from JSON: ${e.message}", e)
//        null
//    }
//}
//
///**
// * 路径文件验证结果
// */
//data class ValidationResult(
//    val isValid: Boolean,
//    val routeName: String? = null,
//    val errorMessage: String? = null
//)
//
///**
// * 验证路径文件是否符合 RouteObject 格式
// */
//fun validateRouteFile(context: Context, uri: Uri): ValidationResult {
//    try {
//        // 读取文件内容
//        val jsonText = context.contentResolver.openInputStream(uri)?.use { inputStream ->
//            inputStream.bufferedReader().use { it.readText() }
//        } ?: return ValidationResult(false, errorMessage = "Cannot read file")
//
//        if (jsonText.isBlank()) {
//            return ValidationResult(false, errorMessage = "File is empty")
//        }
//
//        // 使用 Gson 解析为 RouteObject
//        val gson = Gson()
//        val routeObject = try {
//            gson.fromJson(jsonText, RouteObject::class.java)
//        } catch (e: JsonSyntaxException) {
//            return ValidationResult(false, errorMessage = "Invalid JSON format: ${e.message}")
//        } catch (e: Exception) {
//            return ValidationResult(false, errorMessage = "Parse error: ${e.message}")
//        }
//
//        // 验证必要字段
//        if (routeObject.name.isBlank()) {
//            return ValidationResult(false, errorMessage = "Missing route name")
//        }
//
//        if (routeObject.points.isEmpty()) {
//            return ValidationResult(false, errorMessage = "Route has no points")
//        }
//
//        // 验证点的数据
//        routeObject.points.forEachIndexed { index, point ->
//            // 验证经纬度范围
//            if (point.lat !in -90.0..90.0) {
//                return ValidationResult(
//                    false,
//                    errorMessage = "Invalid latitude at point $index: ${point.lat}"
//                )
//            }
//            if (point.lng !in -180.0..180.0) {
//                return ValidationResult(
//                    false,
//                    errorMessage = "Invalid longitude at point $index: ${point.lng}"
//                )
//            }
//
//            // 验证 type 字段
//            if (point.type.isBlank()) {
//                return ValidationResult(
//                    false,
//                    errorMessage = "Missing type at point $index"
//                )
//            }
//        }
//
//        // 验证连接关系（可选：检查连接的ID是否存在）
//        val pointIds = routeObject.points.map { it.id }.toSet()
//        routeObject.points.forEachIndexed { index, point ->
//            point.connects.forEach { connectId ->
//                if (connectId !in pointIds) {
//                    Logger.w(
//                        "ImportExportDialog",
//                        "Warning: Point ${point.id} connects to non-existent point $connectId"
//                    )
//                }
//            }
//        }
//
//        Logger.d(
//            "ImportExportDialog",
//            "Validation passed: ${routeObject.name}, ${routeObject.points.size} points, type: ${routeObject.meta.type}"
//        )
//
//        return ValidationResult(
//            isValid = true,
//            routeName = routeObject.name
//        )
//
//    } catch (e: Exception) {
//        Logger.e("ImportExportDialog", "Validation error", e)
//        return ValidationResult(false, errorMessage = "Unexpected error: ${e.message}")
//    }
//}