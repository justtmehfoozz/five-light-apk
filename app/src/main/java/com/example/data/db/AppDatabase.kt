package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PrayerLogEntity::class,
        DhikrHistoryEntity::class,
        BookmarkEntity::class,
        DuaCategoryEntity::class,
        DuaEntity::class,
        DownloadedAudioEntity::class,
        HijriCacheEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun dhikrHistoryDao(): DhikrHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun duaDao(): DuaDao
    abstract fun downloadedAudioDao(): DownloadedAudioDao
    abstract fun hijriCacheDao(): HijriCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fivelight_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
