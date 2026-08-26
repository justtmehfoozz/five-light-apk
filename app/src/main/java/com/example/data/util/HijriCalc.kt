package com.example.data.util

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.db.HijriCacheEntity
import com.example.data.model.CalcMethod
import com.example.data.model.HijriDate
import com.example.data.model.HijriDateMethod
import com.example.data.model.IslamicEvent
import com.example.data.model.PrayerName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * CENTRALIZED LOCATION-AWARE, TIME-AWARE, SUNSET-DRIVEN HIJRI DATE ENGINE
 *
 * Core Boundaries & Rules:
 * 1. Gregorian Date changes strictly at local midnight (00:00:00 local time).
 * 2. Hijri Date changes at local sunset / Maghrib.
 * 3. Before Maghrib on civil date D -> Hijri Date corresponds to base mapping for D.
 * 4. After Maghrib on civil date D -> Hijri Date corresponds to base mapping for D + 1 day.
 * 5. At midnight (00:00:00), civil date becomes D + 1. Before Maghrib on D + 1,
 *    the Hijri Date matches the day that started at previous sunset (does not increment at midnight).
 * 6. Regional Moon-Sighting (India / South Asia) is supported as the primary convention:
 *    - 24 August 2026 daytime -> 10 Rabi' al-Awwal 1448 AH
 *    - 24 August 2026 after Maghrib -> 11 Rabi' al-Awwal 1448 AH
 * 7. Internet synchronized with offline caching fallback.
 */
object HijriCalc {

    private const val TAG = "HijriCalc"
    private const val PREFS_CACHE = "fivelight_hijri_cache"
    private const val SYNC_THROTTLE_SUCCESS_MILLIS = 24 * 60 * 60 * 1000L // 24 hours
    private const val SYNC_THROTTLE_FAILURE_MILLIS = 10 * 60 * 1000L // 10 minutes

