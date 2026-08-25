package com.example.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val fajrCompleted: Boolean = false,
    val dhuhrCompleted: Boolean = false,
    val asrCompleted: Boolean = false,
    val maghribCompleted: Boolean = false,
    val ishaCompleted: Boolean = false,
    val fajrMissed: Boolean = false,
    val dhuhrMissed: Boolean = false,
    val asrMissed: Boolean = false,
    val maghribMissed: Boolean = false,
    val ishaMissed: Boolean = false,
    val fajrNote: String? = null,
    val dhuhrNote: String? = null,
    val asrNote: String? = null,
    val maghribNote: String? = null,
    val ishaNote: String? = null,
    val fajrQadaAdded: Boolean = false,
    val dhuhrQadaAdded: Boolean = false,
    val asrQadaAdded: Boolean = false,
    val maghribQadaAdded: Boolean = false,
    val ishaQadaAdded: Boolean = false
) {
    fun isCompleted(prayerName: PrayerName): Boolean {
        return when (prayerName) {
            PrayerName.FAJR -> fajrCompleted
            PrayerName.DHUHR -> dhuhrCompleted
            PrayerName.ASR -> asrCompleted
            PrayerName.MAGHRIB -> maghribCompleted
            PrayerName.ISHA -> ishaCompleted
            PrayerName.SUNRISE -> false
        }
    }

    fun isMissed(prayerName: PrayerName): Boolean {
        return when (prayerName) {
            PrayerName.FAJR -> fajrMissed
            PrayerName.DHUHR -> dhuhrMissed
            PrayerName.ASR -> asrMissed
            PrayerName.MAGHRIB -> maghribMissed
            PrayerName.ISHA -> ishaMissed
            PrayerName.SUNRISE -> false
        }
    }

    fun getNote(prayerName: PrayerName): String? {
        return when (prayerName) {
            PrayerName.FAJR -> fajrNote
            PrayerName.DHUHR -> dhuhrNote
            PrayerName.ASR -> asrNote
            PrayerName.MAGHRIB -> maghribNote
            PrayerName.ISHA -> ishaNote
            PrayerName.SUNRISE -> null
        }
    }

    fun isQadaAdded(prayerName: PrayerName): Boolean {
        return when (prayerName) {
            PrayerName.FAJR -> fajrQadaAdded
            PrayerName.DHUHR -> dhuhrQadaAdded
            PrayerName.ASR -> asrQadaAdded
            PrayerName.MAGHRIB -> maghribQadaAdded
            PrayerName.ISHA -> ishaQadaAdded
            PrayerName.SUNRISE -> false
        }
    }

    fun withNote(prayerName: PrayerName, note: String?): PrayerLogEntity {
        val trimmed = note?.trim()?.ifEmpty { null }
        return when (prayerName) {
            PrayerName.FAJR -> copy(fajrNote = trimmed)
            PrayerName.DHUHR -> copy(dhuhrNote = trimmed)
            PrayerName.ASR -> copy(asrNote = trimmed)
            PrayerName.MAGHRIB -> copy(maghribNote = trimmed)
            PrayerName.ISHA -> copy(ishaNote = trimmed)
            PrayerName.SUNRISE -> this
        }
    }

    fun withQadaAdded(prayerName: PrayerName, added: Boolean): PrayerLogEntity {
        return when (prayerName) {
            PrayerName.FAJR -> copy(fajrQadaAdded = added)
            PrayerName.DHUHR -> copy(dhuhrQadaAdded = added)
            PrayerName.ASR -> copy(asrQadaAdded = added)
            PrayerName.MAGHRIB -> copy(maghribQadaAdded = added)
            PrayerName.ISHA -> copy(ishaQadaAdded = added)
            PrayerName.SUNRISE -> this
        }
    }

    fun getCompletedCount(): Int {
        var count = 0
        if (fajrCompleted) count++
        if (dhuhrCompleted) count++
        if (asrCompleted) count++
        if (maghribCompleted) count++
        if (ishaCompleted) count++
        return count
    }

    fun getMissedCount(): Int {
        var count = 0
        if (fajrMissed) count++
        if (dhuhrMissed) count++
        if (asrMissed) count++
        if (maghribMissed) count++
        if (ishaMissed) count++
        return count
    }

    fun hasAnyNotes(): Boolean {
        return !fajrNote.isNullOrBlank() || !dhuhrNote.isNullOrBlank() || !asrNote.isNullOrBlank() || !maghribNote.isNullOrBlank() || !ishaNote.isNullOrBlank()
    }

    fun getPrayerStatus(
        prayerName: PrayerName,
        prayerTimeMillis: Long?,
        targetDateString: String,
        todayDateString: String,
        nowMillis: Long = System.currentTimeMillis()
    ): PrayerStatus {
        if (prayerName == PrayerName.SUNRISE) return PrayerStatus.FUTURE

        if (isCompleted(prayerName)) return PrayerStatus.PRAYED
        if (isMissed(prayerName)) return PrayerStatus.MISSED

        // Unrecorded outcome
        return when {
            targetDateString < todayDateString -> PrayerStatus.NEEDS_INPUT
            targetDateString > todayDateString -> PrayerStatus.FUTURE
            else -> {
                // Today: Check if current date/time is before the prayer's scheduled time
                if (prayerTimeMillis != null && nowMillis < prayerTimeMillis) {
                    PrayerStatus.FUTURE
                } else {
                    PrayerStatus.NEEDS_INPUT
                }
            }
        }
    }

    companion object {
        fun resolvePrayerStatus(
            log: PrayerLogEntity?,
            prayerName: PrayerName,
            prayerTimeMillis: Long?,
            targetDateString: String,
            todayDateString: String,
            nowMillis: Long = System.currentTimeMillis()
        ): PrayerStatus {
            if (prayerName == PrayerName.SUNRISE) return PrayerStatus.FUTURE

            if (log != null) {
                if (log.isCompleted(prayerName)) return PrayerStatus.PRAYED
                if (log.isMissed(prayerName)) return PrayerStatus.MISSED
            }

            return when {
                targetDateString < todayDateString -> PrayerStatus.NEEDS_INPUT
                targetDateString > todayDateString -> PrayerStatus.FUTURE
                else -> {
                    if (prayerTimeMillis != null && nowMillis < prayerTimeMillis) {
                        PrayerStatus.FUTURE
                    } else {
                        PrayerStatus.NEEDS_INPUT
                    }
                }
            }
        }
    }
}

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

