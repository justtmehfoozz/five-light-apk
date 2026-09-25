package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranWordInfo
import com.example.data.model.WordOccurrence
import com.example.data.util.QuranData
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurface

private val AuthoritativeHorizontalPadding = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordExplorerBottomSheet(
    wordInfo: QuranWordInfo,
    onNavigateToVerse: (surahNumber: Int, verseNumber: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Occurrence selection tab (0: Root family, 1: This form)
    var selectedTab by remember { mutableIntStateOf(0) }

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
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.semanticSecondaryText.copy(alpha = 0.28f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("word_explorer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // 1. Header Bar (Left-aligned title + secondary verse, right-aligned close button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuthoritativeHorizontalPadding, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arabic Word Explorer",
                        fontFamily = SerifHeaderFont,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticPrimaryText,
                        letterSpacing = (-0.3).sp
                    )
                    if (wordInfo.surahNumber > 0 && wordInfo.verseNumber > 0) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Verse ${wordInfo.surahNumber}:${wordInfo.verseNumber}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            color = Color.semanticMutedText
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.semanticSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = AuthoritativeHorizontalPadding, vertical = 4.dp),
                color = Color.semanticBorder.copy(alpha = 0.25f),
                thickness = 0.75.dp
            )

            // Smooth word transition animation when user taps a different word
            AnimatedContent(
                targetState = wordInfo,
                transitionSpec = {
                    fadeIn(animationSpec = tween(160)) togetherWith fadeOut(animationSpec = tween(120))
                },
                label = "word_explorer_content"
            ) { currentWord ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 2. Selected Word Hero (Strictly Centered Typographic Specimen)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AuthoritativeHorizontalPadding, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = currentWord.exactArabic,
                                fontFamily = AmiriFont,
                                fontSize = 38.sp,
                                lineHeight = 48.sp,
                                color = Color.semanticPrimaryText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (currentWord.transliteration.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentWord.transliteration,
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.semanticPrimaryAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (currentWord.meaning.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentWord.meaning,
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = Color.semanticSecondaryText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 3. Centered 3-Column Linguistic Metadata (Equal-width columns: ROOT / LEMMA / POS)
                    val hasProfile = currentWord.root.isNotBlank() || currentWord.lemma.isNotBlank() || currentWord.partOfSpeech.isNotBlank()
                    if (hasProfile) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AuthoritativeHorizontalPadding),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Column 1: ROOT
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ROOT",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentWord.root.isNotBlank()) currentWord.root else "—",
                                    fontFamily = if (currentWord.root.isNotBlank()) AmiriFont else SpaceGrotesk,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.semanticPrimaryText,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Column 2: LEMMA
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "LEMMA",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentWord.lemma.isNotBlank()) currentWord.lemma else "—",
                                    fontFamily = if (currentWord.lemma.isNotBlank()) AmiriFont else SpaceGrotesk,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.semanticPrimaryText,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Column 3: POS
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "POS",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentWord.partOfSpeech.isNotBlank()) currentWord.partOfSpeech else "—",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.semanticPrimaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 4. Centered 2-Column Summary: ROOT FAMILY / THIS FORM
                    if (hasRoot || hasLemma) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AuthoritativeHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            if (hasRoot) {
                                val isSelected = selectedTab == 0 || !hasLemma
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(enabled = hasLemma) { selectedTab = 0 }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ROOT FAMILY",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                        color = if (isSelected) Color.semanticPrimaryAccent else Color.semanticMutedText,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${currentWord.root} · ${rootOccurrences.size} occurrences",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.semanticPrimaryText else Color.semanticSecondaryText,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                    if (hasLemma) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(1.5.dp)
                                                .background(if (isSelected) Color.semanticPrimaryAccent else Color.Transparent)
                                        )
                                    }
                                }
                            }

                            if (hasLemma) {
                                val isSelected = selectedTab == 1 || !hasRoot
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(enabled = hasRoot) { selectedTab = 1 }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "THIS FORM",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                        color = if (isSelected) Color.semanticPrimaryAccent else Color.semanticMutedText,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${currentWord.lemma} · ${lemmaOccurrences.size} occurrences",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.semanticPrimaryText else Color.semanticSecondaryText,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                    if (hasRoot) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(1.5.dp)
                                                .background(if (isSelected) Color.semanticPrimaryAccent else Color.Transparent)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = AuthoritativeHorizontalPadding, vertical = 6.dp),
                        color = Color.semanticBorder.copy(alpha = 0.25f),
                        thickness = 0.75.dp
                    )

                    // 5. Centered Occurrences Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AuthoritativeHorizontalPadding, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "QURAN OCCURRENCES",
                            fontFamily = SpaceGrotesk,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = Color.semanticMutedText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "${currentOccurrences.size} in the Quran",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.semanticSecondaryText,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 6. Editorial Occurrences Reading List
                    if (currentOccurrences.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
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
                                .heightIn(max = 300.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(
                                items = currentOccurrences,
                                key = { "${it.surahNumber}:${it.verseNumber}" }
                            ) { occ ->
                                val isCurrentVerse = occ.surahNumber == currentWord.surahNumber && occ.verseNumber == currentWord.verseNumber
                                WordOccurrenceEditorialItem(
                                    occurrence = occ,
                                    currentWord = currentWord,
                                    selectedTab = selectedTab,
                                    isCurrentVerse = isCurrentVerse,
                                    onClick = {
                                        onNavigateToVerse(occ.surahNumber, occ.verseNumber)
                                        onDismiss()
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = AuthoritativeHorizontalPadding),
                                    color = Color.semanticBorder.copy(alpha = 0.15f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordOccurrenceEditorialItem(
    occurrence: WordOccurrence,
    currentWord: QuranWordInfo,
    selectedTab: Int,
    isCurrentVerse: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val verseWords = remember(occurrence.surahNumber, occurrence.verseNumber) {
        QuranData.getWordsForVerse(context, occurrence.surahNumber, occurrence.verseNumber)
    }

    val primaryAccent = Color.semanticPrimaryAccent

    val annotatedArabic = remember(occurrence.textArabic, verseWords, currentWord, selectedTab, primaryAccent) {
        buildAnnotatedArabicVerse(
            textArabic = occurrence.textArabic,
            verseWords = verseWords,
            targetRoot = if (selectedTab == 0 && currentWord.root.isNotBlank()) currentWord.root else null,
            targetLemma = if (selectedTab == 1 && currentWord.lemma.isNotBlank()) currentWord.lemma else null,
            targetExact = currentWord.exactArabic,
            highlightColor = primaryAccent
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuthoritativeHorizontalPadding, vertical = 10.dp)
    ) {
        // Reference & Action Row (Left-aligned reference, Right-aligned action across full width)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${occurrence.surahNameEnglish} ${occurrence.surahNumber}:${occurrence.verseNumber}",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.semanticPrimaryText
                )
                if (isCurrentVerse) {
                    Text(
                        text = "· Current",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryAccent
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Open",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.semanticPrimaryAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.semanticPrimaryAccent,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Arabic Verse Context with subtle highlight on matched word (RTL, high contrast)
        if (occurrence.textArabic.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = annotatedArabic,
                    fontFamily = AmiriFont,
                    fontSize = 17.5.sp,
                    lineHeight = 27.sp,
                    color = Color.semanticPrimaryText,
                    textAlign = TextAlign.Start,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // English Translation Context (LTR, comfortable line height, natural wrapping)
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

/**
 * Builds an AnnotatedString for Arabic verse text where occurrences of the target word/root/lemma
 * are subtly highlighted with subtle underline and accent tone, preserving the original Quran text.
 */
private fun buildAnnotatedArabicVerse(
    textArabic: String,
    verseWords: List<QuranWordInfo>,
    targetRoot: String?,
    targetLemma: String?,
    targetExact: String,
    highlightColor: Color
): AnnotatedString {
    val tokens = textArabic.split(" ")
    val normTargetExact = QuranData.normalizeArabic(targetExact)

    return buildAnnotatedString {
        tokens.forEachIndexed { index, token ->
            val matchingWordInfo = verseWords.getOrNull(index)

            val isMatch = when {
                targetRoot != null && matchingWordInfo != null -> matchingWordInfo.root == targetRoot
                targetLemma != null && matchingWordInfo != null -> matchingWordInfo.lemma == targetLemma
                matchingWordInfo != null -> {
                    matchingWordInfo.exactArabic == targetExact ||
                        QuranData.normalizeArabic(matchingWordInfo.exactArabic) == normTargetExact
                }
                else -> QuranData.normalizeArabic(token) == normTargetExact
            }

            if (isMatch) {
                pushStyle(
                    SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )
                append(token)
                pop()
            } else {
                append(token)
            }

            if (index < tokens.size - 1) {
                append(" ")
            }
        }
    }
}
