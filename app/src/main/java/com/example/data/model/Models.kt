package com.example.data.model

import androidx.compose.runtime.Immutable

enum class PrayerName(val id: String, val displayName: String, val arabicName: String) {
    FAJR("fajr", "Fajr", "الفجر"),
    SUNRISE("sunrise", "Sunrise", "الشروق"),
    DHUHR("dhuhr", "Dhuhr", "الظهر"),
    ASR("asr", "Asr", "العصر"),
    MAGHRIB("maghrib", "Maghrib", "المغرب"),
    ISHA("isha", "Isha", "العشاء")
}

data class PrayerItem(
    val name: PrayerName,
    val timeFormatted: String, // e.g. "05:15 AM"
    val timeMillis: Long,
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val isCompleted: Boolean = false
)

enum class CalcMethod(val displayName: String, val fajrAngle: Double, val ishaAngle: Double) {
    MWL("Muslim World League", 18.0, 17.0),
    ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
    UMM_AL_QURA("Umm al-Qura University, Makkah", 18.5, 90.0), // 90 min after Maghrib
    EGYPTIAN("Egyptian General Authority", 19.5, 17.5),
    KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0),
    SINGAPORE("Islamic Religious Council of Singapore", 20.0, 18.0)
}

enum class Madhab(val displayName: String) {
    STANDARD("Shafi'i, Maliki, Hanbali"),
    HANAFI("Hanafi")
}

enum class AppearanceMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class TimeFormat(val displayName: String, val is24Hour: Boolean) {
    TWELVE_HOUR("12-hour", false),
    TWENTY_FOUR_HOUR("24-hour", true);

    companion object {
        fun fromIs24Hour(is24Hour: Boolean): TimeFormat {
            return if (is24Hour) TWENTY_FOUR_HOUR else TWELVE_HOUR
        }

        fun fromName(name: String?): TimeFormat {
            return entries.find { it.name == name } ?: TWELVE_HOUR
        }
    }
}

enum class TasbeehSound(val id: String, val displayName: String, val description: String, val resId: Int?) {
    SOFT_TICK("soft_tick", "Soft Tick", "Gentle and subtle", com.example.R.raw.soft_tick),
    GENTLE_TAP("gentle_tap", "Gentle Tap", "Warm tactile tap", com.example.R.raw.gentle_tap),
    WOODEN_TAP("wooden_tap", "Wooden Tap", "Soft organic sound", com.example.R.raw.wooden_tap),
    SOFT_CLICK("soft_click", "Soft Click", "Clean digital feedback", com.example.R.raw.soft_click),
    DIGITAL("digital", "Digital", "Modern electronic feedback", com.example.R.raw.digital),
    OFF("off", "Off", "No sound", null);

    companion object {
        fun fromId(id: String?): TasbeehSound {
            return entries.find { it.id == id } ?: SOFT_TICK
        }
    }
}

data class CityLocation(
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Double = Math.round(longitude / 15.0).toDouble(),
    val defaultCalcMethod: CalcMethod = CalcMethod.MWL
) {
    val fullDisplayName: String get() = if (countryName.startsWith("Asia/")) "$cityName ($countryName)" else "$cityName, $countryName"
}

data class Surah(
    val number: Int,
    val nameEnglish: String,
    val nameArabic: String,
    val englishTranslation: String,
    val versesCount: Int,
    val revelationPlace: String // "Meccan" or "Medinan"
)

@Immutable
data class Verse(
    val surahNumber: Int,
    val verseNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val audioUrl: String = "",
    val verseKey: String = "$surahNumber:$verseNumber"
) {
    val identity: String get() = "$surahNumber:$verseNumber"
    val isBismillahHeader: Boolean get() = verseNumber == 0
}

data class DhikrPreset(
    val id: String,
    val nameEnglish: String,
    val nameArabic: String,
    val translation: String,
    val defaultTarget: Int,
    val isCustom: Boolean = false
)

data class IslamicEvent(
    val title: String,
    val arabicTitle: String,
    val hijriDay: Int,
    val hijriMonthNumber: Int = 0,
    val hijriMonthName: String,
    val description: String
)

enum class HijriDateMethod(
    val displayName: String,
    val description: String
) {
    REGIONAL_INDIA("Regional Moon-Sighting (India / South Asia)", "Local moon-sighting convention for India & South Asia"),
    SAUDI_UMM_AL_QURA("Saudi Arabia (Umm al-Qura)", "Calculated astronomical calendar used in Saudi Arabia"),
    GLOBAL_ASTRONOMICAL("Global Astronomical Calendar", "Global calculated Hijri calendar"),
    CUSTOM_OFFSET("Custom Days Adjustment", "Manual day offset adjustment")
}

data class HijriDate(
    val day: Int,
    val monthName: String,
    val monthArabic: String,
    val monthNumber: Int,
    val year: Int,
    val gregorianDateString: String = "",
    val isAfterMaghrib: Boolean = false,
    val maghribTimeMillis: Long? = null,
    val method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA,
    val isSyncedWithInternet: Boolean = false
)

