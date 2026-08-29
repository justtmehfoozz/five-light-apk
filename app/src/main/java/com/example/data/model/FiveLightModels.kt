package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class QuranLastRead(
    val surahNumber: Int,
    val surahNameEnglish: String,
    val surahNameArabic: String,
    val verseNumber: Int,
    val verseIndex: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class HomeFeaturesPreferences(
    val continueReadingEnabled: Boolean = true,
    val rightNowEnabled: Boolean = true,
    val tonightEnabled: Boolean = true,
    val nextOpportunityEnabled: Boolean = true,
    val prayerPrepEnabled: Boolean = true,
    val weeklyOverviewEnabled: Boolean = true,
    val momentsEnabled: Boolean = true,
    val quietModeEnabled: Boolean = false,
    val prayerJourneyEnabled: Boolean = true,
    val recentlyReadEnabled: Boolean = true,
    val quranLensEnabled: Boolean = true,
    val nightIsComingEnabled: Boolean = true,
    val featureOrder: List<String> = DEFAULT_FEATURE_ORDER
) {
    companion object {
        val DEFAULT_FEATURE_ORDER = listOf(
            "RIGHT_NOW",
            "NEXT_OPPORTUNITY",
            "PRAYER_PREP",
            "NAFL_PRAYERS",
            "TONIGHT",
            "CONTINUE_READING",
            "RECENTLY_READ",
            "MOMENTS",
            "WEEKLY_OVERVIEW"
        )
    }
}

@Immutable
data class PrayerPrepItem(
    val prayerName: PrayerName,
    val minutesRemaining: Int,
    val formattedTime: String,
    val steps: List<String>
)

@Immutable
data class TonightSummary(
    val ishaTimeFormatted: String,
    val fajrTimeFormatted: String,
    val lastThirdStartFormatted: String,
    val tahajjudWindowFormatted: String,
    val isNightActive: Boolean,
    val isLastThirdActive: Boolean = false,
    val isIshaActive: Boolean = false,
    val isTahajjudActive: Boolean = false,
    val isFajrActive: Boolean = false,
    val headerTitle: String = "Night is Coming",
    val subtitleText: String = "A quiet part of the night is ahead.",
    val ishaTimeMillis: Long = 0L,
    val fajrTimeMillis: Long = 0L,
    val lastThirdStartMillis: Long = 0L
)

enum class JourneyNodeType {
    FARD, NAFL, ADHKAR
}

enum class PrayerStatus {
    FUTURE,     // · (prayer time not reached)
    NEEDS_INPUT,// ○ (prayer time reached/passed, unrecorded)
    PRAYED,     // ✓ (explicitly marked prayed)
    MISSED      // ! (explicitly marked missed)
}

@Immutable
data class PrayerJourneyNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val arabicTitle: String? = null,
    val timeFormatted: String,
    val type: JourneyNodeType,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val isCurrentNow: Boolean = false,
    val isUpcoming: Boolean = false,
    val timeMillis: Long = 0L,
    val naflType: NaflType? = null,
    val prayerName: PrayerName? = null
) {
    val isFuture: Boolean get() = isUpcoming && !isCompleted && !isMissed
}

@Immutable
data class VerseOccurrence(
    val surahNumber: Int,
    val surahNameEnglish: String,
    val surahNameArabic: String,
    val verseNumber: Int,
    val textArabic: String,
    val textEnglish: String
)

@Immutable
data class QuranLensInfo(
    val arabicWordOrPhrase: String,
    val transliteration: String,
    val meaning: String,
    val occurrencesCount: Int,
    val occurrences: List<VerseOccurrence>
)

@Immutable
data class NextOpportunityItem(
    val title: String,
    val subtitle: String,
    val timeFormatted: String,
    val actionText: String,
    val actionType: RightNowActionType
)

@Immutable
data class FiveLightMoment(
    val title: String,
    val message: String,
    val tag: String,
    val actionText: String? = null,
    val actionType: RightNowActionType? = null
)

@Immutable
data class CalendarEventMoment(
    val eventTitle: String,
    val arabicTitle: String,
    val description: String,
    val isToday: Boolean
)

@Immutable
data class DayWorshipState(
    val dayOfWeekName: String, // e.g. "Mon", "Tue"
    val dateString: String,    // YYYY-MM-DD
    val isToday: Boolean,
    val fajrStatus: PrayerStatus = PrayerStatus.FUTURE,
    val dhuhrStatus: PrayerStatus = PrayerStatus.FUTURE,
    val asrStatus: PrayerStatus = PrayerStatus.FUTURE,
    val maghribStatus: PrayerStatus = PrayerStatus.FUTURE,
    val ishaStatus: PrayerStatus = PrayerStatus.FUTURE
) {
    val fajrCompleted: Boolean get() = fajrStatus == PrayerStatus.PRAYED
    val dhuhrCompleted: Boolean get() = dhuhrStatus == PrayerStatus.PRAYED
    val asrCompleted: Boolean get() = asrStatus == PrayerStatus.PRAYED
    val maghribCompleted: Boolean get() = maghribStatus == PrayerStatus.PRAYED
    val ishaCompleted: Boolean get() = ishaStatus == PrayerStatus.PRAYED

    fun getStatusForPrayer(prayerName: PrayerName): PrayerStatus {
        return when (prayerName) {
            PrayerName.FAJR -> fajrStatus
            PrayerName.DHUHR -> dhuhrStatus
            PrayerName.ASR -> asrStatus
            PrayerName.MAGHRIB -> maghribStatus
            PrayerName.ISHA -> ishaStatus
            PrayerName.SUNRISE -> PrayerStatus.FUTURE
        }
    }
}

@Immutable
data class WeeklyWorshipOverview(
    val days: List<DayWorshipState>
)

@Immutable
data class FiveLightContextState(
    val prayerPrep: PrayerPrepItem? = null,
    val tonight: TonightSummary? = null,
    val nextOpportunity: NextOpportunityItem? = null,
    val moment: FiveLightMoment? = null,
    val calendarMoment: CalendarEventMoment? = null,
    val weeklyOverview: WeeklyWorshipOverview? = null
)
