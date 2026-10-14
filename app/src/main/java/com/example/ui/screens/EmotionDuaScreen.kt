package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.util.*
import com.example.ui.components.PageHeader
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.rememberPredictiveBackState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionDuaScreen(
    onBack: () -> Unit,
    isActiveTab: Boolean = true,
    initialEmotionId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // State tracking
    var selectedEmotion by rememberSaveable {
        mutableStateOf(
            initialEmotionId?.let { id ->
                FeelingDuaData.ALL_EMOTIONS.find { it.id.equals(id, ignoreCase = true) }
            }
        )
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(EmotionCategoryFilter.ALL) }

    // Bookmarks persistence (reusing "dua_bookmarks_prefs")
    var bookmarkedIds by remember { mutableStateOf(getSavedDuaBookmarks(context)) }

    val toggleBookmark = { id: String ->
        toggleDuaBookmark(context, id)
        bookmarkedIds = getSavedDuaBookmarks(context)
    }

    // Predictive back gesture handling
    val predictiveState = rememberPredictiveBackState()
    val isViewingDetail = selectedEmotion != null

    RegisterPredictiveBackHandler(
        enabled = isActiveTab && isViewingDetail,
        backState = predictiveState,
        onBack = { selectedEmotion = null }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("emotion_dua_screen")
    ) {
        AnimatedContent(
            targetState = selectedEmotion,
            transitionSpec = {
                if (targetState == null) {
                    // Back to Grid
                    slideInHorizontally(animationSpec = tween(240)) { -it / 3 } + fadeIn(animationSpec = tween(240)) togetherWith
                            slideOutHorizontally(animationSpec = tween(200)) { it / 3 } + fadeOut(animationSpec = tween(200))
                } else {
                    // Into Detail
                    slideInHorizontally(animationSpec = tween(240)) { it / 3 } + fadeIn(animationSpec = tween(240)) togetherWith
                            slideOutHorizontally(animationSpec = tween(200)) { -it / 3 } + fadeOut(animationSpec = tween(200))
                }
            },
            label = "emotion_screen_transition"
        ) { currentEmotion ->
            if (currentEmotion == null) {
                // -------------------------------------------------------------
                // 1. EMOTION SELECTION GRID VIEW
                // -------------------------------------------------------------
                EmotionSelectionGridView(
                    onBack = onBack,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it },
                    onEmotionSelect = { emotion ->
                        focusManager.clearFocus()
                        selectedEmotion = emotion
                    }
                )
            } else {
                // -------------------------------------------------------------
                // 2. DUA DETAIL VIEW FOR SELECTED EMOTION
                // -------------------------------------------------------------
                EmotionDuaDetailView(
                    emotion = currentEmotion,
                    onBackToGrid = { selectedEmotion = null },
                    bookmarkedIds = bookmarkedIds,
                    onToggleBookmark = toggleBookmark
                )
            }
        }
    }
}

// =============================================================================
// EMOTION SELECTION GRID VIEW
// =============================================================================
@Composable
private fun EmotionSelectionGridView(
    onBack: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: EmotionCategoryFilter,
    onFilterSelect: (EmotionCategoryFilter) -> Unit,
    onEmotionSelect: (EmotionItem) -> Unit
) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText

    // Filter and search logic across all 50 emotions
    val filteredEmotions = remember(searchQuery, selectedFilter) {
        FeelingDuaData.ALL_EMOTIONS.filter { emotion ->
            emotion.matchesFilter(selectedFilter) && emotion.matchesQuery(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("emotion_grid_view")
    ) {
        // Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("feeling_back_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = textPrimary
                )
            }
            Column(modifier = Modifier.padding(start = 6.dp)) {
                Text(
                    text = "I am feeling…",
                    fontFamily = SerifHeaderFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        }

        Text(
            text = "Whatever you're feeling, take a moment to turn to Allah.",
            fontFamily = SpaceGrotesk,
            fontSize = 13.sp,
            color = textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        EmotionSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontally Scrollable Category Filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(EmotionCategoryFilter.entries) { filter ->
                val isSelected = filter == selectedFilter
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.semanticPrimaryAccent else Color.semanticSurface,
                    border = BorderStroke(1.dp, if (isSelected) Color.semanticPrimaryAccent else Color.semanticBorder),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onFilterSelect(filter) }
                        .testTag("filter_chip_${filter.name}")
                ) {
                    Text(
                        text = filter.label,
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.semanticAccentForeground else textPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Responsive 3-Column Emotion Grid
        if (filteredEmotions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = Color.semanticMutedText,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No emotions found",
                        fontFamily = SerifHeaderFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for a different feeling or clear your filter.",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEmotions, key = { it.id }) { emotion ->
                    EmotionGridCard(
                        emotion = emotion,
                        onClick = { onEmotionSelect(emotion) }
                    )
                }
            }
        }
    }
}

