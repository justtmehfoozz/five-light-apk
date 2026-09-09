package com.example.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Astronomical properties for a single deterministic star in the living splash celestial sky.
 */
private data class SplashStar(
    val relX: Float,
    val relY: Float,
    val radiusDp: Float,
    val minAlpha: Float,
    val maxAlpha: Float,
    val twinkleSpeed: Float,
    val phaseOffset: Float,
    val driftSpeedX: Float,
    val driftSpeedY: Float,
    val driftRadiusPx: Float,
    val birthDelayMs: Float,
    val birthDurationMs: Float,
    val hasHalo: Boolean
)

/**
 * Deterministic generation of ~112 living stars.
 * Pre-allocated once at class loading time to ensure ZERO runtime allocations per frame.
 * Distribution: 60% microscopic dust, 30% small visible, 10% slightly brighter with soft halo.
 * Placement: Asymmetrically biased towards corners & margins with sacred negative space around the logo.
 */
private val splashStars: List<SplashStar> by lazy {
    val random = Random(4242L)
    val list = ArrayList<SplashStar>(112)

    var count = 0
    while (count < 112) {
        val rx = 0.03f + random.nextFloat() * 0.94f
        val ry = 0.03f + random.nextFloat() * 0.94f

        // Respect the sacred logo exclusion zone:
        // Wordmark and golden underline sit around center: relX 0.23f..0.77f and relY 0.41f..0.59f
        if (rx in 0.23f..0.77f && ry in 0.41f..0.59f) {
            continue
        }

        // Category breakdown:
        // index % 10 == 0 -> Brighter star (10%)
        // index % 10 in 1..3 -> Small visible star (30%)
        // index % 10 in 4..9 -> Dust star (60%)
        val cat = count % 10
        val radiusDp: Float
        val minAlpha: Float
        val maxAlpha: Float
        val hasHalo: Boolean

        when (cat) {
            0 -> {
                // Brighter star (1.4dp - 1.8dp)
                radiusDp = 1.4f + random.nextFloat() * 0.4f
                minAlpha = 0.32f + random.nextFloat() * 0.12f
                maxAlpha = 0.72f + random.nextFloat() * 0.16f
                hasHalo = true
            }
            1, 2, 3 -> {
                // Small visible star (0.9dp - 1.3dp)
                radiusDp = 0.9f + random.nextFloat() * 0.38f
                minAlpha = 0.18f + random.nextFloat() * 0.10f
                maxAlpha = 0.48f + random.nextFloat() * 0.18f
                hasHalo = false
            }
            else -> {
                // Dust star (0.5dp - 0.8dp)
                radiusDp = 0.5f + random.nextFloat() * 0.32f
                minAlpha = 0.10f + random.nextFloat() * 0.08f
                maxAlpha = 0.28f + random.nextFloat() * 0.14f
                hasHalo = false
            }
        }

        // Independent breathing duration between 2.2s and 6.5s
        val cycleSec = 2.2f + random.nextFloat() * 4.3f
        val twinkleSpeed = (2 * PI / cycleSec).toFloat()
        val phaseOffset = (random.nextFloat() * 2 * PI).toFloat()

        // Minimal life-like floating movement (0.8px - 2.2px max)
        val driftSpeedX = 0.5f + random.nextFloat() * 1.0f
        val driftSpeedY = 0.4f + random.nextFloat() * 0.9f
        val driftRadiusPx = 0.8f + random.nextFloat() * 1.4f

        // Star birth: 50% already present at 0ms, 30% emerge at 100-350ms, 20% emerge at 350-700ms
        val birthDelayMs = when {
            count % 4 == 0 || count % 4 == 1 -> 0f
            count % 4 == 2 -> 100f + random.nextFloat() * 250f
            else -> 350f + random.nextFloat() * 350f
        }
        val birthDurationMs = 280f + random.nextFloat() * 240f

        list.add(
            SplashStar(
                relX = rx,
                relY = ry,
                radiusDp = radiusDp,
                minAlpha = minAlpha,
                maxAlpha = maxAlpha,
                twinkleSpeed = twinkleSpeed,
                phaseOffset = phaseOffset,
                driftSpeedX = driftSpeedX,
                driftSpeedY = driftSpeedY,
                driftRadiusPx = driftRadiusPx,
                birthDelayMs = birthDelayMs,
                birthDurationMs = birthDurationMs,
                hasHalo = hasHalo
            )
        )
        count++
    }
    list
}

