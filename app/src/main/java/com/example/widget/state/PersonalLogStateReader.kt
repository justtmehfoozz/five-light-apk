package com.example.widget.state

import android.content.Context
import com.example.data.db.AppDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Connects to existing AppDatabase and SharedPreferences to assemble
 * the snapshot for the Personal Log widget without duplicating storage.
 */
object PersonalLogStateReader {

    suspend fun readState(context: Context): PersonalLogWidgetState {
        val appContext = context.applicationContext
        val db = AppDatabase.getDatabase(appContext)
        val prefs = appContext.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

        // 1. Today's Dhikr Count
        var todayDhikr = 0
        try {
            val allHistory = db.dhikrHistoryDao().getAllDhikrHistoryDirect()
            val todayHistory = allHistory.filter { it.timestamp in startOfDay..endOfDay }
            val historySum = todayHistory.sumOf { it.countCompleted }

            // Active counter values across common presets
            val presetIds = listOf(
                "subhanallah", "alhamdulillah", "allahu_akbar",
                "la_ilaha_illallah", "astaghfirullah"
            )
            var activeSum = 0
            for (id in presetIds) {
                activeSum += prefs.getInt("dhikr_count_$id", 0)
            }

            val lastTasbeehUpdate = prefs.getLong("tasbeeh_updated_at", 0L)
            todayDhikr = if (lastTasbeehUpdate in startOfDay..endOfDay) {
                max(historySum, activeSum)
            } else {
                historySum
            }
        } catch (_: Exception) {
            todayDhikr = 0
        }

        // 2. Today's Prayer Completion & Reflection Notes
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateString = sdfDate.format(Date())
        var completedPrayers = 0
        var reflectionSnippet: String? = null

        try {
            val todayLog = db.prayerLogDao().getPrayerLogForDateDirect(todayDateString)
            if (todayLog != null) {
                completedPrayers = todayLog.getCompletedCount()
                reflectionSnippet = todayLog.fajrNote
                    ?: todayLog.dhuhrNote
                    ?: todayLog.asrNote
                    ?: todayLog.maghribNote
                    ?: todayLog.ishaNote
            }
        } catch (_: Exception) {
            completedPrayers = 0
        }

        // 3. Quran Status
        val lastReadTimestamp = prefs.getLong("last_read_timestamp", 0L)
        val hasQuranToday = lastReadTimestamp in startOfDay..endOfDay
        val surahEn = prefs.getString("last_read_surah_en", "").orEmpty()
        val verseNum = prefs.getInt("last_read_verse", 1)

        val quranStatusText = when {
            surahEn.isNotBlank() -> "Surah $surahEn : $verseNum"
            else -> ""
        }

        // 4. Formatted Date
        val sdfDisplay = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val dateFormatted = sdfDisplay.format(Date())

        val hasAnyActivity = todayDhikr > 0 || hasQuranToday || completedPrayers > 0

        return PersonalLogWidgetState(
            todayDhikrCount = todayDhikr,
            quranStatusText = quranStatusText,
            hasQuranActivityToday = hasQuranToday,
            completedPrayersCount = completedPrayers,
            totalPrayersCount = 5,
            hasAnyActivity = hasAnyActivity,
            dateFormatted = dateFormatted,
            reflectionSnippet = reflectionSnippet
        )
    }
}
