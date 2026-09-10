package com.example.widget.state

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.PrayerName
import com.example.data.repository.AppRepository
import com.example.data.util.PrayerDisplayUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Connects directly to the existing AppRepository and AppDatabase to assemble
 * an authoritative snapshot for the Current Prayer Widget.
 *
 * Adheres strictly to the Data Consistency Rule: uses identical prayer times,
 * location configuration, Madhab/calculation methods, and prayer log states.
 */
object PrayerWidgetStateReader {

    suspend fun readState(context: Context): PrayerWidgetState {
        val appContext = context.applicationContext
        val repository = AppRepository.getInstance(appContext)
        val db = AppDatabase.getDatabase(appContext)

        val times = repository.getTodayPrayerTimes()
        val city = repository.selectedCity.value
        val isFriday = PrayerDisplayUtils.isFriday()
        val nowMillis = System.currentTimeMillis()

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateString = repository.getTodayDateString()

        val sdfHeader = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val todayDateFormatted = sdfHeader.format(Date())

        val todayLog = try {
            db.prayerLogDao().getPrayerLogForDateDirect(todayDateString)
        } catch (_: Exception) {
            null
        }

        // Fard prayers in chronological sequence (excluding Sunrise)
        val fardOrder = listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )

        val fardTimes = times.filter { it.name != PrayerName.SUNRISE }

        // Find candidate next prayer identical to AppViewModel logic
        val nextPrayer = fardTimes.find { it.isNext }
            ?: fardTimes.find { it.timeMillis > nowMillis }
            ?: fardTimes.firstOrNull()?.let {
                it.copy(timeMillis = it.timeMillis + (24 * 3600 * 1000L))
            }

        val targetMillis = nextPrayer?.timeMillis ?: (nowMillis + 60 * 60 * 1000L)
        val diffMillis = (targetMillis - nowMillis).coerceAtLeast(0)
        val totalMins = (diffMillis / (1000 * 60)).toInt()
        val hours = totalMins / 60
        val mins = totalMins % 60

        val remainingFormatted = when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m remaining"
            hours > 0 -> "${hours}h remaining"
            totalMins > 0 -> "$mins min remaining"
            else -> "Due now"
        }

        val nextPrayerName = if (nextPrayer != null) {
            PrayerDisplayUtils.getPrayerDisplayName(nextPrayer.name, isFriday)
        } else "Next Prayer"

        val nextPrayerArabicName = if (nextPrayer != null) {
            PrayerDisplayUtils.getPrayerArabicName(nextPrayer.name, isFriday)
        } else ""

        val nextPrayerTimeFormatted = nextPrayer?.timeFormatted ?: ""

        val fardItems = fardOrder.map { prayerName ->
            val pItem = times.find { it.name == prayerName }
            val isCompleted = todayLog?.isCompleted(prayerName) == true
            val isNext = (nextPrayer?.name == prayerName)
            val isPassed = pItem?.let { it.timeMillis <= nowMillis } ?: false
            val displayName = PrayerDisplayUtils.getPrayerDisplayName(prayerName, isFriday)
            val arabicName = PrayerDisplayUtils.getPrayerArabicName(prayerName, isFriday)

            PrayerWidgetItem(
                id = prayerName.id,
                name = displayName,
                arabicName = arabicName,
                timeFormatted = pItem?.timeFormatted ?: "--:--",
                isCompleted = isCompleted,
                isNext = isNext,
                isPassed = isPassed
            )
        }

        val completedCount = fardItems.count { it.isCompleted }

        return PrayerWidgetState(
            nextPrayerName = nextPrayerName,
            nextPrayerArabicName = nextPrayerArabicName,
            nextPrayerTimeFormatted = nextPrayerTimeFormatted,
            nextPrayerRemainingFormatted = remainingFormatted,
            nextPrayerRemainingMinutes = totalMins,
            cityName = city.cityName,
            todayDateFormatted = todayDateFormatted,
            completedPrayersCount = completedCount,
            totalPrayersCount = 5,
            fardPrayers = fardItems,
            isFriday = isFriday
        )
    }
}
