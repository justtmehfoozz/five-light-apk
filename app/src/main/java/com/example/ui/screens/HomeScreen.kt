package com.example.ui.screens

import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.predictiveBackTransform
import com.example.ui.components.rememberPredictiveBackState

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import com.example.data.util.DailyContentProvider
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.ui.theme.*
import com.example.ui.theme.PillInactiveBorder
import com.example.ui.theme.SurfaceLight
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import com.example.data.model.NaflPreferences
import com.example.data.model.NaflPrayerItem
import com.example.data.model.NaflType
import com.example.data.model.RightNowActionType
import com.example.data.model.RightNowItem
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.SerifHeaderFont
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PrayerLogEntity
import com.example.data.model.CityLocation
import com.example.data.model.HijriDate
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.util.HijriCalc
import com.example.ui.components.NavItem
import com.example.ui.components.PageHeader
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.PillActiveBg
import com.example.ui.theme.PillActiveText
import com.example.ui.theme.PillInactiveBg
import com.example.ui.theme.PillInactiveBorder
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.getPrayerGradient
import com.example.ui.theme.getPrayerGradientColors
import com.example.ui.theme.getSeasonalPrayerGradientColors
import com.example.ui.theme.getPrayerDisplayName
import com.example.ui.theme.getPrayerArabicName
import com.example.ui.theme.getPrayerPoeticSubtext
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.ArabicText
import kotlinx.coroutines.launch

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun StaggeredCardEntrance(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}

data class FilterPillItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val navItem: NavItem?
)

