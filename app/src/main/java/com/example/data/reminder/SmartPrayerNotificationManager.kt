package com.example.data.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.data.model.NaflPrayerItem
import com.example.data.model.NaflType
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.util.PrayerCalc
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
        naflWindows: List<NaflPrayerItem> = emptyList(),
        dateString: String? = null
    ) {
        createNotificationChannels(context)

        val targetDate = parseDateOrToday(dateString)
        val targetDateStr = targetDate.toString()

        if (!isSmartNotificationsEnabled || prayerTimes.isEmpty()) {
            if (!isSmartNotificationsEnabled) {
                cancelSchedulesForDate(targetDate)
            }
            return
        }

        val nowMillis = System.currentTimeMillis()

        // 1. Fard Prayer Time Notifications & Pre-Prayer Reminders
        prayerTimes.forEachIndexed { index, prayer ->
            if (prayer.name == PrayerName.SUNRISE) return@forEachIndexed // Sunrise is handled under contextual/nafl

            if (!isPrayerEnabled(prayer.name.id)) {
                cancelAlarmsForPrayer(prayer.name, targetDateStr)
                cancelPrayerNotification(prayer.name, targetDateStr)
                return@forEachIndexed
            }

            val prayerTriggerMillis = prayer.timeMillis
            val nextPrayer = prayerTimes.getOrNull(index + 1)
            val windowEndMillis = nextPrayer?.timeMillis ?: (prayerTriggerMillis + 4 * 3600 * 1000L)
            val formattedTime = prayer.timeFormatted

            // Fard Prayer Time Entry Notification
            if (isPrayerTimeNotificationsEnabled) {
                if (prayerTriggerMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://prayer/$targetDateStr/${prayer.name.name}/entry")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_FARD)
                        putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
                        putExtra(EXTRA_PRAYER_TIME, formattedTime)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, prayerTriggerMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, windowEndMillis)
                    }
                    val requestCode = getAlarmRequestCode(prayer.name, targetDate, ALARM_SLOT_ENTRY)
                    scheduleAlarm(prayerTriggerMillis, intent, requestCode)
                }

                // Follow-up Notification (scheduled ~25 mins after entry if still unrecorded)
                val followUpMillis = prayerTriggerMillis + 25 * 60 * 1000L
                if (followUpMillis > nowMillis && followUpMillis < windowEndMillis) {
                    val followUpIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://prayer/$targetDateStr/${prayer.name.name}/followup")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_FARD_FOLLOWUP)
                        putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
                        putExtra(EXTRA_PRAYER_TIME, formattedTime)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, followUpMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, windowEndMillis)
                    }
                    val followUpReqCode = getAlarmRequestCode(prayer.name, targetDate, ALARM_SLOT_FOLLOWUP)
                    scheduleAlarm(followUpMillis, followUpIntent, followUpReqCode)
                }
            } else {
                cancelAlarmByRequestCode(getAlarmRequestCode(prayer.name, targetDate, ALARM_SLOT_ENTRY))
                cancelAlarmByRequestCode(getAlarmRequestCode(prayer.name, targetDate, ALARM_SLOT_FOLLOWUP))
            }

            // Pre-Prayer Reminder
            if (preReminderOffset != PrePrayerReminderOffset.OFF) {
                val preMillis = prayerTriggerMillis - (preReminderOffset.minutes * 60 * 1000L)
                if (preMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://prayer/$targetDateStr/${prayer.name.name}/pre")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_PRE_PRAYER)
                        putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
                        putExtra(EXTRA_PRAYER_TIME, formattedTime)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_OFFSET_MINS, preReminderOffset.minutes)
                        putExtra(EXTRA_TRIGGER_MILLIS, preMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, prayerTriggerMillis)
                    }
                    val requestCode = getAlarmRequestCode(prayer.name, targetDate, ALARM_SLOT_PRE_REMINDER)
                    scheduleAlarm(preMillis, intent, requestCode)
                }
            } else {
                val preReqCode = getAlarmRequestCode(prayer.name, targetDate, ALARM_SLOT_PRE_REMINDER)
                cancelAlarmByRequestCode(preReqCode)
            }
        }

        // 2. Contextual Reminders (Sunrise/Morning, Maghrib/Evening, Tahajjud/Night)
        if (isContextualRemindersEnabled) {
            // Sunrise / Morning
            prayerTimes.find { it.name == PrayerName.SUNRISE }?.let { sunrise ->
                val sunriseMillis = sunrise.timeMillis
                if (sunriseMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://contextual/$targetDateStr/morning")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_CONTEXTUAL_MORNING)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, sunriseMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, sunriseMillis + 45 * 60 * 1000L)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_MORNING)
                    scheduleAlarm(sunriseMillis, intent, reqCode)
                }
            }

            // Evening (Maghrib transition)
            prayerTimes.find { it.name == PrayerName.MAGHRIB }?.let { maghrib ->
                val maghribMillis = maghrib.timeMillis
                // Avoid collision: If Fard Maghrib prayer notification will be sent at maghribMillis,
                // do NOT send the redundant "Evening Has Begun" contextual reminder at the exact same moment.
                val maghribFardWillNotify = isPrayerTimeNotificationsEnabled && isPrayerEnabled(PrayerName.MAGHRIB.id)
                if (!maghribFardWillNotify && maghribMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://contextual/$targetDateStr/evening")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_CONTEXTUAL_EVENING)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, maghribMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, maghribMillis + 45 * 60 * 1000L)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_EVENING)
                    scheduleAlarm(maghribMillis, intent, reqCode)
                } else if (maghribFardWillNotify) {
                    cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_EVENING))
                    cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_CONTEXTUAL_EVENING)
                }
            }

            // Night (Quiet portion of night transition)
            // Avoid collision: If Nafl Tahajjud notification is active/scheduled, Tahajjud Window will notify the user.
            // Do not send a colliding "Night Has Begun" at the exact same moment.
            val tahajjudItem = naflWindows.find { it.type == NaflType.TAHAJJUD }
            val tahajjudWindowPair: Pair<Long, Long>? = tahajjudItem?.let { Pair(it.startMillis, it.endMillis) }
                ?: PrayerCalc.calculateTahajjudWindow(prayerTimes)?.let { Pair(it.startMillis, it.endMillis) }
            val tahajjudNaflWillNotify = isNaflOpportunitiesEnabled && tahajjudItem != null

            if (!tahajjudNaflWillNotify && tahajjudWindowPair != null) {
                val tahajjudMillis = tahajjudWindowPair.first
                val tahajjudEndMillis = tahajjudWindowPair.second
                if (tahajjudMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://contextual/$targetDateStr/night")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_CONTEXTUAL_NIGHT)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, tahajjudMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, tahajjudEndMillis)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_NIGHT)
                    scheduleAlarm(tahajjudMillis, intent, reqCode)
                }
            } else if (tahajjudNaflWillNotify) {
                cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_NIGHT))
                cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_CONTEXTUAL_NIGHT)
            }
        } else {
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_MORNING))
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_EVENING))
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_CONTEXTUAL_NIGHT))
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_CONTEXTUAL_MORNING)
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_CONTEXTUAL_EVENING)
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_CONTEXTUAL_NIGHT)
        }

        // 3. Nafl Opportunities
        if (isNaflOpportunitiesEnabled) {
            val ishraqItem = naflWindows.find { it.type == NaflType.ISHRAQ }
            val duhaItem = naflWindows.find { it.type == NaflType.DUHA }
            val tahajjudItem = naflWindows.find { it.type == NaflType.TAHAJJUD }
            val awwabinItem = naflWindows.find { it.type == NaflType.AWWABIN }

            // Ishraq
            if (ishraqItem != null) {
                val ishraqMillis = ishraqItem.startMillis
                if (ishraqMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://nafl/$targetDateStr/ishraq")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_NAFL_ISHRAQ)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, ishraqMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, ishraqItem.endMillis)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_ISHRAQ)
                    scheduleAlarm(ishraqMillis, intent, reqCode)
                }
            } else {
                cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_ISHRAQ))
                cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_ISHRAQ)
            }

            // Duha
            if (duhaItem != null) {
                val duhaTriggerMillis = duhaItem.startMillis

                if (duhaTriggerMillis > nowMillis && duhaTriggerMillis < duhaItem.endMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://nafl/$targetDateStr/duha")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_NAFL_DUHA)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, duhaTriggerMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, duhaItem.endMillis)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_DUHA)
                    scheduleAlarm(duhaTriggerMillis, intent, reqCode)
                }
            } else {
                cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_DUHA))
                cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_DUHA)
            }

            // Tahajjud
            if (tahajjudItem != null) {
                val tahajjudMillis = tahajjudItem.startMillis
                if (tahajjudMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://nafl/$targetDateStr/tahajjud")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_NAFL_TAHAJJUD)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, tahajjudMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, tahajjudItem.endMillis)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_TAHAJJUD)
                    scheduleAlarm(tahajjudMillis, intent, reqCode)
                }
            } else {
                cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_TAHAJJUD))
                cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_TAHAJJUD)
            }

            // Awwabin (if enabled in preferences/naflWindows)
            if (awwabinItem != null) {
                val awwabinMillis = awwabinItem.startMillis
                if (awwabinMillis > nowMillis) {
                    val intent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                        action = ACTION_SMART_PRAYER_NOTIF
                        data = Uri.parse("fivelight://nafl/$targetDateStr/awwabin")
                        putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_NAFL_AWWABIN)
                        putExtra(EXTRA_PRAYER_DATE, targetDateStr)
                        putExtra(EXTRA_TRIGGER_MILLIS, awwabinMillis)
                        putExtra(EXTRA_WINDOW_END_MILLIS, awwabinItem.endMillis)
                    }
                    val reqCode = getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_AWWABIN)
                    scheduleAlarm(awwabinMillis, intent, reqCode)
                }
            } else {
                cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_AWWABIN))
                cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_AWWABIN)
            }
        } else {
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_DUHA))
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_TAHAJJUD))
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_ISHRAQ))
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(targetDate, SPECIAL_SLOT_NAFL_AWWABIN))
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_DUHA)
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_TAHAJJUD)
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_ISHRAQ)
            cancelSpecialNotification(targetDateStr, SPECIAL_SLOT_NAFL_AWWABIN)
        }
    }

    fun scheduleFollowUpAlarm(prayerName: PrayerName, dateStr: String, followUpMillis: Long, windowEndMillis: Long) {
        val targetDate = parseDateOrToday(dateStr)
        val followUpIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
            action = ACTION_SMART_PRAYER_NOTIF
            data = Uri.parse("fivelight://prayer/$dateStr/${prayerName.name}/followup")
            putExtra(EXTRA_EVENT_TYPE, EVENT_TYPE_FARD_FOLLOWUP)
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_PRAYER_DATE, dateStr)
            putExtra(EXTRA_TRIGGER_MILLIS, followUpMillis)
            putExtra(EXTRA_WINDOW_END_MILLIS, windowEndMillis)
        }
        val followUpReqCode = getAlarmRequestCode(prayerName, targetDate, ALARM_SLOT_FOLLOWUP)
        scheduleAlarm(followUpMillis, followUpIntent, followUpReqCode)
    }

    internal fun scheduleAlarm(triggerAtMillis: Long, intent: Intent, requestCode: Int) {
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

    fun cancelPrayerNotification(prayerName: PrayerName, dateStr: String = LocalDate.now().toString()) {
        val notifId = getNotificationId(prayerName, dateStr)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notifId)
    }

    fun cancelSpecialNotification(dateStr: String?, slot: Int) {
        val notifId = getSpecialNotificationId(dateStr, slot)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notifId)
    }

    fun cancelFollowUpAlarm(prayerName: PrayerName, dateStr: String = LocalDate.now().toString()) {
        val reqCode = getAlarmRequestCode(prayerName, dateStr, ALARM_SLOT_FOLLOWUP)
        cancelAlarmByRequestCode(reqCode)
    }

    fun cancelFollowUp(prayerName: PrayerName) {
        cancelFollowUpAlarm(prayerName, LocalDate.now().toString())
    }

    fun cancelAlarmsForPrayer(prayerName: PrayerName, dateStr: String = LocalDate.now().toString()) {
        val entryCode = getAlarmRequestCode(prayerName, dateStr, ALARM_SLOT_ENTRY)
        val preCode = getAlarmRequestCode(prayerName, dateStr, ALARM_SLOT_PRE_REMINDER)
        val followUpCode = getAlarmRequestCode(prayerName, dateStr, ALARM_SLOT_FOLLOWUP)
        cancelAlarmByRequestCode(entryCode)
        cancelAlarmByRequestCode(preCode)
        cancelAlarmByRequestCode(followUpCode)
    }

    fun cancelAlarmByRequestCode(requestCode: Int) {
        val intent = Intent(context, SmartPrayerNotificationReceiver::class.java)
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

    fun cancelSchedulesForDate(date: LocalDate) {
        val fardPrayers = listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )
        fardPrayers.forEach { prayer ->
            cancelAlarmsForPrayer(prayer, date.toString())
        }
        for (slot in 1..10) {
            cancelAlarmByRequestCode(getSpecialAlarmRequestCode(date, slot))
            cancelSpecialNotification(date.toString(), slot)
        }
    }

    fun onPrayerCompletedInApp(prayerName: PrayerName, dateStr: String = LocalDate.now().toString()) {
        cancelFollowUpAlarm(prayerName, dateStr)
        cancelPrayerNotification(prayerName, dateStr)
    }

    fun cancelAllSchedules() {
        // Safe fallback for full teardown
        cancelSchedulesForDate(LocalDate.now())
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

        const val ACTION_SMART_PRAYER_NOTIF = "com.example.ACTION_SMART_PRAYER_NOTIF"
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
        const val EVENT_TYPE_NAFL_ISHRAQ = "nafl_ishraq"
        const val EVENT_TYPE_NAFL_AWWABIN = "nafl_awwabin"

        const val ALARM_SLOT_ENTRY = 0
        const val ALARM_SLOT_PRE_REMINDER = 1
        const val ALARM_SLOT_FOLLOWUP = 2

        const val SPECIAL_SLOT_CONTEXTUAL_MORNING = 1
        const val SPECIAL_SLOT_CONTEXTUAL_EVENING = 2
        const val SPECIAL_SLOT_CONTEXTUAL_NIGHT = 3
        const val SPECIAL_SLOT_NAFL_DUHA = 4
        const val SPECIAL_SLOT_NAFL_TAHAJJUD = 5
        const val SPECIAL_SLOT_NAFL_ISHRAQ = 6
        const val SPECIAL_SLOT_NAFL_AWWABIN = 7

        fun parseDateOrToday(dateStr: String?): LocalDate {
            if (dateStr.isNullOrBlank()) return LocalDate.now()
            return try {
                LocalDate.parse(dateStr)
            } catch (_: Exception) {
                LocalDate.now()
            }
        }

        /**
         * Deterministic, 1-to-1 mapping for (Date, PrayerName) -> Notification ID.
         * Guarantees that the same prayer on the same date always gets the exact same notification identity,
         * while different prayers and different dates never collide.
         */
        fun getNotificationId(prayerName: PrayerName, dateStr: String?): Int {
            val localDate = parseDateOrToday(dateStr)
            return getNotificationId(prayerName, localDate)
        }

        fun getNotificationId(prayerName: PrayerName, date: LocalDate): Int {
            val epochDay = date.toEpochDay().toInt()
            return (epochDay * 10) + prayerName.ordinal
        }

        /**
         * Deterministic Alarm Request Code for (Date, PrayerName, AlarmSlot).
         */
        fun getAlarmRequestCode(prayerName: PrayerName, dateStr: String?, slot: Int): Int {
            val localDate = parseDateOrToday(dateStr)
            return getAlarmRequestCode(prayerName, localDate, slot)
        }

        fun getAlarmRequestCode(prayerName: PrayerName, date: LocalDate, slot: Int): Int {
            val epochDay = date.toEpochDay().toInt()
            return ((epochDay * 10 + prayerName.ordinal) * 10) + slot
        }

        fun getSpecialNotificationId(date: LocalDate, slot: Int): Int {
            val epochDay = date.toEpochDay().toInt()
            return (epochDay * 100) + 60 + slot
        }

        fun getSpecialNotificationId(dateStr: String?, slot: Int): Int {
            val localDate = parseDateOrToday(dateStr)
            return getSpecialNotificationId(localDate, slot)
        }

        fun getSpecialAlarmRequestCode(date: LocalDate, slot: Int): Int {
            val epochDay = date.toEpochDay().toInt()
            return (epochDay * 1000) + 600 + slot
        }

        fun getSpecialAlarmRequestCode(dateStr: String?, slot: Int): Int {
            val localDate = parseDateOrToday(dateStr)
            return getSpecialAlarmRequestCode(localDate, slot)
        }

        fun createNotificationChannel(context: Context) {
            createNotificationChannels(context)
        }

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val smartPrayerChannel = NotificationChannel(
                    CHANNEL_SMART_PRAYER,
                    "Smart Prayer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Contextual reminders for day-part transitions"
                    enableVibration(false)
                    setSound(null, null)
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
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Gentle reminders for voluntary prayers and spiritual moments"
                    enableVibration(false)
                    setSound(null, null)
                }

                notificationManager.createNotificationChannels(
                    listOf(smartPrayerChannel, prayerTimeChannel, adhanChannel, quietReminderChannel)
                )
            }
        }
    }
}
