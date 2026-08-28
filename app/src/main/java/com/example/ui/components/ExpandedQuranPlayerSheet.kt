package com.example.ui.components

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import com.example.ui.theme.fiveLightPressable
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.semanticSurfaceElevated

/**
 * Format milliseconds to mm:ss or hh:mm:ss.
 */
private fun formatAudioTime(timeMs: Long): String {
    if (timeMs <= 0) return "0:00"
    val totalSeconds = timeMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
    }
}

/**
 * Lightweight playback waveform indicator.
 * Animates vertical bars only when audio is actively playing and reduced motion is off.
 */
@Composable
private fun PlaybackWaveform(
    isPlayingProvider: () -> Boolean = { false },
    isReducedMotion: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isPlaying = isPlayingProvider()
    val infiniteTransition = rememberInfiniteTransition(label = "waveformAnim")
    val b1 = infiniteTransition.animateFloat(
        initialValue = if (isPlaying && !isReducedMotion) 0.35f else 0.25f, targetValue = if (isPlaying && !isReducedMotion) 0.95f else 0.25f,
        animationSpec = if (isPlaying && !isReducedMotion) infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse) else infiniteRepeatable(tween(300)),
        label = "b1"
    )
    val b2 = infiniteTransition.animateFloat(
        initialValue = if (isPlaying && !isReducedMotion) 0.85f else 0.25f, targetValue = if (isPlaying && !isReducedMotion) 0.25f else 0.25f,
        animationSpec = if (isPlaying && !isReducedMotion) infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse) else infiniteRepeatable(tween(300)),
        label = "b2"
    )
    val b3 = infiniteTransition.animateFloat(
        initialValue = if (isPlaying && !isReducedMotion) 0.40f else 0.25f, targetValue = if (isPlaying && !isReducedMotion) 1.00f else 0.25f,
        animationSpec = if (isPlaying && !isReducedMotion) infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse) else infiniteRepeatable(tween(300)),
        label = "b3"
    )
    val b4 = infiniteTransition.animateFloat(
        initialValue = if (isPlaying && !isReducedMotion) 0.90f else 0.25f, targetValue = if (isPlaying && !isReducedMotion) 0.30f else 0.25f,
        animationSpec = if (isPlaying && !isReducedMotion) infiniteRepeatable(tween(390, easing = FastOutSlowInEasing), RepeatMode.Reverse) else infiniteRepeatable(tween(300)),
        label = "b4"
    )

    androidx.compose.foundation.Canvas(modifier = modifier.width(18.dp).height(14.dp)) {
        val spacing = 2.5.dp.toPx()
        val barWidth = 2.5.dp.toPx()
        val totalHeight = 14.dp.toPx()
        val cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
        
        val fractions = listOf(b1.value, b2.value, b3.value, b4.value)
        var xOffset = 0f
        for (fraction in fractions) {
            val barHeight = (totalHeight * fraction).coerceAtLeast(3.dp.toPx())
            val yOffset = (totalHeight - barHeight) / 2f
            drawRoundRect(
                color = accentColor,
                topLeft = androidx.compose.ui.geometry.Offset(xOffset, yOffset),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
            xOffset += barWidth + spacing
        }
    }
}

