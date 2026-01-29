package ink.moling.mocklocation.utils.logger

import android.os.Process
import android.util.Log
import kotlin.system.exitProcess

class CrashHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            LoggerFile.writeThrowable(
                level = "FATAL",
                tag = "Crash",
                msg = "Thread=${t.name}",
                tr = e
            )
        } catch (ex: Throwable) {
            // 防止二次崩溃
            Log.e("CrashHandler", "log failed", ex)
        }

        // 交还系统处理（弹系统崩溃对话框）
        defaultHandler?.uncaughtException(t, e)
            ?: run {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
    }
}