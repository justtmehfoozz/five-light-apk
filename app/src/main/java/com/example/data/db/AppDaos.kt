package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerLogDao {
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getPrayerLogForDate(date: String): Flow<PrayerLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayerLog(log: PrayerLogEntity)
}

@Dao
interface DhikrHistoryDao {
    @Query("SELECT * FROM dhikr_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllDhikrHistory(): Flow<List<DhikrHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikrHistory(entry: DhikrHistoryEntity)

    @Query("DELETE FROM dhikr_history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber)")
    fun isBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber")
    suspend fun removeBookmark(surahNumber: Int, verseNumber: Int)
}