/**
 * FiveLight Splash Screen — Living Night Sky Experience.
 *
 * Preserves the pristine identity:
 * - Pure deep black background in dark theme (or warm paper in light theme).
 * - Centered FiveLight wordmark with staggered letter emergence and golden shimmer.
 * - Golden underline animation matching wordmark width.
 * - Canvas-based Living Night Sky with ~112 independent breathing stars.
 * - Subtle temporary illumination representing "light appearing from darkness" during reveal.
 * - Smooth, natural crossfade into the destination screen.
 */
@Composable
fun SplashScreen(
    isAppReady: Boolean,
    onExitProgressChanged: (Float) -> Unit,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()

    // 1. Reduced Motion Detection
    val isReducedMotion = remember(context) {
        try {
            val animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            val transitionScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            animatorScale == 0f || transitionScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // 2. Theme-aware Color Palette
    val backgroundColor = if (isDark) Color(0xFF000000) else MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val goldAccentColor = LightAccentGold // 0xFF8D6B1E Fajr/gold accent token

    // Celestial Star & Temporary Reveal Glow Colors
    val starCoreColor = if (isDark) Color(0xFFEDEBF5) else Color(0xFF5A5246)
    val starHaloColor = if (isDark) Color(0xFFB5A9D6) else Color(0xFFD6C8B6)
    val revealGlowColor = if (isDark) Color(0xFFE2C488) else Color(0xFFDFCEB5)

    // Shimmer highlight color
    val shimmerHighlightColor = if (isDark) {
        Color.White.copy(alpha = 0.40f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }

    // 3. Animation State
    var animationTimeMs by remember { mutableFloatStateOf(0f) }
    var isAnimationComplete by remember { mutableStateOf(false) }
    var wordmarkWidthPx by remember { mutableFloatStateOf(0f) }

    val cubicEaseOut = remember { CubicBezierEasing(0.19f, 1.0f, 0.22f, 1.0f) }

    // Display-synchronized, lifecycle-aware animation loop
    LaunchedEffect(isReducedMotion) {
        if (isReducedMotion) {
            animationTimeMs = 1500f
            isAnimationComplete = true
        } else {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - startTime).toFloat()
                animationTimeMs = elapsed
                if (elapsed >= 1500f && !isAnimationComplete) {
                    isAnimationComplete = true
                }
                withFrameNanos { /* synchronize with display vsync */ }
            }
        }
    }

    // Exit condition: BOTH 1500ms reveal complete AND app initialization ready
    val canExit = (isAnimationComplete && isAppReady) || (isReducedMotion && isAppReady)

    // Exit transition animatable (240ms duration smooth cross-fade)
    val exitProgress = remember { Animatable(0f) }
    LaunchedEffect(canExit) {
        if (canExit) {
            exitProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 240, easing = LinearEasing)
            ) {
                onExitProgressChanged(this.value)
            }
            onSplashFinished()
        }
    }

    val splashAlpha = 1f - exitProgress.value
    val density = LocalDensity.current

    // "FiveLight" wordmark characters
    val wordmarkLetters = remember { listOf('F', 'i', 'v', 'e', 'L', 'i', 'g', 'h', 't') }

    // ----------------------------------------------------
    // TIMELINE PHASING (Optimized for ~1.5s splash duration)
    // ----------------------------------------------------
    // 0ms: Faint star field visible
    // 300ms: Stars become more noticeable, letters stagger fade-in (280ms - 540ms)
    // 600ms: Golden underline writes from left to right (600ms - 900ms)
    // 900-1000ms: Subtle star brightness increase & breathing continues
    // 1000-1400ms: Golden shimmer sweep across wordmark
    // 1500ms: Seamless transition to next screen

    // Temporary light emergence effect (200ms - 750ms)
    // Represents "light appearing from darkness", disappearing completely afterward
    val revealGlowAlpha = if (isReducedMotion) {
        0f
    } else {
        when {
            animationTimeMs < 200f -> 0f
            animationTimeMs in 200f..420f -> {
                val p = (animationTimeMs - 200f) / 220f
                cubicEaseOut.transform(p) * (if (isDark) 0.16f else 0.10f)
            }
            animationTimeMs in 420f..750f -> {
                val p = (animationTimeMs - 420f) / 330f
                (1f - cubicEaseOut.transform(p)) * (if (isDark) 0.16f else 0.10f)
            }
            else -> 0f // No permanent glow behind the logo
        }
    }

    // Golden underline reveal progress (600ms - 900ms -> 0f to 1f)
    val underlineProgress = if (isReducedMotion) {
        1.0f
    } else {
        val rawUnderline = ((animationTimeMs - 600f) / 300f).coerceIn(0f, 1f)
        cubicEaseOut.transform(rawUnderline)
    }

    // Shimmer sweep progress (1000ms - 1400ms -> 0f to 1f)
    val shimmerLinearProgress = if (isReducedMotion) {
        0f
    } else {
        ((animationTimeMs - 1000f) / 400f).coerceIn(0f, 1f)
    }

    // Container alpha for reduced motion entrance
    val reducedMotionAlpha = if (isReducedMotion) {
        (animationTimeMs / 200f).coerceIn(0f, 1f)
    } else 1.0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = splashAlpha }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // ----------------------------------------------------
        // LIVING NIGHT SKY CANVAS LAYER (Behind the logo)
        // ----------------------------------------------------
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            // 1. Temporary Light Emergence Effect (Only during 200ms - 750ms reveal)
            if (revealGlowAlpha > 0.001f) {
                val glowCenter = Offset(canvasWidth * 0.50f, canvasHeight * 0.48f)
                val glowRadius = maxOf(1f, 150.dp.toPx())
                val glowBrush = Brush.radialGradient(
                    colors = listOf(
                        revealGlowColor.copy(alpha = revealGlowAlpha),
                        revealGlowColor.copy(alpha = revealGlowAlpha * 0.45f),
                        revealGlowColor.copy(alpha = revealGlowAlpha * 0.12f),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = glowRadius
                )
                drawCircle(
                    brush = glowBrush,
                    radius = glowRadius,
                    center = glowCenter
                )
            }

            // 2. Stars: Independent Twinkling, Birth Emergence & Subtle Life Movement
            val timeSec = animationTimeMs / 1000f

            // Global star field noticeability: faint at 0ms (0.55), subtly ramping to 1.0 by 350ms
            val globalNoticeability = if (isReducedMotion) 1f else {
                0.55f + 0.45f * (animationTimeMs / 350f).coerceIn(0f, 1f)
            }

            splashStars.forEach { star ->
                // Star birth emergence factor
                val birthProgress = if (isReducedMotion) 1f else {
                    if (animationTimeMs < star.birthDelayMs) 0f
                    else ((animationTimeMs - star.birthDelayMs) / star.birthDurationMs).coerceIn(0f, 1f)
                }
                if (birthProgress <= 0.001f) return@forEach

                // Independent sinusoidal breathing
                val phase = (timeSec * star.twinkleSpeed + star.phaseOffset) % (2 * PI.toFloat())
                val breathSine = (sin(phase) + 1f) / 2f
                val starAlpha = (star.minAlpha + (star.maxAlpha - star.minAlpha) * breathSine) *
                        birthProgress * globalNoticeability

                // Subtle organic sub-pixel drift
                val driftX = if (isReducedMotion) 0f else sin(timeSec * star.driftSpeedX + star.phaseOffset) * star.driftRadiusPx
                val driftY = if (isReducedMotion) 0f else cos(timeSec * star.driftSpeedY + star.phaseOffset) * star.driftRadiusPx

                val px = canvasWidth * star.relX + driftX
                val py = canvasHeight * star.relY + driftY
                val center = Offset(px, py)
                val radiusPx = star.radiusDp.dp.toPx()

                // Soft luminous halo for brighter stars
                if (star.hasHalo && starAlpha > 0.25f) {
                    val haloRadius = radiusPx * 2.8f
                    val haloBrush = Brush.radialGradient(
                        colors = listOf(
                            starHaloColor.copy(alpha = starAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = haloRadius
                    )
                    drawCircle(
                        brush = haloBrush,
                        radius = haloRadius,
                        center = center
                    )
                }

                // Crisp star core
                drawCircle(
                    color = starCoreColor.copy(alpha = starAlpha),
                    radius = radiusPx,
                    center = center
                )
            }
        }

        // ----------------------------------------------------
        // FOREGROUND: LOGO & GOLDEN UNDERLINE
        // ----------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { alpha = reducedMotionAlpha }
                .padding(horizontal = 24.dp)
        ) {
            // 1. CENTERED WORDMARK ("FiveLight") WITH STAGGER REVEAL & SHIMMER
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        if (coordinates.size.width > 0) {
                            wordmarkWidthPx = coordinates.size.width.toFloat()
                        }
                    }
                    .clipToBounds()
                    .drawWithContent {
                        drawContent()

                        // Single shimmer sweep effect (1000ms - 1400ms)
                        if (shimmerLinearProgress in 0.001f..0.999f) {
                            val sweepX = size.width * (shimmerLinearProgress * 1.8f - 0.4f)
                            val shimmerWidth = size.width * 0.35f

                            val shimmerBrush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    shimmerHighlightColor,
                                    Color.Transparent
                                ),
                                start = Offset(sweepX - shimmerWidth, 0f),
                                end = Offset(sweepX + shimmerWidth, size.height * 0.8f)
                            )

                            drawRect(
                                brush = shimmerBrush,
                                blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcAtop
                            )
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    wordmarkLetters.forEachIndexed { index, char ->
                        // Staggered letter reveal starting at 280ms, duration 240ms per letter
                        val letterStartTime = 280f + index * 32f
                        val letterProgress = if (isReducedMotion) {
                            1.0f
                        } else {
                            val raw = ((animationTimeMs - letterStartTime) / 240f).coerceIn(0f, 1f)
                            cubicEaseOut.transform(raw)
                        }

                        val letterAlpha = letterProgress
                        val letterOffsetYDp = with(density) { ((1f - letterProgress) * 8.dp.toPx()).toDp() }
                        val letterSpacingSp = ((-0.5) + (1f - letterProgress) * 4.0).sp

                        Text(
                            text = char.toString(),
                            fontFamily = InstrumentSerifItalic,
                            fontStyle = FontStyle.Italic,
                            fontSize = 50.sp,
                            lineHeight = 56.sp,
                            color = primaryTextColor,
                            letterSpacing = letterSpacingSp,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = letterAlpha
                                    translationY = with(density) { letterOffsetYDp.toPx() }
                                }
                        )
                    }
                }
            }

            // Small, intentional gap between wordmark and underline
            Spacer(modifier = Modifier.height(6.dp))

            // 2. GOLD/AMBER UNDERLINE (MATCHING WORDMARK WIDTH EXACTLY)
            val underlineWidthDp = if (wordmarkWidthPx > 0) {
                with(density) { wordmarkWidthPx.toDp() }
            } else 180.dp

            Box(
                modifier = Modifier
                    .width(underlineWidthDp)
                    .height(2.dp)
            ) {
                if (underlineProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(fraction = underlineProgress)
                            .height(2.dp)
                            .background(goldAccentColor)
                    )
                }
            }
        }
    }
}
