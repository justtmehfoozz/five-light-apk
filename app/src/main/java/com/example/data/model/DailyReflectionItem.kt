package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class DailyReflectionItem(
    val id: String,              // Unique stable identity (e.g. "QURAN_13_28" or "HADITH_BUKHARI_1")
    val sourceType: String,      // "QURAN" or "HADITH"
    val reference: String,       // e.g. "Surah Ar-Ra'd (13:28)" or "Sahih al-Bukhari 1"
    val sourceTextArabic: String,// Authentic Arabic source text
    val sourceTextTranslation: String, // Authentic translation of Quran/Hadith source
    val aiReflectionText: String, // Short, thoughtful AI-generated commentary/reflection based strictly on source
    val surahNumber: Int = 0,    // Surah number if Quran, otherwise 0
    val verseNumber: Int = 0     // Verse number if Quran, otherwise 0
)
