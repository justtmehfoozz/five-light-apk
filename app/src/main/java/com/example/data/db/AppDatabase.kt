package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 3,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. PrayerLogEntity: add updatedAt and isDeleted
                db.execSQL("ALTER TABLE prayer_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                // 2. BookmarkEntity: add updatedAt and isDeleted
                db.execSQL("ALTER TABLE quran_bookmarks ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE quran_bookmarks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                // 3. DhikrHistoryEntity: add syncId and assign stable unique syncIds for existing records
                db.execSQL("ALTER TABLE dhikr_history ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE dhikr_history SET syncId = 'legacy_' || id WHERE syncId = '' OR syncId IS NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fivelight_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
