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
                // Show standard notification with actions (Snooze / Dismiss)
                manager.showPrayerNotification(prayerName, notificationId)

                // Trigger in-app full screen overlay
                if (manager.azaanSoundEnabled) {
                    PrayerReminderManager.triggerAzaanOverlay(prayerName)
                }
            }

            PrayerReminderManager.ACTION_SNOOZE_REMINDER -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(notificationId)
                manager.scheduleSnooze(prayerName)
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
