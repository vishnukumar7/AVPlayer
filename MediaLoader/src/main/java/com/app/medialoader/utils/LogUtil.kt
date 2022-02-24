package com.app.medialoader.utils

import android.util.Log
import com.app.medialoader.MediaLoader

object LogUtil {


    private const val LOG_FORMAT = "%1\$s\n%2\$s"

    @Volatile
    private var writeDebugLogs = false

    @Volatile
    private var writeLogs = true

    fun writeDebugLogs(writeDebugLogs: Boolean) {
        LogUtil.writeDebugLogs = writeDebugLogs
    }

    fun writeLogs(writeLogs: Boolean) {
        LogUtil.writeLogs = writeLogs
    }

    fun d(message: String, vararg args: Any) {
        if (writeDebugLogs) {
            LogUtil.log(Log.DEBUG, null, message, *args)
        }
    }

    fun i(message: String, vararg args: Any) {
        LogUtil.log(Log.INFO, null, message, *args)
    }

    fun w(message: String, vararg args: Any) {
        LogUtil.log(Log.WARN, null, message, *args)
    }

    fun e(ex: Throwable) {
        LogUtil.log(Log.ERROR, ex, "null")
    }

    fun e(message: String, vararg args: Any) {
        LogUtil.log(Log.ERROR, null, message, *args)
    }

    fun e(ex: Throwable?, message: String, vararg args: Any) {
        LogUtil.log(Log.ERROR, ex, message, *args)
    }

    private fun log(priority: Int, ex: Throwable?, message: String, vararg args: Any) {
        var message: String? = message
        if (!writeLogs) {
            return
        }
        if (args.isNotEmpty()) {
            message = String.format(message!!, *args)
        }
        val log: String? = if (ex == null) {
            message
        } else {
            val logMessage = message ?: ex.message
            val logBody = Log.getStackTraceString(ex)
            String.format(LOG_FORMAT, logMessage, logBody)
        }
        Log.println(priority, MediaLoader.TAG, log!!)
    }
}