    /**
     * Cache record persisting all essential information for reconstructing the Hijri date.
     */
    data class HijriCacheRecord(
        val gregorianDate: String, // "YYYY-MM-DD"
        val day: Int,
        val monthNumber: Int,
        val monthName: String,
        val monthArabic: String,
        val year: Int,
        val method: String,
        val syncTimestampMillis: Long,
        val cityName: String,
        val latitude: Double,
        val longitude: Double
    ) {
        fun toJsonString(): String {
            val safeMonthEn = monthName.replace("\"", "")
            val safeMonthAr = monthArabic.replace("\"", "")
            val safeCity = cityName.replace("\"", "")
            return "{\"gregorianDate\":\"$gregorianDate\",\"day\":$day,\"monthNumber\":$monthNumber,\"monthName\":\"$safeMonthEn\",\"monthArabic\":\"$safeMonthAr\",\"year\":$year,\"method\":\"$method\",\"syncTimestampMillis\":$syncTimestampMillis,\"cityName\":\"$safeCity\",\"latitude\":$latitude,\"longitude\":$longitude}"
        }

        companion object {
            private val STRING_FIELD_REGEX = "\"([a-zA-Z0-9_]+)\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            private val NUMBER_FIELD_REGEX = "\"([a-zA-Z0-9_]+)\"\\s*:\\s*([0-9.-]+)".toRegex()

            fun fromJsonString(jsonStr: String): HijriCacheRecord? {
                return try {
                    val stringMap = mutableMapOf<String, String>()
                    STRING_FIELD_REGEX.findAll(jsonStr).forEach { match ->
                        stringMap[match.groupValues[1]] = match.groupValues[2]
                    }
                    val numberMap = mutableMapOf<String, String>()
                    NUMBER_FIELD_REGEX.findAll(jsonStr).forEach { match ->
                        numberMap[match.groupValues[1]] = match.groupValues[2]
                    }

                    val day = numberMap["day"]?.toIntOrNull() ?: return null
                    val month = numberMap["monthNumber"]?.toIntOrNull() ?: return null
                    val year = numberMap["year"]?.toIntOrNull() ?: return null
                    val gDate = stringMap["gregorianDate"] ?: return null

                    if (day !in 1..30 || month !in 1..12 || year !in 1400..1550) return null

                    HijriCacheRecord(
                        gregorianDate = gDate,
                        day = day,
                        monthNumber = month,
                        monthName = stringMap["monthName"] ?: (if (month in 1..12) MONTH_NAMES_EN[month - 1] else ""),
                        monthArabic = stringMap["monthArabic"] ?: (if (month in 1..12) MONTH_NAMES_AR[month - 1] else ""),
                        year = year,
                        method = stringMap["method"] ?: "",
                        syncTimestampMillis = numberMap["syncTimestampMillis"]?.toLongOrNull() ?: 0L,
                        cityName = stringMap["cityName"] ?: "",
                        latitude = numberMap["latitude"]?.toDoubleOrNull() ?: 0.0,
                        longitude = numberMap["longitude"]?.toDoubleOrNull() ?: 0.0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    // In-memory cache for fast, thread-safe access: Key = "YYYY-MM-DD_METHOD" -> HijriCacheRecord
    private val memoryCache = ConcurrentHashMap<String, HijriCacheRecord>()
    private val lastSyncSuccessMap = ConcurrentHashMap<String, Long>()
    private val lastSyncFailureMap = ConcurrentHashMap<String, Long>()

    fun getCacheRecord(context: Context?, date: LocalDate, method: HijriDateMethod): HijriCacheRecord? {
        val key = "${date}_${method.name}"
        memoryCache[key]?.let { return it }

        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
                val jsonStr = prefs.getString("entry_$key", null)
                if (!jsonStr.isNullOrEmpty()) {
                    val record = HijriCacheRecord.fromJsonString(jsonStr)
                    if (record != null) {
                        memoryCache[key] = record
                        return record
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading cache from preferences: ${e.message}")
            }
        }
        return null
    }

    fun getLatestCacheRecord(context: Context?, method: HijriDateMethod): HijriCacheRecord? {
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
                val jsonStr = prefs.getString("latest_entry_${method.name}", null)
                if (!jsonStr.isNullOrEmpty()) {
                    return HijriCacheRecord.fromJsonString(jsonStr)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading latest cache: ${e.message}")
            }
        }
        return memoryCache.values.filter { it.method == method.name }
            .maxByOrNull { it.syncTimestampMillis }
    }

    fun saveCacheRecord(context: Context?, record: HijriCacheRecord) {
        val key = "${record.gregorianDate}_${record.method}"
        memoryCache[key] = record
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("entry_$key", record.toJsonString())
                    .putString("latest_entry_${record.method}", record.toJsonString())
                    .putLong("last_success_$key", record.syncTimestampMillis)
                    .apply()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing cache to preferences: ${e.message}")
            }

            // Asynchronously persist normalized Room Entity
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val entity = HijriCacheEntity(
                            gregorianDate = record.gregorianDate,
                            hijriDay = record.day,
                            hijriMonth = record.monthNumber,
                            hijriMonthName = record.monthName,
                            hijriMonthArabic = record.monthArabic,
                            hijriYear = record.year,
                            method = record.method,
                            cityName = record.cityName,
                            latitude = record.latitude,
                            longitude = record.longitude,
                            timezoneId = TimeZone.getDefault().id,
                            syncTimestampMillis = record.syncTimestampMillis
                        )
                        db.hijriCacheDao().insertOrUpdate(entity)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error inserting into Room hijri_cache: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error scheduling Room insert: ${e.message}")
            }
        }
        lastSyncSuccessMap[key] = record.syncTimestampMillis
    }

    /**
     * Preloads the latest verified Room database cache entries into memory on startup.
     */
    suspend fun preloadCacheFromRoom(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val latest = db.hijriCacheDao().getLatestCache()
            if (latest != null) {
                val record = HijriCacheRecord(
                    gregorianDate = latest.gregorianDate,
                    day = latest.hijriDay,
                    monthNumber = latest.hijriMonth,
                    monthName = latest.hijriMonthName,
                    monthArabic = latest.hijriMonthArabic,
                    year = latest.hijriYear,
                    method = latest.method,
                    syncTimestampMillis = latest.syncTimestampMillis,
                    cityName = latest.cityName,
                    latitude = latest.latitude,
                    longitude = latest.longitude
                )
                val key = "${record.gregorianDate}_${record.method}"
                memoryCache[key] = record
                Log.d(TAG, "Preloaded verified Hijri date mapping from Room: $key -> ${record.day} ${record.monthName} ${record.year} AH")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error preloading cache from Room: ${e.message}")
        }
    }

    fun clearCacheForTesting(context: Context? = null) {
        memoryCache.clear()
        lastSyncSuccessMap.clear()
        lastSyncFailureMap.clear()
        if (context != null) {
            try {
                context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE).edit().clear().apply()
            } catch (_: Exception) {}
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        AppDatabase.getDatabase(context).hijriCacheDao().deleteOldCache(Long.MAX_VALUE)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    val MONTH_NAMES_EN = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah"
    )

    val MONTH_NAMES_AR = arrayOf(
        "المحرّم", "صفر", "ربيع الأوّل", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    val KEY_ISLAMIC_EVENTS = listOf(
        IslamicEvent("Islamic New Year", "رأس السنة الهجرية", 1, 1, "Muharram", "Beginning of the Hijri year."),
        IslamicEvent("Day of Ashura", "يوم عاشوراء", 10, 1, "Muharram", "Significant day associated with fasting and remembrance in Islamic tradition."),
        IslamicEvent("Mawlid al-Nabi", "مولد النبي", 12, 3, "Rabi' al-Awwal", "Commemoration of the birth of Prophet Muhammad (PBUH)."),
        IslamicEvent("Isra and Mi'raj", "الإسراء والمعراج", 27, 7, "Rajab", "The miraculous night journey and ascension of Prophet Muhammad (PBUH)."),
        IslamicEvent("Mid-Sha'ban", "ليلة النصف من شعبان", 15, 8, "Sha'ban", "Night of worship, prayer, and reflection preceding Ramadan."),
        IslamicEvent("Ramadan Begins", "بداية شهر رمضان", 1, 9, "Ramadan", "The holy month of fasting, intense devotion, and Quran recitation."),
        IslamicEvent("Battle of Badr", "غزوة بدر الكبرى", 17, 9, "Ramadan", "Historic decisive victory commemorated on the 17th of Ramadan."),
        IslamicEvent("Laylat al-Qadr", "ليلة القدر", 27, 9, "Ramadan", "The Night of Power, better than a thousand months."),
        IslamicEvent("Eid al-Fitr", "عيد الفطر المبارك", 1, 10, "Shawwal", "Joyous festival celebrating the completion of Ramadan."),
        IslamicEvent("Sacred Month Dhu al-Qi'dah", "بداية ذو القعدة", 1, 11, "Dhu al-Qadah", "Beginning of Dhu al-Qi'dah, one of the four sacred months."),
        IslamicEvent("Day of Arafah", "يوم عرفة", 9, 12, "Dhu al-Hijjah", "The pinnacle day of the Hajj pilgrimage and recommended fasting."),
        IslamicEvent("Eid al-Adha", "عيد الأضحى المبارك", 10, 12, "Dhu al-Hijjah", "Festival of Sacrifice honoring Prophet Ibrahim's devotion."),
        IslamicEvent("Day of Tashreeq I", "أيام التشريق - اليوم الأول", 11, 12, "Dhu al-Hijjah", "First of the three days of Tashreeq during Hajj."),
        IslamicEvent("Day of Tashreeq II", "أيام التشريق - اليوم الثاني", 12, 12, "Dhu al-Hijjah", "Second day of Tashreeq during Hajj."),
        IslamicEvent("Day of Tashreeq III", "أيام التشريق - اليوم الثالث", 13, 12, "Dhu al-Hijjah", "Final day of Tashreeq during Hajj.")
    )

    /**
     * Reconstructs an Islamic date for targetDate from a valid cached baseline record.
     */
    fun reconstructFromCachedRecord(record: HijriCacheRecord, targetDate: LocalDate): Triple<Int, Int, Int> {
        val cachedDate = try { LocalDate.parse(record.gregorianDate) } catch (_: Exception) { null }
        if (cachedDate == null || cachedDate == targetDate) {
            return Triple(record.day, record.monthNumber, record.year)
        }
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(cachedDate, targetDate)
        if (daysDiff == 0L) {
            return Triple(record.day, record.monthNumber, record.year)
        }
        var currentDay = record.day
        var currentMonth = record.monthNumber
        var currentYear = record.year

        if (daysDiff > 0) {
            for (i in 0 until daysDiff) {
                val maxDaysInCurrentMonth = getDaysInMonth(currentYear, currentMonth)
                if (currentDay < maxDaysInCurrentMonth) {
                    currentDay++
                } else {
                    currentDay = 1
                    if (currentMonth < 12) {
                        currentMonth++
                    } else {
                        currentMonth = 1
                        currentYear++
                    }
                }
            }
        } else {
            for (i in 0 until (-daysDiff)) {
                if (currentDay > 1) {
                    currentDay--
                } else {
                    if (currentMonth > 1) {
                        currentMonth--
                    } else {
                        currentMonth = 12
                        currentYear--
                    }
                    currentDay = getDaysInMonth(currentYear, currentMonth)
                }
            }
        }
        return Triple(currentDay, currentMonth, currentYear)
    }

    /**
     * Canonical function to calculate the current Islamic (Hijri) date.
     */
    fun getHijriDate(
        timestampMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        latitude: Double = 19.0760,
        longitude: Double = 72.8777,
        maghribTimeMillis: Long? = null,
        method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA,
        customOffsetDays: Int = 0,
        calcMethod: CalcMethod = CalcMethod.KARACHI,
        context: Context? = null
    ): HijriDate {
        val zoneId = try { timeZone.toZoneId() } catch (_: Exception) { java.time.ZoneId.systemDefault() }
        val currentLocalDateTime = Instant.ofEpochMilli(timestampMillis).atZone(zoneId).toLocalDateTime()
        val currentLocalDate = currentLocalDateTime.toLocalDate()

        // 1. Determine local Maghrib (sunset) time for currentLocalDate
        val todayMaghribMillis = if (maghribTimeMillis != null && maghribTimeMillis > 0) {
            maghribTimeMillis
        } else {
            val tzOffset = timeZone.getOffset(timestampMillis) / 3600000.0
            val prayerTimes = PrayerCalc.calculatePrayerTimes(
                latitude = latitude,
                longitude = longitude,
                date = Date(timestampMillis),
                method = calcMethod,
                timeZoneOffsetHours = tzOffset,
                updateActiveTimeZone = false
            )
            prayerTimes.find { it.name == PrayerName.MAGHRIB }?.timeMillis
        }

        // 2. Sunset boundary evaluation
        val isAfterMaghrib = if (todayMaghribMillis != null && todayMaghribMillis > 0) {
            timestampMillis >= todayMaghribMillis
        } else {
            false
        }

        // 3. Target date for Hijri conversion
        val targetDateForHijri = if (isAfterMaghrib) {
            currentLocalDate.plusDays(1)
        } else {
            currentLocalDate
        }

        // 4. Method-specific base mapping
        var isSynced = false
        val cachedExact = getCacheRecord(context, targetDateForHijri, method)
        val (hDay, hMonth, hYear) = if (cachedExact != null) {
            isSynced = true
            Triple(cachedExact.day, cachedExact.monthNumber, cachedExact.year)
        } else {
            val latestCached = getLatestCacheRecord(context, method)
            if (latestCached != null) {
                isSynced = true
                reconstructFromCachedRecord(latestCached, targetDateForHijri)
            } else {
                computeCalibratedHijri(targetDateForHijri, method, customOffsetDays)
            }
        }

        val monthIdx = (hMonth - 1).coerceIn(0, 11)
        val gregorianFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        val gregorianStr = currentLocalDate.format(gregorianFormatter)

        return HijriDate(
            day = hDay,
            monthName = MONTH_NAMES_EN[monthIdx],
            monthArabic = MONTH_NAMES_AR[monthIdx],
            monthNumber = hMonth,
            year = hYear,
            gregorianDateString = gregorianStr,
            isAfterMaghrib = isAfterMaghrib,
            maghribTimeMillis = todayMaghribMillis,
            method = method,
            isSyncedWithInternet = isSynced
        )
    }

    /**
     * Overload accepting java.util.Date for backwards compatibility.
     */
    fun getHijriDate(
        date: Date,
        timeZone: TimeZone = TimeZone.getDefault(),
        latitude: Double = 19.0760,
        longitude: Double = 72.8777,
        maghribTimeMillis: Long? = null,
        method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA,
        customOffsetDays: Int = 0,
        calcMethod: CalcMethod = CalcMethod.KARACHI
    ): HijriDate {
        return getHijriDate(
            timestampMillis = date.time,
            timeZone = timeZone,
            latitude = latitude,
            longitude = longitude,
            maghribTimeMillis = maghribTimeMillis,
            method = method,
            customOffsetDays = customOffsetDays,
            calcMethod = calcMethod
        )
    }

    /**
     * Calibrated fallback calculation based on regional convention.
     */
    private fun computeCalibratedHijri(
        date: LocalDate,
        method: HijriDateMethod,
        customOffsetDays: Int
    ): Triple<Int, Int, Int> {
        val adjustedDate = when (method) {
            HijriDateMethod.REGIONAL_INDIA -> date.minusDays(1).plusDays(customOffsetDays.toLong())
            HijriDateMethod.SAUDI_UMM_AL_QURA -> date.plusDays(customOffsetDays.toLong())
            HijriDateMethod.GLOBAL_ASTRONOMICAL -> date.plusDays(customOffsetDays.toLong())
            HijriDateMethod.CUSTOM_OFFSET -> date.plusDays(customOffsetDays.toLong())
        }

        val hDate = try {
            HijrahDate.from(adjustedDate)
        } catch (e: Exception) {
            HijrahChronology.INSTANCE.date(adjustedDate)
        }

        val y = hDate.get(ChronoField.YEAR)
        val m = hDate.get(ChronoField.MONTH_OF_YEAR)
        val d = hDate.get(ChronoField.DAY_OF_MONTH)
        return Triple(d, m, y)
    }

    /**
     * Synchronizes base mapping with external API (AlAdhan) when internet is available.
     * Strictly validates all responses, prevents spamming via throttling, and preserves
     * cached data across network drops or failure responses.
     */
    suspend fun syncWithInternet(
        context: Context?,
        date: LocalDate = LocalDate.now(),
        method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA,
        cityName: String = "Mumbai",
        latitude: Double = 19.0760,
        longitude: Double = 72.8777,
        force: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val key = "${date}_${method.name}"
        val now = System.currentTimeMillis()

        if (!force) {
            val lastSuccess = lastSyncSuccessMap[key] ?: (if (context != null) {
                try {
                    context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE).getLong("last_success_$key", 0L)
                } catch (_: Exception) { 0L }
            } else 0L)
            if (now - lastSuccess < SYNC_THROTTLE_SUCCESS_MILLIS && lastSuccess > 0L) {
                Log.d(TAG, "Sync skipped: $key recently synced at $lastSuccess")
                return@withContext true
            }

            val lastFailure = lastSyncFailureMap[key] ?: 0L
            if (now - lastFailure < SYNC_THROTTLE_FAILURE_MILLIS && lastFailure > 0L) {
                Log.d(TAG, "Sync skipped: $key failed recently within throttle window")
                return@withContext false
            }
        }

        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "Network not available; using cached/offline Hijri calculation.")
            return@withContext false
        }

