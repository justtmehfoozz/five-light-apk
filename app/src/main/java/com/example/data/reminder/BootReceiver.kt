package com.example.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.util.PrayerCalc
import com.example.data.util.NaflCalc
import com.example.data.model.NaflPreferences
import java.util.Date

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            SmartPrayerNotificationManager.createNotificationChannel(context)

            // Load preferences / defaults to calculate prayer times
            val prefs = context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)
            val lat = prefs.getFloat("lat", 21.4225f).toDouble()
            val lng = prefs.getFloat("lng", 39.8262f).toDouble()

            val calculatedPrayers = PrayerCalc.calculatePrayerTimes(
                latitude = lat,
                longitude = lng,
                date = Date()
            )

            val naflPrefs = NaflPreferences(
                tahajjudEnabled = prefs.getBoolean("nafl_tahajjud_enabled", false),
                ishraqEnabled = prefs.getBoolean("nafl_ishraq_enabled", false),
                duhaEnabled = prefs.getBoolean("nafl_duha_enabled", false),
                awwabinEnabled = prefs.getBoolean("nafl_awwabin_enabled", false)
            )
            
            val naflWindows = NaflCalc.calculateNaflTimes(calculatedPrayers, naflPrefs)

            val manager = SmartPrayerNotificationManager(context)
            manager.scheduleSmartNotifications(calculatedPrayers, naflWindows)
        }
    }
}
