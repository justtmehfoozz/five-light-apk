package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val fajrCompleted: Boolean = false,
    val dhuhrCompleted: Boolean = false,
    val asrCompleted: Boolean = false,
    val maghribCompleted: Boolean = false,
    val ishaCompleted: Boolean = false
)

@Entity(tableName = "dhikr_history")
data class DhikrHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dhikrName: String,
    val arabicText: String,
    val countCompleted: Int,
    val target: Int
)

@Entity(tableName = "quran_bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val verseNumber: Int,
    val surahNameEnglish: String,
    val surahNameArabic: String,
    val verseTextArabic: String,
    val verseTextTranslation: String,
    val timestamp: Long = System.currentTimeMillis()
)
