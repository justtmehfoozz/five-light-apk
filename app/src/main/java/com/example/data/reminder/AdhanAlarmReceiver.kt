package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Dedicated receiver for Adhan alarms, delegating to SmartPrayerNotificationReceiver
 * with structured diagnostic logging for background execution and verification.
 */
class AdhanAlarmReceiver : BroadcastReceiver() {
    private val delegate = SmartPrayerNotificationReceiver()

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: "UNKNOWN"
        val dateStr = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE) ?: "UNKNOWN"
        com.example.util.AdhanDebugLogger.log(
            context,
            "[RECEIVER FIRED] AdhanAlarmReceiver received alarm broadcast (Action=${intent.action}, Prayer=$prayerName, Date=$dateStr, Pid=${android.os.Process.myPid()})"
        )
        PrayerNotificationLogger.logValidationSuccess(
            "adhan_alarm_receiver",
            prayerName,
            dateStr,
            "AdhanAlarmReceiver received alarm intent (action=${intent.action})"
        )
        delegate.onReceive(context, intent)
    }
}
