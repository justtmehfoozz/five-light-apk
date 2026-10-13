package com.example.util

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object CrashLogger {
    private const val FILE_NAME = "last_crash.txt"
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrashReport(appContext ?: context, thread, throwable)
            } catch (_: Throwable) {
                // Prevent crash inside logger
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun log(throwable: Throwable, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null) {
            try {
                writeCrashReport(ctx, Thread.currentThread(), throwable)
            } catch (_: Throwable) {
            }
        }
    }

    fun getCrashReport(context: Context): String? {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists() && file.length() > 0) {
                file.readText()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun deleteCrashReport(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }

    private fun writeCrashReport(context: Context, thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val fullStackTrace = sw.toString()

        val versionName = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${pInfo.versionName} (${pInfo.versionCode})"
        } catch (_: Exception) {
            "Unknown"
        }

        val report = """
            |Thread: ${thread.name}
            |App Version: $versionName
            |Device Model: ${Build.MODEL}
            |Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            |
            |Stack Trace:
            |$fullStackTrace
        """.trimMargin()

        val file = File(context.filesDir, FILE_NAME)
        file.writeText(report)
    }
}
