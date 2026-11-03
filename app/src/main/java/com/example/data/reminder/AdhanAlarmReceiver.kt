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
        val triggerMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, 0L)

        val now = System.currentTimeMillis()
        val delayMs = if (triggerMillis > 0) now - triggerMillis else 0L

        AdhanDebugLogger.log(
            context,
            "[RECEIVER FIRED] AdhanAlarmReceiver received alarm broadcast (Action=${intent.action}, Event=$eventType, Prayer=$prayerName, Date=$dateStr, TriggerMillis=$triggerMillis, DelayMs=$delayMs, Pid=${android.os.Process.myPid()})"
        )

        PrayerNotificationLogger.logValidationSuccess(
            "adhan_alarm_receiver",
            prayerName,
            dateStr,
            "AdhanAlarmReceiver received alarm intent (action=${intent.action})"
        )

        // Stale Alarm Guard: Ignore fard prayer alarms triggered more than 2 minutes after expected trigger time
        val isFardEvent = eventType == SmartPrayerNotificationManager.EVENT_TYPE_FARD ||
                eventType == SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP
        if (isFardEvent && triggerMillis > 0 && (now - triggerMillis > 2 * 60 * 1000L)) {
            AdhanDebugLogger.log(
                context,
                "[STALE ALARM IGNORED] Prayer=$prayerName, Date=$dateStr, TriggerMillis=$triggerMillis, Now=$now, DelayMs=$delayMs > 120000ms"
            )
            return
        }

        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            action = AdhanService.ACTION_START_ADHAN
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, dateStr)
            putExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE, eventType)
            putExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, triggerMillis)
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