        try {
            val formattedDate = String.format(Locale.US, "%02d-%02d-%04d", date.dayOfMonth, date.monthValue, date.year)
            val aladhanMethod = when (method) {
                HijriDateMethod.REGIONAL_INDIA -> 1 // University of Islamic Sciences, Karachi (India/Pakistan)
                HijriDateMethod.SAUDI_UMM_AL_QURA -> 4 // Umm Al-Qura University, Makkah
                HijriDateMethod.GLOBAL_ASTRONOMICAL -> 2 // ISNA
                HijriDateMethod.CUSTOM_OFFSET -> 1
            }
            val urlString = "https://api.aladhan.com/v1/gregorianToHijri/$formattedDate?method=$aladhanMethod"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "FiveLight-IslamicApp/1.6")
            }

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                val data = json.optJSONObject("data")
                val hijri = data?.optJSONObject("hijri")
                if (hijri != null) {
                    val day = hijri.optInt("day")
                    val monthObj = hijri.optJSONObject("month")
                    val month = monthObj?.optInt("number") ?: 0
                    val monthEn = monthObj?.optString("en", "") ?: ""
                    val monthAr = monthObj?.optString("ar", "") ?: ""
                    val year = hijri.optInt("year")

                    // STRICT VALIDATION: Never commit malformed or impossible values
                    if (day in 1..30 && month in 1..12 && year in 1400..1550) {
                        val record = HijriCacheRecord(
                            gregorianDate = date.toString(),
                            day = day,
                            monthNumber = month,
                            monthName = if (monthEn.isNotBlank()) monthEn else MONTH_NAMES_EN[month - 1],
                            monthArabic = if (monthAr.isNotBlank()) monthAr else MONTH_NAMES_AR[month - 1],
                            year = year,
                            method = method.name,
                            syncTimestampMillis = now,
                            cityName = cityName,
                            latitude = latitude,
                            longitude = longitude
                        )
                        saveCacheRecord(context, record)
                        lastSyncFailureMap.remove(key)
                        Log.d(TAG, "Successfully synced and cached Hijri date: $key -> ${record.day} ${record.monthName} ${record.year} AH")
                        return@withContext true
                    } else {
                        Log.w(TAG, "API returned out-of-range Hijri date values: day=$day, month=$month, year=$year. Preserving existing cache.")
                        lastSyncFailureMap[key] = now
                        return@withContext false
                    }
                } else {
                    Log.w(TAG, "API response missing 'hijri' object. Preserving existing cache.")
                    lastSyncFailureMap[key] = now
                    return@withContext false
                }
            } else {
                Log.w(TAG, "API HTTP error: ${connection.responseCode}. Preserving existing cache.")
                lastSyncFailureMap[key] = now
                return@withContext false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network synchronization failed: ${e.message}. Preserving existing cache.")
            lastSyncFailureMap[key] = now
            return@withContext false
        }
    }

    fun getDaysInMonth(year: Int, monthNumber: Int, method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA): Int {
        return try {
            val clampedMonth = monthNumber.coerceIn(1, 12)
            HijrahChronology.INSTANCE.date(year, clampedMonth, 1).lengthOfMonth()
        } catch (e: Exception) {
            if (monthNumber % 2 == 1) 30 else 29
        }
    }

    fun getStartWeekday(year: Int, monthNumber: Int, method: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA): Int {
        return try {
            val clampedMonth = monthNumber.coerceIn(1, 12)
            val hDate = HijrahChronology.INSTANCE.date(year, clampedMonth, 1)
            val gDate = LocalDate.from(hDate)
            gDate.dayOfWeek.value % 7
        } catch (e: Exception) {
            0
        }
    }

    fun getMonthNameEn(monthNumber: Int): String {
        return MONTH_NAMES_EN[(monthNumber - 1).coerceIn(0, 11)]
    }

    fun getMonthNameAr(monthNumber: Int): String {
        return MONTH_NAMES_AR[(monthNumber - 1).coerceIn(0, 11)]
    }

    fun formatHijriString(hijriDate: HijriDate): String {
        return "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} AH"
    }

    fun formatHijriArabicString(hijriDate: HijriDate): String {
        return "${hijriDate.day} ${hijriDate.monthArabic} ${hijriDate.year} هـ"
    }

    fun getDaysUntilAllEvents(
        currentHijriDate: HijriDate? = null,
        fromDate: Date = Date(),
        timeZone: TimeZone = TimeZone.getDefault(),
        maghribTimeMillis: Long? = null
    ): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val currentHDate = currentHijriDate ?: getHijriDate(fromDate.time, timeZone, maghribTimeMillis = maghribTimeMillis)

        for (event in KEY_ISLAMIC_EVENTS) {
            val matchesMonth = event.hijriMonthNumber == currentHDate.monthNumber ||
                    event.hijriMonthName.equals(currentHDate.monthName, ignoreCase = true)
            if (matchesMonth && event.hijriDay == currentHDate.day) {
                result[event.title] = 0
            }
        }

        val zoneId = try { timeZone.toZoneId() } catch (_: Exception) { java.time.ZoneId.systemDefault() }
        var checkLocalDate = Instant.ofEpochMilli(fromDate.time).atZone(zoneId).toLocalDate()
        if (currentHDate.isAfterMaghrib) {
            checkLocalDate = checkLocalDate.plusDays(1)
        }

        for (offset in 1..355) {
            val testDate = checkLocalDate.plusDays(offset.toLong())
            val hDate = try {
                HijrahDate.from(testDate)
            } catch (e: Exception) {
                HijrahChronology.INSTANCE.date(testDate)
            }
            val hMonth = hDate.get(ChronoField.MONTH_OF_YEAR)
            val hDay = hDate.get(ChronoField.DAY_OF_MONTH)
            val monthName = MONTH_NAMES_EN[(hMonth - 1).coerceIn(0, 11)]

            for (event in KEY_ISLAMIC_EVENTS) {
                if (!result.containsKey(event.title)) {
                    val matchesMonth = event.hijriMonthNumber == hMonth ||
                            event.hijriMonthName.equals(monthName, ignoreCase = true)
                    if (matchesMonth && event.hijriDay == hDay) {
                        result[event.title] = offset
                    }
                }
            }
            if (result.size == KEY_ISLAMIC_EVENTS.size) {
                break
            }
        }
        return result
    }

    fun formatDaysUntil(days: Int): String {
        return when (days) {
            0 -> "Today"
            1 -> "in 1 day"
            else -> "in $days days"
        }
    }
}
