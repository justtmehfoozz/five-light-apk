package com.example.widget.state

import androidx.compose.runtime.Immutable

@Immutable
data class DailyAyahWidgetState(
    val arabicText: String = "",
    val shortArabicText: String = "",
    val translation: String = "",
    val surahNameEnglish: String = "",
    val surahNumber: Int = 13,
    val verseNumber: Int = 28,
    val reference: String = "Surah Ar-Ra'd (13:28)",
    val contextTag: String = "Daily Ayah",
    val reflectionCommentary: String = "",
    val dateFormatted: String = ""
)
