package com.example.data.reminder

import com.example.data.model.PrayerName

data class PrayerReminderConfig(
    val prayerName: PrayerName,
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0 // 0 = At prayer time, 5 = 5 min before, etc.
) {
    val offsetLabel: String
        get() = when (offsetMinutes) {
            0 -> "At prayer time"
            5 -> "5 minutes before"
            10 -> "10 minutes before"
            15 -> "15 minutes before"
            30 -> "30 minutes before"
            else -> "$offsetMinutes minutes before"
        }
}

data class ActiveAzaanState(
    val prayerName: PrayerName,
    val isSnoozed: Boolean = false,
    val triggerTimeMillis: Long = System.currentTimeMillis()
)
