package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.ui.screens.VerseNumberBadge
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.rememberIsReducedMotion
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurface

/**
 * Expandable Full Surah Audio Player Bottom Sheet (Phase 2).
 * Synchronized with the compact player and Surah reading screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedQuranPlayerSheet(
    surah: Surah?,
    verse: Verse?,
    currentVerseNumber: Int?,
    isPlaying: Boolean,
    audioProgress: Float,
    isBookmarked: Boolean,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekAudio: (Float) -> Unit,
    onStopAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isDark = isAppInDarkTheme()
    val isReducedMotion = rememberIsReducedMotion()
    val accent = Color.semanticPrimaryAccent
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val sheetBg = Color.semanticSurface

    val totalVerses = surah?.versesCount ?: 1
    val displayVerseNumber = currentVerseNumber ?: verse?.verseNumber ?: 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(textSecondary.copy(alpha = 0.35f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("expanded_quran_player_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar: Collapse Chevron, Surah Badge, Bookmark Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("collapse_full_player_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = textPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Surah Number Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = if (isDark) 0.16f else 0.10f))
                        .border(
                            width = 1.dp,
                            color = accent.copy(alpha = if (isDark) 0.40f else 0.30f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SURAH ${surah?.number ?: 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = accent
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("full_player_bookmark_btn")
                ) {
                    AnimatedContent(
                        targetState = isBookmarked,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) togetherWith
                                    fadeOut(animationSpec = tween(140))
                        },
                        label = "playerBookmarkAnim"
                    ) { activeBk ->
                        Icon(
                            imageVector = if (activeBk) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (activeBk) "Remove Bookmark" else "Bookmark Verse",
                            tint = if (activeBk) accent else textPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Surah Title & Calligraphy
            Text(
                text = surah?.nameEnglish ?: "The Opening",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = SerifHeaderFont,
                    fontWeight = FontWeight.Bold
                ),
                color = textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = surah?.nameArabic ?: "الفاتحة",
                fontFamily = AmiriFont,
                fontSize = 28.sp,
                color = accent,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${surah?.revelationPlace ?: "Meccan"} • ${surah?.versesCount ?: 7} Verses • ${surah?.englishTranslation ?: "The Opening"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.semanticMutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Active Verse Preview Card (with accent border and subtle background tint)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = if (isDark) 0.10f else 0.06f))
                    .border(
                        width = 1.dp,
                        color = accent.copy(alpha = if (isDark) 0.35f else 0.25f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VerseNumberBadge(verseNumber = displayVerseNumber)
                            Text(
                                text = "Verse $displayVerseNumber of $totalVerses",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = accent
                            )
                        }

                        // Subtle active recitation wave
                        val waveTransition = rememberInfiniteTransition(label = "playerWave")
                        val waveHeight by if (isReducedMotion || !isPlaying) {
                            remember { mutableFloatStateOf(0.4f) }
                        } else {
                            waveTransition.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "waveH"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = if (isPlaying) 0.20f else 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = "Active recitation",
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arabic Verse Text
                    val arabicContent = verse?.textArabic ?: (surah?.nameArabic ?: "")
                    if (arabicContent.isNotEmpty()) {
                        Text(
                            text = arabicContent,
                            fontFamily = AmiriFont,
                            fontSize = 24.sp,
                            lineHeight = 42.sp,
                            color = textPrimary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // English Translation
                    val englishContent = verse?.textEnglish ?: ""
                    if (englishContent.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = englishContent,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            ),
                            color = textSecondary,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reciter Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.semanticBorder.copy(alpha = if (isDark) 0.15f else 0.25f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reciter: Mishary Rashid Alafasy",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = textPrimary.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scrubbable Progress Bar & Timers
            var isDraggingProgress by remember { mutableStateOf(false) }
            var dragProgressFraction by remember { mutableFloatStateOf(0f) }
            val currentDisplayProg = if (isDraggingProgress) dragProgressFraction else audioProgress.coerceIn(0f, 1f)

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("full_player_seekbar")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    dragProgressFraction = fraction
                                    isDraggingProgress = true
                                    onSeekAudio(fraction)
                                    tryAwaitRelease()
                                    isDraggingProgress = false
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isDraggingProgress = true
                                    dragProgressFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onSeekAudio(dragProgressFraction)
                                },
                                onDragEnd = {
                                    isDraggingProgress = false
                                    onSeekAudio(dragProgressFraction)
                                },
                                onDragCancel = {
                                    isDraggingProgress = false
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    dragProgressFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onSeekAudio(dragProgressFraction)
                                }
                            )
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    val trackHeight = 4.dp
                    val inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)

                    // Inactive Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(trackHeight)
                            .clip(CircleShape)
                            .background(inactiveTrackColor)
                    )

                    // Active Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = currentDisplayProg)
                            .height(trackHeight)
                            .clip(CircleShape)
                            .background(accent)
                    )

                    // Thumb indicator
                    if (currentDisplayProg > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = currentDisplayProg)
                                .height(16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, accent, CircleShape)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val percentInt = (currentDisplayProg * 100).toInt()
                    Text(
                        text = "$percentInt%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.semanticMutedText
                    )
                    Text(
                        text = if (currentDisplayProg >= 0.99f) "Completed" else "Reciting",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.semanticMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls Row: Stop, Skip Prev, Focal Play/Pause, Skip Next, Close/Stop
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Button
                IconButton(
                    onClick = onStopAudio,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_stop_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop Audio",
                        tint = textPrimary.copy(alpha = 0.65f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Skip Previous
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("full_player_prev_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Verse",
                        tint = textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Focal Play / Pause 64dp Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .border(
                            width = 2.dp,
                            color = accent.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .testTag("full_player_play_pause_btn")
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onPlayPause() })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) togetherWith
                                    fadeOut(animationSpec = tween(140))
                        },
                        label = "fullPlayerPlayPauseAnim"
                    ) { activePlaying ->
                        Icon(
                            imageVector = if (activePlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (activePlaying) "Pause" else "Play",
                            tint = Color.semanticAccentForeground,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Skip Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("full_player_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Verse",
                        tint = textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Redundant/Secondary spacer for balanced alignment
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = textPrimary.copy(alpha = 0.65f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