/**
 * Expandable Full Surah Audio Player Bottom Sheet (Phase 3).
 * Synchronized with the compact player and Surah reading screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedQuranPlayerSheet(
    surah: Surah?,
    verse: Verse?,
    currentVerseNumberProvider: () -> Int? = { null },
    isPlayingProvider: () -> Boolean = { false },
    isLoadingProvider: () -> Boolean = { false },
    audioProgress: Float = 0f,
    audioPositionMs: Long = 0L,
    audioDurationMs: Long = 0L,
    audioProgressFlow: StateFlow<Float>? = null,
    audioPositionMsFlow: StateFlow<Long>? = null,
    audioDurationMsFlow: StateFlow<Long>? = null,
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
    val currentAudioProgress = audioProgressFlow?.collectAsStateWithLifecycle()?.value ?: audioProgress
    val currentAudioPositionMs = audioPositionMsFlow?.collectAsStateWithLifecycle()?.value ?: audioPositionMs
    val currentAudioDurationMs = audioDurationMsFlow?.collectAsStateWithLifecycle()?.value ?: audioDurationMs

    val isDark = isAppInDarkTheme()
    val isPlaying = isPlayingProvider()
    val isLoading = isLoadingProvider()
    val currentVerseNumber = currentVerseNumberProvider()
    val isReducedMotion = rememberIsReducedMotion()
    val accent = Color.semanticPrimaryAccent
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val sheetBg = Color.semanticSurface
    val haptic = LocalHapticFeedback.current

    val totalVerses = if (surah?.number == 1) 6 else (surah?.versesCount ?: 1)
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
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleBookmark()
                    },
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

            // Active Verse Preview Card (with accent border and smooth internal content transition)
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
                AnimatedContent(
                    targetState = Triple(displayVerseNumber, verse?.textArabic, verse?.textEnglish),
                    transitionSpec = {
                        if (isReducedMotion) {
                            fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                        } else {
                            (fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                    slideInVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { 6 }) togetherWith
                                    (fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                            slideOutVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)) { -6 })
                        }
                    },
                    label = "activeVerseContentAnim"
                ) { (vNum, arText, enText) ->
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
                                VerseNumberBadge(verseNumber = vNum)
                                Text(
                                    text = "Verse $vNum of $totalVerses",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = accent
                                )
                            }

                            // Subtle active playback waveform indicator
                            Box(
                                modifier = Modifier
                                    .size(width = 30.dp, height = 24.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = if (isPlaying) 0.18f else 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                PlaybackWaveform(
                                    isPlayingProvider = { isPlaying },
                                    isReducedMotion = isReducedMotion,
                                    accentColor = accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Arabic Verse Text
                        val displayArabic = arText ?: (surah?.nameArabic ?: "")
                        if (displayArabic.isNotEmpty()) {
                            Text(
                                text = displayArabic,
                                fontFamily = AmiriFont,
                                fontSize = 24.sp,
                                lineHeight = 42.sp,
                                color = textPrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // English Translation
                        val displayEnglish = enText ?: ""
                        if (displayEnglish.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = displayEnglish,
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
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reciter Info Bar (Simple text-focused secondary metadata)
            val reciterContainerBg = if (isDark) {
                Color.semanticBorder.copy(alpha = 0.15f)
            } else {
                Color.semanticSurfaceElevated
            }
            val reciterContainerBorder = Color.semanticBorder.copy(alpha = if (isDark) 0.25f else 0.50f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(reciterContainerBg)
                    .border(
                        width = 1.dp,
                        color = reciterContainerBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reciter • Mishary Rashid Alafasy",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    ),
                    color = textSecondary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scrubbable Progress Bar & Timers
            var isDraggingProgress by remember { mutableStateOf(false) }
            var dragProgressFraction by remember { mutableFloatStateOf(0f) }
            val currentDisplayProg = if (isDraggingProgress) dragProgressFraction else currentAudioProgress.coerceIn(0f, 1f)
            val trackHeight by animateDpAsState(
                targetValue = if (isDraggingProgress && !isReducedMotion) 6.dp else 4.dp,
                animationSpec = tween(180),
                label = "sheetTrackH"
            )
            val thumbSize by animateDpAsState(
                targetValue = if (isDraggingProgress && !isReducedMotion) 18.dp else 14.dp,
                animationSpec = tween(180),
                label = "sheetThumbS"
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .testTag("full_player_seekbar")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                .height(20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(thumbSize)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, accent, CircleShape)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayPosMs = if (isDraggingProgress && currentAudioDurationMs > 0) {
                        (dragProgressFraction * currentAudioDurationMs).toLong()
                    } else {
                        currentAudioPositionMs
                    }

                    Text(
                        text = formatAudioTime(displayPosMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = textSecondary.copy(alpha = 0.85f)
                    )
                    val totalDurText = if (isLoading) {
                        "Loading..."
                    } else if (currentAudioDurationMs > 0) {
                        "${formatAudioTime(displayPosMs)} / ${formatAudioTime(currentAudioDurationMs)}"
                    } else {
                        "--:--"
                    }
                    Text(
                        text = totalDurText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = textSecondary.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls Row: Stop, Skip Prev, Focal Play/Pause, Skip Next, Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3. Stop Button (Visually quieter 40dp button container)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .fiveLightPressable(onClick = onStopAudio)
                        .testTag("full_player_stop_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop Audio",
                        tint = textSecondary.copy(alpha = 0.55f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 2. Skip Previous (52dp button, prominent)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .fiveLightPressable(onClick = onSkipPrevious)
                        .testTag("full_player_prev_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Verse",
                        tint = textPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // 1. Focal Play / Pause 64dp Button (Dominant control)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .border(
                            width = 1.5.dp,
                            color = accent.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .fiveLightPressable(onClick = onPlayPause)
                        .testTag("full_player_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = Color.semanticAccentForeground,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                if (isReducedMotion) {
                                    fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                                } else {
                                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                                }
                            },
                            label = "fullPlayerPlayPauseAnim"
                        ) { activePlaying ->
                            Icon(
                                imageVector = if (activePlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (activePlaying) "Pause" else "Play",
                                tint = Color.semanticAccentForeground,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // 2. Skip Next (52dp button, prominent)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .fiveLightPressable(onClick = onSkipNext)
                        .testTag("full_player_next_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Verse",
                        tint = textPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // 4. Collapse Button (Smallest and most understated 40dp button container)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .fiveLightPressable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = textSecondary.copy(alpha = 0.55f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
