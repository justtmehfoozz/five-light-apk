package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BookmarkEntity
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.data.util.QuranData
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.SerifHeaderFont

@Composable
fun QuranScreen(
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
    playingVerseNumber: Int?,
    isPlayingAudio: Boolean,
    onPlayVerseAudio: (Verse) -> Unit,
    bookmarks: List<BookmarkEntity>,
    onToggleBookmark: (Verse, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Surahs, 1: Bookmarks
    var isReadingViewActive by remember { mutableStateOf(false) }
    var showFontSizeControls by remember { mutableStateOf(false) }

    val bgContainer = if (isNightReadingMode) Color(0xFF10141D) else MaterialTheme.colorScheme.background
    val textPrimary = if (isNightReadingMode) Color(0xFFF4F1EA) else MaterialTheme.colorScheme.onSurface
    val cardBg = if (isNightReadingMode) Color(0xFF19202D) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgContainer)
    ) {
        if (isReadingViewActive && selectedSurah != null) {
            // Detailed Verse Reader View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

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
                            imageVector = Icons.Filled.ArrowBack,
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
                            color = MaterialTheme.colorScheme.primary
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

                        IconButton(onClick = onToggleNightReading) {
                            Icon(
                                imageVector = Icons.Outlined.Nightlight,
                                contentDescription = "Night Mode",
                                tint = if (isNightReadingMode) MaterialTheme.colorScheme.primary else textPrimary.copy(alpha = 0.5f)
                            )
                        }
                    }
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

                Spacer(modifier = Modifier.height(12.dp))

                // Verses List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(verses) { verse ->
                        val isBookmarked = bookmarks.any { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }
                        val isPlaying = playingVerseNumber == verse.verseNumber && isPlayingAudio

                        VerseCard(
                            verse = verse,
                            fontSizeSp = fontSizeSp,
                            showTranslation = showEnglishTranslation,
                            isNightMode = isNightReadingMode,
                            isPlaying = isPlaying,
                            isBookmarked = isBookmarked,
                            onPlayAudio = { onPlayVerseAudio(verse) },
                            onToggleBookmark = { onToggleBookmark(verse, isBookmarked) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
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
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Holy Quran",
                    style = MaterialTheme.typography.displayMedium.copy(fontFamily = SerifHeaderFont),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: All Surahs vs Bookmarks
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("All Surahs (114)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Bookmarks (${bookmarks.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // Filtered Surahs List
                    val filteredSurahs = QuranData.SURAHS_DIRECTORY.filter {
                        it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                                it.englishTranslation.contains(searchQuery, ignoreCase = true) ||
                                it.number.toString() == searchQuery
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredSurahs) { surah ->
                            SurahListItem(
                                surah = surah,
                                onClick = {
                                    onSelectSurah(surah)
                                    isReadingViewActive = true
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                } else {
                    // Bookmarks List
                    if (bookmarks.isEmpty()) {
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
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(bookmarks) { bookmark ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${bookmark.surahNameEnglish} (${bookmark.surahNameArabic}) - Verse ${bookmark.verseNumber}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = bookmark.verseTextArabic,
                                            style = ArabicTextStyle.copy(fontSize = 20.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = bookmark.verseTextTranslation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Surah Number Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = surah.nameEnglish,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${surah.englishTranslation} • ${surah.versesCount} verses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = surah.nameArabic,
                style = ArabicTextStyle.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun VerseCard(
    verse: Verse,
    fontSizeSp: Float,
    showTranslation: Boolean,
    isNightMode: Boolean,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isNightMode) Color(0xFF1C2433) else MaterialTheme.colorScheme.surface
    val textPrimary = if (isNightMode) Color(0xFFF4F1EA) else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Action bar for verse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Verse ${verse.verseNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play Audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else textPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Uthmani Arabic Text Rendered Large & Clear
            Text(
                text = verse.textArabic,
                style = ArabicTextStyle.copy(fontSize = fontSizeSp.sp, lineHeight = (fontSizeSp * 1.8f).sp),
                color = textPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            if (showTranslation && verse.textEnglish.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = verse.textEnglish,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = if (isNightMode) Color(0xFFB0AC9F) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
