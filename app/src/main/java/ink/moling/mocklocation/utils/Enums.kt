package ink.moling.mocklocation.utils

/**
 * 模拟模式枚举
 */
enum class MockMode {
    POINT,  // 单点模拟
    ROUTE   // 路线模拟
}

/**
 * Int 转 MockMode
 */
fun intToMockMode(value: Int): MockMode = when (value) {
    1 -> MockMode.ROUTE
    else -> MockMode.POINT
}

/**
 * MockMode 转 Int
 */
fun enumsToInt(mode: MockMode): Int = when (mode) {
    MockMode.POINT -> 0
    MockMode.ROUTE -> 1
}

/**
 * 底部表单页面枚举
 */
enum class WaypointSheet {
    POINTS,  // 路点列表
    DETAIL   // 详情编辑
}
