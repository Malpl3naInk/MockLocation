package ink.moling.mocklocation.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object FileHelper {

    /* -------------------- Import -------------------- */

    /**
     * 从 SAF Uri 导入文件到 app 内部 filesDir
     * @return 导入后的 File
     */
    fun importToFilesDir(context: Context, uri: Uri): File {
        val fileName = getFileName(context, uri)
        val target = File(context.filesDir, fileName)

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
        fileName: String,
        targetUri: Uri
    ) {
        val source = File(context.filesDir, fileName)
        require(source.exists()) { "File not found: $fileName" }

        context.contentResolver.openOutputStream(targetUri)?.use { output ->
            source.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: error("Cannot open output stream for uri")
    }

    /* -------------------- Runtime IO -------------------- */

    fun readText(context: Context, fileName: String): String {
        val file = File(context.filesDir, fileName)
        require(file.exists()) { "File not found: $fileName" }
        return file.readText()
    }

    fun writeText(context: Context, fileName: String, text: String) {
        val file = File(context.filesDir, fileName)
        file.writeText(text)
    }

    fun listFiles(context: Context): List<File> {
        return context.filesDir.listFiles()?.toList() ?: emptyList()
    }

    fun delete(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
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