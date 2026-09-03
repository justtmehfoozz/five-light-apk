package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.screens.SplashScreen
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.rememberPredictiveBackState
import com.example.data.util.DuaItem
import com.example.data.model.AppearanceMode
import com.example.ui.components.ExploreSearchScope
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.rememberThemeTransitionController
import com.example.ui.theme.isSystemReducedMotion
import com.example.ui.theme.themeRadialReveal
import com.example.ui.components.ExpandedQuranPlayerSheet
import com.example.ui.components.NavItem
import com.example.ui.components.SereneBottomNavBar
import com.example.ui.components.rememberDockSelectorController
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.TasbeehScreen
import com.example.ui.theme.FiveLightTheme
import com.example.ui.viewmodel.AppViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            val appearanceMode by viewModel.appearanceMode.collectAsStateWithLifecycle()
            val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
            val themeTransitionController = rememberThemeTransitionController()
            val isReducedMotion = isSystemReducedMotion()
            val isSystemDark = isSystemInDarkTheme()

            FiveLightTheme(
                appearanceMode = appearanceMode,
                themeTransitionController = themeTransitionController
            ) {
                androidx.compose.runtime.CompositionLocalProvider(com.example.ui.theme.LocalVibrationEnabled provides vibrationEnabled) {
                val coroutineScope = rememberCoroutineScope()
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { NavItem.entries.size })
                val selectorController = rememberDockSelectorController(initialIndex = pagerState.currentPage)
                val currentRoute = selectorController.activeNavItem.value.route
                var openQuranReadingDirectly by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                var isQuranReadingModeActive by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                var showSettingsSheet by remember { mutableStateOf(false) }
                var showSearchOverlay by remember { mutableStateOf(false) }
                var dockFifthSlotMode by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("more") }
                var exploreSubRoute by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("main") }

                var targetDuaCategory by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
                var targetDuaId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
                var targetAdhkarTitle by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
                var targetNameNumber by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Int?>(null) }

                // Top-level tab navigation stack history
                val tabStack = androidx.compose.runtime.saveable.rememberSaveable(
                    saver = androidx.compose.runtime.saveable.listSaver(
                        save = { it.toList() },
                        restore = { androidx.compose.runtime.mutableStateListOf<Int>().apply { addAll(it) } }
                    )
                ) { androidx.compose.runtime.mutableStateListOf(0) }

                androidx.compose.runtime.LaunchedEffect(pagerState) {
                    androidx.compose.runtime.snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
                        .collect { (page, offset) ->
                            if (!selectorController.isDragging.value) {
                                selectorController.syncWithPager(page + offset)
                            }
                        }
                }

                androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
                    val current = pagerState.currentPage
                    if (current != 4) {
                        showSearchOverlay = false
                    }
                    if (current == 0) {
                        dockFifthSlotMode = "more"
                        tabStack.clear()
                        tabStack.add(0)
                    } else {
                        if (tabStack.contains(current)) {
                            tabStack.remove(current)
                        }
                        tabStack.add(current)
                    }
                }

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val hazeState = remember { HazeState() }

                val playingSurahNumber  = viewModel.playingSurahNumber.collectAsStateWithLifecycle()
                val showPrayerMode  = viewModel.showPrayerMode.collectAsStateWithLifecycle()
                var showExpandedPlayerSheet by remember { mutableStateOf(false) }
                var isScrolledAwayFromActiveVerse by remember { mutableStateOf(false) }
                var jumpToActiveVerseTrigger by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

                val mainPredictiveState = rememberPredictiveBackState()
                val canPopAppLevel = tabStack.size > 1 && !isQuranReadingModeActive && exploreSubRoute == "main" && showPrayerMode.value == null

                RegisterPredictiveBackHandler(
                    enabled = canPopAppLevel,
                    backState = mainPredictiveState,
                    onBack = {
                        if (tabStack.size > 1) {
                            tabStack.removeAt(tabStack.lastIndex)
                            val prevPage = tabStack.last()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(prevPage)
                            }
                        }
                    }
                )

                val isAppReady by viewModel.isInitialized.collectAsStateWithLifecycle()
                var splashExitProgress by remember { mutableFloatStateOf(0f) }
                var isSplashFinished by remember { mutableStateOf(false) }

                val isPagerSwipeEnabled = !showSearchOverlay && !showSettingsSheet && !showExpandedPlayerSheet &&
                    (showPrayerMode.value == null) && !isQuranReadingModeActive && (exploreSubRoute == "main")

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .themeRadialReveal(themeTransitionController)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val appContentAlpha = if (isSplashFinished) 1f else splashExitProgress
                    val appContentScale = if (isSplashFinished) 1f else (0.98f + 0.02f * splashExitProgress)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = appContentAlpha
                                scaleX = appContentScale
                                scaleY = appContentScale
                            }
                    ) {

                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = isPagerSwipeEnabled,
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .haze(state = hazeState),
                        beyondViewportPageCount = 1
                    ) { pageIndex ->
                        when (pageIndex) {
                            0 -> {
                                val nextPrayer by viewModel.nextPrayer.collectAsStateWithLifecycle()
                                val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
                                val countdownFormatted = viewModel.countdownFormatted.collectAsStateWithLifecycle()
                                val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
                                val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()
                                val islamicDateState by viewModel.islamicDateState.collectAsStateWithLifecycle()
                                val todayLog by viewModel.todayPrayerLog.collectAsStateWithLifecycle()
                                val naflPreferences by viewModel.naflPreferences.collectAsStateWithLifecycle()
                                val naflPrayerItems by viewModel.naflPrayerItems.collectAsStateWithLifecycle()
                                val rightNowItem by viewModel.rightNowItem.collectAsStateWithLifecycle()
                                val contextState by viewModel.contextState.collectAsStateWithLifecycle()
                                val lastReadPosition by viewModel.lastReadPosition.collectAsStateWithLifecycle()
                                val allPrayerLogs by viewModel.allPrayerLogs.collectAsStateWithLifecycle()
                                val qadaCounts by viewModel.qadaCounts.collectAsStateWithLifecycle()
                                val qadaEverAdded by viewModel.qadaEverAdded.collectAsStateWithLifecycle()
                                val homeFeaturesPreferences by viewModel.homeFeaturesPreferences.collectAsStateWithLifecycle()
                                val recentlyReadList by viewModel.recentlyReadList.collectAsStateWithLifecycle()
                                val prayerJourneyNodes by viewModel.prayerJourneyNodes.collectAsStateWithLifecycle()
                                val isFriday by viewModel.isFriday.collectAsStateWithLifecycle()

                                HomeScreen(
                                    nextPrayer = nextPrayer,
                                    prayerTimes = prayerTimes,
                                    qadaCounts = qadaCounts,
                                    qadaEverAdded = qadaEverAdded,
                                    onUpdateQadaCount = { p, c -> viewModel.updateQadaCount(p, c) },
                                    onMakeUpQadaPrayer = { pName -> viewModel.makeUpQadaPrayer(pName) },
                                    countdownFormatted = countdownFormatted,
                                    selectedCity = selectedCity,
                                    hijriDate = hijriDate,
                                    islamicDateState = islamicDateState,
                                    todayLog = todayLog,
                                    allPrayerLogs = allPrayerLogs,
                                    naflPreferences = naflPreferences,
                                    naflPrayerItems = naflPrayerItems,
                                    rightNowItem = rightNowItem,
                                    contextState = contextState,
                                    lastReadPosition = lastReadPosition,
                                    recentlyReadList = recentlyReadList,
                                    prayerJourneyNodes = prayerJourneyNodes,
                                    homeFeaturesPreferences = homeFeaturesPreferences,
                                    showPrayerMode = showPrayerMode.value,
                                    isFriday = isFriday,
                                    todayDateString = viewModel.currentDateString.collectAsStateWithLifecycle().value,
                                    onClosePrayerMode = { viewModel.closePrayerMode() },
                                    onNavigateToQuranSurahVerse = { surahNum, verseNum ->
                                        val surah = com.example.data.util.QuranData.SURAHS_DIRECTORY.find { it.number == surahNum }
                                        if (surah != null) {
                                            viewModel.selectSurah(surah)
                                            openQuranReadingDirectly = true
                                        }
                                        coroutineScope.launch { pagerState.scrollToPage(NavItem.QURAN.ordinal) }
                                    },
                                    onTogglePrayer = { pName -> viewModel.togglePrayerCompleted(pName) },
                                    onSetPrayerStatus = { pName, status -> viewModel.setPrayerStatus(prayerName = pName, status = status) },
                                    onSetPrayerStatusWithDate = { pName, dStr, status -> viewModel.setPrayerStatus(prayerName = pName, dateString = dStr, status = status) },
                                    onSavePrayerNote = { pName, dStr, note -> viewModel.savePrayerNote(prayerName = pName, dateString = dStr, note = note) },
                                    onAddPrayerToQada = { pName, dStr -> viewModel.addPrayerToQada(prayerName = pName, dateString = dStr) },
                                    onQuickAccessNavigate = { navItem ->
                                        coroutineScope.launch { 
                                            val targetPage = navItem.ordinal
                                            val currentPage = pagerState.currentPage
                                            if (targetPage != currentPage) {
                                                if (kotlin.math.abs(targetPage - currentPage) > 1) {
                                                    pagerState.scrollToPage(if (targetPage > currentPage) targetPage - 1 else targetPage + 1)
                                                }
                                                pagerState.animateScrollToPage(targetPage)
                                            }
                                        }
                                    },
                                    onOpenSettings = { showSettingsSheet = true },
                                    isActiveTab = (pagerState.currentPage == 0)
                                )
                            }

                            1 -> {
                                val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
                                val qiblaAngle by viewModel.qiblaAngle.collectAsStateWithLifecycle()
                                val compassHeading by viewModel.compassHeading.collectAsStateWithLifecycle()
                                val isSensorAvailable by viewModel.isSensorAvailable.collectAsStateWithLifecycle()
                                val isAccuracyLow by viewModel.isAccuracyLow.collectAsStateWithLifecycle()
                                val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()
                                val isQiblaActive = pagerState.currentPage == 1 && showPrayerMode.value == null

                                QiblaScreen(
                                    cityLocation = selectedCity,
                                    qiblaAngle = qiblaAngle,
                                    compassHeading = compassHeading,
                                    isSensorAvailable = isSensorAvailable,
                                    isAccuracyLow = isAccuracyLow,
                                    appearanceMode = appearanceMode,
                                    hijriDate = hijriDate,
                                    isActive = isQiblaActive,
                                    onStartListening = { viewModel.startCompassListening() },
                                    onStopListening = { viewModel.stopCompassListening() },
                                    onBackClick = {
                                        if (tabStack.size > 1) {
                                            tabStack.removeAt(tabStack.lastIndex)
                                            val prevPage = tabStack.last()
                                            coroutineScope.launch { pagerState.animateScrollToPage(prevPage) }
                                        } else {
                                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                        }
                                    }
                                )
                            }

                            2 -> {
                                val searchQuery by viewModel.surahSearchQuery.collectAsStateWithLifecycle()
                                val selectedSurah by viewModel.selectedSurah.collectAsStateWithLifecycle()
                                val surahVerses by viewModel.surahVerses.collectAsStateWithLifecycle()
                                val fontSizeSp by viewModel.fontSizeSp.collectAsStateWithLifecycle()
                                val showEnglishTranslation by viewModel.showEnglishTranslation.collectAsStateWithLifecycle()
                                val isNightReadingMode by viewModel.isNightReadingMode.collectAsStateWithLifecycle()
                                val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
                                val lastReadPosition by viewModel.lastReadPosition.collectAsStateWithLifecycle()
                                val surahScrollPositions = viewModel.surahScrollPositions.collectAsStateWithLifecycle()

                                val playingSurahNumber by viewModel.playingSurahNumber.collectAsStateWithLifecycle()
                                val isPlayingAudio by viewModel.isPlayingAudio.collectAsStateWithLifecycle()
                                val isLoadingAudio by viewModel.isLoadingAudio.collectAsStateWithLifecycle()
                                val surahPlaybackProgress = viewModel.surahPlaybackProgress.collectAsStateWithLifecycle()
                                val surahDownloadStates by viewModel.surahDownloadStates.collectAsStateWithLifecycle()
                                val isBulkDownloadingQuran by viewModel.isBulkDownloadingQuran.collectAsStateWithLifecycle()
                                val bulkDownloadProgress by viewModel.bulkDownloadProgress.collectAsStateWithLifecycle()
                                val bulkDownloadStatusText by viewModel.bulkDownloadStatusText.collectAsStateWithLifecycle()

                                val dailyQuranGoal by viewModel.dailyQuranGoal.collectAsStateWithLifecycle()

                                QuranScreen(
                                    dailyQuranGoal = dailyQuranGoal,
                                    onSetDailyGoal = { viewModel.setDailyQuranGoal(it) },
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
                                    playingSurahNumberProvider = { viewModel.playingSurahNumber.value },
                                    playingVerseNumberProvider = { viewModel.playingVerseNumber.value },
                                    isPlayingAudioProvider = { viewModel.isPlayingAudio.value },
                                    isLoadingAudioProvider = { viewModel.isLoadingAudio.value },
                                    audioProgressProvider = { viewModel.audioProgress.value },
                                    surahPlaybackProgress = surahPlaybackProgress,
                                    surahDownloadStates = surahDownloadStates,
                                    onDownloadSurah = { viewModel.downloadSurahAudio(it) },
                                    onDeleteDownloadedSurah = { viewModel.deleteDownloadedSurahAudio(it) },
                                    isBulkDownloadingQuran = isBulkDownloadingQuran,
                                    bulkDownloadProgress = bulkDownloadProgress,
                                    bulkDownloadStatusText = bulkDownloadStatusText,
                                    onDownloadAll114Surahs = { viewModel.downloadAll114Surahs() },
                                    onCancelBulkDownload = { viewModel.cancelBulkQuranDownload() },
                                    onPlayVerseAudio = { verse -> viewModel.playVerseAudio(verse) },
                                    onPlaySurah = { surah -> viewModel.playSurah(surah) },
                                    bookmarks = bookmarks,
                                    onToggleBookmark = { verse, isBk -> viewModel.toggleBookmarkVerse(verse, isBk) },
                                    onToggleSurahBookmark = { surah, isBk -> viewModel.toggleBookmarkForSurah(surah, isBk) },
                                    lastReadPosition = lastReadPosition,
                                    surahScrollPositions = surahScrollPositions,
                                    onSaveScrollPosition = { sNum, vIdx -> viewModel.saveSurahScrollPosition(sNum, vIdx) },
                                    onGetScrollPosition = { sNum -> viewModel.getSurahScrollPosition(sNum) },
                                    initialOpenReadingView = openQuranReadingDirectly,
                                    onResetInitialReadingView = { openQuranReadingDirectly = false },
                                    onScrolledAwayFromActiveVerseChange = { isAway ->
                                        isScrolledAwayFromActiveVerse = isAway
                                    },
                                    jumpToActiveVerseTrigger = jumpToActiveVerseTrigger,
                                    isActiveTab = (pagerState.currentPage == 2),
                                    onReadingModeChange = { isQuranReadingModeActive = it }
                                )
                            }

                            3 -> {
                                val allDhikrs by viewModel.allDhikrs.collectAsStateWithLifecycle()
                                val selectedDhikr by viewModel.selectedDhikr.collectAsStateWithLifecycle()
                                val dhikrCount by viewModel.dhikrCount.collectAsStateWithLifecycle()
                                val dhikrTarget by viewModel.dhikrTarget.collectAsStateWithLifecycle()
                                val allTargets by viewModel.allTargets.collectAsStateWithLifecycle()
                                val dhikrHistory by viewModel.dhikrHistory.collectAsStateWithLifecycle()
                                val tasbeehSound by viewModel.tasbeehSound.collectAsStateWithLifecycle()

                                TasbeehScreen(
                                    presets = allDhikrs,
                                    selectedPreset = selectedDhikr,
                                    dhikrCount = dhikrCount,
                                    dhikrTarget = dhikrTarget,
                                    targets = allTargets,
                                    dhikrHistory = dhikrHistory,
                                    selectedTasbeehSound = tasbeehSound,
                                    onSelectPreset = { preset -> viewModel.selectDhikrPreset(preset) },
                                    onSetTarget = { t -> viewModel.setCustomTarget(t) },
                                    onAddCustomTarget = { t -> viewModel.addCustomTarget(t) },
                                    onIncrement = { viewModel.incrementDhikrCount() },
                                    onDecrement = { viewModel.decrementDhikrCount() },
                                    onReset = { viewModel.resetDhikrCount() },
                                    onAddCustomDhikr = { translit, ar, meaning, target ->
                                        viewModel.addCustomDhikr(translit, ar, meaning, target)
                                    },
                                    onUpdateCustomDhikr = { preset -> viewModel.updateCustomDhikr(preset) },
                                    onDeleteCustomDhikr = { id -> viewModel.deleteCustomDhikr(id) },
                                    onDeleteCustomTarget = { t -> viewModel.deleteCustomTarget(t) },
                                    isActiveTab = (pagerState.currentPage == 3)
                                )
                            }

                            4 -> {
                                val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()
                                val islamicDateState by viewModel.islamicDateState.collectAsStateWithLifecycle()

                                ExploreScreen(
                                    hijriDate = hijriDate,
                                    islamicDateState = islamicDateState,
                                    activeSubRoute = exploreSubRoute,
                                    onSubRouteChange = { 
                                        exploreSubRoute = it
                                        if (it == "main") {
                                            targetDuaCategory = null
                                            targetDuaId = null
                                            targetAdhkarTitle = null
                                            targetNameNumber = null
                                        }
                                    },
                                    targetDuaCategory = targetDuaCategory,
                                    targetDuaId = targetDuaId,
                                    targetAdhkarTitle = targetAdhkarTitle,
                                    targetNameNumber = targetNameNumber,
                                    isActiveTab = (pagerState.currentPage == 4)
                                )
                            }
                        }
                    }

                    // Part 3: App-Wide "Last Light" Maghrib Ambient Overlay (Passive top atmospheric glow)
                    val maghribOverlayAlpha by viewModel.maghribOverlayAlpha.collectAsStateWithLifecycle()
                    val animatedMaghribAlpha by animateFloatAsState(
                        targetValue = maghribOverlayAlpha,
                        animationSpec = tween(durationMillis = 1000),
                        label = "maghrib_ambient_alpha"
                    )

                    if (animatedMaghribAlpha > 0.0005f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val topGlow = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFB14D6B).copy(alpha = animatedMaghribAlpha),
                                            Color(0xFFB14D6B).copy(alpha = animatedMaghribAlpha * 0.35f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width / 2f, 0f),
                                        radius = size.width * 0.95f
                                    )
                                    drawRect(brush = topGlow)
                                }
                        )
                    }

                    val allDhikrs by viewModel.allDhikrs.collectAsStateWithLifecycle()

                    val exploreSearchScope = when {
                        pagerState.currentPage == 4 && exploreSubRoute == "dua" -> ExploreSearchScope.DUA_LIBRARY
                        pagerState.currentPage == 4 && exploreSubRoute == "names" -> ExploreSearchScope.NAMES_OF_ALLAH
                        pagerState.currentPage == 4 && exploreSubRoute == "adhkar" -> ExploreSearchScope.DAILY_ADHKAR
                        else -> ExploreSearchScope.GLOBAL_EXPLORE
                    }

                    // Floating Dock overlay sitting directly over content
                    SereneBottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { navItem ->
                            coroutineScope.launch {
                                val targetPage = when (navItem.route) {
                                    "home" -> 0
                                    "qibla" -> 1
                                    "quran" -> 2
                                    "tasbeeh" -> 3
                                    "calendar", "search", "explore" -> 4
                                    else -> navItem.ordinal.coerceAtMost(4)
                                }
                                val currentPage = pagerState.currentPage
                                if (targetPage != currentPage) {
                                    if (kotlin.math.abs(targetPage - currentPage) > 1) {
                                        pagerState.scrollToPage(if (targetPage > currentPage) targetPage - 1 else targetPage + 1)
                                    }
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            }
                        },
                        hazeState = hazeState,
                        pagerFractionProvider = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                        selectorController = selectorController,
                        isPlaybackModeProvider = { playingSurahNumber.value != null },
                        playingSurahNumberProvider = { playingSurahNumber.value },
                        playingVerseNumberProvider = { viewModel.playingVerseNumber.value },
                        isPlayingProvider = { viewModel.isPlayingAudio.value },
                        isLoadingProvider = { viewModel.isLoadingAudio.value },
                        isPlayingFlow = viewModel.isPlayingAudio,
                        isLoadingFlow = viewModel.isLoadingAudio,
                        audioProgressProvider = { viewModel.audioProgress.value },
                        audioProgressFlow = viewModel.audioProgress,
                        audioDurationMsFlow = viewModel.audioDurationMs,
                        onPlayPause = { viewModel.togglePlayPauseAudio() },
                        onSkipPrevious = { viewModel.playPreviousVerseAudio() },
                        onSkipNext = { viewModel.playNextVerseAudio() },
                        onStopAudio = { viewModel.stopAudio() },
                        onSeekAudio = { viewModel.seekAudioTo(it) },
                        onOpenSearch = {
                            showSearchOverlay = true
                        },
                        isSearchActive = showSearchOverlay,
                        onDismissSearch = { showSearchOverlay = false },
                        searchScope = exploreSearchScope,
                        onSelectSurah = { surah ->
                            showSearchOverlay = false
                            openQuranReadingDirectly = true
                            viewModel.selectSurah(surah)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        onSelectDua = { dua ->
                            showSearchOverlay = false
                            val normAr = com.example.data.util.GlobalSearchEngine.normalizeArabic(dua.arabic)
                            val matchedDuaItem = com.example.data.util.DuaData.ALL_DUAS.find {
                                com.example.data.util.GlobalSearchEngine.normalizeArabic(it.arabic) == normAr ||
                                it.translation.contains(dua.translation, ignoreCase = true) ||
                                dua.translation.contains(it.translation, ignoreCase = true)
                            }
                            if (matchedDuaItem != null) {
                                targetDuaCategory = matchedDuaItem.category
                                targetDuaId = matchedDuaItem.id
                            } else {
                                targetDuaCategory = "Daily Life & Home"
                                targetDuaId = null
                            }
                            exploreSubRoute = "dua"
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        },
                        onSelectDuaItem = { duaItem ->
                            showSearchOverlay = false
                            targetDuaCategory = duaItem.category
                            targetDuaId = duaItem.id
                            exploreSubRoute = "dua"
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        },
                        onSelectDhikr = { dhikr ->
                            showSearchOverlay = false
                            viewModel.selectDhikrPreset(dhikr)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        },
                        onSelectAdhkarItem = { adhkarItem ->
                            showSearchOverlay = false
                            targetAdhkarTitle = adhkarItem.title
                            exploreSubRoute = "adhkar"
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        },
                        onSelectNameOfAllah = { name ->
                            showSearchOverlay = false
                            targetNameNumber = name.number
                            exploreSubRoute = "names"
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        },
                        allDhikrs = allDhikrs,
                        dockFifthSlotMode = dockFifthSlotMode,
                        onFifthSlotMoreTap = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        },
                        onExpandPlayer = {
                            showExpandedPlayerSheet = true
                        },
                        isScrolledAwayFromActiveVerse = isScrolledAwayFromActiveVerse && (playingSurahNumber.value != null) && (viewModel.playingVerseNumber.value != null) && !showExpandedPlayerSheet && (currentRoute == "quran" || pagerState.currentPage == 2),
                        onJumpToActiveVerse = {
                            jumpToActiveVerseTrigger = System.currentTimeMillis()
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (showExpandedPlayerSheet && playingSurahNumber.value != null) {
                        val playingSurah = remember(playingSurahNumber.value) {
                            com.example.data.util.QuranData.SURAHS_DIRECTORY.find { it.number == playingSurahNumber.value }
                        }
                        val bookmarkedVerses by viewModel.bookmarks.collectAsStateWithLifecycle()
                        val isCurrentBookmarked = remember(playingSurahNumber.value, viewModel.playingVerseNumber.value, bookmarkedVerses) {
                            val sNum = playingSurahNumber.value ?: 0
                            val vNum = viewModel.playingVerseNumber.value ?: 0
                            bookmarkedVerses.any { it.surahNumber == sNum && it.verseNumber == vNum }
                        }

                        ExpandedQuranPlayerSheet(
                            surah = playingSurah,
                            verseProvider = { viewModel.playingVerse.value },
                            currentVerseNumberProvider = { viewModel.playingVerseNumber.value },
                            isPlayingProvider = { viewModel.isPlayingAudio.value },
                            isLoadingProvider = { viewModel.isLoadingAudio.value },
                            isPlayingFlow = viewModel.isPlayingAudio,
                            isLoadingFlow = viewModel.isLoadingAudio,
                            audioProgressFlow = viewModel.audioProgress,
                            audioPositionMsFlow = viewModel.audioPositionMs,
                            audioDurationMsFlow = viewModel.audioDurationMs,
                            isBookmarkedProvider = { 
                                val sNum = playingSurahNumber.value ?: 0
                                val vNum = viewModel.playingVerseNumber.value ?: 0
                                val bk = viewModel.bookmarks.value
                                bk.any { it.surahNumber == sNum && it.verseNumber == vNum }
                            },
                            onPlayPause = { viewModel.togglePlayPauseAudio() },
                            onSkipPrevious = { viewModel.playPreviousVerseAudio() },
                            onSkipNext = { viewModel.playNextVerseAudio() },
                            onSeekAudio = { viewModel.seekAudioTo(it) },
                            onStopAudio = {
                                viewModel.stopAudio()
                                showExpandedPlayerSheet = false
                            },
                            onToggleBookmark = {
                                val curV = viewModel.playingVerse.value
                                if (curV != null) {
                                    viewModel.toggleVerseBookmark(curV)
                                }
                            },
                            onDismiss = { showExpandedPlayerSheet = false }
                        )
                    }

                    if (showSettingsSheet) {
                        val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
                        val calcMethod by viewModel.calcMethod.collectAsStateWithLifecycle()
                        val madhab by viewModel.madhab.collectAsStateWithLifecycle()
                        val timeFormat by viewModel.timeFormat.collectAsStateWithLifecycle()
                        val tasbeehSound by viewModel.tasbeehSound.collectAsStateWithLifecycle()
                        val naflPreferences by viewModel.naflPreferences.collectAsStateWithLifecycle()
                        val homeFeaturesPreferences by viewModel.homeFeaturesPreferences.collectAsStateWithLifecycle()
                        val hijriDateMethod by viewModel.hijriDateMethod.collectAsStateWithLifecycle()
                        val customHijriOffset by viewModel.customHijriOffset.collectAsStateWithLifecycle()

                        SettingsBottomSheet(
                            sheetState = sheetState,
                            citiesList = viewModel.repository.PREDEFINED_CITIES,
                            selectedCity = selectedCity,
                            onSelectCity = { city -> viewModel.setCity(city) },
                            selectedCalcMethod = calcMethod,
                            onSelectCalcMethod = { method -> viewModel.setCalcMethod(method) },
                            selectedMadhab = madhab,
                            onSelectMadhab = { m -> viewModel.setMadhab(m) },
                            selectedTimeFormat = timeFormat,
                            onSelectTimeFormat = { tf -> viewModel.setTimeFormat(tf) },
                            selectedAppearanceMode = appearanceMode,
                            onSelectAppearanceMode = { mode ->
                                val currentIsDark = when (appearanceMode) {
                                    AppearanceMode.SYSTEM -> isSystemDark
                                    AppearanceMode.DARK -> true
                                    AppearanceMode.LIGHT -> false
                                }
                                val targetIsDark = when (mode) {
                                    AppearanceMode.SYSTEM -> isSystemDark
                                    AppearanceMode.DARK -> true
                                    AppearanceMode.LIGHT -> false
                                }
                                themeTransitionController.startTransition(
                                    targetMode = mode,
                                    currentMode = appearanceMode,
                                    tapOrigin = androidx.compose.ui.geometry.Offset.Zero,
                                    isReducedMotion = isReducedMotion,
                                    isEffectiveThemeChanging = (currentIsDark != targetIsDark),
                                    onThemeApplied = { newMode ->
                                        viewModel.setAppearanceMode(newMode)
                                    }
                                )
                            },
                            onSelectAppearanceModeWithOrigin = { mode, origin ->
                                val currentIsDark = when (appearanceMode) {
                                    AppearanceMode.SYSTEM -> isSystemDark
                                    AppearanceMode.DARK -> true
                                    AppearanceMode.LIGHT -> false
                                }
                                val targetIsDark = when (mode) {
                                    AppearanceMode.SYSTEM -> isSystemDark
                                    AppearanceMode.DARK -> true
                                    AppearanceMode.LIGHT -> false
                                }
                                themeTransitionController.startTransition(
                                    targetMode = mode,
                                    currentMode = appearanceMode,
                                    tapOrigin = origin,
                                    isReducedMotion = isReducedMotion,
                                    isEffectiveThemeChanging = (currentIsDark != targetIsDark),
                                    onThemeApplied = { newMode ->
                                        viewModel.setAppearanceMode(newMode)
                                    }
                                )
                            },
                            selectedTasbeehSound = tasbeehSound,
                            onSelectTasbeehSound = { s -> viewModel.setTasbeehSound(s) },
                            selectedHijriMethod = hijriDateMethod,
                            onSelectHijriMethod = { m -> viewModel.setHijriDateMethod(m) },
                            customHijriOffset = customHijriOffset,
                            onUpdateCustomHijriOffset = { offset -> viewModel.setCustomHijriOffset(offset) },
                            vibrationEnabled = vibrationEnabled,
                            onToggleVibration = { enabled -> viewModel.setVibrationEnabled(enabled) },
                            naflPreferences = naflPreferences,
                            onUpdateNaflPreference = { t, i, d, a -> viewModel.setNaflPreference(t, i, d, a) },
                            homeFeaturesPreferences = homeFeaturesPreferences,
                            onUpdateHomeFeaturesPreference = { cr, rn, tn, no, pp, wo, mo, qm, pj, rr, ql, nc ->
                                viewModel.setHomeFeaturesPreference(cr, rn, tn, no, pp, wo, mo, qm, pj, rr, ql, nc)
                            },
                            onUpdateHomeFeatureOrder = { order -> viewModel.setHomeFeatureOrder(order) },
                            onResetHomeFeatureOrder = { viewModel.resetHomeFeatureOrder() },
                            onUpdateNaflOrder = { order -> viewModel.setNaflOrder(order) },
                            onResetNaflOrder = { viewModel.resetNaflOrder() },
                            onDismiss = { showSettingsSheet = false },
                            onSettingsChanged = { viewModel.refreshPrayerTimes() }
                        )
                    }
                    } // end inner Box

                    if (!isSplashFinished) {
                        SplashScreen(
                            isAppReady = isAppReady,
                            onExitProgressChanged = { progress -> splashExitProgress = progress },
                            onSplashFinished = { isSplashFinished = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

    override fun onResume() {
        super.onResume()
        viewModel.onAppResume()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
