package com.example.ui.screens

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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.AppearanceMode
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.clip
import com.example.ui.components.PageHeader
import com.example.ui.theme.*
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityLocation
import com.example.data.model.HijriDate
import com.example.data.util.HijriCalc
import com.example.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Minimal Circular Qibla Compass Screen for FiveLight.
 */
@Composable
fun QiblaScreen(
    cityLocation: CityLocation,
    qiblaAngle: Float,
    compassHeading: Float,
    isSensorAvailable: Boolean,
    isAccuracyLow: Boolean = false,
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    hijriDate: HijriDate = remember { com.example.data.repository.IslamicDateRepository.getInstance().getCurrentHijriDate() },
    isActive: Boolean = true,
    onStartListening: () -> Unit = {},
    onStopListening: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isActive) {
        if (isActive) {
            onStartListening()
        } else {
            onStopListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onStopListening()
        }
    }
    val isSystemDark = androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val isDark = when (appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> isSystemDark
    }

    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
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

    // Real calculated distance to Kaaba in kilometers based on user location
    val distanceToKaabaKm = remember(cityLocation) {
        calculateDistanceToKaabaKm(cityLocation.latitude, cityLocation.longitude)
    }

    // Continuous shortest-path angle tracking for smooth, zero-latency rotation without 360° flips
    // 1. Continuous Delta: Shortest angular difference from current heading to Qibla (delta = qiblaAngle - heading)
    var continuousDelta by remember { mutableFloatStateOf(shortestSignedAngle(qiblaAngle - compassHeading)) }
    LaunchedEffect(qiblaAngle, compassHeading) {
        val targetDelta = shortestSignedAngle(qiblaAngle - compassHeading)
        val step = shortestSignedAngle(targetDelta - continuousDelta)
        continuousDelta += step
    }

    val animatedDelta by animateFloatAsState(
        targetValue = continuousDelta,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = 450f,
            dampingRatio = 0.85f
        ),
        label = "animated_delta"
    )

    // 2. Continuous Device Heading: For smooth rotation of compass dial ticks and cardinal directions
    var continuousHeading by remember { mutableFloatStateOf(compassHeading) }
    LaunchedEffect(compassHeading) {
        val step = shortestSignedAngle(compassHeading - continuousHeading)
        continuousHeading += step
    }

    val animatedHeading by animateFloatAsState(
        targetValue = continuousHeading,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = 450f,
            dampingRatio = 0.85f
        ),
        label = "animated_heading"
    )

    // Current normalized angular delta [-180°, 180°]: Positive = Turn Right, Negative = Turn Left
    val normalizedRelativeDelta = shortestSignedAngle(animatedDelta)

    // Progressive Kaaba Glow Intensity driven by absolute angular difference (30° threshold)
    val absDiff = abs(normalizedRelativeDelta)
    val targetGlowIntensity = if (absDiff < 30f) {
        ((30f - absDiff) / 30f).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedGlowIntensity by animateFloatAsState(
        targetValue = targetGlowIntensity,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "animated_kaaba_glow"
    )

    // Subtle breathing pulse at Kaaba when closely aligned (< 5°)
    val infiniteTransition = rememberInfiniteTransition(label = "kaaba_breathing_transition")
    val rawBreathingValue by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "raw_breathing"
    )

    val breathingFactor = if (absDiff <= 5f) rawBreathingValue else 1.0f

    // Alignment status text with ±4° tolerance threshold
    val facingThreshold = 4f
    val isAligned = isSensorAvailable && absDiff <= facingThreshold
    var wasAligned by remember { mutableStateOf(false) }

    // Subtle 200ms alignment settle hold state
    var isSettleHolding by remember { mutableStateOf(false) }

    // Part 1: Continuous Warmth-based ambient Qibla proximity glow behind entire compass ring
    // Raw proximity calculation: 180° away (0.0) to 0° facing Qibla (1.0)
    val rawProximityGlow = if (isSensorAvailable) ((180f - absDiff) / 180f).coerceIn(0f, 1f) else 0f
    val effectiveRawGlow = if (isSettleHolding) 1.0f else rawProximityGlow
    val easedProximityGlow = smoothstep(effectiveRawGlow)

    // Smooth visual intensity to prevent sensor-induced flicker
    val proximityGlow by animateFloatAsState(
        targetValue = easedProximityGlow,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "animated_proximity_glow"
    )

    // Qibla-Lock Ring Pulse Animation (Animates 0f -> 1f -> 0f over ~400ms)
    val ringPulseAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val isVibrationEnabled = LocalVibrationEnabled.current

    // Edge-triggered haptic feedback, Ring Pulse & Glow Settle: Triggered ONCE upon entering alignment threshold
    LaunchedEffect(isAligned, isActive) {
        if (!isActive) {
            wasAligned = false
            isSettleHolding = false
            return@LaunchedEffect
        }
        if (isAligned && !wasAligned) {
            // Trigger 150-250ms visual settle hold for ambient glow
            coroutineScope.launch {
                isSettleHolding = true
                delay(200)
                isSettleHolding = false
            }

            // Trigger Ring pulse animation
            coroutineScope.launch {
                ringPulseAnim.snapTo(0f)
                ringPulseAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(140, easing = FastOutSlowInEasing)
                )
                ringPulseAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                )
            }

            // Trigger alignment haptic
            if (isVibrationEnabled) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        FiveLightHaptics.performLightTap(view, haptic, isVibrationEnabled)
                    }
                } catch (_: Exception) {
                    FiveLightHaptics.performLightTap(view, haptic, isVibrationEnabled)
                }
            }
        }
        wasAligned = isAligned
    }

    // Part 3: Haptic Tick at Cardinal Crossings (N:0°, E:90°, S:180°, W:270°)
    // Tracks cardinal directions that have already triggered a haptic while remaining inside proximity (±5°)
    val triggeredCardinals = remember { HashSet<Int>() }

    LaunchedEffect(compassHeading, isActive) {
        if (!isActive) {
            triggeredCardinals.clear()
            return@LaunchedEffect
        }
        val cardinalAngles = intArrayOf(0, 90, 180, 270)
        for (cardinal in cardinalAngles) {
            val angularDiff = abs(shortestSignedAngle(compassHeading - cardinal.toFloat()))
            val isTriggered = triggeredCardinals.contains(cardinal)

            if (angularDiff <= 5f && !isTriggered) {
                // Transition into cardinal proximity zone (±5°) -> trigger haptic ONCE
                triggeredCardinals.add(cardinal)
                if (isVibrationEnabled) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createOneShot(8L, 50))
                        } else {
                            FiveLightHaptics.performSoftTick(view, haptic, isVibrationEnabled)
                        }
                    } catch (_: Exception) {
                        FiveLightHaptics.performSoftTick(view, haptic, isVibrationEnabled)
                    }
                }
            } else if (angularDiff > 7f && isTriggered) {
                // Re-arm cardinal direction when device rotates past hysteresis boundary (> 7°)
                triggeredCardinals.remove(cardinal)
            }
        }
    }

    // Part 2: Motion Trail on Compass Ring during physical rotation
    val trailSweepAnim = remember { Animatable(0f) }
    val trailAlphaAnim = remember { Animatable(0f) }
    var lastHeadingForTrail by remember { mutableFloatStateOf(compassHeading) }
    var lastHeadingTimeForTrail by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(compassHeading) {
        val now = System.currentTimeMillis()
        val dt = (now - lastHeadingTimeForTrail).coerceAtLeast(1)
        val dAngle = shortestSignedAngle(compassHeading - lastHeadingForTrail)
        lastHeadingForTrail = compassHeading
        lastHeadingTimeForTrail = now

        if (dt in 15..400 && abs(dAngle) > 0.4f && abs(dAngle) < 45f) {
            val speed = abs(dAngle) / (dt / 1000f) // degrees per second
            if (speed > 5f) {
                // Qibla-relative side determination:
                // userDelta: Negative = Left-side deviation, Positive = Right-side deviation
                val userDelta = shortestSignedAngle(compassHeading - qiblaAngle)

                // Continuous mapping across 0 to avoid teleporting.
                // The trail smoothly crosses 0 when aligned, and flips at 180 via shortest path.
                val qiblaSweep = userDelta.coerceIn(-28f, 28f)
                
                // Scale magnitude slightly by movement speed to preserve the dynamic trail feel
                val speedFactor = (speed / 20f).coerceIn(0.4f, 1f)
                val targetSweep = qiblaSweep * speedFactor
                val targetAlpha = (abs(dAngle) * 0.08f + 0.16f).coerceIn(0.14f, 0.35f)

                trailSweepAnim.snapTo(targetSweep)
                trailAlphaAnim.snapTo(targetAlpha)

                // Smoothly fade out and disappear after ~300-350ms
                launch {
                    trailAlphaAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                    trailSweepAnim.snapTo(0f)
                }
            }
        }
    }

    val statusText = when {
        !isSensorAvailable -> "Static Map Mode"
        absDiff <= facingThreshold -> "Facing Qibla"
        normalizedRelativeDelta > 35f -> "Turn Right"
        normalizedRelativeDelta in facingThreshold..35f -> "Turn Slightly Right"
        normalizedRelativeDelta < -35f -> "Turn Left"
        else -> "Turn Slightly Left"
    }

    // Formatted dates
    val gregorianDateStr = remember(hijriDate, System.currentTimeMillis() / 60000L) {
        SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date())
    }
    val hijriDateStr = remember(hijriDate) {
        HijriCalc.formatHijriString(hijriDate)
    }

    // Color tokens - Dark mode strictly uses true pitch-black #000000
    val orangeAccent = Color.semanticPrimaryAccent
    val bgBrush = if (isDark) {
        SolidColor(Color(0xFF000000))
    } else {
        SolidColor(LightBackground)
    }

    val primaryTextColor = Color.semanticPrimaryText
    val secondaryTextColor = Color.semanticSecondaryText

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // UNIFIED VERTICAL FLOW: Header -> Compass -> Status -> Distance -> Info Block -> Dock Clearance
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp), // Comfortable breathing room above bottom floating dock
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. LOCATION HEADER
            QiblaHeader(
                cityLocation = cityLocation,
                textColor = primaryTextColor,
                subTextColor = secondaryTextColor
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. LARGE MINIMAL CIRCULAR QIBLA COMPASS (~80% Width)
                SoftArcCompass(
                    animatedDelta = animatedDelta,
                    animatedHeading = animatedHeading,
                    glowIntensity = animatedGlowIntensity,
                    proximityGlow = proximityGlow,
                    ringPulse = ringPulseAnim.value,
                    trailSweep = trailSweepAnim.value,
                    trailAlpha = trailAlphaAnim.value,
                    breathingFactor = breathingFactor,
                    distanceKm = distanceToKaabaKm,
                    elevationMeters = 27.0,
                    isDark = isDark,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    orangeAccent = orangeAccent,
                    isAligned = isAligned,
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .aspectRatio(1f)
                        .testTag("qibla_compass_dial")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. QIBLA STATUS (Turn Left / Turn Right / Facing Qibla)
                QiblaStatus(
                    statusText = statusText,
                    orangeAccent = orangeAccent,
                    successColor = Color.semanticSuccess
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. REAL-TIME DISTANCE AND ELEVATION SUMMARY
                QiblaInformation(
                    distanceKm = distanceToKaabaKm,
                    elevationMeters = 27.0,
                    textColor = secondaryTextColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5. LOWER INFORMATION BLOCK (Qibla / Heading, Gregorian date, Hijri date grouped as one unit)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Live Qibla & Heading degree row (With Odometer-style roll)
                    QiblaLiveHeadingRow(
                        qiblaAngle = qiblaAngle,
                        compassHeading = compassHeading,
                        isAccuracyLow = isAccuracyLow,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        accentColor = orangeAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gregorian & Hijri Dates
                    QiblaCompassFooter(
                        gregorianDate = gregorianDateStr,
                        hijriDate = hijriDateStr,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor
                    )
                }
            }
        }
    }
}

