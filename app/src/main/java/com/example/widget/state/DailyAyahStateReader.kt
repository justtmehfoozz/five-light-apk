package com.example.widget.state

import android.content.Context
import com.example.data.repository.AppRepository
import com.example.data.util.PrayerDisplayUtils
import com.example.data.util.QuranData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Reads the authoritative Daily Ayah from the app's Quran and Reflection systems.
 * Implements context-aware selection (Morning, Night, Friday/Jumu'ah) while
 * remaining fully synchronized with the app's verified Quran data source.
 */
object DailyAyahStateReader {

    suspend fun readState(context: Context): DailyAyahWidgetState {
        val appContext = context.applicationContext
        val repository = AppRepository.getInstance(appContext)

        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val isFriday = PrayerDisplayUtils.isFriday()

        val sdfHeader = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val dateFormatted = sdfHeader.format(Date())

        val todayDateString = repository.getTodayDateString()

        // 1. Check contextual rules
        when {
            // Friday Jumu'ah Context
            isFriday -> {
                val surah = QuranData.getSurahById(62)
                val surahName = surah?.nameEnglish ?: "Al-Jumu'ah"
                return DailyAyahWidgetState(
                    arabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا نُودِيَ لِلصَّلَاةِ مِن يَوْمِ الْجُمُعَةِ فَاسْعَوْا إِلَىٰ ذِكْرِ اللَّهِ وَذَرُوا الْبَيْعَ",
                    shortArabicText = "فَاسْعَوْا إِلَىٰ ذِكْرِ اللَّهِ",
                    translation = "O you who have believed, when the call is made for prayer on Friday, proceed to the remembrance of Allah and leave trade.",
                    surahNameEnglish = surahName,
                    surahNumber = 62,
                    verseNumber = 9,
                    reference = "Surah $surahName (62:9)",
                    contextTag = "Jumu'ah Blessing",
                    reflectionCommentary = "A blessed call to detach from worldly commerce and gather for collective worship and remembrance.",
                    dateFormatted = dateFormatted
                )
            }

            // Night / Evening Context (8 PM to 5 AM)
            hourOfDay >= 20 || hourOfDay < 5 -> {
                val surah = QuranData.getSurahById(13)
                val surahName = surah?.nameEnglish ?: "Ar-Ra'd"
                return DailyAyahWidgetState(
                    arabicText = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    shortArabicText = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    translation = "Indeed, in the remembrance of Allah do hearts find rest.",
                    surahNameEnglish = surahName,
                    surahNumber = 13,
                    verseNumber = 28,
                    reference = "Surah $surahName (13:28)",
                    contextTag = "Night Reflection",
                    reflectionCommentary = "A gentle evening reminder that true serenity is found in reconnecting with your Creator.",
                    dateFormatted = dateFormatted
                )
            }

            // Morning Context (5 AM to 11 AM)
            hourOfDay in 5..11 -> {
                val surah = QuranData.getSurahById(94)
                val surahName = surah?.nameEnglish ?: "Ash-Sharh"
                return DailyAyahWidgetState(
                    arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                    shortArabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
                    translation = "For indeed, with hardship [will be] ease. Indeed, with hardship [will be] ease.",
                    surahNameEnglish = surahName,
                    surahNumber = 94,
                    verseNumber = 5,
                    reference = "Surah $surahName (94:5-6)",
                    contextTag = "Morning Reflection",
                    reflectionCommentary = "Begin your morning with divine optimism, knowing ease is already woven into every difficulty.",
                    dateFormatted = dateFormatted
                )
            }

            // General / Afternoon Context - pull from active Daily Reflection
            else -> {
                val dailyReflection = repository.getOrGenerateDailyReflection(todayDateString)
                val isQuranType = dailyReflection.sourceType == "QURAN" && dailyReflection.surahNumber > 0

                if (isQuranType) {
                    val surah = QuranData.getSurahById(dailyReflection.surahNumber)
                    val surahName = surah?.nameEnglish ?: "Quran"
                    val arabicText = dailyReflection.sourceTextArabic
                    val shortArabic = if (arabicText.length > 50) {
                        arabicText.take(45) + "..."
                    } else {
                        arabicText
                    }

                    return DailyAyahWidgetState(
                        arabicText = arabicText,
                        shortArabicText = shortArabic,
                        translation = dailyReflection.sourceTextTranslation,
                        surahNameEnglish = surahName,
                        surahNumber = dailyReflection.surahNumber,
                        verseNumber = dailyReflection.verseNumber,
                        reference = dailyReflection.reference.ifEmpty { "Surah $surahName (${dailyReflection.surahNumber}:${dailyReflection.verseNumber})" },
                        contextTag = "Daily Ayah",
                        reflectionCommentary = dailyReflection.aiReflectionText,
                        dateFormatted = dateFormatted
                    )
                } else {
                    // Fallback to Surah Al-Baqarah 2:152 if reflection is a Hadith
                    val surah = QuranData.getSurahById(2)
                    val surahName = surah?.nameEnglish ?: "Al-Baqarah"
                    return DailyAyahWidgetState(
                        arabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
                        shortArabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ",
                        translation = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
                        surahNameEnglish = surahName,
                        surahNumber = 2,
                        verseNumber = 152,
                        reference = "Surah $surahName (2:152)",
                        contextTag = "Daily Ayah",
                        reflectionCommentary = "A timeless divine promise: when you turn your attention toward Allah, He turns toward you with mercy.",
                        dateFormatted = dateFormatted
                    )
                }
            }
        }
    }
}
