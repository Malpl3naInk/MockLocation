package ink.moling.mocklocation.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.*

object FileHelper {

    /* -------------------- Import -------------------- */

    /**
     * 导入文件到 app 内部 filesDir/routes/ 目录
     * 并生成随机文件名（保留原后缀）
     *
     * @return 导入后的 File
     */
    fun importToRoutesDir(context: Context, uri: Uri): File {
        val routesDir = File(context.filesDir, "routes")
        if (!routesDir.exists()) routesDir.mkdirs()

        // 获取原始文件名，提取后缀
        val originalName = getFileName(context, uri)
        val suffix = originalName.substringAfterLast('.', "")

        // 随机文件名
        val randomName = UUID.randomUUID().toString() + if (suffix.isNotBlank()) ".$suffix" else ""

        val target = File(routesDir, randomName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Cannot open input stream for uri")

        return target
    }

    /* -------------------- Export -------------------- */

    /**
     * 从 filesDir 导出文件到 SAF Uri
     */
    fun exportFromFilesDir(
        context: Context,
        file: File,
        targetUri: Uri
    ) {
        require(file.exists()) { "File not found: ${file.absolutePath}" }

        context.contentResolver.openOutputStream(targetUri)?.use { output ->
            file.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: error("Cannot open output stream for uri")
    }

    /* -------------------- Runtime IO -------------------- */

    fun readText(file: File): String {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        return file.readText()
    }

    fun writeText(file: File, text: String) {
        file.writeText(text)
    }

    fun listRouteFiles(context: Context): List<File> {
        val routesDir = File(context.filesDir, "routes")
        if (!routesDir.exists()) return emptyList()
        return routesDir.listFiles()?.toList() ?: emptyList()
    }

    fun deleteFile(file: File): Boolean {
        return file.exists() && file.delete()
    }

    /* -------------------- Utils -------------------- */

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "imported_file"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(index)
            }
        }
        return name
    }
}