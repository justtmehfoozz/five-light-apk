package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.DhikrHistoryEntity
import com.example.data.db.PrayerLogEntity
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.DhikrPreset
import com.example.data.model.Madhab
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.util.PrayerCalc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(
    private val db: AppDatabase,
    context: Context? = null
) {
    private val prefs = context?.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)

    val PREDEFINED_CITIES = listOf(
        // Indian locations
        CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI),
        CityLocation("Bhiwandi", "India", 19.2812, 73.0483, 5.5, CalcMethod.KARACHI),
        CityLocation("Thane", "India", 19.2183, 72.9781, 5.5, CalcMethod.KARACHI),
        CityLocation("Delhi", "India", 28.6139, 77.2090, 5.5, CalcMethod.KARACHI),
        CityLocation("Kolkata", "India", 22.5726, 88.3639, 5.5, CalcMethod.KARACHI),
        CityLocation("Hyderabad", "India", 17.3850, 78.4867, 5.5, CalcMethod.KARACHI),
        CityLocation("Bengaluru", "India", 12.9716, 77.5946, 5.5, CalcMethod.KARACHI),
        CityLocation("India", "Asia/Kolkata", 20.5937, 78.9629, 5.5, CalcMethod.KARACHI),

        // Global locations
        CityLocation("Mecca", "Saudi Arabia", 21.3891, 39.8579, 3.0, CalcMethod.UMM_AL_QURA),
        CityLocation("Medina", "Saudi Arabia", 24.5247, 39.5692, 3.0, CalcMethod.UMM_AL_QURA),
        CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, 3.0, CalcMethod.MWL),
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, 0.0, CalcMethod.MWL),
        CityLocation("New York", "USA", 40.7128, -74.0060, -5.0, CalcMethod.ISNA),
        CityLocation("Cairo", "Egypt", 30.0444, 31.2357, 2.0, CalcMethod.EGYPTIAN),
        CityLocation("Dubai", "UAE", 25.2048, 55.2708, 4.0, CalcMethod.MWL),
        CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, 7.0, CalcMethod.SINGAPORE),
        CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0, CalcMethod.SINGAPORE),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, -5.0, CalcMethod.ISNA),
        CityLocation("Sydney", "Australia", -33.8688, 151.2093, 10.0, CalcMethod.MWL),
        CityLocation("Singapore", "Singapore", 1.3521, 103.8198, 8.0, CalcMethod.SINGAPORE),
        CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, 5.0, CalcMethod.KARACHI),
        CityLocation("Berlin", "Germany", 52.5200, 13.4050, 1.0, CalcMethod.MWL),
        CityLocation("Los Angeles", "USA", 34.0522, -118.2437, -8.0, CalcMethod.ISNA)
    )

    val DHIKR_PRESETS = listOf(
        DhikrPreset("subhanallah", "SubhanAllah", "سُبْحَانَ اللَّهِ", "Glory be to Allah", 33),
        DhikrPreset("alhamdulillah", "Alhamdulillah", "الْحَمْدُ لِلَّهِ", "Praise be to Allah", 33),
        DhikrPreset("allahuakbar", "Allahu Akbar", "اللَّهُ أَكْبَرُ", "Allah is the Greatest", 33),
        DhikrPreset("astaghfirullah", "Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", "I seek forgiveness from Allah", 100),
        DhikrPreset("lailahaillallah", "La ilaha illallah", "لَا إِلَٰهَ إِلَّا اللَّهُ", "There is no god but Allah", 100),
        DhikrPreset("salawat", "Salawat upon Prophet", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ", "O Allah, send blessings upon Muhammad", 100)
    )

    // User Preferences state with SharedPreferences restoration
    private val savedCityName = prefs?.getString("selected_city_name", null)
    private val initialCity = PREDEFINED_CITIES.find { it.cityName == savedCityName } ?: PREDEFINED_CITIES[0]
    private val _selectedCity = MutableStateFlow(initialCity)
    val selectedCity: StateFlow<CityLocation> = _selectedCity

    private val savedCalcMethod = prefs?.getString("calc_method", null)
    private val initialCalcMethod = CalcMethod.entries.find { it.name == savedCalcMethod } ?: initialCity.defaultCalcMethod
    private val _calcMethod = MutableStateFlow(initialCalcMethod)
    val calcMethod: StateFlow<CalcMethod> = _calcMethod

    private val savedMadhab = prefs?.getString("madhab", null)
    private val initialMadhab = Madhab.entries.find { it.name == savedMadhab } ?: Madhab.STANDARD
    private val _madhab = MutableStateFlow(initialMadhab)
    val madhab: StateFlow<Madhab> = _madhab

    private val savedAppearance = prefs?.getString("appearance_mode", null)
    private val initialAppearance = AppearanceMode.entries.find { it.name == savedAppearance } ?: AppearanceMode.SYSTEM
    private val _appearanceMode = MutableStateFlow(initialAppearance)
    val appearanceMode: StateFlow<AppearanceMode> = _appearanceMode

    fun setCity(city: CityLocation) {
        _selectedCity.value = city
        prefs?.edit()?.putString("selected_city_name", city.cityName)?.apply()
    }

    fun setCalcMethod(method: CalcMethod) {
        _calcMethod.value = method
        prefs?.edit()?.putString("calc_method", method.name)?.apply()
    }

    fun setMadhab(m: Madhab) {
        _madhab.value = m
        prefs?.edit()?.putString("madhab", m.name)?.apply()
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        _appearanceMode.value = mode
        prefs?.edit()?.putString("appearance_mode", mode.name)?.apply()
    }

    fun getTodayPrayerTimes(): List<PrayerItem> {
        val city = _selectedCity.value
        return PrayerCalc.calculatePrayerTimes(
            latitude = city.latitude,
            longitude = city.longitude,
            date = Date(),
            method = _calcMethod.value,
            madhab = _madhab.value,
            timeZoneOffsetHours = city.timezoneOffsetHours
        )
    }

    // Room DB integrations
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getPrayerLogForToday(): Flow<PrayerLogEntity?> {
        return db.prayerLogDao().getPrayerLogForDate(getTodayDateString())
    }

    suspend fun togglePrayerCompleted(prayerName: PrayerName, currentLog: PrayerLogEntity?) {
        val date = getTodayDateString()
        val existing = currentLog ?: PrayerLogEntity(date = date)
        val updated = when (prayerName) {
            PrayerName.FAJR -> existing.copy(fajrCompleted = !existing.fajrCompleted)
            PrayerName.DHUHR -> existing.copy(dhuhrCompleted = !existing.dhuhrCompleted)
            PrayerName.ASR -> existing.copy(asrCompleted = !existing.asrCompleted)
            PrayerName.MAGHRIB -> existing.copy(maghribCompleted = !existing.maghribCompleted)
            PrayerName.ISHA -> existing.copy(ishaCompleted = !existing.ishaCompleted)
            PrayerName.SUNRISE -> existing
        }
        db.prayerLogDao().insertOrUpdatePrayerLog(updated)
    }

    // Dhikr History
    val dhikrHistory: Flow<List<DhikrHistoryEntity>> = db.dhikrHistoryDao().getAllDhikrHistory()

    suspend fun recordDhikrCompletion(dhikrPreset: DhikrPreset, count: Int, target: Int) {
        val entity = DhikrHistoryEntity(
            dhikrName = dhikrPreset.nameEnglish,
            arabicText = dhikrPreset.nameArabic,
            countCompleted = count,
            target = target
        )
        db.dhikrHistoryDao().insertDhikrHistory(entity)
    }

    // Quran Bookmarks
    val bookmarks: Flow<List<BookmarkEntity>> = db.bookmarkDao().getAllBookmarks()

    fun isBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean> {
        return db.bookmarkDao().isBookmarked(surahNumber, verseNumber)
    }

    suspend fun toggleBookmark(
        surahNumber: Int,
        verseNumber: Int,
        surahNameEn: String,
        surahNameAr: String,
        textAr: String,
        textEn: String,
        currentlyBookmarked: Boolean
    ) {
        if (currentlyBookmarked) {
            db.bookmarkDao().removeBookmark(surahNumber, verseNumber)
        } else {
            val bookmark = BookmarkEntity(
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                surahNameEnglish = surahNameEn,
                surahNameArabic = surahNameAr,
                verseTextArabic = textAr,
                verseTextTranslation = textEn
            )
            db.bookmarkDao().addBookmark(bookmark)
        }
    }
}
