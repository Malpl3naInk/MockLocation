package ink.moling.mocklocation.utils

/**
 * 模拟模式枚举
 */
enum class MockMode {
    POINT,  // 单点模拟
    ROUTE   // 路线模拟
}

/**
 * 底部表单页面枚举
 */
enum class WaypointSheet {
    POINTS,  // 路点列表
    DETAIL   // 详情编辑
}

/**
 * 路线显示模式枚举
 */
enum class WaypointGraph {
    CANVAS,  // Canvas散点图
    MAP      // Mapbox地图
}
