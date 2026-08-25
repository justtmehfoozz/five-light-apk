package com.example.data.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.Madhab
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.util.PrayerCalc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date

class PrayerReminderManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("fivelight_reminders_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "fivelight_prayer_reminders"
        const val CHANNEL_NAME = "Prayer Time Reminders"
        
        const val ACTION_PRAYER_REMINDER = "com.example.ACTION_PRAYER_REMINDER"
        const val ACTION_SNOOZE_REMINDER = "com.example.ACTION_SNOOZE_REMINDER"
        const val ACTION_DISMISS_REMINDER = "com.example.ACTION_DISMISS_REMINDER"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        private val _activeAzaanState = MutableStateFlow<ActiveAzaanState?>(null)
        val activeAzaanState: StateFlow<ActiveAzaanState?> = _activeAzaanState.asStateFlow()

        fun triggerAzaanOverlay(prayerName: PrayerName) {
            _activeAzaanState.value = ActiveAzaanState(prayerName)
        }

        fun dismissAzaanOverlay() {
            _activeAzaanState.value = null
        }
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Muslim daily prayer times"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Master enabled setting
    var masterEnabled: Boolean
        get() = prefs.getBoolean("master_reminders_enabled", true)
        set(value) {
            prefs.edit().putBoolean("master_reminders_enabled", value).apply()
            rescheduleAll()
        }

    // Azaan sound enabled setting
    var azaanSoundEnabled: Boolean
        get() = prefs.getBoolean("azaan_sound_enabled", true)
        set(value) {
            prefs.edit().putBoolean("azaan_sound_enabled", value).apply()
        }

    // Get config for a specific prayer
    fun getPrayerConfig(prayerName: PrayerName): PrayerReminderConfig {
        val enabled = prefs.getBoolean("reminder_enabled_${prayerName.name}", true)
        val offset = prefs.getInt("reminder_offset_${prayerName.name}", 0)
        return PrayerReminderConfig(prayerName, enabled, offset)
    }

    // Save config for a specific prayer
    fun savePrayerConfig(prayerName: PrayerName, enabled: Boolean, offsetMinutes: Int) {
        prefs.edit()
            .putBoolean("reminder_enabled_${prayerName.name}", enabled)
            .putInt("reminder_offset_${prayerName.name}", offsetMinutes)
            .apply()
        rescheduleAll()
    }

    // Master enable / disable all
    fun setAllPrayerRemindersEnabled(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean("master_reminders_enabled", enabled)
        val prayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        for (p in prayers) {
            editor.putBoolean("reminder_enabled_${p.name}", enabled)
        }
        editor.apply()
        rescheduleAll()
    }

    // Check battery optimization exemption
    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    // Intent to request battery optimization exemption
    fun createBatteryOptimizationIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations()) {
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + context.packageName)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else null
    }

    // Reschedule all enabled alarms based on current settings
    fun rescheduleAll() {
        val mainPrefs = context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)
        val savedCityName = mainPrefs.getString("selected_city_name", null)
        val city = com.example.data.repository.AppRepository(
            com.example.data.db.AppDatabase.getDatabase(context), context
        ).PREDEFINED_CITIES.find { it.cityName == savedCityName } ?: com.example.data.repository.AppRepository(
            com.example.data.db.AppDatabase.getDatabase(context), context
        ).PREDEFINED_CITIES[0]

        val savedCalcMethod = mainPrefs.getString("calc_method", null)
        val method = CalcMethod.entries.find { it.name == savedCalcMethod } ?: city.defaultCalcMethod

        val savedMadhab = mainPrefs.getString("madhab", null)
        val madhab = Madhab.entries.find { it.name == savedMadhab } ?: Madhab.STANDARD

        rescheduleForParams(city, method, madhab)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun rescheduleForParams(city: CityLocation, method: CalcMethod, madhab: Madhab) {
        cancelAllAlarms()

        if (!masterEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule for today and tomorrow
        for (dayOffset in 0..1) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val prayerTimes = PrayerCalc.calculatePrayerTimes(
                latitude = city.latitude,
                longitude = city.longitude,
                date = cal.time,
                method = method,
                madhab = madhab,
                timeZoneOffsetHours = city.timezoneOffsetHours
            )

            for (pItem in prayerTimes) {
                if (pItem.name == PrayerName.SUNRISE) continue // Only main 5 prayers

                val config = getPrayerConfig(pItem.name)
                if (!config.enabled) continue

                val reminderTimeMillis = pItem.timeMillis - (config.offsetMinutes * 60 * 1000L)
                if (reminderTimeMillis > System.currentTimeMillis()) {
                    val requestCode = (pItem.name.ordinal * 10) + dayOffset
                    val intent = Intent(context, PrayerReminderReceiver::class.java).apply {
                        action = ACTION_PRAYER_REMINDER
                        putExtra(EXTRA_PRAYER_NAME, pItem.name.name)
                        putExtra(EXTRA_NOTIFICATION_ID, requestCode)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminderTimeMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                reminderTimeMillis,
                                pendingIntent
                            )
                        }
                    } catch (e: SecurityException) {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }

    fun cancelAllAlarms() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        for (dayOffset in 0..1) {
            for (p in prayers) {
                val requestCode = (p.ordinal * 10) + dayOffset
                val intent = Intent(context, PrayerReminderReceiver::class.java).apply {
                    action = ACTION_PRAYER_REMINDER
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }

    // Schedule 5-minute snooze alarm
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleSnooze(prayerName: PrayerName) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (5 * 60 * 1000L)
        val requestCode = 9990 + prayerName.ordinal

        val intent = Intent(context, PrayerReminderReceiver::class.java).apply {
            action = ACTION_PRAYER_REMINDER
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    // Show Notification with Action Buttons (Snooze 5 min & Dismiss)
    fun showPrayerNotification(prayerName: PrayerName, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeIntent = Intent(context, PrayerReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE_REMINDER
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Action
        val dismissIntent = Intent(context, PrayerReminderReceiver::class.java).apply {
            action = ACTION_DISMISS_REMINDER
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isFriday = com.example.data.util.PrayerDisplayUtils.isFriday()
        val displayName = com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(prayerName, isFriday)
        val subtext = com.example.data.util.PrayerDisplayUtils.getPrayerPoeticSubtext(prayerName, isFriday)
        val title = "$displayName Prayer Reminder"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(subtext)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Snooze 5 min", snoozePendingIntent)
            .addAction(0, "Dismiss", dismissPendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }

    fun getPrayerPoeticSubtext(prayerName: PrayerName): String {
        return when (prayerName) {
            PrayerName.FAJR -> "Dawn breaks with stillness and promise"
            PrayerName.SUNRISE -> "Morning light fills the horizon"
            PrayerName.DHUHR -> "Midday sun reaches its zenith"
            PrayerName.ASR -> "Afternoon light softens into warmth"
            PrayerName.MAGHRIB -> "Day yields to twilight's glow"
            PrayerName.ISHA -> "Night settles into quiet peace"
        }
    }
}
