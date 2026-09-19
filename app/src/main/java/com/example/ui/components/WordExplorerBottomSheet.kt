package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranWordInfo
import com.example.data.model.WordOccurrence
import com.example.data.util.QuranData
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordExplorerBottomSheet(
    wordInfo: QuranWordInfo,
    onNavigateToVerse: (surahNumber: Int, verseNumber: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()

    // Determine occurrences: prioritize root occurrences, or lemma occurrences if root is absent
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Root occurrences, 1: Lemma occurrences

    val rootOccurrences = remember(wordInfo.root) {
        if (wordInfo.root.isNotBlank()) {
            QuranData.getOccurrencesForRoot(context, wordInfo.root)
        } else {
            emptyList()
        }
    }

    val lemmaOccurrences = remember(wordInfo.lemma) {
        if (wordInfo.lemma.isNotBlank()) {
            QuranData.getOccurrencesForLemma(context, wordInfo.lemma)
        } else {
            emptyList()
        }
    }

    val hasRoot = wordInfo.root.isNotBlank() && rootOccurrences.isNotEmpty()
    val hasLemma = wordInfo.lemma.isNotBlank() && lemmaOccurrences.isNotEmpty()

    // Active occurrences list based on tab
    val currentOccurrences = if (hasRoot && (selectedTab == 0 || !hasLemma)) {
        rootOccurrences
    } else {
        lemmaOccurrences
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.semanticSurface,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.semanticSecondaryText.copy(alpha = 0.35f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("word_explorer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.semanticPrimaryAccent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ع",
                            fontFamily = AmiriFont,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticPrimaryAccent
                        )
                    }
                    Column {
                        Text(
                            text = "Arabic Word Explorer",
                            fontFamily = SerifHeaderFont,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticPrimaryText
                        )
                        if (wordInfo.surahNumber > 0 && wordInfo.verseNumber > 0) {
                            Text(
                                text = "Verse ${wordInfo.surahNumber}:${wordInfo.verseNumber}",
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.5.sp,
                                color = Color.semanticMutedText
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.semanticPrimaryText.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Primary Word Showcase Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color.semanticSurfaceElevated else Color.semanticControl,
                border = BorderStroke(1.dp, Color.semanticBorder.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Exact Arabic Word in Quran Text
                    Text(
                        text = wordInfo.exactArabic,
                        fontFamily = AmiriFont,
                        fontSize = 38.sp,
                        lineHeight = 52.sp,
                        color = Color.semanticPrimaryAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Transliteration
                    if (wordInfo.transliteration.isNotBlank()) {
                        Text(
                            text = wordInfo.transliteration,
                            fontFamily = SpaceGrotesk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Direct Contextual Meaning
                    if (wordInfo.meaning.isNotBlank()) {
                        Text(
                            text = wordInfo.meaning,
                            fontFamily = SpaceGrotesk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.semanticSecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(
                        color = Color.semanticBorder.copy(alpha = 0.4f),
                        thickness = 0.75.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Linguistic Anatomy Pill Grid: Root, Lemma, Part of Speech
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Root
                        LinguisticAttributePill(
                            label = "ROOT",
                            value = if (wordInfo.root.isNotBlank()) wordInfo.root else "—",
                            isArabic = wordInfo.root.isNotBlank()
                        )

                        // Lemma
                        LinguisticAttributePill(
                            label = "LEMMA",
                            value = if (wordInfo.lemma.isNotBlank()) wordInfo.lemma else "—",
                            isArabic = wordInfo.lemma.isNotBlank()
                        )

                        // Part of Speech
                        LinguisticAttributePill(
                            label = "POS",
                            value = if (wordInfo.partOfSpeech.isNotBlank()) wordInfo.partOfSpeech else "Particle",
                            isArabic = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Occurrences Section
            if (hasRoot && hasLemma) {
                // Tab switcher between Root and Lemma
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.semanticSurfaceElevated)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OccurrenceTabButton(
                        title = "Root (${wordInfo.root})",
                        count = rootOccurrences.size,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    OccurrenceTabButton(
                        title = "Form (${wordInfo.lemma})",
                        count = lemmaOccurrences.size,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasRoot) "Occurrences of Root (${wordInfo.root})" else if (hasLemma) "Occurrences of Form (${wordInfo.lemma})" else "Quran Occurrences",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.semanticPrimaryText
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.semanticPrimaryAccent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${currentOccurrences.size} verses",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Occurrences List
            if (currentOccurrences.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unique single occurrence in the Quran.",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = Color.semanticMutedText
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = currentOccurrences,
                        key = { "${it.surahNumber}:${it.verseNumber}" }
                    ) { occ ->
                        WordOccurrenceCard(
                            occurrence = occ,
                            onClick = {
                                onNavigateToVerse(occ.surahNumber, occ.verseNumber)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinguisticAttributePill(
    label: String,
    value: String,
    isArabic: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = SpaceGrotesk,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.semanticMutedText,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.semanticSurface,
            border = BorderStroke(1.dp, Color.semanticBorder.copy(alpha = 0.5f))
        ) {
            Text(
                text = value,
                fontFamily = if (isArabic) AmiriFont else SpaceGrotesk,
                fontSize = if (isArabic) 17.sp else 12.sp,
                fontWeight = if (isArabic) FontWeight.SemiBold else FontWeight.Medium,
                color = Color.semanticPrimaryText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun OccurrenceTabButton(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.semanticSurface else Color.Transparent,
        animationSpec = tween(150),
        label = "tab_bg"
    )
    val textColor = if (isSelected) Color.semanticPrimaryAccent else Color.semanticMutedText

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        color = bgColor,
        border = if (isSelected) BorderStroke(0.5.dp, Color.semanticBorder.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = SpaceGrotesk,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
            Spacer(modifier = Modifier.width(5.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color.semanticPrimaryAccent.copy(alpha = 0.15f) else Color.semanticBorder.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "$count",
                    fontFamily = SpaceGrotesk,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun WordOccurrenceCard(
    occurrence: WordOccurrence,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.semanticSurfaceElevated.copy(alpha = 0.6f),
        border = BorderStroke(0.75.dp, Color.semanticBorder.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.semanticPrimaryAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Surah ${occurrence.surahNameEnglish} ${occurrence.surahNumber}:${occurrence.verseNumber}",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = Color.semanticPrimaryAccent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Open",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        color = Color.semanticMutedText
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.semanticMutedText,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            if (occurrence.textArabic.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = occurrence.textArabic,
                    fontFamily = AmiriFont,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = Color.semanticPrimaryText,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (occurrence.textEnglish.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = occurrence.textEnglish,
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color.semanticSecondaryText,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
