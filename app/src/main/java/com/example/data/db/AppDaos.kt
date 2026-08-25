package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerLogDao {
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getPrayerLogForDate(date: String): Flow<PrayerLogEntity?>

    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    suspend fun getPrayerLogForDateDirect(date: String): PrayerLogEntity?

    @Query("SELECT * FROM prayer_logs WHERE date IN (:dates)")
    fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>>
    @Query("SELECT * FROM prayer_logs ORDER BY date DESC")
    fun getAllPrayerLogs(): Flow<List<PrayerLogEntity>>

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

@Dao
interface DuaDao {
    @Transaction
    @Query("SELECT * FROM dua_categories ORDER BY displayOrder ASC")
    fun getCategoriesWithDuas(): Flow<List<DuaCategoryWithDuas>>

    @Transaction
    @Query("SELECT * FROM dua_categories WHERE id = :categoryIdOrTitle OR title = :categoryIdOrTitle LIMIT 1")
    fun getCategoryWithDuas(categoryIdOrTitle: String): Flow<DuaCategoryWithDuas?>

    @Query("SELECT * FROM duas WHERE categoryId = :categoryIdOrTitle OR categoryTitle = :categoryIdOrTitle ORDER BY displayOrder ASC")
    fun getDuasByCategory(categoryIdOrTitle: String): Flow<List<DuaEntity>>

    @Query("SELECT * FROM duas WHERE categoryId = :categoryIdOrTitle OR categoryTitle = :categoryIdOrTitle ORDER BY displayOrder ASC")
    suspend fun getDuasByCategoryDirect(categoryIdOrTitle: String): List<DuaEntity>

    @Query("SELECT * FROM duas ORDER BY displayOrder ASC")
    fun getAllDuas(): Flow<List<DuaEntity>>

    @Query("SELECT * FROM duas ORDER BY displayOrder ASC")
    suspend fun getAllDuasDirect(): List<DuaEntity>

    @Query("SELECT * FROM dua_categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<DuaCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<DuaCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuas(duas: List<DuaEntity>)

    @Query("SELECT COUNT(*) FROM dua_categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT COUNT(*) FROM duas")
    suspend fun getDuaCount(): Int
}

@Dao
interface DownloadedAudioDao {
    @Query("SELECT * FROM downloaded_audio WHERE reciterId = :reciterId")
    fun getAllDownloadedAudio(reciterId: String = "ar.alafasy"): Flow<List<DownloadedAudioEntity>>

    @Query("SELECT * FROM downloaded_audio WHERE surahNumber = :surahNumber AND reciterId = :reciterId LIMIT 1")
    suspend fun getDownloadedAudioDirect(surahNumber: Int, reciterId: String = "ar.alafasy"): DownloadedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: DownloadedAudioEntity)

    @Query("DELETE FROM downloaded_audio WHERE surahNumber = :surahNumber AND reciterId = :reciterId")
    suspend fun deleteAudio(surahNumber: Int, reciterId: String = "ar.alafasy")

    @Query("DELETE FROM downloaded_audio WHERE reciterId = :reciterId")
    suspend fun deleteAllAudio(reciterId: String = "ar.alafasy")
}

@Dao
interface HijriCacheDao {
    @Query("SELECT * FROM hijri_cache WHERE gregorianDate = :gregorianDate AND method = :method LIMIT 1")
    suspend fun getCacheForDateAndMethod(gregorianDate: String, method: String): HijriCacheEntity?

    @Query("SELECT * FROM hijri_cache WHERE method = :method ORDER BY syncTimestampMillis DESC LIMIT 1")
    suspend fun getLatestCacheForMethod(method: String): HijriCacheEntity?

    @Query("SELECT * FROM hijri_cache ORDER BY syncTimestampMillis DESC LIMIT 1")
    suspend fun getLatestCache(): HijriCacheEntity?

    @Query("SELECT * FROM hijri_cache WHERE method = :method ORDER BY syncTimestampMillis DESC LIMIT 1")
    fun observeLatestCacheForMethod(method: String): Flow<HijriCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: HijriCacheEntity)

    @Query("DELETE FROM hijri_cache WHERE syncTimestampMillis < :beforeTimestamp")
    suspend fun deleteOldCache(beforeTimestamp: Long)
}


