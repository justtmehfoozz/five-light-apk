package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Operating atmosphere mode for FiveLight ambient background.
 */
enum class AmbientUniverseMode {
    /**
     * Feature Introduction Screen (Prelude / onboarding).
     * Discovery feeling, more alive, cinematic, 15–30s rare shooting stars, five primary lights.
     */
    FEATURE_INTRO,

    /**
     * Login / Register / Pre-login prompt screens.
     * Peaceful arrival, calm, elegant, 12–20s softer/thinner shooting stars, zero distraction.
     */
    LOGIN_REGISTER;

    companion object {
        /**
         * Alias for backwards compatibility with existing PRELUDE references.
         */
        val PRELUDE get() = FEATURE_INTRO
    }
}

/**
 * Astronomical behavior classification for primary stars and legacy types.
 */
enum class StarTwinkleType {
    STEADY,
    BREATHING,
    RARE_LUMINOUS
}

/**
 * Data representation of a large Atmospheric Depth Light sitting behind the star field.
 * Provides soft photographic depth and distant cosmic haze without acting as a visible object.
 */
data class AtmosphericDepthLight(
    val relX: Float,
    val relY: Float,
    val radiusDp: Float,
    val pulseSpeed: Float,
    val orbitSpeedX: Float,
    val orbitSpeedY: Float,
    val orbitRadiusDp: Float,
    val phaseOffset: Float,
    val baseAlphaDark: Float,
    val baseAlphaLight: Float
)

/**
 * Data representation of a star in the FiveLight living universe star field.
 *
 * Each star features:
 * - Individual opacity boundaries (minAlpha, maxAlpha)
 * - Individual breathing timing (cycleDurationSec)
 * - Harmonic twinkling frequency multiplier (harmonicMultiplier)
 * - Random phase offset
 * - Independent 1–2px micro-drift speed & direction
 * - Star birth envelope for gentle emergence from darkness (hasStarBirth, birthCycleSec, birthPhase)
 */
data class AmbientStar(
    val relX: Float,
    val relY: Float,
    val radiusDp: Float,
    val minAlpha: Float,
    val maxAlpha: Float,
    val cycleDurationSec: Float,
    val phaseOffset: Float,
    val harmonicMultiplier: Float,
    val driftSpeedX: Float,
    val driftSpeedY: Float,
    val driftRadiusPx: Float,
    val isPrimary: Boolean = false,
    val layer: Int = 0, // 0 = distant dust, 1 = mid secondary, 2 = near brighter star, 3 = primary
    val hasHalo: Boolean = false,
    val hasStarBirth: Boolean = false,
    val birthCycleSec: Float = 0f,
    val birthPhase: Float = 0f
)

/**
 * Ultra-subtle floating cosmic dust particle for photographic depth.
 */
data class CosmicParticle(
    val startX: Float,
    val startY: Float,
    val speedX: Float,
    val speedY: Float,
    val radiusDp: Float,
    val baseAlpha: Float,
    val phaseOffset: Float
)

/**
 * Monotonic continuous universe clock shared across screens.
 * Ensures star positions, breathing phases, shooting star timelines, and atmospheric depth lights
 * never jump or reset during navigation between Splash, Prelude, Pre-Login, Login, and Register.
 */
object AmbientUniverseClock {
    private val appStartTimeNanos: Long = System.nanoTime()

    fun getElapsedSeconds(): Float {
        return (System.nanoTime() - appStartTimeNanos) / 1_000_000_000f
    }
}

/**
 * Deterministic catalog of celestial bodies ensuring zero reshuffling or heap churn across compositions.
 */
object AmbientUniverseData {
    val depthLights: List<AtmosphericDepthLight> = emptyList()
    val primaryStars: List<AmbientStar> = emptyList()
    val cosmicParticles: List<CosmicParticle> = emptyList()