// =============================================================================
// SEARCH BAR COMPONENT
// =============================================================================
@Composable
private fun EmotionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val surfaceColor = Color.semanticSurface
    val borderColor = Color.semanticBorder

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search 50 emotions...",
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.sp,
                        color = Color.semanticMutedText
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontFamily = SpaceGrotesk
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.semanticPrimaryAccent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emotion_search_input")
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// EMOTION CARD IN GRID
// =============================================================================
@Composable
private fun EmotionGridCard(
    emotion: EmotionItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
        label = "emotionCardScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.semanticSurface,
        border = BorderStroke(1.dp, Color.semanticBorder),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .testTag("emotion_card_${emotion.id}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = emotion.name,
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.semanticPrimaryText,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =============================================================================
// DUA DETAIL VIEW FOR SELECTED EMOTION
// =============================================================================
@Composable
private fun EmotionDuaDetailView(
    emotion: EmotionItem,
    onBackToGrid: () -> Unit,
    bookmarkedIds: Set<String>,
    onToggleBookmark: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText

    val verifiedDuas = remember(emotion.id) {
        FeelingDuaData.getDuasForEmotion(emotion.id)
    }

    val primaryDua = verifiedDuas.firstOrNull()
    val isBookmarked = primaryDua != null && bookmarkedIds.contains(primaryDua.id)

    val copyWithFeedback = { text: String, label: String ->
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    val shareDua = { dua: VerifiedDuaRecord ->
        val shareText = """
            Feeling ${emotion.name} — Islamic Supplication:
            
            ${dua.arabic}
            
            Transliteration: ${dua.transliteration}
            
            Translation: ${dua.translation}
            
            Source: ${dua.sourceCollection} (${dua.referenceNumber}) • Grade: ${dua.authenticityGrade}
            Shared via FiveLight App
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Supplication")
        context.startActivity(shareIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp)
            .testTag("emotion_detail_view")
    ) {
        // Top Navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            IconButton(
                onClick = onBackToGrid,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = textPrimary
                )
            }

            Text(
                text = "Feeling ${emotion.name}",
                fontFamily = SerifHeaderFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            if (primaryDua != null) {
                IconButton(
                    onClick = { onToggleBookmark(primaryDua.id) },
                    modifier = Modifier.testTag("detail_bookmark_btn")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color.semanticPrimaryAccent else textSecondary
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SENSITIVE EMOTION HANDLING: SUICIDAL CRISIS SUPPORT BANNER
        if (emotion.id == "suicidal") {
            SuicidalCrisisSupportBanner(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        // VERIFIED DUAS DISPLAY OR FALLBACK
        if (verifiedDuas.isEmpty()) {
            NoVerifiedDuaFallback(
                emotionName = emotion.name,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            )
        } else {
            verifiedDuas.forEach { dua ->
                VerifiedDuaCard(
                    dua = dua,
                    emotionName = emotion.name,
                    isBookmarked = bookmarkedIds.contains(dua.id),
                    onToggleBookmark = { onToggleBookmark(dua.id) },
                    onCopyArabic = { copyWithFeedback(dua.arabic, "Arabic text") },
                    onCopyTransliteration = { copyWithFeedback(dua.transliteration, "Transliteration") },
                    onCopyTranslation = { copyWithFeedback(dua.translation, "English meaning") },
                    onCopyFull = {
                        val full = "${dua.arabic}\n\n${dua.transliteration}\n\n${dua.translation}\n\nSource: ${dua.sourceCollection} (${dua.referenceNumber})"
                        copyWithFeedback(full, "Complete supplication")
                    },
                    onShare = { shareDua(dua) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// =============================================================================
// VERIFIED DUA DISPLAY CARD
// =============================================================================
@Composable
private fun VerifiedDuaCard(
    dua: VerifiedDuaRecord,
    emotionName: String,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onCopyArabic: () -> Unit,
    onCopyTransliteration: () -> Unit,
    onCopyTranslation: () -> Unit,
    onCopyFull: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val accentColor = Color.semanticPrimaryAccent

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.semanticSurface,
        border = BorderStroke(1.dp, Color.semanticBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Title & Relevance Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dua.title,
                    fontFamily = SerifHeaderFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = dua.relevanceType.displayLabel,
                        fontFamily = SpaceGrotesk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. ARABIC TEXT (Centered, Large, Full Tashkeel, Elegant)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.semanticSurfaceElevated,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = dua.arabic,
                        fontFamily = SerifHeaderFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = onCopyArabic) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy Arabic",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. TRANSLITERATION SECTION
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TRANSLITERATION",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticMutedText,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onCopyTransliteration, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy transliteration",
                            tint = Color.semanticMutedText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dua.transliteration,
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp,
                    color = textPrimary,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(16.dp))

            // 3. ENGLISH MEANING SECTION
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ENGLISH MEANING",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticMutedText,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onCopyTranslation, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy translation",
                            tint = Color.semanticMutedText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dua.translation,
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp,
                    color = textPrimary,
                    lineHeight = 22.sp
                )
            }

            // 4. HISTORICAL / SPIRITUAL CONTEXT EXPLANATION
            if (dua.contextExplanation != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.semanticSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dua.contextExplanation,
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))

            // 5. HADITH SOURCE & AUTHENTICITY PROVENANCE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HADITH SOURCE & REFERENCE",
                        fontFamily = SpaceGrotesk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticMutedText,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${dua.sourceCollection} • ${dua.referenceNumber}",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.semanticSuccess.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = dua.authenticityGrade,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticSuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. BOTTOM ACTION BAR: Copy All & Share
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCopyFull,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint = textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy Full",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = Color.semanticAccentForeground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share Dua",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticAccentForeground
                    )
                }
            }
        }
    }
}

// =============================================================================
// SPECIAL HANDLING FOR SUICIDAL EMOTION: CRISIS SUPPORT BANNER
// =============================================================================
@Composable
private fun SuicidalCrisisSupportBanner(modifier: Modifier = Modifier) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.40f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.HealthAndSafety,
                    contentDescription = "Crisis Support",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "You are not alone. Help is available.",
                    fontFamily = SerifHeaderFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "You are deeply valuable and loved by Allah. Experiencing intense pain or dark thoughts is an overwhelming test and is never a sign of weak faith. Please reach out to someone who can support you right now:",
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = textSecondary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "• US / Canada: Call or text 988 (24/7 Crisis Lifeline)",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Text(
                    text = "• UK: Call 111 (NHS) or 116 123 (Samaritans)",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Text(
                    text = "• International: Visit befrienders.org for crisis support worldwide",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
            }
        }
    }
}

// =============================================================================
// FALLBACK WHEN NO SPECIFICALLY VERIFIED DUA IS PRESENT
// =============================================================================
@Composable
private fun NoVerifiedDuaFallback(
    emotionName: String,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.semanticSurface,
        border = BorderStroke(1.dp, Color.semanticBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Color.semanticPrimaryAccent,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Turn to Allah for feeling $emotionName",
                fontFamily = SerifHeaderFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No specifically relevant, authenticated supplication has been verified for this exact feeling yet. You can still turn to Allah in your own words — Allah understands the language of your heart in every state.",
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
