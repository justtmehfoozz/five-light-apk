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
    val layer: Int = 0, // 0 = Layer 1 (far micro stars), 1 = Layer 2 (medium visible), 2 = Layer 2 (brighter supporting), 3 = Layer 3 (primary)
    val hasHalo: Boolean = false,
    val hasStarBirth: Boolean = false,
    val birthCycleSec: Float = 0f,
    val birthPhase: Float = 0f,
    val awakeningDelaySec: Float = 0f
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
    // 3 Large atmospheric depth lights providing soft moonlight haze and cosmic depth
    val depthLights: List<AtmosphericDepthLight> = listOf(
        AtmosphericDepthLight(
            relX = 0.22f,
            relY = 0.16f,
            radiusDp = 175f,
            pulseSpeed = 0.35f, // ~18s cycle
            orbitSpeedX = 0.045f,
            orbitSpeedY = 0.030f,
            orbitRadiusDp = 14f,
            phaseOffset = 0.0f,
            baseAlphaDark = 0.11f,
            baseAlphaLight = 0.065f
        ),
        AtmosphericDepthLight(
            relX = 0.80f,
            relY = 0.28f,
            radiusDp = 210f,
            pulseSpeed = 0.28f, // ~22s cycle
            orbitSpeedX = 0.035f,
            orbitSpeedY = 0.042f,
            orbitRadiusDp = 18f,
            phaseOffset = 2.4f,
            baseAlphaDark = 0.13f,
            baseAlphaLight = 0.075f
        ),
        AtmosphericDepthLight(
            relX = 0.48f,
            relY = 0.76f,
            radiusDp = 160f,
            pulseSpeed = 0.42f, // ~15s cycle
            orbitSpeedX = 0.048f,
            orbitSpeedY = 0.036f,
            orbitRadiusDp = 12f,
            phaseOffset = 4.3f,
            baseAlphaDark = 0.09f,
            baseAlphaLight = 0.055f
        )
    )

    // EXACTLY 5 PRIMARY LIGHTS - Sacred coordinates in 5 distinct regions around the UI
    // Cycle: 3–6 seconds breathing rhythm
    val primaryStars: List<AmbientStar> = listOf(
        AmbientStar(
            relX = 0.18f, relY = 0.13f, radiusDp = 2.4f,
            minAlpha = 0.80f, maxAlpha = 0.98f, cycleDurationSec = 3.8f,
            phaseOffset = 0.0f, harmonicMultiplier = 2.2f,
            driftSpeedX = 0.08f, driftSpeedY = 0.06f, driftRadiusPx = 0.6f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.82f, relY = 0.16f, radiusDp = 2.4f,
            minAlpha = 0.80f, maxAlpha = 0.98f, cycleDurationSec = 4.4f,
            phaseOffset = 1.35f, harmonicMultiplier = 1.8f,
            driftSpeedX = 0.07f, driftSpeedY = 0.09f, driftRadiusPx = 0.6f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.50f, relY = 0.26f, radiusDp = 2.5f,
            minAlpha = 0.82f, maxAlpha = 1.00f, cycleDurationSec = 3.6f,
            phaseOffset = 2.70f, harmonicMultiplier = 2.4f,
            driftSpeedX = 0.09f, driftSpeedY = 0.07f, driftRadiusPx = 0.7f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.14f, relY = 0.42f, radiusDp = 2.3f,
            minAlpha = 0.78f, maxAlpha = 0.95f, cycleDurationSec = 4.8f,
            phaseOffset = 4.05f, harmonicMultiplier = 1.7f,
            driftSpeedX = 0.06f, driftSpeedY = 0.08f, driftRadiusPx = 0.5f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.86f, relY = 0.38f, radiusDp = 2.3f,
            minAlpha = 0.78f, maxAlpha = 0.95f, cycleDurationSec = 5.2f,
            phaseOffset = 5.40f, harmonicMultiplier = 2.0f,
            driftSpeedX = 0.08f, driftSpeedY = 0.05f, driftRadiusPx = 0.5f,
            isPrimary = true, layer = 3, hasHalo = true
        )
    )

    /**
     * Deterministic catalog of 200 secondary stars (in the 150–250 range).
     * Pre-allocated once at class loading time to ensure ZERO runtime allocations per frame.
     *
     * Distribution:
     * - 70% Dust-like micro stars: 15% -> 50% -> 15%, 2.5–5s cycle (Layer 1: almost static, slow breathing)
     * - 25% Small visible stars: 25% -> 70% -> 25%, 4–7s cycle (Layer 2: visible twinkle, 1-2px movement)
     * - 5% Slightly brighter supporting stars: 40% -> 90% -> 40%, 5–8s cycle (Layer 2: 1-2px movement)
     *
     * Placement:
     * - Asymmetric natural deep-space distribution biased towards outer margins and corners
     * - Central UI exclusion zone kept clean for cards, headers, and form inputs
     *
     * Star Birth:
     * - ~30% of stars possess an organic 16–32s birth-and-fade envelope:
     *   Disappear completely -> slowly appear -> become visible -> fade again
     *
     * Star Movement:
     * - 1–3 pixel floating movement with independent direction and velocity per star
     */
    val secondaryStars: List<AmbientStar> by lazy {
        val random = Random(19950711L)
        val list = ArrayList<AmbientStar>(200)

        var count = 0
        while (count < 200) {
            val rx = 0.015f + random.nextFloat() * 0.97f
            val ry = 0.02f + random.nextFloat() * 0.96f

            // Negative space check: Keep center region sparse so cards and forms are prominent
            val inCentralArea = (rx in 0.22f..0.78f && ry in 0.28f..0.72f)
            if (inCentralArea && random.nextFloat() > 0.10f) {
                continue
            }

            // Category breakdown (exact 70% / 25% / 5% distribution):
            // 5% (cat == 0): Slightly brighter supporting stars (10 stars)
            // 25% (cat in 1..5): Small visible stars (50 stars)
            // 70% (cat in 6..19): Dust-like micro stars (140 stars)
            val cat = count % 20
            val radiusDp: Float
            val minAlpha: Float
            val maxAlpha: Float
            val cycleSec: Float
            val hasHalo: Boolean
            val layer: Int
            val driftRadiusPx: Float

            when (cat) {
                0 -> {
                    // Slightly brighter supporting stars (5%): 40% -> 90% -> 40%, 5–8 second cycle
                    radiusDp = 1.30f + random.nextFloat() * 0.35f
                    minAlpha = 0.38f + random.nextFloat() * 0.04f
                    maxAlpha = 0.86f + random.nextFloat() * 0.05f
                    cycleSec = 5.0f + random.nextFloat() * 3.0f
                    hasHalo = false
                    layer = 2
                    driftRadiusPx = 1.1f + random.nextFloat() * 0.8f // 1.1px .. 1.9px
                }
                in 1..5 -> {
                    // Small visible stars (25%): 25% -> 70% -> 25%, 4–7 second cycle
                    radiusDp = 0.75f + random.nextFloat() * 0.28f
                    minAlpha = 0.23f + random.nextFloat() * 0.04f
                    maxAlpha = 0.67f + random.nextFloat() * 0.05f
                    cycleSec = 4.0f + random.nextFloat() * 3.0f
                    hasHalo = false
                    layer = 1
                    driftRadiusPx = 0.9f + random.nextFloat() * 0.7f // 0.9px .. 1.6px
                }
                else -> {
                    // Dust-like micro stars (70%): 15% -> 50% -> 15%, 2.5–5 second cycle
                    radiusDp = 0.35f + random.nextFloat() * 0.22f
                    minAlpha = 0.13f + random.nextFloat() * 0.04f
                    maxAlpha = 0.47f + random.nextFloat() * 0.05f
                    cycleSec = 2.5f + random.nextFloat() * 2.5f
                    hasHalo = false
                    layer = 0 // Layer 1 in space depth: far micro stars, almost static
                    driftRadiusPx = 0.35f + random.nextFloat() * 0.35f // 0.35px .. 0.70px
                }
            }

            val phaseOffset = (random.nextFloat() * 2 * PI).toFloat()
            val twinkleFreq = 1.1f + random.nextFloat() * 1.1f

            // Floating movement with independent speed & direction per star
            val driftSpeedX = (0.10f + random.nextFloat() * 0.20f) * (if (random.nextBoolean()) 1f else -1f)
            val driftSpeedY = (0.08f + random.nextFloat() * 0.18f) * (if (random.nextBoolean()) 1f else -1f)

            // Star birth effect: ~30% of stars slowly emerge from darkness and fade back
            val hasStarBirth = (count % 3 == 1)
            val birthCycleSec = if (hasStarBirth) 16.0f + random.nextFloat() * 16.0f else 0f
            val birthPhase = if (hasStarBirth) (random.nextFloat() * 2 * PI).toFloat() else 0f

            // Introduction Screen awakening delay: Stars gently awaken over the first 0..2.2 seconds
            val awakeningDelaySec = if (cat == 0) 0f else (count.toFloat() / 200f) * 1.8f

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
                    birthPhase = birthPhase,
                    awakeningDelaySec = awakeningDelaySec
                )
            )
            count++
        }
        list
    }

    // 14 Rare microscopic cosmic particles creating photographic depth
    val cosmicParticles: List<CosmicParticle> = listOf(
        CosmicParticle(startX = 0.12f, startY = 0.20f, speedX = 0.003f, speedY = -0.012f, radiusDp = 0.8f, baseAlpha = 0.15f, phaseOffset = 0.2f),
        CosmicParticle(startX = 0.28f, startY = 0.45f, speedX = -0.002f, speedY = -0.009f, radiusDp = 0.6f, baseAlpha = 0.12f, phaseOffset = 1.4f),
        CosmicParticle(startX = 0.74f, startY = 0.15f, speedX = 0.004f, speedY = -0.015f, radiusDp = 0.9f, baseAlpha = 0.18f, phaseOffset = 2.8f),
        CosmicParticle(startX = 0.88f, startY = 0.60f, speedX = -0.003f, speedY = -0.010f, radiusDp = 0.7f, baseAlpha = 0.14f, phaseOffset = 4.1f),
        CosmicParticle(startX = 0.08f, startY = 0.75f, speedX = 0.002f, speedY = -0.014f, radiusDp = 0.8f, baseAlpha = 0.16f, phaseOffset = 5.3f),
        CosmicParticle(startX = 0.65f, startY = 0.82f, speedX = -0.004f, speedY = -0.011f, radiusDp = 0.6f, baseAlpha = 0.11f, phaseOffset = 0.9f),
        CosmicParticle(startX = 0.45f, startY = 0.10f, speedX = 0.003f, speedY = -0.008f, radiusDp = 0.7f, baseAlpha = 0.13f, phaseOffset = 2.1f),
        CosmicParticle(startX = 0.19f, startY = 0.90f, speedX = -0.002f, speedY = -0.013f, radiusDp = 0.9f, baseAlpha = 0.17f, phaseOffset = 3.6f),
        CosmicParticle(startX = 0.82f, startY = 0.40f, speedX = 0.003f, speedY = -0.016f, radiusDp = 0.7f, baseAlpha = 0.14f, phaseOffset = 4.9f),
        CosmicParticle(startX = 0.35f, startY = 0.80f, speedX = -0.003f, speedY = -0.009f, radiusDp = 0.6f, baseAlpha = 0.12f, phaseOffset = 1.7f),
        CosmicParticle(startX = 0.92f, startY = 0.25f, speedX = 0.002f, speedY = -0.012f, radiusDp = 0.8f, baseAlpha = 0.15f, phaseOffset = 3.1f),
        CosmicParticle(startX = 0.04f, startY = 0.30f, speedX = -0.003f, speedY = -0.014f, radiusDp = 0.7f, baseAlpha = 0.13f, phaseOffset = 4.5f),
        CosmicParticle(startX = 0.52f, startY = 0.95f, speedX = 0.004f, speedY = -0.010f, radiusDp = 0.8f, baseAlpha = 0.16f, phaseOffset = 0.4f),
        CosmicParticle(startX = 0.78f, startY = 0.70f, speedX = -0.002f, speedY = -0.011f, radiusDp = 0.6f, baseAlpha = 0.10f, phaseOffset = 2.5f)
    )
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
 * - Layer 4: Living star field (120 stars with individual breathing, natural twinkling, star-birth emergence & 1-2px drift)
 * - Layer 5: Exactly FIVE sacred primary lights (with micro-bloom and diffraction flare)
 * - Layer 6: Subtle feature transition light streak on rare scene changes
 */
