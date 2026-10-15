package com.example.data.reminder

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.CalcMethod
import com.example.data.model.Madhab
import com.example.data.model.PrayerName
import com.example.data.repository.AppRepository
import com.example.data.util.PrayerCalc
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.math.abs

/**
 * Strict validation gate for verifying prayer notification and Azaan delivery.
 * Rejects stale, premature, disabled, duplicate, or miscalculated events.
 */
object PrayerNotificationValidator {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validateFardEntry(
        context: Context,
        prayerName: PrayerName,
        prayerDateStr: String,
        triggerMillis: Long,
        windowEndMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): ValidationResult {
        val manager = SmartPrayerNotificationManager(context.applicationContext)

        // 1. Check Notification Toggles
        if (!manager.isSmartNotificationsEnabled) {
            return ValidationResult.Invalid("Master Smart Notifications switch is disabled")
        }
        if (!manager.isPrayerTimeNotificationsEnabled) {
            return ValidationResult.Invalid("Prayer Time Notifications setting is disabled")
        }
        if (!manager.isPrayerEnabled(prayerName.name)) {
            return ValidationResult.Invalid("Individual toggle for ${prayerName.name} is disabled")
        }

        // 2. Early Delivery Guard (e.g. alarm delivered hours early)
        val deliveryDriftMs = nowMillis - triggerMillis
        if (deliveryDriftMs < -5 * 60 * 1000L) { // Delivered > 5 minutes before scheduled time
            return ValidationResult.Invalid(
                "Premature alarm delivery: delivered ${abs(deliveryDriftMs) / 1000}s before scheduled start time " +
                        "(now=${PrayerNotificationLogger.formatTime(nowMillis)}, scheduled=${PrayerNotificationLogger.formatTime(triggerMillis)})"
            )
        }

        // 3. Stale Delivery Guard (e.g. delivered > 30 minutes late or past window end)
        if (windowEndMillis > 0L && nowMillis > windowEndMillis) {
            return ValidationResult.Invalid("Event expired: current time is past window end time ${PrayerNotificationLogger.formatTime(windowEndMillis)}")
        }
        if (deliveryDriftMs > 30 * 60 * 1000L) {
            return ValidationResult.Invalid("Stale alarm delivery: delivered ${deliveryDriftMs / 1000}s after scheduled start time")
        }

        // 4. Authoritative Schedule Alignment
        val authoritativeMillis = calculateAuthoritativeStartMillis(context, prayerName, prayerDateStr)
        if (authoritativeMillis != null) {
            val scheduleDrift = abs(authoritativeMillis - triggerMillis)
            if (scheduleDrift > 10 * 60 * 1000L) {
                return ValidationResult.Invalid(
                    "Obsolete schedule: intent timestamp (${PrayerNotificationLogger.formatTime(triggerMillis)}) " +
                            "drifts by ${scheduleDrift / 1000}s from authoritative schedule (${PrayerNotificationLogger.formatTime(authoritativeMillis)})"
                )
            }
        }

        // 5. Deduplication Check
        val prefs = context.getSharedPreferences("smart_prayer_validation_prefs", Context.MODE_PRIVATE)
        val dedupeKey = "entry_${prayerName.name}_$prayerDateStr"
        if (prefs.getBoolean(dedupeKey, false)) {
            return ValidationResult.Invalid("Duplicate event: Fard prayer entry for ${prayerName.name} on $prayerDateStr was already triggered")
        }

        return ValidationResult.Valid
    }

    fun validateFardFollowUp(
        context: Context,
        prayerName: PrayerName,
        prayerDateStr: String,
        triggerMillis: Long,
        windowEndMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): ValidationResult {
        val manager = SmartPrayerNotificationManager(context.applicationContext)

        if (!manager.isSmartNotificationsEnabled || !manager.isPrayerTimeNotificationsEnabled || !manager.isPrayerEnabled(prayerName.name)) {
            return ValidationResult.Invalid("Notification settings disabled for ${prayerName.name}")
        }

        // 1. Delivery Timing Guard
        val deliveryDriftMs = nowMillis - triggerMillis
        if (deliveryDriftMs < -5 * 60 * 1000L) {
            return ValidationResult.Invalid("Premature follow-up alarm delivered before 45-minute mark")
        }
        if (windowEndMillis > 0L && nowMillis > windowEndMillis) {
            return ValidationResult.Invalid("Follow-up expired past prayer window end")
        }
        if (deliveryDriftMs > 45 * 60 * 1000L) {
            return ValidationResult.Invalid("Stale follow-up alarm delivered > 45 minutes late")
        }

        // 2. Check if prayer already logged as prayed in Personal Log
        try {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val log = kotlinx.coroutines.runBlocking {
                db.prayerLogDao().getPrayerLogForDateDirect(prayerDateStr)
            }
            if (log != null && log.isCompleted(prayerName)) {
                return ValidationResult.Invalid("Prayer ${prayerName.name} on $prayerDateStr is already marked as prayed in Personal Log")
            }
        } catch (_: Exception) {}

        // 3. Deduplication Check
        val prefs = context.getSharedPreferences("smart_prayer_validation_prefs", Context.MODE_PRIVATE)
        val dedupeKey = "followup_${prayerName.name}_$prayerDateStr"
        if (prefs.getBoolean(dedupeKey, false)) {
            return ValidationResult.Invalid("Duplicate event: Follow-up for ${prayerName.name} on $prayerDateStr was already triggered")
        }

        return ValidationResult.Valid
    }

    fun markFardEntryTriggered(context: Context, prayerName: PrayerName, prayerDateStr: String) {
        val prefs = context.getSharedPreferences("smart_prayer_validation_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("entry_${prayerName.name}_$prayerDateStr", true).apply()
    }

    fun markFardFollowUpTriggered(context: Context, prayerName: PrayerName, prayerDateStr: String) {
        val prefs = context.getSharedPreferences("smart_prayer_validation_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("followup_${prayerName.name}_$prayerDateStr", true).apply()
    }

    internal fun calculateAuthoritativeStartMillis(
        context: Context,
        prayerName: PrayerName,
        prayerDateStr: String
    ): Long? {
        return try {
            val mainPrefs = context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)
            val savedCityName = mainPrefs.getString("selected_city_name", null)
            val repo = AppRepository(AppDatabase.getDatabase(context.applicationContext), context.applicationContext)
            val city = repo.PREDEFINED_CITIES.find { it.cityName == savedCityName } ?: repo.PREDEFINED_CITIES[0]

            val savedCalcMethod = mainPrefs.getString("calc_method", null)
            val method = CalcMethod.entries.find { it.name == savedCalcMethod } ?: city.defaultCalcMethod

            val savedMadhab = mainPrefs.getString("madhab", null)
            val madhab = Madhab.entries.find { it.name == savedMadhab } ?: Madhab.STANDARD

            val date = LocalDate.parse(prayerDateStr)
            val javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

            val prayerTimes = PrayerCalc.calculatePrayerTimes(
                latitude = city.latitude,
                longitude = city.longitude,
                date = javaDate,
                method = method,
                madhab = madhab,
                timeZoneOffsetHours = city.timezoneOffsetHours,
                updateActiveTimeZone = false
            )

            prayerTimes.find { it.name == prayerName }?.timeMillis
        } catch (_: Exception) {
            null
        }
    }
}
