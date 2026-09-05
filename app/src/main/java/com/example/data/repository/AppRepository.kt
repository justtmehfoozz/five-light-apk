package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.DhikrHistoryEntity
import com.example.data.db.DuaCategoryEntity
import com.example.data.db.DuaCategoryWithDuas
import com.example.data.db.DuaEntity
import com.example.data.db.PrayerLogEntity
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.DhikrPreset
import com.example.data.model.Madhab
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.TimeFormat
import com.example.data.util.PrayerCalc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(
    private val db: AppDatabase,
    context: Context? = null
) {
    private val appContext = context?.applicationContext
    private val prefs = context?.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)
    val authRepository: com.example.data.auth.AuthRepository? = appContext?.let { com.example.data.auth.AuthRepository.getInstance(it) }
    var syncManager: com.example.data.sync.FirestoreSyncManager? = null

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

    // Downloaded Audio Repository Methods
    val allDownloadedAudio: Flow<List<com.example.data.db.DownloadedAudioEntity>> = db.downloadedAudioDao().getAllDownloadedAudio()

    suspend fun recordDownloadedAudio(
        surahNumber: Int,
        reciterId: String = "ar.alafasy",
        status: String,
        totalVerses: Int,
        downloadedVerses: Int,
        sizeBytes: Long
    ) {
        db.downloadedAudioDao().insertOrUpdate(
            com.example.data.db.DownloadedAudioEntity(
                surahNumber = surahNumber,
                reciterId = reciterId,
                status = status,
                totalVerses = totalVerses,
                downloadedVerses = downloadedVerses,
                sizeBytes = sizeBytes
            )
        )
    }

    suspend fun removeDownloadedAudioRecord(surahNumber: Int, reciterId: String = "ar.alafasy") {
        db.downloadedAudioDao().deleteAudio(surahNumber, reciterId)
    }

    suspend fun clearAllDownloadedAudioRecords(reciterId: String = "ar.alafasy") {
        db.downloadedAudioDao().deleteAllAudio(reciterId)
    }

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

    private val savedTimeFormat = prefs?.getString("time_format", null)
    private val initialTimeFormat = TimeFormat.fromName(savedTimeFormat)
    private val _timeFormat = MutableStateFlow(initialTimeFormat)
    val timeFormat: StateFlow<TimeFormat> = _timeFormat

    private val savedTasbeehSoundId = prefs?.getString("tasbeeh_sound", null)
    private val _tasbeehSound = MutableStateFlow(com.example.data.model.TasbeehSound.fromId(savedTasbeehSoundId))
    val tasbeehSound: StateFlow<com.example.data.model.TasbeehSound> = _tasbeehSound

    private val savedVibrationEnabled = prefs?.getBoolean("vibration_enabled", true) ?: true
    private val _vibrationEnabled = MutableStateFlow(savedVibrationEnabled)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled

    private val savedHijriMethod = prefs?.getString("hijri_date_method", null)
    private val initialHijriMethod = com.example.data.model.HijriDateMethod.entries.find { it.name == savedHijriMethod }
        ?: com.example.data.model.HijriDateMethod.REGIONAL_INDIA
    private val _hijriDateMethod = MutableStateFlow(initialHijriMethod)
    val hijriDateMethod: StateFlow<com.example.data.model.HijriDateMethod> = _hijriDateMethod

    private val savedCustomHijriOffset = prefs?.getInt("custom_hijri_offset", 0) ?: 0
    private val _customHijriOffset = MutableStateFlow(savedCustomHijriOffset)
    val customHijriOffset: StateFlow<Int> = _customHijriOffset

    val islamicDateRepository: IslamicDateRepository = IslamicDateRepository.getInstance(context).apply {
        updateLocation(_selectedCity.value)
        updateCalcMethod(_calcMethod.value)
        updateHijriMethod(_hijriDateMethod.value)
        updateCustomOffset(_customHijriOffset.value)
    }

    private val savedNaflOrderStr = prefs?.getString("nafl_prayer_order", null)
    private val initialNaflOrder = if (!savedNaflOrderStr.isNullOrEmpty()) {
        val parsed = savedNaflOrderStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val missing = com.example.data.model.NaflPreferences.DEFAULT_NAFL_ORDER.filter { !parsed.contains(it) }
        parsed + missing
    } else {
        com.example.data.model.NaflPreferences.DEFAULT_NAFL_ORDER
    }

    private val initialNaflPrefs = com.example.data.model.NaflPreferences(
        tahajjudEnabled = prefs?.getBoolean("nafl_tahajjud_enabled", false) ?: false,
        ishraqEnabled = prefs?.getBoolean("nafl_ishraq_enabled", false) ?: false,
        duhaEnabled = prefs?.getBoolean("nafl_duha_enabled", false) ?: false,
        awwabinEnabled = prefs?.getBoolean("nafl_awwabin_enabled", false) ?: false,
        naflOrder = initialNaflOrder
    )
    private val _naflPreferences = MutableStateFlow(initialNaflPrefs)
    val naflPreferences: StateFlow<com.example.data.model.NaflPreferences> = _naflPreferences

    // Home Features Preferences
    private val savedFeatureOrderStr = prefs?.getString("home_feature_order", null)
    private val initialFeatureOrder = if (!savedFeatureOrderStr.isNullOrEmpty()) {
        val parsed = savedFeatureOrderStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val missing = com.example.data.model.HomeFeaturesPreferences.DEFAULT_FEATURE_ORDER.filter { !parsed.contains(it) }
        parsed + missing
    } else {
        com.example.data.model.HomeFeaturesPreferences.DEFAULT_FEATURE_ORDER
    }

    private val initialHomeFeatures = com.example.data.model.HomeFeaturesPreferences(
        continueReadingEnabled = prefs?.getBoolean("feat_continue_reading", true) ?: true,
        rightNowEnabled = prefs?.getBoolean("feat_right_now", true) ?: true,
        tonightEnabled = prefs?.getBoolean("feat_tonight", true) ?: true,
        nextOpportunityEnabled = prefs?.getBoolean("feat_next_opportunity", true) ?: true,
        prayerPrepEnabled = prefs?.getBoolean("feat_prayer_prep", true) ?: true,
        weeklyOverviewEnabled = prefs?.getBoolean("feat_weekly_overview", true) ?: true,
        momentsEnabled = prefs?.getBoolean("feat_moments", true) ?: true,
        quietModeEnabled = prefs?.getBoolean("feat_quiet_mode", false) ?: false,
        prayerJourneyEnabled = prefs?.getBoolean("feat_prayer_journey", true) ?: true,
        recentlyReadEnabled = prefs?.getBoolean("feat_recently_read", true) ?: true,
        quranLensEnabled = prefs?.getBoolean("feat_quran_lens", true) ?: true,
        nightIsComingEnabled = prefs?.getBoolean("feat_night_is_coming", true) ?: true,
        featureOrder = initialFeatureOrder
    )
    private val _homeFeaturesPreferences = MutableStateFlow(initialHomeFeatures)
    val homeFeaturesPreferences: StateFlow<com.example.data.model.HomeFeaturesPreferences> = _homeFeaturesPreferences

    // Quran Last Read Position
    private val initialLastRead: com.example.data.model.QuranLastRead? = run {
        val surahNum = prefs?.getInt("last_read_surah", -1) ?: -1
        if (surahNum > 0) {
            com.example.data.model.QuranLastRead(
                surahNumber = surahNum,
                surahNameEnglish = prefs?.getString("last_read_surah_en", "") ?: "",
                surahNameArabic = prefs?.getString("last_read_surah_ar", "") ?: "",
                verseNumber = prefs?.getInt("last_read_verse", 1) ?: 1,
                verseIndex = prefs?.getInt("last_read_verse_index", 0) ?: 0,
                timestamp = prefs?.getLong("last_read_timestamp", System.currentTimeMillis()) ?: System.currentTimeMillis()
            )
        } else null
    }
    private val _lastReadPosition = MutableStateFlow<com.example.data.model.QuranLastRead?>(initialLastRead)
    val lastReadPosition: StateFlow<com.example.data.model.QuranLastRead?> = _lastReadPosition

    // Recently Read History (Last 7 Verses)
    private val _recentlyReadList = MutableStateFlow<List<com.example.data.model.QuranLastRead>>(loadRecentlyReadFromPrefs())
    val recentlyReadList: StateFlow<List<com.example.data.model.QuranLastRead>> = _recentlyReadList

    private fun loadRecentlyReadFromPrefs(): List<com.example.data.model.QuranLastRead> {
        val jsonString = prefs?.getString("recently_read_json", null) ?: return initialLastRead?.let { listOf(it) } ?: emptyList()
        return try {
            val array = org.json.JSONArray(jsonString)
            val list = mutableListOf<com.example.data.model.QuranLastRead>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.data.model.QuranLastRead(
                        surahNumber = obj.getInt("surahNumber"),
                        surahNameEnglish = obj.optString("surahNameEnglish", ""),
                        surahNameArabic = obj.optString("surahNameArabic", ""),
                        verseNumber = obj.optInt("verseNumber", 1),
                        verseIndex = obj.optInt("verseIndex", 0),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list.take(7)
        } catch (e: Exception) {
            initialLastRead?.let { listOf(it) } ?: emptyList()
        }
    }

    private fun saveRecentlyReadToPrefs(list: List<com.example.data.model.QuranLastRead>) {
        val array = org.json.JSONArray()
        list.forEach { item ->
            val obj = org.json.JSONObject()
            obj.put("surahNumber", item.surahNumber)
            obj.put("surahNameEnglish", item.surahNameEnglish)
            obj.put("surahNameArabic", item.surahNameArabic)
            obj.put("verseNumber", item.verseNumber)
            obj.put("verseIndex", item.verseIndex)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs?.edit()?.putString("recently_read_json", array.toString())?.apply()
    }

    // Custom Dhikrs Persistence
    private val _customDhikrs = MutableStateFlow<List<DhikrPreset>>(loadCustomDhikrs())
    val customDhikrs: StateFlow<List<DhikrPreset>> = _customDhikrs

    private fun loadCustomDhikrs(): List<DhikrPreset> {
        val jsonString = prefs?.getString("custom_dhikrs_json", null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonString)
            val list = mutableListOf<DhikrPreset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    DhikrPreset(
                        id = obj.getString("id"),
                        nameEnglish = obj.getString("nameEnglish"),
                        nameArabic = obj.optString("nameArabic", ""),
                        translation = obj.optString("translation", ""),
                        defaultTarget = obj.optInt("defaultTarget", 33),
                        isCustom = true
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomDhikrsToPrefs(list: List<DhikrPreset>) {
        val jsonArray = org.json.JSONArray()
        list.forEach { preset ->
            val obj = org.json.JSONObject()
            obj.put("id", preset.id)
            obj.put("nameEnglish", preset.nameEnglish)
            obj.put("nameArabic", preset.nameArabic)
            obj.put("translation", preset.translation)
            obj.put("defaultTarget", preset.defaultTarget)
            jsonArray.put(obj)
        }
        prefs?.edit()?.putString("custom_dhikrs_json", jsonArray.toString())?.apply()
    }

    fun addCustomDhikr(preset: DhikrPreset) {
        val updated = _customDhikrs.value + preset
        _customDhikrs.value = updated
        saveCustomDhikrsToPrefs(updated)
        prefs?.edit()?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    fun updateCustomDhikr(preset: DhikrPreset) {
        val updated = _customDhikrs.value.map { if (it.id == preset.id) preset else it }
        _customDhikrs.value = updated
        saveCustomDhikrsToPrefs(updated)
        prefs?.edit()?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    fun deleteCustomDhikr(presetId: String) {
        val updated = _customDhikrs.value.filterNot { it.id == presetId }
        _customDhikrs.value = updated
        saveCustomDhikrsToPrefs(updated)
        prefs?.edit()?.remove("dhikr_count_$presetId")?.remove("dhikr_target_$presetId")
            ?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    // Custom Targets Persistence
    val BUILT_IN_TARGETS = listOf(33, 99, 100)
    private val _customTargets = MutableStateFlow<List<Int>>(loadCustomTargets())
    val customTargets: StateFlow<List<Int>> = _customTargets

    private fun loadCustomTargets(): List<Int> {
        val jsonString = prefs?.getString("custom_targets_json", null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonString)
            val list = mutableListOf<Int>()
            for (i in 0 until jsonArray.length()) {
                val target = jsonArray.getInt(i)
                if (target > 0 && !BUILT_IN_TARGETS.contains(target) && !list.contains(target)) {
                    list.add(target)
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomTargetsToPrefs(list: List<Int>) {
        val jsonArray = org.json.JSONArray()
        list.forEach { jsonArray.put(it) }
        prefs?.edit()?.putString("custom_targets_json", jsonArray.toString())?.apply()
    }

    fun addCustomTarget(target: Int) {
        if (target <= 0 || BUILT_IN_TARGETS.contains(target) || _customTargets.value.contains(target)) return
        val updated = _customTargets.value + target
        _customTargets.value = updated
        saveCustomTargetsToPrefs(updated)
        prefs?.edit()?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    fun deleteCustomTarget(target: Int) {
        if (BUILT_IN_TARGETS.contains(target)) return
        val updated = _customTargets.value.filterNot { it == target }
        _customTargets.value = updated
        saveCustomTargetsToPrefs(updated)
        prefs?.edit()?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    // Per-Dhikr Count & Target Persistence
    fun getSavedDhikrCount(presetId: String): Int {
        return prefs?.getInt("dhikr_count_$presetId", 0) ?: 0
    }

    fun getDhikrCount(presetId: String): Int {
        return getSavedDhikrCount(presetId)
    }

    fun saveDhikrCount(presetId: String, count: Int) {
        prefs?.edit()?.putInt("dhikr_count_$presetId", count)
            ?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    fun getSavedDhikrTarget(presetId: String, defaultTarget: Int): Int {
        return prefs?.getInt("dhikr_target_$presetId", defaultTarget) ?: defaultTarget
    }

    fun getDhikrTarget(presetId: String): Int {
        return getSavedDhikrTarget(presetId, 33)
    }

    fun saveDhikrTarget(presetId: String, target: Int) {
        prefs?.edit()?.putInt("dhikr_target_$presetId", target)
            ?.putLong("tasbeeh_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyTasbeehStateChanged()
    }

    fun setCity(city: CityLocation) {
        _selectedCity.value = city
        prefs?.edit()?.putString("selected_city_name", city.cityName)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        islamicDateRepository.updateLocation(city)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setCalcMethod(method: CalcMethod) {
        _calcMethod.value = method
        prefs?.edit()?.putString("calc_method", method.name)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        islamicDateRepository.updateCalcMethod(method)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setMadhab(m: Madhab) {
        _madhab.value = m
        prefs?.edit()?.putString("madhab", m.name)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        _appearanceMode.value = mode
        prefs?.edit()?.putString("appearance_mode", mode.name)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setTimeFormat(format: TimeFormat) {
        _timeFormat.value = format
        prefs?.edit()?.putString("time_format", format.name)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setTasbeehSound(sound: com.example.data.model.TasbeehSound) {
        _tasbeehSound.value = sound
        prefs?.edit()?.putString("tasbeeh_sound", sound.id)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs?.edit()?.putBoolean("vibration_enabled", enabled)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setHijriDateMethod(method: com.example.data.model.HijriDateMethod) {
        _hijriDateMethod.value = method
        prefs?.edit()?.putString("hijri_date_method", method.name)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        islamicDateRepository.updateHijriMethod(method)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setCustomHijriOffset(offset: Int) {
        _customHijriOffset.value = offset
        prefs?.edit()?.putInt("custom_hijri_offset", offset)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        islamicDateRepository.updateCustomOffset(offset)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setNaflPreference(
        tahajjud: Boolean = _naflPreferences.value.tahajjudEnabled,
        ishraq: Boolean = _naflPreferences.value.ishraqEnabled,
        duha: Boolean = _naflPreferences.value.duhaEnabled,
        awwabin: Boolean = _naflPreferences.value.awwabinEnabled
    ) {
        val updated = com.example.data.model.NaflPreferences(
            tahajjudEnabled = tahajjud,
            ishraqEnabled = ishraq,
            duhaEnabled = duha,
            awwabinEnabled = awwabin,
            naflOrder = _naflPreferences.value.naflOrder
        )
        _naflPreferences.value = updated
        prefs?.edit()
            ?.putBoolean("nafl_tahajjud_enabled", tahajjud)
            ?.putBoolean("nafl_ishraq_enabled", ishraq)
            ?.putBoolean("nafl_duha_enabled", duha)
            ?.putBoolean("nafl_awwabin_enabled", awwabin)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())
            ?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setNaflOrder(newOrder: List<String>) {
        val updated = _naflPreferences.value.copy(naflOrder = newOrder)
        _naflPreferences.value = updated
        prefs?.edit()?.putString("nafl_prayer_order", newOrder.joinToString(","))
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun resetNaflOrder() {
        setNaflOrder(com.example.data.model.NaflPreferences.DEFAULT_NAFL_ORDER)
    }

    fun getTodayPrayerTimes(): List<PrayerItem> {
        val city = _selectedCity.value
        return PrayerCalc.calculatePrayerTimes(
            latitude = city.latitude,
            longitude = city.longitude,
            date = Date(),
            method = _calcMethod.value,
            madhab = _madhab.value,
            timeZoneOffsetHours = city.timezoneOffsetHours,
            is24Hour = _timeFormat.value.is24Hour
        )
    }

    fun setHomeFeaturesPreference(
        continueReading: Boolean = _homeFeaturesPreferences.value.continueReadingEnabled,
        rightNow: Boolean = _homeFeaturesPreferences.value.rightNowEnabled,
        tonight: Boolean = _homeFeaturesPreferences.value.tonightEnabled,
        nextOpportunity: Boolean = _homeFeaturesPreferences.value.nextOpportunityEnabled,
        prayerPrep: Boolean = _homeFeaturesPreferences.value.prayerPrepEnabled,
        weeklyOverview: Boolean = _homeFeaturesPreferences.value.weeklyOverviewEnabled,
        moments: Boolean = _homeFeaturesPreferences.value.momentsEnabled,
        quietMode: Boolean = _homeFeaturesPreferences.value.quietModeEnabled,
        prayerJourney: Boolean = _homeFeaturesPreferences.value.prayerJourneyEnabled,
        recentlyRead: Boolean = _homeFeaturesPreferences.value.recentlyReadEnabled,
        quranLens: Boolean = _homeFeaturesPreferences.value.quranLensEnabled,
        nightIsComing: Boolean = _homeFeaturesPreferences.value.nightIsComingEnabled
    ) {
        val updated = com.example.data.model.HomeFeaturesPreferences(
            continueReadingEnabled = continueReading,
            rightNowEnabled = rightNow,
            tonightEnabled = tonight,
            nextOpportunityEnabled = nextOpportunity,
            prayerPrepEnabled = prayerPrep,
            weeklyOverviewEnabled = weeklyOverview,
            momentsEnabled = moments,
            quietModeEnabled = quietMode,
            prayerJourneyEnabled = prayerJourney,
            recentlyReadEnabled = recentlyRead,
            quranLensEnabled = quranLens,
            nightIsComingEnabled = nightIsComing,
            featureOrder = _homeFeaturesPreferences.value.featureOrder
        )
        _homeFeaturesPreferences.value = updated
        prefs?.edit()
            ?.putBoolean("feat_continue_reading", continueReading)
            ?.putBoolean("feat_right_now", rightNow)
            ?.putBoolean("feat_tonight", tonight)
            ?.putBoolean("feat_next_opportunity", nextOpportunity)
            ?.putBoolean("feat_prayer_prep", prayerPrep)
            ?.putBoolean("feat_weekly_overview", weeklyOverview)
            ?.putBoolean("feat_moments", moments)
            ?.putBoolean("feat_quiet_mode", quietMode)
            ?.putBoolean("feat_prayer_journey", prayerJourney)
            ?.putBoolean("feat_recently_read", recentlyRead)
            ?.putBoolean("feat_quran_lens", quranLens)
            ?.putBoolean("feat_night_is_coming", nightIsComing)
            ?.putLong("preferences_updated_at", System.currentTimeMillis())
            ?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun setHomeFeatureOrder(newOrder: List<String>) {
        val updated = _homeFeaturesPreferences.value.copy(featureOrder = newOrder)
        _homeFeaturesPreferences.value = updated
        prefs?.edit()?.putString("home_feature_order", newOrder.joinToString(","))
            ?.putLong("preferences_updated_at", System.currentTimeMillis())?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPreferencesChanged()
    }

    fun resetHomeFeatureOrder() {
        setHomeFeatureOrder(com.example.data.model.HomeFeaturesPreferences.DEFAULT_FEATURE_ORDER)
    }

    fun saveLastReadPosition(
        surahNumber: Int,
        surahNameEn: String,
        surahNameAr: String,
        verseNumber: Int,
        verseIndex: Int
    ) {
        val now = System.currentTimeMillis()
        val lastRead = com.example.data.model.QuranLastRead(
            surahNumber = surahNumber,
            surahNameEnglish = surahNameEn,
            surahNameArabic = surahNameAr,
            verseNumber = verseNumber,
            verseIndex = verseIndex,
            timestamp = now
        )
        _lastReadPosition.value = lastRead

        // Update recently read list (max 7 items, deduplicated)
        val currentList = _recentlyReadList.value.filterNot {
            it.surahNumber == surahNumber && it.verseNumber == verseNumber
        }.toMutableList()
        currentList.add(0, lastRead)
        val trimmed = currentList.take(7)
        _recentlyReadList.value = trimmed
        saveRecentlyReadToPrefs(trimmed)

        prefs?.edit()
            ?.putInt("last_read_surah", surahNumber)
            ?.putString("last_read_surah_en", surahNameEn)
            ?.putString("last_read_surah_ar", surahNameAr)
            ?.putInt("last_read_verse", verseNumber)
            ?.putInt("last_read_verse_index", verseIndex)
            ?.putLong("last_read_timestamp", lastRead.timestamp)
            ?.putLong("quran_progress_updated_at", now)
            ?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyQuranProgressChanged()
    }

    fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>> {
        return db.prayerLogDao().getPrayerLogsForDates(dates)
    }
    // Qada Persistence
    fun getQadaCount(prayerName: com.example.data.model.PrayerName): Int {
        return prefs?.getInt("qada_${prayerName.name}", 0) ?: 0
    }

    fun getQadaTimestamp(prayerName: com.example.data.model.PrayerName): Long {
        return prefs?.getLong("qada_timestamp_${prayerName.name}", 0L) ?: 0L
    }

    fun setQadaCount(prayerName: com.example.data.model.PrayerName, count: Int) {
        val safeCount = Math.max(0, count)
        val now = System.currentTimeMillis()
        val editor = prefs?.edit()
        editor?.putInt("qada_${prayerName.name}", safeCount)
        editor?.putLong("qada_timestamp_${prayerName.name}", now)
        if (safeCount > 0) {
            editor?.putBoolean("qada_ever_added_${prayerName.name}", true)
        }
        editor?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyQadaChanged(prayerName)
    }

    fun hasEverHadQada(prayerName: com.example.data.model.PrayerName): Boolean {
        return prefs?.getBoolean("qada_ever_added_${prayerName.name}", false) ?: false
    }

    fun setHasEverHadQada(prayerName: com.example.data.model.PrayerName, everAdded: Boolean) {
        prefs?.edit()?.putBoolean("qada_ever_added_${prayerName.name}", everAdded)
            ?.putLong("qada_timestamp_${prayerName.name}", System.currentTimeMillis())
            ?.apply()
    }
    
    // Quran Goal Persistence
    fun getDailyQuranGoal(): Int {
        return prefs?.getInt("daily_quran_goal", 0) ?: 0
    }
    
    fun setDailyQuranGoal(pages: Int) {
        val safePages = Math.max(0, pages)
        val now = System.currentTimeMillis()
        prefs?.edit()?.putInt("daily_quran_goal", safePages)
            ?.putLong("quran_progress_updated_at", now)?.apply()
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyQuranProgressChanged()
    }

    // Surah Playback Progress Persistence
    fun getSurahPlaybackProgressMap(): Map<Int, Float> {
        val result = mutableMapOf<Int, Float>()
        val all = prefs?.all ?: emptyMap()
        for ((key, value) in all) {
            if (key.startsWith("surah_audio_prog_")) {
                val sNum = key.removePrefix("surah_audio_prog_").toIntOrNull()
                val prog = when (value) {
                    is Float -> value
                    is Double -> value.toFloat()
                    is Int -> value.toFloat()
                    is String -> value.toFloatOrNull() ?: 0f
                    else -> 0f
                }
                if (sNum != null && prog > 0f) {
                    result[sNum] = prog
                }
            }
        }
        return result
    }

    fun setSurahPlaybackProgress(surahNumber: Int, progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        prefs?.edit()?.putFloat("surah_audio_prog_$surahNumber", clamped)?.apply()
    }

    fun getAllPrayerLogs(): kotlinx.coroutines.flow.Flow<List<com.example.data.db.PrayerLogEntity>> {
        return db.prayerLogDao().getAllPrayerLogs()
    }


    // Room DB integrations
    fun getTodayDateString(): String {
        val city = _selectedCity.value
        val offsetMillis = (city.timezoneOffsetHours * 3600000).toInt()
        val offsetHours = offsetMillis / 3600000
        val offsetMins = Math.abs((offsetMillis / 60000) % 60)
        val tzId = String.format(java.util.Locale.US, "GMT%+03d:%02d", offsetHours, offsetMins)
        val cityTz = java.util.SimpleTimeZone(offsetMillis, tzId)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = cityTz
        return sdf.format(Date())
    }

    private val prayerLogMutex = Mutex()

    fun getPrayerLogForToday(): Flow<PrayerLogEntity?> {
        return db.prayerLogDao().getPrayerLogForDate(getTodayDateString())
    }

    fun getPrayerLogForDateFlow(dateString: String): Flow<PrayerLogEntity?> {
        return db.prayerLogDao().getPrayerLogForDate(dateString)
    }

    suspend fun getPrayerLogForDateDirect(dateString: String): PrayerLogEntity? {
        return db.prayerLogDao().getPrayerLogForDateDirect(dateString)
    }

    suspend fun savePrayerNote(
        prayerName: PrayerName,
        dateString: String = getTodayDateString(),
        note: String?
    ): Boolean = prayerLogMutex.withLock {
        if (prayerName == PrayerName.SUNRISE) return@withLock false
        val existing = db.prayerLogDao().getPrayerLogForDateDirect(dateString) ?: PrayerLogEntity(date = dateString)
        val now = System.currentTimeMillis()
        val updated = existing.withNote(prayerName, note).copy(updatedAt = now, isDeleted = false)
        db.prayerLogDao().insertOrUpdatePrayerLog(updated)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPrayerLogChanged(updated)
        return@withLock true
    }

    suspend fun setPrayerQadaAdded(
        prayerName: PrayerName,
        dateString: String = getTodayDateString(),
        added: Boolean
    ): Boolean = prayerLogMutex.withLock {
        if (prayerName == PrayerName.SUNRISE) return@withLock false
        val existing = db.prayerLogDao().getPrayerLogForDateDirect(dateString) ?: PrayerLogEntity(date = dateString)
        val now = System.currentTimeMillis()
        val updated = existing.withQadaAdded(prayerName, added).copy(updatedAt = now, isDeleted = false)
        db.prayerLogDao().insertOrUpdatePrayerLog(updated)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPrayerLogChanged(updated)
        return@withLock true
    }

    suspend fun setPrayerStatus(
        prayerName: PrayerName,
        dateString: String = getTodayDateString(),
        status: com.example.data.model.PrayerStatus
    ): Boolean = prayerLogMutex.withLock {
        if (prayerName == PrayerName.SUNRISE) return@withLock false

        val todayDate = getTodayDateString()
        if (dateString == todayDate && status == com.example.data.model.PrayerStatus.PRAYED) {
            val todayTimes = getTodayPrayerTimes()
            val prayer = todayTimes.find { it.name == prayerName }
            if (prayer != null && System.currentTimeMillis() < prayer.timeMillis) {
                // Do not allow marking upcoming prayer as completed
                return@withLock false
            }
        }

        val existing = db.prayerLogDao().getPrayerLogForDateDirect(dateString) ?: PrayerLogEntity(date = dateString)
        val wasQadaAdded = existing.isQadaAdded(prayerName)
        val isCompleted = status == com.example.data.model.PrayerStatus.PRAYED
        val isMissed = status == com.example.data.model.PrayerStatus.MISSED
        val now = System.currentTimeMillis()

        val updated = when (prayerName) {
            PrayerName.FAJR -> existing.copy(
                fajrCompleted = isCompleted,
                fajrMissed = isMissed,
                fajrQadaAdded = if (isCompleted) false else existing.fajrQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.DHUHR -> existing.copy(
                dhuhrCompleted = isCompleted,
                dhuhrMissed = isMissed,
                dhuhrQadaAdded = if (isCompleted) false else existing.dhuhrQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.ASR -> existing.copy(
                asrCompleted = isCompleted,
                asrMissed = isMissed,
                asrQadaAdded = if (isCompleted) false else existing.asrQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.MAGHRIB -> existing.copy(
                maghribCompleted = isCompleted,
                maghribMissed = isMissed,
                maghribQadaAdded = if (isCompleted) false else existing.maghribQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.ISHA -> existing.copy(
                ishaCompleted = isCompleted,
                ishaMissed = isMissed,
                ishaQadaAdded = if (isCompleted) false else existing.ishaQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.SUNRISE -> existing
        }
        db.prayerLogDao().insertOrUpdatePrayerLog(updated)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPrayerLogChanged(updated)

        appContext?.let { ctx ->
            if (isCompleted) {
                try {
                    val smartManager = com.example.data.reminder.SmartPrayerNotificationManager(ctx)
                    smartManager.onPrayerCompletedInApp(prayerName, dateString)
                } catch (_: Exception) {}
            } else if (isMissed) {
                try {
                    val smartManager = com.example.data.reminder.SmartPrayerNotificationManager(ctx)
                    smartManager.cancelFollowUpAlarm(prayerName, dateString)
                    smartManager.cancelPrayerNotification(prayerName, dateString)
                } catch (_: Exception) {}
            }
        }

        if (isCompleted && wasQadaAdded) {
            val currentCount = getQadaCount(prayerName)
            if (currentCount > 0) {
                setQadaCount(prayerName, currentCount - 1)
            }
        }
        return@withLock true
    }

    suspend fun makeUpQadaPrayer(prayerName: PrayerName): Boolean = prayerLogMutex.withLock {
        if (prayerName == PrayerName.SUNRISE) return@withLock false

        val currentCount = getQadaCount(prayerName)
        if (currentCount <= 0) return@withLock false

        // 1. Decrement Qada count
        val newCount = (currentCount - 1).coerceAtLeast(0)
        setQadaCount(prayerName, newCount)

        // 2. Identify the originating missed prayer record in Room DB (FIFO: oldest first)
        val allLogsAsc = db.prayerLogDao().getAllPrayerLogsDirectAsc()

        // Prioritize finding the oldest record where this prayer was marked missed and explicitly added to Qada
        val explicitTarget = allLogsAsc.firstOrNull {
            it.isMissed(prayerName) && it.isQadaAdded(prayerName) && !it.isCompleted(prayerName)
        }

        // Fallback: oldest record where this prayer is missed and not completed
        val fallbackTarget = if (explicitTarget == null) {
            allLogsAsc.firstOrNull { it.isMissed(prayerName) && !it.isCompleted(prayerName) }
        } else null

        val targetLog = explicitTarget ?: fallbackTarget

        if (targetLog != null) {
            val now = System.currentTimeMillis()
            val updated = when (prayerName) {
                PrayerName.FAJR -> targetLog.copy(fajrCompleted = true, fajrMissed = false, fajrQadaAdded = false, updatedAt = now, isDeleted = false)
                PrayerName.DHUHR -> targetLog.copy(dhuhrCompleted = true, dhuhrMissed = false, dhuhrQadaAdded = false, updatedAt = now, isDeleted = false)
                PrayerName.ASR -> targetLog.copy(asrCompleted = true, asrMissed = false, asrQadaAdded = false, updatedAt = now, isDeleted = false)
                PrayerName.MAGHRIB -> targetLog.copy(maghribCompleted = true, maghribMissed = false, maghribQadaAdded = false, updatedAt = now, isDeleted = false)
                PrayerName.ISHA -> targetLog.copy(ishaCompleted = true, ishaMissed = false, ishaQadaAdded = false, updatedAt = now, isDeleted = false)
                PrayerName.SUNRISE -> targetLog
            }
            db.prayerLogDao().insertOrUpdatePrayerLog(updated)
            if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPrayerLogChanged(updated)
        }

        return@withLock true
    }

    suspend fun setPrayerCompleted(
        prayerName: PrayerName,
        dateString: String = getTodayDateString(),
        completed: Boolean
    ): Boolean {
        val status = if (completed) com.example.data.model.PrayerStatus.PRAYED else com.example.data.model.PrayerStatus.NEEDS_INPUT
        return setPrayerStatus(prayerName, dateString, status)
    }

    suspend fun togglePrayerCompleted(
        prayerName: PrayerName,
        currentLog: PrayerLogEntity? = null,
        dateString: String = getTodayDateString()
    ): Boolean = prayerLogMutex.withLock {
        if (prayerName == PrayerName.SUNRISE) return@withLock false

        val date = dateString
        // Always read direct from DB inside lock to ensure fresh state and prevent rapid-tap race conditions
        val existing = db.prayerLogDao().getPrayerLogForDateDirect(date) ?: PrayerLogEntity(date = date)
        val isCurrentlyCompleted = existing.isCompleted(prayerName)

        val targetStatus = if (isCurrentlyCompleted) {
            com.example.data.model.PrayerStatus.NEEDS_INPUT
        } else {
            com.example.data.model.PrayerStatus.PRAYED
        }

        val todayDate = getTodayDateString()
        if (date == todayDate && targetStatus == com.example.data.model.PrayerStatus.PRAYED) {
            val todayTimes = getTodayPrayerTimes()
            val prayer = todayTimes.find { it.name == prayerName }
            if (prayer != null && System.currentTimeMillis() < prayer.timeMillis) {
                // Do not allow marking upcoming prayer as completed
                return@withLock false
            }
        }

        val wasQadaAdded = existing.isQadaAdded(prayerName)
        val isCompleted = targetStatus == com.example.data.model.PrayerStatus.PRAYED
        val isMissed = targetStatus == com.example.data.model.PrayerStatus.MISSED
        val now = System.currentTimeMillis()

        val updated = when (prayerName) {
            PrayerName.FAJR -> existing.copy(
                fajrCompleted = isCompleted,
                fajrMissed = isMissed,
                fajrQadaAdded = if (isCompleted) false else existing.fajrQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.DHUHR -> existing.copy(
                dhuhrCompleted = isCompleted,
                dhuhrMissed = isMissed,
                dhuhrQadaAdded = if (isCompleted) false else existing.dhuhrQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.ASR -> existing.copy(
                asrCompleted = isCompleted,
                asrMissed = isMissed,
                asrQadaAdded = if (isCompleted) false else existing.asrQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.MAGHRIB -> existing.copy(
                maghribCompleted = isCompleted,
                maghribMissed = isMissed,
                maghribQadaAdded = if (isCompleted) false else existing.maghribQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.ISHA -> existing.copy(
                ishaCompleted = isCompleted,
                ishaMissed = isMissed,
                ishaQadaAdded = if (isCompleted) false else existing.ishaQadaAdded,
                updatedAt = now,
                isDeleted = false
            )
            PrayerName.SUNRISE -> existing
        }
        db.prayerLogDao().insertOrUpdatePrayerLog(updated)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyPrayerLogChanged(updated)

        if (isCompleted && wasQadaAdded) {
            val currentCount = getQadaCount(prayerName)
            if (currentCount > 0) {
                setQadaCount(prayerName, currentCount - 1)
            }
        }
        return@withLock true
    }

    // Dhikr History
    val dhikrHistory: Flow<List<DhikrHistoryEntity>> = db.dhikrHistoryDao().getAllDhikrHistory()

    suspend fun recordDhikrCompletion(dhikrPreset: DhikrPreset, count: Int, target: Int) {
        val entity = DhikrHistoryEntity(
            dhikrName = dhikrPreset.nameEnglish,
            arabicText = dhikrPreset.nameArabic,
            countCompleted = count,
            target = target,
            syncId = java.util.UUID.randomUUID().toString()
        )
        db.dhikrHistoryDao().insertDhikrHistory(entity)
        if (syncManager?.isSyncingFromRemote?.get() != true) syncManager?.notifyDhikrHistoryAdded(entity)
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
        val now = System.currentTimeMillis()
        if (currentlyBookmarked) {
            db.bookmarkDao().markBookmarkDeleted(surahNumber, verseNumber, now)
            val deleted = db.bookmarkDao().getBookmarkByVerse(surahNumber, verseNumber)
            if (deleted != null && syncManager?.isSyncingFromRemote?.get() != true) {
                syncManager?.notifyBookmarkChanged(deleted)
            }
        } else {
            val existing = db.bookmarkDao().getBookmarkByVerse(surahNumber, verseNumber)
            val bookmark = BookmarkEntity(
                id = existing?.id ?: 0,
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                surahNameEnglish = surahNameEn,
                surahNameArabic = surahNameAr,
                verseTextArabic = textAr,
                verseTextTranslation = textEn,
                timestamp = now,
                updatedAt = now,
                isDeleted = false
            )
            db.bookmarkDao().addBookmark(bookmark)
            if (syncManager?.isSyncingFromRemote?.get() != true) {
                syncManager?.notifyBookmarkChanged(bookmark)
            }
        }
    }

    // Dua Library DAO Integration (One-to-Many Collections)
    val allCategoriesWithDuas: Flow<List<DuaCategoryWithDuas>> = db.duaDao().getCategoriesWithDuas()
    val allDuasFlow: Flow<List<DuaEntity>> = db.duaDao().getAllDuas()
    val allCategoriesFlow: Flow<List<DuaCategoryEntity>> = db.duaDao().getAllCategories()

    fun getDuasForCategory(categoryIdOrTitle: String): Flow<List<DuaEntity>> {
        return db.duaDao().getDuasByCategory(categoryIdOrTitle)
    }

    fun getCategoryWithDuas(categoryIdOrTitle: String): Flow<DuaCategoryWithDuas?> {
        return db.duaDao().getCategoryWithDuas(categoryIdOrTitle)
    }

    suspend fun getDuasForCategoryDirect(categoryIdOrTitle: String): List<DuaEntity> {
        return db.duaDao().getDuasByCategoryDirect(categoryIdOrTitle)
    }

    suspend fun ensureDuaDataPopulated(
        categories: List<DuaCategoryEntity>,
        duas: List<DuaEntity>
    ) {
        if (db.duaDao().getCategoryCount() == 0 || db.duaDao().getDuaCount() == 0) {
            db.duaDao().insertCategories(categories)
            db.duaDao().insertDuas(duas)
        }
    }

    // =========================================================================
    // SYNC MANAGER SUPPORT METHODS (LOCAL PERSISTENCE LAYER FOR CLOUD SYNC)
    // =========================================================================

    fun getPreferencesTimestamp(): Long = prefs?.getLong("preferences_updated_at", 0L) ?: 0L
    fun getQuranProgressTimestamp(): Long = prefs?.getLong("quran_progress_updated_at", 0L) ?: 0L
    fun getTasbeehStateTimestamp(): Long = prefs?.getLong("tasbeeh_updated_at", 0L) ?: 0L

    suspend fun getAllRawPrayerLogsDirect(): List<PrayerLogEntity> = db.prayerLogDao().getAllRawPrayerLogsDirect()
    suspend fun getRawPrayerLogForDateDirect(date: String): PrayerLogEntity? = db.prayerLogDao().getRawPrayerLogForDateDirect(date)

    suspend fun applyPrayerLogFromRemote(log: PrayerLogEntity) {
        db.prayerLogDao().insertOrUpdatePrayerLog(log)
    }

    suspend fun getAllRawBookmarksDirect(): List<BookmarkEntity> = db.bookmarkDao().getAllRawBookmarksDirect()
    suspend fun getRawBookmarkDirect(surah: Int, verse: Int): BookmarkEntity? = db.bookmarkDao().getBookmarkByVerse(surah, verse)

    suspend fun applyBookmarkFromRemote(bookmark: BookmarkEntity) {
        val existing = db.bookmarkDao().getBookmarkByVerse(bookmark.surahNumber, bookmark.verseNumber)
        val entity = bookmark.copy(id = existing?.id ?: 0)
        db.bookmarkDao().addBookmark(entity)
    }

    suspend fun getAllDhikrHistoryDirect(): List<DhikrHistoryEntity> = db.dhikrHistoryDao().getAllDhikrHistoryDirect()
    suspend fun getDhikrHistoryBySyncId(syncId: String): DhikrHistoryEntity? = db.dhikrHistoryDao().getDhikrHistoryBySyncId(syncId)

    suspend fun applyDhikrHistoryFromRemote(entry: DhikrHistoryEntity) {
        val existing = db.dhikrHistoryDao().getDhikrHistoryBySyncId(entry.syncId)
        if (existing == null) {
            db.dhikrHistoryDao().insertDhikrHistory(entry.copy(id = 0))
        }
    }

    fun applyQadaFromRemote(prayerName: PrayerName, count: Int, everAdded: Boolean, timestamp: Long) {
        val editor = prefs?.edit()
        editor?.putInt("qada_${prayerName.name}", count)
        editor?.putBoolean("qada_ever_added_${prayerName.name}", everAdded)
        editor?.putLong("qada_timestamp_${prayerName.name}", timestamp)
        editor?.apply()
    }

    fun applyQuranProgressFromRemote(
        lastRead: com.example.data.model.QuranLastRead?,
        recentlyRead: List<com.example.data.model.QuranLastRead>,
        dailyGoal: Int,
        timestamp: Long
    ) {
        _lastReadPosition.value = lastRead
        _recentlyReadList.value = recentlyRead
        saveRecentlyReadToPrefs(recentlyRead)
        val editor = prefs?.edit()
        if (lastRead != null) {
            editor?.putInt("last_read_surah", lastRead.surahNumber)
                ?.putString("last_read_surah_en", lastRead.surahNameEnglish)
                ?.putString("last_read_surah_ar", lastRead.surahNameArabic)
                ?.putInt("last_read_verse", lastRead.verseNumber)
                ?.putInt("last_read_verse_index", lastRead.verseIndex)
                ?.putLong("last_read_timestamp", lastRead.timestamp)
        }
        editor?.putInt("daily_quran_goal", dailyGoal)
        editor?.putLong("quran_progress_updated_at", timestamp)
        editor?.apply()
    }

    fun applyTasbeehStateFromRemote(
        customPresets: List<DhikrPreset>,
        customTargets: List<Int>,
        activeCounts: Map<String, Int>,
        activeTargets: Map<String, Int>,
        timestamp: Long
    ) {
        // Merge custom presets (union by ID)
        val currentPresets = _customDhikrs.value.associateBy { it.id }.toMutableMap()
        for (p in customPresets) {
            currentPresets[p.id] = p
        }
        val mergedPresets = currentPresets.values.toList()
        _customDhikrs.value = mergedPresets
        saveCustomDhikrsToPrefs(mergedPresets)

        // Merge custom targets (union)
        val mergedTargets = (_customTargets.value + customTargets).distinct().filter { !BUILT_IN_TARGETS.contains(it) }
        _customTargets.value = mergedTargets
        saveCustomTargetsToPrefs(mergedTargets)

        // Apply counts and targets
        val editor = prefs?.edit()
        for ((k, v) in activeCounts) {
            editor?.putInt("dhikr_count_$k", v)
        }
        for ((k, v) in activeTargets) {
            editor?.putInt("dhikr_target_$k", v)
        }
        editor?.putLong("tasbeeh_updated_at", timestamp)
        editor?.apply()
    }

    fun applyPreferencesFromRemote(map: Map<String, Any?>, timestamp: Long) {
        val editor = prefs?.edit()

        (map["calcMethod"] as? String)?.let { name ->
            try {
                val method = CalcMethod.valueOf(name)
                _calcMethod.value = method
                editor?.putString("calc_method", name)
                islamicDateRepository.updateCalcMethod(method)
            } catch (_: Exception) {}
        }

        (map["madhab"] as? String)?.let { name ->
            try {
                val m = Madhab.valueOf(name)
                _madhab.value = m
                editor?.putString("madhab", name)
            } catch (_: Exception) {}
        }

        (map["appearanceMode"] as? String)?.let { name ->
            try {
                val mode = AppearanceMode.valueOf(name)
                _appearanceMode.value = mode
                editor?.putString("appearance_mode", name)
            } catch (_: Exception) {}
        }

        (map["timeFormat"] as? String)?.let { name ->
            try {
                val fmt = TimeFormat.valueOf(name)
                _timeFormat.value = fmt
                editor?.putString("time_format", name)
            } catch (_: Exception) {}
        }

        (map["hijriDateMethod"] as? String)?.let { name ->
            try {
                val hMethod = com.example.data.model.HijriDateMethod.valueOf(name)
                _hijriDateMethod.value = hMethod
                editor?.putString("hijri_date_method", name)
                islamicDateRepository.updateHijriMethod(hMethod)
            } catch (_: Exception) {}
        }

        (map["customHijriOffset"] as? Number)?.let { offset ->
            _customHijriOffset.value = offset.toInt()
            editor?.putInt("custom_hijri_offset", offset.toInt())
            islamicDateRepository.updateCustomOffset(offset.toInt())
        }

        (map["tasbeehSound"] as? String)?.let { soundId ->
            com.example.data.model.TasbeehSound.values().find { it.id == soundId }?.let { sound ->
                _tasbeehSound.value = sound
                editor?.putString("tasbeeh_sound", soundId)
            }
        }

        (map["vibrationEnabled"] as? Boolean)?.let { vib ->
            _vibrationEnabled.value = vib
            editor?.putBoolean("vibration_enabled", vib)
        }

        (map["homeFeatureOrder"] as? String)?.let { orderStr ->
            if (orderStr.isNotBlank()) {
                val order = orderStr.split(",")
                _homeFeaturesPreferences.value = _homeFeaturesPreferences.value.copy(featureOrder = order)
                editor?.putString("home_feature_order", orderStr)
            }
        }

        (map["selectedCity"] as? String)?.let { cityName ->
            if (cityName.isNotBlank()) {
                PREDEFINED_CITIES.find { it.cityName.equals(cityName, ignoreCase = true) }?.let { city ->
                    _selectedCity.value = city
                    editor?.putString("selected_city_name", city.cityName)
                    islamicDateRepository.updateLocation(city)
                }
            }
        }

        (map["bookmarkedDuaIds"] as? String)?.let { jsonStr ->
            try {
                val arr = org.json.JSONArray(jsonStr)
                val set = mutableSetOf<String>()
                for (i in 0 until arr.length()) set.add(arr.getString(i))
                appContext?.getSharedPreferences("dua_bookmarks_prefs", android.content.Context.MODE_PRIVATE)
                    ?.edit()?.putStringSet("bookmarked_dua_ids", set)?.apply()
            } catch (_: Exception) {}
        }

        // Apply notification settings via SmartPrayerNotificationManager
        appContext?.let { ctx ->
            try {
                val smart = com.example.data.reminder.SmartPrayerNotificationManager(ctx)
                (map["notificationsSmartEnabled"] as? Boolean)?.let { smart.isSmartNotificationsEnabled = it }
                (map["notificationsPrayerTimeEnabled"] as? Boolean)?.let { smart.isPrayerTimeNotificationsEnabled = it }
                (map["notificationsPreReminderMins"] as? Number)?.let { mins ->
                    com.example.data.reminder.PrePrayerReminderOffset.values().find { it.minutes == mins.toInt() }?.let {
                        smart.preReminderOffset = it
                    }
                }
                (map["notificationsContextualEnabled"] as? Boolean)?.let { smart.isContextualRemindersEnabled = it }
                (map["notificationsNaflEnabled"] as? Boolean)?.let { smart.isNaflOpportunitiesEnabled = it }
                (map["notificationsPrayerTogglesJson"] as? String)?.let { jsonStr ->
                    try {
                        val obj = org.json.JSONObject(jsonStr)
                        for (key in listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")) {
                            if (obj.has(key)) {
                                smart.setPrayerEnabled(key, obj.getBoolean(key))
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        editor?.putLong("preferences_updated_at", timestamp)
        editor?.apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = AppRepository(db, context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