@Composable
fun FiveLightAmbientBackground(
    modifier: Modifier = Modifier,
    initialAlpha: Float = 1.0f,
    mode: AmbientUniverseMode = AmbientUniverseMode.FEATURE_INTRO,
    showPrimaryLights: Boolean = true,
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

    // 1. Theme-adaptive Color System with smooth 400ms transitions (300-500ms range)
    val durationMs = 400
    val tweenSpec = tween<Color>(durationMillis = durationMs, easing = FastOutSlowInEasing)

    val bgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF000000) else Color(0xFFFAF7F2),
        animationSpec = tweenSpec,
        label = "bgAnim"
    )

    // Depth Lights Colors (Soft diffused nebula glow)
    val depthLightColor1 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1B152B) else Color(0xFFEADECC),
        animationSpec = tweenSpec,
        label = "depthLight1Anim"
    )

    val depthLightColor2 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF141024) else Color(0xFFE3D6C2),
        animationSpec = tweenSpec,
        label = "depthLight2Anim"
    )

    val depthLightColor3 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF181328) else Color(0xFFDFD1BC),
        animationSpec = tweenSpec,
        label = "depthLight3Anim"
    )

    // Secondary Stars Colors (Crisp and visible on warm paper or deep sky)
    val secondaryStarColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFDBD6E8) else Color(0xFF635A4D),
        animationSpec = tweenSpec,
        label = "secondaryStarAnim"
    )

    val secondaryStarGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF7E7399) else Color(0xFFD6C8B6),
        animationSpec = tweenSpec,
        label = "secondaryGlowAnim"
    )

    // Cosmic Particle Color
    val particleColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF8B81A8) else Color(0xFFB5A998),
        animationSpec = tweenSpec,
        label = "particleAnim"
    )

    // Shooting Star Colors: White core with golden celestial tint
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

    // Primary Lights Sacred Colors (Gold/Champagne Radiance)
    val primaryCoreColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFAF8FF) else Color(0xFF4A3E31),
        animationSpec = tweenSpec,
        label = "primaryCoreAnim"
    )

    val primaryCoreHighlightColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFFF9F5EE),
        animationSpec = tweenSpec,
        label = "primaryHighlightAnim"
    )

    val primaryInnerGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFEDE9FC) else Color(0xFFBFA98D),
        animationSpec = tweenSpec,
        label = "primaryInnerGlowAnim"
    )

    val primaryOuterGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFA294CE) else Color(0xFFE0CDB7),
        animationSpec = tweenSpec,
        label = "primaryOuterGlowAnim"
    )

    val primaryAuraColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF453A60) else Color(0xFFEDE0CF),
        animationSpec = tweenSpec,
        label = "primaryAuraAnim"
    )

    val primaryBloomColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF322846) else Color(0xFFF3E7D7),
        animationSpec = tweenSpec,
        label = "primaryBloomAnim"
    )

    val primaryFlareColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFDFD9F5) else Color(0xFF8C775D),
        animationSpec = tweenSpec,
        label = "primaryFlareAnim"
    )

    // Mode-dependent pace & intensity
    val isLoginMode = (mode == AmbientUniverseMode.LOGIN_REGISTER)
    val speedScale = if (isLoginMode) 0.55f else 1.0f
    val depthAlphaScale = if (isLoginMode) 0.78f else 1.0f
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

    // Pre-calculate dimensional density constants
    val primaryBloomRadiusPx = with(density) { 32.dp.toPx() }
    val primaryAuraRadiusPx = with(density) { 22.dp.toPx() }
    val primaryOuterGlowRadiusPx = with(density) { 11.dp.toPx() }
    val primaryInnerGlowRadiusPx = with(density) { 5.5.dp.toPx() }
    val flareArmLengthPx = with(density) { 8.5.dp.toPx() }
    val flareStrokePx = with(density) { 1.0.dp.toPx() }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Read animation time exclusively inside DrawScope to prevent Composable recomposition
        val rawTime = timeSecondsState.floatValue
        val timeSeconds = rawTime * speedScale

        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Fill solid background canvas
        drawRect(color = bgColor)

        if (initialAlpha <= 0.001f || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        // 2. LAYER 1: ATMOSPHERIC DEPTH (In light mode subtle warm tint; in dark mode pure black #000000 maintained)
        if (!isDark) {
            AmbientUniverseData.depthLights.forEachIndexed { index, light ->
                val color = when (index % 3) {
                    0 -> depthLightColor1
                    1 -> depthLightColor2
                    else -> depthLightColor3
                }

                val dPhase = (timeSeconds * light.pulseSpeed + light.phaseOffset) % (2 * PI.toFloat())
                val dPulse = (sin(dPhase) + 1f) / 2f

                val baseAlpha = light.baseAlphaLight
                val dAlpha = (baseAlpha * (0.85f + 0.30f * dPulse) * initialAlpha * depthAlphaScale).coerceIn(0.01f, 0.25f)

                val orbitDriftX = if (reduceMotion) 0f else {
                    sin(timeSeconds * light.orbitSpeedX + light.phaseOffset) * light.orbitRadiusDp.dp.toPx() * 0.035f
                }
                val orbitDriftY = if (reduceMotion) 0f else {
                    cos(timeSeconds * light.orbitSpeedY + light.phaseOffset) * light.orbitRadiusDp.dp.toPx() * 0.030f
                }

                val dCenterX = canvasWidth * light.relX + orbitDriftX
                val dCenterY = canvasHeight * light.relY + orbitDriftY
                val dOffset = Offset(dCenterX, dCenterY)
                val dRadiusPx = maxOf(1f, light.radiusDp.dp.toPx() * (0.94f + 0.12f * dPulse))

                val dBrush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = dAlpha),
                        color.copy(alpha = dAlpha * 0.55f),
                        color.copy(alpha = dAlpha * 0.18f),
                        Color.Transparent
                    ),
                    center = dOffset,
                    radius = dRadiusPx
                )

                drawCircle(
                    brush = dBrush,
                    radius = dRadiusPx,
                    center = dOffset
                )
            }
        }

        // 3. LAYER 2: SUBTLE FLOATING COSMIC PARTICLES (Drifting celestial dust)
        AmbientUniverseData.cosmicParticles.forEach { particle ->
            val normX = if (reduceMotion) particle.startX else {
                val raw = (particle.startX + particle.speedX * timeSeconds + 1000f) % 1.0f
                if (raw < 0f) raw + 1.0f else raw
            }
            val normY = if (reduceMotion) particle.startY else {
                val raw = (particle.startY + particle.speedY * timeSeconds + 1000f) % 1.0f
                if (raw < 0f) raw + 1.0f else raw
            }

            val px = normX * canvasWidth
            val py = normY * canvasHeight
            val pPulse = 0.72f + 0.28f * sin(timeSeconds * 0.35f + particle.phaseOffset)
            val particleAlpha = (particle.baseAlpha * pPulse * initialAlpha * (if (isLoginMode) 0.60f else 1.0f))
                .coerceIn(0.015f, 0.20f)
            val pRadius = particle.radiusDp.dp.toPx()

            drawCircle(
                color = particleColor.copy(alpha = particleAlpha),
                radius = pRadius,
                center = Offset(px, py)
            )
        }

        // 4. LAYER 3: RARE CINEMATIC SHOOTING STARS (Zero allocation per frame)
        // Feature Introduction: 18–28s interval, duration 650–1000ms.
        // Login / Register: 14–22s interval, duration 600–850ms, thinner (0.85dp) & softer (alpha 0.30).
        // Direction variation: ↘, ↙, → (randomized angle, speed, position, white/gold tint)
        // Guaranteed: Exactly ONE shooting star at a time.
        if (!reduceMotion) {
            val slotDuration = if (isLoginMode) 17.0f else 24.0f
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
                3.0f + rng.nextFloat() * 10.0f // 3.0s .. 13.0s
            } else {
                4.0f + rng.nextFloat() * 14.0f // 4.0s .. 18.0s
            }

            val shootDuration = if (isLoginMode) {
                0.60f + rng.nextFloat() * 0.25f // 600ms .. 850ms
            } else {
                0.65f + rng.nextFloat() * 0.35f // 650ms .. 1000ms
            }

            if (slotLocalTime in delayInSlot..(delayInSlot + shootDuration)) {
                val progress = ((slotLocalTime - delayInSlot) / shootDuration).coerceIn(0f, 1f)

                // 3 randomized trajectory directions: ↘, ↙, →
                val dirMode = rng.nextInt(3)
                val startX: Float
                val startY: Float
                val angleDeg: Float

                when (dirMode) {
                    0 -> {
                        // ↘ (Down-Right)
                        startX = 0.05f + rng.nextFloat() * 0.45f
                        startY = 0.03f + rng.nextFloat() * 0.22f
                        angleDeg = 28f + rng.nextFloat() * 18f // 28° .. 46°
                    }
                    1 -> {
                        // ↙ (Down-Left)
                        startX = 0.50f + rng.nextFloat() * 0.45f
                        startY = 0.03f + rng.nextFloat() * 0.22f
                        angleDeg = 134f + rng.nextFloat() * 18f // 134° .. 152°
                    }
                    else -> {
                        // → (Shallow across)
                        startX = 0.03f + rng.nextFloat() * 0.30f
                        startY = 0.06f + rng.nextFloat() * 0.25f
                        angleDeg = 8f + rng.nextFloat() * 12f // 8° .. 20°
                    }
                }
                val angleRad = (angleDeg * PI / 180f).toFloat()

                val travelDistancePx = (if (isLoginMode) 130.dp else (190f + rng.nextFloat() * 50f).dp).toPx()
                val trailLengthPx = (if (isLoginMode) 34.dp else 52.dp).toPx()
                val strokeWidthPx = (if (isLoginMode) 0.85.dp else 1.15.dp).toPx()
                val peakAlpha = if (isLoginMode) 0.30f else 0.58f

                // Color tint: Mostly white, occasionally slight warm/golden tint
                val hasWarmTint = rng.nextFloat() < 0.25f
                val headColor = if (hasWarmTint) Color(0xFFFFFDF5) else Color.White
                val tailColor = if (hasWarmTint) Color(0xFFE5C78A) else Color(0xFFE2E4E9)

                // Smooth bell curve fade: 600-1000ms duration
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
                            tailColor.copy(alpha = starAlpha * 0.35f),
                            tailColor.copy(alpha = starAlpha * 0.70f),
                            headColor.copy(alpha = starAlpha)
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
                        color = headColor.copy(alpha = starAlpha),
                        radius = strokeWidthPx * 1.15f,
                        center = headOffset
                    )
                }
            }
        }

        // 5. LAYER 4: LIVING SECONDARY STAR FIELD (200 Stars)
        // Natural twinkling, breathing animation, individual timing, star birth effect & 1-3px drift
        AmbientUniverseData.secondaryStars.forEach { star ->
            // Individual breathing & harmonic twinkling
            val twinkleSpeed = (2 * PI / star.cycleDurationSec).toFloat()
            val phase = (timeSeconds * twinkleSpeed + star.phaseOffset) % (2 * PI.toFloat())
            val rawSine = (sin(phase) + 1f) / 2f
            val harmonic = (sin(timeSeconds * twinkleSpeed * star.harmonicMultiplier + star.phaseOffset * 1.4f) + 1f) / 2f
            val breathFactor = rawSine * 0.82f + harmonic * 0.18f

            val baseOpacity = star.minAlpha + (star.maxAlpha - star.minAlpha) * breathFactor

            // Star birth effect: Disappear completely -> slowly appear -> become visible -> fade again
            val birthEnvelope = if (star.hasStarBirth && star.birthCycleSec > 0f && !reduceMotion) {
                val bSpeed = (2 * PI / star.birthCycleSec).toFloat()
                val bRaw = sin(timeSeconds * bSpeed + star.birthPhase)
                if (bRaw <= 0f) {
                    0f // Disappear completely into darkness
                } else {
                    (sin(bRaw * (PI / 2f).toFloat())).pow(1.6f)
                }
            } else {
                1.0f
            }

            if (birthEnvelope <= 0.005f) return@forEach

            // Introduction Screen awakening: Stars gently appear over the first ~2 seconds
            val awakeningFactor = if (mode == AmbientUniverseMode.FEATURE_INTRO && !reduceMotion && star.awakeningDelaySec > 0f) {
                if (timeSeconds < star.awakeningDelaySec) {
                    0f
                } else {
                    ((timeSeconds - star.awakeningDelaySec) / 0.8f).coerceIn(0f, 1f)
                }
            } else {
                1.0f
            }

            if (awakeningFactor <= 0.005f) return@forEach

            // 1–3 pixel subtle life-like floating movement (different speed & direction per star)
            val driftX = if (reduceMotion) 0f else sin(timeSeconds * star.driftSpeedX + star.phaseOffset) * star.driftRadiusPx
            val driftY = if (reduceMotion) 0f else cos(timeSeconds * star.driftSpeedY + star.phaseOffset) * star.driftRadiusPx

            val pointX = canvasWidth * star.relX + driftX
            val pointY = canvasHeight * star.relY + driftY
            val centerOffset = Offset(pointX, pointY)

            // Extra dampening in central card/form area during Login/Register for flawless form legibility
            val isInCentralFormArea = isLoginMode &&
                    star.relX in 0.18f..0.82f && star.relY in 0.32f..0.74f
            val formDampening = if (isInCentralFormArea) 0.55f else 1.0f

            val currentAlpha = (baseOpacity * birthEnvelope * awakeningFactor * initialAlpha * starAlphaScale * formDampening)
                .coerceIn(0f, 0.95f)

            if (currentAlpha <= 0.01f) return@forEach

            val starRadiusPx = star.radiusDp.dp.toPx()

            // Crisp star core (no glow/gradients)
            drawCircle(
                color = secondaryStarColor.copy(alpha = currentAlpha),
                radius = starRadiusPx,
                center = centerOffset
            )
        }

        // 6. LAYER 5: EXACTLY FIVE PRIMARY HERO STARS
        if (showPrimaryLights) {
            AmbientUniverseData.primaryStars.forEach { star ->
                val twinkleSpeed = (2 * PI / star.cycleDurationSec).toFloat()
                val phase = (timeSeconds * twinkleSpeed + star.phaseOffset) % (2 * PI.toFloat())
                val rawSine = (sin(phase) + 1f) / 2f
                val twinkle = (sin(timeSeconds * twinkleSpeed * star.harmonicMultiplier + star.phaseOffset * 1.3f) + 1f) / 2f
                val combinedPulse = rawSine * 0.80f + twinkle * 0.20f

                // Subtle breathing scale: 0.98 -> 1.04 -> 0.98
                val breathScale = 0.98f + 0.06f * combinedPulse
                // Subtle brightness breathing: 50% -> 100% -> 50%
                val breathBrightness = 0.50f + 0.50f * combinedPulse

                // Subtle breathing movement
                val driftX = if (reduceMotion) 0f else {
                    sin(timeSeconds * star.driftSpeedX + star.phaseOffset) * star.driftRadiusPx * density.density
                }
                val driftY = if (reduceMotion) 0f else {
                    cos(timeSeconds * star.driftSpeedY + star.phaseOffset) * star.driftRadiusPx * density.density
                }

                val pointX = canvasWidth * star.relX + driftX
                val pointY = canvasHeight * star.relY + driftY
                val centerOffset = Offset(pointX, pointY)

                val primaryAlphaMult = breathBrightness * initialAlpha * (if (isLoginMode) 0.80f else 1.0f)

                // Layer A: Micro-bloom Halo (Radius ~ 32dp * breathScale)
                val bloomRadius = primaryBloomRadiusPx * breathScale * (0.92f + 0.08f * combinedPulse)
                val bloomBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryBloomColor.copy(alpha = 0.28f * primaryAlphaMult),
                        primaryBloomColor.copy(alpha = 0.10f * primaryAlphaMult),
                        primaryBloomColor.copy(alpha = 0.02f * primaryAlphaMult),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = bloomRadius
                )
                drawCircle(
                    brush = bloomBrush,
                    radius = bloomRadius,
                    center = centerOffset
                )

                // Layer B: Soft Atmospheric Aura (Radius ~ 22dp * breathScale)
                val auraRadius = primaryAuraRadiusPx * breathScale * (0.91f + 0.09f * combinedPulse)
                val auraBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryAuraColor.copy(alpha = 0.38f * primaryAlphaMult),
                        primaryAuraColor.copy(alpha = 0.12f * primaryAlphaMult),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = auraRadius
                )
                drawCircle(
                    brush = auraBrush,
                    radius = auraRadius,
                    center = centerOffset
                )

                // Layer C: Outer Concentrated Glow (Radius ~ 11dp * breathScale)
                val outerGlowRadius = primaryOuterGlowRadiusPx * breathScale * (0.93f + 0.07f * combinedPulse)
                val outerGlowBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryOuterGlowColor.copy(alpha = 0.65f * primaryAlphaMult),
                        primaryOuterGlowColor.copy(alpha = 0.20f * primaryAlphaMult),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = outerGlowRadius
                )
                drawCircle(
                    brush = outerGlowBrush,
                    radius = outerGlowRadius,
                    center = centerOffset
                )

                // Layer D: Inner Luminous Glow (Radius ~ 5.5dp * breathScale)
                val innerGlowRadius = primaryInnerGlowRadiusPx * breathScale * (0.96f + 0.04f * combinedPulse)
                val innerGlowBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryInnerGlowColor.copy(alpha = 0.85f * primaryAlphaMult),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = innerGlowRadius
                )
                drawCircle(
                    brush = innerGlowBrush,
                    radius = innerGlowRadius,
                    center = centerOffset
                )

                // Layer E: 4-Point Optical Diffraction Flare (Subtle Thin Celestial Cross)
                val currentFlareArm = flareArmLengthPx * breathScale * (0.88f + 0.12f * combinedPulse)
                val currentFlareAlpha = (0.28f + 0.16f * combinedPulse) * primaryAlphaMult

                // Horizontal flare arm
                drawLine(
                    color = primaryFlareColor.copy(alpha = currentFlareAlpha),
                    start = Offset(pointX - currentFlareArm, pointY),
                    end = Offset(pointX + currentFlareArm, pointY),
                    strokeWidth = flareStrokePx
                )
                // Vertical flare arm
                drawLine(
                    color = primaryFlareColor.copy(alpha = currentFlareAlpha),
                    start = Offset(pointX, pointY - currentFlareArm),
                    end = Offset(pointX, pointY + currentFlareArm),
                    strokeWidth = flareStrokePx
                )

                // Layer F: Crisp Refined Core (Radius ~ 2.4dp * breathScale)
                val coreRadiusPx = (star.radiusDp.dp.toPx()) * breathScale * (0.96f + 0.06f * combinedPulse)
                drawCircle(
                    color = primaryCoreColor.copy(alpha = 0.96f * primaryAlphaMult),
                    radius = coreRadiusPx,
                    center = centerOffset
                )

                // Layer G: Center Highlight Point in Light Mode
                if (!isDark) {
                    drawCircle(
                        color = primaryCoreHighlightColor.copy(alpha = 0.90f * primaryAlphaMult),
                        radius = coreRadiusPx * 0.45f,
                        center = centerOffset
                    )
                }
            }
        }

        // 7. LAYER 6: VERY RARE FEATURE TRANSITION LIGHT STREAK (Extremely subtle, 650ms)
        if (streakActive && !reduceMotion) {
            val streakElapsed = rawTime - lastStreakStartTime
            if (streakElapsed in 0f..0.65f) {
                val streakProgress = streakElapsed / 0.65f
                val streakAlpha = (sin(streakProgress * PI.toFloat()) * (if (isDark) 0.18f else 0.12f)).coerceIn(0f, 0.20f)
                val streakY = canvasHeight * 0.18f
                val streakLen = 90.dp.toPx()
                val streakStartX = canvasWidth * 0.10f + (canvasWidth * 0.55f) * streakProgress
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
                    strokeWidth = 1.0.dp.toPx(),
                    cap = StrokeCap.Round
                )
            } else if (streakElapsed > 0.65f) {
                streakActive = false
            }
        }
    }
}
