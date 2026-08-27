package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HijriDate
import com.example.data.model.IslamicDateState
import com.example.data.util.PrayerDisplayUtils
import com.example.ui.components.PageHeader
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.predictiveBackChildTransform
import com.example.ui.components.predictiveBackTransform
import com.example.ui.components.rememberPredictiveBackState
import com.example.ui.theme.*

// ---------------------------------------------------------------------------
// Category Usage Tracker (Local On-Device Storage)
// ---------------------------------------------------------------------------
object ExploreUsageTracker {
    private const val PREFS_NAME = "explore_category_usage"
    private const val KEY_PREFIX = "usage_"

    fun incrementUsage(context: Context, categoryId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_PREFIX + categoryId, 0)
        prefs.edit().putInt(KEY_PREFIX + categoryId, current + 1).apply()
    }

    fun getUsageCount(context: Context, categoryId: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_PREFIX + categoryId, 0)
    }

    fun getOrderedCategories(context: Context): List<String> {
        val defaultOrder = listOf("dua", "names", "calendar")
        val counts = defaultOrder.associateWith { getUsageCount(context, it) }

        val maxCount = counts.values.maxOrNull() ?: 0
        val minCount = counts.values.minOrNull() ?: 0

        // Only reorder if there is a meaningful difference in usage (>= 2 opens)
        if (maxCount - minCount < 2) {
            return defaultOrder
        }

        // Sort descending by usage count, preserving default order on ties
        return defaultOrder.sortedWith(
            compareByDescending<String> { counts[it] ?: 0 }
                .thenBy { defaultOrder.indexOf(it) }
        )
    }
}