@Entity(tableName = "dua_categories")
data class DuaCategoryEntity(
    @PrimaryKey val id: String, // e.g. "Protection", "Anxiety & Sorrow", etc.
    val title: String,
    val description: String,
    val iconName: String = "",
    val displayOrder: Int = 0
)

@Entity(
    tableName = "duas",
    indices = [Index(value = ["categoryId"]), Index(value = ["categoryTitle"])]
)
data class DuaEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val categoryTitle: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val reference: String? = null,
    val displayOrder: Int = 0
)

data class DuaCategoryWithDuas(
    @Embedded val category: DuaCategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val duas: List<DuaEntity>
)

@Entity(tableName = "downloaded_audio", primaryKeys = ["surahNumber", "reciterId"])
data class DownloadedAudioEntity(
    val surahNumber: Int,
    val reciterId: String,
    val status: String, // "NOT_DOWNLOADED", "DOWNLOADING", "DOWNLOADED", "ERROR"
    val totalVerses: Int,
    val downloadedVerses: Int,
    val sizeBytes: Long,
    val downloadTimestamp: Long = System.currentTimeMillis()
)

/**
 * Normalized Room Entity for persisting verified Gregorian -> Hijri calendar mappings.
 * Stores only normalized attributes required to reconstruct the date accurately.
 */
@Entity(
    tableName = "hijri_cache",
    primaryKeys = ["gregorianDate", "method"]
)
data class HijriCacheEntity(
    val gregorianDate: String, // "YYYY-MM-DD"
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriMonthName: String,
    val hijriMonthArabic: String,
    val hijriYear: Int,
    val method: String,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val syncTimestampMillis: Long
)

