package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.PrayerName

class PrayerReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val manager = PrayerReminderManager(context)
        val prayerNameString = intent.getStringExtra(PrayerReminderManager.EXTRA_PRAYER_NAME)
        val notificationId = intent.getIntExtra(PrayerReminderManager.EXTRA_NOTIFICATION_ID, 1001)

        val prayerName = try {
            if (prayerNameString != null) PrayerName.valueOf(prayerNameString) else PrayerName.FAJR
        } catch (e: Exception) {
            PrayerName.FAJR
        }

        when (intent.action) {
            PrayerReminderManager.ACTION_PRAYER_REMINDER -> {
                PrayerNotificationLogger.logValidationFailure(
                    eventType = "legacy_prayer_reminder",
                    prayerNameStr = prayerName.name,
                    dateStr = "unknown",
                    reason = "Obsolete legacy PrayerReminderReceiver intent received. Rejecting and cancelling alarm."
                )
                try {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                } catch (_: Exception) {}
            }

            PrayerReminderManager.ACTION_SNOOZE_REMINDER -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(notificationId)
                PrayerReminderManager.dismissAzaanOverlay()
            }

            PrayerReminderManager.ACTION_DISMISS_REMINDER -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(notificationId)
                PrayerReminderManager.dismissAzaanOverlay()
            }
        }
    }
}
