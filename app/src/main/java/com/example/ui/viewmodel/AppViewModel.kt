package com.example.ui.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.DhikrHistoryEntity
import com.example.data.db.PrayerLogEntity
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.DhikrPreset
import com.example.data.model.HijriDate
import com.example.data.model.Madhab
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.data.repository.AppRepository
import com.example.data.util.HijriCalc
import com.example.data.util.QiblaCalc
import com.example.data.util.QuranData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class AppViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(db, application)

    // User Preferences
    val selectedCity: StateFlow<CityLocation> = repository.selectedCity
    val calcMethod: StateFlow<CalcMethod> = repository.calcMethod
    val madhab: StateFlow<Madhab> = repository.madhab
    val appearanceMode: StateFlow<AppearanceMode> = repository.appearanceMode

    // Prayer Times & Countdown
    private val _prayerTimes = MutableStateFlow<List<PrayerItem>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerItem>> = _prayerTimes.asStateFlow()

    private val _nextPrayer = MutableStateFlow<PrayerItem?>(null)
    val nextPrayer: StateFlow<PrayerItem?> = _nextPrayer.asStateFlow()

    private val _countdownFormatted = MutableStateFlow("00:00:00")
    val countdownFormatted: StateFlow<String> = _countdownFormatted.asStateFlow()

    // Today's Prayer Log
    val todayPrayerLog: StateFlow<PrayerLogEntity?> = repository.getPrayerLogForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Qibla & Sensors
    private val _qiblaAngle = MutableStateFlow(0f)
    val qiblaAngle: StateFlow<Float> = _qiblaAngle.asStateFlow()

    private val _compassHeading = MutableStateFlow(0f)
    val compassHeading: StateFlow<Float> = _compassHeading.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(true)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var sensorManager: SensorManager? = null
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

    // Audio Playback
    private var mediaPlayer: MediaPlayer? = null
    private val _playingVerseNumber = MutableStateFlow<Int?>(null)
    val playingVerseNumber: StateFlow<Int?> = _playingVerseNumber.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    // Tasbeeh State
    private val _selectedDhikr = MutableStateFlow(repository.DHIKR_PRESETS[0])
    val selectedDhikr: StateFlow<DhikrPreset> = _selectedDhikr.asStateFlow()

    private val _dhikrCount = MutableStateFlow(0)
    val dhikrCount: StateFlow<Int> = _dhikrCount.asStateFlow()

    private val _dhikrTarget = MutableStateFlow(33)
    val dhikrTarget: StateFlow<Int> = _dhikrTarget.asStateFlow()

    val dhikrHistory: StateFlow<List<DhikrHistoryEntity>> = repository.dhikrHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hijri Calendar
    private val _hijriDate = MutableStateFlow(HijriCalc.getHijriDate())
    val hijriDate: StateFlow<HijriDate> = _hijriDate.asStateFlow()

    private var tickerJob: Job? = null

    init {
        refreshPrayerTimes()
        startCountdownTicker()
        initSensors()
        loadSurahVerses(QuranData.SURAHS_DIRECTORY[0])
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

    fun refreshPrayerTimes() {
        val times = repository.getTodayPrayerTimes()
        _prayerTimes.value = times

        val next = times.find { it.isNext } ?: times.find { it.name == PrayerName.FAJR }
        _nextPrayer.value = next

        // Calculate Qibla angle for current city
        val city = selectedCity.value
        _qiblaAngle.value = QiblaCalc.calculateQiblaDirection(city.latitude, city.longitude)
    }

    private fun startCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
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

                    _countdownFormatted.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
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

    // Compass Sensor Initialization
    private fun initSensors() {
        sensorManager = getApplication<Application>().getSystemService(Application.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val hasSensors = accelerometer != null && magnetometer != null
        _isSensorAvailable.value = hasSensors

        if (hasSensors) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private var lastHeadingTime = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val now = SystemClock.uptimeMillis()
            if (now - lastHeadingTime < 150) return
            lastHeadingTime = now

            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f

                if (kotlin.math.abs(azimuthDeg - _compassHeading.value) >= 1.0f) {
                    _compassHeading.value = azimuthDeg
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Quran Methods
    fun setSurahSearchQuery(query: String) {
        _surahSearchQuery.value = query
    }

    fun selectSurah(surah: Surah) {
        _selectedSurah.value = surah
        loadSurahVerses(surah)
    }

    private fun loadSurahVerses(surah: Surah) {
        val verses = QuranData.getVersesForSurah(surah.number)
        _surahVerses.value = verses
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

    // Audio Player
    fun playVerseAudio(verse: Verse) {
        if (_playingVerseNumber.value == verse.verseNumber && _isPlayingAudio.value) {
            pauseAudio()
            return
        }

        val url = verse.audioUrl.ifEmpty { "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3" }
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    _playingVerseNumber.value = verse.verseNumber
                    _isPlayingAudio.value = true
                }
                setOnCompletionListener {
                    _isPlayingAudio.value = false
                    _playingVerseNumber.value = null
                }
                setOnErrorListener { _, _, _ ->
                    _isPlayingAudio.value = false
                    _playingVerseNumber.value = null
                    true
                }
            }
        } catch (e: Exception) {
            _isPlayingAudio.value = false
            _playingVerseNumber.value = null
        }
    }

    fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        _isPlayingAudio.value = false
    }

    // Tasbeeh Methods
    fun selectDhikrPreset(preset: DhikrPreset) {
        _selectedDhikr.value = preset
        _dhikrTarget.value = preset.defaultTarget
        _dhikrCount.value = 0
    }

    fun setCustomTarget(target: Int) {
        _dhikrTarget.value = target
    }

    fun incrementDhikrCount() {
        val newCount = _dhikrCount.value + 1
        _dhikrCount.value = newCount

        if (newCount == _dhikrTarget.value) {
            // Target reached! Record in DB history
            viewModelScope.launch {
                repository.recordDhikrCompletion(selectedDhikr.value, newCount, _dhikrTarget.value)
            }
        }
    }

    fun resetDhikrCount() {
        _dhikrCount.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        sensorManager?.unregisterListener(this)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
