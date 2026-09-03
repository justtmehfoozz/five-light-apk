package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.repository.AppRepository
import com.example.data.util.NaflCalc

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            SmartPrayerNotificationManager.createNotificationChannels(context)

            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val repo = AppRepository(db, context.applicationContext)
                val calculatedPrayers = repo.getTodayPrayerTimes()
                val is24H = repo.timeFormat.value.is24Hour
                val naflWindows = NaflCalc.calculateNaflTimes(
                    fardPrayers = calculatedPrayers,
                    preferences = repo.naflPreferences.value,
                    is24Hour = is24H
                )
                val todayStr = repo.getTodayDateString()

                val manager = SmartPrayerNotificationManager(context)
                manager.scheduleSmartNotifications(calculatedPrayers, naflWindows, todayStr)
            } catch (_: Exception) {}
        }
    }
}