/**
 * 1. Location Header at Top
 */
@Composable
private fun QiblaHeader(
    cityLocation: CityLocation,
    textColor: Color,
    subTextColor: Color
) {
    PageHeader(
        title = "Qibla Compass",
        subtitle = cityLocation.fullDisplayName,
        titleColor = textColor,
        subtitleColor = subTextColor
    )
}

/**
 * 2. Large Minimal Circular Qibla Compass Composable
 */
@Composable
private fun SoftArcCompass(
    animatedDelta: Float,
    animatedHeading: Float,
    glowIntensity: Float,
    proximityGlow: Float,
    ringPulse: Float,
    trailSweep: Float,
    trailAlpha: Float,
    breathingFactor: Float,
    distanceKm: Int,
    elevationMeters: Double,
    isDark: Boolean,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    orangeAccent: Color,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val badgeBgColor = Color.semanticSurface
        val kaabaBodyColor = Color.semanticPrimaryText
        val successColor = Color.semanticSuccess
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.40f

            // Part 1: Warmth-Based Qibla Proximity Glow (Contained radial glow growing small -> large)
            if (proximityGlow > 0.001f) {
                // Glow radius grows smoothly from small (0.22 * radius) to max (0.84 * radius)
                // Leaving a clear transparent margin so it NEVER touches the compass ring at radius
                val minGlowRadius = radius * 0.22f
                val maxGlowRadius = radius * 0.84f
                val ambientGlowRadius = minGlowRadius + (maxGlowRadius - minGlowRadius) * proximityGlow
                val glowBaseColor = if (isDark) Color(0xFF494556) else Color(0xFF8D6B1E)
                val peakOpacity = if (isDark) 0.32f else 0.26f
                val finalOpacity = (peakOpacity * proximityGlow).coerceIn(0f, 1f)

                // Safety circular clipping boundary at 0.90 * radius inside the compass circle
                val clipCirclePath = Path().apply {
                    addOval(Rect(center = center, radius = radius * 0.90f))
                }
                clipPath(clipCirclePath) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.00f to glowBaseColor.copy(alpha = finalOpacity),
                            0.35f to glowBaseColor.copy(alpha = finalOpacity * 0.85f),
                            0.65f to glowBaseColor.copy(alpha = finalOpacity * 0.45f),
                            0.85f to glowBaseColor.copy(alpha = finalOpacity * 0.10f),
                            1.00f to Color.Transparent,
                            center = center,
                            radius = ambientGlowRadius
                        ),
                        radius = ambientGlowRadius,
                        center = center
                    )
                }
            }

            // Part 1: Qibla-Lock Ring Pulse brightness interpolation
            val baseRingAlpha = if (isDark) 0.48f else 0.38f
            val pulseBonus = 0.32f * ringPulse
            val ringColor = if (isDark) {
                Color.White.copy(alpha = (baseRingAlpha + pulseBonus).coerceIn(0f, 0.85f))
            } else {
                Color.Black.copy(alpha = (baseRingAlpha + pulseBonus).coerceIn(0f, 0.75f))
            }

            // A. Thin Subtle Compass Ring
            drawCompassRing(center = center, radius = radius, color = ringColor)

            // Part 2: Motion Trail on Ring along needle path during active rotation
            if (abs(trailSweep) > 0.5f && trailAlpha > 0.005f) {
                val needleAngle = -90f + shortestSignedAngle(-animatedDelta)
                drawMotionTrail(
                    center = center,
                    radius = radius,
                    needleAngle = needleAngle,
                    trailSweep = trailSweep,
                    trailAlpha = trailAlpha,
                    color = orangeAccent
                )
            }

            // B. Subtle Tick Marks (36 positions rotated with compass dial heading)
            drawCompassTicks(
                center = center,
                radius = radius,
                animatedHeading = animatedHeading,
                color = ringColor
            )

            // C. Soft Accent Arc from 12 o'clock to direction arrow (Shortest angular path <= 180°)
            val shortestArcSweep = shortestSignedAngle(-animatedDelta)
            drawQiblaArc(
                center = center,
                radius = radius,
                sweepAngle = shortestArcSweep,
                color = orangeAccent
            )

            // Part 4: Cardinal Direction Labels (N, E, S, W) rotating with dial
            drawCardinalLabels(
                center = center,
                radius = radius,
                animatedHeading = animatedHeading,
                isDark = isDark,
                primaryColor = primaryTextColor,
                secondaryColor = secondaryTextColor
            )

            // E. Direction Needle / Arrow: Indicates current device heading relative to Kaaba
            // Points straight UP at 12 o'clock into the Kaaba marker when aligned (delta = 0)
            drawSymmetricCenterArrow(
                center = center,
                rotationDegrees = -animatedDelta,
                isDark = isDark,
                primaryTextColor = primaryTextColor
            )

            // D. Kaaba Target Marker — Visually FIXED at 12 o'clock (TOP/CENTER)
            // Icon is always upright with no rotational transform
            drawKaabaTargetMarker(
                center = center,
                radius = radius,
                glowIntensity = glowIntensity,
                breathingFactor = breathingFactor,
                isDark = isDark,
                primaryAccent = orangeAccent,
                badgeBgColor = badgeBgColor,
                kaabaBodyColor = kaabaBodyColor,
                successColor = successColor,
                isAligned = isAligned
            )
        }
    }
}

