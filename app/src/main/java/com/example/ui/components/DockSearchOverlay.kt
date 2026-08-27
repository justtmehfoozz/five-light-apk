package com.example.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrPreset
import com.example.data.model.NameOfAllah
import com.example.data.model.NamesOfAllahData
import com.example.data.model.Surah
import com.example.data.util.DailyContentProvider
import com.example.data.util.DailyDua
import com.example.data.util.DuaData
import com.example.data.util.DuaItem
import com.example.data.util.GlobalSearchEngine
import com.example.data.util.QuranData
import com.example.ui.screens.AdhkarItem
import com.example.ui.screens.eveningAdhkar
import com.example.ui.screens.getSavedDuaBookmarks
import com.example.ui.screens.morningAdhkar
import com.example.ui.screens.toggleDuaBookmark
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Defines the search scope within the Explore section.
 */
enum class ExploreSearchScope {
    GLOBAL_EXPLORE,
    DUA_LIBRARY,
    NAMES_OF_ALLAH,
    DAILY_ADHKAR
}

// Unified Search Result Types
sealed class SearchResultItem {
    data class SurahResult(val surah: Surah) : SearchResultItem()
    data class NameResult(val name: NameOfAllah) : SearchResultItem()
    data class DailyDuaResult(val dua: DailyDua) : SearchResultItem()
    data class DuaLibraryResult(val dua: DuaItem) : SearchResultItem()
    data class DhikrPresetResult(val dhikr: DhikrPreset) : SearchResultItem()
    data class AdhkarResult(val item: AdhkarItem) : SearchResultItem()
}

