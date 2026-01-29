package ink.moling.mocklocation.utils.logger

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object LoggerFile {
    private const val MAX_LOG_COUNT = 20
    private const val DIR_NAME = "logs"

    private lateinit var logDir: File
    private lateinit var logFile: File

    private val lock = ReentrantLock()
    private val formatter =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        logDir = File(context.filesDir, DIR_NAME)
        if (!logDir.exists()) logDir.mkdirs()

        cleanupOldLogs()

        val sessionTime = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss",
            Locale.US
        ).format(Date())

        logFile = File(logDir, "app_$sessionTime.log")
        logFile.createNewFile()
    }

    fun write(level: String, tag: String?, msg: String) {
        if (!::logFile.isInitialized) return

        val time = formatter.format(Date())
        val line = "$time [$level/$tag] $msg\n"

        lock.withLock {
            FileWriter(logFile, true).use {
                it.write(line)
            }
        }
    }

    private fun cleanupOldLogs() {
        val files = logDir.listFiles { file ->
            file.isFile && file.name.endsWith(".log")
        } ?: return

        if (files.size <= MAX_LOG_COUNT) return

        files
            .sortedByDescending { it.lastModified() }
            .drop(MAX_LOG_COUNT)
            .forEach { it.delete() }
    }

    fun writeThrowable(
        level: String,
        tag: String?,
        msg: String,
        tr: Throwable?
    ) {
        write(
            level,
            tag,
            "$msg\n${Log.getStackTraceString(tr)}"
        )
    }

    fun getCurrentLogFile(): File? =
        if (::logFile.isInitialized) logFile else null
}