    /**
     * Deterministic catalog of exactly 120 stars for the FiveLight Living Night Sky.
     * Pre-allocated once at class loading time to ensure ZERO runtime allocations per frame.
     *
     * Distribution:
     * - 60% (72 stars) Tiny dust-like stars: 20% -> 45% -> 20%, 4–7s cycle
     * - 30% (36 stars) Small visible stars: 25% -> 55% -> 25%, 5–8s cycle
     * - 10% (12 stars) Slightly brighter stars: 40% -> 80% -> 40%, 6–10s cycle with soft micro-halo
     *
     * Placement:
     * - Asymmetric random distribution biased towards empty corners and outer sky margins
     * - Clear important UI areas (top header / logo zone, central card and form zone)
     * - Pure deep space feeling, never a repetitive wallpaper or grid
     *
     * Natural Twinkling & Star Birth:
     * - Organic multi-harmonic breathing rhythm per star (no blinking / LED flashing)
     * - ~25% of stars gently emerge from 0% opacity and fade back into darkness (24–40s cycle)
     *
     * Subtle Star Movement:
     * - Floating micro-drift of only 1–2 pixels with independent direction and velocity
     */
    val livingStars: List<AmbientStar> by lazy {
        val random = Random(42421995L)
        val list = ArrayList<AmbientStar>(120)

        var count = 0
        while (count < 120) {
            val rx: Float
            val ry: Float
            val edgeRoll = random.nextFloat()

            // 65% bias towards empty margins and corners
            if (edgeRoll < 0.65f) {
                when (random.nextInt(4)) {
                    0 -> { // Upper night sky
                        rx = 0.03f + random.nextFloat() * 0.94f
                        ry = 0.02f + random.nextFloat() * 0.26f
                    }
                    1 -> { // Lower night sky
                        rx = 0.03f + random.nextFloat() * 0.94f
                        ry = 0.74f + random.nextFloat() * 0.24f
                    }
                    2 -> { // Left periphery
                        rx = 0.02f + random.nextFloat() * 0.16f
                        ry = 0.20f + random.nextFloat() * 0.58f
                    }
                    else -> { // Right periphery
                        rx = 0.82f + random.nextFloat() * 0.16f
                        ry = 0.20f + random.nextFloat() * 0.58f
                    }
                }
            } else {
                rx = 0.04f + random.nextFloat() * 0.92f
                ry = 0.04f + random.nextFloat() * 0.92f
            }

            // Keep logos, headers, and central card/form areas clear
            val inTopHeaderZone = (rx in 0.22f..0.78f && ry in 0.06f..0.22f)
            val inCentralContentZone = (rx in 0.16f..0.84f && ry in 0.26f..0.76f)
            if (inTopHeaderZone || inCentralContentZone) {
                // Heavily reject: only allow 8% chance and only if tiny dust star
                if (random.nextFloat() > 0.08f) {
                    continue
                }
            }

            // Distribution:
            // 60% Tiny dust-like stars (count % 10 in 4..9)
            // 30% Small visible stars (count % 10 in 1..3)
            // 10% Slightly brighter stars (count % 10 == 0)
            val cat = count % 10
            val radiusDp: Float
            val minAlpha: Float
            val maxAlpha: Float
            val cycleSec: Float
            val hasHalo: Boolean
            val layer: Int

            when (cat) {
                0 -> {
                    // Slightly brighter stars (10%): 40% -> 80% -> 40%, 6–10 second cycle
                    radiusDp = 1.30f + random.nextFloat() * 0.28f
                    minAlpha = 0.38f + random.nextFloat() * 0.04f
                    maxAlpha = 0.78f + random.nextFloat() * 0.04f
                    cycleSec = 6.0f + random.nextFloat() * 4.0f
                    hasHalo = true
                    layer = 2
                }
                1, 2, 3 -> {
                    // Small visible stars (30%): 25% -> 55% -> 25%, 5–8 second cycle
                    radiusDp = 0.90f + random.nextFloat() * 0.25f
                    minAlpha = 0.22f + random.nextFloat() * 0.05f
                    maxAlpha = 0.52f + random.nextFloat() * 0.05f
                    cycleSec = 5.0f + random.nextFloat() * 3.0f
                    hasHalo = false
                    layer = 1
                }
                else -> {
                    // Tiny dust-like stars (60%): 20% -> 45% -> 20%, 4–7 second cycle
                    radiusDp = 0.52f + random.nextFloat() * 0.22f
                    minAlpha = 0.18f + random.nextFloat() * 0.04f
                    maxAlpha = 0.42f + random.nextFloat() * 0.05f
                    cycleSec = 4.0f + random.nextFloat() * 3.0f
                    hasHalo = false
                    layer = 0
                }
            }

            val phaseOffset = (random.nextFloat() * 2 * PI).toFloat()
            val twinkleFreq = 1.15f + random.nextFloat() * 1.25f

            // Minimal floating drift: 0.8px .. 1.7px (only 1–2 pixels)
            val driftSpeedX = (0.16f + random.nextFloat() * 0.22f) * (if (random.nextBoolean()) 1f else -1f)
            val driftSpeedY = (0.14f + random.nextFloat() * 0.20f) * (if (random.nextBoolean()) 1f else -1f)
            val driftRadiusPx = 0.8f + random.nextFloat() * 0.9f

            // Star birth effect: ~25% of stars slowly fade from 0% -> faint -> normal -> slow fade
            val hasStarBirth = (count % 4 == 2)
            val birthCycleSec = if (hasStarBirth) 24.0f + random.nextFloat() * 16.0f else 0f
            val birthPhase = if (hasStarBirth) (random.nextFloat() * 2 * PI).toFloat() else 0f

            list.add(
                AmbientStar(
                    relX = rx,
                    relY = ry,
                    radiusDp = radiusDp,
                    minAlpha = minAlpha,
                    maxAlpha = maxAlpha,
                    cycleDurationSec = cycleSec,
                    phaseOffset = phaseOffset,
                    harmonicMultiplier = twinkleFreq,
                    driftSpeedX = driftSpeedX,
                    driftSpeedY = driftSpeedY,
                    driftRadiusPx = driftRadiusPx,
                    isPrimary = false,
                    layer = layer,
                    hasHalo = hasHalo,
                    hasStarBirth = hasStarBirth,
                    birthCycleSec = birthCycleSec,
                    birthPhase = birthPhase
                )
            )
            count++
        }
        list
    }

