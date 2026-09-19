package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Surah
import com.example.data.model.SurahOverview
import com.example.data.model.SurahSection
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.rememberIsReducedMotion
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurfaceElevated

/**
 * SurahOverviewContent displays the editorial FiveLight Surah Overview & Surah Map.
 * Fits seamlessly below the existing Reader header and sub-navigation.
 *
 * @param surah Current Surah object
 * @param overview Structural metadata and verified sections from surah_structure.json
 * @param currentReadingVerseNumber Current verse being read in the Reader to indicate active section
 * @param onNavigateToVerse Callback invoked when a user taps a section to read
 * @param modifier Root modifier
 * @param listState Scroll state for the overview list
 */
@Composable
fun SurahOverviewContent(
    surah: Surah,
    overview: SurahOverview?,
    currentReadingVerseNumber: Int = 1,
    onNavigateToVerse: (verseNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val isReducedMotion = rememberIsReducedMotion()
    var isRevealed by remember { mutableStateOf(isReducedMotion) }

    LaunchedEffect(surah.number) {
        if (!isReducedMotion) {
            isRevealed = true
        }
    }

    val headerAlpha by animateFloatAsState(
        targetValue = if (isRevealed || isReducedMotion) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "overview_header_alpha"
    )

    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val textMuted = Color.semanticMutedText
    val borderCol = Color.semanticBorder

    val sections = overview?.sections ?: emptyList()
    val isSingleSection = sections.size <= 1

    // Clean Juz label formatting without "Juz Juz" duplication
    val juzText = remember(overview?.juzDisplay) {
        val raw = overview?.juzDisplay?.trim() ?: ""
        if (raw.isBlank()) {
            "Juz -"
        } else if (raw.startsWith("Juz", ignoreCase = true)) {
            raw
        } else {
            "Juz $raw"
        }
    }

    val revelationText = remember(overview?.revelationPlace, overview?.revelationOrder, surah.revelationPlace) {
        val place = overview?.revelationPlace?.ifBlank { null } ?: surah.revelationPlace
        val order = if (overview != null && overview.revelationOrder > 0) " · Revelation ${overview.revelationOrder}" else ""
        "$place$order"
    }

    val versesText = remember(juzText, surah.versesCount) {
        "$juzText · ${surah.versesCount} verses"
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("surah_overview_content"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 220.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 1. Overview-Specific Metadata Block
        item(key = "overview_metadata_header", contentType = "overview_metadata_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(headerAlpha)
                    .padding(top = 2.dp, bottom = 12.dp)
                    .testTag("surah_overview_metadata"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = revelationText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = versesText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(
                    color = borderCol.copy(alpha = 0.35f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // 2. Section Heading: Surah Map + Count
        item(key = "overview_map_heading", contentType = "overview_map_heading") {
            val secCount = overview?.sectionCount ?: if (sections.isNotEmpty()) sections.size else 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(headerAlpha)
                    .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SURAH MAP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = textMuted
                )

                Text(
                    text = if (secCount == 1) "1 Section" else "$secCount Sections",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    ),
                    color = textMuted
                )
            }
        }

        // 3. Continuous Vertical Structural Timeline Map
        if (isSingleSection) {
            val singleSection = sections.firstOrNull() ?: SurahSection(
                sectionNumber = 1,
                startVerse = 1,
                endVerse = surah.versesCount,
                verseCount = surah.versesCount,
                openingExcerpt = ""
            )

            item(key = "single_section_timeline") {
                SurahMapTimelineRow(
                    section = singleSection,
                    isFirst = true,
                    isLast = true,
                    isSingle = true,
                    isCurrentSection = true,
                    itemAlpha = headerAlpha,
                    onClick = { onNavigateToVerse(singleSection.startVerse) }
                )
            }
        } else {
            itemsIndexed(
                items = sections,
                key = { _, section -> "section_${section.sectionNumber}_${section.startVerse}" },
                contentType = { _, _ -> "surah_map_timeline_node" }
            ) { index, section ->
                val isCurrent = currentReadingVerseNumber in section.startVerse..section.endVerse

                val itemAlpha by animateFloatAsState(
                    targetValue = if (isRevealed || isReducedMotion) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 280,
                        delayMillis = if (isReducedMotion) 0 else (index * 18).coerceAtMost(280),
                        easing = FastOutSlowInEasing
                    ),
                    label = "item_alpha_$index"
                )

                SurahMapTimelineRow(
                    section = section,
                    isFirst = index == 0,
                    isLast = index == sections.lastIndex,
                    isSingle = false,
                    isCurrentSection = isCurrent,
                    itemAlpha = itemAlpha,
                    onClick = { onNavigateToVerse(section.startVerse) }
                )
            }
        }
    }
}

/**
 * Editorial timeline row for the continuous Surah Map.
 * Connected seamlessly via a structural vertical line with nodes and typography.
 */
@Composable
private fun SurahMapTimelineRow(
    section: SurahSection,
    isFirst: Boolean,
    isLast: Boolean,
    isSingle: Boolean,
    isCurrentSection: Boolean,
    itemAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val textMuted = Color.semanticMutedText
    val borderCol = Color.semanticBorder
    val accentCol = Color.semanticPrimaryAccent
    val surfaceElevated = Color.semanticSurfaceElevated

    val formattedNumber = String.format("%02d", section.sectionNumber)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .alpha(itemAlpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = accentCol.copy(alpha = 0.12f)),
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                contentDescription = "Section ${section.sectionNumber}, ${section.verseRangeDisplay}, ${section.verseCount} verses. Tap to read."
            }
            .testTag("surah_map_section_${section.sectionNumber}")
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Continuous Structural Timeline Spine + Node
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spineX = size.width / 2
                val nodeCenterY = 10.dp.toPx()
                val lineStrokeWidth = 1.25.dp.toPx()
                val lineColor = borderCol.copy(alpha = 0.35f)

                // Draw connecting structural line segments
                if (!isSingle) {
                    if (!isFirst) {
                        // Top line connecting from previous section
                        drawLine(
                            color = lineColor,
                            start = Offset(spineX, 0f),
                            end = Offset(spineX, nodeCenterY - 6.5.dp.toPx()),
                            strokeWidth = lineStrokeWidth
                        )
                    }
                    if (!isLast) {
                        // Bottom line connecting to next section
                        drawLine(
                            color = lineColor,
                            start = Offset(spineX, nodeCenterY + 6.5.dp.toPx()),
                            end = Offset(spineX, size.height),
                            strokeWidth = lineStrokeWidth
                        )
                    }
                }

                // Draw Node Circle
                if (isCurrentSection) {
                    // Active reading node: distinct accent outer ring and inner filled dot
                    drawCircle(
                        color = surfaceElevated,
                        radius = 6.dp.toPx(),
                        center = Offset(spineX, nodeCenterY)
                    )
                    drawCircle(
                        color = accentCol,
                        radius = 6.dp.toPx(),
                        center = Offset(spineX, nodeCenterY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = accentCol,
                        radius = 2.5.dp.toPx(),
                        center = Offset(spineX, nodeCenterY)
                    )
                } else {
                    // Standard structural node: quiet border & subtle center
                    drawCircle(
                        color = surfaceElevated,
                        radius = 4.5.dp.toPx(),
                        center = Offset(spineX, nodeCenterY)
                    )
                    drawCircle(
                        color = borderCol.copy(alpha = 0.7f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(spineX, nodeCenterY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = textMuted.copy(alpha = 0.4f),
                        radius = 1.5.dp.toPx(),
                        center = Offset(spineX, nodeCenterY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Section Content (Typography + Hierarchy)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            // Section Title Line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formattedNumber,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    ),
                    color = if (isCurrentSection) accentCol else textMuted
                )

                Text(
                    text = if (isSingle) "Complete Surah" else "Section ${section.sectionNumber}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = if (isCurrentSection) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.5.sp
                    ),
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Secondary: Verse Range & optional subtle Reading status
            Text(
                text = if (isCurrentSection) {
                    "${section.verseRangeDisplay} • ${section.verseCount} ${if (section.verseCount == 1) "verse" else "verses"} · Reading"
                } else {
                    "${section.verseRangeDisplay} • ${section.verseCount} ${if (section.verseCount == 1) "verse" else "verses"}"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp
                ),
                color = if (isCurrentSection) textPrimary.copy(alpha = 0.85f) else textSecondary
            )
        }
    }
}