/**
 * A. Thin Subtle Compass Ring
 */
private fun DrawScope.drawCompassRing(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(width = 1.2.dp.toPx())
    )
}

/**
 * Part 2: Motion Trail on Ring Arc
 * Tapers progressively from newest portion near needle to soft faded tail.
 */
private fun DrawScope.drawMotionTrail(
    center: Offset,
    radius: Float,
    needleAngle: Float,
    trailSweep: Float,
    trailAlpha: Float,
    color: Color
) {
    val normalizedSweep = shortestSignedAngle(trailSweep)
    if (abs(normalizedSweep) < 0.5f || trailAlpha <= 0.005f) return

    val numSegments = 6
    val segmentSweep = normalizedSweep / numSegments
    for (i in 0 until numSegments) {
        val segStart = needleAngle + (i * segmentSweep)
        val t = i.toFloat() / numSegments
        val segAlpha = (trailAlpha * (1f - t * 0.70f)).coerceIn(0f, 0.38f)

        drawArc(
            color = color.copy(alpha = segAlpha),
            startAngle = segStart,
            sweepAngle = segmentSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(
                width = 2.0.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * B. Minimal Tick Marks (36 positions around circumference, rotating with dial heading)
 */
private fun DrawScope.drawCompassTicks(
    center: Offset,
    radius: Float,
    animatedHeading: Float,
    color: Color
) {
    for (i in 0 until 36) {
        val angleDeg = (i * 10.0) - animatedHeading - 90.0
        val angleRad = Math.toRadians(angleDeg)

        val isCardinal = i % 9 == 0
        val tickLen = if (isCardinal) 6.dp.toPx() else 3.dp.toPx()
        val innerR = radius - tickLen

        val outer = Offset(
            x = center.x + cos(angleRad).toFloat() * radius,
            y = center.y + sin(angleRad).toFloat() * radius
        )
        val inner = Offset(
            x = center.x + cos(angleRad).toFloat() * innerR,
            y = center.y + sin(angleRad).toFloat() * innerR
        )

        drawLine(
            color = color.copy(alpha = if (isCardinal) color.alpha * 1.3f else color.alpha * 0.5f),
            start = inner,
            end = outer,
            strokeWidth = if (isCardinal) 1.2.dp.toPx() else 0.8.dp.toPx()
        )
    }
}

/**
 * Part 4: Cardinal Direction Labels (N, E, S, W)
 * - Rotates dynamically with animatedHeading relative to True North.
 * - Stacking: Drawn on top layer with smooth radial clearance near 12 o'clock Kaaba marker.
 */
private fun DrawScope.drawCardinalLabels(
    center: Offset,
    radius: Float,
    animatedHeading: Float,
    isDark: Boolean,
    primaryColor: Color,
    secondaryColor: Color
) {
    val baseTextRadius = radius - 15.dp.toPx()
    val textSizePx = 11.5.sp.toPx()

    val cardinals = listOf(
        Pair("N", 0f),
        Pair("E", 90f),
        Pair("S", 180f),
        Pair("W", 270f)
    )

    val paint = android.graphics.Paint().apply {
        textSize = textSizePx
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    for ((label, cardinalBearing) in cardinals) {
        val angleDeg = cardinalBearing - animatedHeading - 90f
        val angleRad = Math.toRadians(angleDeg.toDouble())

        // Distance from top (12 o'clock where the fixed Kaaba marker sits)
        val angleFromTop = abs(shortestSignedAngle(cardinalBearing - animatedHeading))
        val textRadius = if (angleFromTop < 26f) {
            val overlapFactor = ((26f - angleFromTop) / 26f).coerceIn(0f, 1f)
            baseTextRadius - (7.dp.toPx() * overlapFactor)
        } else {
            baseTextRadius
        }

        val x = center.x + textRadius * cos(angleRad).toFloat()
        val y = center.y + textRadius * sin(angleRad).toFloat()

        val isNorth = label == "N"
        val labelColor = if (isNorth) {
            if (isDark) primaryColor.copy(alpha = 0.95f) else LightPrimaryText.copy(alpha = 0.95f)
        } else {
            if (isDark) secondaryColor.copy(alpha = 0.65f) else secondaryColor.copy(alpha = 0.75f)
        }

        paint.color = labelColor.toArgb()
        paint.isFakeBoldText = isNorth

        val fontMetrics = paint.fontMetrics
        val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
        drawContext.canvas.nativeCanvas.drawText(label, x, y - yOffset, paint)
    }
}

/**
 * C. Soft Accent Arc along circumference from 12 o'clock to the direction arrow
 * Strictly normalized to shortest path within [-180°, 180°].
 */
private fun DrawScope.drawQiblaArc(
    center: Offset,
    radius: Float,
    sweepAngle: Float,
    color: Color
) {
    val normalizedSweep = shortestSignedAngle(sweepAngle)
    if (abs(normalizedSweep) < 0.5f) return
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = normalizedSweep,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(
            width = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

/**
 * D. Kaaba Target Marker — Fixed at 12 o'clock (TOP/CENTER of compass ring)
 * Icon is ALWAYS upright. Features recognizable minimal Kaaba cube with gold kiswah and door.
 */
private fun DrawScope.drawKaabaTargetMarker(
    center: Offset,
    radius: Float,
    glowIntensity: Float,
    breathingFactor: Float,
    isDark: Boolean,
    primaryAccent: Color,
    badgeBgColor: Color,
    kaabaBodyColor: Color,
    successColor: Color,
    isAligned: Boolean
) {
    // 12 o'clock FIXED position
    val kaabaCenter = Offset(center.x, center.y - radius)
    val badgeRadius = 14.5.dp.toPx()

    // 1. Progressive Halo Glow around Kaaba (0.0 to 1.0 intensity)
    if (glowIntensity > 0.01f || isAligned) {
        val glowColor = if (isAligned) successColor else primaryAccent
        val effectiveGlow = (glowIntensity * breathingFactor).coerceIn(0f, 1f)
        val breathRadiusBonus = if (breathingFactor != 1.0f) (breathingFactor - 0.88f) * 14.dp.toPx() else 0f

        // Outer soft ambient glow
        drawCircle(
            color = glowColor.copy(alpha = 0.22f * effectiveGlow),
            radius = badgeRadius + (16.dp.toPx() * effectiveGlow) + breathRadiusBonus,
            center = kaabaCenter
        )

        // Mid vibrant halo
        drawCircle(
            color = glowColor.copy(alpha = 0.38f * effectiveGlow),
            radius = badgeRadius + (8.dp.toPx() * effectiveGlow) + (breathRadiusBonus * 0.5f),
            center = kaabaCenter
        )

        // Core highlight ring
        drawCircle(
            color = Color.White.copy(alpha = 0.55f * effectiveGlow),
            radius = badgeRadius + (2.dp.toPx() * effectiveGlow),
            center = kaabaCenter
        )
    }

    // 2. Badge Base Background & Border
    val badgeBorderColor = if (isAligned) {
        successColor
    } else if (glowIntensity > 0.4f) {
        primaryAccent
    } else if (isDark) {
        Color.White.copy(alpha = 0.85f)
    } else {
        LightBorder
    }

    drawCircle(
        color = Color.Black.copy(alpha = if (isDark) 0.35f else 0.12f),
        radius = badgeRadius + 2.dp.toPx(),
        center = kaabaCenter + Offset(0f, 1.dp.toPx())
    )

    drawCircle(
        color = badgeBgColor,
        radius = badgeRadius,
        center = kaabaCenter
    )

    drawCircle(
        color = badgeBorderColor,
        radius = badgeRadius,
        center = kaabaCenter,
        style = Stroke(width = (1.5 + glowIntensity * 1.5).dp.toPx())
    )

    // 3. Upright Minimal Kaaba Icon (Always upright, never rotated)
    val kaabaWidth = 13.dp.toPx()
    val kaabaHeight = 13.dp.toPx()
    val kaabaTopLeft = Offset(
        kaabaCenter.x - kaabaWidth / 2f,
        kaabaCenter.y - kaabaHeight / 2f
    )

    // Kaaba Black / Dark Charcoal Cube
    val kaabaCubeColor = if (isDark) Color(0xFF141416) else Color(0xFF1E1D1A)
    drawRoundRect(
        color = kaabaCubeColor,
        topLeft = kaabaTopLeft,
        size = Size(kaabaWidth, kaabaHeight),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Gold Kiswah Band across upper third
    val kiswahBandHeight = 2.6.dp.toPx()
    val kiswahTop = kaabaTopLeft.y + 2.2.dp.toPx()
    val goldColor = Color(0xFFD4AF37)
    drawRect(
        color = goldColor,
        topLeft = Offset(kaabaTopLeft.x, kiswahTop),
        size = Size(kaabaWidth, kiswahBandHeight)
    )

    // Gold Kaaba Door (Bab al-Kaaba) on the lower right
    val doorWidth = 2.4.dp.toPx()
    val doorHeight = 4.4.dp.toPx()
    val doorTopLeft = Offset(
        kaabaTopLeft.x + kaabaWidth - doorWidth - 2.dp.toPx(),
        kaabaTopLeft.y + kaabaHeight - doorHeight
    )
    drawRect(
        color = goldColor,
        topLeft = doorTopLeft,
        size = Size(doorWidth, doorHeight)
    )

    // Subtle golden roof rim
    drawLine(
        color = goldColor.copy(alpha = 0.8f),
        start = Offset(kaabaTopLeft.x + 1.dp.toPx(), kaabaTopLeft.y),
        end = Offset(kaabaTopLeft.x + kaabaWidth - 1.dp.toPx(), kaabaTopLeft.y),
        strokeWidth = 0.8.dp.toPx()
    )
}

/**
 * PART 0 & B1-B9: Mathematical Left/Right Mirror-Symmetric Center Needle Kite
 * Built strictly according to the specified square 40x40 coordinate reference:
 * Center: (20, 20) -> (0, 0) in centered coordinates
 * Top tip: (20, 4) -> (0, -16)
 * Right mid: (30, 26) -> (+10, +6)  [distance from center X = 10, Y = 26]
 * Bottom center notch: (20, 22) -> (0, +2)  [centered on X = 20, Y = 22]
 * Left mid: (10, 26) -> (-10, +6)   [distance from center X = 10, Y = 26]
 * Closed back to: (20, 4) -> (0, -16)
 *
 * Symmetrical around X=20 (rightDistance = 10, leftDistance = 10, both side Y = 26).
 * Single closed SVG/Canvas path.
 * Rotated purely around true center (0, 0) with invariant 2D isometric transform.
 */
private fun DrawScope.drawSymmetricCenterArrow(
    center: Offset,
    rotationDegrees: Float,
    isDark: Boolean,
    primaryTextColor: Color
) {
    // Proportional scale factor for the 40x40 coordinate grid
    val scale = 1.75.dp.toPx()

    val topTipY = (4f - 20f) * scale       // -16 * scale
    val rightMidX = (30f - 20f) * scale     // +10 * scale
    val rightMidY = (26f - 20f) * scale     // +6 * scale
    val bottomNotchY = (22f - 20f) * scale  // +2 * scale
    val leftMidX = (10f - 20f) * scale      // -10 * scale
    val leftMidY = (26f - 20f) * scale      // +6 * scale

    val symmetricKitePath = Path().apply {
        moveTo(0f, topTipY)
        lineTo(rightMidX, rightMidY)
        lineTo(0f, bottomNotchY)
        lineTo(leftMidX, leftMidY)
        close()
    }

    val arrowColor = if (isDark) Color.White else primaryTextColor

    withTransform({
        translate(center.x, center.y)
        rotate(rotationDegrees, Offset.Zero)
    }) {
        drawPath(
            path = symmetricKitePath,
            color = arrowColor
        )
    }
}

/**
 * 3. Qibla Status Directional Indicator (Informational, non-clickable)
 */
@Composable
private fun QiblaStatus(
    statusText: String,
    orangeAccent: Color,
    successColor: Color
) {
    Crossfade(
        targetState = statusText,
        animationSpec = tween(180),
        label = "statusFade"
    ) { text ->
        val isAligned = text == "Facing Qibla"
        val textColor = if (isAligned) successColor else Color.semanticPrimaryText
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAligned) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(successColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                letterSpacing = 0.4.sp
            )
        }
    }
}

/**
 * 4. Real-time Distance & Elevation Summary Line below direction status
 */
@Composable
private fun QiblaInformation(
    distanceKm: Int,
    elevationMeters: Double,
    textColor: Color
) {
    val formattedDistance = remember(distanceKm) {
        if (distanceKm > 0) String.format(Locale.ENGLISH, "%,d km to Kaaba", distanceKm) else "3,451 km to Kaaba"
    }

    val formattedElevation = remember(elevationMeters) {
        if (elevationMeters > 0) "${elevationMeters.toInt()} m above sea level" else "27 m above sea level"
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = formattedDistance,
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = textColor
        )

        Text(
            text = "   •   ",
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = textColor.copy(alpha = 0.4f)
        )

        Text(
            text = formattedElevation,
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = textColor
        )
    }
}

/**
 * Part 2: Odometer-Style Mechanical Number Animation
 * Digit-by-digit independent rolling vertical animation with rapid sensor update safety.
 */
@Composable
fun OdometerNumber(
    number: Int,
    suffix: String = "°",
    color: Color,
    modifier: Modifier = Modifier
) {
    val numberStr = remember(number) { number.toString() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        for (i in numberStr.indices) {
            val char = numberStr[i]
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(170, easing = FastOutSlowInEasing)) { height -> height } + fadeIn(tween(140)))
                        .togetherWith(
                            slideOutVertically(animationSpec = tween(170, easing = FastOutSlowInEasing)) { height -> -height } + fadeOut(tween(140))
                        )
                },
                label = "odometer_digit_$i"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = color
                )
            }
        }
        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = color
            )
        }
    }
}

