package com.example.data.reminder

import android.util.Log
import com.example.data.model.PrayerName
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Diagnostic logger for FiveLight prayer notification scheduling, event delivery, and validation.
 */
object PrayerNotificationLogger {
    private const val TAG = "PrayerNotification"
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        .withZone(ZoneId.systemDefault())

    fun formatTime(epochMillis: Long): String {
        return if (epochMillis <= 0L) "N/A"
        else try {
            formatter.format(Instant.ofEpochMilli(epochMillis))
        } catch (_: Exception) {
            "$epochMillis ms"
        }
    }

    fun logSchedule(
        prayerName: PrayerName,
        dateStr: String,
        triggerMillis: Long,
        requestCode: Int,
        eventType: String,
        alertMode: String
    ) {
        val formattedTrigger = formatTime(triggerMillis)
        val nowMillis = System.currentTimeMillis()
        val delaySec = (triggerMillis - nowMillis) / 1000
        Log.d(
            TAG,
            "[SCHEDULED] Prayer: ${prayerName.name}, Date: $dateStr, Event: $eventType, " +
                    "TriggerTime: $formattedTrigger, ReqCode: $requestCode, " +
                    "Delay: ${delaySec}s, AlertMode: $alertMode"
        )
    }

    fun logDelivery(
        eventType: String,
        prayerNameStr: String,
        dateStr: String,
        scheduledMillis: Long,
        deliveryMillis: Long
    ) {
        val formattedScheduled = formatTime(scheduledMillis)
        val formattedDelivery = formatTime(deliveryMillis)
        val driftMs = deliveryMillis - scheduledMillis
        Log.i(
            TAG,
            "[DELIVERED] Event: $eventType, Prayer: $prayerNameStr, Date: $dateStr, " +
                    "Scheduled: $formattedScheduled, Delivered: $formattedDelivery, " +
                    "Drift: ${driftMs / 1000}s (${driftMs}ms)"
        )
    }

    fun logValidationSuccess(
        eventType: String,
        prayerNameStr: String,
        dateStr: String,
        details: String
    ) {
        Log.i(
            TAG,
            "[VALIDATION PASSED] Event: $eventType, Prayer: $prayerNameStr, Date: $dateStr. Details: $details"
        )
    }

    fun logValidationFailure(
        eventType: String,
        prayerNameStr: String,
        dateStr: String,
        reason: String
    ) {
        Log.w(
            TAG,
            "[VALIDATION REJECTED] Event: $eventType, Prayer: $prayerNameStr, Date: $dateStr. Reason: $reason"
        )
    }

    fun logStaleAlarmCancellation(requestCode: Int, reason: String) {
        Log.d(TAG, "[CANCELLED STALE ALARM] ReqCode: $requestCode. Reason: $reason")
    }
}
