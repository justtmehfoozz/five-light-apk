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
    @Query("SELECT * FROM prayer_logs WHERE date = :date AND isDeleted = 0")
    fun getPrayerLogForDate(date: String): Flow<PrayerLogEntity?>

    @Query("SELECT * FROM prayer_logs WHERE date = :date AND isDeleted = 0")
    suspend fun getPrayerLogForDateDirect(date: String): PrayerLogEntity?

    @Query("SELECT * FROM prayer_logs WHERE date IN (:dates) AND isDeleted = 0")
    fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllPrayerLogs(): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE isDeleted = 0 ORDER BY date ASC")
    suspend fun getAllPrayerLogsDirectAsc(): List<PrayerLogEntity>

    @Query("SELECT * FROM prayer_logs WHERE date = :date LIMIT 1")
    suspend fun getRawPrayerLogForDateDirect(date: String): PrayerLogEntity?

    @Query("SELECT * FROM prayer_logs")
    suspend fun getAllRawPrayerLogsDirect(): List<PrayerLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayerLog(log: PrayerLogEntity)
}

@Dao
interface DhikrHistoryDao {
    @Query("SELECT * FROM dhikr_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllDhikrHistory(): Flow<List<DhikrHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikrHistory(entry: DhikrHistoryEntity)

    @Query("SELECT * FROM dhikr_history WHERE syncId = :syncId LIMIT 1")
    suspend fun getDhikrHistoryBySyncId(syncId: String): DhikrHistoryEntity?

    @Query("SELECT * FROM dhikr_history ORDER BY timestamp DESC")
    suspend fun getAllDhikrHistoryDirect(): List<DhikrHistoryEntity>

    @Query("DELETE FROM dhikr_history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM quran_bookmarks WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber AND isDeleted = 0)")
    fun isBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber LIMIT 1")
    suspend fun getBookmarkByVerse(surahNumber: Int, verseNumber: Int): BookmarkEntity?

    @Query("UPDATE quran_bookmarks SET isDeleted = 1, updatedAt = :updatedAt WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber")
    suspend fun markBookmarkDeleted(surahNumber: Int, verseNumber: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber")
    suspend fun removeBookmark(surahNumber: Int, verseNumber: Int)

    @Query("SELECT * FROM quran_bookmarks")
    suspend fun getAllRawBookmarksDirect(): List<BookmarkEntity>
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


