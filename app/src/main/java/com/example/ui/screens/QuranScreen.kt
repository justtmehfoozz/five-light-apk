package com.example.ui.screens
import android.content.Intent
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.predictiveBackTransform
import com.example.ui.components.rememberPredictiveBackState
import com.example.ui.theme.semanticDockBorder

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


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.theme.*
import com.example.ui.theme.FiveLightMotion
import com.example.ui.theme.rememberIsReducedMotion
import com.example.ui.theme.fiveLightPressable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.CloudDownload
import com.example.data.audio.SurahDownloadStatus
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BookmarkEntity
import com.example.data.model.QuranLastRead
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.data.util.QuranData
import com.example.ui.components.PageHeader
import com.example.ui.components.SegmentedTabs
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.SerifHeaderFont

@Composable
fun QuranScreen(
    dailyQuranGoal: Int = 0,
    onSetDailyGoal: (Int) -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedSurah: Surah?,
    verses: List<Verse>,
    onSelectSurah: (Surah) -> Unit,
    fontSizeSp: Float,
    onFontSizeChange: (Float) -> Unit,
    showEnglishTranslation: Boolean,
    onToggleEnglish: () -> Unit,
    isNightReadingMode: Boolean,
    onToggleNightReading: () -> Unit,
    playingSurahNumber: Int?,
    playingVerseNumber: Int?,
    isPlayingAudio: Boolean,
    audioProgress: Float = 0f,
    surahPlaybackProgress: Map<Int, Float> = emptyMap(),
    surahDownloadStates: Map<Int, SurahDownloadStatus> = emptyMap(),
    onDownloadSurah: (Int) -> Unit = {},
    onDeleteDownloadedSurah: (Int) -> Unit = {},
    isBulkDownloadingQuran: Boolean = false,
    bulkDownloadProgress: Float = 0f,
    bulkDownloadStatusText: String = "",
    onDownloadAll114Surahs: () -> Unit = {},
    onCancelBulkDownload: () -> Unit = {},
    onPlayVerseAudio: (Verse) -> Unit,
    onPlaySurah: (Surah) -> Unit = {},
    bookmarks: List<BookmarkEntity>,
    onToggleBookmark: (Verse, Boolean) -> Unit,
    onToggleSurahBookmark: (Surah, Boolean) -> Unit = { _, _ -> },
    lastReadPosition: QuranLastRead? = null,
    surahScrollPositions: Map<Int, Int> = emptyMap(),
    onSaveScrollPosition: (surahNumber: Int, verseIndex: Int) -> Unit = { _, _ -> },
    onGetScrollPosition: (surahNumber: Int) -> Int = { 0 },
    initialOpenReadingView: Boolean = false,
    onResetInitialReadingView: () -> Unit = {},
    onScrolledAwayFromActiveVerseChange: (Boolean) -> Unit = {},
    jumpToActiveVerseTrigger: Long = 0L,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: All Surahs, 1: Bookmarks
    var isReadingViewActive by rememberSaveable { mutableStateOf(false) }
    var showFontSizeControls by remember { mutableStateOf(false) }
    var activeLensVerse by remember { mutableStateOf<Verse?>(null) }
    var quickActionSurah by remember { mutableStateOf<Surah?>(null) }
    var contextMenuVerse by remember { mutableStateOf<Verse?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val isReducedMotion = remember {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2000)
            feedbackMessage = null
        }
    }

    var hasCompletedEntrance by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasCompletedEntrance) {
            kotlinx.coroutines.delay(550)
            hasCompletedEntrance = true
        }
    }

    LaunchedEffect(initialOpenReadingView) {
        if (initialOpenReadingView && selectedSurah != null) {
            isReadingViewActive = true
            onResetInitialReadingView()
        }
    }

    val surahListState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) { androidx.compose.foundation.lazy.LazyListState() }
    val bookmarksListState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) { androidx.compose.foundation.lazy.LazyListState() }

    val bgContainer = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val cardBg = MaterialTheme.colorScheme.surface

    val quranPredictiveState = rememberPredictiveBackState()
    val isQuranBackActive = showFontSizeControls || activeLensVerse != null || quickActionSurah != null || contextMenuVerse != null || isReadingViewActive

    RegisterPredictiveBackHandler(
        enabled = isQuranBackActive,
        backState = quranPredictiveState,
        onBack = {
            if (showFontSizeControls) {
                showFontSizeControls = false
            } else if (activeLensVerse != null) {
                activeLensVerse = null
            } else if (quickActionSurah != null) {
                quickActionSurah = null
            } else if (contextMenuVerse != null) {
                contextMenuVerse = null
            } else if (isReadingViewActive) {
                isReadingViewActive = false
            }
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(bgContainer)
    ) {
        val isMotionReduced = rememberIsReducedMotion()
        val currentSurah = selectedSurah

        AnimatedContent(
            targetState = isReadingViewActive && currentSurah != null,
            modifier = Modifier.predictiveBackTransform(quranPredictiveState.progress, quranPredictiveState.swipeEdge),
            transitionSpec = {
                if (targetState) {
                    FiveLightMotion.slideFadeForward(isMotionReduced)
                } else {
                    FiveLightMotion.slideFadeBackward(isMotionReduced)
                }
            },
            label = "QuranScreenViewTransition"
        ) { isReaderView ->
            if (isReaderView && currentSurah != null) {
            val listState = remember(currentSurah.number) { androidx.compose.foundation.lazy.LazyListState() }

            // Restore scroll position ONLY AFTER loading verses (when verses.isNotEmpty())
            LaunchedEffect(currentSurah.number, verses.isNotEmpty()) {
                if (verses.isNotEmpty()) {
                    val savedIndex = onGetScrollPosition(currentSurah.number)
                    val targetIndex = if (savedIndex in 0 until verses.size) savedIndex else 0
                    listState.scrollToItem(targetIndex)
                }
            }

            // Save scroll position as user scrolls (debounced to avoid jank during scrolling)
            LaunchedEffect(listState, currentSurah.number) {
                snapshotFlow { listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .debounce(250L)
                    .collect { index ->
                        if (verses.isNotEmpty() && index >= 0) {
                            onSaveScrollPosition(currentSurah.number, index)
                        }
                    }
            }

            var userPausedAutoScroll by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            // Derive presentation layout: ceremonial Bismillah header + body flow verses
            val readerLayout = remember(selectedSurah.number, verses) {
                com.example.data.util.QuranData.getSurahReaderLayout(selectedSurah.number, verses)
            }

            // Find playing item index in the list
            val currentPlayingIndex = remember(playingVerseNumber, playingSurahNumber, currentSurah.number, readerLayout) {
                if (playingSurahNumber == currentSurah.number && playingVerseNumber != null) {
                    val bismillah = readerLayout.bismillahHeader
                    val hasBismillah = bismillah != null
                    if (bismillah != null && (playingVerseNumber == bismillah.verseNumber || (currentSurah.number == 1 && playingVerseNumber == 1) || playingVerseNumber == 0)) {
                        0
                    } else {
                        val rawIndex = readerLayout.flowVerses.indexOfFirst { it.verseNumber == playingVerseNumber }
                        if (rawIndex >= 0) {
                            if (hasBismillah) rawIndex + 1 else rawIndex
                        } else -1
                    }
                } else -1
            }

            // Track user scroll interactions: respect manual scrolling with robust hysteresis
            LaunchedEffect(listState, currentPlayingIndex) {
                snapshotFlow {
                    Pair(listState.isScrollInProgress, listState.layoutInfo.visibleItemsInfo.map { it.index })
                }.collect { (isScrolling, visibleIndices) ->
                    if (currentPlayingIndex >= 0) {
                        if (isScrolling) {
                            if (visibleIndices.isNotEmpty() && !visibleIndices.contains(currentPlayingIndex)) {
                                val firstVis = visibleIndices.first()
                                val lastVis = visibleIndices.last()
                                // Requires scrolling at least 2 items away before separating
                                if (currentPlayingIndex < firstVis - 1 || currentPlayingIndex > lastVis + 1) {
                                    userPausedAutoScroll = true
                                }
                            }
                        } else {
                            // When user scrolls back and active verse enters viewport, reset pause
                            if (visibleIndices.contains(currentPlayingIndex)) {
                                userPausedAutoScroll = false
                            }
                        }
                    }
                }
            }

            // Sync scrolled-away status with parent / bottom audio dock
            LaunchedEffect(userPausedAutoScroll, currentPlayingIndex, isReadingViewActive, selectedSurah.number, playingSurahNumber) {
                val isAway = userPausedAutoScroll && (currentPlayingIndex >= 0) && isReadingViewActive && (selectedSurah.number == playingSurahNumber)
                onScrolledAwayFromActiveVerseChange(isAway)
            }

            // Handle jump to active verse trigger from the integrated audio player morph
            LaunchedEffect(jumpToActiveVerseTrigger) {
                if (jumpToActiveVerseTrigger > 0L && currentPlayingIndex >= 0) {
                    userPausedAutoScroll = false
                    onScrolledAwayFromActiveVerseChange(false)
                    val targetScrollIndex = (currentPlayingIndex - 1).coerceAtLeast(0)
                    if (isReducedMotion) {
                        listState.scrollToItem(targetScrollIndex)
                    } else {
                        listState.animateScrollToItem(
                            index = targetScrollIndex,
                            scrollOffset = 0
                        )
                    }
                }
            }

            // Auto-scroll list smoothly to keep currently playing verse in view when not paused by manual scrolling
            LaunchedEffect(currentPlayingIndex, userPausedAutoScroll) {
                if (currentPlayingIndex >= 0 && !userPausedAutoScroll) {
                    val targetScrollIndex = (currentPlayingIndex - 1).coerceAtLeast(0)
                    if (isReducedMotion) {
                        listState.scrollToItem(targetScrollIndex)
                    } else {
                        listState.animateScrollToItem(
                            index = targetScrollIndex,
                            scrollOffset = 0
                        )
                    }
                }
            }

            // Detailed Verse Reader View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Reader Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isReadingViewActive = false },
                        modifier = Modifier.testTag("quran_reader_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedSurah.nameEnglish,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifHeaderFont),
                            color = textPrimary
                        )
                        Text(
                            text = "${selectedSurah.nameArabic} • ${selectedSurah.versesCount} Verses",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.semanticMutedText
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleEnglish) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "Toggle Translation",
                                tint = if (showEnglishTranslation) MaterialTheme.colorScheme.primary else textPrimary.copy(alpha = 0.5f)
                            )
                        }

                        IconButton(onClick = { showFontSizeControls = !showFontSizeControls }) {
                            Icon(
                                imageVector = Icons.Outlined.FormatSize,
                                contentDescription = "Font Size",
                                tint = textPrimary
                            )
                        }
                    }
                }

                // 7. Reading Progress Indicator (strictly scroll position only, theme accent)
                val readingProgress by remember(listState, verses.size) {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        if (totalItems <= 1) {
                            0f
                        } else {
                            val visibleItems = layoutInfo.visibleItemsInfo
                            if (visibleItems.isEmpty()) {
                                0f
                            } else {
                                val firstVisible = visibleItems.first()
                                val firstIndex = firstVisible.index
                                val itemSize = firstVisible.size
                                val offset = if (itemSize > 0) listState.firstVisibleItemScrollOffset.toFloat() / itemSize else 0f
                                val exactIndex = firstIndex + offset
                                val maxIndex = (totalItems - 1).coerceAtLeast(1).toFloat()
                                (exactIndex / maxIndex).coerceIn(0f, 1f)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.semanticBorder.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = readingProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.semanticPrimaryAccent)
                    )
                }

                // Font Size Slider Overlay
                if (showFontSizeControls) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Arabic Text Size: ${fontSizeSp.toInt()} sp",
                                style = MaterialTheme.typography.labelLarge,
                                color = textPrimary
                            )
                            Slider(
                                value = fontSizeSp,
                                onValueChange = onFontSizeChange,
                                valueRange = 18f..42f,
                                modifier = Modifier.testTag("font_size_slider")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Verses List with Shimmer Skeletons
                AnimatedContent(
                    targetState = verses.isEmpty(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "verses_loading_transition"
                ) { isLoading ->
                    if (isLoading) {
                        val infiniteTransition = rememberInfiniteTransition(label = "quran_skeleton_shimmer")
                        val shimmerAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.06f,
                            targetValue = 0.10f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1350, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "shimmer_alpha"
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(4) {
                                VerseCardSkeleton(
                                    isNightMode = isNightReadingMode,
                                    shimmerAlpha = shimmerAlpha
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(140.dp))
                            }
                        }
                    } else {
                        val bookmarkedKeys = remember(bookmarks) {
                            bookmarks.map { "${it.surahNumber}_${it.verseNumber}" }.toSet()
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (readerLayout.bismillahHeader != null) {
                                    item(key = "bismillah_header_${selectedSurah.number}") {
                                        val bismillahVerse = readerLayout.bismillahHeader
                                        val isBismillahActive = (playingSurahNumber == selectedSurah.number) &&
                                                (playingVerseNumber == bismillahVerse.verseNumber || (selectedSurah.number == 1 && playingVerseNumber == 1) || playingVerseNumber == 0)

                                        BismillahHeader(
                                            verse = bismillahVerse,
                                            fontSizeSp = fontSizeSp,
                                            showTranslation = showEnglishTranslation,
                                            isPlaying = isBismillahActive && isPlayingAudio,
                                            isVerseActive = isBismillahActive,
                                            onOpenLens = { activeLensVerse = bismillahVerse },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuVerse = bismillahVerse
                                            }
                                        )
                                    }
                                }

                                items(readerLayout.flowVerses, key = { it.verseKey }) { verse ->
                                    val isBookmarked = bookmarkedKeys.contains("${verse.surahNumber}_${verse.verseNumber}")
                                    val isVerseActive = (playingSurahNumber == verse.surahNumber) && (playingVerseNumber == verse.verseNumber)

                                    VerseCard(
                                        verse = verse,
                                        fontSizeSp = fontSizeSp,
                                        showTranslation = showEnglishTranslation,
                                        isNightMode = isNightReadingMode,
                                        isVerseActive = isVerseActive,
                                        isPlaying = isVerseActive && isPlayingAudio,
                                        isBookmarked = isBookmarked,
                                        onPlayAudio = { onPlayVerseAudio(verse) },
                                        onToggleBookmark = { onToggleBookmark(verse, isBookmarked) },
                                        onOpenLens = { activeLensVerse = verse },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            contextMenuVerse = verse
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(180.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Directory View (Surah List / Bookmarks)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                PageHeader(
                    title = "Holy Quran",
                    subtitle = null,
                    includeStatusBarPadding = false,
                    horizontalPadding = 0.dp,
                    titleColor = textPrimary
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_search_input"),
                    placeholder = { Text("Search Surah name or number...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.semanticControl,
                        unfocusedContainerColor = Color.semanticControl,
                        focusedBorderColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticPrimaryAccent else Color.semanticDockBorder,
                        unfocusedBorderColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticBorder else Color.semanticDockBorder,
                        focusedTextColor = Color.semanticPrimaryText,
                        unfocusedTextColor = Color.semanticPrimaryText,
                        focusedLeadingIconColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticPrimaryAccent else Color.semanticDockBorder,
                        unfocusedLeadingIconColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticSecondaryText else Color.semanticDockBorder,
                        focusedPlaceholderColor = Color.semanticMutedText,
                        unfocusedPlaceholderColor = Color.semanticMutedText
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Minimal Segmented Tabs: All Surahs, Bookmarks, Planner
                val quranTabs = remember { listOf("All Surahs", "Bookmarks", "Planner") }
                SegmentedTabs(
                    tabs = quranTabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth(),
                    testTagPrefix = "quran_tab"
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Filtered Surahs List - Memoized to prevent heavy allocations during scroll
                    val filteredSurahs = remember(searchQuery) {
                        QuranData.SURAHS_DIRECTORY.filter {
                            it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                                    it.englishTranslation.contains(searchQuery, ignoreCase = true) ||
                                    it.number.toString() == searchQuery
                        }
                    }

                    LazyColumn(
                        state = surahListState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (searchQuery.isEmpty()) {
                            item(key = "bulk_audio_download_banner") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.semanticBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.CloudDownload,
                                                        contentDescription = null,
                                                        tint = Color.semanticPrimaryAccent,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Offline-First Audio Recitation",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textPrimary
                                                    )
                                                    Text(
                                                        text = if (isBulkDownloadingQuran) bulkDownloadStatusText else "Download all 114 Surahs for 100% offline playback",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            if (isBulkDownloadingQuran) {
                                                IconButton(onClick = onCancelBulkDownload) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Cancel Download",
                                                        tint = Color.semanticError
                                                    )
                                                }
                                            } else {
                                                Surface(
                                                    onClick = onDownloadAll114Surahs,
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.semanticPrimaryAccent.copy(alpha = 0.12f)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Download,
                                                            contentDescription = null,
                                                            tint = Color.semanticPrimaryAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Download All",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.semanticPrimaryAccent
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (isBulkDownloadingQuran) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            androidx.compose.material3.LinearProgressIndicator(
                                                progress = { bulkDownloadProgress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = Color.semanticPrimaryAccent,
                                                trackColor = Color.semanticBorder
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(filteredSurahs, key = { it.number }) { surah ->
                            val isPlayingThisSurah = playingSurahNumber == surah.number
                            val isLastReadSurah = playingSurahNumber == null && lastReadPosition?.surahNumber == surah.number
                            val isCurrent = isPlayingThisSurah || isLastReadSurah

                            val audioProgressVal = surahPlaybackProgress[surah.number] ?: 0f
                            val scrollPos = surahScrollPositions[surah.number]
                            val readingProgressVal = if (scrollPos != null && surah.versesCount > 0) {
                                ((scrollPos + 1).toFloat() / surah.versesCount.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            val progress = if (audioProgressVal > 0f) audioProgressVal else readingProgressVal
                            val dlStatus = surahDownloadStates[surah.number]

                            SurahListItem(
                                surah = surah,
                                isCurrent = isCurrent,
                                playbackProgress = progress,
                                downloadStatus = dlStatus,
                                onDownloadClick = { onDownloadSurah(surah.number) },
                                onClick = {
                                    onSelectSurah(surah)
                                    isReadingViewActive = true
                                },
                                onLongClick = {
                                    quickActionSurah = surah
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                } else if (selectedTab == 1) {
                    // Bookmarks List
                    if (bookmarks.isEmpty()) {
                        QuietEmptyState(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No bookmarked verses yet.\nTap the bookmark icon while reading to save verses.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = bookmarksListState,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(bookmarks, key = { "${it.surahNumber}_${it.verseNumber}" }) { bookmark ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val surahMeta = QuranData.SURAHS_DIRECTORY.find { it.number == bookmark.surahNumber }
                                            if (surahMeta != null) {
                                                onSelectSurah(surahMeta)
                                                isReadingViewActive = true
                                            }
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    val isDarkScreen = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${bookmark.surahNameEnglish} (${bookmark.surahNameArabic}) - Verse ${bookmark.verseNumber}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isDarkScreen) Color(0xFFB0B0AA) else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = bookmark.verseTextArabic,
                                            style = ArabicTextStyle.copy(fontSize = 20.sp),
                                            color = if (isDarkScreen) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = bookmark.verseTextTranslation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDarkScreen) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(120.dp))
                            }
                        }
                    }
                }
            }
        }

        // Part 5: Long-Press Surah Quick Actions Bottom Sheet
        quickActionSurah?.let { surah ->
            val isSurahBookmarked = bookmarks.any { it.surahNumber == surah.number }
            val savedVerseIndex = surahScrollPositions[surah.number]
            val status = surahDownloadStates[surah.number]

            SurahQuickActionSheet(
                surah = surah,
                isBookmarked = isSurahBookmarked,
                savedVerseIndex = savedVerseIndex,
                downloadStatus = status,
                onToggleBookmark = {
                    onToggleSurahBookmark(surah, isSurahBookmarked)
                },
                onPlayAudio = {
                    onPlaySurah(surah)
                },
                onDownloadAudio = {
                    onDownloadSurah(surah.number)
                },
                onDeleteDownload = {
                    onDeleteDownloadedSurah(surah.number)
                },
                onJumpToLastRead = {
                    onSelectSurah(surah)
                    isReadingViewActive = true
                },
                onDismiss = { quickActionSurah = null }
            )
        }

        if (activeLensVerse != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val lensInfo = remember(activeLensVerse) {
                com.example.data.util.QuranData.getQuranLensInfoForVerse(
                    context,
                    activeLensVerse!!.surahNumber,
                    activeLensVerse!!.verseNumber
                )
            }
            com.example.ui.components.QuranLensSheet(
                lensInfo = lensInfo,
                onSelectOccurrence = { surahNum, verseNum ->
                    val surahMeta = com.example.data.util.QuranData.SURAHS_DIRECTORY.find { it.number == surahNum }
                    if (surahMeta != null) {
                        onSelectSurah(surahMeta)
                        isReadingViewActive = true
                    }
                },
                onDismiss = { activeLensVerse = null }
            )
        }

        // Verse Context Menu Sheet (Part 3 — Context Menu)
        if (contextMenuVerse != null) {
            val menuVerse = contextMenuVerse!!
            val isMenuVerseBookmarked = bookmarks.any { it.surahNumber == menuVerse.surahNumber && it.verseNumber == menuVerse.verseNumber }
            val currentSurahName = selectedSurah?.nameEnglish ?: "Surah ${menuVerse.surahNumber}"

            VerseContextMenuSheet(
                verse = menuVerse,
                surahName = currentSurahName,
                isBookmarked = isMenuVerseBookmarked,
                onCopyArabic = {
                    clipboardManager.setText(AnnotatedString(menuVerse.textArabic))
                    feedbackMessage = "Arabic text copied"
                },
                onShareVerse = {
                    val shareText = buildString {
                        append(menuVerse.textArabic)
                        if (menuVerse.textEnglish.isNotBlank()) {
                            append("\n\n")
                            append(menuVerse.textEnglish)
                        }
                        append("\n\n— Surah ")
                        append(currentSurahName)
                        append(" (")
                        append(menuVerse.surahNumber)
                        append(":")
                        append(menuVerse.verseNumber)
                        append(")")
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_TITLE, "$currentSurahName ${menuVerse.surahNumber}:${menuVerse.verseNumber}")
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Verse")
                    context.startActivity(shareIntent)
                },
                onCopyVerseWithTranslation = {
                    val fullText = buildString {
                        append(menuVerse.textArabic)
                        if (menuVerse.textEnglish.isNotBlank()) {
                            append("\n\n")
                            append(menuVerse.textEnglish)
                        }
                        append("\n\n— Surah ")
                        append(currentSurahName)
                        append(" (")
                        append(menuVerse.surahNumber)
                        append(":")
                        append(menuVerse.verseNumber)
                        append(")")
                    }
                    clipboardManager.setText(AnnotatedString(fullText))
                    feedbackMessage = "Verse & translation copied"
                },
                onPlayAudio = {
                    onPlayVerseAudio(menuVerse)
                },
                onToggleBookmark = {
                    onToggleBookmark(menuVerse, isMenuVerseBookmarked)
                    feedbackMessage = if (isMenuVerseBookmarked) "Bookmark removed" else "Bookmark saved"
                },
                onDismiss = { contextMenuVerse = null }
            )
        }

        // Subdued Minimal Feedback Banner
        AnimatedVisibility(
            visible = feedbackMessage != null,
            enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(140)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.semanticSurfaceElevated,
                border = BorderStroke(1.dp, Color.semanticBorder),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = feedbackMessage ?: "",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.semanticPrimaryText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurahListItem(
    surah: Surah,
    isCurrent: Boolean = false,
    playbackProgress: Float = 0f,
    downloadStatus: SurahDownloadStatus? = null,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val cardBg = MaterialTheme.colorScheme.surface
    val defaultBorder = MaterialTheme.colorScheme.outline
    val goldAccent = Color.semanticPrimaryAccent
    val cardBorder = if (isCurrent) {
        goldAccent.copy(alpha = if (isDark) 0.85f else 0.75f)
    } else {
        defaultBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .fiveLightPressable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isCurrent) goldAccent.copy(alpha = if (isDark) 0.08f else 0.05f)
                    else Color.Transparent
                )
        ) {
            // Amber left-edge accent for Current Surah
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .width(3.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(goldAccent)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Surah Number Badge with Circular Playback Progress Ring
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Badge background circle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2C2C2E).copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        )

                        // Circular playback progress ring around Surah number
                        if (playbackProgress > 0f) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            ) {
                                val strokeWidth = 2.dp.toPx()
                                drawArc(
                                    color = goldAccent.copy(alpha = if (isDark) 0.95f else 0.90f),
                                    startAngle = -90f,
                                    sweepAngle = 360f * playbackProgress.coerceIn(0f, 1f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Text(
                            text = "${surah.number}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isDark) Color(0xFFB0B0AA) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = surah.nameEnglish,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(goldAccent.copy(alpha = 0.18f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Current",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = goldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${surah.englishTranslation} • ${surah.versesCount} verses",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (downloadStatus) {
                        is SurahDownloadStatus.Downloaded -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Downloaded Offline",
                                tint = goldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        is SurahDownloadStatus.Downloading -> {
                            CircularProgressIndicator(
                                progress = { downloadStatus.progress },
                                modifier = Modifier.size(16.dp),
                                color = goldAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        else -> {}
                    }

                    Text(
                        text = surah.nameArabic,
                        style = ArabicTextStyle.copy(fontSize = 22.sp),
                        color = if (isDark) Color(0xFFB0B0AA) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahQuickActionSheet(
    surah: Surah,
    isBookmarked: Boolean,
    savedVerseIndex: Int?,
    downloadStatus: SurahDownloadStatus? = null,
    onToggleBookmark: () -> Unit,
    onPlayAudio: () -> Unit,
    onDownloadAudio: () -> Unit = {},
    onDeleteDownload: () -> Unit = {},
    onJumpToLastRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${surah.number}. ${surah.nameEnglish}",
                        fontFamily = SerifHeaderFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${surah.englishTranslation} • ${surah.versesCount} verses",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = surah.nameArabic,
                    style = ArabicTextStyle.copy(fontSize = 24.sp),
                    color = if (isDark) Color(0xFFB0B0AA) else MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // Action 1: Bookmark / Remove Bookmark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onToggleBookmark()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isBookmarked) Color.semanticPrimaryAccent else Color.semanticPrimaryAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isBookmarked) "Remove Bookmark" else "Bookmark Surah",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBookmarked) "Remove from saved bookmarks" else "Save verse 1 to bookmarks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action 2: Play Audio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onPlayAudio()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Play Audio",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Listen from Verse 1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action 3: Offline Download / Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (downloadStatus is SurahDownloadStatus.Downloaded) {
                            onDeleteDownload()
                        } else {
                            onDownloadAudio()
                        }
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (downloadStatus) {
                        is SurahDownloadStatus.Downloaded -> Icons.Filled.Delete
                        is SurahDownloadStatus.Downloading -> Icons.Filled.DownloadDone
                        else -> Icons.Outlined.Download
                    },
                    contentDescription = null,
                    tint = if (downloadStatus is SurahDownloadStatus.Downloaded) Color.semanticError else Color.semanticPrimaryAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (downloadStatus) {
                            is SurahDownloadStatus.Downloaded -> "Delete Offline Audio"
                            is SurahDownloadStatus.Downloading -> "Downloading (${(downloadStatus.progress * 100).toInt()}%)"
                            else -> "Download Audio for Offline Use"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (downloadStatus) {
                            is SurahDownloadStatus.Downloaded -> "Remove downloaded MP3 files from device storage"
                            is SurahDownloadStatus.Downloading -> "Downloading ${downloadStatus.downloadedVerses} of ${downloadStatus.totalVerses} verses..."
                            else -> "Save recitation to disk for 100% offline listening"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action 3: Jump to Last Read Verse (if history exists)
            if (savedVerseIndex != null) {
                val verseNum = (savedVerseIndex + 1).coerceAtMost(surah.versesCount)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onJumpToLastRead()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Jump to Last Read Verse",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Resume at Verse $verseNum of ${surah.versesCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun Int.toArabicIndicDigits(): String {
    val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val sb = StringBuilder(str.length)
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(arabicIndicDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

@Composable
fun VerseNumberBadge(
    verseNumber: Int,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val accent = Color.semanticPrimaryAccent
    val arabicNumeral = remember(verseNumber) { verseNumber.toArabicIndicDigits() }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = if (isDark) 0.12f else 0.08f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = if (isDark) 0.5f else 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = arabicNumeral,
            fontFamily = AmiriFont,
            fontSize = if (arabicNumeral.length > 2) 10.5.sp else 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-1).dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BismillahHeader(
    verse: Verse,
    fontSizeSp: Float,
    showTranslation: Boolean,
    isPlaying: Boolean,
    isVerseActive: Boolean,
    onOpenLens: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val accent = if (isDark) Color(0xFF494556) else Color(0xFF8D6B1E)
    val textPrimary = Color.semanticPrimaryText
    val isReducedMotion = rememberIsReducedMotion()

    val hasBeenSeen = rememberSaveable(verse.verseKey) { mutableStateOf(false) }
    val animProgress = remember { Animatable(if (isReducedMotion || hasBeenSeen.value) 1f else 0f) }

    LaunchedEffect(verse.verseKey) {
        if (!isReducedMotion && !hasBeenSeen.value) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 230, easing = FastOutSlowInEasing)
            )
            hasBeenSeen.value = true
        } else {
            animProgress.snapTo(1f)
        }
    }

    val motionModifier = if (isReducedMotion || (hasBeenSeen.value && animProgress.value == 1f)) {
        Modifier
    } else {
        Modifier.graphicsLayer {
            alpha = animProgress.value
            translationY = (1f - animProgress.value) * 12.dp.toPx()
        }
    }

    val activeAlpha by animateFloatAsState(
        targetValue = if (isVerseActive) 1f else 0f,
        animationSpec = if (isReducedMotion) snap() else tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "bismillahActiveHighlight"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(motionModifier)
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                if (activeAlpha > 0.005f) {
                    val bgTint = if (isDark) Color(0xFF494556).copy(alpha = 0.20f * activeAlpha) else Color(0xFF8D6B1E).copy(alpha = 0.10f * activeAlpha)
                    drawRoundRect(
                        color = bgTint,
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                    val barWidth = 3.5.dp.toPx()
                    val barMargin = 8.dp.toPx()
                    drawRoundRect(
                        color = (if (isDark) Color(0xFFE2E0EC) else Color(0xFF8D6B1E)).copy(alpha = activeAlpha * 0.95f),
                        topLeft = Offset(4.dp.toPx(), barMargin),
                        size = Size(barWidth, (size.height - barMargin * 2).coerceAtLeast(0f)),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
            .padding(top = 20.dp, bottom = 24.dp, start = if (isVerseActive) 14.dp else 12.dp, end = 12.dp)
            .combinedClickable(
                onClick = { onOpenLens?.invoke() },
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ceremonial divider ABOVE Bismillah (tapered gradient line with diamond accent)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.50f)
                .height(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        (if (isDark) Color(0xFF8B859D) else Color(0xFF8D6B1E)).copy(alpha = if (isDark) 0.5f else 0.45f),
                        Color.Transparent
                    )
                )
                drawRect(brush = brush)
            }
            Canvas(modifier = Modifier.size(6.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height / 2f)
                    close()
                }
                drawPath(path = path, color = if (isDark) Color(0xFFB0ACC0) else Color(0xFF8D6B1E))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Prominent Arabic Bismillah text centered
        ArabicText(
            text = verse.textArabic,
            fontSize = (fontSizeSp * 1.10f).sp,
            color = textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Translation centered below it if enabled
        if (showTranslation && verse.textEnglish.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = verse.textEnglish,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                ),
                color = Color.semanticSecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Ceremonial bottom separator line (tapered divider)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.38f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            (if (isDark) Color(0xFF8B859D) else Color(0xFF8D6B1E)).copy(alpha = if (isDark) 0.35f else 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerseCard(
    verse: Verse,
    fontSizeSp: Float,
    showTranslation: Boolean,
    isNightMode: Boolean,
    isVerseActive: Boolean,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenLens: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isNightMode || isAppInDarkTheme()
    val accent = Color.semanticPrimaryAccent
    val textPrimary = Color.semanticPrimaryText
    val isReducedMotion = rememberIsReducedMotion()

    // 8. Subtle first-appearance animation in the current reading session
    val hasBeenSeen = rememberSaveable(verse.verseKey) { mutableStateOf(false) }
    val animProgress = remember { Animatable(if (isReducedMotion || hasBeenSeen.value) 1f else 0f) }

    LaunchedEffect(verse.verseKey) {
        if (!isReducedMotion && !hasBeenSeen.value) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 230, easing = FastOutSlowInEasing)
            )
            hasBeenSeen.value = true
        } else {
            animProgress.snapTo(1f)
        }
    }

    val motionModifier = if (isReducedMotion || (hasBeenSeen.value && animProgress.value == 1f)) {
        Modifier
    } else {
        Modifier.graphicsLayer {
            alpha = animProgress.value
            translationY = (1f - animProgress.value) * 12.dp.toPx()
        }
    }

    val activeAlpha by animateFloatAsState(
        targetValue = if (isVerseActive) 1f else 0f,
        animationSpec = if (isReducedMotion) snap() else tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "verseActiveHighlight"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(motionModifier)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                if (activeAlpha > 0.005f) {
                    val bgTint = accent.copy(alpha = (if (isDark) 0.14f else 0.10f) * activeAlpha)
                    drawRoundRect(
                        color = bgTint,
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                    val barWidth = 3.5.dp.toPx()
                    val barMargin = 6.dp.toPx()
                    drawRoundRect(
                        color = accent.copy(alpha = activeAlpha * 0.95f),
                        topLeft = Offset(3.dp.toPx(), barMargin),
                        size = Size(barWidth, (size.height - barMargin * 2).coerceAtLeast(0f)),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
            .combinedClickable(
                onClick = { onOpenLens?.invoke() },
                onLongClick = onLongClick
            )
            .padding(start = if (isVerseActive) 14.dp else 6.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
    ) {
        // Verse Header: Arabic-Indic Number Badge on Start, Action Icons on End
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3. Small circular verse-number badge with Arabic-Indic numerals
            VerseNumberBadge(verseNumber = verse.verseNumber)

            // 2. Action Icons Hierarchy: Play prioritized, Search & Bookmark subtle
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Button (Prioritized with subtle circular accent background)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onPlayAudio),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying) accent
                                else accent.copy(alpha = if (isDark) 0.18f else 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) togetherWith
                                        fadeOut(animationSpec = tween(140))
                            },
                            label = "versePlayPauseAnim"
                        ) { activePlaying ->
                            Icon(
                                imageVector = if (activePlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (activePlaying) "Pause Audio" else "Play Audio",
                                tint = if (activePlaying) Color.White else accent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Quran Lens / Search (50-60% opacity, 18dp icon, 48dp touch target)
                if (onOpenLens != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onOpenLens),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Quran Lens",
                            tint = textPrimary.copy(alpha = 0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bookmark (Accurate state: filled accent when bookmarked, 55% outlined when not)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onToggleBookmark),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isBookmarked,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) togetherWith
                                    fadeOut(animationSpec = tween(140))
                        },
                        label = "verseBookmarkAnim"
                    ) { activeBookmarked ->
                        Icon(
                            imageVector = if (activeBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (activeBookmarked) "Remove Bookmark" else "Bookmark",
                            tint = if (activeBookmarked) accent else textPrimary.copy(alpha = 0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Dominant Arabic Qur'an Text (RTL, large, generous line-height)
        ArabicText(
            text = verse.textArabic,
            fontSize = fontSizeSp.sp,
            color = textPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        // Translation: visually secondary, comfortable typography, vertical breathing room
        if (showTranslation && verse.textEnglish.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = verse.textEnglish,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                ),
                color = Color.semanticSecondaryText,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 5. Continuous flow divider (subtle, non-distracting)
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            color = Color.semanticBorder.copy(alpha = if (isDark) 0.25f else 0.35f),
            thickness = 0.75.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseContextMenuSheet(
    verse: Verse,
    surahName: String,
    isBookmarked: Boolean,
    onCopyArabic: () -> Unit,
    onShareVerse: () -> Unit,
    onCopyVerseWithTranslation: () -> Unit,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetBg = Color.semanticSurface
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val accent = Color.semanticPrimaryAccent

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(textSecondary.copy(alpha = 0.35f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Verse reference & Arabic snippet
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (verse.verseNumber == 0) "Bismillah" else "Surah $surahName • Verse ${verse.verseNumber}",
                        fontFamily = SerifHeaderFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Verse Actions",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (verse.verseNumber == 0) "Opening" else "${verse.surahNumber}:${verse.verseNumber}",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color.semanticBorder,
                thickness = 1.dp
            )

            // Action 1: Copy Arabic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onCopyArabic()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Copy Arabic",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = "Copies exact Arabic Uthmani text",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            // Action 2: Share Verse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onShareVerse()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Share Verse",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = "Share Arabic, translation, and reference",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            // Action 3: Copy Verse with Translation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onCopyVerseWithTranslation()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notes,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Copy Arabic & Translation",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = "Copies text with English meaning and reference",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            // Action 4: Play Audio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onPlayAudio()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Play Recitation",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = "Listen to audio recitation of this verse",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            // Action 5: Bookmark Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onToggleBookmark()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isBookmarked) accent else textSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isBookmarked) "Remove Bookmark" else "Bookmark Verse",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = if (isBookmarked) "Remove from saved bookmarks" else "Save this verse for quick access",
                        fontFamily = SpaceGrotesk,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun VerseCardSkeleton(
    isNightMode: Boolean,
    shimmerAlpha: Float,
    modifier: Modifier = Modifier
) {
    val isDark = isNightMode || isAppInDarkTheme()
    val baseColor = if (isDark) Color.White else LightPrimaryText
    val skeletonFill = baseColor.copy(alpha = shimmerAlpha)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        // Header: circular badge on left, action icons on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(skeletonFill)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(skeletonFill)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(skeletonFill)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(skeletonFill)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Arabic text block placeholders (RTL right-aligned)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(skeletonFill)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(skeletonFill)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // English translation placeholders (LTR left-aligned)
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(skeletonFill)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(skeletonFill)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            color = Color.semanticBorder.copy(alpha = 0.2f),
            thickness = 0.75.dp
        )
    }
}
