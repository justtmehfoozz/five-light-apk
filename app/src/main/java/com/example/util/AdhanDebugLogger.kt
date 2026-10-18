package com.example.util

import android.content.Context
import android.util.Log
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Diagnostic logger for FiveLight Adhan alarm scheduling, receiver execution,
 * service lifecycle, audio playback, and full-screen activity launches.
 * Persists entries to disk so logs survive process termination and device locks.
 */
object AdhanDebugLogger {
    private const val TAG = "AdhanDebug"
    private const val FILE_NAME = "adhan_debug_log.txt"
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    @Synchronized
    fun log(context: Context?, message: String) {
        val timestamp = try {
            LocalDateTime.now().format(formatter)
        } catch (_: Exception) {
            System.currentTimeMillis().toString()
        }
        val line = "[$timestamp] $message"
        Log.d(TAG, line)

        if (context != null) {
            try {
                val file = File(context.applicationContext.filesDir, FILE_NAME)
                file.appendText("$line\n")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write adhan debug log: ${e.message}")
            }
        }
    }

    fun getLogText(context: Context): String {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists() && file.length() > 0) {
                file.readLines().asReversed().joinToString("\n")
            } else {
                "No Adhan debug logs recorded yet."
            }
        } catch (e: Exception) {
            "Error reading Adhan debug log: ${e.message}"
        }
    }

    fun clearLog(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
    }
}
