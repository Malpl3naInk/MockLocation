package ink.moling.mocklocation.data.models

import java.io.File

/**
 * 路径项数据模型
 * @param displayName 显示名称
 * @param file 路径文件
 */
data class RouteItem(
    val displayName: String,
    val file: File
)

