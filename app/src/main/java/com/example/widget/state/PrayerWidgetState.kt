package com.example.widget.state

import androidx.compose.runtime.Immutable

@Immutable
data class PrayerWidgetItem(
    val id: String,
    val name: String,
    val arabicName: String,
    val timeFormatted: String,
    val isCompleted: Boolean,
    val isNext: Boolean,
    val isPassed: Boolean
)

@Immutable
data class PrayerWidgetState(
    val nextPrayerName: String = "",
    val nextPrayerArabicName: String = "",
    val nextPrayerTimeFormatted: String = "",
    val nextPrayerRemainingFormatted: String = "",
    val nextPrayerRemainingMinutes: Int = 0,
    val cityName: String = "",
    val todayDateFormatted: String = "",
    val completedPrayersCount: Int = 0,
    val totalPrayersCount: Int = 5,
    val fardPrayers: List<PrayerWidgetItem> = emptyList(),
    val isFriday: Boolean = false
)
