package com.example.data.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.NaflPrayerItem
import com.example.data.model.NaflType
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class PrePrayerReminderOffset(val minutes: Int, val label: String) {
    OFF(0, "Off"),
    MIN_5(5, "5 min"),
    MIN_10(10, "10 min"),
    MIN_15(15, "15 min"),
    MIN_30(30, "30 min")
}

class SmartPrayerNotificationManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("smart_prayer_notifications_prefs", Context.MODE_PRIVATE)
    private val oldPrefs = context.getSharedPreferences("prayer_reminders_prefs", Context.MODE_PRIVATE)

    var isSmartNotificationsEnabled: Boolean
        get() {
            if (!prefs.contains(KEY_SMART_ENABLED) && oldPrefs.contains("global_enabled")) {
                val oldVal = oldPrefs.getBoolean("global_enabled", true)
                prefs.edit().putBoolean(KEY_SMART_ENABLED, oldVal).apply()
                return oldVal
            }
            return prefs.getBoolean(KEY_SMART_ENABLED, true)
        }
        set(value) = prefs.edit().putBoolean(KEY_SMART_ENABLED, value).apply()

    var isPrayerTimeNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PRAYER_TIME_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_PRAYER_TIME_ENABLED, value).apply()

    var preReminderOffset: PrePrayerReminderOffset
        get() {
            if (!prefs.contains(KEY_PRE_REMINDER_MINS) && oldPrefs.contains("pre_reminder_mins")) {
                val oldMins = oldPrefs.getInt("pre_reminder_mins", 0)
                prefs.edit().putInt(KEY_PRE_REMINDER_MINS, oldMins).apply()
            }
            val mins = prefs.getInt(KEY_PRE_REMINDER_MINS, 0)
            return PrePrayerReminderOffset.entries.find { it.minutes == mins } ?: PrePrayerReminderOffset.OFF
        }
        set(value) = prefs.edit().putInt(KEY_PRE_REMINDER_MINS, value.minutes).apply()

    var isContextualRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONTEXTUAL_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CONTEXTUAL_ENABLED, value).apply()

    var isNaflOpportunitiesEnabled: Boolean
        get() = prefs.getBoolean(KEY_NAFL_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_NAFL_ENABLED, value).apply()

    fun isPrayerEnabled(prayerName: String): Boolean {
        return prefs.getBoolean("prayer_enabled_$prayerName", true)
    }

    fun setPrayerEnabled(prayerName: String, enabled: Boolean) {
        prefs.edit().putBoolean("prayer_enabled_$prayerName", enabled).apply()
    }

    fun getSummaryText(): String {
        if (!isSmartNotificationsEnabled) return "Disabled"
        if (isPrayerTimeNotificationsEnabled &&
            preReminderOffset == PrePrayerReminderOffset.OFF &&
            !isContextualRemindersEnabled &&
            !isNaflOpportunitiesEnabled
        ) {
            return "Prayer times only"
        }
        return "Enabled"
    }

    fun isNotificationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    fun openBatteryOptimizationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try { context.startActivity(fallback) } catch (_: Exception) {}
        }
    }

    fun openAppNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun sendTestNotification(): Boolean {
        createNotificationChannels(context)
        val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_TEST)
        }
        context.sendBroadcast(intent)
        return true
    }

    fun scheduleSmartNotifications(
        prayerTimes: List<PrayerItem>,
        naflWindows: List<NaflPrayerItem> = emptyList()
    ) {
        createNotificationChannels(context)
        cancelAllSchedules()

        if (!isSmartNotificationsEnabled || prayerTimes.isEmpty()) {
            return
        }

        val nowMillis = System.currentTimeMillis()
        val todayDate = java.time.LocalDate.now().toString()

        // 1. Fard Prayer Time Notifications & Pre-Prayer Reminders
        prayerTimes.forEachIndexed { index, prayer ->
            if (prayer.name == PrayerName.SUNRISE) return@forEachIndexed // Sunrise is handled under contextual/nafl

            if (!isPrayerEnabled(prayer.name.id)) return@forEachIndexed

            val prayerTriggerMillis = prayer.timeMillis
            val nextPrayer = prayerTimes.getOrNull(index + 1)
            val windowEndMillis = nextPrayer?.timeMillis ?: (prayerTriggerMillis + 4 * 3600 * 1000L)
            val formattedTime = prayer.timeFormatted

            // Fard Prayer Time Entry Notification
            if (isPrayerTimeNotificationsEnabled) {
                if (prayerTriggerMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_FARD)
                        putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
                        putExtra(EXTRA_PRAYER_TIME, formattedTime)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, prayerTriggerMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, windowEndMillis)
                    }
                    val requestCode = REQ_CODE_FARD_BASE + prayer.name.ordinal
                    scheduleAlarm(prayerTriggerMillis, intent, requestCode)
                }

                // Follow-up Notification (scheduled ~25 mins after entry if still unrecorded)
                val followUpMillis = prayerTriggerMillis + 25 * 60 * 1000L
                if (followUpMillis > nowMillis && followUpMillis < windowEndMillis) {
                    val followUpIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_FARD_FOLLOWUP)
                        putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
                        putExtra(EXTRA_PRAYER_TIME, formattedTime)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, followUpMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, windowEndMillis)
                    }
                    val followUpReqCode = REQ_CODE_FARD_FOLLOWUP_BASE + prayer.name.ordinal
                    scheduleAlarm(followUpMillis, followUpIntent, followUpReqCode)
                }
            }

            // Pre-Prayer Reminder
            if (preReminderOffset != PrePrayerReminderOffset.OFF) {
                val preMillis = prayerTriggerMillis - (preReminderOffset.minutes * 60 * 1000L)
                if (preMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_PRE_PRAYER)
                        putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
                        putExtra(EXTRA_PRAYER_TIME, formattedTime)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_OFFSET_MINS, preReminderOffset.minutes)
                        putExtra(EXTRA_TRIGGER_MILLIS, preMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, prayerTriggerMillis)
                    }
                    val requestCode = REQ_CODE_PRE_BASE + prayer.name.ordinal
                    scheduleAlarm(preMillis, intent, requestCode)
                }
            }
        }

        // 2. Contextual Reminders (Sunrise, Maghrib transition, Tahajjud)
        if (isContextualRemindersEnabled) {
            // Sunrise / Morning
            prayerTimes.find { it.name == PrayerName.SUNRISE }?.let { sunrise ->
                val sunriseMillis = sunrise.timeMillis
                if (sunriseMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_CONTEXTUAL_MORNING)
                        putExtra(EXTRA_PRAYER_TIME, sunrise.timeFormatted)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, sunriseMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, sunriseMillis + 45 * 60 * 1000L)
                    }
                    scheduleAlarm(sunriseMillis, intent, REQ_CODE_CONTEXTUAL_MORNING)
                }
            }

            // Evening (Maghrib)
            prayerTimes.find { it.name == PrayerName.MAGHRIB }?.let { maghrib ->
                val maghribMillis = maghrib.timeMillis
                if (maghribMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_CONTEXTUAL_EVENING)
                        putExtra(EXTRA_PRAYER_TIME, maghrib.timeFormatted)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, maghribMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, maghribMillis + 45 * 60 * 1000L)
                    }
                    scheduleAlarm(maghribMillis, intent, REQ_CODE_CONTEXTUAL_EVENING)
                }
            }

            // Night (Tahajjud window)
            naflWindows.find { it.type == NaflType.TAHAJJUD }?.let { tahajjud ->
                val tahajjudMillis = tahajjud.startMillis
                if (tahajjudMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_CONTEXTUAL_NIGHT)
                        putExtra(EXTRA_WINDOW_TEXT, tahajjud.timeFormatted)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, tahajjudMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, tahajjud.endMillis)
                    }
                    scheduleAlarm(tahajjudMillis, intent, REQ_CODE_CONTEXTUAL_NIGHT)
                }
            }
        }

        // 3. Nafl Opportunities
        if (isNaflOpportunitiesEnabled) {
            // Duha
            naflWindows.find { it.type == NaflType.DUHA }?.let { duha ->
                val duhaMillis = duha.startMillis
                if (duhaMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_NAFL_DUHA)
                        putExtra(EXTRA_WINDOW_TEXT, duha.timeFormatted)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, duhaMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, duha.endMillis)
                    }
                    scheduleAlarm(duhaMillis, intent, REQ_CODE_NAFL_DUHA)
                }
            }

            // Tahajjud
            naflWindows.find { it.type == NaflType.TAHAJJUD }?.let { tahajjud ->
                val tahajjudMillis = tahajjud.startMillis
                if (tahajjudMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_NAFL_TAHAJJUD)
                        putExtra(EXTRA_WINDOW_TEXT, tahajjud.timeFormatted)
                        putExtra(EXTRA_PRAYER_DATE, todayDate)
                        putExtra(EXTRA_TRIGGER_MILLIS, tahajjudMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, tahajjud.endMillis)
                    }
                    scheduleAlarm(tahajjudMillis, intent, REQ_CODE_NAFL_TAHAJJUD)
                }
            }
        }
    }

    private fun scheduleAlarm(triggerAtMillis: Long, intent: Intent, requestCode: Int) {
        val nowMillis = System.currentTimeMillis()
        if (triggerAtMillis <= nowMillis) {
            // CRITICAL: NEVER schedule an alarm in the past! AlarmManager fires it immediately.
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    fun cancelFollowUp(prayerName: PrayerName) {
        val intent = Intent(context, SmartPrayerNotificationReceiver::class.java)
        val code = REQ_CODE_FARD_FOLLOWUP_BASE + prayerName.ordinal
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllSchedules() {
        val intent = Intent(context, SmartPrayerNotificationReceiver::class.java)
        val allRequestCodes = listOf(
            100, 101, 102, 103, 104, 105,
            200, 201, 202, 203, 204, 205,
            301, 302, 303,
            401, 402,
            500, 501, 502, 503, 504, 505
        )
        allRequestCodes.forEach { code ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    companion object {
        const val CHANNEL_SMART_PRAYER = "fivelight_smart_prayer"
        const val CHANNEL_PRAYER_TIME = "fivelight_prayer_time"
        const val CHANNEL_ADHAN = "fivelight_adhan_audio"
        const val CHANNEL_QUIET_REMINDER = "fivelight_quiet_reminder"

        const val CHANNEL_ID = CHANNEL_SMART_PRAYER

        const val KEY_SMART_ENABLED = "smart_enabled"
        const val KEY_PRAYER_TIME_ENABLED = "prayer_time_enabled"
        const val KEY_PRE_REMINDER_MINS = "pre_reminder_mins"
        const val KEY_CONTEXTUAL_ENABLED = "contextual_enabled"
        const val KEY_NAFL_ENABLED = "nafl_enabled"

        const val EXTRA_EVENT_TYPE = "extra_event_type"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_PRAYER_DATE = "extra_prayer_date"
        const val EXTRA_TRIGGER_MILLIS = "extra_trigger_millis"
        const val EXTRA_WINDOW_END_MILLIS = "extra_window_end_millis"
        const val EXTRA_NOTIF_ID = "extra_notif_id"
        const val EXTRA_OFFSET_MINS = "extra_offset_mins"
        const val EXTRA_WINDOW_TEXT = "extra_window_text"

        const val ACTION_MARK_PRAYED = "com.example.ACTION_MARK_PRAYED"
        const val ACTION_UNDO_PRAYED = "com.example.ACTION_UNDO_PRAYED"

        const val EVENT_TYPE_TEST = "test_smart_prayer"
        const val EVENT_TYPE_FARD = "fard_prayer"
        const val EVENT_TYPE_FARD_FOLLOWUP = "fard_prayer_followup"
        const val EVENT_TYPE_PRE_PRAYER = "pre_prayer"
        const val EVENT_TYPE_CONTEXTUAL_MORNING = "contextual_morning"
        const val EVENT_TYPE_CONTEXTUAL_EVENING = "contextual_evening"
        const val EVENT_TYPE_CONTEXTUAL_NIGHT = "contextual_night"
        const val EVENT_TYPE_NAFL_DUHA = "nafl_duha"
        const val EVENT_TYPE_NAFL_TAHAJJUD = "nafl_tahajjud"

        const val REQ_CODE_FARD_BASE = 100
        const val REQ_CODE_PRE_BASE = 200
        const val REQ_CODE_CONTEXTUAL_MORNING = 301
        const val REQ_CODE_CONTEXTUAL_EVENING = 302
        const val REQ_CODE_CONTEXTUAL_NIGHT = 303
        const val REQ_CODE_NAFL_DUHA = 401
        const val REQ_CODE_NAFL_TAHAJJUD = 402
        const val REQ_CODE_FARD_FOLLOWUP_BASE = 500

        fun createNotificationChannel(context: Context) {
            createNotificationChannels(context)
        }

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val smartPrayerChannel = NotificationChannel(
                    CHANNEL_SMART_PRAYER,
                    "Smart Prayer",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Contextual and intelligent reminders for prayer and spiritual moments"
                    enableVibration(true)
                }

                val prayerTimeChannel = NotificationChannel(
                    CHANNEL_PRAYER_TIME,
                    "Prayer Time",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications when a fard prayer time enters or is approaching"
                    enableVibration(true)
                }

                val adhanChannel = NotificationChannel(
                    CHANNEL_ADHAN,
                    "Adhan & Prayer Audio",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Audio call to prayer notifications"
                    enableVibration(true)
                }

                val quietReminderChannel = NotificationChannel(
                    CHANNEL_QUIET_REMINDER,
                    "Quiet Prayer Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Gentle reminders for voluntary prayers and dhikr"
                    enableVibration(false)
                }

                notificationManager.createNotificationChannels(
                    listOf(smartPrayerChannel, prayerTimeChannel, adhanChannel, quietReminderChannel)
                )
            }
        }
    }
}