    /** Alias for backwards compatibility with any existing secondaryStars references */
    val secondaryStars: List<AmbientStar> get() = livingStars
}

/**
 * Native Jetpack Compose Ambient "Five Lights" Background — Living Universe.
 *
 * Screen Hierarchy:
 * - Feature Introduction Screen: Discovery feeling, more alive, cinematic universe experience, 15–30s rare shooting stars.
 * - Login / Register Screen: Peaceful arrival, calmer version of the same universe, 12–20s softer/thinner shooting stars.
 * - Continuous Universe: Shared monotonic universe clock, continuous star positions across navigation.
 *
 * Cosmic Depth Layering:
 * - Layer 1: Atmospheric depth lights (slow diffused moonlight haze)
 * - Layer 2: Subtle floating cosmic dust particles
 * - Layer 3: Rare cinematic shooting stars (one at a time, thin trail, white/gold tint, smooth fade in/out)
 * - Layer 4: Living star field (~96 stars with individual breathing, natural twinkling, star-birth emergence & 1-2px drift)
 * - Layer 5: Exactly FIVE sacred primary lights (in FEATURE_INTRO mode with micro-bloom and diffraction flare)
 * - Layer 6: Subtle feature transition light streak on rare scene changes
 */
@Composable
fun FiveLightAmbientBackground(
    modifier: Modifier = Modifier,
    initialAlpha: Float = 1.0f,
    mode: AmbientUniverseMode = AmbientUniverseMode.FEATURE_INTRO,
    showPrimaryLights: Boolean = (mode == AmbientUniverseMode.FEATURE_INTRO),
    transitionStreakIndex: Int = 0
) {
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Detect system reduced motion preference
    val reduceMotion = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // 1. Theme-adaptive Color System with smooth 700ms transitions
    val durationMs = 700
    val tweenSpec = tween<Color>(durationMillis = durationMs, easing = FastOutSlowInEasing)

    val bgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF000000) else Color(0xFFFAF7F2),
        animationSpec = tweenSpec,
        label = "bgAnim"
    )

    // Living Stars Colors (Crisp and visible on warm paper or deep pitch sky)
    val starColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFDBD6E8) else Color(0xFF635A4D),
        animationSpec = tweenSpec,
        label = "starAnim"
    )

    val starHaloColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF7E7399) else Color(0xFFD6C8B6),
        animationSpec = tweenSpec,
        label = "starHaloAnim"
    )

    // Shooting Star Colors: Crisp luminous core with golden celestial tint
    val shootingStarHeadColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFDF8) else Color(0xFF383022),
        animationSpec = tweenSpec,
        label = "shootHeadAnim"
    )

    val shootingStarTailColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFE5C78A) else Color(0xFF8D6B1E),
        animationSpec = tweenSpec,
        label = "shootTailAnim"
    )

    // Mode-dependent pace & intensity
    val isLoginMode = (mode == AmbientUniverseMode.LOGIN_REGISTER)
    val speedScale = if (isLoginMode) 0.55f else 1.0f
    val starAlphaScale = if (isLoginMode) 0.82f else 1.0f

    // 2. Lifecycle-Aware Frame Loop: Updates only when screen is STARTED
    val timeSecondsState = remember { mutableFloatStateOf(AmbientUniverseClock.getElapsedSeconds()) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                withFrameNanos {
                    timeSecondsState.floatValue = AmbientUniverseClock.getElapsedSeconds()
                }
            }
        }
    }

    // 3. Rare Transition Light Streak State (Triggered on rare scene changes, e.g. odd pages)
    var lastStreakStartTime by remember { mutableFloatStateOf(-100f) }
    var streakActive by remember { mutableStateOf(false) }

    LaunchedEffect(transitionStreakIndex) {
        if (!reduceMotion && transitionStreakIndex > 0 && (transitionStreakIndex % 2 == 1)) {
            // Very rare subtle streak on feature transitions
            lastStreakStartTime = AmbientUniverseClock.getElapsedSeconds()
            streakActive = true
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Read animation time exclusively inside DrawScope to prevent Composable recomposition
        val rawTime = timeSecondsState.floatValue
        val timeSeconds = rawTime * speedScale

        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Pure Pitch Black Canvas (#000000) in dark mode, no radial gradients or cloudy haze
        drawRect(color = bgColor)

        if (initialAlpha <= 0.001f || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        // 2. RARE CINEMATIC SHOOTING STARS (Zero allocation per frame)
        // Feature Introduction: 15–30s interval, duration 650–950ms, peak alpha 0.60, trail 56dp.
        // Login / Register: 12–20s interval, duration 600–850ms, thinner (0.80dp) & softer (peak alpha 0.30).
        // Guaranteed: Exactly ONE shooting star at a time, random direction, random position, white/gold tint.
        if (!reduceMotion) {
            val slotDuration = if (isLoginMode) 16.0f else 22.0f
            val currentSlot = (rawTime / slotDuration).toLong()
            val slotLocalTime = rawTime - currentSlot * slotDuration

            // Deterministic PRNG seeded by slot index
            val slotSeed = if (isLoginMode) {
                (currentSlot * 7919L + 31L) xor 0x5DEECE66DL
            } else {
                (currentSlot * 104729L + 17L) xor 0xBADC0FFEEL
            }
            val rng = Random(slotSeed)

            val delayInSlot = if (isLoginMode) {
                2.5f + rng.nextFloat() * 10.0f // 2.5s .. 12.5s
            } else {
                3.0f + rng.nextFloat() * 14.0f // 3.0s .. 17.0s
            }

            val shootDuration = if (isLoginMode) {
                0.60f + rng.nextFloat() * 0.25f // 600ms .. 850ms
            } else {
                0.65f + rng.nextFloat() * 0.30f // 650ms .. 950ms
            }

            if (slotLocalTime in delayInSlot..(delayInSlot + shootDuration)) {
                val progress = ((slotLocalTime - delayInSlot) / shootDuration).coerceIn(0f, 1f)

                val startX = 0.08f + rng.nextFloat() * 0.84f
                val startY = 0.04f + rng.nextFloat() * 0.28f
                val dirRight = rng.nextBoolean()
                val angleDeg = if (dirRight) {
                    32f + rng.nextFloat() * 22f // 32° .. 54° (down and right)
                } else {
                    126f + rng.nextFloat() * 22f // 126° .. 148° (down and left)
                }
                val angleRad = (angleDeg * PI / 180f).toFloat()

                val travelDistancePx = (if (isLoginMode) 130.dp else 210.dp).toPx()
                val trailLengthPx = (if (isLoginMode) 32.dp else 56.dp).toPx()
                val strokeWidthPx = (if (isLoginMode) 0.80.dp else 1.15.dp).toPx()
                val peakAlpha = if (isLoginMode) 0.30f else 0.60f

                // Smooth bell curve fade: ease in 0..0.18, hold 0.18..0.65, graceful ease out 0.65..1.0
                val fadeEnvelope = when {
                    progress < 0.18f -> (progress / 0.18f).pow(1.5f)
                    progress < 0.65f -> 1.0f
                    else -> ((1.0f - progress) / 0.35f).pow(1.5f)
                }
                val starAlpha = (fadeEnvelope * peakAlpha * initialAlpha).coerceIn(0f, 1f)

                val curTravel = travelDistancePx * progress
                val headX = canvasWidth * startX + cos(angleRad) * curTravel
                val headY = canvasHeight * startY + sin(angleRad) * curTravel
                val headOffset = Offset(headX, headY)

                val effectiveTrail = minOf(trailLengthPx, curTravel)
                val tailX = headX - cos(angleRad) * effectiveTrail
                val tailY = headY - sin(angleRad) * effectiveTrail
                val tailOffset = Offset(tailX, tailY)

                if (starAlpha > 0.005f && effectiveTrail > 1f) {
                    val trailBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            shootingStarTailColor.copy(alpha = starAlpha * 0.35f),
                            shootingStarTailColor.copy(alpha = starAlpha * 0.70f),
                            shootingStarHeadColor.copy(alpha = starAlpha)
                        ),
                        start = tailOffset,
                        end = headOffset
                    )

                    // Thin celestial trail
                    drawLine(
                        brush = trailBrush,
                        start = tailOffset,
                        end = headOffset,
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )

                    // Tiny crisp luminous head point
                    drawCircle(
                        color = shootingStarHeadColor.copy(alpha = starAlpha),
                        radius = strokeWidthPx * 1.15f,
                        center = headOffset
                    )
                }
            }
        }

        // 3. LIVING NIGHT SKY STAR FIELD (120 Stars)
        // Natural multi-harmonic breathing, star birth effect & 1–2px floating drift
        AmbientUniverseData.livingStars.forEach { star ->
            // Natural breathing & harmonic twinkling
            val twinkleSpeed = (2 * PI / star.cycleDurationSec).toFloat()
            val phase = (timeSeconds * twinkleSpeed + star.phaseOffset) % (2 * PI.toFloat())
            val rawSine = (sin(phase) + 1f) / 2f
            val harmonic = (sin(timeSeconds * twinkleSpeed * star.harmonicMultiplier + star.phaseOffset * 1.5f) + 1f) / 2f
            val breathFactor = rawSine * 0.80f + harmonic * 0.20f

            val baseOpacity = star.minAlpha + (star.maxAlpha - star.minAlpha) * breathFactor

            // Star birth effect: 0% -> faint -> normal -> slow fade back into darkness
            val birthEnvelope = if (star.hasStarBirth && star.birthCycleSec > 0f) {
                val bSpeed = (2 * PI / star.birthCycleSec).toFloat()
                val bRaw = (sin(timeSeconds * bSpeed + star.birthPhase) + 1f) / 2f
                bRaw.pow(2.4f)
            } else {
                1.0f
            }

            if (birthEnvelope <= 0.005f) return@forEach

            // 1–2 pixel subtle life-like drifting (scaled in login mode for calm stillness)
            val movementScale = if (isLoginMode) 0.60f else 1.0f
            val driftX = if (reduceMotion) 0f else sin(timeSeconds * star.driftSpeedX + star.phaseOffset) * star.driftRadiusPx * movementScale
            val driftY = if (reduceMotion) 0f else cos(timeSeconds * star.driftSpeedY + star.phaseOffset) * star.driftRadiusPx * movementScale

            val pointX = canvasWidth * star.relX + driftX
            val pointY = canvasHeight * star.relY + driftY
            val centerOffset = Offset(pointX, pointY)

            // Extra dampening in central card/form area during Login/Register for effortless legibility
            val isInCentralFormArea = isLoginMode &&
                    star.relX in 0.16f..0.84f && star.relY in 0.26f..0.76f
            val formDampening = if (isInCentralFormArea) 0.45f else 1.0f

            val currentAlpha = (baseOpacity * birthEnvelope * initialAlpha * starAlphaScale * formDampening)
                .coerceIn(0.04f, 0.95f)
            val starRadiusPx = star.radiusDp.dp.toPx()

            // Subtle pinpoint micro-halo around the 10% slightly brighter stars
            if (star.hasHalo && currentAlpha > 0.30f) {
                val haloRadius = starRadiusPx * 2.2f
                val haloBrush = Brush.radialGradient(
                    colors = listOf(
                        starHaloColor.copy(alpha = currentAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = haloRadius
                )
                drawCircle(
                    brush = haloBrush,
                    radius = haloRadius,
                    center = centerOffset
                )
            }

            // Crisp star core
            drawCircle(
                color = starColor.copy(alpha = currentAlpha),
                radius = starRadiusPx,
                center = centerOffset
            )
        }

        // 4. VERY RARE FEATURE TRANSITION LIGHT STREAK (Extremely subtle, 650ms)
        if (streakActive && !reduceMotion) {
            val streakElapsed = rawTime - lastStreakStartTime
            if (streakElapsed in 0f..0.65f) {
                val streakProgress = streakElapsed / 0.65f
                val streakAlpha = (sin(streakProgress * PI.toFloat()) * (if (isDark) 0.15f else 0.10f)).coerceIn(0f, 0.15f)
                val streakY = canvasHeight * 0.16f
                val streakLen = 85.dp.toPx()
                val streakStartX = canvasWidth * 0.12f + (canvasWidth * 0.50f) * streakProgress
                val streakEndX = streakStartX + streakLen

                val streakBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        shootingStarTailColor.copy(alpha = streakAlpha),
                        Color.Transparent
                    ),
                    start = Offset(streakStartX, streakY),
                    end = Offset(streakEndX, streakY)
                )

                drawLine(
                    brush = streakBrush,
                    start = Offset(streakStartX, streakY),
                    end = Offset(streakEndX, streakY),
                    strokeWidth = 0.9.dp.toPx(),
                    cap = StrokeCap.Round
                )
            } else if (streakElapsed > 0.65f) {
                streakActive = false
            }
        }
    }
}
