package ink.moling.mocklocation.utils.logger

import android.util.Log

object Logger {
    fun v(tag: String?, msg: String) {
        Log.v(tag, msg)
        LoggerFile.write("V", tag, msg)
    }
    fun v(tag: String?, msg: String, tr: Throwable?) {
        Log.v(tag, msg, tr)
        LoggerFile.writeThrowable("V", tag, msg, tr)
    }

    fun d(tag: String?, msg: String) {
        Log.d(tag, msg)
        LoggerFile.write("D", tag, msg)
    }
    fun d(tag: String?, msg: String, tr: Throwable?) {
        Log.d(tag, msg, tr)
        LoggerFile.writeThrowable("D", tag, msg, tr)
    }

    fun i(tag: String?, msg: String) {
        Log.i(tag, msg)
        LoggerFile.write("I", tag, msg)
    }
    fun i(tag: String?, msg: String, tr: Throwable?) {
        Log.i(tag, msg, tr)
        LoggerFile.writeThrowable("I", tag, msg, tr)
    }

    fun w(tag: String?, msg: String) {
        Log.w(tag, msg)
        LoggerFile.write("W", tag, msg)
    }
    fun w(tag: String?, msg: String, tr: Throwable?) {
        Log.w(tag, msg, tr)
        LoggerFile.writeThrowable("W", tag, msg, tr)
    }

    fun e(tag: String?, msg: String) {
        Log.e(tag, msg)
        LoggerFile.write("E", tag, msg)
    }
    fun e(tag: String?, msg: String, tr: Throwable?) {
        Log.e(tag, msg, tr)
        LoggerFile.writeThrowable("E", tag, msg, tr)
    }
}