@Composable
fun DockSearchResultsPanel(
    searchQuery: String,
    scope: ExploreSearchScope = ExploreSearchScope.GLOBAL_EXPLORE,
    allDhikrs: List<DhikrPreset> = emptyList(),
    onSelectSurah: (Surah) -> Unit = {},
    onSelectDua: (DailyDua) -> Unit = {},
    onSelectDuaItem: (DuaItem) -> Unit = {},
    onSelectDhikr: (DhikrPreset) -> Unit = {},
    onSelectAdhkarItem: (AdhkarItem) -> Unit = {},
    onSelectNameOfAllah: (NameOfAllah) -> Unit = {},
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.semanticBackground
    val borderColor = if (isDark) Color.White.copy(alpha = 0.12f) else LightBorder.copy(alpha = 0.35f)
    val textPrimary = if (isDark) Color(0xFFF2F2EE) else Color.semanticPrimaryText
    val textSecondary = if (isDark) Color(0xFFA8A8A2) else LightMutedText.copy(alpha = 0.8f)
    val accentGold = if (isDark) Color(0xFFE8DCC4) else Color.semanticPrimaryAccent
    val sectionHeaderColor = if (isDark) Color(0xFFA8A8A2) else Color.semanticMutedText
    val activeBookmarkColor = if (isDark) Color(0xFF494556) else Color(0xFF8D6B1E)

    // Reactive observation of saved Dua bookmarks from the single source of truth
    var bookmarkedDuaIds by remember { mutableStateOf(getSavedDuaBookmarks(context)) }

    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("dua_bookmarks_prefs", Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "bookmarked_dua_ids") {
                bookmarkedDuaIds = getSavedDuaBookmarks(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        bookmarkedDuaIds = getSavedDuaBookmarks(context)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val cleanQuery = searchQuery.trim()
    val isQueryBlank = cleanQuery.isBlank()

    // 1. Surah Search (Global only)
    val matchingSurahs = remember(cleanQuery, scope) {
        if (isQueryBlank || scope != ExploreSearchScope.GLOBAL_EXPLORE) {
            emptyList()
        } else {
            GlobalSearchEngine.searchSurahs(cleanQuery)
        }
    }

    // 2. Names of Allah Search (Global or Names of Allah scope)
    val matchingNames = remember(cleanQuery, scope) {
        if (isQueryBlank || (scope != ExploreSearchScope.GLOBAL_EXPLORE && scope != ExploreSearchScope.NAMES_OF_ALLAH)) {
            emptyList()
        } else {
            GlobalSearchEngine.searchNamesOfAllah(cleanQuery)
        }
    }

    // 3. Duas Search (Global or Dua Library scope)
    val matchingDailyDuas = remember(cleanQuery, scope) {
        if (isQueryBlank || (scope != ExploreSearchScope.GLOBAL_EXPLORE && scope != ExploreSearchScope.DUA_LIBRARY)) {
            emptyList()
        } else {
            GlobalSearchEngine.searchDailyDuas(cleanQuery)
        }
    }

    val matchingLibraryDuas = remember(cleanQuery, scope) {
        if (isQueryBlank || (scope != ExploreSearchScope.GLOBAL_EXPLORE && scope != ExploreSearchScope.DUA_LIBRARY)) {
            emptyList()
        } else {
            GlobalSearchEngine.searchLibraryDuas(cleanQuery)
        }
    }

    // 4. Adhkar Search (Global or Daily Adhkar scope)
    val matchingDhikrs = remember(cleanQuery, scope, allDhikrs) {
        if (isQueryBlank || (scope != ExploreSearchScope.GLOBAL_EXPLORE && scope != ExploreSearchScope.DAILY_ADHKAR)) {
            emptyList()
        } else {
            GlobalSearchEngine.searchDhikrPresets(cleanQuery, allDhikrs)
        }
    }

    val combinedDailyAdhkar = remember(morningAdhkar, eveningAdhkar) {
        (morningAdhkar + eveningAdhkar).distinctBy { it.title }
    }

    val matchingDailyAdhkar = remember(cleanQuery, scope, combinedDailyAdhkar) {
        if (isQueryBlank || (scope != ExploreSearchScope.GLOBAL_EXPLORE && scope != ExploreSearchScope.DAILY_ADHKAR)) {
            emptyList()
        } else {
            GlobalSearchEngine.searchDailyAdhkar(cleanQuery, combinedDailyAdhkar)
        }
    }

    val hasResults = matchingSurahs.isNotEmpty() ||
            matchingNames.isNotEmpty() ||
            matchingDailyDuas.isNotEmpty() ||
            matchingLibraryDuas.isNotEmpty() ||
            matchingDhikrs.isNotEmpty() ||
            matchingDailyAdhkar.isNotEmpty()

    // Determine content state: IDLE_EMPTY, NO_RESULTS, or RESULTS
    val contentState = when {
        isQueryBlank -> SearchState.IDLE
        !hasResults -> SearchState.NO_RESULTS
        else -> SearchState.RESULTS
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        AnimatedContent(
            targetState = contentState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                 scaleIn(initialScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing))) togetherWith
                (fadeOut(animationSpec = tween(120, easing = FastOutLinearInEasing)) +
                 scaleOut(targetScale = 0.98f, animationSpec = tween(120, easing = FastOutLinearInEasing)) +
                 slideOutVertically(targetOffsetY = { -15 }, animationSpec = tween(120)))
            },
            label = "search_state_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { targetState ->
            when (targetState) {
                SearchState.IDLE -> {
                    // Clean Empty/Discovery State
                    val emptyTitle = when (scope) {
                        ExploreSearchScope.GLOBAL_EXPLORE -> "Search the Qur’an, duas, adhkar & more"
                        ExploreSearchScope.DUA_LIBRARY -> "Search duas or categories"
                        ExploreSearchScope.NAMES_OF_ALLAH -> "Search the 99 Beautiful Names"
                        ExploreSearchScope.DAILY_ADHKAR -> "Search adhkar"
                    }
                    val emptySubtitle = when (scope) {
                        ExploreSearchScope.GLOBAL_EXPLORE -> "Find Surahs, supplications, divine names & daily remembrance"
                        ExploreSearchScope.DUA_LIBRARY -> "Find supplications by title, category, meaning or Arabic text"
                        ExploreSearchScope.NAMES_OF_ALLAH -> "Find by divine name, English meaning, Arabic or number"
                        ExploreSearchScope.DAILY_ADHKAR -> "Find morning, evening & tasbeeh remembrances"
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDark) Color(0xFF2C2C2E) else Color.semanticSurfaceElevated
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = emptyTitle,
                                fontFamily = SerifHeaderFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = emptySubtitle,
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                SearchState.NO_RESULTS -> {
                    // No Results State
                    val noResultsMessage = when (scope) {
                        ExploreSearchScope.GLOBAL_EXPLORE -> "No results found"
                        ExploreSearchScope.DUA_LIBRARY -> "No matching duas found"
                        ExploreSearchScope.NAMES_OF_ALLAH -> "No matching Names found"
                        ExploreSearchScope.DAILY_ADHKAR -> "No matching adhkar found"
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = noResultsMessage,
                                fontFamily = SerifHeaderFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching with a different term or keyword",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                SearchState.RESULTS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        // 1. QURAN SURAHS SECTION
                        if (matchingSurahs.isNotEmpty()) {
                            item(key = "header_surahs") {
                                SearchSectionHeader(
                                    title = "QURAN SURAHS",
                                    color = sectionHeaderColor,
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                        fadeOutSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing),
                                        placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                    )
                                )
                            }
                            itemsIndexed(matchingSurahs, key = { _, s -> "surah_${s.number}" }) { _, surah ->
                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                            placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            keyboardController?.hide()
                                            onSelectSurah(surah)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF2C2C2E) else Color.semanticSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${surah.number}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFF2F2EE) else Color.semanticPrimaryText
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = surah.nameEnglish,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "${surah.englishTranslation} • ${surah.revelationPlace} (${if (surah.number == 1) 6 else surah.versesCount} verses)",
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = surah.nameArabic,
                                        fontSize = 16.sp,
                                        color = accentGold
                                    )
                                }
                            }
                        }

                        // 2. NAMES OF ALLAH SECTION
                        if (matchingNames.isNotEmpty()) {
                            item(key = "header_names") {
                                Column(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                        fadeOutSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing),
                                        placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                    )
                                ) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SearchSectionHeader(
                                        title = "NAMES OF ALLAH",
                                        color = sectionHeaderColor
                                    )
                                }
                            }
                            itemsIndexed(matchingNames, key = { _, n -> "name_${n.number}" }) { _, name ->
                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                            placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            keyboardController?.hide()
                                            onSelectNameOfAllah(name)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF2C2C2E) else Color.semanticSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${name.number}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name.nameTransliteration,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = name.englishMeaning,
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name.nameArabic,
                                        fontSize = 16.sp,
                                        color = accentGold
                                    )
                                }
                            }
                        }

                        // 3. DUAS SECTION
                        if (matchingDailyDuas.isNotEmpty() || matchingLibraryDuas.isNotEmpty()) {
                            item(key = "header_duas") {
                                Column(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                        fadeOutSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing),
                                        placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                    )
                                ) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SearchSectionHeader(
                                        title = if (scope == ExploreSearchScope.DUA_LIBRARY) "MATCHING DUAS" else "DUAS & SUPPLICATIONS",
                                        color = sectionHeaderColor
                                    )
                                }
                            }
                            // Daily Duas
                            itemsIndexed(matchingDailyDuas, key = { _, d -> "daily_dua_${d.id}" }) { _, dua ->
                                val matchedDuaItem = remember(dua) {
                                    val normAr = GlobalSearchEngine.normalizeArabic(dua.arabic)
                                    DuaData.ALL_DUAS.find {
                                        GlobalSearchEngine.normalizeArabic(it.arabic) == normAr ||
                                        it.translation.contains(dua.translation, ignoreCase = true) ||
                                        dua.translation.contains(it.translation, ignoreCase = true)
                                    }
                                }
                                val isBookmarked = matchedDuaItem?.let { bookmarkedDuaIds.contains(it.id) } ?: false

                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                            placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            keyboardController?.hide()
                                            onSelectDua(dua)
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            matchedDuaItem?.let { item ->
                                                toggleDuaBookmark(context, item.id)
                                                bookmarkedDuaIds = getSavedDuaBookmarks(context)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark dua",
                                            tint = if (isBookmarked) activeBookmarkColor else textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dua.transliteration.removeSurrounding("\""),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${dua.translation} • ${dua.reference}",
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // Library Duas
                            itemsIndexed(matchingLibraryDuas, key = { _, d -> "lib_dua_${d.title}_${d.category}" }) { _, dua ->
                                val isBookmarked = bookmarkedDuaIds.contains(dua.id)

                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                            placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            keyboardController?.hide()
                                            onSelectDuaItem(dua)
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            toggleDuaBookmark(context, dua.id)
                                            bookmarkedDuaIds = getSavedDuaBookmarks(context)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark dua",
                                            tint = if (isBookmarked) activeBookmarkColor else textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dua.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isDark) Color(0xFF2C2C2E) else Color.semanticSurfaceElevated,
                                                border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.08f) else LightBorder.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = dua.category,
                                                    fontFamily = SpaceGrotesk,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = dua.translation,
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // 4. ADHKAR SECTION
                        if (matchingDhikrs.isNotEmpty() || matchingDailyAdhkar.isNotEmpty()) {
                            item(key = "header_adhkar") {
                                Column(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                        fadeOutSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing),
                                        placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                    )
                                ) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SearchSectionHeader(
                                        title = if (scope == ExploreSearchScope.DAILY_ADHKAR) "MATCHING ADHKAR" else "DAILY ADHKAR",
                                        color = sectionHeaderColor
                                    )
                                }
                            }
                            // Dhikr Presets
                            itemsIndexed(matchingDhikrs, key = { _, d -> "dhikr_${d.id}" }) { _, dhikr ->
                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                            placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            keyboardController?.hide()
                                            onSelectDhikr(dhikr)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RadioButtonChecked,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dhikr.nameEnglish,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "${dhikr.translation} (Target: ${dhikr.defaultTarget})",
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dhikr.nameArabic,
                                        fontSize = 16.sp,
                                        color = accentGold
                                    )
                                }
                            }
                            // Daily Adhkar Items
                            itemsIndexed(matchingDailyAdhkar, key = { _, a -> "adhkar_${a.title}" }) { _, adhkar ->
                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                            placementSpec = spring(dampingRatio = 0.85f, stiffness = 450f)
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            keyboardController?.hide()
                                            onSelectAdhkarItem(adhkar)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RadioButtonChecked,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = adhkar.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "${adhkar.translation} (${adhkar.count}x)",
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = adhkar.arabic,
                                        fontSize = 15.sp,
                                        color = accentGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontFamily = SpaceGrotesk,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = color,
        modifier = modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp)
    )
}

private enum class SearchState {
    IDLE,
    NO_RESULTS,
    RESULTS
}
