package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.util.AdhanDebugLogger

/**
 * Dedicated, autonomous receiver for Adhan alarms.
 * Directly initiates AdhanService via ContextCompat.startForegroundService.
 */
class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: "UNKNOWN"
        val dateStr = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE) ?: "UNKNOWN"
        val eventType = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE) ?: "UNKNOWN"

        AdhanDebugLogger.log(
            context,
            "[RECEIVER FIRED] AdhanAlarmReceiver received alarm broadcast (Action=${intent.action}, Event=$eventType, Prayer=$prayerName, Date=$dateStr, Pid=${android.os.Process.myPid()})"
        )

        PrayerNotificationLogger.logValidationSuccess(
            "adhan_alarm_receiver",
            prayerName,
            dateStr,
            "AdhanAlarmReceiver received alarm intent (action=${intent.action})"
        )

        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            action = AdhanService.ACTION_START_ADHAN
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, dateStr)
            putExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE, eventType)
        }

        AdhanDebugLogger.log(
            context,
            "[SERVICE START REQUESTED] Calling ContextCompat.startForegroundService for AdhanService (Prayer=$prayerName, Date=$dateStr)"
        )

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
            AdhanDebugLogger.log(
                context,
                "[SERVICE START SUCCESS] ContextCompat.startForegroundService executed successfully for AdhanService"
            )
        } catch (e: Exception) {
            AdhanDebugLogger.log(
                context,
                "[SERVICE START ERROR] ContextCompat.startForegroundService failed: ${e.message}"
            )
        }
    }
}
