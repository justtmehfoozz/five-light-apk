package com.example.ui.viewmodel

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.isActive
import com.example.data.audio.QuranAudioRepository
import com.example.data.audio.SurahDownloadStatus
import com.example.data.util.NetworkUtils
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.DhikrHistoryEntity
import com.example.data.db.DuaCategoryEntity
import com.example.data.db.DuaCategoryWithDuas
import com.example.data.db.DuaEntity
import com.example.data.db.PrayerLogEntity
import com.example.data.util.DuaData
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.DhikrPreset
import com.example.data.model.HijriDate
import com.example.data.model.Madhab
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.Surah
import com.example.data.model.TimeFormat
import com.example.data.model.Verse
import com.example.data.repository.AppRepository
import com.example.data.util.HijriCalc
import com.example.data.util.QiblaCalc
import com.example.data.util.QuranData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(db, application)

    // Initialization State
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // User Preferences
    val selectedCity: StateFlow<CityLocation> = repository.selectedCity
    val calcMethod: StateFlow<CalcMethod> = repository.calcMethod
    val madhab: StateFlow<Madhab> = repository.madhab
    val appearanceMode: StateFlow<AppearanceMode> = repository.appearanceMode
    val timeFormat: StateFlow<TimeFormat> = repository.timeFormat
    val hijriDateMethod: StateFlow<com.example.data.model.HijriDateMethod> = repository.hijriDateMethod
    val customHijriOffset: StateFlow<Int> = repository.customHijriOffset
    val tasbeehSound: StateFlow<com.example.data.model.TasbeehSound> = repository.tasbeehSound
    val vibrationEnabled: StateFlow<Boolean> = repository.vibrationEnabled
    val naflPreferences: StateFlow<com.example.data.model.NaflPreferences> = repository.naflPreferences
    val homeFeaturesPreferences: StateFlow<com.example.data.model.HomeFeaturesPreferences> = repository.homeFeaturesPreferences
    val lastReadPosition: StateFlow<com.example.data.model.QuranLastRead?> = repository.lastReadPosition
    val recentlyReadList: StateFlow<List<com.example.data.model.QuranLastRead>> = repository.recentlyReadList

    val islamicDateRepository: com.example.data.repository.IslamicDateRepository = repository.islamicDateRepository
    val islamicDateState: StateFlow<com.example.data.model.IslamicDateState> = islamicDateRepository.islamicDateState

    // Hijri Calendar (single source of truth mapped from IslamicDateRepository)
    val hijriDate: StateFlow<HijriDate> = islamicDateState
        .map { it.hijriDate }
        .stateIn(viewModelScope, SharingStarted.Eagerly, islamicDateRepository.getCurrentHijriDate())

    // Prayer Mode State
    private val _showPrayerMode = MutableStateFlow<PrayerItem?>(null)
    val showPrayerMode: StateFlow<PrayerItem?> = _showPrayerMode.asStateFlow()

    fun openPrayerMode(prayer: PrayerItem) { _showPrayerMode.value = prayer }
    fun closePrayerMode() { _showPrayerMode.value = null }

    // Weekly Dates & Logs
    private fun calculateCurrentWeekDates(): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY)
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        val dates = mutableListOf<String>()
        for (i in 0 until 7) {
            dates.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dates
    }

    val currentWeekDates: List<String> = calculateCurrentWeekDates()

    val weeklyPrayerLogs: StateFlow<Map<String, PrayerLogEntity>> = repository.getPrayerLogsForDates(currentWeekDates)
        .map { list -> list.associateBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allPrayerLogs: StateFlow<List<PrayerLogEntity>> = repository.getAllPrayerLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _qadaCounts = MutableStateFlow<Map<PrayerName, Int>>(emptyMap())
    val qadaCounts: StateFlow<Map<PrayerName, Int>> = _qadaCounts

    private val _qadaEverAdded = MutableStateFlow<Map<PrayerName, Boolean>>(emptyMap())
    val qadaEverAdded: StateFlow<Map<PrayerName, Boolean>> = _qadaEverAdded

    private val _dailyQuranGoal = MutableStateFlow(0)
    val dailyQuranGoal: StateFlow<Int> = _dailyQuranGoal
    
    fun refreshQadaCounts() {
        val countMap = mutableMapOf<PrayerName, Int>()
        val everMap = mutableMapOf<PrayerName, Boolean>()
        listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA).forEach {
            val count = repository.getQadaCount(it)
            countMap[it] = count
            val hadEver = repository.hasEverHadQada(it) || count > 0
            everMap[it] = hadEver
        }
        _qadaCounts.value = countMap
        _qadaEverAdded.value = everMap
    }
    
    fun updateQadaCount(prayerName: PrayerName, count: Int) {
        repository.setQadaCount(prayerName, count)
        refreshQadaCounts()
    }
    
    fun setDailyQuranGoal(pages: Int) {
        repository.setDailyQuranGoal(pages)
        _dailyQuranGoal.value = repository.getDailyQuranGoal()
    }


    // Nafl & Right Now
    private val _naflPrayerItems = MutableStateFlow<List<com.example.data.model.NaflPrayerItem>>(emptyList())
    val naflPrayerItems: StateFlow<List<com.example.data.model.NaflPrayerItem>> = _naflPrayerItems.asStateFlow()

    private val _rightNowItem = MutableStateFlow<com.example.data.model.RightNowItem?>(null)
    val rightNowItem: StateFlow<com.example.data.model.RightNowItem?> = _rightNowItem.asStateFlow()

    // Prayer Times & Countdown
    private val _prayerTimes = MutableStateFlow<List<PrayerItem>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerItem>> = _prayerTimes.asStateFlow()

    private val _nextPrayer = MutableStateFlow<PrayerItem?>(null)
    val nextPrayer: StateFlow<PrayerItem?> = _nextPrayer.asStateFlow()

    private val _countdownFormatted = MutableStateFlow("00:00:00")
    val countdownFormatted: StateFlow<String> = _countdownFormatted.asStateFlow()

    // Part 3: App-Wide "Last Light" Maghrib Ambient Overlay State
    private val _maghribOverlayAlpha = MutableStateFlow(0f)
    val maghribOverlayAlpha: StateFlow<Float> = _maghribOverlayAlpha.asStateFlow()

    // Today's Prayer Log
    val todayPrayerLog: StateFlow<PrayerLogEntity?> = repository.getPrayerLogForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Centralized FiveLight Context State
    val contextState: StateFlow<com.example.data.model.FiveLightContextState> = combine(
        prayerTimes,
        timeFormat,
        hijriDate,
        weeklyPrayerLogs,
        rightNowItem
    ) { fard, tf, hijri, weeklyLogs, rightNow ->
        com.example.data.util.FiveLightContextEngine.computeContextState(
            fardPrayers = fard,
            is24Hour = tf.is24Hour,
            hijriDate = hijri,
            weeklyLogsMap = weeklyLogs,
            nowMillis = System.currentTimeMillis(),
            rightNowItem = rightNow
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.model.FiveLightContextState())

    val prayerJourneyNodes: StateFlow<List<com.example.data.model.PrayerJourneyNode>> = combine(
        prayerTimes,
        naflPrayerItems,
        todayPrayerLog,
        timeFormat
    ) { fard, nafl, log, tf ->
        com.example.data.util.FiveLightContextEngine.computePrayerJourney(
            fardPrayers = fard,
            naflPrayers = nafl,
            todayPrayerLog = log,
            is24Hour = tf.is24Hour,
            nowMillis = System.currentTimeMillis()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Date & Friday detection
    private val _currentDateString = MutableStateFlow(repository.getTodayDateString())
    val currentDateString: StateFlow<String> = _currentDateString.asStateFlow()

    val isFriday: StateFlow<Boolean> = _currentDateString.map { dateStr ->
        try {
            java.time.LocalDate.parse(dateStr).dayOfWeek == java.time.DayOfWeek.FRIDAY
        } catch (_: Exception) {
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), run {
        try {
            java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.FRIDAY
        } catch (_: Exception) {
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        }
    })

    // Qibla & Sensors
    private val _qiblaAngle = MutableStateFlow(0f)
    val qiblaAngle: StateFlow<Float> = _qiblaAngle.asStateFlow()

    private val _compassHeading = MutableStateFlow(0f)
    val compassHeading: StateFlow<Float> = _compassHeading.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(true)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private val _isAccuracyLow = MutableStateFlow(false)
    val isAccuracyLow: StateFlow<Boolean> = _isAccuracyLow.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var rotationVectorSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Quran State
    private val _surahSearchQuery = MutableStateFlow("")
    val surahSearchQuery: StateFlow<String> = _surahSearchQuery.asStateFlow()

    private val _selectedSurah = MutableStateFlow<Surah?>(QuranData.SURAHS_DIRECTORY[0])
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _surahVerses = MutableStateFlow<List<Verse>>(emptyList())
    val surahVerses: StateFlow<List<Verse>> = _surahVerses.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(24f)
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    private val _showEnglishTranslation = MutableStateFlow(true)
    val showEnglishTranslation: StateFlow<Boolean> = _showEnglishTranslation.asStateFlow()

    private val _isNightReadingMode = MutableStateFlow(false)
    val isNightReadingMode: StateFlow<Boolean> = _isNightReadingMode.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audio Playback State
    private var mediaPlayer: MediaPlayer? = null
    private val _playingSurahNumber = MutableStateFlow<Int?>(null)
    val playingSurahNumber: StateFlow<Int?> = _playingSurahNumber.asStateFlow()

    private val _playingVerseNumber = MutableStateFlow<Int?>(null)
    val playingVerseNumber: StateFlow<Int?> = _playingVerseNumber.asStateFlow()

    private val _playingVerse = MutableStateFlow<Verse?>(null)
    val playingVerse: StateFlow<Verse?> = _playingVerse.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _audioProgress = MutableStateFlow(0f)
    val audioProgress: StateFlow<Float> = _audioProgress.asStateFlow()

    private val _surahPlaybackProgress = MutableStateFlow<Map<Int, Float>>(repository.getSurahPlaybackProgressMap())
    val surahPlaybackProgress: StateFlow<Map<Int, Float>> = _surahPlaybackProgress.asStateFlow()

    // Quran Audio Download States
    private val _surahDownloadStates = MutableStateFlow<Map<Int, SurahDownloadStatus>>(emptyMap())
    val surahDownloadStates: StateFlow<Map<Int, SurahDownloadStatus>> = _surahDownloadStates.asStateFlow()

    private val _isBulkDownloadingQuran = MutableStateFlow(false)
    val isBulkDownloadingQuran: StateFlow<Boolean> = _isBulkDownloadingQuran.asStateFlow()

    private val _bulkDownloadProgress = MutableStateFlow(0f)
    val bulkDownloadProgress: StateFlow<Float> = _bulkDownloadProgress.asStateFlow()

    private val _bulkDownloadStatusText = MutableStateFlow("")
    val bulkDownloadStatusText: StateFlow<String> = _bulkDownloadStatusText.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private var bulkDownloadJob: Job? = null

    private fun updateSurahProgress(surahNumber: Int, progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _surahPlaybackProgress.value = _surahPlaybackProgress.value + (surahNumber to clamped)
        repository.setSurahPlaybackProgress(surahNumber, clamped)
    }

    private fun computeCurrentSurahProgress(surahNumber: Int, verseNumber: Int, intraVerseProgress: Float): Float {
        val totalVerses = QuranData.SURAHS_DIRECTORY.find { it.number == surahNumber }?.versesCount ?: 1
        return ((verseNumber - 1 + intraVerseProgress.coerceIn(0f, 1f)) / totalVerses.toFloat()).coerceIn(0f, 1f)
    }

    private var progressJob: Job? = null
    private var lastPreviousTapTime: Long = 0L

    // Scroll Position Memory per surah
    private val scrollPrefs = application.getSharedPreferences("quran_scroll_memory", Context.MODE_PRIVATE)

    private fun loadAllSurahScrollPositions(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val all = scrollPrefs.all
        for ((key, value) in all) {
            if (key.startsWith("surah_") && value is Int) {
                val sNum = key.removePrefix("surah_").toIntOrNull()
                if (sNum != null) {
                    result[sNum] = value
                }
            }
        }
        lastReadPosition.value?.let { lr ->
            if (!result.containsKey(lr.surahNumber)) {
                result[lr.surahNumber] = lr.verseIndex
            }
        }
        return result
    }

    private val _surahScrollPositions = MutableStateFlow<Map<Int, Int>>(loadAllSurahScrollPositions())
    val surahScrollPositions: StateFlow<Map<Int, Int>> = _surahScrollPositions.asStateFlow()

    fun saveSurahScrollPosition(surahNumber: Int, verseIndex: Int) {
        if (surahNumber > 0 && verseIndex >= 0) {
            scrollPrefs.edit().putInt("surah_$surahNumber", verseIndex).apply()
            _surahScrollPositions.value = _surahScrollPositions.value + (surahNumber to verseIndex)
            val surah = QuranData.SURAHS_DIRECTORY.find { it.number == surahNumber }
            if (surah != null) {
                val verseNum = (verseIndex + 1).coerceAtMost(surah.versesCount)
                repository.saveLastReadPosition(
                    surahNumber = surahNumber,
                    surahNameEn = surah.nameEnglish,
                    surahNameAr = surah.nameArabic,
                    verseNumber = verseNum,
                    verseIndex = verseIndex
                )
            }
        }
    }

    fun getSurahScrollPosition(surahNumber: Int): Int {
        return _surahScrollPositions.value[surahNumber] ?: scrollPrefs.getInt("surah_$surahNumber", 0)
    }

    fun toggleBookmarkForSurah(surah: Surah, isCurrentlyBookmarked: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyBookmarked) {
                val existing = bookmarks.value.filter { it.surahNumber == surah.number }
                existing.forEach { b ->
                    repository.toggleBookmark(
                        surahNumber = b.surahNumber,
                        verseNumber = b.verseNumber,
                        surahNameEn = b.surahNameEnglish,
                        surahNameAr = b.surahNameArabic,
                        textAr = b.verseTextArabic,
                        textEn = b.verseTextTranslation,
                        currentlyBookmarked = true
                    )
                }
            } else {
                val verses = QuranData.getVersesForSurah(getApplication(), surah.number)
                val firstVerse = verses.firstOrNull { it.verseNumber == 1 } ?: verses.firstOrNull()
                if (firstVerse != null) {
                    repository.toggleBookmark(
                        surahNumber = surah.number,
                        verseNumber = firstVerse.verseNumber,
                        surahNameEn = surah.nameEnglish,
                        surahNameAr = surah.nameArabic,
                        textAr = firstVerse.textArabic,
                        textEn = firstVerse.textEnglish,
                        currentlyBookmarked = false
                    )
                }
            }
        }
    }

    fun toggleVerseBookmark(verse: Verse, surah: Surah? = null) {
        viewModelScope.launch {
            val isBookmarked = bookmarks.value.any { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }
            val resolvedSurah = surah ?: QuranData.SURAHS_DIRECTORY.find { it.number == verse.surahNumber }
            val nameEn = resolvedSurah?.nameEnglish ?: "Surah ${verse.surahNumber}"
            val nameAr = resolvedSurah?.nameArabic ?: ""
            repository.toggleBookmark(
                surahNumber = verse.surahNumber,
                verseNumber = verse.verseNumber,
                surahNameEn = nameEn,
                surahNameAr = nameAr,
                textAr = verse.textArabic,
                textEn = verse.textEnglish,
                currentlyBookmarked = isBookmarked
            )
        }
    }

    fun playSurah(surah: Surah) {
        selectSurah(surah)
        viewModelScope.launch(Dispatchers.IO) {
            val verses = QuranData.getVersesForSurah(getApplication(), surah.number)
            val firstVerseNum = verses.minOfOrNull { it.verseNumber } ?: 1
            withContext(Dispatchers.Main) {
                playVerseByNumber(surah.number, firstVerseNum)
            }
        }
    }

    fun getQuranLensInfo(context: Context, surahNumber: Int, verseNumber: Int): com.example.data.model.QuranLensInfo {
        return QuranData.getQuranLensInfoForVerse(context, surahNumber, verseNumber)
    }

    fun getVerseByKey(verseKey: String): Verse? {
        return QuranData.getVerseByKey(getApplication(), verseKey)
    }

    fun getTranslationByKey(verseKey: String): String? {
        return QuranData.getTranslationByKey(getApplication(), verseKey)
    }

    fun setHomeFeaturesPreference(
        continueReading: Boolean = homeFeaturesPreferences.value.continueReadingEnabled,
        rightNow: Boolean = homeFeaturesPreferences.value.rightNowEnabled,
        tonight: Boolean = homeFeaturesPreferences.value.tonightEnabled,
        nextOpportunity: Boolean = homeFeaturesPreferences.value.nextOpportunityEnabled,
        prayerPrep: Boolean = homeFeaturesPreferences.value.prayerPrepEnabled,
        weeklyOverview: Boolean = homeFeaturesPreferences.value.weeklyOverviewEnabled,
        moments: Boolean = homeFeaturesPreferences.value.momentsEnabled,
        quietMode: Boolean = homeFeaturesPreferences.value.quietModeEnabled,
        prayerJourney: Boolean = homeFeaturesPreferences.value.prayerJourneyEnabled,
        recentlyRead: Boolean = homeFeaturesPreferences.value.recentlyReadEnabled,
        quranLens: Boolean = homeFeaturesPreferences.value.quranLensEnabled,
        nightIsComing: Boolean = homeFeaturesPreferences.value.nightIsComingEnabled
    ) {
        repository.setHomeFeaturesPreference(
            continueReading = continueReading,
            rightNow = rightNow,
            tonight = tonight,
            nextOpportunity = nextOpportunity,
            prayerPrep = prayerPrep,
            weeklyOverview = weeklyOverview,
            moments = moments,
            quietMode = quietMode,
            prayerJourney = prayerJourney,
            recentlyRead = recentlyRead,
            quranLens = quranLens,
            nightIsComing = nightIsComing
        )
    }

    fun setHomeFeatureOrder(order: List<String>) {
        repository.setHomeFeatureOrder(order)
    }

    fun resetHomeFeatureOrder() {
        repository.resetHomeFeatureOrder()
    }

    // Tasbeeh State
    val customDhikrs: StateFlow<List<DhikrPreset>> = repository.customDhikrs
    val allDhikrs: StateFlow<List<DhikrPreset>> = kotlinx.coroutines.flow.combine(
        MutableStateFlow(repository.DHIKR_PRESETS),
        customDhikrs
    ) { presets, custom ->
        presets + custom
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.DHIKR_PRESETS)

    val customTargets: StateFlow<List<Int>> = repository.customTargets
    val allTargets: StateFlow<List<Int>> = kotlinx.coroutines.flow.combine(
        MutableStateFlow(repository.BUILT_IN_TARGETS),
        customTargets
    ) { builtIn, custom ->
        val result = builtIn.toMutableList()
        custom.forEach { c ->
            if (!result.contains(c)) {
                result.add(c)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.BUILT_IN_TARGETS)

    private val _selectedDhikr = MutableStateFlow(repository.DHIKR_PRESETS[0])
    val selectedDhikr: StateFlow<DhikrPreset> = _selectedDhikr.asStateFlow()

    private val _dhikrCount = MutableStateFlow(repository.getSavedDhikrCount(repository.DHIKR_PRESETS[0].id))
    val dhikrCount: StateFlow<Int> = _dhikrCount.asStateFlow()

    private val _dhikrTarget = MutableStateFlow(repository.getSavedDhikrTarget(repository.DHIKR_PRESETS[0].id, repository.DHIKR_PRESETS[0].defaultTarget))
    val dhikrTarget: StateFlow<Int> = _dhikrTarget.asStateFlow()

    val dhikrHistory: StateFlow<List<DhikrHistoryEntity>> = repository.dhikrHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dua Library (One-to-Many Categories and Duas collections via Room DAO)
    val duaCategoriesWithDuas: StateFlow<List<DuaCategoryWithDuas>> = repository.allCategoriesWithDuas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDuasFromDb: StateFlow<List<DuaEntity>> = repository.allDuasFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getDuasForCategory(categoryTitleOrId: String): Flow<List<DuaEntity>> {
        return repository.getDuasForCategory(categoryTitleOrId)
    }

    fun getCategoryWithDuas(categoryTitleOrId: String): Flow<DuaCategoryWithDuas?> {
        return repository.getCategoryWithDuas(categoryTitleOrId)
    }

    private var tickerJob: Job? = null

    init {
        refreshPrayerTimes()
        startCountdownTicker()
        initSensors()
        refreshQadaCounts()
        _dailyQuranGoal.value = repository.getDailyQuranGoal()

        loadSurahVerses(QuranData.SURAHS_DIRECTORY[0])
        refreshDownloadStates()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val catEntities = DuaData.CATEGORIES.mapIndexed { index, cat ->
                    DuaCategoryEntity(
                        id = cat.title,
                        title = cat.title,
                        description = cat.description,
                        iconName = "",
                        displayOrder = index
                    )
                }
                val duaEntities = DuaData.ALL_DUAS.mapIndexed { index, dua ->
                    DuaEntity(
                        id = dua.id.ifEmpty { "dua_$index" },
                        categoryId = dua.category,
                        categoryTitle = dua.category,
                        title = dua.title,
                        arabic = dua.arabic,
                        transliteration = dua.transliteration,
                        translation = dua.translation,
                        reference = dua.reference,
                        displayOrder = index
                    )
                }
                repository.ensureDuaDataPopulated(catEntities, duaEntities)
            } finally {
                _isInitialized.value = true
            }
        }
    }


    fun setCity(city: CityLocation) {
        repository.setCity(city)
        refreshPrayerTimes()
    }

    fun setCalcMethod(method: CalcMethod) {
        repository.setCalcMethod(method)
        refreshPrayerTimes()
    }

    fun setMadhab(m: Madhab) {
        repository.setMadhab(m)
        refreshPrayerTimes()
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        repository.setAppearanceMode(mode)
    }

    fun setTimeFormat(format: TimeFormat) {
        repository.setTimeFormat(format)
        refreshPrayerTimes()
    }

    fun setTasbeehSound(sound: com.example.data.model.TasbeehSound) {
        repository.setTasbeehSound(sound)
    }

    fun setNaflPreference(
        tahajjud: Boolean = naflPreferences.value.tahajjudEnabled,
        ishraq: Boolean = naflPreferences.value.ishraqEnabled,
        duha: Boolean = naflPreferences.value.duhaEnabled,
        awwabin: Boolean = naflPreferences.value.awwabinEnabled
    ) {
        repository.setNaflPreference(tahajjud, ishraq, duha, awwabin)
        refreshPrayerTimes()
    }

    fun setNaflOrder(order: List<String>) {
        repository.setNaflOrder(order)
        refreshPrayerTimes()
    }

    fun resetNaflOrder() {
        repository.resetNaflOrder()
        refreshPrayerTimes()
    }

    fun refreshPrayerTimes() {
        val times = repository.getTodayPrayerTimes()
        _prayerTimes.value = times

        val next = times.find { it.isNext } ?: times.find { it.name == PrayerName.FAJR }?.let {
            it.copy(isNext = true, timeMillis = it.timeMillis + (24 * 3600 * 1000L))
        }
        _nextPrayer.value = next

        refreshHijriDate()

        // Calculate Qibla angle for current city
        val city = selectedCity.value
        _qiblaAngle.value = QiblaCalc.calculateQiblaDirection(city.latitude, city.longitude)

        // Calculate Nafl prayer times & Right Now item
        val is24H = timeFormat.value.is24Hour
        _naflPrayerItems.value = com.example.data.util.NaflCalc.calculateNaflTimes(
            fardPrayers = times,
            preferences = repository.naflPreferences.value,
            is24Hour = is24H
        )
        _rightNowItem.value = com.example.data.util.NaflCalc.calculateRightNowItem(
            fardPrayers = times,
            is24Hour = is24H
        )

        // Reschedule reminders for updated prayer calculation
        try {
            val smartManager = com.example.data.reminder.SmartPrayerNotificationManager(getApplication())
            smartManager.scheduleSmartNotifications(times, _naflPrayerItems.value)
        } catch (e: Exception) {
            // Ignore if context/alarm exception
        }
    }

    fun setHijriDateMethod(method: com.example.data.model.HijriDateMethod) {
        repository.setHijriDateMethod(method)
    }

    fun setCustomHijriOffset(offset: Int) {
        repository.setCustomHijriOffset(offset)
    }

    fun onAppResume() {
        refreshPrayerTimes()
        islamicDateRepository.onAppResume()
    }

    fun refreshHijriDate() {
        islamicDateRepository.refresh()
    }

    fun getCurrentGregorianDate() = islamicDateRepository.getCurrentGregorianDate()
    fun getCurrentHijriDate() = islamicDateRepository.getCurrentHijriDate()
    fun getCurrentSunset() = islamicDateRepository.getCurrentSunset()
    fun isAfterMaghrib() = islamicDateRepository.isAfterMaghrib()
    fun getCurrentIslamicDateState() = islamicDateRepository.getCurrentIslamicDateState()

    private fun startCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                // Check if calendar date changed (e.g. midnight rollover)
                val todayDate = repository.getTodayDateString()
                if (todayDate != _currentDateString.value) {
                    _currentDateString.value = todayDate
                    refreshPrayerTimes()
                }

                var next = _nextPrayer.value
                if (next != null) {
                    val now = System.currentTimeMillis()
                    var diffMillis = next.timeMillis - now
                    if (diffMillis < 0) {
                        refreshPrayerTimes()
                        next = _nextPrayer.value
                        diffMillis = if (next != null) {
                            (next.timeMillis - System.currentTimeMillis()).coerceAtLeast(0)
                        } else 0
                    }

                    val totalSeconds = diffMillis / 1000
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60
                    _countdownFormatted.value = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                }

                delay(1000)
            }
        }
    }

    fun togglePrayerCompleted(prayerName: PrayerName) {
        viewModelScope.launch {
            repository.togglePrayerCompleted(prayerName, todayPrayerLog.value)
        }
    }

    fun setPrayerStatus(
        prayerName: PrayerName,
        dateString: String = repository.getTodayDateString(),
        status: com.example.data.model.PrayerStatus
    ) {
        viewModelScope.launch {
            repository.setPrayerStatus(prayerName, dateString, status)
            refreshQadaCounts()
        }
    }

    fun makeUpQadaPrayer(prayerName: PrayerName) {
        viewModelScope.launch {
            repository.makeUpQadaPrayer(prayerName)
            refreshQadaCounts()
        }
    }

    fun savePrayerNote(
        prayerName: PrayerName,
        dateString: String = repository.getTodayDateString(),
        note: String?
    ) {
        viewModelScope.launch {
            repository.savePrayerNote(prayerName, dateString, note)
        }
    }

    fun addPrayerToQada(
        prayerName: PrayerName,
        dateString: String = repository.getTodayDateString()
    ) {
        viewModelScope.launch {
            val currentCount = repository.getQadaCount(prayerName)
            updateQadaCount(prayerName, currentCount + 1)
            repository.setPrayerQadaAdded(prayerName, dateString, true)
        }
    }

    // Compass Sensor Management
    private fun initSensors() {
        try {
            sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            val hasSensors = rotationVectorSensor != null || (accelerometer != null && magnetometer != null)
            _isSensorAvailable.value = hasSensors
        } catch (_: Exception) {
            _isSensorAvailable.value = false
        }
    }

    private var smoothedHeading = -1f

    fun startCompassListening() {
        smoothedHeading = -1f
        if (_isSensorAvailable.value) {
            if (rotationVectorSensor != null) {
                sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
            } else {
                sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    fun stopCompassListening() {
        sensorManager?.unregisterListener(this)
    }

    private fun updateHeading(rawAzimuthDeg: Float) {
        if (smoothedHeading < 0f) {
            smoothedHeading = rawAzimuthDeg
            _compassHeading.value = smoothedHeading
            return
        }

        // Shortest angular difference between raw azimuth and current smoothed heading:
        val shortestDiff = ((rawAzimuthDeg - smoothedHeading + 540f) % 360f) - 180f
        val absDiff = kotlin.math.abs(shortestDiff)

        // Dynamic low-latency filter: instant reaction during motion, calm stability on rest
        val alpha = when {
            absDiff > 12f -> 0.85f  // Large fast turn -> immediate follow-through
            absDiff > 4f  -> 0.65f  // Moderate rotation -> responsive tracking
            absDiff > 1f  -> 0.40f  // Smooth gentle turn
            absDiff > 0.2f -> 0.22f // Micro alignment
            else -> 0f              // Sub-0.2 degree hardware noise threshold
        }

        if (alpha > 0f) {
            smoothedHeading = (smoothedHeading + alpha * shortestDiff + 360f) % 360f
            _compassHeading.value = smoothedHeading
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val r = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(r, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            var rawAzimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (rawAzimuthDeg < 0) rawAzimuthDeg += 360f
            updateHeading(rawAzimuthDeg)
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                var rawAzimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (rawAzimuthDeg < 0) rawAzimuthDeg += 360f
                updateHeading(rawAzimuthDeg)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR || sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            _isAccuracyLow.value = (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        }
    }

    // Quran Methods
    fun setSurahSearchQuery(query: String) {
        _surahSearchQuery.value = query
    }

    fun selectSurah(surah: Surah) {
        if (_selectedSurah.value?.number != surah.number) {
            _surahVerses.value = emptyList()
        }
        _selectedSurah.value = surah
        loadSurahVerses(surah)
    }

    private fun loadSurahVerses(surah: Surah) {
        viewModelScope.launch(Dispatchers.IO) {
            val verses = QuranData.getVersesForSurah(getApplication(), surah.number)
            withContext(Dispatchers.Main) {
                _surahVerses.value = verses
            }
        }
    }

    fun setFontSize(sp: Float) {
        _fontSizeSp.value = sp
    }

    fun toggleEnglishTranslation() {
        _showEnglishTranslation.value = !_showEnglishTranslation.value
    }

    fun toggleNightReadingMode() {
        _isNightReadingMode.value = !_isNightReadingMode.value
    }

    fun toggleBookmarkVerse(verse: Verse, isBookmarked: Boolean) {
        val surah = selectedSurah.value ?: return
        viewModelScope.launch {
            repository.toggleBookmark(
                surahNumber = verse.surahNumber,
                verseNumber = verse.verseNumber,
                surahNameEn = surah.nameEnglish,
                surahNameAr = surah.nameArabic,
                textAr = verse.textArabic,
                textEn = verse.textEnglish,
                currentlyBookmarked = isBookmarked
            )
        }
    }

    // Audio Player & Offline Download Engine
    fun refreshDownloadStates() {
        viewModelScope.launch(Dispatchers.IO) {
            val appCtx = getApplication<Application>()
            val newMap = mutableMapOf<Int, SurahDownloadStatus>()
            QuranData.SURAHS_DIRECTORY.forEach { surah ->
                val totalVerses = surah.versesCount
                val cachedCount = QuranAudioRepository.getSurahCachedVerseCount(appCtx, surah.number, totalVerses)
                val status = when {
                    cachedCount == totalVerses && totalVerses > 0 -> {
                        val size = QuranAudioRepository.getSurahDownloadedSizeBytes(appCtx, surah.number)
                        SurahDownloadStatus.Downloaded(size)
                    }
                    cachedCount > 0 -> {
                        SurahDownloadStatus.Downloading(cachedCount.toFloat() / totalVerses.toFloat(), cachedCount, totalVerses)
                    }
                    else -> SurahDownloadStatus.NotDownloaded
                }
                newMap[surah.number] = status
            }
            _surahDownloadStates.value = newMap
        }
    }

    fun downloadSurahAudio(surahNumber: Int) {
        val surah = QuranData.SURAHS_DIRECTORY.find { it.number == surahNumber } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val appCtx = getApplication<Application>()
            if (!NetworkUtils.isNetworkAvailable(appCtx)) {
                withContext(Dispatchers.Main) {
                    val msg = "Internet connection required to download Surah ${surah.nameEnglish}."
                    _playbackError.value = msg
                    android.widget.Toast.makeText(appCtx, msg, android.widget.Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            _surahDownloadStates.value = _surahDownloadStates.value + (surahNumber to SurahDownloadStatus.Downloading(0f, 0, surah.versesCount))
            val success = QuranAudioRepository.downloadSurah(
                context = appCtx,
                surahNumber = surahNumber,
                versesCount = surah.versesCount,
                onProgress = { downloadedVerses, totalVerses, progressFraction ->
                    _surahDownloadStates.value = _surahDownloadStates.value + (surahNumber to SurahDownloadStatus.Downloading(progressFraction, downloadedVerses, totalVerses))
                }
            )
            if (success) {
                val size = QuranAudioRepository.getSurahDownloadedSizeBytes(appCtx, surahNumber)
                _surahDownloadStates.value = _surahDownloadStates.value + (surahNumber to SurahDownloadStatus.Downloaded(size))
                repository.recordDownloadedAudio(surahNumber = surahNumber, status = "DOWNLOADED", totalVerses = surah.versesCount, downloadedVerses = surah.versesCount, sizeBytes = size)
            } else {
                _surahDownloadStates.value = _surahDownloadStates.value + (surahNumber to SurahDownloadStatus.Error("Download failed."))
            }
        }
    }

    fun deleteDownloadedSurahAudio(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val appCtx = getApplication<Application>()
            QuranAudioRepository.deleteDownloadedSurah(appCtx, surahNumber)
            repository.removeDownloadedAudioRecord(surahNumber)
            _surahDownloadStates.value = _surahDownloadStates.value + (surahNumber to SurahDownloadStatus.NotDownloaded)
        }
    }

    fun downloadAll114Surahs() {
        if (_isBulkDownloadingQuran.value) return
        bulkDownloadJob = viewModelScope.launch(Dispatchers.IO) {
            val appCtx = getApplication<Application>()
            if (!NetworkUtils.isNetworkAvailable(appCtx)) {
                withContext(Dispatchers.Main) {
                    val msg = "Internet connection required for complete Qur'an download."
                    _playbackError.value = msg
                    android.widget.Toast.makeText(appCtx, msg, android.widget.Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            _isBulkDownloadingQuran.value = true
            _bulkDownloadProgress.value = 0f
            _bulkDownloadStatusText.value = "Preparing Qur'an recitation download..."

            val allSurahs = QuranData.SURAHS_DIRECTORY
            val totalSurahs = allSurahs.size
            var completedCount = 0

            for (surah in allSurahs) {
                if (!isActive) break
                _bulkDownloadStatusText.value = "Downloading Surah ${surah.number}/${totalSurahs}: ${surah.nameEnglish}..."
                val success = QuranAudioRepository.downloadSurah(
                    context = appCtx,
                    surahNumber = surah.number,
                    versesCount = surah.versesCount,
                    onProgress = { downloadedVerses, totalVerses, progressFraction ->
                        val overall = (completedCount + progressFraction) / totalSurahs.toFloat()
                        _bulkDownloadProgress.value = overall.coerceIn(0f, 1f)
                        _surahDownloadStates.value = _surahDownloadStates.value + (surah.number to SurahDownloadStatus.Downloading(progressFraction, downloadedVerses, totalVerses))
                    }
                )
                if (success) {
                    val size = QuranAudioRepository.getSurahDownloadedSizeBytes(appCtx, surah.number)
                    _surahDownloadStates.value = _surahDownloadStates.value + (surah.number to SurahDownloadStatus.Downloaded(size))
                    repository.recordDownloadedAudio(surah.number, status = "DOWNLOADED", totalVerses = surah.versesCount, downloadedVerses = surah.versesCount, sizeBytes = size)
                }
                completedCount++
                _bulkDownloadProgress.value = (completedCount.toFloat() / totalSurahs.toFloat()).coerceIn(0f, 1f)
            }

            _isBulkDownloadingQuran.value = false
            _bulkDownloadStatusText.value = if (completedCount == totalSurahs) "All 114 Surahs downloaded successfully!" else "Complete Qur'an download finished."
        }
    }

    fun cancelBulkQuranDownload() {
        bulkDownloadJob?.cancel()
        bulkDownloadJob = null
        _isBulkDownloadingQuran.value = false
        _bulkDownloadStatusText.value = "Download cancelled."
    }

    fun clearPlaybackError() {
        _playbackError.value = null
    }

    fun playVerseAudio(verse: Verse) {
        if (_playingSurahNumber.value == verse.surahNumber && _playingVerseNumber.value == verse.verseNumber) {
            if (_isPlayingAudio.value) {
                pauseAudio()
            } else {
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        mp.start()
                        _isPlayingAudio.value = true
                        startProgressTracker()
                    } catch (e: Exception) {
                        reloadAndPlayVerse(verse)
                    }
                } else {
                    reloadAndPlayVerse(verse)
                }
            }
            return
        }

        reloadAndPlayVerse(verse)
    }

    private fun reloadAndPlayVerse(verse: Verse) {
        _playingSurahNumber.value = verse.surahNumber
        _playingVerseNumber.value = verse.verseNumber
        _playingVerse.value = verse
        _audioProgress.value = 0f
        _playbackError.value = null

        val surahProg = computeCurrentSurahProgress(verse.surahNumber, verse.verseNumber, 0f)
        updateSurahProgress(verse.surahNumber, surahProg)

        progressJob?.cancel()
        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        viewModelScope.launch(Dispatchers.IO) {
            val appCtx = getApplication<Application>()
            val cachedFile = QuranAudioRepository.getVerseAudioFile(appCtx, verse.surahNumber, verse.verseNumber)
            val isLocalAvailable = cachedFile.exists() && cachedFile.length() > 0
            val isNetworkOk = NetworkUtils.isNetworkAvailable(appCtx)

            if (!isLocalAvailable && !isNetworkOk) {
                withContext(Dispatchers.Main) {
                    stopAudio()
                    val surahName = QuranData.SURAHS_DIRECTORY.find { it.number == verse.surahNumber }?.nameEnglish ?: "Surah ${verse.surahNumber}"
                    val msg = "Download required for offline playback. Connect to internet or download $surahName."
                    _playbackError.value = msg
                    android.widget.Toast.makeText(appCtx, msg, android.widget.Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            val sourcePathOrUrl: String = if (isLocalAvailable) {
                cachedFile.absolutePath
            } else {
                val downloadedFile = QuranAudioRepository.getOrCacheVerseAudioFile(appCtx, verse.surahNumber, verse.verseNumber)
                if (downloadedFile != null && downloadedFile.exists()) {
                    downloadedFile.absolutePath
                } else {
                    QuranAudioRepository.getCanonicalVerseAudioUrl(verse.surahNumber, verse.verseNumber)
                }
            }

            try {
                val mp = MediaPlayer()
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mp.setDataSource(sourcePathOrUrl)

                withContext(Dispatchers.Main) {
                    mediaPlayer = mp
                    mp.prepareAsync()
                    mp.setOnPreparedListener {
                        it.start()
                        _isPlayingAudio.value = true
                        startProgressTracker()
                    }
                    mp.setOnCompletionListener {
                        _audioProgress.value = 0f
                        val nextVerseNum = verse.verseNumber + 1
                        val totalVerses = QuranData.SURAHS_DIRECTORY.find { it.number == verse.surahNumber }?.versesCount ?: 1
                        if (nextVerseNum > totalVerses) {
                            updateSurahProgress(verse.surahNumber, 1.0f)
                            stopAudio()
                        } else {
                            playVerseByNumber(verse.surahNumber, nextVerseNum)
                        }
                    }
                    mp.setOnErrorListener { player, _, _ ->
                        try {
                            player.reset()
                            player.release()
                        } catch (_: Exception) {}
                        if (mediaPlayer == player) {
                            mediaPlayer = null
                            _isPlayingAudio.value = false
                            _playingSurahNumber.value = null
                            _playingVerseNumber.value = null
                            _playingVerse.value = null
                            _audioProgress.value = 0f
                            progressJob?.cancel()
                        }

                        val surahName = QuranData.SURAHS_DIRECTORY.find { it.number == verse.surahNumber }?.nameEnglish ?: "Surah ${verse.surahNumber}"
                        val errMsg = if (!NetworkUtils.isNetworkAvailable(appCtx)) {
                            "Download required for offline playback. Connect to internet or download $surahName."
                        } else {
                            "Unable to play recitation for $surahName. Check connection."
                        }
                        _playbackError.value = errMsg
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            android.widget.Toast.makeText(appCtx, errMsg, android.widget.Toast.LENGTH_LONG).show()
                        }
                        true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    stopAudio()
                    val surahName = QuranData.SURAHS_DIRECTORY.find { it.number == verse.surahNumber }?.nameEnglish ?: "Surah ${verse.surahNumber}"
                    val errMsg = if (!NetworkUtils.isNetworkAvailable(appCtx)) {
                        "Download required for offline playback. Connect to internet or download $surahName."
                    } else {
                        "Unable to play recitation for $surahName."
                    }
                    _playbackError.value = errMsg
                    android.widget.Toast.makeText(appCtx, errMsg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun playVerseByNumber(surahNumber: Int, verseNumber: Int) {
        _playingSurahNumber.value = surahNumber
        _playingVerseNumber.value = verseNumber
        _audioProgress.value = 0f

        val initialSurahProg = computeCurrentSurahProgress(surahNumber, verseNumber, 0f)
        updateSurahProgress(surahNumber, initialSurahProg)

        viewModelScope.launch(Dispatchers.IO) {
            val currentVerses = if (_selectedSurah.value?.number == surahNumber && _surahVerses.value.isNotEmpty()) {
                _surahVerses.value
            } else {
                QuranData.getVersesForSurah(getApplication(), surahNumber)
            }
            val nextVerse = currentVerses.find { it.verseNumber == verseNumber }
            withContext(Dispatchers.Main) {
                if (nextVerse != null) {
                    reloadAndPlayVerse(nextVerse)
                } else {
                    // Reached end of surah
                    updateSurahProgress(surahNumber, 1.0f)
                    stopAudio()
                }
            }
        }
    }

    fun togglePlayPauseAudio() {
        if (_isPlayingAudio.value) {
            pauseAudio()
        } else {
            val mp = mediaPlayer
            if (mp != null) {
                mp.start()
                _isPlayingAudio.value = true
                startProgressTracker()
            } else {
                val sNum = _playingSurahNumber.value ?: _selectedSurah.value?.number ?: 1
                val minV = _surahVerses.value.firstOrNull()?.verseNumber ?: 1
                val vNum = _playingVerseNumber.value ?: minV
                playVerseByNumber(sNum, vNum)
            }
        }
    }

    fun playNextVerseAudio() {
        val sNum = _playingSurahNumber.value ?: _selectedSurah.value?.number ?: 1
        val minV = _surahVerses.value.firstOrNull()?.verseNumber ?: 1
        val currentV = _playingVerseNumber.value ?: minV
        val nextV = currentV + 1
        _playingSurahNumber.value = sNum
        _playingVerseNumber.value = nextV
        _audioProgress.value = 0f
        playVerseByNumber(sNum, nextV)
    }

    fun playPreviousVerseAudio() {
        val sNum = _playingSurahNumber.value ?: _selectedSurah.value?.number ?: 1
        val minV = _surahVerses.value.firstOrNull()?.verseNumber ?: 0
        val currentV = _playingVerseNumber.value ?: minV
        val now = System.currentTimeMillis()
        val currentPos = try { mediaPlayer?.currentPosition ?: 0 } catch (e: Exception) { 0 }

        // If played > 3s AND it is the first tap in a sequence, restart current verse
        if (currentPos > 3000 && (now - lastPreviousTapTime) > 1500) {
            try {
                mediaPlayer?.seekTo(0)
                _audioProgress.value = 0f
                val surahProg = computeCurrentSurahProgress(sNum, currentV, 0f)
                updateSurahProgress(sNum, surahProg)
            } catch (e: Exception) {
                playVerseByNumber(sNum, currentV)
            }
        } else {
            val prevV = (currentV - 1).coerceAtLeast(minV)
            _playingSurahNumber.value = sNum
            _playingVerseNumber.value = prevV
            _audioProgress.value = 0f
            playVerseByNumber(sNum, prevV)
        }
        lastPreviousTapTime = now
    }

    fun pauseAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (e: Exception) { }
        _isPlayingAudio.value = false
        progressJob?.cancel()
    }

    fun stopAudio() {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) { }
        mediaPlayer = null
        _isPlayingAudio.value = false
        _playingSurahNumber.value = null
        _playingVerseNumber.value = null
        _playingVerse.value = null
        _audioProgress.value = 0f
        progressJob?.cancel()
    }

    fun seekAudioTo(progressFraction: Float) {
        val coerced = progressFraction.coerceIn(0f, 1f)
        _audioProgress.value = coerced
        try {
            val mp = mediaPlayer
            if (mp != null && mp.duration > 0) {
                val targetMs = (coerced * mp.duration).toInt()
                mp.seekTo(targetMs)
            }
            val sNum = _playingSurahNumber.value
            val vNum = _playingVerseNumber.value ?: 1
            if (sNum != null) {
                val surahProg = computeCurrentSurahProgress(sNum, vNum, coerced)
                updateSurahProgress(sNum, surahProg)
            }
        } catch (e: Exception) { }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && _isPlayingAudio.value) {
                try {
                    val mp = mediaPlayer
                    if (mp != null && mp.isPlaying && mp.duration > 0) {
                        val intraProg = (mp.currentPosition.toFloat() / mp.duration.toFloat()).coerceIn(0f, 1f)
                        _audioProgress.value = intraProg
                        val sNum = _playingSurahNumber.value
                        val vNum = _playingVerseNumber.value ?: 1
                        if (sNum != null) {
                            val surahProg = computeCurrentSurahProgress(sNum, vNum, intraProg)
                            updateSurahProgress(sNum, surahProg)
                        }
                    }
                } catch (e: Exception) { }
                delay(100)
            }
        }
    }

    // Tasbeeh Methods
    fun selectDhikrPreset(preset: DhikrPreset) {
        _selectedDhikr.value = preset
        val savedCount = repository.getSavedDhikrCount(preset.id)
        val savedTarget = repository.getSavedDhikrTarget(preset.id, preset.defaultTarget)
        _dhikrCount.value = savedCount
        _dhikrTarget.value = savedTarget
    }

    fun setCustomTarget(target: Int) {
        _dhikrTarget.value = target
        repository.saveDhikrTarget(selectedDhikr.value.id, target)
    }

    fun addCustomTarget(target: Int) {
        repository.addCustomTarget(target)
        setCustomTarget(target)
    }

    fun deleteCustomTarget(target: Int) {
        val wasSelected = _dhikrTarget.value == target
        repository.deleteCustomTarget(target)
        if (wasSelected) {
            val remaining = allTargets.value.filter { it != target }
            val fallback = remaining.firstOrNull() ?: 33
            setCustomTarget(fallback)
        }
    }

    fun incrementDhikrCount() {
        val newCount = _dhikrCount.value + 1
        _dhikrCount.value = newCount
        repository.saveDhikrCount(selectedDhikr.value.id, newCount)

        if (newCount == _dhikrTarget.value) {
            // Target reached! Record in DB history
            viewModelScope.launch {
                repository.recordDhikrCompletion(selectedDhikr.value, newCount, _dhikrTarget.value)
            }
        }
    }

    fun decrementDhikrCount() {
        val current = _dhikrCount.value
        if (current > 0) {
            val newCount = current - 1
            _dhikrCount.value = newCount
            repository.saveDhikrCount(selectedDhikr.value.id, newCount)
        }
    }

    fun resetDhikrCount() {
        _dhikrCount.value = 0
        repository.saveDhikrCount(selectedDhikr.value.id, 0)
    }

    fun addCustomDhikr(transliteration: String, arabicText: String, meaning: String, defaultTarget: Int) {
        val preset = DhikrPreset(
            id = "custom_" + System.currentTimeMillis(),
            nameEnglish = transliteration,
            nameArabic = arabicText,
            translation = meaning,
            defaultTarget = defaultTarget,
            isCustom = true
        )
        repository.addCustomDhikr(preset)
        selectDhikrPreset(preset)
    }

    fun updateCustomDhikr(preset: DhikrPreset) {
        repository.updateCustomDhikr(preset)
        if (_selectedDhikr.value.id == preset.id) {
            _selectedDhikr.value = preset
        }
    }

    fun deleteCustomDhikr(presetId: String) {
        val wasSelected = _selectedDhikr.value.id == presetId
        repository.deleteCustomDhikr(presetId)
        if (wasSelected) {
            selectDhikrPreset(repository.DHIKR_PRESETS[0])
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.setVibrationEnabled(enabled)
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        sensorManager?.unregisterListener(this)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
