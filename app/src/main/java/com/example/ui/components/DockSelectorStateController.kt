package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.example.ui.theme.LocalVibrationEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.round

/**
 * Centralized state controller for the selector ball's drag progress.
 * Maintains a clean, circular selector ball with a subtle, non-deforming scale animation on press/hold.
 */
@Stable
class DockSelectorStateController(
    private val coroutineScope: CoroutineScope,
    private val view: View?,
    private val hapticFeedback: HapticFeedback?,
    private val isVibrationEnabled: Boolean,
    initialIndex: Int = 0
) {
    // Core position and uniform scale animatables
    val ballPositionAnimatable = Animatable(initialIndex.toFloat())
    val ballScale = Animatable(1.00f)

    // Brief ring-ripple animation triggered upon initial touch press
    val pressRippleProgress = Animatable(0f)
    val pressRippleAlpha = Animatable(0f)

    // Backwards-compatible scale accessors maintaining perfect circular shape
    val ballScaleX: Animatable<Float, *> get() = ballScale
    val ballScaleY: Animatable<Float, *> get() = ballScale

    // Discrete active navigation index
    private val _activeNavIndex = mutableIntStateOf(initialIndex)
    val activeNavIndex: State<Int> = _activeNavIndex

    val activeNavItem: State<NavItem> = derivedStateOf {
        val idx = _activeNavIndex.intValue.coerceIn(0, NavItem.entries.size - 1)
        NavItem.entries[idx]
    }

    // Drag interaction states
    private val _isDragging = mutableStateOf(false)
    val isDragging: State<Boolean> = _isDragging

    private val _isHoldAndSlideActive = mutableStateOf(false)
    val isHoldAndSlideActive: State<Boolean> = _isHoldAndSlideActive

    // Page loading synchronization fraction (0f..4f continuous)
    private val _pageLoadProgress = mutableFloatStateOf(initialIndex.toFloat())
    val pageLoadProgress: State<Float> = _pageLoadProgress

    // Tracking variables during gesture
    private var lastHapticSlot: Int = initialIndex
    private var longPressJob: Job? = null
    var isLongPressedPopoverTriggered: Boolean = false
    private var lastMoveX: Float = 0f
    private var lastMoveTime: Long = 0L
    private var dragVelocity: Float = 0f
    private var isBoundaryHapticTriggered: Boolean = false

    // Haptic helpers
    fun performActivationHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view?.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } catch (_: Exception) {
            try {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }

    fun performItemChangeHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view?.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        } catch (_: Exception) {
            try {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    fun performBoundaryResistanceHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        } catch (_: Exception) {
            try {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }

    /**
     * Maps screen X coordinate into a continuous slot fraction (-0.38f..4.38f)
     * applying non-linear rubber-band resistance when dragged beyond dock boundaries.
     */
    fun computeFraction(x: Float, slotPitchPx: Float, startOffsetPx: Float): Float {
        val linearFraction = (x - startOffsetPx) / slotPitchPx
        return when {
            linearFraction < 0f -> {
                // Non-linear rubber-band stretch beyond left boundary (max ~ -0.38)
                val overscroll = -linearFraction
                val damped = (1f - kotlin.math.exp(-overscroll * 0.75f)) * 0.38f
                -damped
            }
            linearFraction > 4f -> {
                // Non-linear rubber-band stretch beyond right boundary (max ~ +4.38)
                val overscroll = linearFraction - 4f
                val damped = (1f - kotlin.math.exp(-overscroll * 0.75f)) * 0.38f
                4f + damped
            }
            else -> linearFraction
        }
    }

    /**
     * Applies a non-linear magnetic attraction field that pulls the selector ball towards
     * the center of each destination icon slot during continuous dragging.
     */
    fun applyMagneticSnap(fraction: Float, snapRadius: Float = 0.40f, magneticStrength: Float = 0.48f): Float {
        if (fraction < 0f || fraction > 4f) {
            // Beyond boundary: governed by rubber-band stretch and boundary bounce spring
            return fraction
        }
        val nearestSlot = round(fraction).coerceIn(0f, 4f)
        val delta = fraction - nearestSlot
        val absDelta = abs(delta)
        return if (absDelta < snapRadius) {
            val normalized = absDelta / snapRadius // 0f..1f
            // Quadratic pull factor: maximum attraction at center, fading gently to boundary
            val pullFactor = 1f - (1f - normalized) * (1f - normalized) * magneticStrength
            (nearestSlot + delta * pullFactor).coerceIn(0f, 4f)
        } else {
            fraction.coerceIn(0f, 4f)
        }
    }

    /**
     * Starts the press gesture on the selector ball.
     * Smoothly scales the ball to 1.10f without any deformation.
     */
    fun onGestureStart(
        startX: Float,
        slotPitchPx: Float,
        startOffsetPx: Float,
        currentRoute: String,
        onFifthSlotAction: () -> Unit
    ) {
        val rawFraction = computeFraction(startX, slotPitchPx, startOffsetPx)
        val magnetizedFraction = applyMagneticSnap(rawFraction)
        val startSlot = round(magnetizedFraction).toInt().coerceIn(0, 4)
        lastHapticSlot = startSlot
        _isHoldAndSlideActive.value = false
        isLongPressedPopoverTriggered = false
        isBoundaryHapticTriggered = false
        lastMoveX = startX
        lastMoveTime = System.currentTimeMillis()
        dragVelocity = 0f

        // Trigger a very brief, minimal ring-ripple animation upon exact initial press
        coroutineScope.launch {
            pressRippleProgress.snapTo(0f)
            pressRippleAlpha.snapTo(0.70f)
            launch {
                pressRippleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 260, easing = EaseOut)
                )
            }
            launch {
                pressRippleAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 260, easing = EaseOut)
                )
            }
        }

        // Subtle, premium press-and-hold scale up (1.00 -> 1.10) with no deformation
        coroutineScope.launch {
            ballScale.animateTo(
                targetValue = 1.10f,
                animationSpec = tween(durationMillis = 140, easing = EaseOut)
            )
        }

        longPressJob?.cancel()
        longPressJob = coroutineScope.launch {
            if (startSlot == 4) {
                delay(380L)
                isLongPressedPopoverTriggered = true
                onFifthSlotAction()
                performActivationHaptic()
            } else {
                delay(130L)
                _isHoldAndSlideActive.value = true
                _isDragging.value = true
                launch {
                    ballPositionAnimatable.animateTo(magnetizedFraction, DOCK_DRAG_SPRING)
                }
                performActivationHaptic()
            }
        }
    }

    /**
     * Processes live pointer movement, keeping the ball a clean circle and updating navigation atomically.
     */
    fun onGestureMove(
        currentX: Float,
        currentY: Float,
        startX: Float,
        startY: Float,
        containerHeightPx: Float,
        slotPitchPx: Float,
        startOffsetPx: Float,
        onNavigate: (NavItem) -> Unit
    ): Boolean {
        val verticalDist = abs(currentY - startY)
        val horizontalDist = abs(currentX - startX)

        if (verticalDist > containerHeightPx * 6.0f) {
            // Out of bounds cancel
            cancelGesture()
            return false
        }

        val now = System.currentTimeMillis()
        val dt = (now - lastMoveTime).coerceAtLeast(1L)
        if (dt < 250L) {
            val dSlot = (currentX - lastMoveX) / slotPitchPx
            val instantaneousVelocity = (dSlot / (dt / 1000f))
            dragVelocity = dragVelocity * 0.35f + instantaneousVelocity * 0.65f
        }
        lastMoveX = currentX
        lastMoveTime = now

        if (horizontalDist > 8f) {
            _isHoldAndSlideActive.value = true
            _isDragging.value = true
            longPressJob?.cancel()
        }

        if (_isHoldAndSlideActive.value) {
            val rawFraction = computeFraction(currentX, slotPitchPx, startOffsetPx)
            val magnetizedFraction = applyMagneticSnap(rawFraction)
            _pageLoadProgress.floatValue = magnetizedFraction.coerceIn(0f, 4f)

            // Tactile sense of boundary constraint
            val isOvershooting = rawFraction < -0.02f || rawFraction > 4.02f
            if (isOvershooting && !isBoundaryHapticTriggered) {
                isBoundaryHapticTriggered = true
                performBoundaryResistanceHaptic()
            } else if (!isOvershooting && isBoundaryHapticTriggered) {
                isBoundaryHapticTriggered = false
            }

            coroutineScope.launch {
                ballPositionAnimatable.snapTo(magnetizedFraction)
            }

            val currentSlot = round(magnetizedFraction).toInt().coerceIn(0, 4)
            if (currentSlot != lastHapticSlot) {
                lastHapticSlot = currentSlot
                _activeNavIndex.intValue = currentSlot
                performItemChangeHaptic()
                onNavigate(NavItem.entries[currentSlot])
            }
        }
        return true
    }

    /**
     * Handles touch release, applying a spring-based deceleration animation to glide into the nearest slot,
     * or an overshoot bounce spring when releasing beyond dock boundaries.
     */
    fun onGestureRelease(
        currentRoute: String,
        onNavigate: (NavItem) -> Unit,
        onFifthSlotAction: () -> Unit
    ) {
        longPressJob?.cancel()

        val now = System.currentTimeMillis()
        val effectiveVelocity = if (now - lastMoveTime < 140L) dragVelocity.coerceIn(-14f, 14f) else 0f
        val isOvershooting = ballPositionAnimatable.value < -0.02f || ballPositionAnimatable.value > 4.02f
        val snapSpring = if (isOvershooting) DOCK_BOUNDARY_BOUNCE_SPRING else DOCK_RELEASE_SNAP_SPRING

        if (!_isHoldAndSlideActive.value) {
            val startSlot = lastHapticSlot
            if (startSlot == 4 && !isLongPressedPopoverTriggered) {
                onFifthSlotAction()
                performItemChangeHaptic()
            } else if (startSlot != 4) {
                _activeNavIndex.intValue = startSlot
                onNavigate(NavItem.entries[startSlot])
                performItemChangeHaptic()
            }
            coroutineScope.launch {
                ballPositionAnimatable.animateTo(
                    targetValue = startSlot.toFloat(),
                    animationSpec = snapSpring,
                    initialVelocity = effectiveVelocity
                )
            }
        } else {
            // Predict the target slot based on current position, release momentum, and magnetic snap
            val rawPredictedFraction = ballPositionAnimatable.value + (effectiveVelocity * 0.09f)
            val magnetizedPredicted = applyMagneticSnap(rawPredictedFraction)
            val finalSlot = round(magnetizedPredicted).toInt().coerceIn(0, 4)
            _activeNavIndex.intValue = finalSlot
            if (finalSlot == 4 && currentRoute == "explore" && !isLongPressedPopoverTriggered) {
                onNavigate(NavItem.EXPLORE)
            } else {
                onNavigate(NavItem.entries[finalSlot])
            }
            performItemChangeHaptic()
            coroutineScope.launch {
                ballPositionAnimatable.animateTo(
                    targetValue = finalSlot.toFloat(),
                    animationSpec = snapSpring,
                    initialVelocity = effectiveVelocity
                )
            }
        }

        // Smoothly return to normal size (1.10 -> 1.00) without bounce or overshoot
        coroutineScope.launch {
            ballScale.animateTo(
                targetValue = 1.00f,
                animationSpec = tween(durationMillis = 180, easing = EaseOut)
            )
        }

        _isDragging.value = false
        _isHoldAndSlideActive.value = false
    }

    /**
     * Cancels active drag gesture and returns smoothly to normal size and resting slot.
     */
    fun cancelGesture(targetSlot: Float? = null) {
        longPressJob?.cancel()
        _isDragging.value = false
        _isHoldAndSlideActive.value = false

        val isOvershooting = ballPositionAnimatable.value < -0.02f || ballPositionAnimatable.value > 4.02f
        val snapSpring = if (isOvershooting) DOCK_BOUNDARY_BOUNCE_SPRING else DOCK_RELEASE_SNAP_SPRING
        val target = targetSlot ?: _activeNavIndex.intValue.toFloat()
        coroutineScope.launch {
            ballPositionAnimatable.animateTo(target, snapSpring)
        }
        coroutineScope.launch {
            ballScale.animateTo(
                targetValue = 1.00f,
                animationSpec = tween(durationMillis = 180, easing = EaseOut)
            )
        }
    }

    /**
     * Synchronizes the ball's position with the pager when not dragging.
     */
    fun syncWithPager(targetFraction: Float) {
        if (!_isDragging.value) {
            _pageLoadProgress.floatValue = targetFraction
            _activeNavIndex.intValue = round(targetFraction).toInt().coerceIn(0, 4)
            coroutineScope.launch {
                ballPositionAnimatable.animateTo(targetFraction, DOCK_RESTING_SPRING)
            }
        }
    }

    /**
     * Programmatically navigates to a slot with spring animation.
     */
    fun animateToSlot(slot: Int, onNavigate: ((NavItem) -> Unit)? = null) {
        val targetSlot = slot.coerceIn(0, 4)
        _activeNavIndex.intValue = targetSlot
        _pageLoadProgress.floatValue = targetSlot.toFloat()
        onNavigate?.invoke(NavItem.entries[targetSlot])
        coroutineScope.launch {
            ballPositionAnimatable.animateTo(targetSlot.toFloat(), DOCK_RESTING_SPRING)
        }
    }
}

/**
 * Remember helper function to construct and provide a [DockSelectorStateController].
 */
@Composable
fun rememberDockSelectorController(
    initialIndex: Int = 0
): DockSelectorStateController {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    val isVibrationEnabled = LocalVibrationEnabled.current

    return remember {
        DockSelectorStateController(
            coroutineScope = coroutineScope,
            view = view,
            hapticFeedback = hapticFeedback,
            isVibrationEnabled = isVibrationEnabled,
            initialIndex = initialIndex
        )
    }
}
