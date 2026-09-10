package com.example.ui.screens

import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.rememberPredictiveBackState

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
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
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.util.LocalVolumeKeyDispatcher
import com.example.ui.util.VolumeKeyDispatcher
import com.example.ui.util.VolumeKeyEventListener
import com.example.ui.util.findActivity
import kotlinx.coroutines.Job
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.db.DhikrHistoryEntity
import com.example.data.model.DhikrPreset
import com.example.data.model.TasbeehSound
import com.example.ui.theme.*
import com.example.ui.theme.QuietEmptyState
import com.example.ui.theme.LocalVibrationEnabled
import com.example.ui.components.PageHeader
import com.example.ui.theme.ArabicText
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.mutableIntStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehScreen(
    presets: List<DhikrPreset>,
    selectedPreset: DhikrPreset,
    dhikrCount: Int,
    dhikrTarget: Int,
    targets: List<Int> = listOf(33, 99, 100),
    dhikrHistory: List<DhikrHistoryEntity>,
    selectedTasbeehSound: TasbeehSound,
    onSelectPreset: (DhikrPreset) -> Unit,
    onReorderPresets: (List<DhikrPreset>) -> Unit = {},
    onDeleteDhikr: (String) -> Unit = {},
    onRestoreDefaultDhikrs: () -> Unit = {},
    onSetTarget: (Int) -> Unit,
    onAddCustomTarget: (Int) -> Unit = {},
    onDeleteCustomTarget: (Int) -> Unit = {},
    onIncrement: () -> Unit,
    onDecrement: () -> Unit = {},
    onReset: () -> Unit,
    onAddCustomDhikr: (transliteration: String, arabicText: String, meaning: String, target: Int) -> Unit = { _, _, _, _ -> },
    onUpdateCustomDhikr: (DhikrPreset) -> Unit = {},
    onDeleteCustomDhikr: (String) -> Unit = {},
    onToggleVibration: (Boolean) -> Unit = {},
    onSelectTasbeehSound: (TasbeehSound) -> Unit = {},
    isActiveTab: Boolean = true,
    dhikrVolumeControlsEnabled: Boolean = true,
    onToggleVolumeControls: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Vibrator Service Reference
    val vibrator = remember(context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    // Modal & Sheet States
    var showHistorySheet by remember { mutableStateOf(false) }
    var showFeedbackSettings by remember { mutableStateOf(false) }
    var showRestoreDefaultDialog by remember { mutableStateOf(false) }
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dhikr Customization & Reorder States
    var activeEditDhikrId by rememberSaveable { mutableStateOf<String?>(null) }

    // Separate Dialog States: Dhikr Dialog vs Target Dialog
    var showCustomDhikrDialog by remember { mutableStateOf(false) }
    var editingCustomDhikr by remember { mutableStateOf<DhikrPreset?>(null) }

    var showAddTargetDialog by remember { mutableStateOf(false) }

    // Deletion Dialog States
    var dhikrToDelete by remember { mutableStateOf<DhikrPreset?>(null) }
    var targetToDelete by remember { mutableStateOf<Int?>(null) }

    val tasbeehPredictiveState = rememberPredictiveBackState()
    val isTasbeehOverlayActive = showHistorySheet || showCustomDhikrDialog || showAddTargetDialog ||
            dhikrToDelete != null || targetToDelete != null || showFeedbackSettings ||
            showRestoreDefaultDialog || activeEditDhikrId != null

    RegisterPredictiveBackHandler(
        enabled = isActiveTab && isTasbeehOverlayActive,
        backState = tasbeehPredictiveState,
        onBack = {
            if (showHistorySheet) {
                showHistorySheet = false
            } else if (showCustomDhikrDialog) {
                showCustomDhikrDialog = false
            } else if (showAddTargetDialog) {
                showAddTargetDialog = false
            } else if (dhikrToDelete != null) {
                dhikrToDelete = null
            } else if (targetToDelete != null) {
                targetToDelete = null
            } else if (showRestoreDefaultDialog) {
                showRestoreDefaultDialog = false
            } else if (showFeedbackSettings) {
                showFeedbackSettings = false
            } else if (activeEditDhikrId != null) {
                activeEditDhikrId = null
            }
        }
    )

    // User Feedback Preferences
    val globalVibrationEnabled = LocalVibrationEnabled.current
    var isVibrationEnabled by remember(globalVibrationEnabled) { mutableStateOf(globalVibrationEnabled) }
    val currentSelectedSound by rememberUpdatedState(selectedTasbeehSound)
    val isSoundEnabled = currentSelectedSound != TasbeehSound.OFF
    var isAutoCountEnabled by rememberSaveable { mutableStateOf(false) }
    var autoCountSpeedSec by rememberSaveable { mutableFloatStateOf(2.0f) }

    val tasbeehAudioPlayer = remember(context) { com.example.data.audio.TasbeehAudioPlayer.getInstance(context) }

    fun playSound() {
        tasbeehAudioPlayer.playSound(currentSelectedSound, 1.0f)
    }

    fun triggerVibration(isCompletion: Boolean) {
        if (!isVibrationEnabled || !globalVibrationEnabled) return
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (isCompletion) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // TARGET COMPLETION: Strong continuous vibration for ~1 second (1000ms)
                        val effect = VibrationEffect.createOneShot(1000L, VibrationEffect.DEFAULT_AMPLITUDE)
                        vibrator.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(1000L)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Light single tap micro-haptic
                        val effect = VibrationEffect.createOneShot(16L, 90)
                        vibrator.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(16L)
                    }
                }
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (_: Exception) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    // Micro-interaction & Completion Animations
    var isCounterPressed by remember { mutableStateOf(false) }
    var isCompletionPulseActive by remember { mutableStateOf(false) }

    // Milestone pulse & haptic feedback for multiples of 33 (33, 66, 99, 132...)
    var previousCount by remember { mutableIntStateOf(dhikrCount) }
    val milestonePulseAnimatable = remember { Animatable(0f) }

    LaunchedEffect(dhikrCount) {
        val prev = previousCount
        previousCount = dhikrCount

        // Trigger milestone effect when entering a multiple of 33 (33, 66, 99, 132...) from a lower count
        if (dhikrCount > 0 && dhikrCount % 33 == 0 && dhikrCount > prev) {
            // 1. Subtle, quiet tactile tick
            if (isVibrationEnabled && globalVibrationEnabled) {
                try {
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val effect = VibrationEffect.createOneShot(24L, 140)
                            vibrator.vibrate(effect)
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(24L)
                        }
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                } catch (_: Exception) {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                }
            }

            // 2. Soft, restrained accent pulse animation (normal -> soft peak -> smooth fade back)
            coroutineScope.launch {
                milestonePulseAnimatable.snapTo(0f)
                milestonePulseAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
                milestonePulseAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val resetAnimatable = remember { Animatable(0f) }

    // Target completion pulse coroutine
    fun triggerCompletionPulse() {
        coroutineScope.launch {
            isCompletionPulseActive = true
            delay(400)
            isCompletionPulseActive = false
        }
    }

    // Auto-count coroutine timer
    LaunchedEffect(isAutoCountEnabled, autoCountSpeedSec, dhikrCount, dhikrTarget) {
        if (isAutoCountEnabled && dhikrCount < dhikrTarget) {
            val delayMs = (autoCountSpeedSec * 1000).toLong().coerceAtLeast(300L)
            delay(delayMs)

            val nextCount = dhikrCount + 1
            val isTargetReached = (nextCount == dhikrTarget && dhikrTarget > 0)

            triggerVibration(isCompletion = isTargetReached)
            playSound()
            if (isTargetReached) {
                triggerCompletionPulse()
            }
            onIncrement()
        } else if (dhikrCount >= dhikrTarget && isAutoCountEnabled) {
            isAutoCountEnabled = false
        }
    }

    val triggerIncrement = {
        val nextCount = dhikrCount + 1
        val isTargetReached = (nextCount == dhikrTarget && dhikrTarget > 0)

        // Micro-interaction press animation
        coroutineScope.launch {
            isCounterPressed = true
            delay(90)
            isCounterPressed = false
        }

        triggerVibration(isCompletion = isTargetReached)
        playSound()
        if (isTargetReached) {
            triggerCompletionPulse()
        }
        onIncrement()
    }

    val triggerDecrement = {
        if (dhikrCount > 0) {
            // Micro-interaction press animation on counter disc
            coroutineScope.launch {
                isCounterPressed = true
                delay(90)
                isCounterPressed = false
            }
            triggerVibration(isCompletion = false)
            playSound()
            onDecrement()
        }
    }

    val triggerReset = {
        isAutoCountEnabled = false
        coroutineScope.launch {
            resetAnimatable.snapTo(0f)
            resetAnimatable.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        }
        triggerVibration(isCompletion = false)
        onReset()
    }

    // Physical Volume Button Controls (Volume Up = Increment, Volume Down = Decrement)
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentIncrement by rememberUpdatedState(triggerIncrement)
    val currentDecrement by rememberUpdatedState(triggerDecrement)

    val volumeButtonController = remember(coroutineScope) {
        object : VolumeKeyEventListener {
            private var activeKeyCode: Int? = null
            private var repeatJob: Job? = null

            fun cancel() {
                repeatJob?.cancel()
                repeatJob = null
                activeKeyCode = null
            }

            override fun onVolumeKeyDown(keyCode: Int): Boolean {
                if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
                    return false
                }

                // If this volume key is already held down, consume OS auto-repeats without double-counting
                if (activeKeyCode == keyCode) {
                    return true
                }

                cancel()
                activeKeyCode = keyCode

                // Execute immediate count for the initial press
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    currentIncrement()
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    currentDecrement()
                }

                // Smooth controlled repeat on long press:
                // 500ms initial threshold prevents accidental rapid repeats on standard clicks.
                // 250ms interval ensures smooth, controlled 4-counts-per-second cadence.
                repeatJob = coroutineScope.launch {
                    delay(500L)
                    while (isActive) {
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            currentIncrement()
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            currentDecrement()
                        }
                        delay(250L)
                    }
                }
                return true
            }

            override fun onVolumeKeyUp(keyCode: Int): Boolean {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    if (activeKeyCode == keyCode) {
                        cancel()
                    }
                    return true
                }
                return false
            }
        }
    }

    val volumeDispatcher = LocalVolumeKeyDispatcher.current
        ?: (context.findActivity() as? VolumeKeyDispatcher)

    // Volume interception is active ONLY when:
    // 1. Dhikr screen is visible and active (isActiveTab == true)
    // 2. User preference "Dhikr Volume Button Controls" is ON (dhikrVolumeControlsEnabled == true)
    // 3. No text entry dialogs are active (e.g. adding custom dhikr or custom target)
    val isVolumeInterceptionActive = isActiveTab && dhikrVolumeControlsEnabled && !showCustomDhikrDialog && !showAddTargetDialog

    DisposableEffect(isVolumeInterceptionActive, lifecycleOwner, volumeDispatcher) {
        if (!isVolumeInterceptionActive || volumeDispatcher == null) {
            volumeButtonController.cancel()
            volumeDispatcher?.setVolumeKeyEventListener(null)
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        volumeDispatcher.setVolumeKeyEventListener(volumeButtonController)
                    }
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> {
                        volumeButtonController.cancel()
                        volumeDispatcher.setVolumeKeyEventListener(null)
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                volumeDispatcher.setVolumeKeyEventListener(volumeButtonController)
            }

            onDispose {
                volumeButtonController.cancel()
                volumeDispatcher.setVolumeKeyEventListener(null)
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    // Progress Fraction Calculation Clamped [0, 1]
    val safeTarget = dhikrTarget.coerceAtLeast(1)
    val progressFraction = (dhikrCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "progressRing"
    )

    // Animated Scale for Counter Disc
    val discScale by animateFloatAsState(
        targetValue = if (isCompletionPulseActive) 1.03f else if (isCounterPressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 650f),
        label = "discScale"
    )

    val scrollState = rememberScrollState()
    val targetScrollState = rememberScrollState()

    // Position Tracking for Single Shared Sliding Target Indicator
    val targetBoundsMap = remember { mutableStateMapOf<Int, Pair<Float, Float>>() } // target -> Pair(leftPx, widthPx)

    val activeTargetBounds = targetBoundsMap[dhikrTarget]
    val rawTargetX = activeTargetBounds?.first ?: 0f
    val rawTargetW = activeTargetBounds?.second ?: 0f

    val animatedTargetX by animateFloatAsState(
        targetValue = rawTargetX,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
        label = "targetIndicatorX"
    )

    val animatedTargetW by animateFloatAsState(
        targetValue = if (rawTargetW > 0f) rawTargetW else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
        label = "targetIndicatorW"
    )

    // Auto-scroll target selector when target changes / added
    LaunchedEffect(dhikrTarget, activeTargetBounds) {
        if (activeTargetBounds != null) {
            val itemStart = activeTargetBounds.first.toInt()
            val itemEnd = (activeTargetBounds.first + activeTargetBounds.second).toInt()
            val currentScroll = targetScrollState.value
            val viewportWidth = targetScrollState.viewportSize

            if (viewportWidth > 0) {
                if (itemStart < currentScroll) {
                    targetScrollState.animateScrollTo((itemStart - 24).coerceAtLeast(0))
                } else if (itemEnd > (currentScroll + viewportWidth)) {
                    targetScrollState.animateScrollTo(itemEnd - viewportWidth + 24)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (activeEditDhikrId != null) {
                    activeEditDhikrId = null
                }
            }
    ) {
        // SECTION 1: Unified Top Header & Action Buttons
        PageHeader(
            title = "Tasbeeh",
            subtitle = "Digital Dhikr Counter",
            titleColor = Color.semanticPrimaryText,
            subtitleColor = Color.semanticMutedText,
            actions = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Settings Button
                    Surface(
                        onClick = { showFeedbackSettings = !showFeedbackSettings },
                        shape = CircleShape,
                        color = Color.semanticSurface,
                        border = BorderStroke(1.dp, if (showFeedbackSettings) (Color.semanticPrimaryAccent) else Color.semanticBorder),
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("dhikr_settings_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = if (showFeedbackSettings) (Color.semanticPrimaryAccent) else Color.semanticMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // History Button
                    Surface(
                        onClick = { showHistorySheet = true },
                        shape = CircleShape,
                        color = Color.semanticSurface,
                        border = BorderStroke(1.dp, Color.semanticBorder),
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("dhikr_history_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "History",
                                tint = Color.semanticMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Feedback & Auto Count Expandable Settings Panel
        AnimatedVisibility(visible = showFeedbackSettings) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.semanticSurface
                ),
                border = BorderStroke(1.dp, Color.semanticBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Counter Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.semanticPrimaryAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Vibration, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.semanticPrimaryAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vibration Feedback", style = MaterialTheme.typography.bodyMedium, color = Color.semanticPrimaryText)
                        }
                        Switch(
                            checked = isVibrationEnabled,
                            onCheckedChange = {
                                isVibrationEnabled = it
                                onToggleVibration(it)
                            },
                            modifier = Modifier.testTag("vibration_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.semanticAccentForeground,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color.semanticSecondaryText,
                                uncheckedTrackColor = Color.semanticControl,
                                uncheckedBorderColor = Color.semanticBorder
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.semanticPrimaryAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tap Sound", style = MaterialTheme.typography.bodyMedium, color = Color.semanticPrimaryText)
                        }
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { isChecked ->
                                if (!isChecked) {
                                    onSelectTasbeehSound(TasbeehSound.OFF)
                                } else if (selectedTasbeehSound == TasbeehSound.OFF) {
                                    onSelectTasbeehSound(TasbeehSound.SOFT_TICK)
                                }
                            },
                            modifier = Modifier.testTag("sound_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.semanticAccentForeground,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color.semanticSecondaryText,
                                uncheckedTrackColor = Color.semanticControl,
                                uncheckedBorderColor = Color.semanticBorder
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.semanticPrimaryAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Dhikr Volume Button Controls",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.semanticPrimaryText
                                )
                                Text(
                                    "Use volume up/down keys to count Dhikr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.semanticMutedText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = dhikrVolumeControlsEnabled,
                            onCheckedChange = onToggleVolumeControls,
                            modifier = Modifier.testTag("dhikr_volume_controls_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.semanticAccentForeground,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color.semanticSecondaryText,
                                uncheckedTrackColor = Color.semanticControl,
                                uncheckedBorderColor = Color.semanticBorder
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.semanticBorder
                    )

                    // Restore Default Dhikrs Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showRestoreDefaultDialog = true
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.semanticPrimaryAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Restore Default Dhikr",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.semanticPrimaryText
                                )
                                Text(
                                    "Reset Dhikr chips and order to defaults",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.semanticMutedText
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.semanticMutedText
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 2 & 3: DHIKR SELECTOR + STANDALONE DHIKR "+" BUTTON
        // Displaced reordering layout with spring animation & continuous spatial tracking
        // =========================================================================
        ReorderableDhikrChipsRow(
            presets = presets,
            selectedPreset = selectedPreset,
            activeEditDhikrId = activeEditDhikrId,
            onSelectPreset = { preset ->
                onSelectPreset(preset)
                isAutoCountEnabled = false
                if (activeEditDhikrId != null && activeEditDhikrId != preset.id) {
                    activeEditDhikrId = null
                }
            },
            onReorderPresets = onReorderPresets,
            onDeleteDhikr = onDeleteDhikr,
            onEditActiveDhikrChange = { activeEditDhikrId = it },
            onAddCustomDhikrClick = {
                editingCustomDhikr = null
                showCustomDhikrDialog = true
            },
            isDarkTheme = isDarkTheme,
            isVibrationEnabled = isVibrationEnabled,
            globalVibrationEnabled = globalVibrationEnabled
        )

        // SECTION 4: Reserved Stable Content Container (Arabic + Meaning)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = selectedPreset,
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 5 })
                        .togetherWith(fadeOut(tween(130)) + slideOutVertically(tween(130)) { -it / 5 })
                },
                label = "dhikrContentTransition"
            ) { targetPreset ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (targetPreset.nameArabic.isNotEmpty()) {
                        ArabicText(
                            text = targetPreset.nameArabic,
                            fontSize = 28.sp,
                            color = if (isDarkTheme) Color(0xFFD4D4CC) else Color.semanticPrimaryAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        if (targetPreset.translation.isNotEmpty()) {
                            Text(
                                text = targetPreset.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkTheme) Color(0xFFB0B0AA) else Color.semanticMutedText,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }

                        if (targetPreset.isCustom) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    editingCustomDhikr = targetPreset
                                    showCustomDhikrDialog = true
                                },
                                modifier = Modifier.size(22.dp).testTag("edit_custom_dhikr_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Custom Dhikr",
                                    tint = Color.semanticPrimaryAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 5: DOMINANT MAIN COUNTER DISC
        // Large central counter with progress ring, haptics, and micro-interactions
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val ringTrackColor = if (isDarkTheme) Color.semanticSurface else LightBorder.copy(alpha = 0.35f)
            val ringProgressColor = Color.semanticPrimaryAccent
            val isTargetReached = (dhikrCount >= dhikrTarget && dhikrTarget > 0)
            val milestonePulse = milestonePulseAnimatable.value

            // Soft outer accent halo when milestone is reached
            if (milestonePulse > 0f) {
                Box(
                    modifier = Modifier
                        .size(284.dp)
                        .graphicsLayer {
                            alpha = milestonePulse
                            scaleX = 0.96f + (0.05f * milestonePulse)
                            scaleY = 0.96f + (0.05f * milestonePulse)
                        }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ringProgressColor.copy(alpha = 0.22f * milestonePulse),
                                    ringProgressColor.copy(alpha = 0.07f * milestonePulse),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(268.dp)
                    .graphicsLayer {
                        scaleX = discScale
                        scaleY = discScale
                    }
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerIncrement()
                    }
                    .testTag("tasbeeh_tap_area"),
                contentAlignment = Alignment.Center
            ) {
                // Background Track & Subtle Progress Ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeftOffset = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                    // 1. Background full track ring
                    drawArc(
                        color = ringTrackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 2. Smooth animated progress arc
                    if (animatedProgress > 0f) {
                        drawArc(
                            color = ringProgressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // 3. Milestone accent ring pulse
                    if (milestonePulse > 0f) {
                        drawArc(
                            color = ringProgressColor.copy(alpha = 0.35f * milestonePulse),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth + (4.dp.toPx() * milestonePulse),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                // Inner Main Disc Surface
                Surface(
                    modifier = Modifier.size(244.dp),
                    shape = CircleShape,
                    color = Color.semanticSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = if (isDarkTheme) 2.dp else 4.dp,
                    border = BorderStroke(
                        1.dp + (0.5.dp * milestonePulse),
                        if (isTargetReached) ringProgressColor.copy(alpha = 0.8f)
                        else if (milestonePulse > 0f) {
                            ringProgressColor.copy(alpha = 0.4f * milestonePulse + 0.2f)
                        } else Color.semanticBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = dhikrCount,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInVertically(animationSpec = tween(120)) { it / 4 } +
                                            fadeIn(animationSpec = tween(120)) +
                                            scaleIn(initialScale = 0.94f, animationSpec = tween(120)))
                                        .togetherWith(
                                            slideOutVertically(animationSpec = tween(100)) { -it / 4 } +
                                                    fadeOut(animationSpec = tween(100)) +
                                                    scaleOut(targetScale = 1.04f, animationSpec = tween(100))
                                        )
                                } else {
                                    (slideInVertically(animationSpec = tween(120)) { -it / 4 } +
                                            fadeIn(animationSpec = tween(120)) +
                                            scaleIn(initialScale = 1.04f, animationSpec = tween(120)))
                                        .togetherWith(
                                            slideOutVertically(animationSpec = tween(100)) { it / 4 } +
                                                    fadeOut(animationSpec = tween(100)) +
                                                    scaleOut(targetScale = 0.94f, animationSpec = tween(100))
                                        )
                                }
                            },
                            label = "tasbeehCountTransition"
                        ) { count ->
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = SerifHeaderFont,
                                    fontSize = 76.sp
                                ),
                                color = Color.semanticPrimaryText,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "$dhikrCount / $dhikrTarget",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isTargetReached) {
                                Color.semanticSuccess
                            } else {
                                if (isDarkTheme) Color(0xFFB0B0AA) else Color.semanticPrimaryAccent
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isAutoCountEnabled) "Counting automatically" else "Tap anywhere to count",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) Color(0xFF9E9E98) else Color.semanticMutedText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 6, 7, 8, 13, 14, 15: TARGET SELECTOR WITH ONE SHARED SLIDING INDICATOR
        // Independent segmented control: [ 33 ] [ 99 ] [ 100 ] [ 500 ] [ + ]
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.semanticControl,
            border = BorderStroke(1.dp, Color.semanticBorder),
            modifier = Modifier
                .height(44.dp)
        ) {
            Box(
                modifier = Modifier
                    .horizontalScroll(targetScrollState)
                    .padding(3.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // ONE Shared Animated Sliding Indicator / Pill
                if (animatedTargetW > 0f) {
                    val indicatorWidthDp = with(density) { animatedTargetW.toDp() }
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = animatedTargetX
                            }
                            .width(indicatorWidthDp)
                            .height(38.dp)
                            .background(
                                color = Color.semanticPrimaryAccent,
                                shape = RoundedCornerShape(11.dp)
                            )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    targets.forEach { targetVal ->
                        val isTargetSelected = dhikrTarget == targetVal
                        val isBuiltInTarget = listOf(33, 99, 100).contains(targetVal)
                        val targetTextColor by animateColorAsState(
                            targetValue = if (isTargetSelected) {
                                if (isDarkTheme) Color(0xFFFFFFFF) else Color.semanticAccentForeground
                            } else {
                                Color.semanticSecondaryText
                            },
                            animationSpec = tween(180),
                            label = "targetTextColor"
                        )

                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    val bounds = coordinates.boundsInParent()
                                    targetBoundsMap[targetVal] = Pair(bounds.left, bounds.width)
                                }
                                .clip(RoundedCornerShape(11.dp))
                                .pointerInput(targetVal) {
                                    detectTapGestures(
                                        onTap = {
                                            onSetTarget(targetVal)
                                            isAutoCountEnabled = false
                                        },
                                        onLongPress = {
                                            if (!isBuiltInTarget) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                targetToDelete = targetVal
                                            }
                                        }
                                    )
                                }
                                .semantics {
                                    contentDescription = if (!isBuiltInTarget) {
                                        "$targetVal repetitions, long press to delete"
                                    } else {
                                        "$targetVal repetitions"
                                    }
                                    if (isTargetSelected) {
                                        stateDescription = "selected"
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                                .testTag("target_chip_$targetVal"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$targetVal",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.Medium,
                                color = targetTextColor
                            )
                        }
                    }

                    // Target "+" Button: Standalone Add Target Count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showAddTargetDialog = true
                            }
                            .semantics {
                                contentDescription = "Add Target Count"
                            }
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                            .testTag("add_target_count_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Target Count",
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 9: ACTION CONTROLS [ − / Auto / ↻ ]
        // Decrement, Auto Counter & Reset Controls sharing uniform height and centerline
        // =========================================================================
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            // Decrement / Undo Button
            Surface(
                onClick = {
                    isAutoCountEnabled = false
                    triggerDecrement()
                },
                enabled = dhikrCount > 0,
                shape = CircleShape,
                color = Color.semanticSurface,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier
                    .size(46.dp)
                    .testTag("decrement_dhikr_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = "Decrement / Undo",
                        tint = if (dhikrCount > 0) Color.semanticPrimaryText else Color.semanticMutedText.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Compact Auto Counter Button
            Surface(
                onClick = { isAutoCountEnabled = !isAutoCountEnabled },
                shape = CircleShape,
                color = if (isAutoCountEnabled) Color.semanticPrimaryAccent.copy(alpha = 0.18f) else Color.semanticSurface,
                border = BorderStroke(
                    1.dp,
                    if (isAutoCountEnabled) Color.semanticPrimaryAccent else Color.semanticBorder
                ),
                modifier = Modifier
                    .size(46.dp)
                    .testTag("autocount_switch")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isAutoCountEnabled) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (isAutoCountEnabled) "Auto Counter on" else "Auto Counter off",
                        tint = if (isAutoCountEnabled) Color.semanticPrimaryAccent else Color.semanticPrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Reset Button with subtle rotation feedback
            Surface(
                onClick = triggerReset,
                shape = CircleShape,
                color = Color.semanticSurface,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier
                    .size(46.dp)
                    .testTag("reset_dhikr_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Reset",
                        tint = Color.semanticPrimaryText,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = resetAnimatable.value
                            }
                    )
                }
            }
        }

        // Compact Pace Control (Revealed unobtrusively when Auto Counter is ON)
        AnimatedVisibility(
            visible = isAutoCountEnabled,
            enter = expandVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(250)),
            exit = shrinkVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pace",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticMutedText
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", autoCountSpeedSec)} sec",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.semanticPrimaryAccent
                    )
                }
                Slider(
                    value = autoCountSpeedSec,
                    onValueChange = { autoCountSpeedSec = it },
                    valueRange = 0.5f..5.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("autocount_speed_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.semanticPrimaryAccent,
                        activeTrackColor = Color.semanticPrimaryAccent,
                        inactiveTrackColor = Color.semanticControl
                    )
                )
            }
        }

        // Comfortable breathing space above bottom dock
        Spacer(modifier = Modifier.height(130.dp))
        }
    }

    // SECTION 10: ModalBottomSheet for Dhikr History
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = historySheetState,
            containerColor = Color.semanticSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completed Dhikr Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = SerifHeaderFont,
                        color = Color.semanticPrimaryAccent
                    )
                    if (dhikrHistory.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.semanticBorder,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${dhikrHistory.size} total",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.semanticPrimaryText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dhikrHistory.isEmpty()) {
                    QuietEmptyState(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No completed sessions logged yet.\nComplete a dhikr round to see history.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.semanticMutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        dhikrHistory.take(8).forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.semanticSurface,
                                border = BorderStroke(1.dp, Color.semanticBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.dhikrName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.semanticPrimaryText
                                        )
                                        if (item.arabicText.isNotEmpty()) {
                                            Text(
                                                text = item.arabicText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.semanticPrimaryAccent
                                            )
                                        }
                                        val dateFormatted = try {
                                            val sdf = java.text.SimpleDateFormat("d MMM yyyy, h:mm a", java.util.Locale.ENGLISH)
                                            sdf.format(java.util.Date(item.timestamp))
                                        } catch (_: Exception) { "" }
                                        if (dateFormatted.isNotEmpty()) {
                                            Text(
                                                text = dateFormatted,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.semanticMutedText
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.semanticBorder
                                    ) {
                                        Text(
                                            text = "${item.countCompleted}/${item.target}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.semanticPrimaryText,
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
    }

    // =========================================================================
    // SECTION 11: ADD / EDIT CUSTOM DHIKR DIALOG
    // Contains ONLY Dhikr fields (Transliteration, Arabic, Meaning, Target)
    // =========================================================================
    if (showCustomDhikrDialog) {
        val isEditing = editingCustomDhikr != null
        var transliterationInput by remember { mutableStateOf(editingCustomDhikr?.nameEnglish ?: "") }
        var arabicInput by remember { mutableStateOf(editingCustomDhikr?.nameArabic ?: "") }
        var meaningInput by remember { mutableStateOf(editingCustomDhikr?.translation ?: "") }
        var targetInput by remember { mutableStateOf((editingCustomDhikr?.defaultTarget ?: dhikrTarget).toString()) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCustomDhikrDialog = false },
            title = {
                Text(
                    text = if (isEditing) "Edit Custom Dhikr" else "Add Custom Dhikr",
                    fontFamily = SerifHeaderFont
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = transliterationInput,
                        onValueChange = {
                            transliterationInput = it
                            errorMsg = null
                        },
                        label = { Text("Transliteration / Name *") },
                        placeholder = { Text("e.g. Astaghfirullah") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_name_input")
                    )

                    OutlinedTextField(
                        value = arabicInput,
                        onValueChange = { arabicInput = it },
                        label = { Text("Arabic Text (Optional)") },
                        placeholder = { Text("e.g. أَسْتَغْفِرُ اللَّهَ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_arabic_input")
                    )

                    OutlinedTextField(
                        value = meaningInput,
                        onValueChange = { meaningInput = it },
                        label = { Text("Meaning / Translation (Optional)") },
                        placeholder = { Text("e.g. I seek forgiveness from Allah") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_meaning_input")
                    )

                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = { Text("Target Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_target_input")
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (transliterationInput.isBlank()) {
                            errorMsg = "Please enter a name for the dhikr"
                            return@TextButton
                        }
                        val parsedTarget = targetInput.toIntOrNull()?.coerceAtLeast(1) ?: dhikrTarget

                        if (isEditing && editingCustomDhikr != null) {
                            val updated = editingCustomDhikr!!.copy(
                                nameEnglish = transliterationInput.trim(),
                                nameArabic = arabicInput.trim(),
                                translation = meaningInput.trim(),
                                defaultTarget = parsedTarget
                            )
                            onUpdateCustomDhikr(updated)
                        } else {
                            onAddCustomDhikr(
                                transliterationInput.trim(),
                                arabicInput.trim(),
                                meaningInput.trim(),
                                parsedTarget
                            )
                        }
                        showCustomDhikrDialog = false
                    },
                    modifier = Modifier.testTag("save_custom_dhikr_btn")
                ) {
                    Text(if (isEditing) "Save" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDhikrDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // SECTION 12: ADD TARGET COUNT DIALOG
    // Contains ONLY Numeric Target field (e.g. 500, 1000)
    // =========================================================================
    if (showAddTargetDialog) {
        var newTargetInput by remember { mutableStateOf("") }
        var targetErrorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddTargetDialog = false },
            title = {
                Text(
                    text = "Add Target Count",
                    fontFamily = SerifHeaderFont
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter a custom repetition target for your Dhikr sessions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newTargetInput,
                        onValueChange = {
                            newTargetInput = it
                            targetErrorMsg = null
                        },
                        label = { Text("Target Repetitions *") },
                        placeholder = { Text("e.g. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_target_count_input")
                    )

                    if (targetErrorMsg != null) {
                        Text(
                            text = targetErrorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = newTargetInput.trim().toIntOrNull()
                        if (parsed == null || parsed <= 0) {
                            targetErrorMsg = "Please enter a valid positive number"
                            return@TextButton
                        }
                        onAddCustomTarget(parsed)
                        onSetTarget(parsed)
                        showAddTargetDialog = false
                    },
                    modifier = Modifier.testTag("save_target_count_btn")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // SECTION 13: DELETE CONFIRMATION DIALOG FOR CUSTOM DHIKR
    // =========================================================================
    if (dhikrToDelete != null) {
        val preset = dhikrToDelete!!
        AlertDialog(
            onDismissRequest = { dhikrToDelete = null },
            title = {
                Text(
                    text = "Delete Custom Dhikr?",
                    fontFamily = SerifHeaderFont
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${preset.nameEnglish}\"?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCustomDhikr(preset.id)
                        dhikrToDelete = null
                    },
                    modifier = Modifier
                        .testTag("confirm_delete_dhikr_btn")
                        .semantics { contentDescription = "Delete custom Dhikr ${preset.nameEnglish}" }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dhikrToDelete = null },
                    modifier = Modifier.semantics { contentDescription = "Cancel" }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // SECTION 14: DELETE CONFIRMATION DIALOG FOR CUSTOM TARGET COUNT
    // =========================================================================
    if (targetToDelete != null) {
        val targetVal = targetToDelete!!
        AlertDialog(
            onDismissRequest = { targetToDelete = null },
            title = {
                Text(
                    text = "Delete Target Count?",
                    fontFamily = SerifHeaderFont
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete target count $targetVal?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        targetBoundsMap.remove(targetVal)
                        onDeleteCustomTarget(targetVal)
                        targetToDelete = null
                    },
                    modifier = Modifier
                        .testTag("confirm_delete_target_btn")
                        .semantics { contentDescription = "Delete custom target $targetVal" }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { targetToDelete = null },
                    modifier = Modifier.semantics { contentDescription = "Cancel" }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // SECTION 15: RESTORE DEFAULT DHIKRS DIALOG
    // =========================================================================
    if (showRestoreDefaultDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDefaultDialog = false },
            title = {
                Text(
                    text = "Restore Default Dhikrs?",
                    fontFamily = SerifHeaderFont
                )
            },
            text = {
                Text(
                    text = "This will restore the original Dhikr presets and reset their order. Your Dhikr count history will not be affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreDefaultDhikrs()
                        showRestoreDefaultDialog = false
                    },
                    modifier = Modifier
                        .testTag("confirm_restore_defaults_btn")
                        .semantics { contentDescription = "Restore default Dhikr presets" }
                ) {
                    Text("Restore", color = Color.semanticPrimaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreDefaultDialog = false },
                    modifier = Modifier.semantics { contentDescription = "Cancel" }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private data class DhikrChipBounds(val x: Float, val width: Float)

@Composable
private fun ReorderableDhikrChipsRow(
    presets: List<DhikrPreset>,
    selectedPreset: DhikrPreset,
    activeEditDhikrId: String?,
    onSelectPreset: (DhikrPreset) -> Unit,
    onReorderPresets: (List<DhikrPreset>) -> Unit,
    onDeleteDhikr: (String) -> Unit,
    onEditActiveDhikrChange: (String?) -> Unit,
    onAddCustomDhikrClick: () -> Unit,
    isDarkTheme: Boolean,
    isVibrationEnabled: Boolean,
    globalVibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var localList by remember(presets) { mutableStateOf(presets) }
    var draggingPresetId by remember { mutableStateOf<String?>(null) }
    var initialIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    val dragOffset = remember { Animatable(0f) }
    var isSettling by remember { mutableStateOf(false) }

    var currentPointerScreenX by remember { mutableFloatStateOf(0f) }
    var pointerGrabOffset by remember { mutableFloatStateOf(0f) }
    var currentInitialSlotX by remember { mutableFloatStateOf(0f) }

    val safeInsetPx = with(density) { 12.dp.toPx() }
    val hysteresisPx = with(density) { 8.dp.toPx() }
    var leftBoundaryHit by remember { mutableStateOf(false) }
    var rightBoundaryHit by remember { mutableStateOf(false) }

    LaunchedEffect(presets) {
        if (draggingPresetId == null && !isSettling) {
            localList = presets
        }
    }

    LaunchedEffect(draggingPresetId) {
        if (draggingPresetId == null) {
            leftBoundaryHit = false
            rightBoundaryHit = false
        }
    }

    val itemBounds = remember { mutableStateMapOf<String, DhikrChipBounds>() }
    val itemShiftAnims = remember { mutableStateMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val spacingPx = with(density) { 8.dp.toPx() }

    // Edge auto-scroll during active drag
    LaunchedEffect(draggingPresetId, isSettling) {
        val currentDragId = draggingPresetId
        if (currentDragId == null || isSettling) return@LaunchedEffect
        val edgeThresholdPx = with(density) { 60.dp.toPx() }
        val maxSpeedPx = with(density) { 14.dp.toPx() }
        val minSpeedPx = with(density) { 2.dp.toPx() }

        while (isActive && draggingPresetId == currentDragId && !isSettling) {
            val viewportWidth = scrollState.viewportSize.toFloat()
            if (viewportWidth > 0f) {
                val pX = currentPointerScreenX
                var scrollDelta = 0f

                if (pX < edgeThresholdPx && scrollState.value > 0) {
                    val proximity = ((edgeThresholdPx - pX) / edgeThresholdPx).coerceIn(0f, 1f)
                    val speed = minSpeedPx + (maxSpeedPx - minSpeedPx) * proximity
                    scrollDelta = -minOf(speed, scrollState.value.toFloat())
                } else if (pX > viewportWidth - edgeThresholdPx && scrollState.value < scrollState.maxValue) {
                    val proximity = ((pX - (viewportWidth - edgeThresholdPx)) / edgeThresholdPx).coerceIn(0f, 1f)
                    val speed = minSpeedPx + (maxSpeedPx - minSpeedPx) * proximity
                    val maxCanScroll = (scrollState.maxValue - scrollState.value).toFloat()
                    scrollDelta = minOf(speed, maxCanScroll)
                }

                if (scrollDelta != 0f) {
                    scrollState.scrollBy(scrollDelta)
                    val draggedW = itemBounds[currentDragId]?.width ?: 0f
                    val scaleExpansion = (draggedW * 0.03f / 2f).coerceAtLeast(0f)
                    val rawScreenLeft = (currentPointerScreenX - pointerGrabOffset)
                    val minScreenLeft = safeInsetPx + scaleExpansion
                    val maxScreenLeft = maxOf(minScreenLeft, viewportWidth - safeInsetPx - draggedW - scaleExpansion)
                    val clampedScreenLeft = rawScreenLeft.coerceIn(minScreenLeft, maxScreenLeft)
                    val targetOffset = clampedScreenLeft + scrollState.value - currentInitialSlotX
                    dragOffset.snapTo(targetOffset)

                    // Dynamically recalculate targetIndex as new content is scrolled into view
                    val initialSlotCenter = currentInitialSlotX + (draggedW / 2f)
                    val visualCenter = initialSlotCenter + targetOffset

                    fun getTargetSlotX(idx: Int): Float {
                        if (idx == initialIndex) return currentInitialSlotX
                        val targetItem = localList.getOrNull(idx) ?: return currentInitialSlotX
                        val targetBounds = itemBounds[targetItem.id] ?: return currentInitialSlotX
                        return if (idx > initialIndex) {
                            targetBounds.x + targetBounds.width - draggedW
                        } else {
                            targetBounds.x
                        }
                    }

                    fun getTargetSlotCenter(idx: Int): Float = getTargetSlotX(idx) + (draggedW / 2f)

                    val n = localList.size
                    var newTarget = initialIndex
                    for (k in 0 until n) {
                        val centerK = getTargetSlotCenter(k)
                        if (k == 0 && visualCenter <= centerK) {
                            newTarget = 0
                            break
                        }
                        if (k == n - 1 && visualCenter >= centerK) {
                            newTarget = n - 1
                            break
                        }
                        if (k < n - 1) {
                            val centerNext = getTargetSlotCenter(k + 1)
                            val mid = (centerK + centerNext) / 2f
                            if (visualCenter < mid) {
                                newTarget = k
                                break
                            }
                        }
                    }

                    if (newTarget != targetIndex && newTarget in 0 until n) {
                        targetIndex = newTarget
                        if (isVibrationEnabled && globalVibrationEnabled) {
                            FiveLightHaptics.performSoftTick(view, haptic, true)
                        }
                    }
                }
            }
            delay(16)
        }
    }

    fun finishDrag() {
        val fromIdx = initialIndex
        val toIdx = targetIndex
        val currentDragId = draggingPresetId
        if (currentDragId != null && fromIdx != -1 && toIdx != -1 && fromIdx != toIdx &&
            fromIdx in localList.indices && toIdx in localList.indices
        ) {
            isSettling = true
            val draggedW = itemBounds[currentDragId]?.width ?: 0f
            val initialSlotX = itemBounds[currentDragId]?.x ?: 0f
            val targetItem = localList[toIdx]
            val targetBounds = itemBounds[targetItem.id]

            val targetSlotX = if (targetBounds != null) {
                if (toIdx > fromIdx) {
                    targetBounds.x + targetBounds.width - draggedW
                } else {
                    targetBounds.x
                }
            } else {
                initialSlotX
            }
            val settleDelta = targetSlotX - initialSlotX

            coroutineScope.launch {
                dragOffset.animateTo(
                    targetValue = settleDelta,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
                )
                val updated = localList.toMutableList()
                val moved = updated.removeAt(fromIdx)
                updated.add(toIdx, moved)
                localList = updated
                onReorderPresets(updated)

                // Simultaneously snap all offsets to 0 alongside list reorder
                dragOffset.snapTo(0f)
                itemShiftAnims.values.forEach { anim ->
                    anim.snapTo(0f)
                }
                draggingPresetId = null
                initialIndex = -1
                targetIndex = -1
                isSettling = false
            }
        } else {
            isSettling = true
            coroutineScope.launch {
                dragOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
                )
                itemShiftAnims.values.forEach { anim ->
                    anim.snapTo(0f)
                }
                draggingPresetId = null
                initialIndex = -1
                targetIndex = -1
                isSettling = false
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState, enabled = draggingPresetId == null),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            localList.forEachIndexed { i, preset ->
                key(preset.id) {
                    val isThisDragging = draggingPresetId == preset.id
                    val isActiveEdit = activeEditDhikrId == preset.id
                    val isSelected = preset.id == selectedPreset.id
                    val canDelete = localList.size > 1

                    val shiftAnim = remember(preset.id) { Animatable(0f) }
                    DisposableEffect(preset.id) {
                        itemShiftAnims[preset.id] = shiftAnim
                        onDispose {
                            itemShiftAnims.remove(preset.id)
                        }
                    }

                    val targetShiftX = if (draggingPresetId != null && !isThisDragging && initialIndex != -1 && targetIndex != -1) {
                        val draggedWidth = (itemBounds[draggingPresetId]?.width ?: 0f) + spacingPx
                        if (targetIndex > initialIndex && i > initialIndex && i <= targetIndex) {
                            -draggedWidth
                        } else if (targetIndex < initialIndex && i >= targetIndex && i < initialIndex) {
                            +draggedWidth
                        } else {
                            0f
                        }
                    } else {
                        0f
                    }

                    LaunchedEffect(targetShiftX, draggingPresetId) {
                        if (draggingPresetId != null) {
                            shiftAnim.animateTo(
                                targetValue = targetShiftX,
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
                            )
                        } else if (!isSettling) {
                            shiftAnim.snapTo(0f)
                        }
                    }

                    val itemScale by animateFloatAsState(
                        targetValue = if (isThisDragging) 1.03f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
                        label = "chipScale_${preset.id}"
                    )
                    val itemElevation by animateDpAsState(
                        targetValue = if (isThisDragging) 8.dp else 0.dp,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
                        label = "chipElevation_${preset.id}"
                    )
                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) {
                            Color.semanticPrimaryAccent
                        } else {
                            Color.semanticSurface
                        },
                        animationSpec = tween(180),
                        label = "pillBg_${preset.id}"
                    )
                    val pillText by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isDarkTheme) Color(0xFFFFFFFF) else Color.semanticAccentForeground
                        } else {
                            Color.semanticSecondaryText
                        },
                        animationSpec = tween(180),
                        label = "pillText_${preset.id}"
                    )
                    val pillBorder = if (isSelected) {
                        BorderStroke(1.2.dp, Color.semanticPrimaryAccent)
                    } else {
                        BorderStroke(1.dp, Color.semanticBorder)
                    }

                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInParent()
                                itemBounds[preset.id] = DhikrChipBounds(
                                    x = pos.x,
                                    width = coords.size.width.toFloat()
                                )
                            }
                            .pointerInput(preset.id) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val downTime = System.currentTimeMillis()
                                    val downPos = down.position
                                    val longPressTimeout = viewConfig.longPressTimeoutMillis
                                    var isLongPress = false
                                    var hasMovedBeyondSlop = false

                                    while (true) {
                                        val remaining = (longPressTimeout - (System.currentTimeMillis() - downTime)).coerceAtLeast(1L)
                                        val event = withTimeoutOrNull(remaining) {
                                            awaitPointerEvent(PointerEventPass.Main)
                                        }

                                        if (event == null) {
                                            isLongPress = true
                                            break
                                        }

                                        val change = event.changes.firstOrNull { it.id == down.id }
                                        if (change == null || !change.pressed) {
                                            if (!hasMovedBeyondSlop && !isSettling && draggingPresetId == null) {
                                                onSelectPreset(preset)
                                                if (activeEditDhikrId != null && activeEditDhikrId != preset.id) {
                                                    onEditActiveDhikrChange(null)
                                                }
                                            }
                                            return@awaitEachGesture
                                        }

                                        val dist = (change.position - downPos).getDistance()
                                        if (dist > viewConfig.touchSlop) {
                                            hasMovedBeyondSlop = true
                                            return@awaitEachGesture
                                        }
                                    }

                                    if (isLongPress && !isSettling) {
                                        onEditActiveDhikrChange(preset.id)
                                        val currIdx = localList.indexOfFirst { it.id == preset.id }
                                        if (currIdx != -1) {
                                            draggingPresetId = preset.id
                                            initialIndex = currIdx
                                            targetIndex = currIdx
                                            val initialSlotX = itemBounds[preset.id]?.x ?: 0f
                                            currentInitialSlotX = initialSlotX
                                            pointerGrabOffset = downPos.x
                                            val initialScreenX = initialSlotX - scrollState.value + downPos.x
                                            val viewportW = scrollState.viewportSize.toFloat()
                                            currentPointerScreenX = if (viewportW > 0f) initialScreenX.coerceIn(0f, viewportW) else initialScreenX
                                            coroutineScope.launch { dragOffset.snapTo(0f) }
                                            if (isVibrationEnabled && globalVibrationEnabled) {
                                                FiveLightHaptics.performStrongTap(view, haptic, true)
                                            }

                                            while (true) {
                                                val event = awaitPointerEvent(PointerEventPass.Main)
                                                val change = event.changes.firstOrNull { it.id == down.id }
                                                if (change == null || !change.pressed) {
                                                    finishDrag()
                                                    break
                                                }

                                                val dragDelta = change.positionChange()
                                                change.consume()
                                                val deltaX = dragDelta.x
                                                val currentViewportW = scrollState.viewportSize.toFloat()
                                                currentPointerScreenX = if (currentViewportW > 0f) {
                                                    (currentPointerScreenX + deltaX).coerceIn(0f, currentViewportW)
                                                } else {
                                                    currentPointerScreenX + deltaX
                                                }

                                                val draggedW = itemBounds[preset.id]?.width ?: 0f
                                                val scaleExpansion = (draggedW * 0.03f / 2f).coerceAtLeast(0f)
                                                val rawScreenLeft = (currentPointerScreenX - pointerGrabOffset)
                                                val minScreenLeft = safeInsetPx + scaleExpansion
                                                val maxScreenLeft = if (currentViewportW > 0f) {
                                                    maxOf(minScreenLeft, currentViewportW - safeInsetPx - draggedW - scaleExpansion)
                                                } else {
                                                    minScreenLeft + 1000f
                                                }
                                                val clampedScreenLeft = rawScreenLeft.coerceIn(minScreenLeft, maxScreenLeft)

                                                // Boundary detection & haptic
                                                val isAtLeftBoundary = rawScreenLeft <= minScreenLeft
                                                val isAtRightBoundary = if (currentViewportW > 0f) {
                                                    (rawScreenLeft + draggedW + scaleExpansion) >= (currentViewportW - safeInsetPx)
                                                } else {
                                                    false
                                                }

                                                if (isAtLeftBoundary) {
                                                    if (!leftBoundaryHit) {
                                                        leftBoundaryHit = true
                                                        if (isVibrationEnabled && globalVibrationEnabled) {
                                                            FiveLightHaptics.performLightTap(view, haptic, true)
                                                        }
                                                    }
                                                } else if (rawScreenLeft > minScreenLeft + hysteresisPx) {
                                                    leftBoundaryHit = false
                                                }

                                                if (isAtRightBoundary) {
                                                    if (!rightBoundaryHit) {
                                                        rightBoundaryHit = true
                                                        if (isVibrationEnabled && globalVibrationEnabled) {
                                                            FiveLightHaptics.performLightTap(view, haptic, true)
                                                        }
                                                    }
                                                } else if (rawScreenLeft < maxScreenLeft - hysteresisPx) {
                                                    rightBoundaryHit = false
                                                }

                                                val targetOffset = clampedScreenLeft + scrollState.value - currentInitialSlotX
                                                coroutineScope.launch {
                                                    dragOffset.snapTo(targetOffset)
                                                }

                                                val initialSlotCenter = initialSlotX + (draggedW / 2f)
                                                val visualCenter = initialSlotCenter + targetOffset

                                                fun getTargetSlotX(idx: Int): Float {
                                                    if (idx == currIdx) return initialSlotX
                                                    val targetItem = localList.getOrNull(idx) ?: return initialSlotX
                                                    val targetBounds = itemBounds[targetItem.id] ?: return initialSlotX
                                                    return if (idx > currIdx) {
                                                        targetBounds.x + targetBounds.width - draggedW
                                                    } else {
                                                        targetBounds.x
                                                    }
                                                }

                                                fun getTargetSlotCenter(idx: Int): Float = getTargetSlotX(idx) + (draggedW / 2f)

                                                val n = localList.size
                                                var newTarget = currIdx
                                                for (k in 0 until n) {
                                                    val centerK = getTargetSlotCenter(k)
                                                    if (k == 0 && visualCenter <= centerK) {
                                                        newTarget = 0
                                                        break
                                                    }
                                                    if (k == n - 1 && visualCenter >= centerK) {
                                                        newTarget = n - 1
                                                        break
                                                    }
                                                    if (k < n - 1) {
                                                        val centerNext = getTargetSlotCenter(k + 1)
                                                        val mid = (centerK + centerNext) / 2f
                                                        if (visualCenter < mid) {
                                                            newTarget = k
                                                            break
                                                        }
                                                    }
                                                }

                                                if (newTarget != targetIndex && newTarget in 0 until n) {
                                                    targetIndex = newTarget
                                                    if (isVibrationEnabled && globalVibrationEnabled) {
                                                        FiveLightHaptics.performSoftTick(view, haptic, true)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .zIndex(if (isThisDragging) 100f else 1f)
                            .graphicsLayer {
                                translationX = if (isThisDragging) dragOffset.value else shiftAnim.value
                                scaleX = itemScale
                                scaleY = itemScale
                                shadowElevation = itemElevation.toPx()
                                shape = RoundedCornerShape(16.dp)
                                clip = false
                            }
                    ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pillBg,
                        border = pillBorder,
                        modifier = Modifier
                            .testTag("preset_${preset.id}")
                            .semantics {
                                contentDescription = if (isActiveEdit) {
                                    "${preset.nameEnglish}, edit mode active"
                                } else {
                                    preset.nameEnglish
                                }
                                if (isSelected) stateDescription = "selected"
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                start = 14.dp,
                                end = if (isActiveEdit && canDelete) 6.dp else 14.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            Text(
                                text = preset.nameEnglish,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = pillText
                            )

                            AnimatedVisibility(
                                visible = isActiveEdit && canDelete,
                                enter = fadeIn(tween(180)) + expandHorizontally(tween(180), expandFrom = Alignment.Start),
                                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150), shrinkTowards = Alignment.Start)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    if (isDarkTheme) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.35f)
                                                } else {
                                                    if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
                                                }
                                            )
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = true, radius = 11.dp)
                                            ) {
                                                if (canDelete) {
                                                    if (isVibrationEnabled && globalVibrationEnabled) {
                                                        try {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        } catch (_: Exception) {}
                                                    }
                                                    onDeleteDhikr(preset.id)
                                                    if (activeEditDhikrId == preset.id) {
                                                        onEditActiveDhikrChange(null)
                                                    }
                                                }
                                            }
                                            .semantics { contentDescription = "Delete ${preset.nameEnglish}" }
                                            .testTag("delete_dhikr_${preset.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Delete ${preset.nameEnglish}",
                                            tint = if (isSelected) {
                                                if (isDarkTheme) Color.White else Color.semanticAccentForeground
                                            } else {
                                                Color.semanticPrimaryText
                                            },
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standalone "+" Icon Button to Add Custom Dhikr
        Surface(
            onClick = onAddCustomDhikrClick,
            shape = RoundedCornerShape(16.dp),
            color = Color.semanticSurface,
            border = BorderStroke(1.dp, Color.semanticBorder),
            modifier = Modifier
                .height(34.dp)
                .width(38.dp)
                .testTag("add_custom_dhikr_btn")
                .semantics {
                    contentDescription = "Add Custom Dhikr"
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Custom Dhikr",
                    tint = Color.semanticPrimaryAccent,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
}
