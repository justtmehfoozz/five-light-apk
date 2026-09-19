package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ViewStream
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Surah
import com.example.data.model.SurahOverview
import com.example.data.model.SurahSection
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated

/**
 * SurahOverviewContent displays the metadata chips and the interactive Surah Map.
 * Matches FiveLight's calm, elegant, editorial aesthetic.
 *
 * @param surah Current Surah object
 * @param overview Structural metadata and verified sections
 * @param onNavigateToVerse Callback invoked when a user taps a section to read
 * @param modifier Root modifier
 */
@Composable
fun SurahOverviewContent(
    surah: Surah,
    overview: SurahOverview?,
    onNavigateToVerse: (verseNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val textPrimary = Color.semanticPrimaryText
    val textMuted = Color.semanticMutedText
    val cardBg = Color.semanticSurface
    val borderCol = Color.semanticBorder

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("surah_overview_content"),
        contentPadding = PaddingValues(bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Header Card: Surah Name, Meaning, Arabic Calligraphy, and Metadata
        item(key = "overview_header", contentType = "overview_header") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("surah_overview_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = surah.nameArabic,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${surah.number}. ${surah.nameEnglish}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = SerifHeaderFont,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary
                    )

                    Text(
                        text = surah.englishTranslation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = SpaceGrotesk
                        ),
                        color = textMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Metadata Metrics Grid (Revelation, Order, Juz, Verses)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OverviewMetricPill(
                            label = "Revelation",
                            value = overview?.revelationPlace ?: surah.revelationPlace,
                            testTag = "metric_revelation"
                        )
                        OverviewMetricDivider()
                        OverviewMetricPill(
                            label = "Order",
                            value = if (overview != null) "#${overview.revelationOrder}" else "-",
                            testTag = "metric_order"
                        )
                        OverviewMetricDivider()
                        OverviewMetricPill(
                            label = "Juz",
                            value = overview?.juzDisplay ?: "-",
                            testTag = "metric_juz"
                        )
                        OverviewMetricDivider()
                        OverviewMetricPill(
                            label = "Verses",
                            value = "${surah.versesCount}",
                            testTag = "metric_verses"
                        )
                    }
                }
            }
        }

        // 2. Section Heading: Surah Map
        item(key = "overview_map_heading", contentType = "overview_map_heading") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ViewStream,
                        contentDescription = null,
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Surah Map",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary
                    )
                }

                val secCount = overview?.sectionCount ?: overview?.sections?.size ?: 0
                Text(
                    text = if (secCount == 1) "1 Section" else "$secCount Sections",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = SpaceGrotesk
                    ),
                    color = textMuted
                )
            }
        }

        // 3. Interactive Section Cards
        val sections = overview?.sections ?: emptyList()
        if (sections.isNotEmpty()) {
            items(
                items = sections,
                key = { "section_${it.sectionNumber}_${it.startVerse}" },
                contentType = { "surah_section_item" }
            ) { section ->
                SurahMapSectionCard(
                    section = section,
                    onClick = { onNavigateToVerse(section.startVerse) }
                )
            }
        } else {
            item(key = "overview_single_section") {
                SurahMapSectionCard(
                    section = SurahSection(
                        sectionNumber = 1,
                        startVerse = 1,
                        endVerse = surah.versesCount,
                        verseCount = surah.versesCount,
                        openingExcerpt = "Complete Surah"
                    ),
                    onClick = { onNavigateToVerse(1) }
                )
            }
        }
    }
}

@Composable
private fun OverviewMetricPill(
    label: String,
    value: String,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = Color.semanticPrimaryText
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = SpaceGrotesk,
                fontSize = 11.sp
            ),
            color = Color.semanticMutedText
        )
    }
}

@Composable
private fun OverviewMetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color.semanticBorder.copy(alpha = 0.6f))
    )
}

/**
 * Individual interactive card representing a structural section of the Surah.
 * Tapping smoothly navigates the reader to the beginning verse of this section.
 */
@Composable
fun SurahMapSectionCard(
    section: SurahSection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = Color.semanticSurface
    val borderCol = Color.semanticBorder
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val textMuted = Color.semanticMutedText
    val badgeBg = Color.semanticSurfaceElevated
    val badgeFg = Color.semanticPrimaryAccent

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                contentDescription = "Section ${section.sectionNumber}, ${section.verseRangeDisplay}. Tap to read."
            }
            .testTag("surah_map_section_${section.sectionNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section Index Circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeBg)
                    .border(1.dp, borderCol, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${section.sectionNumber}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = badgeFg
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Section details: range + opening excerpt
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = section.verseRangeDisplay,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = textPrimary
                    )

                    Text(
                        text = " • ${section.verseCount} ${if (section.verseCount == 1) "verse" else "verses"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGrotesk
                        ),
                        color = textMuted
                    )
                }

                if (section.openingExcerpt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = section.openingExcerpt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Calm navigation cue icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = textMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