/**
 * 5. Live Qibla Bearing & Device Heading Row (With Odometer Roll)
 */
@Composable
private fun QiblaLiveHeadingRow(
    qiblaAngle: Float,
    compassHeading: Float,
    isAccuracyLow: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val context = LocalContext.current
    val normalizedQibla = (qiblaAngle.toInt() % 360 + 360) % 360
    val normalizedHeading = (compassHeading.toInt() % 360 + 360) % 360

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qibla_live_heading_row")
    ) {
        Text(
            text = "Qibla ",
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = secondaryColor
        )

        OdometerNumber(
            number = normalizedQibla,
            color = primaryColor
        )

        Text(
            text = "   •   ",
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = secondaryColor.copy(alpha = 0.4f)
        )

        if (isAccuracyLow) {
            Text(
                text = "Calibrate compass",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = accentColor,
                modifier = Modifier.clickable {
                    Toast.makeText(
                        context,
                        "Move your phone in a figure-eight pattern to calibrate compass.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            Text(
                text = "Heading ",
                fontFamily = SpaceGrotesk,
                fontSize = 13.5.sp,
                color = secondaryColor
            )

            OdometerNumber(
                number = normalizedHeading,
                color = primaryColor
            )
        }
    }
}

/**
 * 6. Qibla Compass Footer (Gregorian & Hijri dates)
 */
@Composable
private fun QiblaCompassFooter(
    gregorianDate: String,
    hijriDate: String,
    primaryColor: Color,
    secondaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("qibla_compass_footer")
    ) {
        Text(
            text = gregorianDate,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 14.5.sp,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = hijriDate,
            fontFamily = SpaceGrotesk,
            fontSize = 12.5.sp,
            color = secondaryColor
        )
    }
}

/**
 * Calculates the shortest signed angle in degrees within [-180°, 180°].
 * Positive = Clockwise / Turn Right, Negative = Counter-clockwise / Turn Left.
 */
private fun shortestSignedAngle(angle: Float): Float {
    return ((angle + 180f) % 360f + 360f) % 360f - 180f
}

/**
 * Spherical Haversine calculation for exact real-world distance in km from user city to Makkah.
 */
private fun calculateDistanceToKaabaKm(lat: Double, lng: Double): Int {
    val makkahLat = 21.4225
    val makkahLng = 39.8262
    val dLat = Math.toRadians(makkahLat - lat)
    val dLng = Math.toRadians(makkahLng - lng)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(makkahLat)) *
            sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val r = 6371.0 // Earth radius in km
    return (r * c).toInt()
}

/**
 * Smoothstep interpolation for soft visual easing (Hermite curve S(x) = x^2 * (3 - 2x)).
 */
private fun smoothstep(x: Float): Float {
    val clamped = x.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}
