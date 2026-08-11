package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NavItem
import com.example.ui.components.SereneBottomNavBar
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.TasbeehScreen
import com.example.ui.theme.FiveLightTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appearanceMode by viewModel.appearanceMode.collectAsStateWithLifecycle()

            FiveLightTheme(appearanceMode = appearanceMode) {
                var currentRoute by remember { mutableStateOf(NavItem.HOME.route) }
                var showSettingsSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        SereneBottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate = { navItem -> currentRoute = navItem.route }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(
                            targetState = currentRoute,
                            animationSpec = tween(durationMillis = 300),
                            label = "screenTransition"
                        ) { route ->
                            when (route) {
                                NavItem.HOME.route -> {
                                    val nextPrayer by viewModel.nextPrayer.collectAsStateWithLifecycle()
                                    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
                                    val countdownFormatted by viewModel.countdownFormatted.collectAsStateWithLifecycle()
                                    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
                                    val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()
                                    val todayLog by viewModel.todayPrayerLog.collectAsStateWithLifecycle()

                                    HomeScreen(
                                        nextPrayer = nextPrayer,
                                        prayerTimes = prayerTimes,
                                        countdownFormatted = countdownFormatted,
                                        selectedCity = selectedCity,
                                        hijriDate = hijriDate,
                                        todayLog = todayLog,
                                        onTogglePrayer = { pName -> viewModel.togglePrayerCompleted(pName) },
                                        onQuickAccessNavigate = { navItem -> currentRoute = navItem.route },
                                        onOpenSettings = { showSettingsSheet = true }
                                    )
                                }

                                NavItem.QIBLA.route -> {
                                    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
                                    val qiblaAngle by viewModel.qiblaAngle.collectAsStateWithLifecycle()
                                    val compassHeading by viewModel.compassHeading.collectAsStateWithLifecycle()
                                    val isSensorAvailable by viewModel.isSensorAvailable.collectAsStateWithLifecycle()

                                    QiblaScreen(
                                        cityLocation = selectedCity,
                                        qiblaAngle = qiblaAngle,
                                        compassHeading = compassHeading,
                                        isSensorAvailable = isSensorAvailable
                                    )
                                }

                                NavItem.QURAN.route -> {
                                    val searchQuery by viewModel.surahSearchQuery.collectAsStateWithLifecycle()
                                    val selectedSurah by viewModel.selectedSurah.collectAsStateWithLifecycle()
                                    val surahVerses by viewModel.surahVerses.collectAsStateWithLifecycle()
                                    val fontSizeSp by viewModel.fontSizeSp.collectAsStateWithLifecycle()
                                    val showEnglishTranslation by viewModel.showEnglishTranslation.collectAsStateWithLifecycle()
                                    val isNightReadingMode by viewModel.isNightReadingMode.collectAsStateWithLifecycle()
                                    val playingVerseNumber by viewModel.playingVerseNumber.collectAsStateWithLifecycle()
                                    val isPlayingAudio by viewModel.isPlayingAudio.collectAsStateWithLifecycle()
                                    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

                                    QuranScreen(
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { query -> viewModel.setSurahSearchQuery(query) },
                                        selectedSurah = selectedSurah,
                                        verses = surahVerses,
                                        onSelectSurah = { surah -> viewModel.selectSurah(surah) },
                                        fontSizeSp = fontSizeSp,
                                        onFontSizeChange = { sp -> viewModel.setFontSize(sp) },
                                        showEnglishTranslation = showEnglishTranslation,
                                        onToggleEnglish = { viewModel.toggleEnglishTranslation() },
                                        isNightReadingMode = isNightReadingMode,
                                        onToggleNightReading = { viewModel.toggleNightReadingMode() },
                                        playingVerseNumber = playingVerseNumber,
                                        isPlayingAudio = isPlayingAudio,
                                        onPlayVerseAudio = { verse -> viewModel.playVerseAudio(verse) },
                                        bookmarks = bookmarks,
                                        onToggleBookmark = { verse, isBk -> viewModel.toggleBookmarkVerse(verse, isBk) }
                                    )
                                }

                                NavItem.TASBEEH.route -> {
                                    val selectedDhikr by viewModel.selectedDhikr.collectAsStateWithLifecycle()
                                    val dhikrCount by viewModel.dhikrCount.collectAsStateWithLifecycle()
                                    val dhikrTarget by viewModel.dhikrTarget.collectAsStateWithLifecycle()
                                    val dhikrHistory by viewModel.dhikrHistory.collectAsStateWithLifecycle()

                                    TasbeehScreen(
                                        presets = viewModel.repository.DHIKR_PRESETS,
                                        selectedPreset = selectedDhikr,
                                        dhikrCount = dhikrCount,
                                        dhikrTarget = dhikrTarget,
                                        dhikrHistory = dhikrHistory,
                                        onSelectPreset = { preset -> viewModel.selectDhikrPreset(preset) },
                                        onSetTarget = { t -> viewModel.setCustomTarget(t) },
                                        onIncrement = { viewModel.incrementDhikrCount() },
                                        onReset = { viewModel.resetDhikrCount() }
                                    )
                                }

                                NavItem.CALENDAR.route -> {
                                    val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()

                                    CalendarScreen(
                                        hijriDate = hijriDate
                                    )
                                }
                            }
                        }

                        if (showSettingsSheet) {
                            val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
                            val calcMethod by viewModel.calcMethod.collectAsStateWithLifecycle()
                            val madhab by viewModel.madhab.collectAsStateWithLifecycle()

                            SettingsBottomSheet(
                                sheetState = sheetState,
                                citiesList = viewModel.repository.PREDEFINED_CITIES,
                                selectedCity = selectedCity,
                                onSelectCity = { city -> viewModel.setCity(city) },
                                selectedCalcMethod = calcMethod,
                                onSelectCalcMethod = { method -> viewModel.setCalcMethod(method) },
                                selectedMadhab = madhab,
                                onSelectMadhab = { m -> viewModel.setMadhab(m) },
                                selectedAppearanceMode = appearanceMode,
                                onSelectAppearanceMode = { mode -> viewModel.setAppearanceMode(mode) },
                                onDismiss = { showSettingsSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
