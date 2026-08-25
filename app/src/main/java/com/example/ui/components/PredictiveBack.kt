package com.example.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Encapsulates Predictive Back state for Android back gestures.
 */
class PredictiveBackState {
    var progress by mutableFloatStateOf(0f)
        internal set
    var swipeEdge by mutableIntStateOf(BackEventCompat.EDGE_LEFT)
        internal set

    suspend fun processBackFlow(flow: Flow<BackEventCompat>, onCommit: () -> Unit) {
        try {
            flow.collect { backEvent ->
                progress = backEvent.progress
                swipeEdge = backEvent.swipeEdge
            }
            // Gesture completed successfully
            progress = 0f
            onCommit()
        } catch (e: CancellationException) {
            // Gesture cancelled by user sliding back
            progress = 0f
            throw e
        }
    }
}

@Composable
fun rememberPredictiveBackState(): PredictiveBackState {
    return remember { PredictiveBackState() }
}

@Composable
fun RegisterPredictiveBackHandler(
    enabled: Boolean,
    backState: PredictiveBackState = rememberPredictiveBackState(),
    onBack: () -> Unit
) {
    if (enabled) {
        PredictiveBackHandler(enabled = true) { progressFlow ->
            backState.processBackFlow(progressFlow, onBack)
        }
    }
}

fun Modifier.predictiveBackTransform(progress: Float, swipeEdge: Int = BackEventCompat.EDGE_LEFT): Modifier {
    if (progress <= 0f) return this
    val scale = 1f - (progress * 0.08f)
    val translationXPx = if (swipeEdge == BackEventCompat.EDGE_RIGHT) {
        -progress * 60f
    } else {
        progress * 60f
    }
    val alpha = 1f - (progress * 0.12f)

    return this.graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale
        this.translationX = translationXPx
        this.alpha = alpha
        this.clip = true
        this.shape = RoundedCornerShape((progress * 20).dp)
    }
}
