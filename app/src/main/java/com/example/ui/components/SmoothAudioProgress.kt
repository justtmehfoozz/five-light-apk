package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.max

/**
 * Provides a smooth, frame-interpolated visual progress State for audio progress bars.
 * Derives authoritative position from the underlying audio player's progress and duration,
 * smoothly advancing monotonically between discrete player polling updates while preventing
 * backward jitter and instantly synchronizing on user seeks, track changes, or pauses.
 */
@Composable
fun rememberSmoothAudioProgressState(
    audioProgressFlow: StateFlow<Float>?,
    audioDurationMsFlow: StateFlow<Long>?,
    fallbackProgress: Float = 0f,
    fallbackDurationMs: Long = 0L,
    isPlaying: Boolean,
    isLoading: Boolean,
    isDragging: Boolean,
    dragProgressFraction: Float
): State<Float> {
    val progressState = remember { mutableFloatStateOf(fallbackProgress) }

    if (isDragging) {
        progressState.floatValue = dragProgressFraction.coerceIn(0f, 1f)
        return progressState
    }

    val rawProgress = audioProgressFlow?.collectAsStateWithLifecycle()?.value ?: fallbackProgress
    val durationMs = audioDurationMsFlow?.collectAsStateWithLifecycle()?.value ?: fallbackDurationMs

    // Synchronize with authoritative player state changes
    LaunchedEffect(rawProgress, isPlaying, isLoading) {
        if (!isPlaying || isLoading || rawProgress == 0f) {
            progressState.floatValue = rawProgress
        } else {
            val diff = abs(progressState.floatValue - rawProgress)
            // Instant snap on seek or major desync (> 5%), otherwise keep forward monotonic flow
            if (diff > 0.05f || rawProgress < (progressState.floatValue - 0.05f)) {
                progressState.floatValue = rawProgress
            } else if (rawProgress > progressState.floatValue) {
                // If authoritative progress is ahead of interpolated progress, smoothly anchor to it
                progressState.floatValue = max(progressState.floatValue, rawProgress)
            }
        }
    }

    // Frame-by-frame smooth continuous advance during uninterrupted active playback
    LaunchedEffect(isPlaying, isLoading, durationMs) {
        if (isPlaying && !isLoading && durationMs > 0L) {
            var lastFrameNanos = 0L
            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameNanos != 0L) {
                        val dtMs = (frameTimeNanos - lastFrameNanos) / 1_000_000f
                        // Guard against unrealistic frame deltas (e.g., app backgrounding or lag spike)
                        if (dtMs in 1f..100f) {
                            val advanceFraction = dtMs / durationMs.toFloat()
                            val nextVal = (progressState.floatValue + advanceFraction).coerceIn(0f, 1f)
                            progressState.floatValue = nextVal
                        }
                    }
                    lastFrameNanos = frameTimeNanos
                }
            }
        }
    }

    if (!isPlaying || isLoading) {
        progressState.floatValue = rawProgress.coerceIn(0f, 1f)
    }

    return progressState
}

@Composable
fun rememberSmoothAudioProgress(
    audioProgressFlow: StateFlow<Float>?,
    audioDurationMsFlow: StateFlow<Long>?,
    fallbackProgress: Float = 0f,
    fallbackDurationMs: Long = 0L,
    isPlaying: Boolean,
    isLoading: Boolean,
    isDragging: Boolean,
    dragProgressFraction: Float
): Float {
    return rememberSmoothAudioProgressState(
        audioProgressFlow = audioProgressFlow,
        audioDurationMsFlow = audioDurationMsFlow,
        fallbackProgress = fallbackProgress,
        fallbackDurationMs = fallbackDurationMs,
        isPlaying = isPlaying,
        isLoading = isLoading,
        isDragging = isDragging,
        dragProgressFraction = dragProgressFraction
    ).value
}
