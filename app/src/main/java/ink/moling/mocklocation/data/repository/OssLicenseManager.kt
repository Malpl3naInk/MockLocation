package ink.moling.mocklocation.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ink.moling.mocklocation.data.models.OssLicense

object OssLicenseManager {

    private const val ASSETS_OPEN_SOURCE_LICENSES = "open_source_licenses.json"
    private const val ASSETS_EXTERNAL_LICENSES = "external_oss_licenses.json"

    private val gson = Gson()

    fun loadLicenses(context: Context): List<OssLicense> {
        val baseLicenses = loadLicensesFromAsset(context, ASSETS_OPEN_SOURCE_LICENSES)
        val externalLicenses = loadLicensesFromAsset(context, ASSETS_EXTERNAL_LICENSES)

        // 使用 external 覆盖 base，以 dependency 为 key
        val licenseMap = baseLicenses.associateBy { it.dependency }.toMutableMap()

        externalLicenses.forEach { external ->
            external.dependency?.let { dependency ->
                licenseMap[dependency] = external
            }
        }

        // 按 project 名称去重（不同 platform 变体如 -android 和 -jvmstubs 有相同 project 名）
        // 保留第一个（通常是 -android）
        val seenProjects = mutableSetOf<String>()
        val uniqueLicenses = licenseMap.values.filter { license ->
            val key = license.project.lowercase()
            if (key in seenProjects) {
                false
            } else {
                seenProjects.add(key)
                true
            }
        }

        // 按项目名称排序返回
        return uniqueLicenses.sortedBy { it.project.lowercase() }
    }

    private fun loadLicensesFromAsset(context: Context, fileName: String): List<OssLicense> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                val content = inputStream.bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<OssLicense>>() {}.type
                gson.fromJson(content, type)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
