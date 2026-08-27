package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.predictiveBackChildTransform
import com.example.ui.components.predictiveBackTransform
import com.example.ui.components.rememberPredictiveBackState
import com.example.data.db.DuaCategoryEntity
import com.example.data.db.DuaCategoryWithDuas
import com.example.data.db.DuaEntity
import com.example.data.util.DuaCategoryInfo
import com.example.data.util.DuaData
import com.example.data.util.DuaItem
import com.example.data.util.PastelTheme
import com.example.ui.components.PageHeader
import com.example.ui.components.SegmentedTabs
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

// =========================================================================
// DATA MODELS, CONVERTERS & CANONICAL ALIASES
// =========================================================================

fun DuaEntity.toDuaItem(): DuaItem = DuaItem(
    id = id,
    title = title,
    arabic = arabic,
    transliteration = transliteration,
    translation = translation,
    category = categoryTitle,
    reference = reference
)

fun DuaItem.toDuaEntity(displayOrder: Int = 0): DuaEntity = DuaEntity(
    id = id.ifEmpty { "dua_${title.lowercase().replace(" ", "_")}" },
    categoryId = category,
    categoryTitle = category,
    title = title,
    arabic = arabic,
    transliteration = transliteration,
    translation = translation,
    reference = reference,
    displayOrder = displayOrder
)

val categoryList: List<DuaCategoryInfo> get() = DuaData.CATEGORIES
val allDuas: List<DuaItem> get() = DuaData.ALL_DUAS


// =========================================================================
// PART 5: LOCAL BOOKMARKS & SHARING ENGINE
// =========================================================================

fun getSavedDuaBookmarks(context: Context): Set<String> {
    val prefs = context.getSharedPreferences("dua_bookmarks_prefs", Context.MODE_PRIVATE)
    return prefs.getStringSet("bookmarked_dua_ids", emptySet()) ?: emptySet()
}

fun toggleDuaBookmark(context: Context, duaId: String): Boolean {
    val prefs = context.getSharedPreferences("dua_bookmarks_prefs", Context.MODE_PRIVATE)
    val current = prefs.getStringSet("bookmarked_dua_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    val isNowBookmarked = if (current.contains(duaId)) {
        current.remove(duaId)
        false
    } else {
        current.add(duaId)
        true
    }
    prefs.edit().putStringSet("bookmarked_dua_ids", current).apply()
    return isNowBookmarked
}

fun shareDuaText(context: Context, dua: DuaItem) {
    val shareContent = buildString {
        appendLine("✨ ${dua.title}")
        appendLine()
        appendLine(dua.arabic)
        appendLine()
        appendLine(dua.transliteration)
        appendLine()
        appendLine("Translation:")
        appendLine("\"${dua.translation}\"")
        if (!dua.reference.isNullOrBlank()) {
            appendLine()
            appendLine("📚 Source: ${dua.reference} (${dua.authenticityGrade})")
        }
        if (!dua.recommendedCount.isNullOrBlank()) {
            appendLine("⏱️ Recommended: ${dua.recommendedCount}")
        }
        appendLine()
        append("— Shared via FiveLight")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, dua.title)
        putExtra(Intent.EXTRA_TEXT, shareContent)
    }
    context.startActivity(Intent.createChooser(intent, "Share Supplication"))
}

fun copyDuaText(context: Context, clipboardManager: ClipboardManager, dua: DuaItem) {
    val copyContent = buildString {
        appendLine("✨ ${dua.title}")
        appendLine()
        appendLine(dua.arabic)
        appendLine()
        appendLine(dua.transliteration)
        appendLine()
        appendLine("Translation:")
        appendLine("\"${dua.translation}\"")
        if (!dua.reference.isNullOrBlank()) {
            appendLine()
            appendLine("📚 Source: ${dua.reference} (${dua.authenticityGrade})")
        }
        if (!dua.recommendedCount.isNullOrBlank()) {
            appendLine("⏱️ Recommended: ${dua.recommendedCount}")
        }
        appendLine()
        append("— FiveLight Supplications")
    }

    clipboardManager.setText(AnnotatedString(copyContent))
    Toast.makeText(context, "Supplication copied to clipboard", Toast.LENGTH_SHORT).show()
}

