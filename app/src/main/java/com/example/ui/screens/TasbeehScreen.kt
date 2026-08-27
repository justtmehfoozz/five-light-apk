package com.example.ui.screens

import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.predictiveBackTransform
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
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
    onSetTarget: (Int) -> Unit,
    onAddCustomTarget: (Int) -> Unit = {},
    onDeleteCustomTarget: (Int) -> Unit = {},
    onIncrement: () -> Unit,
    onDecrement: () -> Unit = {},
    onReset: () -> Unit,
    onAddCustomDhikr: (transliteration: String, arabicText: String, meaning: String, target: Int) -> Unit = { _, _, _, _ -> },
    onUpdateCustomDhikr: (DhikrPreset) -> Unit = {},
    onDeleteCustomDhikr: (String) -> Unit = {},
    isActiveTab: Boolean = true,
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
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Separate Dialog States: Dhikr Dialog vs Target Dialog
    var showCustomDhikrDialog by remember { mutableStateOf(false) }
    var editingCustomDhikr by remember { mutableStateOf<DhikrPreset?>(null) }

    var showAddTargetDialog by remember { mutableStateOf(false) }

    // Deletion Dialog States
    var dhikrToDelete by remember { mutableStateOf<DhikrPreset?>(null) }
    var targetToDelete by remember { mutableStateOf<Int?>(null) }

    val tasbeehPredictiveState = rememberPredictiveBackState()
    val isTasbeehOverlayActive = showHistorySheet || showCustomDhikrDialog || showAddTargetDialog || dhikrToDelete != null || targetToDelete != null || showFeedbackSettings

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
            } else if (showFeedbackSettings) {
                showFeedbackSettings = false
            }
        }
    )

    // User Feedback Preferences
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isAutoCountEnabled by remember { mutableStateOf(false) }
    var autoCountSpeedSec by remember { mutableFloatStateOf(2.0f) }

    // SoundPool for Instantaneous, Zero-Latency Tap Audio Playback
    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    val soundIdMap = remember(soundPool) {
        val map = mutableMapOf<TasbeehSound, Int>()
        TasbeehSound.entries.forEach { sound ->
            sound.resId?.let { resId ->
                try {
                    val soundId = soundPool.load(context, resId, 1)
                    map[sound] = soundId
                } catch (_: Exception) {}
            }
        }
        map
    }

    DisposableEffect(soundPool) {
        onDispose {
            try {
                soundPool.release()
            } catch (_: Exception) {}
        }
    }

    fun playSound() {
        if (!isSoundEnabled || selectedTasbeehSound == TasbeehSound.OFF) return
        val soundId = soundIdMap[selectedTasbeehSound]
        if (soundId != null && soundId > 0) {
            try {
                soundPool.play(soundId, 0.9f, 0.9f, 1, 0, 1.0f)
            } catch (_: Exception) {}
        }
    }

    val globalVibrationEnabled = LocalVibrationEnabled.current
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
            .predictiveBackTransform(tasbeehPredictiveState.progress, tasbeehPredictiveState.swipeEdge)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
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
                            onCheckedChange = { isVibrationEnabled = it },
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
                            onCheckedChange = { isSoundEnabled = it },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isAutoCountEnabled) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.semanticPrimaryAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto Counter", style = MaterialTheme.typography.bodyMedium, color = Color.semanticPrimaryText)
                        }
                        Switch(
                            checked = isAutoCountEnabled,
                            onCheckedChange = { isAutoCountEnabled = it },
                            modifier = Modifier.testTag("autocount_switch"),
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

                    if (isAutoCountEnabled) {
                        Column {
                            Text(
                                text = "Pace: ${String.format(java.util.Locale.US, "%.1f", autoCountSpeedSec)}s per count",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.semanticMutedText
                            )
                            Slider(
                                value = autoCountSpeedSec,
                                onValueChange = { autoCountSpeedSec = it },
                                valueRange = 0.5f..5.0f,
                                modifier = Modifier.testTag("autocount_speed_slider")
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 2 & 3: DHIKR SELECTOR + STANDALONE DHIKR "+" BUTTON
        // Which Dhikr is being recited. Horizontally scrollable row + standalone '+'
        // =========================================================================
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets, key = { it.id }) { preset ->
                val isSelected = preset.id == selectedPreset.id
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) {
                        Color.semanticPrimaryAccent
                    } else {
                        Color.semanticSurface
                    },
                    animationSpec = tween(180),
                    label = "pillBg"
                )
                val pillText by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isDarkTheme) Color(0xFFFFFFFF) else Color.semanticAccentForeground
                    } else {
                        Color.semanticSecondaryText
                    },
                    animationSpec = tween(180),
                    label = "pillText"
                )
                val pillBorder = if (isSelected) {
                    BorderStroke(1.2.dp, Color.semanticPrimaryAccent)
                } else {
                    BorderStroke(1.dp, Color.semanticBorder)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = pillBg,
                    border = pillBorder,
                    modifier = Modifier
                        .testTag("preset_${preset.id}")
                        .semantics {
                            contentDescription = if (preset.isCustom) {
                                "${preset.nameEnglish}, long press to delete"
                            } else {
                                preset.nameEnglish
                            }
                        }
                        .pointerInput(preset.id) {
                            detectTapGestures(
                                onTap = {
                                    onSelectPreset(preset)
                                    isAutoCountEnabled = false
                                },
                                onLongPress = {
                                    if (preset.isCustom) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        dhikrToDelete = preset
                                    }
                                }
                            )
                        }
                ) {
                    Text(
                        text = preset.nameEnglish,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = pillText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Standalone "+" Icon Button to Add Custom Dhikr
            item {
                Surface(
                    onClick = {
                        editingCustomDhikr = null
                        showCustomDhikrDialog = true
                    },
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
                                    (slideInVertically(animationSpec = tween(120)) { it / 4 } + fadeIn(animationSpec = tween(120)))
                                        .togetherWith(slideOutVertically(animationSpec = tween(100)) { -it / 4 } + fadeOut(animationSpec = tween(100)))
                                } else {
                                    (slideInVertically(animationSpec = tween(120)) { -it / 4 } + fadeIn(animationSpec = tween(120)))
                                        .togetherWith(slideOutVertically(animationSpec = tween(100)) { it / 4 } + fadeOut(animationSpec = tween(100)))
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
                            text = if (isAutoCountEnabled) "Auto-counting..." else "Tap anywhere to count",
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
        // SECTION 9: ACTION CONTROLS [ − / ↻ ]
        // Decrement & Reset Controls sharing uniform height and centerline
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
}
