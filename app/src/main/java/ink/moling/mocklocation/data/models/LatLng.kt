package ink.moling.mocklocation.data.models

/**
 * 经纬度数据模型
 * @param lat 纬度
 * @param lng 经度
 * @param type 类型
 * @param connections 连接点索引列表
 */
data class LatLng(
    val lat: Double,
    val lng: Double,
    val type: String = "",
    val connections: List<Int> = emptyList()
)