// =========================================================================
// MAIN DUA LIBRARY SCREEN COMPOSABLE
// =========================================================================

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DuaLibraryScreen(
    onBack: () -> Unit,
    initialCategory: String? = null,
    initialDuaId: String? = null,
    isActiveTab: Boolean = true
) {
    var activeCategory by rememberSaveable { mutableStateOf(initialCategory) }
    var targetDuaId by remember { mutableStateOf(initialDuaId) }
    var selectedCategoryTab by rememberSaveable { mutableIntStateOf(0) } // 0: All, 1: Bookmarks
    var arabicFontSizeSp by rememberSaveable { mutableFloatStateOf(26f) }
    var showFontControls by rememberSaveable { mutableStateOf(false) }
    var inspectedDuaForSource by remember { mutableStateOf<DuaItem?>(null) }

    LaunchedEffect(initialCategory, initialDuaId) {
        if (initialCategory != null) {
            activeCategory = initialCategory
        }
        if (initialDuaId != null) {
            targetDuaId = initialDuaId
        }
    }

    val context = LocalContext.current
    var bookmarkedIds by remember { mutableStateOf(getSavedDuaBookmarks(context)) }

    val predictiveState = rememberPredictiveBackState()
    val isInternalBack = inspectedDuaForSource != null || showFontControls || activeCategory != null

    RegisterPredictiveBackHandler(
        enabled = isActiveTab && isInternalBack,
        backState = predictiveState,
        onBack = {
            if (inspectedDuaForSource != null) {
                inspectedDuaForSource = null
            } else if (showFontControls) {
                showFontControls = false
            } else if (activeCategory != null) {
                activeCategory = null
                targetDuaId = null
            }
        }
    )

    val isCategoryActive = activeCategory != null

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .predictiveBackChildTransform(if (activeCategory != null) predictiveState.progress else 0f)
        ) {
            DuaLibraryMainContent(
                onBack = onBack,
                onCategoryClick = { 
                    targetDuaId = null
                    activeCategory = it 
                },
                bookmarkedIds = bookmarkedIds,
                onToggleBookmark = { id ->
                    toggleDuaBookmark(context, id)
                    bookmarkedIds = getSavedDuaBookmarks(context)
                },
                selectedTab = selectedCategoryTab,
                onTabSelect = { selectedCategoryTab = it },
                arabicFontSizeSp = arabicFontSizeSp,
                showFontControls = showFontControls,
                onToggleFontControls = { showFontControls = !showFontControls },
                onFontSizeChange = { arabicFontSizeSp = it },
                onInspectSource = { inspectedDuaForSource = it }
            )
        }

        if (activeCategory != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .predictiveBackTransform(predictiveState.progress, predictiveState.swipeEdge)
            ) {
                DuaCategoryDetailScreen(
                    categoryTitle = activeCategory!!,
                    arabicFontSizeSp = arabicFontSizeSp,
                    onBack = { 
                        activeCategory = null 
                        targetDuaId = null
                    },
                    bookmarkedIds = bookmarkedIds,
                    onToggleBookmark = { id ->
                        toggleDuaBookmark(context, id)
                        bookmarkedIds = getSavedDuaBookmarks(context)
                    },
                    showFontControls = showFontControls,
                    onToggleFontControls = { showFontControls = !showFontControls },
                    onFontSizeChange = { arabicFontSizeSp = it },
                    onInspectSource = { inspectedDuaForSource = it },
                    initialDuaId = targetDuaId
                )
            }
        }
    }

    // PART 4: Interactive Source / Reference Citation Modal Bottom Sheet
    if (inspectedDuaForSource != null) {
        DuaSourceCitationSheet(
            dua = inspectedDuaForSource!!,
            onDismiss = { inspectedDuaForSource = null }
        )
    }
}

// =========================================================================
// DUA LIBRARY MAIN CONTENT (GRID + FEATURED + BOOKMARKS TAB)
// =========================================================================

