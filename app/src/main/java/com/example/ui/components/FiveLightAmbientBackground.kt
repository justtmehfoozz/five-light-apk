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
    val primaryStars: List<AmbientStar> = listOf(
        AmbientStar(
            relX = 0.18f, relY = 0.13f, radiusDp = 2.4f,
            minAlpha = 0.80f, maxAlpha = 0.98f, cycleDurationSec = 7.4f,
            phaseOffset = 0.0f, harmonicMultiplier = 2.2f,
            driftSpeedX = 0.08f, driftSpeedY = 0.06f, driftRadiusPx = 0.6f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.82f, relY = 0.16f, radiusDp = 2.4f,
            minAlpha = 0.80f, maxAlpha = 0.98f, cycleDurationSec = 9.2f,
            phaseOffset = 1.35f, harmonicMultiplier = 1.8f,
            driftSpeedX = 0.07f, driftSpeedY = 0.09f, driftRadiusPx = 0.6f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.50f, relY = 0.26f, radiusDp = 2.5f,
            minAlpha = 0.82f, maxAlpha = 1.00f, cycleDurationSec = 6.2f,
            phaseOffset = 2.70f, harmonicMultiplier = 2.4f,
            driftSpeedX = 0.09f, driftSpeedY = 0.07f, driftRadiusPx = 0.7f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.14f, relY = 0.42f, radiusDp = 2.3f,
            minAlpha = 0.78f, maxAlpha = 0.95f, cycleDurationSec = 8.3f,
            phaseOffset = 4.05f, harmonicMultiplier = 1.7f,
            driftSpeedX = 0.06f, driftSpeedY = 0.08f, driftRadiusPx = 0.5f,
            isPrimary = true, layer = 3, hasHalo = true
        ),
        AmbientStar(
            relX = 0.86f, relY = 0.38f, radiusDp = 2.3f,
            minAlpha = 0.78f, maxAlpha = 0.95f, cycleDurationSec = 10.5f,
            phaseOffset = 5.40f, harmonicMultiplier = 2.0f,
            driftSpeedX = 0.08f, driftSpeedY = 0.05f, driftRadiusPx = 0.5f,
            isPrimary = true, layer = 3, hasHalo = true
        )
    )

    /**
     * Deterministic catalog of 96 secondary stars.
     * Pre-allocated once at class loading time to ensure ZERO runtime allocations per frame.
     *
     * Distribution:
     * - 60% Small / Dust stars: 15% -> 35% -> 15%, 4–7s cycle
     * - 25% Mid stars: 20% -> 50% -> 20%, 5–8.5s cycle
     * - 15% Brighter stars: 35% -> 75% -> 35%, 6–10s cycle with soft luminous halo
     *
     * Star Birth:
     * - ~25% of stars possess an organic 20–36s birth-and-fade envelope (0% -> faint -> normal -> fade)
     *
     * Subtle Star Movement:
     * - 1–2 pixel drifting with independent direction and velocity per star
     */
    val secondaryStars: List<AmbientStar> by lazy {
        val random = Random(19950711L)
        val list = ArrayList<AmbientStar>(96)

        var count = 0
        while (count < 96) {
            val rx = 0.03f + random.nextFloat() * 0.94f
            val ry = 0.04f + random.nextFloat() * 0.92f

            // Negative space check: Keep center region sparse so cards and forms are prominent
            val inCentralCardArea = (rx in 0.22f..0.78f && ry in 0.30f..0.70f)
            if (inCentralCardArea && random.nextFloat() > 0.18f) {
                continue
            }

            val cat = count % 10
            val radiusDp: Float
            val minAlpha: Float
            val maxAlpha: Float
            val cycleSec: Float
            val hasHalo: Boolean
            val layer: Int

            when {
                cat == 0 || cat == 5 -> {
                    // Brighter stars (15%): 35% -> 75% -> 35%, 6–10 second cycle
                    radiusDp = 1.4f + random.nextFloat() * 0.38f
                    minAlpha = 0.33f + random.nextFloat() * 0.05f
                    maxAlpha = 0.70f + random.nextFloat() * 0.07f
                    cycleSec = 6.0f + random.nextFloat() * 4.0f
                    hasHalo = true
                    layer = 2
                }
                cat in 1..3 -> {
                    // Mid stars (25%): 20% -> 50% -> 20%, 5–8.5 second cycle
                    radiusDp = 1.0f + random.nextFloat() * 0.32f
                    minAlpha = 0.20f + random.nextFloat() * 0.05f
                    maxAlpha = 0.48f + random.nextFloat() * 0.06f
                    cycleSec = 5.0f + random.nextFloat() * 3.5f
                    hasHalo = false
                    layer = 1
                }
                else -> {
                    // Small / Dust stars (60%): 15% -> 35% -> 15%, 4–7 second cycle
                    radiusDp = 0.6f + random.nextFloat() * 0.32f
                    minAlpha = 0.14f + random.nextFloat() * 0.04f
                    maxAlpha = 0.32f + random.nextFloat() * 0.05f
                    cycleSec = 4.0f + random.nextFloat() * 3.0f
                    hasHalo = false
                    layer = 0
                }
            }

            val phaseOffset = (random.nextFloat() * 2 * PI).toFloat()
            val twinkleFreq = 1.2f + random.nextFloat() * 1.2f

            // 1–2 pixel drifting with different speed & direction per star
            val driftSpeedX = (0.18f + random.nextFloat() * 0.28f) * (if (random.nextBoolean()) 1f else -1f)
            val driftSpeedY = (0.14f + random.nextFloat() * 0.26f) * (if (random.nextBoolean()) 1f else -1f)
            val driftRadiusPx = 0.8f + random.nextFloat() * 1.0f // 0.8px .. 1.8px (1-2px)

            // Star birth effect: ~25% of stars slowly fade from 0% -> faint -> normal -> slow fade
            val hasStarBirth = (count % 4 == 2)
            val birthCycleSec = if (hasStarBirth) 20.0f + random.nextFloat() * 16.0f else 0f
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
        targetValue = if (isDark) Color(0xFF050408) else Color(0xFFFAF7F2),
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

        // 2. LAYER 1: ATMOSPHERIC DEPTH LIGHTS (Slow diffused cosmic haze)
        AmbientUniverseData.depthLights.forEachIndexed { index, light ->
            val color = when (index % 3) {
                0 -> depthLightColor1
                1 -> depthLightColor2
                else -> depthLightColor3
            }

            val dPhase = (timeSeconds * light.pulseSpeed + light.phaseOffset) % (2 * PI.toFloat())
            val dPulse = (sin(dPhase) + 1f) / 2f

            val baseAlpha = if (isDark) light.baseAlphaDark else light.baseAlphaLight
            val dAlpha = (baseAlpha * (0.85f + 0.30f * dPulse) * initialAlpha * depthAlphaScale).coerceIn(0.01f, 0.25f)

            // Orbital sub-pixel drift for photographic depth
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
        // Feature Introduction: 15–30s interval, duration 650–950ms.
        // Login / Register: 12–20s interval, duration 600–850ms, thinner (0.85dp) & softer (alpha 0.36).
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

                val travelDistancePx = (if (isLoginMode) 140.dp else 220.dp).toPx()
                val trailLengthPx = (if (isLoginMode) 38.dp else 62.dp).toPx()
                val strokeWidthPx = (if (isLoginMode) 0.85.dp else 1.25.dp).toPx()
                val peakAlpha = if (isLoginMode) 0.36f else 0.65f

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

                    // Micro-halo at the head in Feature Intro
                    if (!isLoginMode && starAlpha > 0.28f) {
                        val haloRadius = strokeWidthPx * 3.5f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    shootingStarTailColor.copy(alpha = starAlpha * 0.30f),
                                    Color.Transparent
                                ),
                                center = headOffset,
                                radius = haloRadius
                            ),
                            radius = haloRadius,
                            center = headOffset
                        )
                    }
                }
            }
        }

        // 5. LAYER 4: LIVING SECONDARY STAR FIELD (~96 Stars)
        // Natural twinkling, breathing animation, individual timing, star birth effect & 1-2px drift
        AmbientUniverseData.secondaryStars.forEach { star ->
            // Individual breathing & harmonic twinkling
            val twinkleSpeed = (2 * PI / star.cycleDurationSec).toFloat()
            val phase = (timeSeconds * twinkleSpeed + star.phaseOffset) % (2 * PI.toFloat())
            val rawSine = (sin(phase) + 1f) / 2f
            val harmonic = (sin(timeSeconds * twinkleSpeed * star.harmonicMultiplier + star.phaseOffset * 1.6f) + 1f) / 2f
            val breathFactor = rawSine * 0.82f + harmonic * 0.18f

            val baseOpacity = star.minAlpha + (star.maxAlpha - star.minAlpha) * breathFactor

            // Star birth effect: 0% -> faint -> normal -> slow fade
            val birthEnvelope = if (star.hasStarBirth && star.birthCycleSec > 0f) {
                val bSpeed = (2 * PI / star.birthCycleSec).toFloat()
                val bRaw = (sin(timeSeconds * bSpeed + star.birthPhase) + 1f) / 2f
                bRaw.pow(2.6f)
            } else {
                1.0f
            }

            if (birthEnvelope <= 0.01f) return@forEach

            // 1–2 pixel subtle life-like drifting (different speed & direction per star)
            val driftX = if (reduceMotion) 0f else sin(timeSeconds * star.driftSpeedX + star.phaseOffset) * star.driftRadiusPx
            val driftY = if (reduceMotion) 0f else cos(timeSeconds * star.driftSpeedY + star.phaseOffset) * star.driftRadiusPx

            val pointX = canvasWidth * star.relX + driftX
            val pointY = canvasHeight * star.relY + driftY
            val centerOffset = Offset(pointX, pointY)

            // Extra dampening in central card/form area during Login/Register for flawless form legibility
            val isInCentralFormArea = isLoginMode &&
                    star.relX in 0.16f..0.84f && star.relY in 0.32f..0.74f
            val formDampening = if (isInCentralFormArea) 0.55f else 1.0f

            val currentAlpha = (baseOpacity * birthEnvelope * initialAlpha * starAlphaScale * formDampening)
                .coerceIn(0.04f, 0.90f)
            val starRadiusPx = star.radiusDp.dp.toPx()

            // Subtle luminous halo around brighter stars
            if (star.hasHalo && currentAlpha > 0.28f) {
                val haloRadius = starRadiusPx * 2.8f
                val haloBrush = Brush.radialGradient(
                    colors = listOf(
                        secondaryStarGlowColor.copy(alpha = currentAlpha * 0.38f),
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
                color = secondaryStarColor.copy(alpha = currentAlpha),
                radius = starRadiusPx,
                center = centerOffset
            )
        }

        // 6. LAYER 5: EXACTLY FIVE PRIMARY LIGHTS (In FEATURE_INTRO mode with micro-bloom and diffraction flare)
        if (showPrimaryLights) {
            AmbientUniverseData.primaryStars.forEach { star ->
                val twinkleSpeed = (2 * PI / star.cycleDurationSec).toFloat()
                val phase = (timeSeconds * twinkleSpeed + star.phaseOffset) % (2 * PI.toFloat())
                val rawSine = (sin(phase) + 1f) / 2f
                val twinkle = (sin(timeSeconds * twinkleSpeed * star.harmonicMultiplier + star.phaseOffset) + 1f) / 2f
                val combinedPulse = rawSine * 0.78f + twinkle * 0.22f

                // Subtle optical lock drift
                val driftX = if (reduceMotion) 0f else {
                    sin(timeSeconds * star.driftSpeedX + star.phaseOffset) * star.driftRadiusPx * density.density
                }
                val driftY = if (reduceMotion) 0f else {
                    cos(timeSeconds * star.driftSpeedY + star.phaseOffset) * star.driftRadiusPx * density.density
                }

                val pointX = canvasWidth * star.relX + driftX
                val pointY = canvasHeight * star.relY + driftY
                val centerOffset = Offset(pointX, pointY)

                val primaryAlphaMult = (0.80f + 0.20f * combinedPulse) * initialAlpha

                // Layer A: Micro-bloom Halo (Radius ~ 32dp)
                val bloomRadius = primaryBloomRadiusPx * (0.92f + 0.08f * combinedPulse)
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

                // Layer B: Soft Atmospheric Aura (Radius ~ 22dp)
                val auraRadius = primaryAuraRadiusPx * (0.91f + 0.09f * combinedPulse)
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

                // Layer C: Outer Concentrated Glow (Radius ~ 11dp)
                val outerGlowRadius = primaryOuterGlowRadiusPx * (0.93f + 0.07f * combinedPulse)
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

                // Layer D: Inner Luminous Glow (Radius ~ 5.5dp)
                val innerGlowRadius = primaryInnerGlowRadiusPx * (0.96f + 0.04f * combinedPulse)
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
                val currentFlareArm = flareArmLengthPx * (0.88f + 0.12f * combinedPulse)
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

                // Layer F: Crisp Refined Core (Radius ~ 2.4dp)
                val coreRadiusPx = (star.radiusDp.dp.toPx()) * (0.96f + 0.06f * combinedPulse)
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