data class IslamicDateState(
    val gregorianDate: java.time.LocalDate = java.time.LocalDate.now(),
    val gregorianDateFormatted: String = "",
    val hijriDate: HijriDate = HijriDate(1, "Muharram", "المحرّم", 1, 1448),
    val hijriDateFormatted: String = "",
    val timeZone: java.util.TimeZone = java.util.TimeZone.getDefault(),
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777,
    val cityName: String = "Mumbai",
    val sunsetTimeMillis: Long? = null,
    val sunsetTimeFormatted: String = "",
    val isAfterMaghrib: Boolean = false,
    val method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA,
    val isSyncedWithInternet: Boolean = false,
    val isCachedOffline: Boolean = false,
    val lastSyncTimestampMillis: Long? = null,
    val sourceDescription: String = "",
    val moonPhase: MoonPhase = MoonPhase()
)

data class NaflPreferences(
    val tahajjudEnabled: Boolean = false,
    val ishraqEnabled: Boolean = false,
    val duhaEnabled: Boolean = false,
    val awwabinEnabled: Boolean = false,
    val naflOrder: List<String> = DEFAULT_NAFL_ORDER
) {
    val isAnyEnabled: Boolean get() = tahajjudEnabled || ishraqEnabled || duhaEnabled || awwabinEnabled
    val enabledCount: Int get() = listOf(tahajjudEnabled, ishraqEnabled, duhaEnabled, awwabinEnabled).count { it }

    companion object {
        val DEFAULT_NAFL_ORDER = listOf("TAHAJJUD", "ISHRAQ", "DUHA", "AWWABIN")
    }
}

enum class NaflType(
    val id: String,
    val displayName: String,
    val arabicName: String,
    val category: String,
    val defaultSubtitle: String,
    val evidenceTitle: String,
    val description: String,
    val referenceText: String,
    val gradingText: String,
    val scholarlyNote: String?
) {
    TAHAJJUD(
        id = "tahajjud",
        displayName = "Tahajjud",
        arabicName = "صلاة التهجد",
        category = "Night prayer",
        defaultSubtitle = "Last third of the night",
        evidenceTitle = "Tahajjud (Qiyam al-Layl) Evidence",
        description = "Voluntary night prayer performed in the final third of the night before Fajr.",
        referenceText = "\"Our Lord descends every night to the lowest heaven when one-third of the night remains, saying: 'Who calls upon Me that I may answer him?'\"",
        gradingText = "Authentic (Sahih al-Bukhari 1145, Sahih Muslim 758)",
        scholarlyNote = "Best offered after waking from sleep during the last third of the night before Fajr."
    ),
    ISHRAQ(
        id = "ishraq",
        displayName = "Ishraq",
        arabicName = "صلاة الإشراق",
        category = "After sunrise",
        defaultSubtitle = "Post-sunrise solar window",
        evidenceTitle = "Ishraq Prayer Evidence",
        description = "Voluntary two-rak'ah prayer performed shortly after sunrise once the prohibited solar zenith window passes (~15–20 mins after sunrise).",
        referenceText = "\"Whoever prays Fajr in congregation, then sits remembering Allah until the sun rises, then prays two rak'ahs, will have a reward like that of Hajj and 'Umrah...\"",
        gradingText = "Hasan (Jami' at-Tirmidhi 586)",
        scholarlyNote = "Performed approximately 15–20 minutes after sunrise when the sun rises a spear's height above the horizon."
    ),
    DUHA(
        id = "duha",
        displayName = "Duha / Chasht",
        arabicName = "صلاة الضحى",
        category = "Morning prayer",
        defaultSubtitle = "Forenoon voluntary prayer",
        evidenceTitle = "Salat al-Duha Evidence",
        description = "Voluntary morning prayer performed between Ishraq and shortly before solar noon (Dhuhr).",
        referenceText = "\"In the morning, charity is due from every joint of your body... and two rak'ahs offered in Duha fulfills all of that.\"",
        gradingText = "Authentic (Sahih Muslim 720)",
        scholarlyNote = "The most virtuous time is when the morning sun grows intensely warm (mid-morning / Chasht)."
    ),
    AWWABIN(
        id = "awwabin",
        displayName = "Awwabin",
        arabicName = "صلاة الأوابين",
        category = "Post-Maghrib voluntary",
        defaultSubtitle = "Evening voluntary prayer window",
        evidenceTitle = "Awwabin Terminology & Evidence Note",
        description = "Voluntary prayer units offered between Maghrib and Isha.",
        referenceText = "\"The prayer of the Awwabin (the oft-repentant) is when the young camels feel the heat of the sun.\"",
        gradingText = "Authentic Hadith (Sahih Muslim 748)",
        scholarlyNote = "While authentic Hadith explicitly refers to Salat al-Duha as 'the Prayer of the Awwabin' when the sun grows hot, classical Islamic jurisprudence (Fiqh) works also use the term informally for voluntary Nafl prayers offered between Maghrib and Isha."
    )
}

data class NaflPrayerItem(
    val type: NaflType,
    val timeFormatted: String,
    val isCurrentWindow: Boolean = false,
    val startMillis: Long = 0L,
    val endMillis: Long = 0L
)

enum class RightNowActionType {
    OPEN_ADHKAR,
    VIEW_PRAYER,
    OPEN_QURAN
}

data class RightNowItem(
    val title: String,
    val subtitle: String,
    val description: String,
    val actionText: String,
    val actionType: RightNowActionType,
    val naflType: NaflType? = null
)
