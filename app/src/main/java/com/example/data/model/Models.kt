package com.example.data.model

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

data class Verse(
    val surahNumber: Int,
    val verseNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val audioUrl: String = ""
)

data class DhikrPreset(
    val id: String,
    val nameEnglish: String,
    val nameArabic: String,
    val translation: String,
    val defaultTarget: Int
)

data class IslamicEvent(
    val title: String,
    val arabicTitle: String,
    val hijriDay: Int,
    val hijriMonthName: String,
    val description: String
)

data class HijriDate(
    val day: Int,
    val monthName: String,
    val monthArabic: String,
    val monthNumber: Int,
    val year: Int
)
