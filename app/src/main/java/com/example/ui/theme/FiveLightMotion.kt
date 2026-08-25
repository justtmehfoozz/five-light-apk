package com.example.ui.theme

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch

/**
 * Composition Local for Global Vibration preference.
 */
val LocalVibrationEnabled = compositionLocalOf { true }

/**
 * Centralized FiveLight Motion & Haptic System.
 * Standardizes durations, spring configs, reduced-motion behavior, and press feedback across all screens.
 */
object FiveLightMotion {
    // 1. MICRO (120ms): Button press feedback, icon press, small toggle state changes
    val DURATION_MICRO = 120
    val SPEC_MICRO_TWEEN: TweenSpec<Float> = tween(durationMillis = DURATION_MICRO)

    // 2. STANDARD (200ms): Content crossfade, deletion collapse, date switching
    val DURATION_STANDARD = 200
    val SPEC_STANDARD_TWEEN: TweenSpec<Float> = tween(durationMillis = DURATION_STANDARD)

    // 3. NAVIGATION (250ms): Screen-to-screen transitions, reader view toggle
    val DURATION_NAVIGATION = 250
    val SPEC_NAV_TWEEN: TweenSpec<Float> = tween(durationMillis = DURATION_NAVIGATION)
    val SPEC_NAV_OFFSET_TWEEN: TweenSpec<androidx.compose.ui.unit.IntOffset> = tween(durationMillis = DURATION_NAVIGATION)

    // 4. SHEET (300ms): Bottom sheet & modal slide/fade
    val DURATION_SHEET = 300
    val SPEC_SHEET_TWEEN: TweenSpec<Float> = tween(durationMillis = DURATION_SHEET)
    val SPEC_SHEET_OFFSET_TWEEN: TweenSpec<androidx.compose.ui.unit.IntOffset> = tween(durationMillis = DURATION_SHEET)

    // 5. SHARED SPRINGS
    val SPRING_DOCK: SpringSpec<Float> = spring(
        stiffness = 500f,
        dampingRatio = 0.7071f
    )

    val SPRING_PILL: SpringSpec<Float> = spring(
        stiffness = 500f,
        dampingRatio = 0.80f
    )

    val SPRING_CALENDAR: SpringSpec<Float> = spring(
        stiffness = 480f,
        dampingRatio = 0.82f
    )

    val SPRING_PRESS: SpringSpec<Float> = spring(
        stiffness = 600f,
        dampingRatio = 0.75f
    )

    /**
     * Helper to detect system reduced-motion preference.
     */
    fun isSystemReducedMotion(context: Context): Boolean {
        return try {
            val scale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Standardized slide+fade forward transition (Screen A -> Screen B).
     */
    fun slideFadeForward(isReducedMotion: Boolean = false) = if (isReducedMotion) {
        fadeIn(snap()) togetherWith fadeOut(snap())
    } else {
        (slideInHorizontally(animationSpec = SPEC_NAV_OFFSET_TWEEN) { (it * 0.08f).toInt() } + fadeIn(SPEC_NAV_TWEEN)) togetherWith
                (slideOutHorizontally(animationSpec = SPEC_NAV_OFFSET_TWEEN) { -(it * 0.08f).toInt() } + fadeOut(SPEC_NAV_TWEEN))
    }

    /**
     * Standardized slide+fade backward transition (Screen B -> Screen A).
     */
    fun slideFadeBackward(isReducedMotion: Boolean = false) = if (isReducedMotion) {
        fadeIn(snap()) togetherWith fadeOut(snap())
    } else {
        (slideInHorizontally(animationSpec = SPEC_NAV_OFFSET_TWEEN) { -(it * 0.08f).toInt() } + fadeIn(SPEC_NAV_TWEEN)) togetherWith
                (slideOutHorizontally(animationSpec = SPEC_NAV_OFFSET_TWEEN) { (it * 0.08f).toInt() } + fadeOut(SPEC_NAV_TWEEN))
    }
}

/**
 * Standardized FiveLight Haptic Hierarchy.
 * Strictly respects global vibration preference (if false -> zero vibration).
 */
object FiveLightHaptics {
    fun performLightTap(view: View?, haptic: HapticFeedback?, isVibrationEnabled: Boolean) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        } catch (_: Exception) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    fun performMediumTap(view: View?, haptic: HapticFeedback?, isVibrationEnabled: Boolean) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view?.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        } catch (_: Exception) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }

    fun performStrongTap(view: View?, haptic: HapticFeedback?, isVibrationEnabled: Boolean) {
        if (!isVibrationEnabled) return
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }

    fun performSoftTick(view: View?, haptic: HapticFeedback?, isVibrationEnabled: Boolean) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view?.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        } catch (_: Exception) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }
}

/**
 * Composable helper to remember whether reduced motion is active.
 */
@Composable
fun rememberIsReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { FiveLightMotion.isSystemReducedMotion(context) }
}

/**
 * Universal tactile press modifier for FiveLight controls.
 * Applies a subtle physical scale feedback (1.0 -> 0.97 -> 1.0) and optional haptic feedback.
 */
fun Modifier.fiveLightPressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    performHaptic: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val isReducedMotion = rememberIsReducedMotion()
    val isVibrationEnabled = LocalVibrationEnabled.current
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isReducedMotion) pressedScale else 1.0f,
        animationSpec = FiveLightMotion.SPRING_PRESS,
        label = "fiveLightPressScale"
    )

    val opacity by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isReducedMotion) 0.94f else 1.0f,
        animationSpec = FiveLightMotion.SPEC_MICRO_TWEEN,
        label = "fiveLightPressOpacity"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = opacity
        }
        .pointerInput(enabled, isVibrationEnabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = {
                    if (performHaptic) {
                        FiveLightHaptics.performLightTap(view, hapticFeedback, isVibrationEnabled)
                    }
                    onClick()
                },
                onLongPress = if (onLongClick != null) {
                    {
                        if (performHaptic) {
                            FiveLightHaptics.performStrongTap(view, hapticFeedback, isVibrationEnabled)
                        }
                        onLongClick()
                    }
                } else null
            )
        }
}

/**
 * Quiet Empty State container that fades in softly with subtle scale (0.97 -> 1.0).
 */
@Composable
fun QuietEmptyState(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isReducedMotion = rememberIsReducedMotion()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = FiveLightMotion.SPEC_STANDARD_TWEEN,
        label = "quietEmptyAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible || isReducedMotion) 1f else 0.97f,
        animationSpec = FiveLightMotion.SPEC_STANDARD_TWEEN,
        label = "quietEmptyScale"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
        }
    ) {
        content()
    }
}