// ---------------------------------------------------------------------------
// Featured Content Candidate Model
// ---------------------------------------------------------------------------
data class FeaturedCandidate(
    val id: String,
    val title: String,
    val description: String,
    val actionText: String,
    val icon: ImageVector,
    val badgeText: String,
    val destination: String,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    hijriDate: HijriDate,
    islamicDateState: IslamicDateState? = null,
    activeSubRoute: String = "main",
    onSubRouteChange: (String) -> Unit = {},
    targetDuaCategory: String? = null,
    targetDuaId: String? = null,
    targetAdhkarTitle: String? = null,
    targetNameNumber: Int? = null,
    isActiveTab: Boolean = true,
    modifier: Modifier = Modifier
) {
    val predictiveState = rememberPredictiveBackState()
    val isSubRouteDirectBack = activeSubRoute != "main"

    RegisterPredictiveBackHandler(
        enabled = isActiveTab && isSubRouteDirectBack,
        backState = predictiveState,
        onBack = { onSubRouteChange("main") }
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .predictiveBackChildTransform(if (isSubRouteDirectBack) predictiveState.progress else 0f)
        ) {
            ExploreMainContent(
                hijriDate = hijriDate,
                onNavigate = { destination -> onSubRouteChange(destination) },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isSubRouteDirectBack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .predictiveBackTransform(predictiveState.progress, predictiveState.swipeEdge)
            ) {
                when (activeSubRoute) {
                    "adhkar" -> {
                        AdhkarScreen(
                            onBack = { onSubRouteChange("main") },
                            initialItemTitle = targetAdhkarTitle,
                            isActiveTab = isActiveTab
                        )
                    }
                    "dua" -> {
                        DuaLibraryScreen(
                            onBack = { onSubRouteChange("main") },
                            initialCategory = targetDuaCategory,
                            initialDuaId = targetDuaId,
                            isActiveTab = isActiveTab
                        )
                    }
                    "names" -> {
                        NamesOfAllahScreen(
                            onBack = { onSubRouteChange("main") },
                            initialNameNumber = targetNameNumber
                        )
                    }
                    "calendar" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .statusBarsPadding()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                IconButton(
                                    onClick = { onSubRouteChange("main") },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .testTag("calendar_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Text(
                                    text = "Hijri Calendar",
                                    fontFamily = SerifHeaderFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            if (islamicDateState != null) {
                                CalendarScreen(hijriDate = hijriDate, islamicDateState = islamicDateState)
                            } else {
                                CalendarScreen(hijriDate = hijriDate)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreMainContent(
    hijriDate: HijriDate = HijriDate(day = 1, monthName = "Muharram", monthArabic = "محرم", monthNumber = 1, year = 1448),
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val bgColor = MaterialTheme.colorScheme.background
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val sectionHeaderColor = Color.semanticMutedText
    val accentColor = Color.semanticPrimaryAccent
    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder

    // Determine qualifying Featured candidates
    val featuredCandidates = remember(hijriDate) {
        val candidates = mutableListOf<FeaturedCandidate>()

        // 1. Default / Fallback: Daily Adhkar
        candidates.add(
            FeaturedCandidate(
                id = "adhkar",
                title = "Daily Adhkar",
                description = "Morning & Evening Supplications for daily remembrance and protection",
                actionText = "Open Adhkar",
                icon = Icons.Filled.WbSunny,
                badgeText = "FEATURED",
                destination = "adhkar",
                testTag = "explore_card_adhkar"
            )
        )

        // 2. Friday: Jumu'ah Essentials (if Friday)
        val isFriday = PrayerDisplayUtils.isFriday()
        if (isFriday) {
            candidates.add(
                0, // Prioritize on Friday
                FeaturedCandidate(
                    id = "jumuah",
                    title = "Jumu'ah Essentials",
                    description = "Recite Surah Al-Kahf, send Salawat upon the Prophet ﷺ, and make Friday Duas",
                    actionText = "Explore Duas",
                    icon = Icons.Filled.AutoAwesome,
                    badgeText = "FRIDAY",
                    destination = "dua",
                    testTag = "explore_card_jumuah"
                )
            )
        }

        // 3. Ramadan: Ramadan Devotions (during Ramadan)
        val isRamadan = hijriDate.monthNumber == 9 || hijriDate.monthName.equals("Ramadan", ignoreCase = true)
        if (isRamadan) {
            candidates.add(
                0,
                FeaturedCandidate(
                    id = "ramadan",
                    title = "Ramadan Devotions",
                    description = "Supplications for fasting, Iftar, Tahajjud, and elevated spiritual devotion",
                    actionText = "Explore Duas",
                    icon = Icons.Filled.NightsStay,
                    badgeText = "RAMADAN",
                    destination = "dua",
                    testTag = "explore_card_ramadan"
                )
            )
        }

        candidates
    }

    // Category data mapping
    val categoryDefinitions = remember {
        mapOf(
            "dua" to CategoryData(
                id = "dua",
                title = "Dua Library",
                subtitle = "Daily supplications",
                icon = Icons.Filled.AutoAwesome,
                badgeText = null,
                testTag = "explore_card_dua",
                destination = "dua"
            ),
            "names" to CategoryData(
                id = "names",
                title = "Names of Allah",
                subtitle = "The 99 Beautiful Names",
                icon = Icons.Filled.AutoAwesome,
                badgeText = "99",
                testTag = "explore_card_names",
                destination = "names"
            ),
            "calendar" to CategoryData(
                id = "calendar",
                title = "Hijri Calendar",
                subtitle = "Islamic lunar calendar & key spiritual events",
                icon = Icons.Filled.CalendarMonth,
                badgeText = null,
                testTag = "explore_card_calendar",
                destination = "calendar"
            )
        )
    }

    // Dynamic Category Order based on usage
    var orderedCategoryIds by remember {
        mutableStateOf(ExploreUsageTracker.getOrderedCategories(context))
    }

    val handleCategoryClick = { destination: String ->
        ExploreUsageTracker.incrementUsage(context, destination)
        // Refresh ordered list
        orderedCategoryIds = ExploreUsageTracker.getOrderedCategories(context)
        onNavigate(destination)
    }

    val exploreListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .testTag("explore_screen_main")
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = if (isVisible) 1f else 0f
                    translationY = if (isVisible) 0f else -16.dp.toPx()
                }
        ) {
            PageHeader(
                title = "Explore",
                subtitle = "Discover tools for remembrance, worship & reflection",
                titleColor = textPrimary,
                subtitleColor = textSecondary,
                includeStatusBarPadding = false,
                horizontalPadding = 20.dp
            )
        }

        LazyColumn(
            state = exploreListState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // PART 1: ROTATING FEATURED CARD
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isVisible) 1f else 0f
                        translationY = if (isVisible) 0f else 16.dp.toPx()
                    }
                ) {
                    Text(
                        text = "FEATURED",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = sectionHeaderColor,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    FeaturedRotatingCard(
                        candidates = featuredCandidates,
                        onCandidateClick = { candidate -> onNavigate(candidate.destination) },
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor
                    )
                }
            }

            // PART 2 & 3: USAGE-BASED REORDERED DISCOVER CATEGORIES WITH MICRO-INTERACTIONS
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isVisible) 1f else 0f
                        translationY = if (isVisible) 0f else 24.dp.toPx()
                    }
                ) {
                    Text(
                        text = "DISCOVER CATEGORIES",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = sectionHeaderColor,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val topTwo = orderedCategoryIds.take(2).mapNotNull { categoryDefinitions[it] }
                    val third = orderedCategoryIds.drop(2).firstOrNull()?.let { categoryDefinitions[it] }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Top two items in a responsive 2-column grid
                        if (topTwo.size == 2) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                topTwo.forEach { category ->
                                    CategoryGridCard(
                                        key = category.id,
                                        title = category.title,
                                        subtitle = category.subtitle,
                                        icon = category.icon,
                                        badgeText = category.badgeText,
                                        onClick = { handleCategoryClick(category.destination) },
                                        isDark = isDark,
                                        cardBg = cardBg,
                                        cardBorder = cardBorder,
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary,
                                        accentColor = accentColor,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag(category.testTag)
                                    )
                                }
                            }
                        }

                        // Third item rendered as the wide banner card
                        third?.let { category ->
                            CategoryWideCard(
                                key = category.id,
                                title = category.title,
                                subtitle = category.subtitle,
                                icon = category.icon,
                                onClick = { handleCategoryClick(category.destination) },
                                isDark = isDark,
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                accentColor = accentColor,
                                modifier = Modifier.testTag(category.testTag)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class CategoryData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeText: String?,
    val testTag: String,
    val destination: String
)

// ---------------------------------------------------------------------------
// Featured Rotating Card with Horizontal Swipe & Pagination Dots
// ---------------------------------------------------------------------------
@Composable
fun FeaturedRotatingCard(
    candidates: List<FeaturedCandidate>,
    onCandidateClick: (FeaturedCandidate) -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    var slideDirection by remember { mutableStateOf(1) } // 1: next (swipe left), -1: prev (swipe right)
    var totalDragX by remember { mutableStateOf(0f) }

    val safeIndex = if (candidates.isNotEmpty()) currentIndex.coerceIn(0, candidates.size - 1) else 0
    val currentCandidate = if (candidates.isNotEmpty()) candidates[safeIndex] else return

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "featuredCardScale"
    )

    // PART 3: Icon tile micro-interaction (scale 0.92f + 3-5 deg tilt on press)
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 110),
        label = "featuredIconScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isPressed) -4.0f else 0.0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
        label = "featuredIconRotation"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, Color.semanticBorder),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .pointerInput(candidates.size) {
                if (candidates.size > 1) {
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onDragEnd = {
                            if (totalDragX < -40f) {
                                // Swipe left -> Next candidate
                                slideDirection = 1
                                currentIndex = (currentIndex + 1) % candidates.size
                            } else if (totalDragX > 40f) {
                                // Swipe right -> Previous candidate
                                slideDirection = -1
                                currentIndex = if (currentIndex - 1 < 0) candidates.size - 1 else currentIndex - 1
                            }
                            totalDragX = 0f
                        },
                        onDragCancel = { totalDragX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount
                        }
                    )
                }
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onCandidateClick(currentCandidate) }
            )
            .testTag(currentCandidate.testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Micro-animated icon tile
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.semanticSurfaceElevated,
                        border = BorderStroke(1.dp, Color.semanticBorder),
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                                rotationZ = iconRotation
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = currentCandidate.icon,
                                contentDescription = null,
                                tint = Color.semanticPrimaryText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Badge
                    Surface(
                        shape = CircleShape,
                        color = Color.semanticPrimaryAccent.copy(alpha = 0.12f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentCandidate.badgeText,
                                fontFamily = SpaceGrotesk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticPrimaryAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated swipeable content
                AnimatedContent(
                    targetState = safeIndex,
                    transitionSpec = {
                        if (slideDirection > 0) {
                            slideInHorizontally(animationSpec = tween(220)) { it / 2 } + fadeIn(animationSpec = tween(220)) togetherWith
                                    slideOutHorizontally(animationSpec = tween(180)) { -it / 2 } + fadeOut(animationSpec = tween(180))
                        } else {
                            slideInHorizontally(animationSpec = tween(220)) { -it / 2 } + fadeIn(animationSpec = tween(220)) togetherWith
                                    slideOutHorizontally(animationSpec = tween(180)) { it / 2 } + fadeOut(animationSpec = tween(180))
                        }
                    },
                    label = "featuredCandidateTransition"
                ) { targetIdx ->
                    val candidate = candidates[targetIdx.coerceIn(0, candidates.size - 1)]
                    Column {
                        Text(
                            text = candidate.title,
                            fontFamily = SerifHeaderFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = candidate.description,
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pagination Dots (reusing ReflectionOfTheDayCard dot style)
                    if (candidates.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            candidates.indices.forEach { index ->
                                val isSelected = index == safeIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 6.dp else 4.5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.semanticPrimaryAccent
                                            else Color.semanticBorder
                                        )
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentCandidate.actionText,
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryAccent
                        )

                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Category Grid Card with Part 3 Icon-Tile Micro-Interaction
// ---------------------------------------------------------------------------
@Composable
fun CategoryGridCard(
    key: String = "",
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    onClick: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "gridCardScale"
    )

    // PART 3: Icon tile micro-interaction (scale 0.92f + 3-5 deg tilt on press)
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 110),
        label = "gridIconScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isPressed) -4.0f else 0.0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
        label = "gridIconRotation"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        interactionSource = interactionSource,
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Micro-animated icon tile
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.semanticSurfaceElevated,
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            rotationZ = iconRotation
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (badgeText != null) {
                            Text(
                                text = badgeText,
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticPrimaryText
                            )
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.semanticPrimaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.semanticPrimaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontFamily = SerifHeaderFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontFamily = SpaceGrotesk,
                fontSize = 12.sp,
                color = textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Category Wide Card with Part 3 Icon-Tile Micro-Interaction
// ---------------------------------------------------------------------------
@Composable
fun CategoryWideCard(
    key: String = "",
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "wideCardScale"
    )

    // PART 3: Icon tile micro-interaction (scale 0.92f + 3-5 deg tilt on press)
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 110),
        label = "wideIconScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isPressed) -4.0f else 0.0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
        label = "wideIconRotation"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Micro-animated icon tile
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.semanticSurfaceElevated,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        rotationZ = iconRotation
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.semanticPrimaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SerifHeaderFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.semanticPrimaryAccent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