@Composable
fun HomeFilterPillRow(
    selectedId: String = "today",
    onSelectFilter: (FilterPillItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            FilterPillItem("today", "Today", Icons.Outlined.WbSunny, null),
            FilterPillItem("qibla", "Qibla", Icons.Outlined.Explore, NavItem.QIBLA),
            FilterPillItem("quran", "Quran", Icons.Outlined.AutoStories, NavItem.QURAN),
            FilterPillItem("tasbeeh", "Tasbeeh", Icons.Outlined.RadioButtonUnchecked, NavItem.TASBEEH)
        )
    }

    val selectedIndex = items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)

    val trackBg = Color.semanticControl
    val activePillBg = Color.semanticPrimaryAccent
    val activeContentColor = Color.semanticAccentForeground
    val inactiveContentColor = Color.semanticSecondaryText

    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE, 1f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }

    val startSpec = if (isReducedMotion) snap() else spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val endSpec = if (isReducedMotion) snap() else spring<Float>(
        dampingRatio = 0.55f,
        stiffness = Spring.StiffnessLow
    )

    val animatedStartIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = startSpec,
        label = "filterStart"
    )

    val animatedEndIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = endSpec,
        label = "filterEnd"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(CircleShape)
            .background(trackBg)
            .border(1.dp, Color.semanticBorder, CircleShape)
            .padding(4.dp)
            .testTag("home_filter_pill_row")
    ) {
        val totalWidth = maxWidth
        val segmentWidth = totalWidth / items.size.toFloat()

        val startVal = minOf(animatedStartIndex, animatedEndIndex)
        val endVal = maxOf(animatedStartIndex, animatedEndIndex)

        val leftPos = (startVal * segmentWidth.value).dp
        val rightPos = ((endVal + 1f) * segmentWidth.value).dp
        val indicatorWidth = (rightPos - leftPos).coerceAtLeast(segmentWidth)

        Box(
            modifier = Modifier
                .offset(x = leftPos)
                .width(indicatorWidth)
                .height(40.dp)
                .clip(CircleShape)
                .background(activePillBg)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.id == selectedId

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(CircleShape)
                        .clickable { onSelectFilter(item) }
                        .testTag("filter_pill_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) activeContentColor else inactiveContentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.label,
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activeContentColor else inactiveContentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Refined Home Header Date Presentation
 * Location (small muted) -> Gregorian Date (small/medium primary with midnight transition)
 * -> Hijri Date (small secondary with maghrib transition and subtle "After Maghrib" indicator)
 */
@Composable
fun HomeHeaderDateSection(
    locationName: String,
    islamicDateState: com.example.data.model.IslamicDateState,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val durationScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            val transitionScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            )
            durationScale == 0f || transitionScale == 0f
        } catch (_: Throwable) {
            false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_header_date_section"),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 1. Location: small muted text
        if (locationName.isNotBlank()) {
            Text(
                text = locationName,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("header_location_text")
            )
        }

        // 2. Gregorian Date: small/medium primary or slightly emphasized text with Midnight transition
        val gregorianText = islamicDateState.gregorianDateFormatted.ifEmpty { "Today" }
        AnimatedContent(
            targetState = gregorianText,
            transitionSpec = {
                if (isReducedMotion) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                            slideInVertically(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) { (it * 0.25f).toInt().coerceAtLeast(4) })
                        .togetherWith(
                            fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                                    slideOutVertically(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) { -(it * 0.25f).toInt().coerceAtLeast(4) }
                        )
                }
            },
            label = "gregorian_date_transition"
        ) { targetGregorian ->
            Text(
                text = targetGregorian,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("gregorian_date_text")
            )
        }

        // 3. Hijri Date: small muted/secondary text with subtle "After Maghrib" indicator
        val hijriFormatted = if (islamicDateState.hijriDateFormatted.isNotEmpty()) {
            islamicDateState.hijriDateFormatted
        } else {
            HijriCalc.formatHijriString(islamicDateState.hijriDate)
        }
        val isAfterMaghrib = islamicDateState.isAfterMaghrib

        AnimatedContent(
            targetState = Pair(hijriFormatted, isAfterMaghrib),
            transitionSpec = {
                if (isReducedMotion) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) +
                            slideInVertically(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) { (it * 0.2f).toInt().coerceAtLeast(3) })
                        .togetherWith(
                            fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) +
                                    slideOutVertically(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) { -(it * 0.2f).toInt().coerceAtLeast(3) }
                        )
                }
            },
            label = "hijri_date_transition"
        ) { (hijriText, afterMaghrib) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hijriText,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("hijri_date_text")
                )

                if (afterMaghrib) {
                    Text(
                        text = "·",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "After Maghrib",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("after_maghrib_indicator")
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    nextPrayer: PrayerItem?,
    prayerTimes: List<PrayerItem>,
    allPrayerLogs: List<PrayerLogEntity> = emptyList(),
    qadaCounts: Map<PrayerName, Int> = emptyMap(),
    qadaEverAdded: Map<PrayerName, Boolean> = emptyMap(),
    onUpdateQadaCount: (PrayerName, Int) -> Unit = { _, _ -> },
    onMakeUpQadaPrayer: (PrayerName) -> Unit = {},
    countdownFormatted: String,
    selectedCity: CityLocation,
    hijriDate: HijriDate,
    islamicDateState: com.example.data.model.IslamicDateState = com.example.data.model.IslamicDateState(),
    todayLog: PrayerLogEntity?,
    naflPreferences: NaflPreferences = NaflPreferences(),
    naflPrayerItems: List<NaflPrayerItem> = emptyList(),
    rightNowItem: RightNowItem? = null,
    contextState: com.example.data.model.FiveLightContextState = com.example.data.model.FiveLightContextState(),
    lastReadPosition: com.example.data.model.QuranLastRead? = null,
    recentlyReadList: List<com.example.data.model.QuranLastRead> = emptyList(),
    prayerJourneyNodes: List<com.example.data.model.PrayerJourneyNode> = emptyList(),
    homeFeaturesPreferences: com.example.data.model.HomeFeaturesPreferences = com.example.data.model.HomeFeaturesPreferences(),
    showPrayerMode: PrayerItem? = null,
    isFriday: Boolean = false,
    onClosePrayerMode: () -> Unit = {},
    onNavigateToQuranSurahVerse: (Int, Int) -> Unit = { _, _ -> },
    onTogglePrayer: (PrayerName) -> Unit,
    onSetPrayerStatus: (PrayerName, com.example.data.model.PrayerStatus) -> Unit = { _, _ -> },
    onSetPrayerStatusWithDate: (PrayerName, String, com.example.data.model.PrayerStatus) -> Unit = { _, _, _ -> },
    onSavePrayerNote: (PrayerName, String, String?) -> Unit = { _, _, _ -> },
    onAddPrayerToQada: (PrayerName, String) -> Unit = { _, _ -> },
    onQuickAccessNavigate: (NavItem) -> Unit,
    onOpenSettings: () -> Unit,
    isActiveTab: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val coroutineScope = rememberCoroutineScope()
    val todayStr = remember(hijriDate, System.currentTimeMillis() / 60000L) { java.time.LocalDate.now().toString() }

    var showDuaModal by remember { mutableStateOf(false) }
    var showHadithModal by remember { mutableStateOf(false) }
    var showPrayerJourneySheet by remember { mutableStateOf(false) }
    var showPersonalLogSheet by remember { mutableStateOf(false) }
    var activeEvidenceNaflType by remember { mutableStateOf<NaflType?>(null) }

    val homePredictiveState = rememberPredictiveBackState()
    val isHomeOverlayActive = showPrayerMode != null || showPrayerJourneySheet || showPersonalLogSheet || showDuaModal || showHadithModal || activeEvidenceNaflType != null

    RegisterPredictiveBackHandler(
        enabled = isActiveTab && isHomeOverlayActive,
        backState = homePredictiveState,
        onBack = {
            if (showPrayerMode != null) {
                onClosePrayerMode()
            } else if (showPrayerJourneySheet) {
                showPrayerJourneySheet = false
            } else if (showPersonalLogSheet) {
                showPersonalLogSheet = false
            } else if (showDuaModal) {
                showDuaModal = false
            } else if (showHadithModal) {
                showHadithModal = false
            } else if (activeEvidenceNaflType != null) {
                activeEvidenceNaflType = null
            }
        }
    )

    // DEDUPLICATION 1: Filter Recently Read to exclude the primary location shown in Continue Reading
    val filteredRecentlyRead = remember(recentlyReadList, lastReadPosition) {
        if (lastReadPosition != null) {
            recentlyReadList.filterNot {
                it.surahNumber == lastReadPosition.surahNumber && it.verseNumber == lastReadPosition.verseNumber
            }
        } else {
            recentlyReadList
        }
    }

    // DEDUPLICATION 2: Ensure Next Opportunity does not duplicate active Right Now item or Prayer Prep
    val showNextOpportunity = remember(contextState.nextOpportunity, rightNowItem, contextState.prayerPrep) {
        val nextOpp = contextState.nextOpportunity ?: return@remember null
        if (rightNowItem != null && (
            nextOpp.title.equals(rightNowItem.title, ignoreCase = true) ||
            (nextOpp.title.contains("Adhkar", ignoreCase = true) && rightNowItem.title.contains("Adhkar", ignoreCase = true)) ||
            (nextOpp.title.contains("Tahajjud", ignoreCase = true) && rightNowItem.title.contains("Tahajjud", ignoreCase = true))
        )) {
            null
        } else {
            nextOpp
        }
    }

    androidx.compose.foundation.lazy.LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER
        item {
            PageHeader(
                title = "FiveLight",
                bottomPadding = 0.dp,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onOpenSettings() }
                            .testTag("header_search_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                subtitleContent = {
                    HomeHeaderDateSection(
                        locationName = selectedCity.fullDisplayName,
                        islamicDateState = islamicDateState
                    )
                }
            )
        }

        // 2. CONTEXTUAL CHIPS ROW
        item {
            val chipScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionChip(
                    label = "Prayer",
                    icon = Icons.Outlined.WbSunny,
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(3)
                        }
                    }
                )
                QuickActionChip(
                    label = "Prayer Journey",
                    icon = Icons.Filled.Explore,
                    onClick = { showPrayerJourneySheet = true }
                )
                QuickActionChip(
                    label = "Dua of the Day",
                    icon = Icons.Outlined.AutoAwesome,
                    onClick = { showDuaModal = true }
                )
                QuickActionChip(
                    label = "Hadith of the Day",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    onClick = { showHadithModal = true }
                )
                QuickActionChip(
                    label = "Adhkar",
                    icon = Icons.Outlined.RadioButtonUnchecked,
                    onClick = { onQuickAccessNavigate(NavItem.TASBEEH) }
                )
            }
        }

        // 3. CURRENT / NEXT PRAYER HIGHLIGHT HERO CARD
        val heroPrayer = nextPrayer ?: prayerTimes.firstOrNull()
        var cardIndex = 0

        if (heroPrayer != null) {
            val heroStatus = com.example.data.db.PrayerLogEntity.resolvePrayerStatus(todayLog, heroPrayer.name, heroPrayer.timeMillis, todayStr, todayStr)
            val currentIdx = cardIndex++

            item {
                StaggeredCardEntrance(index = currentIdx) {
                    AnimatedContent(
                        targetState = heroPrayer.name,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "hero_card_transition"
                    ) { _ ->
                        FeaturedPrayerHeroCard(
                            prayer = heroPrayer,
                            countdown = countdownFormatted,
                            status = heroStatus,
                            onToggle = { onTogglePrayer(heroPrayer.name) },
                            onSetStatus = { s -> onSetPrayerStatus(heroPrayer.name, s) },
                            isFriday = isFriday,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }

        // 4. TODAY'S FIVE PRAYERS 2-COLUMN GRID
        val dailyPrayersIdx = cardIndex++
        item {
            StaggeredCardEntrance(index = dailyPrayersIdx) {
                val dailyPrayers = prayerTimes.filter { it.name != PrayerName.SUNRISE }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Prayers",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Tap circle or long-press",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val chunkedPrayers = dailyPrayers.chunked(2)
                    chunkedPrayers.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            pair.forEach { prayer ->
                                val status = com.example.data.db.PrayerLogEntity.resolvePrayerStatus(todayLog, prayer.name, prayer.timeMillis, todayStr, todayStr)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    PrayerGridCard(
                                        prayer = prayer,
                                        status = status,
                                        onToggle = { onTogglePrayer(prayer.name) },
                                        onSetStatus = { s -> onSetPrayerStatus(prayer.name, s) },
                                        isFriday = isFriday,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 5. REORDERABLE HOME FEATURES & NAFL PRAYERS
        homeFeaturesPreferences.featureOrder.forEach { featureKey ->
            when (featureKey.uppercase()) {
                "PRAYER_PREP" -> {
                    if (contextState.prayerPrep != null && homeFeaturesPreferences.prayerPrepEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    com.example.ui.components.PrayerPrepCard(
                                        prep = contextState.prayerPrep,
                                        onQiblaClick = { onQuickAccessNavigate(NavItem.QIBLA) }
                                    )
                                }
                            }
                        }
                    }
                }
                "RIGHT_NOW" -> {
                    if (rightNowItem != null && homeFeaturesPreferences.rightNowEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    RightNowCard(
                                        item = rightNowItem,
                                        onActionClick = {
                                            when (rightNowItem.actionType) {
                                                RightNowActionType.OPEN_ADHKAR -> onQuickAccessNavigate(NavItem.TASBEEH)
                                                RightNowActionType.OPEN_QURAN -> onQuickAccessNavigate(NavItem.QURAN)
                                                RightNowActionType.VIEW_PRAYER -> {
                                                    rightNowItem.naflType?.let { activeEvidenceNaflType = it }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                "CONTINUE_READING" -> {
                    if (lastReadPosition != null && homeFeaturesPreferences.continueReadingEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    com.example.ui.components.ContinueReadingCard(
                                        lastRead = lastReadPosition,
                                        onContinueClick = {
                                            onNavigateToQuranSurahVerse(
                                                lastReadPosition.surahNumber,
                                                lastReadPosition.verseNumber
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                "NEXT_OPPORTUNITY" -> {
                    if (showNextOpportunity != null && homeFeaturesPreferences.nextOpportunityEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    com.example.ui.components.NextOpportunityCard(
                                        item = showNextOpportunity,
                                        onActionClick = {
                                            when (showNextOpportunity.actionType) {
                                                RightNowActionType.OPEN_ADHKAR -> onQuickAccessNavigate(NavItem.TASBEEH)
                                                RightNowActionType.OPEN_QURAN -> onQuickAccessNavigate(NavItem.QURAN)
                                                else -> onQuickAccessNavigate(NavItem.HOME)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                "RECENTLY_READ" -> {
                    if (recentlyReadList.isNotEmpty() && homeFeaturesPreferences.recentlyReadEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                com.example.ui.components.RecentlyReadSection(
                                    recentlyReadList = recentlyReadList,
                                    onSelectReadItem = { surah, verseIdx ->
                                        onNavigateToQuranSurahVerse(surah, verseIdx + 1)
                                    }
                                )
                            }
                        }
                    }
                }
                "TONIGHT" -> {
                    if (contextState.tonight != null && contextState.tonight.isNightActive && homeFeaturesPreferences.tonightEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    com.example.ui.components.TonightCard(
                                        tonight = contextState.tonight
                                    )
                                }
                            }
                        }
                    }
                }
                "MOMENTS" -> {
                    if (contextState.moment != null && homeFeaturesPreferences.momentsEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    com.example.ui.components.FiveLightMomentCard(
                                        moment = contextState.moment,
                                        onActionClick = {
                                            when (contextState.moment.actionType) {
                                                RightNowActionType.OPEN_QURAN -> onQuickAccessNavigate(NavItem.QURAN)
                                                RightNowActionType.OPEN_ADHKAR -> onQuickAccessNavigate(NavItem.TASBEEH)
                                                else -> onQuickAccessNavigate(NavItem.HOME)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                "WEEKLY_OVERVIEW" -> {
                    if (contextState.weeklyOverview != null && homeFeaturesPreferences.weeklyOverviewEnabled) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    com.example.ui.components.WeeklyWorshipOverviewCard(
                                        overview = contextState.weeklyOverview,
                                        onPrayerCellClick = { prayerName, dateStr, currentStatus ->
                                            val nextStatus = when (currentStatus) {
                                                com.example.data.model.PrayerStatus.PRAYED -> com.example.data.model.PrayerStatus.NEEDS_INPUT
                                                com.example.data.model.PrayerStatus.MISSED -> com.example.data.model.PrayerStatus.PRAYED
                                                com.example.data.model.PrayerStatus.NEEDS_INPUT -> com.example.data.model.PrayerStatus.PRAYED
                                                com.example.data.model.PrayerStatus.FUTURE -> com.example.data.model.PrayerStatus.FUTURE
                                            }
                                            if (nextStatus != com.example.data.model.PrayerStatus.FUTURE) {
                                                onSetPrayerStatusWithDate(prayerName, dateStr, nextStatus)
                                            }
                                        },
                                        onOpenPersonalLog = { showPersonalLogSheet = true }
                                    )
                                }
                            }
                        }
                    }
                }
                "NAFL_PRAYERS" -> {
                    if (naflPreferences.isAnyEnabled && naflPrayerItems.isNotEmpty()) {
                        val cIdx = cardIndex++
                        item {
                            StaggeredCardEntrance(index = cIdx) {
                                NaflPrayersSection(
                                    naflPrayerItems = naflPrayerItems,
                                    onOpenEvidence = { type -> activeEvidenceNaflType = type }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ISLAMIC CALENDAR EVENT MOMENT CARD
        if (contextState.calendarMoment != null) {
            val cIdx = cardIndex++
            item {
                StaggeredCardEntrance(index = cIdx) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        com.example.ui.components.CalendarEventMomentCard(
                            eventMoment = contextState.calendarMoment
                        )
                    }
                }
            }
        }

        // REFLECTION OF THE DAY CARD
        val refIdx = cardIndex++
        item {
            StaggeredCardEntrance(index = refIdx) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    com.example.ui.components.ReflectionOfTheDayCard(
                        onReflectClick = { surahNum, verseNum ->
                            onNavigateToQuranSurahVerse(surahNum, verseNum)
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(140.dp)) }
    }

    if (showPrayerJourneySheet) {
        com.example.ui.components.PrayerJourneySheet(
            nodes = prayerJourneyNodes,
            onTogglePrayer = onTogglePrayer,
            onSetPrayerStatus = onSetPrayerStatus,
            onDismiss = { showPrayerJourneySheet = false }
        )
    }

    if (showPersonalLogSheet) {
        com.example.ui.components.PersonalLogSheet(
            overview = contextState.weeklyOverview,
            todayLog = todayLog,
            allPrayerLogs = allPrayerLogs,
            qadaCounts = qadaCounts,
            qadaEverAdded = qadaEverAdded,
            onUpdateQadaCount = onUpdateQadaCount,
            onMakeUpQadaPrayer = onMakeUpQadaPrayer,
            onSetPrayerStatus = { pName, dStr, pStatus ->
                onSetPrayerStatusWithDate(pName, dStr, pStatus)
            },
            onSavePrayerNote = onSavePrayerNote,
            onAddPrayerToQada = onAddPrayerToQada,
            onDismiss = { showPersonalLogSheet = false }
        )
    }

    if (showPrayerMode != null) {
        com.example.ui.components.PrayerModeModal(
            prayer = showPrayerMode,
            onDismiss = onClosePrayerMode,
            onQiblaClick = { onQuickAccessNavigate(NavItem.QIBLA) }
        )
    }

    if (activeEvidenceNaflType != null) {
        NaflEvidenceModalBottomSheet(
            naflType = activeEvidenceNaflType!!,
            onDismiss = { activeEvidenceNaflType = null }
        )
    }

    val todayDua = remember(todayStr) { DailyContentProvider.getDuaForDate() }
    val todayHadith = remember(todayStr) { DailyContentProvider.getHadithForDate() }

    if (showDuaModal) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDuaModal = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dua of the Day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { showDuaModal = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ArabicText(
                        text = todayDua.arabic,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = todayDua.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = todayDua.translation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = todayDua.reference,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showHadithModal) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showHadithModal = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hadith of the Day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { showHadithModal = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ArabicText(
                        text = todayHadith.arabic,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = todayHadith.translation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = todayHadith.narrator,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = todayHadith.reference,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = todayHadith.grade,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "chipScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "chipAlpha"
    )

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }

    // Elongated rounded-pill shape surface colors
    val pillBg = if (isDark) {
        if (isPressed) com.example.ui.theme.SurfaceVariantDark else com.example.ui.theme.SurfaceDark
    } else {
        if (isPressed) com.example.ui.theme.SurfaceVariantLight else com.example.ui.theme.SurfaceLight
    }

    // Clearly visible outer boundary
    val outerBorder = if (isDark) {
        com.example.ui.theme.BorderDark
    } else {
        com.example.ui.theme.BorderLight
    }

    // Subtle inner highlight stroke
    val innerHighlight = if (isDark) {
        com.example.ui.theme.TextPrimary.copy(alpha = 0.17f)
    } else {
        com.example.ui.theme.TextPrimaryLight.copy(alpha = 0.25f)
    }

    // Clean, high contrast text and icon color
    val contentColor = if (isDark) {
        com.example.ui.theme.TextPrimary
    } else {
        com.example.ui.theme.TextPrimaryLight
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .shadow(
                elevation = if (isDark) 2.dp else 1.dp,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(pillBg)
            .border(
                border = BorderStroke(1.dp, outerBorder),
                shape = CircleShape
            )
            .border(
                border = BorderStroke(0.5.dp, innerHighlight),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .semantics {
                contentDescription = label
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedPrayerHeroCard(
    prayer: PrayerItem,
    countdown: String,
    status: com.example.data.model.PrayerStatus,
    onToggle: () -> Unit,
    onSetStatus: (com.example.data.model.PrayerStatus) -> Unit = {},
    isFriday: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    var showMenu by remember { mutableStateOf(false) }

    // Reduced motion accessibility check
    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val durationScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            val transitionScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            durationScale == 0f || transitionScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // Base color transition when active prayer changes
    val (targetStart, targetEnd) = getPrayerGradientColors(prayer.name, isFriday)
    val animStartColor by animateColorAsState(
        targetValue = targetStart,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "hero_bg_start"
    )
    val animEndColor by animateColorAsState(
        targetValue = targetEnd,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "hero_bg_end"
    )

    // Feature 1: Ambient Breathing Gradient Drift (8.5s slow continuous loop, 3-5% subtle variation)
    val infiniteTransition = rememberInfiniteTransition(label = "hero_alive_transitions")
    val driftProgress by if (!isReducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hero_gradient_drift"
        )
    } else {
        rememberUpdatedState(0f)
    }

    // Feature 2: Last 15 Minutes Urgency State
    val isUrgent = remember(prayer.timeMillis, countdown) {
        val now = System.currentTimeMillis()
        val diff = prayer.timeMillis - now
        val clean = countdown.removePrefix("-")
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val stringMatches = h == 0 && m <= 15
        (diff in 1..(15 * 60 * 1000 + 999)) || (stringMatches && clean != "00:00:00" && clean != "00")
    }

    // Subtle 6% size emphasis when <=15m (350ms smooth transition)
    val urgencyBaseScale by animateFloatAsState(
        targetValue = if (isUrgent) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "hero_urgency_scale"
    )

    // Calm heartbeat pulse (1.00 -> 1.025 -> 1.00 over 1.7s cycle, low amplitude)
    val heartbeatPulse by if (isUrgent && !isReducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hero_heartbeat_pulse"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .drawWithContent {
                val currentDrift = driftProgress
                val subtleStart = lerp(animStartColor, animEndColor, 0.04f * currentDrift)
                val subtleEnd = lerp(animEndColor, animStartColor, 0.05f * (1f - currentDrift))
                val subtleMid = lerp(animStartColor, animEndColor, 0.5f + 0.03f * (currentDrift - 0.5f))

                // Feature 1: Ambient breathing background gradient with subtle angle/offset drift
                val startX = size.width * (0.00f + 0.04f * currentDrift)
                val startY = size.height * (0.00f - 0.03f * currentDrift)
                val endX = size.width * (1.00f - 0.04f * (1f - currentDrift))
                val endY = size.height * (1.00f + 0.03f * (1f - currentDrift))
                val brush = Brush.linearGradient(
                    colors = listOf(subtleStart, subtleMid, subtleEnd),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                )
                drawRect(brush = brush)

                drawContent()

                // Top-left radial white highlight overlay
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .padding(20.dp)
            .testTag("featured_hero_card")
            .testTag("today_card")
    ) {
        // Decorative top-right pin dot accent
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
                .align(Alignment.TopEnd)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HandDrawnSkyIcon(prayerName = prayer.name, isFriday = isFriday)
                    Spacer(modifier = Modifier.width(10.dp))
                    AnimatedContent(
                        targetState = Pair(getPrayerDisplayName(prayer.name, isFriday), getPrayerArabicName(prayer.name, isFriday)),
                        transitionSpec = {
                            fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith fadeOut(tween(500, easing = FastOutSlowInEasing))
                        },
                        label = "hero_title_transition"
                    ) { (displayName, arabicName) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = arabicName,
                                fontFamily = AmiriFont,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                // Next Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Next",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = getPrayerPoeticSubtext(prayer.name, isFriday),
                transitionSpec = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                label = "hero_subtext_transition"
            ) { subtext ->
                Text(
                    text = subtext,
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.68f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Countdown Centerpiece
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "PRAYER IN",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    val cleanCountdown = countdown.removePrefix("-")
                    val parts = cleanCountdown.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    val hoursStr = parts.getOrNull(0) ?: "00"
                    val minutesStr = parts.getOrNull(1) ?: "00"
                    val secondsStr = parts.getOrNull(2) ?: cleanCountdown

                    val showHours = h > 0
                    val showMinutes = h > 0 || m > 0

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.graphicsLayer {
                            val totalCountdownScale = urgencyBaseScale * heartbeatPulse
                            scaleX = totalCountdownScale
                            scaleY = totalCountdownScale
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        }
                    ) {
                        AnimatedVisibility(
                            visible = showHours,
                            enter = fadeIn(tween(400)) + expandHorizontally(tween(400), expandFrom = Alignment.Start),
                            exit = fadeOut(tween(400)) + shrinkHorizontally(tween(400), shrinkTowards = Alignment.Start)
                        ) {
                            Text(
                                text = "$hoursStr:",
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isUrgent) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.White
                            )
                        }
                        AnimatedVisibility(
                            visible = showMinutes,
                            enter = fadeIn(tween(400)) + expandHorizontally(tween(400), expandFrom = Alignment.Start),
                            exit = fadeOut(tween(400)) + shrinkHorizontally(tween(400), shrinkTowards = Alignment.Start)
                        ) {
                            Text(
                                text = "$minutesStr:",
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isUrgent) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = secondsStr,
                            fontFamily = SpaceGrotesk,
                            fontWeight = if (isUrgent) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                }

                // Completion Toggle Button & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        val isPrayed = status == com.example.data.model.PrayerStatus.PRAYED
                        val isMissed = status == com.example.data.model.PrayerStatus.MISSED
                        val isFuture = status == com.example.data.model.PrayerStatus.FUTURE

                        val targetBgColor = when {
                            isPrayed -> Color.White
                            isMissed -> Color.semanticError
                            else -> Color.Transparent
                        }
                        val animatedBgColor by animateColorAsState(
                            targetValue = targetBgColor,
                            animationSpec = tween(durationMillis = 300),
                            label = "hero_status_bg"
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(animatedBgColor)
                                .border(
                                    border = when {
                                        isPrayed || isMissed -> BorderStroke(0.dp, Color.Transparent)
                                        isFuture -> BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f))
                                        else -> BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f))
                                    },
                                    shape = CircleShape
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (!isFuture) {
                                            onToggle()
                                        }
                                    },
                                    onLongClick = {
                                        if (!isFuture) {
                                            showMenu = true
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = status,
                                transitionSpec = {
                                    fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                                },
                                label = "hero_status_icon"
                            ) { targetStatus ->
                                when (targetStatus) {
                                    com.example.data.model.PrayerStatus.PRAYED -> {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Completed",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    com.example.data.model.PrayerStatus.MISSED -> {
                                        Text(
                                            text = "!",
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                    else -> {
                                        Spacer(modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✓ Prayed") },
                                onClick = {
                                    showMenu = false
                                    onSetStatus(com.example.data.model.PrayerStatus.PRAYED)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("! Missed") },
                                onClick = {
                                    showMenu = false
                                    onSetStatus(com.example.data.model.PrayerStatus.MISSED)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("○ Reset (Needs input)") },
                                onClick = {
                                    showMenu = false
                                    onSetStatus(com.example.data.model.PrayerStatus.NEEDS_INPUT)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = prayer.timeFormatted,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrayerGridCard(
    prayer: PrayerItem,
    status: com.example.data.model.PrayerStatus,
    onToggle: () -> Unit,
    onSetStatus: (com.example.data.model.PrayerStatus) -> Unit = {},
    isFriday: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    var showMenu by remember { mutableStateOf(false) }

    val isPrayed = status == com.example.data.model.PrayerStatus.PRAYED
    val isMissed = status == com.example.data.model.PrayerStatus.MISSED
    val isFuture = status == com.example.data.model.PrayerStatus.FUTURE

    val (targetStart, targetEnd) = getSeasonalPrayerGradientColors(prayer.name, prayer.timeMillis, isFriday)
    val animStartColor by animateColorAsState(
        targetValue = targetStart,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "grid_bg_start"
    )
    val animEndColor by animateColorAsState(
        targetValue = targetEnd,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "grid_bg_end"
    )
    val gradientBrush = remember(animStartColor, animEndColor) {
        Brush.linearGradient(listOf(animStartColor, animEndColor))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBrush)
            .drawWithContent {
                drawContent()
                // Radial highlight overlay top-left
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .combinedClickable(
                onClick = {
                    if (!isFuture) {
                        onToggle()
                    }
                },
                onLongClick = {
                    if (!isFuture) {
                        showMenu = true
                    }
                }
            )
            .padding(16.dp)
            .testTag("prayer_card_${prayer.name.id}")
    ) {
        // Top-Right Pin Dot Accent
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
                .align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                HandDrawnSkyIcon(prayerName = prayer.name, isFriday = isFriday, modifier = Modifier.size(24.dp))

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = Pair(getPrayerDisplayName(prayer.name, isFriday), getPrayerArabicName(prayer.name, isFriday)),
                    transitionSpec = {
                        fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith fadeOut(tween(500, easing = FastOutSlowInEasing))
                    },
                    label = "grid_title_transition"
                ) { (displayName, arabicName) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = arabicName,
                            fontFamily = AmiriFont,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.62f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = getPrayerPoeticSubtext(prayer.name, isFriday),
                    transitionSpec = {
                        fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith fadeOut(tween(500, easing = FastOutSlowInEasing))
                    },
                    label = "grid_subtext_transition"
                ) { subtext ->
                    Text(
                        text = subtext,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Checkmark Badge + Prayer Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    val targetBgColor = when {
                        isPrayed -> Color.White
                        isMissed -> Color.semanticError
                        else -> Color.Transparent
                    }
                    val animatedBgColor by animateColorAsState(
                        targetValue = targetBgColor,
                        animationSpec = tween(durationMillis = 300),
                        label = "grid_status_bg"
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(animatedBgColor)
                            .border(
                                border = when {
                                    isPrayed || isMissed -> BorderStroke(0.dp, Color.Transparent)
                                    isFuture -> BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f))
                                    else -> BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f))
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                if (!isFuture) {
                                    onToggle()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = status,
                            transitionSpec = {
                                fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                            },
                            label = "grid_status_icon"
                        ) { targetStatus ->
                            when (targetStatus) {
                                com.example.data.model.PrayerStatus.PRAYED -> {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Completed",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                com.example.data.model.PrayerStatus.MISSED -> {
                                    Text(
                                        text = "!",
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                                else -> {
                                    Spacer(modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("✓ Prayed") },
                            onClick = {
                                showMenu = false
                                onSetStatus(com.example.data.model.PrayerStatus.PRAYED)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("! Missed") },
                            onClick = {
                                showMenu = false
                                onSetStatus(com.example.data.model.PrayerStatus.MISSED)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("○ Reset (Needs input)") },
                            onClick = {
                                showMenu = false
                                onSetStatus(com.example.data.model.PrayerStatus.NEEDS_INPUT)
                            }
                        )
                    }
                }

                Text(
                    text = prayer.timeFormatted,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun HandDrawnSkyIcon(
    prayerName: PrayerName,
    isFriday: Boolean = false,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)

        when (prayerName) {
            PrayerName.FAJR, PrayerName.SUNRISE -> {
                drawLine(color, Offset(w * 0.15f, h * 0.75f), Offset(w * 0.85f, h * 0.75f), strokeWidth = stroke.width, cap = stroke.cap)
                drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.3f, h * 0.4f), size = Size(w * 0.4f, h * 0.4f), style = stroke)
                drawLine(color, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.3f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(color, Offset(w * 0.28f, h * 0.36f), Offset(w * 0.36f, h * 0.42f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(color, Offset(w * 0.72f, h * 0.36f), Offset(w * 0.64f, h * 0.42f), strokeWidth = stroke.width, cap = stroke.cap)
            }
            PrayerName.DHUHR -> {
                if (isFriday) {
                    // Jummah Mosque / Minaret Line-art
                    // 1. Base line
                    drawLine(color, Offset(w * 0.12f, h * 0.84f), Offset(w * 0.88f, h * 0.84f), strokeWidth = stroke.width, cap = stroke.cap)

                    // 2. Central Dome
                    val domePath = Path().apply {
                        moveTo(w * 0.30f, h * 0.84f)
                        lineTo(w * 0.30f, h * 0.54f)
                        cubicTo(
                            w * 0.30f, h * 0.36f,
                            w * 0.44f, h * 0.24f,
                            w * 0.50f, h * 0.18f
                        )
                        cubicTo(
                            w * 0.56f, h * 0.24f,
                            w * 0.70f, h * 0.36f,
                            w * 0.70f, h * 0.54f
                        )
                        lineTo(w * 0.70f, h * 0.84f)
                    }
                    drawPath(domePath, color, style = stroke)

                    // 3. Central Dome Top Spire
                    drawLine(color, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.10f), strokeWidth = stroke.width, cap = stroke.cap)

                    // 4. Central Arch Doorway
                    val archDoorPath = Path().apply {
                        moveTo(w * 0.42f, h * 0.84f)
                        lineTo(w * 0.42f, h * 0.68f)
                        cubicTo(
                            w * 0.42f, h * 0.58f,
                            w * 0.58f, h * 0.58f,
                            w * 0.58f, h * 0.68f
                        )
                        lineTo(w * 0.58f, h * 0.84f)
                    }
                    drawPath(archDoorPath, color, style = stroke)

                    // 5. Left & Right Minaret Pillars & Spires
                    // Left minaret
                    drawLine(color, Offset(w * 0.18f, h * 0.84f), Offset(w * 0.18f, h * 0.34f), strokeWidth = stroke.width, cap = stroke.cap)
                    drawLine(color, Offset(w * 0.13f, h * 0.34f), Offset(w * 0.23f, h * 0.34f), strokeWidth = stroke.width, cap = stroke.cap)
                    drawLine(color, Offset(w * 0.18f, h * 0.34f), Offset(w * 0.18f, h * 0.22f), strokeWidth = stroke.width, cap = stroke.cap)

                    // Right minaret
                    drawLine(color, Offset(w * 0.82f, h * 0.84f), Offset(w * 0.82f, h * 0.34f), strokeWidth = stroke.width, cap = stroke.cap)
                    drawLine(color, Offset(w * 0.77f, h * 0.34f), Offset(w * 0.87f, h * 0.34f), strokeWidth = stroke.width, cap = stroke.cap)
                    drawLine(color, Offset(w * 0.82f, h * 0.34f), Offset(w * 0.82f, h * 0.22f), strokeWidth = stroke.width, cap = stroke.cap)
                } else {
                    drawCircle(color, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                    for (i in 0 until 8) {
                        val angle = Math.toRadians(i * 45.0)
                        val x1 = (w * 0.5f + cos(angle) * w * 0.28f).toFloat()
                        val y1 = (h * 0.5f + sin(angle) * h * 0.28f).toFloat()
                        val x2 = (w * 0.5f + cos(angle) * w * 0.38f).toFloat()
                        val y2 = (h * 0.5f + sin(angle) * h * 0.38f).toFloat()
                        drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke.width, cap = stroke.cap)
                    }
                }
            }
            PrayerName.ASR -> {
                drawLine(color, Offset(w * 0.15f, h * 0.8f), Offset(w * 0.85f, h * 0.8f), strokeWidth = stroke.width, cap = stroke.cap)
                drawCircle(color, radius = w * 0.18f, center = Offset(w * 0.32f, h * 0.42f), style = stroke)
                drawLine(color, Offset(w * 0.38f, h * 0.8f), Offset(w * 0.82f, h * 0.8f), strokeWidth = stroke.width + 1.dp.toPx(), cap = stroke.cap)
            }
            PrayerName.MAGHRIB -> {
                drawLine(color, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.65f), strokeWidth = stroke.width, cap = stroke.cap)
                drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.32f, h * 0.48f), size = Size(w * 0.36f, h * 0.36f), style = stroke)
                drawLine(color, Offset(w * 0.5f, h * 0.22f), Offset(w * 0.5f, h * 0.38f), strokeWidth = stroke.width, cap = stroke.cap)
            }
            PrayerName.ISHA -> {
                val moonPath = Path().apply {
                    moveTo(w * 0.6f, h * 0.2f)
                    cubicTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.75f, w * 0.6f, h * 0.8f)
                    cubicTo(w * 0.4f, h * 0.7f, w * 0.4f, h * 0.3f, w * 0.6f, h * 0.2f)
                    close()
                }
                drawPath(moonPath, color, style = stroke)
                drawCircle(color, radius = 1.5.dp.toPx(), center = Offset(w * 0.75f, h * 0.32f))
                drawCircle(color, radius = 1.2.dp.toPx(), center = Offset(w * 0.82f, h * 0.52f))
            }
        }
    }
}

@Composable
fun RightNowCard(
    item: RightNowItem,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RIGHT NOW",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = item.subtitle,
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = onActionClick,
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.actionText,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NaflPrayersSection(
    naflPrayerItems: List<NaflPrayerItem>,
    onOpenEvidence: (NaflType) -> Unit,
    modifier: Modifier = Modifier
) {
    if (naflPrayerItems.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nafl Prayers",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Recommended, not obligatory",
                fontFamily = SpaceGrotesk,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                naflPrayerItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    NaflPrayerRow(
                        item = item,
                        onOpenEvidence = { onOpenEvidence(item.type) }
                    )
                }
            }
        }
    }
}

@Composable
fun NaflPrayerRow(
    item: NaflPrayerItem,
    onOpenEvidence: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.type.displayName.take(1),
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.type.displayName,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onOpenEvidence,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Evidence & Scholarly Note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                val now = System.currentTimeMillis()
                val isEnded = item.endMillis in 1..<now
                val subtitle = if (isEnded) {
                    "That window has ended. Another opportunity comes tomorrow."
                } else {
                    item.type.defaultSubtitle
                }

                Text(
                    text = subtitle,
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = if (isEnded) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else (if (isDark) Color(0xFFA8A8A2) else LightMutedText)
                )
            }
        }

        Text(
            text = item.timeFormatted,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (item.isCurrentWindow) (if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary) else (if (isDark) Color(0xFFA8A8A2) else LightMutedText)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaflEvidenceModalBottomSheet(
    naflType: NaflType,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = naflType.evidenceTitle,
                        fontFamily = SerifHeaderFont,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = naflType.arabicName,
                        fontFamily = AmiriFont,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            Text(
                text = naflType.description,
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HADITH / TEXT REFERENCE",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = naflType.referenceText,
                        fontFamily = SpaceGrotesk,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Grading: ${naflType.gradingText}",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!naflType.scholarlyNote.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SCHOLARLY NOTE",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = naflType.scholarlyNote,
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

