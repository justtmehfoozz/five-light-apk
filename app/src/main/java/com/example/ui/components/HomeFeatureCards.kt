package com.example.ui.components

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.isAppInDarkTheme
import androidx.compose.ui.graphics.Color


import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CalendarEventMoment
import com.example.data.model.FiveLightMoment
import com.example.data.model.NextOpportunityItem
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerPrepItem
import com.example.data.model.QuranLastRead
import com.example.data.model.TonightSummary
import com.example.data.model.WeeklyWorshipOverview
import com.example.data.util.DailyContentProvider
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.ArabicText
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

@Composable
fun ContinueReadingCard(
    lastRead: QuranLastRead,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val progress = (lastRead.surahNumber.toFloat() / 114f).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onContinueClick() }
            .testTag("continue_reading_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoStories,
                            contentDescription = "Continue Reading",
                            tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Continue Reading",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${lastRead.surahNameEnglish} • Verse ${lastRead.verseNumber}",
                            fontFamily = SerifHeaderFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onContinueClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continue",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // PART 5: CONTINUE READING PROGRESS SLIVER (thin bar along bottom edge)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(2.5.dp)
                        .background(if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.65f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                )
            }
        }
    }
}

@Composable
fun PrayerPrepCard(
    prep: PrayerPrepItem,
    onQiblaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_prep_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prayer Preparation",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${prep.prayerName.displayName} in ${prep.minutesRemaining} min",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Prepare yourself for ${prep.prayerName.displayName} (${prep.formattedTime})",
                fontFamily = SerifHeaderFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prep.steps.forEach { step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "• ",
                            fontFamily = SpaceGrotesk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = step,
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onQiblaClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CompassCalibration,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Qibla Direction →",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TonightCard(
    tonight: TonightSummary,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    var currentMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000L)
            currentMillis = System.currentTimeMillis()
        }
    }

    val ishaMillis = tonight.ishaTimeMillis
    val fajrMillis = tonight.fajrTimeMillis

    val nightProgress = remember(currentMillis, ishaMillis, fajrMillis) {
        if (ishaMillis > 0L && fajrMillis > ishaMillis) {
            var adjustedNow = currentMillis
            if (adjustedNow < ishaMillis - 12 * 3600 * 1000L) {
                adjustedNow += 24 * 3600 * 1000L
            }
            ((adjustedNow - ishaMillis).toFloat() / (fajrMillis - ishaMillis).toFloat()).coerceIn(0f, 1f)
        } else {
            0.5f
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tonight_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.NightsStay,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tonight.headerTitle,
                        fontFamily = SerifHeaderFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (tonight.subtitleText.isNotBlank()) {
                    Text(
                        text = tonight.subtitleText,
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Isha
                val isIshaActive = tonight.isIshaActive
                Column {
                    Text(
                        text = "Isha",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = if (isIshaActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isIshaActive) {
                            if (isDark) Color(0xFFB5B5AE) else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = tonight.ishaTimeFormatted,
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIshaActive) {
                            if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                // Tahajjud Window
                val isTahajjudActive = tonight.isTahajjudActive
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tahajjud Window",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = if (isTahajjudActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTahajjudActive) {
                            if (isDark) Color(0xFFB5B5AE) else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = tonight.tahajjudWindowFormatted,
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTahajjudActive) {
                            if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                // Fajr
                val isFajrActive = tonight.isFajrActive
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fajr",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = if (isFajrActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isFajrActive) {
                            if (isDark) Color(0xFFB5B5AE) else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = tonight.fajrTimeFormatted,
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFajrActive) {
                            if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            // PART 2: LIVE POSITION MARKER ON OVERNIGHT TRACK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )

                // Filled portion
                Box(
                    modifier = Modifier
                        .fillMaxWidth(nightProgress)
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )

                // Marker Dot
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val markerOffset = maxWidth * nightProgress - 4.dp
                    Box(
                        modifier = Modifier
                            .offset(x = markerOffset.coerceAtLeast(0.dp))
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Last third of the night begins at ${tonight.lastThirdStartFormatted}",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun NextOpportunityCard(
    item: NextOpportunityItem,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("next_opportunity_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Next Worship Opportunity",
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.title,
                    fontFamily = SerifHeaderFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.timeFormatted,
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onActionClick() }
            ) {
                Text(
                    text = item.actionText,
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FiveLightMomentCard(
    moment: FiveLightMoment,
    onActionClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    // PART 4: OCCASION-AWARE FIVELIGHT MOMENT ICON
    val occasionIcon = remember(moment.tag, moment.title) {
        val tagLower = moment.tag.lowercase()
        val titleLower = moment.title.lowercase()
        when {
            tagLower.contains("ashura") || titleLower.contains("ashura") -> Icons.Outlined.WaterDrop
            tagLower.contains("ramadan") || titleLower.contains("ramadan") -> Icons.Outlined.NightsStay
            tagLower.contains("friday") || titleLower.contains("jumu") || titleLower.contains("kahf") -> Icons.Outlined.AutoStories
            tagLower.contains("dhikr") || tagLower.contains("adhkar") -> Icons.Outlined.AutoAwesome
            else -> Icons.Outlined.AutoAwesome
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fivelight_moment_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = occasionIcon,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = moment.tag,
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "FiveLight Moment",
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = moment.title,
                fontFamily = SerifHeaderFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = moment.message,
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (!moment.actionText.isNullOrEmpty() && onActionClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF3A3845) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onActionClick() }
                    ) {
                        Text(
                            text = moment.actionText ?: "",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventMomentCard(
    eventMoment: CalendarEventMoment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_event_moment_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = eventMoment.eventTitle,
                    fontFamily = SerifHeaderFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (eventMoment.arabicTitle.isNotEmpty()) {
                    ArabicText(
                        text = eventMoment.arabicTitle,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = eventMoment.description,
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ReflectionOfTheDayCard(
    onReflectClick: (surahNumber: Int, verseNumber: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // PART 3: REFLECTION OF THE DAY SWIPE TO REVEAL
    val reflections = remember { DailyContentProvider.getReflections() }
    var currentIndex by remember { mutableStateOf(0) }
    var slideDirection by remember { mutableStateOf(1) } // 1 for left/next, -1 for right/prev
    var totalDragX by remember { mutableStateOf(0f) }

    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val cardBg = if (isDarkTheme) Color(0xFF1B1A19) else Color.semanticSurface
    val cardBorder = if (isDarkTheme) BorderStroke(1.dp, Color(0xFF3A3836)) else BorderStroke(1.dp, Color(0xFFD5D1C9))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(reflections.size) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX < -35f) {
                            // Swipe left -> Next
                            slideDirection = 1
                            currentIndex = (currentIndex + 1) % reflections.size
                        } else if (totalDragX > 35f) {
                            // Swipe right -> Previous
                            slideDirection = -1
                            currentIndex = if (currentIndex - 1 < 0) reflections.size - 1 else currentIndex - 1
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
            .testTag("reflection_of_day_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = cardBorder
    ) {
        val titleColor = if (isDarkTheme) Color(0xFFD6D2CC) else Color(0xFF66635E)
        val iconTint = if (isDarkTheme) Color(0xFFD6D2CC) else Color(0xFF8D6B1E)
        val refColor = if (isDarkTheme) Color(0xFFB8B3AD) else Color(0xFF66635E)
        val arabicColor = if (isDarkTheme) Color(0xFFD8D3CC) else Color(0xFF8D6B1E)
        val translationColor = if (isDarkTheme) Color(0xFFC8C3BC) else Color(0xFF1E1D1A)
        val buttonBg = if (isDarkTheme) Color(0xFF494556) else Color(0xFF8D6B1E)
        val buttonFg = Color(0xFFFFFFFF)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val reflection = reflections[currentIndex]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reflection of the Day",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                }

                Text(
                    text = reflection.reference,
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    color = refColor
                )
            }

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    if (slideDirection > 0) {
                        (slideInHorizontally(tween(350)) { width -> (width * 0.2f).toInt() } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(tween(350)) { width -> -(width * 0.2f).toInt() } + fadeOut(tween(350)))
                    } else {
                        (slideInHorizontally(tween(350)) { width -> -(width * 0.2f).toInt() } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(tween(350)) { width -> (width * 0.2f).toInt() } + fadeOut(tween(350)))
                    }
                },
                label = "reflection_content_transition"
            ) { targetIdx ->
                val activeRef = reflections[targetIdx]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArabicText(
                        text = activeRef.arabic,
                        fontSize = 22.sp,
                        color = arabicColor,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = activeRef.translation,
                        fontFamily = SerifHeaderFont,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.5.sp,
                        color = translationColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pagination Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    reflections.indices.forEach { dotIdx ->
                        val isSelected = dotIdx == currentIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 6.dp else 4.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) (if (isDarkTheme) Color(0xFF494556) else Color(0xFF8D6B1E))
                                    else (if (isDarkTheme) Color(0xFF3A3836) else Color(0xFFD5D1C9))
                                )
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = buttonBg,
                    modifier = Modifier.clickable {
                        onReflectClick(reflection.surahNumber, reflection.verseNumber)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reflect",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = buttonFg
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = buttonFg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyWorshipOverviewCard(
    overview: WeeklyWorshipOverview,
    onPrayerCellClick: (prayerName: com.example.data.model.PrayerName, dateString: String, currentStatus: com.example.data.model.PrayerStatus) -> Unit = { _, _, _ -> },
    onOpenPersonalLog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val cardBg = if (isDarkTheme) Color(0xFF1B1A19) else Color.semanticSurface
    val cardBorder = if (isDarkTheme) BorderStroke(1.dp, Color(0xFF3A3836)) else BorderStroke(1.dp, Color(0xFFD5D1C9))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_worship_overview_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Week",
                    fontFamily = SerifHeaderFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1E1D1A)
                )

                Surface(
                    onClick = onOpenPersonalLog,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkTheme) Color(0xFF494556).copy(alpha = 0.35f) else Color(0xFF8D6B1E).copy(alpha = 0.12f),
                    modifier = Modifier.testTag("open_personal_log_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Personal Log",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color(0xFFE6DEF6) else Color(0xFF8D6B1E)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color(0xFFE6DEF6) else Color(0xFF8D6B1E),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = if (isDarkTheme) Color(0xFF2D2B29) else Color(0xFFE3DFD6))

            // Day Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prayer",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF66635E),
                    modifier = Modifier.width(60.dp)
                )

                overview.days.forEach { day ->
                    Text(
                        text = day.dayOfWeekName,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (day.isToday) (if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF8D6B1E)) else (if (isDarkTheme) Color(0xFFA8A8A2) else Color(0xFF66635E)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val prayerRows = listOf(
                Pair(com.example.data.model.PrayerName.FAJR, "Fajr") to { d: com.example.data.model.DayWorshipState -> d.fajrStatus },
                Pair(com.example.data.model.PrayerName.DHUHR, "Dhuhr") to { d: com.example.data.model.DayWorshipState -> d.dhuhrStatus },
                Pair(com.example.data.model.PrayerName.ASR, "Asr") to { d: com.example.data.model.DayWorshipState -> d.asrStatus },
                Pair(com.example.data.model.PrayerName.MAGHRIB, "Maghrib") to { d: com.example.data.model.DayWorshipState -> d.maghribStatus },
                Pair(com.example.data.model.PrayerName.ISHA, "Isha") to { d: com.example.data.model.DayWorshipState -> d.ishaStatus }
            )

            prayerRows.forEach { (prayerInfo, getStatus) ->
                val (pEnum, pDisplayName) = prayerInfo
                WeeklyPrayerRow(
                    prayerName = pEnum,
                    displayName = pDisplayName,
                    days = overview.days,
                    getStatus = getStatus,
                    onPrayerCellClick = onPrayerCellClick
                )
            }
        }
    }
}

@Composable
private fun WeeklyPrayerRow(
    prayerName: com.example.data.model.PrayerName,
    displayName: String,
    days: List<com.example.data.model.DayWorshipState>,
    getStatus: (com.example.data.model.DayWorshipState) -> com.example.data.model.PrayerStatus,
    onPrayerCellClick: (prayerName: com.example.data.model.PrayerName, dateString: String, currentStatus: com.example.data.model.PrayerStatus) -> Unit
) {
    val statuses = remember(days) { days.map { getStatus(it) } }
    val isAllSevenPrayed = statuses.size == 7 && statuses.all { it == com.example.data.model.PrayerStatus.PRAYED }

    // PART 1: 1.1 - 1.4 CONNECTING LINE BETWEEN CONSECUTIVE PRAYED DAYS
    val connectedSegments = remember(statuses) {
        List(6) { i ->
            statuses.getOrNull(i) == com.example.data.model.PrayerStatus.PRAYED &&
            statuses.getOrNull(i + 1) == com.example.data.model.PrayerStatus.PRAYED
        }
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val streakLineColor = if (isDarkTheme) Color(0xFF494556).copy(alpha = 0.6f) else Color(0xFF8D6B1E).copy(alpha = 0.42f)

    // PART 1: 1.6 SUBTLE ROW GLOW IF ALL 7 DAYS ARE PRAYED
    val rowModifier = if (isAllSevenPrayed) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        if (isDarkTheme) Color(0xFF494556).copy(alpha = 0.12f) else Color(0xFF8D6B1E).copy(alpha = 0.09f),
                        if (isDarkTheme) Color(0xFF494556).copy(alpha = 0.08f) else Color(0xFF8D6B1E).copy(alpha = 0.08f),
                        if (isDarkTheme) Color(0xFF494556).copy(alpha = 0.04f) else Color(0xFF8D6B1E).copy(alpha = 0.04f)
                    )
                )
            )
            .padding(vertical = 2.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    }

    Row(
        modifier = rowModifier
            .drawBehind {
                val labelWidth = 60.dp.toPx()
                val daysCount = days.size.coerceAtLeast(1)
                val cellWidth = (size.width - labelWidth) / daysCount.toFloat()
                val centerY = size.height / 2f

                // Draw connecting streak lines between centers of consecutive PRAYED circles
                for (i in 0 until (daysCount - 1)) {
                    if (connectedSegments.getOrElse(i) { false }) {
                        val startX = labelWidth + (i + 0.5f) * cellWidth
                        val endX = labelWidth + (i + 1.5f) * cellWidth
                        drawLine(
                            color = streakLineColor,
                            start = Offset(startX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = 1.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayName,
            fontFamily = SpaceGrotesk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1E1D1A),
            modifier = Modifier.width(60.dp)
        )

        days.forEachIndexed { idx, day ->
            val status = statuses.getOrElse(idx) { getStatus(day) }
            val isFuture = status == com.example.data.model.PrayerStatus.FUTURE
            val (symbol, color) = when (status) {
                com.example.data.model.PrayerStatus.PRAYED -> "✓" to (if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF8D6B1E))
                com.example.data.model.PrayerStatus.MISSED -> "!" to (if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFC62828))
                com.example.data.model.PrayerStatus.NEEDS_INPUT -> "○" to (if (isDarkTheme) Color(0xFFA8A8A2) else Color(0xFF66635E))
                com.example.data.model.PrayerStatus.FUTURE -> "·" to (if (isDarkTheme) Color(0xFFA8A8A2).copy(alpha = 0.5f) else Color(0xFF66635E).copy(alpha = 0.35f))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (!isFuture) {
                            Modifier.clickable {
                                onPrayerCellClick(prayerName, day.dateString, status)
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 4.dp)
                    .testTag("week_cell_${day.dateString}_${displayName.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                // Circle backdrop for PRAYED state so the circle stays visually dominant over the connecting line
                if (status == com.example.data.model.PrayerStatus.PRAYED) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF494556) else Color(0xFF8D6B1E).copy(alpha = 0.15f))
                    )
                }
                Text(
                    text = symbol,
                    fontFamily = SpaceGrotesk,
                    fontSize = if (status == com.example.data.model.PrayerStatus.FUTURE) 16.sp else 13.sp,
                    fontWeight = if (status == com.example.data.model.PrayerStatus.PRAYED || status == com.example.data.model.PrayerStatus.MISSED) FontWeight.Bold else FontWeight.Normal,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

enum class PersonalLogTab { LOG, INSIGHTS, QADA }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PersonalLogSheet(
    overview: com.example.data.model.WeeklyWorshipOverview?,
    todayLog: com.example.data.db.PrayerLogEntity?,
    allPrayerLogs: List<com.example.data.db.PrayerLogEntity>,
    qadaCounts: Map<com.example.data.model.PrayerName, Int>,
    onUpdateQadaCount: (com.example.data.model.PrayerName, Int) -> Unit,
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,
    onSavePrayerNote: (com.example.data.model.PrayerName, String, String?) -> Unit = { _, _, _ -> },
    onAddPrayerToQada: (com.example.data.model.PrayerName, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(PersonalLogTab.LOG) }
    
    val sheetBg = Color.semanticSurfaceElevated

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle(
                color = Color.semanticBorder
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("personal_log_sheet")
    ) {
        val sheetScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(sheetScrollState)
                .padding(horizontal = 24.dp)
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Personal Log",
                        fontFamily = com.example.ui.theme.SerifHeaderFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticPrimaryText
                    )
                    Text(
                        text = "Unified Fard Prayer Records",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 13.sp,
                        color = Color.semanticSecondaryText
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.semanticSecondaryText
                    )
                }
            }

            // Tabs: strictly Log | Qada | Insights
            val tabs = listOf(
                com.example.ui.screens.PillItem(PersonalLogTab.LOG, "Log"),
                com.example.ui.screens.PillItem(PersonalLogTab.QADA, "Qada"),
                com.example.ui.screens.PillItem(PersonalLogTab.INSIGHTS, "Insights")
            )
            com.example.ui.screens.SpringPillSelector(
                items = tabs,
                selectedItem = currentTab,
                onItemSelected = { currentTab = it }
            )

            HorizontalDivider(color = Color.semanticBorder)

            // Dynamic Content Viewport
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.animation.Crossfade(
                    targetState = currentTab,
                    modifier = Modifier.fillMaxWidth()
                ) { tab ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (tab) {
                            PersonalLogTab.LOG -> {
                                PersonalLogTabContent(
                                    overview = overview,
                                    allPrayerLogs = allPrayerLogs,
                                    onSetPrayerStatus = onSetPrayerStatus,
                                    onSavePrayerNote = onSavePrayerNote,
                                    onAddPrayerToQada = onAddPrayerToQada
                                )
                            }
                            PersonalLogTab.QADA -> {
                                PersonalLogQadaContent(
                                    qadaCounts = qadaCounts,
                                    onUpdateQadaCount = onUpdateQadaCount
                                )
                            }
                            PersonalLogTab.INSIGHTS -> {
                                PersonalLogInsightsContent(allPrayerLogs = allPrayerLogs)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class LogViewMode { WEEK, MONTH }

@Composable
fun PersonalLogTabContent(
    overview: com.example.data.model.WeeklyWorshipOverview?,
    allPrayerLogs: List<com.example.data.db.PrayerLogEntity>,
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,
    onSavePrayerNote: (com.example.data.model.PrayerName, String, String?) -> Unit = { _, _, _ -> },
    onAddPrayerToQada: (com.example.data.model.PrayerName, String) -> Unit = { _, _ -> }
) {
    var logViewMode by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(LogViewMode.WEEK)
    }

    androidx.compose.animation.AnimatedContent(
        targetState = logViewMode,
        transitionSpec = {
            (fadeIn(animationSpec = tween(180)) + slideInHorizontally(animationSpec = tween(200)) { if (targetState == LogViewMode.MONTH) 30 else -30 })
                .togetherWith(fadeOut(animationSpec = tween(140)) + slideOutHorizontally(animationSpec = tween(180)) { if (targetState == LogViewMode.MONTH) -30 else 30 })
        },
        label = "LogViewModeTransition"
    ) { mode ->
        when (mode) {
            LogViewMode.WEEK -> {
                PersonalLogWeekView(
                    overview = overview,
                    allPrayerLogs = allPrayerLogs,
                    onOpenMonthView = { logViewMode = LogViewMode.MONTH },
                    onSetPrayerStatus = onSetPrayerStatus,
                    onSavePrayerNote = onSavePrayerNote,
                    onAddPrayerToQada = onAddPrayerToQada
                )
            }
            LogViewMode.MONTH -> {
                PersonalLogMonthContent(
                    allPrayerLogs = allPrayerLogs,
                    onBackToWeekView = { logViewMode = LogViewMode.WEEK },
                    onSetPrayerStatus = onSetPrayerStatus,
                    onSavePrayerNote = onSavePrayerNote,
                    onAddPrayerToQada = onAddPrayerToQada
                )
            }
        }
    }
}

@Composable
fun PersonalLogWeekView(
    overview: com.example.data.model.WeeklyWorshipOverview?,
    allPrayerLogs: List<com.example.data.db.PrayerLogEntity>,
    onOpenMonthView: () -> Unit = {},
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,
    onSavePrayerNote: (com.example.data.model.PrayerName, String, String?) -> Unit = { _, _, _ -> },
    onAddPrayerToQada: (com.example.data.model.PrayerName, String) -> Unit = { _, _ -> }
) {
    val days = overview?.days ?: emptyList()
    var selectedDateString by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }

    val todayDateString = androidx.compose.runtime.remember(days) { days.find { it.isToday }?.dateString }
    val effectiveDateString = selectedDateString ?: todayDateString ?: days.firstOrNull()?.dateString

    val selectedDay = days.find { it.dateString == effectiveDateString } ?: days.find { it.isToday } ?: days.firstOrNull()
    val activeDayLog = androidx.compose.runtime.remember(effectiveDateString, allPrayerLogs) {
        allPrayerLogs.find { it.date == effectiveDateString }
    }

    // Dynamically formatted weekly date range (e.g., "17 – 23 Aug 2026", "31 Aug – 6 Sep 2026")
    val weekDateRange = androidx.compose.runtime.remember(days) {
        if (days.isEmpty()) ""
        else {
            try {
                val first = java.time.LocalDate.parse(days.first().dateString)
                val last = java.time.LocalDate.parse(days.last().dateString)
                val monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.getDefault())
                val firstMonth = first.format(monthFormatter)
                val lastMonth = last.format(monthFormatter)

                if (first.year == last.year) {
                    if (first.monthValue == last.monthValue) {
                        "${first.dayOfMonth} – ${last.dayOfMonth} $lastMonth ${last.year}"
                    } else {
                        "${first.dayOfMonth} $firstMonth – ${last.dayOfMonth} $lastMonth ${last.year}"
                    }
                } else {
                    "${first.dayOfMonth} $firstMonth ${first.year} – ${last.dayOfMonth} $lastMonth ${last.year}"
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Weekly Records Header with Date Range and Month Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = "Weekly Records",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.semanticPrimaryText
                )
                if (weekDateRange.isNotBlank()) {
                    Text(
                        text = weekDateRange,
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.semanticSecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Single compact Month button
            Surface(
                onClick = onOpenMonthView,
                shape = RoundedCornerShape(8.dp),
                color = Color.semanticControl,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier.testTag("btn_month_view")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Month",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAppInDarkTheme()) Color(0xFFFFFFFF) else Color.semanticPrimaryAccent
                    )
                }
            }
        }

        if (days.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    val isSelected = day.dateString == effectiveDateString
                    val dayPillColor = if (isSelected) Color.semanticPrimaryAccent else Color.semanticControl
                    val contentColor = if (isSelected) Color.semanticAccentForeground else Color.semanticPrimaryText
                    val pillBorder = if (isSelected) BorderStroke(1.5.dp, Color.semanticPrimaryAccent) else BorderStroke(1.dp, Color.semanticBorder)

                    val dayNum = try {
                        day.dateString.substringAfterLast("-")
                    } catch (e: Exception) { "" }

                    Surface(
                        onClick = { selectedDateString = day.dateString },
                        shape = RoundedCornerShape(12.dp),
                        color = dayPillColor,
                        border = pillBorder,
                        modifier = Modifier.width(42.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = day.dayOfWeekName.take(3),
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) contentColor.copy(alpha = 0.8f) else Color.semanticSecondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayNum,
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) contentColor else Color.semanticPrimaryAccent)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedDay != null) {
            val fullFormattedDate = androidx.compose.runtime.remember(selectedDay.dateString) {
                try {
                    val date = java.time.LocalDate.parse(selectedDay.dateString)
                    date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.ENGLISH))
                } catch (e: Exception) {
                    selectedDay.dateString
                }
            }
            val isFridayDay = androidx.compose.runtime.remember(selectedDay.dateString) {
                try {
                    java.time.LocalDate.parse(selectedDay.dateString).dayOfWeek == java.time.DayOfWeek.FRIDAY
                } catch (e: Exception) {
                    false
                }
            }

            Text(
                text = fullFormattedDate,
                fontFamily = com.example.ui.theme.SerifHeaderFont,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.semanticPrimaryAccent
            )

            val dhuhrTitle = com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(com.example.data.model.PrayerName.DHUHR, isFridayDay)
            val prayerItems = listOf(
                Triple(com.example.data.model.PrayerName.FAJR, "Fajr", selectedDay.fajrStatus),
                Triple(com.example.data.model.PrayerName.DHUHR, dhuhrTitle, selectedDay.dhuhrStatus),
                Triple(com.example.data.model.PrayerName.ASR, "Asr", selectedDay.asrStatus),
                Triple(com.example.data.model.PrayerName.MAGHRIB, "Maghrib", selectedDay.maghribStatus),
                Triple(com.example.data.model.PrayerName.ISHA, "Isha", selectedDay.ishaStatus)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                prayerItems.forEach { (pEnum, pDisplayName, status) ->
                    val note = activeDayLog?.getNote(pEnum)
                    val isQadaAdded = activeDayLog?.isQadaAdded(pEnum) ?: false

                    PersonalLogPrayerRow(
                        prayerName = pEnum,
                        displayName = pDisplayName,
                        status = status,
                        note = note,
                        isQadaAdded = isQadaAdded,
                        onSetStatus = { newStatus ->
                            onSetPrayerStatus(pEnum, selectedDay.dateString, newStatus)
                        },
                        onSaveNote = { newNote ->
                            onSavePrayerNote(pEnum, selectedDay.dateString, newNote)
                        },
                        onAddQada = {
                            onAddPrayerToQada(pEnum, selectedDay.dateString)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalLogMonthContent(
    allPrayerLogs: List<com.example.data.db.PrayerLogEntity>,
    onBackToWeekView: () -> Unit = {},
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,
    onSavePrayerNote: (com.example.data.model.PrayerName, String, String?) -> Unit,
    onAddPrayerToQada: (com.example.data.model.PrayerName, String) -> Unit
) {
    val today = remember { java.time.LocalDate.now() }
    var currentYearMonth by remember { mutableStateOf(java.time.YearMonth.from(today)) }
    var selectedDateForDetail by remember { mutableStateOf<java.time.LocalDate?>(null) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7 // Monday = 0

    val isCurrentMonth = currentYearMonth.year == today.year && currentYearMonth.monthValue == today.monthValue

    // Calculate month stats
    val (completedDaysCount, loggedDaysCount) = remember(currentYearMonth, allPrayerLogs) {
        var comp = 0
        var logged = 0
        for (day in 1..daysInMonth) {
            val dateStr = currentYearMonth.atDay(day).toString()
            val log = allPrayerLogs.find { it.date == dateStr }
            if (log != null) {
                val completedCount = log.getCompletedCount()
                if (completedCount > 0 || log.getMissedCount() > 0) logged++
                if (completedCount == 5) comp++
            }
        }
        Pair(comp, logged)
    }

    // Hijri info for the middle of the month
    val hijriMonthName = remember(currentYearMonth) {
        try {
            val midDate = currentYearMonth.atDay(15)
            val hijri = com.example.data.repository.IslamicDateRepository.getInstance().getHijriDateForLocalDate(midDate)
            "${hijri.monthName} ${hijri.year} AH"
        } catch (e: Exception) {
            ""
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Monthly Records Header with Back to Week Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Records",
                fontFamily = com.example.ui.theme.SpaceGrotesk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.semanticPrimaryText
            )

            Surface(
                onClick = onBackToWeekView,
                shape = RoundedCornerShape(8.dp),
                color = Color.semanticControl,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier.testTag("btn_back_to_week")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Today,
                        contentDescription = null,
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Week",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAppInDarkTheme()) Color(0xFFFFFFFF) else Color.semanticPrimaryAccent
                    )
                }
            }
        }

        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_prev_month")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = Color.semanticSecondaryText
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                val monthTitle = currentYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()) + " " + currentYearMonth.year
                Text(
                    text = monthTitle,
                    fontFamily = com.example.ui.theme.SerifHeaderFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.semanticPrimaryText
                )
                if (hijriMonthName.isNotBlank()) {
                    Text(
                        text = hijriMonthName,
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 12.sp,
                        color = Color.semanticSecondaryText
                    )
                }
                if (!isCurrentMonth) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        onClick = { currentYearMonth = java.time.YearMonth.from(today) },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.semanticControl,
                        border = BorderStroke(1.dp, Color.semanticBorder),
                        modifier = Modifier.testTag("btn_month_today")
                    ) {
                        Text(
                            text = "Today",
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_next_month")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    tint = Color.semanticSecondaryText
                )
            }
        }

        // Summary Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.semanticControl,
            border = BorderStroke(1.dp, Color.semanticBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "5/5 Prayed Days",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 12.sp,
                    color = Color.semanticSecondaryText
                )
                Text(
                    text = "$completedDaysCount of $daysInMonth days",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.semanticPrimaryAccent
                )
            }
        }

        // Day of Week Header
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { dayName ->
                Text(
                    text = dayName,
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.semanticSecondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calendar Grid
        val totalCells = dayOfWeekOffset + daysInMonth
        val numRows = (totalCells + 6) / 7

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0 until numRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - dayOfWeekOffset + 1

                        if (dayNum in 1..daysInMonth) {
                            val cellDate = currentYearMonth.atDay(dayNum)
                            val cellDateString = cellDate.toString()
                            val cellLog = allPrayerLogs.find { it.date == cellDateString }
                            val isCellToday = cellDate == today
                            val isCellFuture = cellDate.isAfter(today)

                            val completedCount = cellLog?.getCompletedCount() ?: 0
                            val missedCount = cellLog?.getMissedCount() ?: 0
                            val hasNotes = cellLog?.hasAnyNotes() == true

                            // Heatmap styling according to 5 daily prayers completion
                            val cellBg = when {
                                isCellFuture -> Color.semanticControl.copy(alpha = 0.25f)
                                completedCount == 5 -> Color.semanticPrimaryAccent
                                completedCount == 4 -> Color.semanticPrimaryAccent.copy(alpha = 0.75f)
                                completedCount == 3 -> Color.semanticPrimaryAccent.copy(alpha = 0.55f)
                                completedCount == 2 -> Color.semanticPrimaryAccent.copy(alpha = 0.35f)
                                completedCount == 1 -> Color.semanticPrimaryAccent.copy(alpha = 0.18f)
                                else -> Color.semanticControl.copy(alpha = 0.45f)
                            }

                            val textColor = when {
                                completedCount >= 4 -> Color.semanticAccentForeground
                                isCellToday -> Color.semanticPrimaryAccent
                                isCellFuture -> Color.semanticSecondaryText.copy(alpha = 0.35f)
                                completedCount in 1..3 -> Color.semanticPrimaryText
                                else -> Color.semanticSecondaryText
                            }

                            val borderStroke = when {
                                isCellToday -> BorderStroke(1.5.dp, Color.semanticPrimaryAccent)
                                else -> BorderStroke(0.5.dp, Color.semanticBorder.copy(alpha = if (isCellFuture) 0.2f else 0.45f))
                            }

                            Surface(
                                onClick = { selectedDateForDetail = cellDate },
                                shape = RoundedCornerShape(10.dp),
                                color = cellBg,
                                border = borderStroke,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("month_cell_$cellDateString")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                                            fontSize = 13.sp,
                                            fontWeight = if (completedCount == 5 || isCellToday) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )

                                        // Status indicators
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            if (missedCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.5.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.semanticError)
                                                )
                                            }
                                            if (hasNotes) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (completedCount >= 4) Color.semanticAccentForeground else Color.semanticSecondaryText)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Empty placeholder cell
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Heatmap Legend (Part 1 — Month View Legend)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Intensity progression: Less ▪ ▪ ▪ ▪ ▪ More
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Less",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 10.5.sp,
                    color = Color.semanticSecondaryText
                )
                // 0 / 5
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.semanticControl.copy(alpha = 0.45f))
                        .border(0.5.dp, Color.semanticBorder.copy(alpha = 0.45f), RoundedCornerShape(2.5.dp))
                )
                // 1 / 5
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.semanticPrimaryAccent.copy(alpha = 0.18f))
                )
                // 2 / 5
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.semanticPrimaryAccent.copy(alpha = 0.35f))
                )
                // 3 / 5
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.semanticPrimaryAccent.copy(alpha = 0.55f))
                )
                // 4 / 5
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.semanticPrimaryAccent.copy(alpha = 0.75f))
                )
                // 5 / 5
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.semanticPrimaryAccent)
                )
                Text(
                    text = "More",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 10.5.sp,
                    color = Color.semanticSecondaryText
                )
            }

            // Status indicators: Missed, Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.semanticError)
                    )
                    Text(
                        text = "Missed",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 10.5.sp,
                        color = Color.semanticSecondaryText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.semanticSecondaryText)
                    )
                    Text(
                        text = "Note",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 10.5.sp,
                        color = Color.semanticSecondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Day Detail Sub-Sheet
    if (selectedDateForDetail != null) {
        val detailDate = selectedDateForDetail!!
        val dateString = detailDate.toString()
        val detailLog = allPrayerLogs.find { it.date == dateString }

        PersonalLogDayDetailSheet(
            date = detailDate,
            log = detailLog,
            onSetPrayerStatus = { pName, pStatus ->
                onSetPrayerStatus(pName, dateString, pStatus)
            },
            onSavePrayerNote = { pName, note ->
                onSavePrayerNote(pName, dateString, note)
            },
            onAddPrayerToQada = { pName ->
                onAddPrayerToQada(pName, dateString)
            },
            onDismiss = { selectedDateForDetail = null }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PersonalLogDayDetailSheet(
    date: java.time.LocalDate,
    log: com.example.data.db.PrayerLogEntity?,
    onSetPrayerStatus: (com.example.data.model.PrayerName, com.example.data.model.PrayerStatus) -> Unit,
    onSavePrayerNote: (com.example.data.model.PrayerName, String?) -> Unit,
    onAddPrayerToQada: (com.example.data.model.PrayerName) -> Unit,
    onDismiss: () -> Unit
) {
    val dateString = date.toString()
    val today = remember { java.time.LocalDate.now() }
    val isToday = date == today
    val isFriday = date.dayOfWeek == java.time.DayOfWeek.FRIDAY
    val isFuture = date.isAfter(today)

    val sheetBg = Color.semanticSurfaceElevated
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Hijri date calculation
    val hijriString = remember(date) {
        try {
            val h = com.example.data.repository.IslamicDateRepository.getInstance().getHijriDateForLocalDate(date)
            com.example.data.repository.IslamicDateRepository.getInstance().formatHijriString(h)
        } catch (e: Exception) {
            ""
        }
    }

    val completedCount = log?.getCompletedCount() ?: 0
    val missedCount = log?.getMissedCount() ?: 0

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle(
                color = Color.semanticBorder
            )
        },
        modifier = Modifier.fillMaxWidth().testTag("day_detail_sheet_$dateString")
    ) {
        val detailScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(detailScrollState)
                .padding(horizontal = 24.dp)
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                    Text(
                        text = if (isToday) "Today ($formattedDate)" else formattedDate,
                        fontFamily = com.example.ui.theme.SerifHeaderFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticPrimaryText
                    )
                    if (hijriString.isNotBlank()) {
                        Text(
                            text = hijriString,
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 12.sp,
                            color = Color.semanticSecondaryText
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
                        tint = Color.semanticSecondaryText
                    )
                }
            }

            // Summary Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (completedCount == 5) Color.semanticPrimaryAccent else Color.semanticControl,
                border = BorderStroke(1.dp, if (completedCount == 5) Color.semanticPrimaryAccent else Color.semanticBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val summaryText = when {
                        isFuture -> "Upcoming Day"
                        completedCount == 5 -> "5/5 Prayed • Complete"
                        completedCount > 0 && missedCount > 0 -> "$completedCount Prayed • $missedCount Missed"
                        completedCount > 0 -> "$completedCount of 5 Prayed"
                        missedCount > 0 -> "$missedCount Missed"
                        else -> "No prayers recorded"
                    }

                    Text(
                        text = summaryText,
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (completedCount == 5) Color.semanticAccentForeground else Color.semanticPrimaryText
                    )

                    if (completedCount == 5) {
                        Text(
                            text = "✓",
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticAccentForeground
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.semanticBorder)

            // Prayers list
            val dhuhrTitle = com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(com.example.data.model.PrayerName.DHUHR, isFriday)
            val todayDateString = remember { java.time.LocalDate.now().toString() }

            val prayerList = listOf(
                com.example.data.model.PrayerName.FAJR to "Fajr",
                com.example.data.model.PrayerName.DHUHR to dhuhrTitle,
                com.example.data.model.PrayerName.ASR to "Asr",
                com.example.data.model.PrayerName.MAGHRIB to "Maghrib",
                com.example.data.model.PrayerName.ISHA to "Isha"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                prayerList.forEach { (pEnum, pDisplayName) ->
                    val status = com.example.data.db.PrayerLogEntity.resolvePrayerStatus(
                        log = log,
                        prayerName = pEnum,
                        prayerTimeMillis = null,
                        targetDateString = dateString,
                        todayDateString = todayDateString
                    )

                    val note = log?.getNote(pEnum)
                    val isQadaAdded = log?.isQadaAdded(pEnum) ?: false

                    PersonalLogPrayerRow(
                        prayerName = pEnum,
                        displayName = pDisplayName,
                        status = status,
                        note = note,
                        isQadaAdded = isQadaAdded,
                        onSetStatus = { newStatus ->
                            onSetPrayerStatus(pEnum, newStatus)
                        },
                        onSaveNote = { newNote ->
                            onSavePrayerNote(pEnum, newNote)
                        },
                        onAddQada = {
                            onAddPrayerToQada(pEnum)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalLogInsightsContent(allPrayerLogs: List<com.example.data.db.PrayerLogEntity>) {
    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    if (allPrayerLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Not enough data yet",
                    fontFamily = com.example.ui.theme.SerifHeaderFont,
                    fontSize = 18.sp,
                    color = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1E1D1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Keep logging your prayers to see your consistency insights.",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 13.sp,
                    color = if (isDarkTheme) Color(0xFFB8B5B0) else Color(0xFF66635E),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    var currentStreak = 0
    var previousDate = java.time.LocalDate.now()
    
    val sortedLogs = allPrayerLogs.sortedByDescending { it.date }
    for (log in sortedLogs) {
        val logDate = java.time.LocalDate.parse(log.date)
        if (logDate.isAfter(previousDate)) continue
        if (java.time.temporal.ChronoUnit.DAYS.between(logDate, previousDate) > 1 && currentStreak > 0) {
            break
        }
        val allCompleted = log.fajrCompleted && log.dhuhrCompleted && log.asrCompleted && log.maghribCompleted && log.ishaCompleted
        if (allCompleted) {
            currentStreak++
            previousDate = logDate
        } else {
            if (currentStreak > 0 || logDate.isBefore(java.time.LocalDate.now())) break
        }
    }

    val weekAgo = java.time.LocalDate.now().minusDays(7)
    val weekLogs = allPrayerLogs.filter { java.time.LocalDate.parse(it.date).isAfter(weekAgo) }
    var totalPrayersWeek = 0
    var completedPrayersWeek = 0
    var fajrComp = 0; var dhuhrComp = 0; var asrComp = 0; var maghribComp = 0; var ishaComp = 0

    weekLogs.forEach { log ->
        val logDate = java.time.LocalDate.parse(log.date)
        if (logDate.isBefore(java.time.LocalDate.now()) || logDate.isEqual(java.time.LocalDate.now())) {
            totalPrayersWeek += 5
            if (log.fajrCompleted) { completedPrayersWeek++; fajrComp++ }
            if (log.dhuhrCompleted) { completedPrayersWeek++; dhuhrComp++ }
            if (log.asrCompleted) { completedPrayersWeek++; asrComp++ }
            if (log.maghribCompleted) { completedPrayersWeek++; maghribComp++ }
            if (log.ishaCompleted) { completedPrayersWeek++; ishaComp++ }
        }
    }
    
    val weekPercent = if (totalPrayersWeek > 0) (completedPrayersWeek * 100) / totalPrayersWeek else 0

    var af=0; var ad=0; var aa=0; var am=0; var ai=0
    allPrayerLogs.forEach { log ->
        if(log.fajrCompleted) af++
        if(log.dhuhrCompleted) ad++
        if(log.asrCompleted) aa++
        if(log.maghribCompleted) am++
        if(log.ishaCompleted) ai++
    }
    
    val comps = listOf(
        Pair("Fajr", af), Pair("Dhuhr", ad), Pair("Asr", aa), Pair("Maghrib", am), Pair("Isha", ai)
    )
    val mostConsistent = comps.maxByOrNull { it.second }?.first ?: "N/A"
    val leastConsistent = comps.minByOrNull { it.second }?.first ?: "N/A"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InsightCard("Current streak", "$currentStreak days", Modifier.weight(1f))
            InsightCard("This week", "$weekPercent%", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InsightCard("Most consistent", mostConsistent, Modifier.weight(1f))
            InsightCard("Needs attention", leastConsistent, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(56.dp))
    }
}

@Composable
fun InsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val cardBg = if (isDarkTheme) Color(0xFF20201E) else Color.semanticSurface
    val cardBorder = if (isDarkTheme) BorderStroke(1.dp, Color(0xFF3A3836)) else BorderStroke(1.dp, Color(0xFFD5D1C9))
    val titleColor = if (isDarkTheme) Color(0xFFB8B5B0) else Color(0xFF66635E)
    val valueColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1E1D1A)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = cardBorder,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = com.example.ui.theme.SpaceGrotesk,
                fontSize = 12.sp,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = com.example.ui.theme.SerifHeaderFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun PersonalLogQadaContent(
    qadaCounts: Map<com.example.data.model.PrayerName, Int>,
    onUpdateQadaCount: (com.example.data.model.PrayerName, Int) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Track missed obligatory prayers you intend to make up.",
            fontFamily = com.example.ui.theme.SpaceGrotesk,
            fontSize = 13.sp,
            color = if (isDarkTheme) Color(0xFFB8B5B0) else Color(0xFF66635E)
        )
        Spacer(modifier = Modifier.height(4.dp))

        listOf(
            com.example.data.model.PrayerName.FAJR,
            com.example.data.model.PrayerName.DHUHR,
            com.example.data.model.PrayerName.ASR,
            com.example.data.model.PrayerName.MAGHRIB,
            com.example.data.model.PrayerName.ISHA
        ).forEach { prayer ->
            val count = qadaCounts[prayer] ?: 0
            QadaRow(
                name = prayer.name.lowercase().replaceFirstChar { it.uppercase() },
                count = count,
                onIncrement = { onUpdateQadaCount(prayer, count + 1) },
                onDecrement = { if (count > 0) onUpdateQadaCount(prayer, count - 1) },
                onComplete = { if (count > 0) onUpdateQadaCount(prayer, count - 1) }
            )
        }
        Spacer(modifier = Modifier.height(56.dp))
    }
}

@Composable
fun QadaRow(
    name: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onComplete: () -> Unit
) {
    val cardBg = Color.semanticControl
    val cardBorder = BorderStroke(1.dp, Color.semanticBorder)
    val titleColor = Color.semanticPrimaryText
    val buttonBg = Color.semanticPrimaryAccent
    val buttonFg = Color.semanticAccentForeground

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = cardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontFamily = com.example.ui.theme.SerifHeaderFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1
                )
                Text(
                    text = if (count > 0) "$count remaining" else "Completed",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 12.sp,
                    color = if (count > 0) Color.semanticError else Color.semanticSuccess,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    enabled = count > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (count > 0) Color.semanticSecondaryText else Color.semanticMutedText)
                }
                Text(
                    text = count.toString(),
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.semanticPrimaryText,
                    modifier = Modifier.widthIn(min = 28.dp)
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.semanticSecondaryText)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onComplete,
                    enabled = count > 0,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = buttonFg,
                        disabledContainerColor = Color.semanticControl,
                        disabledContentColor = Color.semanticMutedText
                    )
                ) {
                    Text(
                        text = "Make up",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PersonalLogPrayerRow(
    prayerName: com.example.data.model.PrayerName,
    displayName: String,
    status: com.example.data.model.PrayerStatus,
    note: String? = null,
    isQadaAdded: Boolean = false,
    onSetStatus: (com.example.data.model.PrayerStatus) -> Unit,
    onSaveNote: ((String?) -> Unit)? = null,
    onAddQada: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isFuture = status == com.example.data.model.PrayerStatus.FUTURE
    var isEditingNote by remember { mutableStateOf(false) }
    var textFieldValue by remember(note) {
        mutableStateOf(
            TextFieldValue(
                text = note ?: "",
                selection = TextRange((note ?: "").length)
            )
        )
    }
    var isActivelyTyping by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.isNotEmpty()) {
            isActivelyTyping = true
            kotlinx.coroutines.delay(1800L)
            isActivelyTyping = false
        } else {
            isActivelyTyping = false
        }
    }

    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isEditingNote) {
        if (isEditingNote) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textFieldValue.text.length)
            )
            kotlinx.coroutines.delay(100L)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
                // Focus requester not attached yet
            }
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (e: Exception) {
                // Bring into view failed gracefully
            }
        } else {
            isInputFocused = false
        }
    }

    val animatedBorderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isInputFocused) Color.semanticPrimaryAccent else Color.semanticBorder,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
        label = "noteInputBorderColorAnim"
    )

    val cardBg = Color.semanticControl
    val cardBorder = BorderStroke(1.dp, Color.semanticBorder)
    val prayerNameColor = Color.semanticPrimaryText
    val prayerArabicColor = Color.semanticSecondaryText

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .testTag("personal_log_row_${prayerName.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = cardBorder
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
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = prayerNameColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = prayerName.arabicName,
                            fontFamily = com.example.ui.theme.AmiriFont,
                            fontSize = 12.sp,
                            color = prayerArabicColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    val statusText = when (status) {
                        com.example.data.model.PrayerStatus.PRAYED -> "✓ Prayed"
                        com.example.data.model.PrayerStatus.MISSED -> "! Missed"
                        com.example.data.model.PrayerStatus.NEEDS_INPUT -> "○ Needs Input"
                        com.example.data.model.PrayerStatus.FUTURE -> "• Future"
                    }
                    val targetStatusColor = when (status) {
                        com.example.data.model.PrayerStatus.PRAYED -> Color.semanticSuccess
                        com.example.data.model.PrayerStatus.MISSED -> Color.semanticError
                        com.example.data.model.PrayerStatus.NEEDS_INPUT -> Color.semanticSecondaryText
                        com.example.data.model.PrayerStatus.FUTURE -> Color.semanticSecondaryText.copy(alpha = 0.5f)
                    }
                    val statusColor by androidx.compose.animation.animateColorAsState(
                        targetValue = targetStatusColor,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                        label = "prayerStatusTextColor"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedContent(
                            targetState = statusText,
                            transitionSpec = {
                                fadeIn(animationSpec = androidx.compose.animation.core.tween(180)) togetherWith
                                        fadeOut(animationSpec = androidx.compose.animation.core.tween(140))
                            },
                            label = "prayerStatusTextTransition"
                        ) { targetText ->
                            Text(
                                text = targetText,
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (!isFuture && onSaveNote != null) {
                            Surface(
                                onClick = { isEditingNote = !isEditingNote },
                                shape = RoundedCornerShape(6.dp),
                                color = if (note.isNullOrBlank()) Color.Transparent else Color.semanticPrimaryAccent.copy(alpha = 0.15f),
                                border = if (note.isNullOrBlank()) null else BorderStroke(0.5.dp, Color.semanticPrimaryAccent.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.EditNote,
                                        contentDescription = "Private Reflection Note",
                                        tint = if (note.isNullOrBlank()) Color.semanticSecondaryText.copy(alpha = 0.6f) else Color.semanticPrimaryAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    if (!note.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Note",
                                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.semanticPrimaryAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (isFuture) {
                    Box(
                        modifier = Modifier.padding(end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "—",
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.semanticSecondaryText.copy(alpha = 0.45f)
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isPrayed = status == com.example.data.model.PrayerStatus.PRAYED
                        val isMissed = status == com.example.data.model.PrayerStatus.MISSED

                        // Neutral unselected state (identical for both buttons when unselected)
                        val neutralBg = Color.semanticControl
                        val neutralBorder = BorderStroke(1.dp, Color.semanticBorder)
                        val neutralTextColor = Color.semanticSecondaryText

                        // Prayed Button
                        val targetPrayedBg = if (isPrayed) Color.semanticPrimaryAccent else neutralBg
                        val targetPrayedTextColor = if (isPrayed) Color.semanticAccentForeground else neutralTextColor
                        val targetPrayedBorderColor = if (isPrayed) Color.semanticPrimaryAccent else Color.semanticBorder
                        val prayedBg by androidx.compose.animation.animateColorAsState(
                            targetValue = targetPrayedBg,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                            label = "prayedBgAnim"
                        )
                        val prayedTextColor by androidx.compose.animation.animateColorAsState(
                            targetValue = targetPrayedTextColor,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                            label = "prayedTextAnim"
                        )
                        val prayedBorderColor by androidx.compose.animation.animateColorAsState(
                            targetValue = targetPrayedBorderColor,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                            label = "prayedBorderAnim"
                        )

                        Surface(
                            onClick = { onSetStatus(com.example.data.model.PrayerStatus.PRAYED) },
                            shape = RoundedCornerShape(10.dp),
                            color = prayedBg,
                            border = BorderStroke(1.dp, prayedBorderColor),
                            modifier = Modifier.testTag("btn_prayed_${prayerName.name.lowercase()}")
                        ) {
                            Text(
                                text = "✓ Prayed",
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 11.sp,
                                fontWeight = if (isPrayed) FontWeight.Bold else FontWeight.Normal,
                                color = prayedTextColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Missed Button
                        val targetMissedBg = if (isMissed) Color.semanticPrimaryAccent else neutralBg
                        val targetMissedTextColor = if (isMissed) Color.semanticAccentForeground else neutralTextColor
                        val targetMissedBorderColor = if (isMissed) Color.semanticPrimaryAccent else Color.semanticBorder
                        val missedBg by androidx.compose.animation.animateColorAsState(
                            targetValue = targetMissedBg,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                            label = "missedBgAnim"
                        )
                        val missedTextColor by androidx.compose.animation.animateColorAsState(
                            targetValue = targetMissedTextColor,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                            label = "missedTextAnim"
                        )
                        val missedBorderColor by androidx.compose.animation.animateColorAsState(
                            targetValue = targetMissedBorderColor,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 180),
                            label = "missedBorderAnim"
                        )

                        Surface(
                            onClick = { onSetStatus(com.example.data.model.PrayerStatus.MISSED) },
                            shape = RoundedCornerShape(10.dp),
                            color = missedBg,
                            border = BorderStroke(1.dp, missedBorderColor),
                            modifier = Modifier.testTag("btn_missed_${prayerName.name.lowercase()}")
                        ) {
                            Text(
                                text = "! Missed",
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 11.sp,
                                fontWeight = if (isMissed) FontWeight.Bold else FontWeight.Normal,
                                color = missedTextColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (isPrayed || isMissed) {
                            val resetBg = Color.semanticControl
                            val resetBorder = BorderStroke(1.dp, Color.semanticBorder)
                            val circleColor = Color.semanticSecondaryText

                            Surface(
                                onClick = { onSetStatus(com.example.data.model.PrayerStatus.NEEDS_INPUT) },
                                shape = RoundedCornerShape(10.dp),
                                color = resetBg,
                                border = resetBorder,
                                modifier = Modifier.testTag("btn_reset_${prayerName.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(11.dp)
                                            .border(1.5.dp, circleColor, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Inline Qada Action when Missed (Part 4 — Scoped State 4)
            if (status == com.example.data.model.PrayerStatus.MISSED && onAddQada != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = isQadaAdded,
                        transitionSpec = {
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(180)) togetherWith
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(140))
                        },
                        label = "qadaInlineTransition"
                    ) { qadaAdded ->
                        if (qadaAdded) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.semanticControl,
                                border = BorderStroke(1.dp, Color.semanticBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "✓ Added to Qada",
                                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.semanticSecondaryText
                                    )
                                }
                            }
                        } else {
                            Surface(
                                onClick = onAddQada,
                                shape = RoundedCornerShape(8.dp),
                                color = Color.semanticPrimaryAccent.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color.semanticPrimaryAccent.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("btn_add_qada_${prayerName.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "+ Add to Qada",
                                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.semanticPrimaryAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Private Note Editor
            AnimatedVisibility(
                visible = isEditingNote,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        placeholder = {
                            Text(
                                text = "Private reflection (e.g. khushu, prayed in jama'ah)...",
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 12.sp,
                                color = Color.semanticSecondaryText.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_${prayerName.name.lowercase()}")
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isInputFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(120)
                                        try {
                                            bringIntoViewRequester.bringIntoView()
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                }
                            },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 12.5.sp,
                            color = Color.semanticPrimaryText
                        ),
                        minLines = 1,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = animatedBorderColor,
                            unfocusedBorderColor = animatedBorderColor,
                            focusedContainerColor = Color.semanticSurface,
                            unfocusedContainerColor = Color.semanticSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val charCount = textFieldValue.text.length
                        val wordCount = remember(textFieldValue.text) {
                            if (textFieldValue.text.isBlank()) 0
                            else textFieldValue.text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
                        }

                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isActivelyTyping && textFieldValue.text.isNotEmpty(),
                                enter = fadeIn(animationSpec = tween(200)),
                                exit = fadeOut(animationSpec = tween(400))
                            ) {
                                Text(
                                    text = "$wordCount ${if (wordCount == 1) "word" else "words"} • $charCount ${if (charCount == 1) "char" else "chars"}",
                                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                                    fontSize = 11.sp,
                                    color = Color.semanticSecondaryText.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .testTag("note_live_counter_${prayerName.name.lowercase()}")
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!note.isNullOrBlank()) {
                                Surface(
                                    onClick = {
                                        textFieldValue = TextFieldValue("")
                                        onSaveNote?.invoke(null)
                                        isEditingNote = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "Delete",
                                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                                        fontSize = 11.sp,
                                        color = Color.semanticError,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    textFieldValue = TextFieldValue(note ?: "", selection = TextRange((note ?: "").length))
                                    isEditingNote = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.semanticControl,
                                border = BorderStroke(1.dp, Color.semanticBorder),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                                    fontSize = 11.sp,
                                    color = Color.semanticSecondaryText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                onClick = {
                                    onSaveNote?.invoke(textFieldValue.text.trim().ifEmpty { null })
                                    isEditingNote = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.semanticPrimaryAccent,
                                modifier = Modifier.testTag("btn_save_note_${prayerName.name.lowercase()}")
                            ) {
                                Text(
                                    text = "Save Note",
                                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.semanticAccentForeground,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrayerModeModal(
    prayer: com.example.data.model.PrayerItem,
    onDismiss: () -> Unit,
    onQiblaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Prayer Mode: ${prayer.name.name.lowercase().replaceFirstChar { it.uppercase() }}",
                fontFamily = com.example.ui.theme.SerifHeaderFont,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Time: ${prayer.timeFormatted}",
                fontFamily = com.example.ui.theme.SpaceGrotesk,
                fontSize = 16.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = {
                    onQiblaClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.semanticPrimaryAccent,
                    contentColor = androidx.compose.ui.graphics.Color.semanticAccentForeground
                )
            ) {
                Text("Find Qibla", fontFamily = com.example.ui.theme.SpaceGrotesk)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