@Composable
fun DuaLibraryMainContent(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    bookmarkedIds: Set<String>,
    onToggleBookmark: (String) -> Unit,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    arabicFontSizeSp: Float,
    showFontControls: Boolean,
    onToggleFontControls: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onInspectSource: (DuaItem) -> Unit
) {
    val isDark = isAppInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val sectionHeaderColor = Color.semanticMutedText
    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder
    val accentColor = Color.semanticPrimaryAccent

    // PART 7: Gentle Entrance Animation (0 -> 1 alpha, 14dp -> 0dp translateY)
    val categoryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isEntered = true }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "duaLibraryEntranceAlpha"
    )
    val entranceTranslateY by animateDpAsState(
        targetValue = if (isEntered) 0.dp else 12.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "duaLibraryEntranceTranslateY"
    )

    // PART 2: Daily Rotating Featured Dua (Deterministic Day-of-Year Modulo)
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val featuredDua = remember(dayOfYear) { allDuas[dayOfYear % allDuas.size] }

    val bookmarkedDuas = remember(bookmarkedIds) {
        allDuas.filter { bookmarkedIds.contains(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslateY.toPx()
            }
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("dua_library_back_btn")
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = textPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            PageHeader(
                title = "Dua Library",
                subtitle = "Supplications from Qur'an & Sahih Hadith",
                titleColor = textPrimary,
                subtitleColor = textSecondary,
                includeStatusBarPadding = false,
                horizontalPadding = 0.dp
            )
            Spacer(modifier = Modifier.weight(1f))

            // Font Size Control Action
            IconButton(
                onClick = onToggleFontControls,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("dua_library_font_size_btn")
            ) {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = "Adjust Font Size",
                    tint = if (showFontControls) accentColor else textPrimary
                )
            }
        }

        // PART 6: Expandable Font Size Controls Bar
        AnimatedVisibility(
            visible = showFontControls,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arabic Text Size",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "${arabicFontSizeSp.toInt()} sp",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = arabicFontSizeSp,
                        onValueChange = onFontSizeChange,
                        valueRange = 20f..38f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = cardBorder
                        )
                    )
                }
            }
        }

        // Filter Tabs: [Categories] & [Saved Bookmarks]
        val duaTabs = remember { listOf("Categories", "Saved Bookmarks") }
        SegmentedTabs(
            tabs = duaTabs,
            selectedIndex = selectedTab,
            onTabSelected = onTabSelect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            testTagPrefix = "dua_tab"
        )

        val categoryCounts = remember(allDuas) {
            allDuas.groupingBy { it.category }.eachCount()
        }

        LazyColumn(
            state = categoryListState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedTab == 0) {
                // PART 2: Daily Rotating Featured Dua Hero Card
                item {
                    Text(
                        text = "TODAY'S FEATURED DUA",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = sectionHeaderColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                item {
                    FeaturedDuaCard(
                        dua = featuredDua,
                        arabicFontSizeSp = arabicFontSizeSp,
                        isBookmarked = bookmarkedIds.contains(featuredDua.id),
                        onToggleBookmark = { onToggleBookmark(featuredDua.id) },
                        onInspectSource = { onInspectSource(featuredDua) },
                        onClick = { onCategoryClick(featuredDua.category) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "ALL CATEGORIES",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = sectionHeaderColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // PART 1 & 8: Pastel Category Icon Badges & Dynamic Dua Counts
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val chunkedCategories = categoryList.chunked(2)
                        chunkedCategories.forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (cat in rowItems) {
                                    val count = categoryCounts[cat.title] ?: 0
                                    DuaCategoryCard(
                                        category = cat,
                                        duaCount = count,
                                        onClick = { onCategoryClick(cat.title) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                // Saved Bookmarks List
                if (bookmarkedDuas.isEmpty()) {
                    item {
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = textSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Bookmarked Duas Yet",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the bookmark icon on any supplication to save it here for quick daily access.",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 13.sp,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "SAVED BOOKMARKS (${bookmarkedDuas.size})",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = sectionHeaderColor,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    items(bookmarkedDuas) { dua ->
                        DuaDetailCard(
                            dua = dua,
                            arabicFontSizeSp = arabicFontSizeSp,
                            isBookmarked = true,
                            onToggleBookmark = { onToggleBookmark(dua.id) },
                            onInspectSource = { onInspectSource(dua) },
                            enableVerticalScroll = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// PART 1 & 8: DUA CATEGORY CARD COMPOSABLE (PASTEL BADGE + DUA COUNT)
// =========================================================================

@Composable
fun DuaCategoryCard(
    category: DuaCategoryInfo,
    duaCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText

    val badgeBg = if (isDark) category.pastelTheme.darkBg else category.pastelTheme.lightBg
    val badgeIconTint = if (isDark) category.pastelTheme.darkIcon else category.pastelTheme.lightIcon

    // FIX 3: Subtle Category Card Tint (3-6% opacity whisper tint over Surface token)
    val cardTint = if (isDark) {
        category.pastelTheme.darkIcon.copy(alpha = 0.045f)
    } else {
        category.pastelTheme.lightIcon.copy(alpha = 0.045f)
    }

    val cardShape = RoundedCornerShape(22.dp)

    // FIX 1: Card Elevation and Depth (subtle, non-glossy)
    Surface(
        onClick = onClick,
        shape = cardShape,
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .shadow(
                elevation = if (isDark) 3.dp else 2.dp,
                shape = cardShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.05f)
            )
            .testTag("category_card_${category.title.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardTint)
                .padding(15.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Header Row: Larger Category Icon + Refined Dua Count Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FIX 2: Larger Category Icon (56dp circle with 28dp glyph)
                    Surface(
                        shape = CircleShape,
                        color = badgeBg,
                        border = BorderStroke(1.dp, badgeIconTint.copy(alpha = 0.22f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = badgeIconTint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // FIX 4: Refined Dua Count Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.035f),
                        border = BorderStroke(1.dp, badgeIconTint.copy(alpha = 0.18f))
                    ) {
                        Text(
                            text = "$duaCount ${if (duaCount == 1) "Dua" else "Duas"}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(13.dp))

                // FIX 5: Category Card Typography Hierarchy
                Text(
                    text = category.title,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = category.description,
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.5.sp
                )
            }
        }
    }
}

// =========================================================================
// PART 2 & FIX 8: FEATURED DUA CARD (DAILY ROTATION HERO CARD)
// =========================================================================

@Composable
fun FeaturedDuaCard(
    dua: DuaItem,
    arabicFontSizeSp: Float,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onInspectSource: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val accentColor = Color.semanticPrimaryAccent
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val heroShape = RoundedCornerShape(24.dp)

    // Featured Card Hero Treatment with purely content-driven height & subtle accent gradient wash
    Surface(
        onClick = onClick,
        shape = heroShape,
        color = cardBg,
        border = BorderStroke(1.dp, if (isDark) cardBorder else cardBorder.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = if (isDark) 8.dp else 6.dp,
                shape = heroShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.16f),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.48f) else Color.Black.copy(alpha = 0.09f)
            )
            .testTag("featured_dua_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isDark) 0.07f else 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Header Row: Category Badge + Grouped Bookmark/Share Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Refined Category Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = if (isDark) 0.22f else 0.10f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = dua.category.trim().uppercase(),
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = if (isDark) Color.White else accentColor,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp)
                    )
                }

                // Grouped Bookmark + Copy + Share Action Container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.055f) else Color.Black.copy(alpha = 0.035f),
                    border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBookmark()
                            },
                            modifier = Modifier.size(34.dp).testTag("featured_bookmark_btn")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) accentColor else textSecondary,
                                modifier = Modifier.size(18.5.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(cardBorder.copy(alpha = 0.5f))
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                copyDuaText(context, clipboardManager, dua)
                            },
                            modifier = Modifier.size(34.dp).testTag("featured_copy_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy Supplication",
                                tint = textSecondary,
                                modifier = Modifier.size(16.5.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(cardBorder.copy(alpha = 0.5f))
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                shareDuaText(context, dua)
                            },
                            modifier = Modifier.size(34.dp).testTag("featured_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = textSecondary,
                                modifier = Modifier.size(17.5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dua Title
            Text(
                text = dua.title.trim(),
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = textPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Script - Content-driven without arbitrary maxLines cropping
            Text(
                text = dua.arabic.trim(),
                style = ArabicTextStyle.copy(
                    fontSize = (arabicFontSizeSp * 0.95f).sp,
                    lineHeight = (arabicFontSizeSp * 1.62f).sp
                ),
                color = textPrimary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Transliteration (if available)
            if (dua.transliteration.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dua.transliteration.trim(),
                    fontFamily = SpaceGrotesk,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = textSecondary.copy(alpha = 0.88f),
                    lineHeight = 18.sp
                )
            }

            // Translation - Content-driven without arbitrary maxLines cropping
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dua.translation.trim(),
                fontFamily = SpaceGrotesk,
                fontSize = 13.5.sp,
                color = textSecondary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Interactive Source Pill & "Read Category" Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (dua.reference != null) {
                    Surface(
                        onClick = onInspectSource,
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = dua.reference.trim(),
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Text(
                        text = "Read Category",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// PART 3: IN-CATEGORY HORIZONTAL SWIPE / CAROUSEL & DETAIL SCREEN
// =========================================================================

@Composable
fun DuaCategoryDetailScreen(
    categoryTitle: String,
    arabicFontSizeSp: Float,
    onBack: () -> Unit,
    bookmarkedIds: Set<String>,
    onToggleBookmark: (String) -> Unit,
    showFontControls: Boolean,
    onToggleFontControls: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onInspectSource: (DuaItem) -> Unit,
    initialDuaId: String? = null
) {
    val isDark = isAppInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder
    val accentColor = Color.semanticPrimaryAccent

    val categoryDuas = remember(categoryTitle) {
        allDuas.filter { it.category == categoryTitle }
    }

    val initialIndex = remember(categoryTitle, initialDuaId) {
        if (initialDuaId != null) {
            categoryDuas.indexOfFirst { it.id == initialDuaId }.coerceAtLeast(0)
        } else {
            0
        }
    }

    var isFocusCarouselMode by rememberSaveable { mutableStateOf(true) }
    val detailListState = rememberSaveable(categoryTitle, saver = LazyListState.Saver) { LazyListState(firstVisibleItemIndex = initialIndex) }
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { categoryDuas.size.coerceAtLeast(1) })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(initialDuaId, categoryDuas) {
        if (initialDuaId != null) {
            val targetIndex = categoryDuas.indexOfFirst { it.id == initialDuaId }
            if (targetIndex >= 0) {
                pagerState.scrollToPage(targetIndex)
                detailListState.scrollToItem(targetIndex)
            }
        }
    }

    // PART 7: Gentle Entrance Animation
    var isEntered by remember(categoryTitle) { mutableStateOf(false) }
    LaunchedEffect(categoryTitle) { isEntered = true }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = LinearOutSlowInEasing),
        label = "categoryDetailEntranceAlpha"
    )
    val entranceTranslateY by animateDpAsState(
        targetValue = if (isEntered) 0.dp else 12.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "categoryDetailEntranceTranslateY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslateY.toPx()
            }
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("dua_detail_back_btn")
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = textPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryTitle,
                    fontFamily = SpaceGrotesk,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${categoryDuas.size} Authenticated Supplications",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }

            // Toggle Mode Action (Carousel vs List)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isFocusCarouselMode = !isFocusCarouselMode
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("dua_view_mode_toggle_btn")
            ) {
                Icon(
                    imageVector = if (isFocusCarouselMode) Icons.Filled.ViewAgenda else Icons.Filled.ViewCarousel,
                    contentDescription = if (isFocusCarouselMode) "Switch to List View" else "Switch to Focus Reader",
                    tint = textPrimary
                )
            }

            // Font Size Control Action
            IconButton(
                onClick = onToggleFontControls,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("dua_detail_font_btn")
            ) {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = "Font Size",
                    tint = if (showFontControls) accentColor else textPrimary
                )
            }
        }

        // Expandable Font Size Slider
        AnimatedVisibility(
            visible = showFontControls,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arabic Text Size",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "${arabicFontSizeSp.toInt()} sp",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = arabicFontSizeSp,
                        onValueChange = onFontSizeChange,
                        valueRange = 20f..38f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = cardBorder
                        )
                    )
                }
            }
        }

        if (categoryDuas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No supplications available in this category.",
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }
        } else if (isFocusCarouselMode) {
            // =================================================================
            // PART 3: FOCUS READER MODE (HORIZONTAL SWIPE WITH POSITION COUNTER)
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 110.dp)
            ) {
                // Top Position Indicator Strip: [Dua 2 of 6] + Upgraded Segmented Pill Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = if (isDark) 0.22f else 0.10f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "Dua ${pagerState.currentPage + 1} of ${categoryDuas.size}",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isDark) Color.White else accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.5.dp)
                        )
                    }

                    // UPGRADED SWIPE POSITION INDICATOR: Active Accent Pill + Muted Low-Opacity Segments
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.025f),
                        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)
                        ) {
                            categoryDuas.indices.forEach { index ->
                                val isCurrent = pagerState.currentPage == index
                                val segmentWidth by animateDpAsState(
                                    targetValue = if (isCurrent) 28.dp else 10.dp,
                                    animationSpec = spring(
                                        dampingRatio = 0.76f,
                                        stiffness = 380f
                                    ),
                                    label = "segmentWidth"
                                )
                                val segmentColor by animateColorAsState(
                                    targetValue = if (isCurrent) accentColor else (if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
                                    animationSpec = spring(
                                        dampingRatio = 0.84f,
                                        stiffness = 380f
                                    ),
                                    label = "segmentColor"
                                )
                                Box(
                                    modifier = Modifier
                                        .height(5.dp)
                                        .width(segmentWidth)
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(segmentColor)
                                        .clickable {
                                            if (pagerState.currentPage != index) {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }

                // Horizontal Pager with Full Dua Cards and subtle swipe parallax animation
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val dua = categoryDuas[pageIndex]
                    val isBookmarked = bookmarkedIds.contains(dua.id)

                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)

                    DuaDetailCard(
                        dua = dua,
                        arabicFontSizeSp = arabicFontSizeSp,
                        isBookmarked = isBookmarked,
                        onToggleBookmark = { onToggleBookmark(dua.id) },
                        onInspectSource = { onInspectSource(dua) },
                        enableVerticalScroll = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp)
                            .graphicsLayer {
                                // Subtle horizontal slide translation / parallax during swipe
                                translationX = pageOffset * 28f
                                // Gentle scale and fade for cards transitioning in/out
                                val scale = 1f - (absOffset * 0.035f)
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - (absOffset * 0.22f)
                            }
                    )
                }

                // Navigation Prev / Next Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    Surface(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0,
                        shape = RoundedCornerShape(14.dp),
                        color = if (pagerState.currentPage > 0) cardBg else cardBg.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChevronLeft,
                                contentDescription = "Previous",
                                tint = if (pagerState.currentPage > 0) textPrimary else textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Previous",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (pagerState.currentPage > 0) textPrimary else textSecondary.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Next Button
                    val isLastPage = pagerState.currentPage == categoryDuas.size - 1
                    Surface(
                        onClick = {
                            if (!isLastPage) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        enabled = !isLastPage,
                        shape = RoundedCornerShape(14.dp),
                        color = if (!isLastPage) accentColor.copy(alpha = if (isDark) 0.25f else 0.15f) else cardBg.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, if (!isLastPage) accentColor.copy(alpha = 0.4f) else cardBorder),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = if (isLastPage) "End of Category" else "Next Dua",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!isLastPage) (if (isDark) Color.White else accentColor) else textSecondary.copy(alpha = 0.4f)
                            )
                            if (!isLastPage) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Next",
                                    tint = if (isDark) Color.White else accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // =================================================================
            // LIST VIEW MODE (ALL DUAS IN CATEGORY SCROLLABLE)
            // =================================================================
            LazyColumn(
                state = detailListState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categoryDuas) { dua ->
                    val isBookmarked = bookmarkedIds.contains(dua.id)
                    DuaDetailCard(
                        dua = dua,
                        arabicFontSizeSp = arabicFontSizeSp,
                        isBookmarked = isBookmarked,
                        onToggleBookmark = { onToggleBookmark(dua.id) },
                        onInspectSource = { onInspectSource(dua) },
                        enableVerticalScroll = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// =========================================================================
// PART 4 & 5: DUA DETAIL CARD WITH BOOKMARK, SHARE & SOURCE TRIGGER
// =========================================================================

@Composable
fun DuaDetailCard(
    dua: DuaItem,
    arabicFontSizeSp: Float,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onInspectSource: () -> Unit,
    modifier: Modifier = Modifier,
    enableVerticalScroll: Boolean = false
) {
    val isDark = isAppInDarkTheme()
    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val accentColor = Color.semanticPrimaryAccent
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val detailShape = RoundedCornerShape(22.dp)

    // Subtle, smooth fade-in and scale-up entrance animation for main text content
    var isContentVisible by remember(dua.id) { mutableStateOf(false) }
    LaunchedEffect(dua.id) {
        isContentVisible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isContentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing),
        label = "duaDetailTextAlpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (isContentVisible) 1f else 0.97f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 400f),
        label = "duaDetailTextScale"
    )

    // FIX 1: Card Elevation and Depth
    Surface(
        shape = detailShape,
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .shadow(
                elevation = if (isDark) 4.dp else 2.5.dp,
                shape = detailShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.08f),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.05f)
            )
    ) {
        val columnModifier = if (enableVerticalScroll) {
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        }
        Column(
            modifier = columnModifier
        ) {
            // FIX 5 & FIX 7: Header Row with Title, Category Tag & Grouped Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    // Category / Context Tag
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = accentColor.copy(alpha = if (isDark) 0.20f else 0.10f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = dua.category.uppercase(),
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isDark) Color.White else accentColor,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Dua Title
                    Text(
                        text = dua.title,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 16.5.sp,
                        lineHeight = 22.sp
                    )
                }

                // FIX 7: Grouped Bookmark + Copy + Share Action Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.055f) else Color.Black.copy(alpha = 0.035f),
                    border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBookmark()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("bookmark_btn_${dua.id}")
                        ) {
                            AnimatedContent(
                                targetState = isBookmarked,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(180)) togetherWith
                                            fadeOut(animationSpec = tween(140))
                                },
                                label = "duaDetailBookmarkAnim"
                            ) { activeBookmarked ->
                                Icon(
                                    imageVector = if (activeBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = if (activeBookmarked) "Remove Bookmark" else "Save to Bookmarks",
                                    tint = if (activeBookmarked) accentColor else textSecondary,
                                    modifier = Modifier.size(18.5.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(cardBorder.copy(alpha = 0.5f))
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                copyDuaText(context, clipboardManager, dua)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("copy_btn_${dua.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy Supplication",
                                tint = textSecondary,
                                modifier = Modifier.size(16.5.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(cardBorder.copy(alpha = 0.5f))
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                shareDuaText(context, dua)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("share_btn_${dua.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share Supplication",
                                tint = textSecondary,
                                modifier = Modifier.size(17.5.dp)
                            )
                        }
                    }
                }
            }

            // Main Text Content Section with smooth fade-in and scale-up
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentAlpha
                        scaleX = contentScale
                        scaleY = contentScale
                        transformOrigin = TransformOrigin(0.5f, 0.15f)
                    }
            ) {
                // Recommended Repetition Badge if available
                if (!dua.recommendedCount.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dua.recommendedCount,
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FIX 5: Arabic Text Hierarchy & Reading Size
                Text(
                    text = dua.arabic,
                    style = ArabicTextStyle.copy(
                        fontSize = arabicFontSizeSp.sp,
                        lineHeight = (arabicFontSizeSp * 1.68f).sp
                    ),
                    textAlign = TextAlign.Right,
                    color = textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Transliteration
                Text(
                    text = dua.transliteration,
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.5.sp,
                    color = textSecondary,
                    style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    lineHeight = 19.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Translation
                Text(
                    text = dua.translation,
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.5.sp,
                    color = textPrimary.copy(alpha = if (isDark) 0.92f else 0.88f),
                    lineHeight = 21.5.sp
                )

                // Interactive Source / Citation Row
                if (dua.reference != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        onClick = onInspectSource,
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Outlined.Verified,
                                    contentDescription = "Verified Reference",
                                    tint = accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = dua.reference,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "View Source Details",
                                tint = textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// PART 4: VERIFIED SOURCE CITATION BOTTOM SHEET
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaSourceCitationSheet(
    dua: DuaItem,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val accentColor = Color.semanticPrimaryAccent
    val cardBorder = Color.semanticBorder
    val sheetBg = Color.semanticSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Source & Authenticity",
                            fontFamily = SpaceGrotesk,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Strictly Verified Reference",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = if (isDark) 0.22f else 0.12f)
                ) {
                    Text(
                        text = dua.authenticityGrade,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Supplication Title
            Text(
                text = dua.title,
                fontFamily = SpaceGrotesk,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Reference Citation Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRIMARY RECORD & COLLECTION",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticMutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dua.reference ?: "Standard Prophetic Supplication",
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            // Benefit & Hadith Context Card
            if (!dua.benefitOrNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TRANSMITTED BENEFIT & CONTEXT",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticMutedText,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dua.benefitOrNotes,
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.5.sp,
                            color = textSecondary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            if (!dua.recommendedCount.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "RECOMMENDED TIMING & REPETITION",
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticMutedText,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = dua.recommendedCount,
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Done / Close Action Button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Close Source Details",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
