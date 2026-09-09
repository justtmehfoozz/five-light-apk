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
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Operating atmosphere mode for FiveLight ambient background.
 */
enum class AmbientUniverseMode {
    /**
     * Cinematic, alive, full ambient presence for onboarding / prelude.
     */
    PRELUDE,

    /**
     * Serene, slower, calmer atmosphere supporting login / register forms with zero distraction.
     */
    LOGIN_REGISTER
}

/**
 * Astronomical behavior classification for secondary stars.
 */
enum class StarTwinkleType {
    /**
     * Tranquil, steady star with subtle micro-luminosity fluctuation (~30% of stars).
     */
    STEADY,

    /**
     * Smooth 3.5s - 7.5s sinusoidal breathing cycle (~55% of stars).
     */
    BREATHING,

    /**
     * Infrequent, slow, gentle 10s - 15s luminous flare peak (~15% of stars).
     */
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
 * Data representation of a star in the FiveLight ambient star field.
 */
data class AmbientStar(
    val relX: Float,          // Relative X position (0.0 to 1.0)
    val relY: Float,          // Relative Y position (0.0 to 1.0)
    val radiusDp: Float,      // Core radius in dp (0.7dp to 2.4dp)
    val baseAlpha: Float,     // Base opacity
    val twinkleType: StarTwinkleType,
    val pulseSpeed: Float,    // Animation breathing speed factor
    val twinkleFreq: Float,   // Twinkle harmonic multiplier
    val phaseOffset: Float,   // Phase shift in radians
    val isPrimary: Boolean,   // True for EXACTLY 5 primary lights
    val layer: Int            // 0 = distant dim, 1 = mid secondary, 2 = near secondary, 3 = primary
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
 * Ensures star positions, breathing phases, and atmospheric depth lights never jump or reset
 * during navigation between Prelude, Pre-Login, Login, and Register screens.
 */
object AmbientUniverseClock {
    private val appStartTimeNanos: Long = System.nanoTime()

    fun getElapsedSeconds(): Float {
        return (System.nanoTime() - appStartTimeNanos) / 1_000_000_000f
    }
}

/**
 * Deterministic catalog of celestial bodies ensuring zero reshuffling across compositions.
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
            relX = 0.18f, relY = 0.13f, radiusDp = 2.4f, baseAlpha = 0.95f,
            twinkleType = StarTwinkleType.BREATHING, pulseSpeed = 0.85f, twinkleFreq = 2.2f,
            phaseOffset = 0.0f, isPrimary = true, layer = 3
        ),
        AmbientStar(
            relX = 0.82f, relY = 0.16f, radiusDp = 2.4f, baseAlpha = 0.95f,
            twinkleType = StarTwinkleType.BREATHING, pulseSpeed = 0.68f, twinkleFreq = 1.8f,
            phaseOffset = 1.35f, isPrimary = true, layer = 3
        ),
        AmbientStar(
            relX = 0.50f, relY = 0.26f, radiusDp = 2.5f, baseAlpha = 0.98f,
            twinkleType = StarTwinkleType.BREATHING, pulseSpeed = 1.02f, twinkleFreq = 2.4f,
            phaseOffset = 2.70f, isPrimary = true, layer = 3
        ),
        AmbientStar(
            relX = 0.14f, relY = 0.42f, radiusDp = 2.3f, baseAlpha = 0.92f,
            twinkleType = StarTwinkleType.BREATHING, pulseSpeed = 0.76f, twinkleFreq = 1.7f,
            phaseOffset = 4.05f, isPrimary = true, layer = 3
        ),
        AmbientStar(
            relX = 0.86f, relY = 0.38f, radiusDp = 2.3f, baseAlpha = 0.92f,
            twinkleType = StarTwinkleType.BREATHING, pulseSpeed = 0.60f, twinkleFreq = 2.0f,
            phaseOffset = 5.40f, isPrimary = true, layer = 3
        )
    )

    // ~44 Asymmetrically placed secondary stars with deliberate negative space over UI reading areas
    val secondaryStars: List<AmbientStar> = listOf(
        // Constellation 1: Upper-Left Sky
        AmbientStar(0.06f, 0.06f, 1.1f, 0.55f, StarTwinkleType.STEADY, 0.45f, 1.1f, 0.3f, false, 0),
        AmbientStar(0.12f, 0.09f, 1.4f, 0.68f, StarTwinkleType.BREATHING, 1.25f, 1.6f, 1.8f, false, 1),
        AmbientStar(0.22f, 0.05f, 0.9f, 0.45f, StarTwinkleType.STEADY, 0.35f, 0.9f, 3.2f, false, 0),
        AmbientStar(0.09f, 0.17f, 1.6f, 0.78f, StarTwinkleType.RARE_LUMINOUS, 0.52f, 1.4f, 4.6f, false, 2),
        AmbientStar(0.27f, 0.12f, 1.2f, 0.62f, StarTwinkleType.BREATHING, 1.05f, 1.8f, 0.8f, false, 1),
        AmbientStar(0.05f, 0.25f, 0.8f, 0.40f, StarTwinkleType.STEADY, 0.40f, 1.0f, 2.1f, false, 0),
        AmbientStar(0.21f, 0.22f, 1.5f, 0.72f, StarTwinkleType.BREATHING, 0.95f, 1.5f, 5.2f, false, 2),
        AmbientStar(0.15f, 0.30f, 1.0f, 0.48f, StarTwinkleType.STEADY, 0.50f, 1.2f, 1.4f, false, 0),
        AmbientStar(0.31f, 0.19f, 1.3f, 0.65f, StarTwinkleType.RARE_LUMINOUS, 0.48f, 1.3f, 3.8f, false, 1),

        // Constellation 2: Upper-Right Sky
        AmbientStar(0.69f, 0.06f, 1.2f, 0.60f, StarTwinkleType.BREATHING, 1.15f, 1.7f, 2.5f, false, 1),
        AmbientStar(0.76f, 0.04f, 0.8f, 0.42f, StarTwinkleType.STEADY, 0.38f, 0.9f, 4.1f, false, 0),
        AmbientStar(0.86f, 0.07f, 1.5f, 0.76f, StarTwinkleType.RARE_LUMINOUS, 0.55f, 1.5f, 0.6f, false, 2),
        AmbientStar(0.94f, 0.11f, 1.0f, 0.50f, StarTwinkleType.STEADY, 0.42f, 1.1f, 2.9f, false, 0),
        AmbientStar(0.73f, 0.14f, 1.4f, 0.70f, StarTwinkleType.BREATHING, 0.90f, 1.9f, 5.0f, false, 1),
        AmbientStar(0.91f, 0.19f, 1.6f, 0.80f, StarTwinkleType.BREATHING, 1.35f, 2.1f, 1.2f, false, 2),
        AmbientStar(0.64f, 0.21f, 0.9f, 0.45f, StarTwinkleType.STEADY, 0.48f, 1.0f, 3.4f, false, 0),
        AmbientStar(0.79f, 0.23f, 1.3f, 0.64f, StarTwinkleType.STEADY, 0.55f, 1.3f, 0.4f, false, 1),
        AmbientStar(0.95f, 0.27f, 1.1f, 0.58f, StarTwinkleType.RARE_LUMINOUS, 0.42f, 1.2f, 2.2f, false, 1),
        AmbientStar(0.70f, 0.31f, 1.4f, 0.68f, StarTwinkleType.BREATHING, 1.10f, 1.6f, 4.7f, false, 2),

        // Left Margin Stream (relX 0.04f to 0.16f, relY 0.35f to 0.70f)
        AmbientStar(0.05f, 0.38f, 1.2f, 0.60f, StarTwinkleType.BREATHING, 0.98f, 1.4f, 1.9f, false, 1),
        AmbientStar(0.11f, 0.46f, 0.9f, 0.42f, StarTwinkleType.STEADY, 0.36f, 1.0f, 3.7f, false, 0),
        AmbientStar(0.04f, 0.54f, 1.5f, 0.74f, StarTwinkleType.RARE_LUMINOUS, 0.46f, 1.5f, 5.8f, false, 2),
        AmbientStar(0.12f, 0.62f, 1.1f, 0.58f, StarTwinkleType.BREATHING, 1.20f, 1.8f, 2.1f, false, 1),
        AmbientStar(0.06f, 0.68f, 0.8f, 0.40f, StarTwinkleType.STEADY, 0.44f, 0.9f, 0.9f, false, 0),

        // Right Margin Stream (relX 0.84f to 0.96f, relY 0.35f to 0.70f)
        AmbientStar(0.95f, 0.36f, 1.3f, 0.62f, StarTwinkleType.BREATHING, 1.02f, 1.6f, 4.2f, false, 1),
        AmbientStar(0.89f, 0.45f, 1.6f, 0.78f, StarTwinkleType.RARE_LUMINOUS, 0.50f, 1.3f, 1.1f, false, 2),
        AmbientStar(0.96f, 0.53f, 0.9f, 0.44f, StarTwinkleType.STEADY, 0.38f, 1.1f, 3.0f, false, 0),
        AmbientStar(0.88f, 0.61f, 1.4f, 0.70f, StarTwinkleType.BREATHING, 1.18f, 1.7f, 5.4f, false, 2),
        AmbientStar(0.94f, 0.69f, 1.0f, 0.48f, StarTwinkleType.STEADY, 0.40f, 1.0f, 2.7f, false, 0),

        // Lower Ambient Horizon (relY 0.74f to 0.95f)
        AmbientStar(0.14f, 0.76f, 1.2f, 0.56f, StarTwinkleType.BREATHING, 1.05f, 1.5f, 0.5f, false, 1),
        AmbientStar(0.24f, 0.82f, 0.9f, 0.42f, StarTwinkleType.STEADY, 0.35f, 0.9f, 2.6f, false, 0),
        AmbientStar(0.08f, 0.86f, 1.4f, 0.72f, StarTwinkleType.RARE_LUMINOUS, 0.44f, 1.4f, 4.4f, false, 2),
        AmbientStar(0.32f, 0.89f, 1.0f, 0.50f, StarTwinkleType.BREATHING, 0.92f, 1.6f, 1.5f, false, 0),
        AmbientStar(0.18f, 0.93f, 0.8f, 0.38f, StarTwinkleType.STEADY, 0.40f, 1.0f, 3.9f, false, 0),
        AmbientStar(0.72f, 0.77f, 1.3f, 0.62f, StarTwinkleType.BREATHING, 1.14f, 1.8f, 5.1f, false, 1),
        AmbientStar(0.85f, 0.79f, 1.1f, 0.52f, StarTwinkleType.STEADY, 0.46f, 1.2f, 0.7f, false, 0),
        AmbientStar(0.64f, 0.85f, 0.9f, 0.44f, StarTwinkleType.STEADY, 0.36f, 0.9f, 2.8f, false, 0),
        AmbientStar(0.78f, 0.88f, 1.5f, 0.74f, StarTwinkleType.RARE_LUMINOUS, 0.48f, 1.3f, 4.9f, false, 2),
        AmbientStar(0.91f, 0.92f, 0.8f, 0.40f, StarTwinkleType.BREATHING, 1.22f, 1.9f, 1.6f, false, 0),
        AmbientStar(0.42f, 0.92f, 1.1f, 0.54f, StarTwinkleType.STEADY, 0.52f, 1.1f, 3.3f, false, 1),
        AmbientStar(0.56f, 0.94f, 0.9f, 0.45f, StarTwinkleType.BREATHING, 0.88f, 1.4f, 5.7f, false, 0),

        // Deep Minimal Backdrop (Subtle distant pinpricks in negative space zones)
        AmbientStar(0.38f, 0.33f, 0.7f, 0.24f, StarTwinkleType.STEADY, 0.30f, 0.8f, 1.2f, false, 0),
        AmbientStar(0.62f, 0.35f, 0.8f, 0.26f, StarTwinkleType.STEADY, 0.32f, 0.8f, 3.5f, false, 0),
        AmbientStar(0.40f, 0.66f, 0.7f, 0.20f, StarTwinkleType.STEADY, 0.28f, 0.8f, 2.0f, false, 0),
        AmbientStar(0.60f, 0.68f, 0.7f, 0.20f, StarTwinkleType.STEADY, 0.28f, 0.8f, 4.2f, false, 0)
    )

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
 * Native Jetpack Compose Ambient "Five Lights" Background.
 *
 * Visual System:
 * - 3 Atmospheric Depth Lights behind the star field with soft moonlight haze.
 * - Natural secondary star field with 3 distinct twinkling dynamics (steady, breathing, rare luminous).
 * - Art-directed asymmetric distribution with generous negative space over UI reading areas.
 * - EXACTLY FIVE refined luminous primary lights with subtle micro-bloom halo & diffraction cross.
 * - Ultra-subtle floating cosmic particles adding photographic depth.
 * - Monotonic universe clock ensuring 100% transition continuity with zero resets.
 * - Theme adaptive (Dark Mode cosmic night vs Light Mode warm parchment).
 * - High performance: lifecycle-aware frame loop, zero composable recomposition.
 */
@Composable
fun FiveLightAmbientBackground(
    modifier: Modifier = Modifier,
    initialAlpha: Float = 1.0f,
    mode: AmbientUniverseMode = AmbientUniverseMode.PRELUDE,
    showPrimaryLights: Boolean = (mode == AmbientUniverseMode.PRELUDE)
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

    // 1. Color System Definitions with smooth 700ms transitions
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
        targetValue = if (isDark) Color(0xFFB6AFD1) else Color(0xFF887D6E),
        animationSpec = tweenSpec,
        label = "particleColorAnim"
    )

    // Primary 5 Lights Palette (Small, luminous, refined, sacred)
    val primaryCoreColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF262018),
        animationSpec = tweenSpec,
        label = "primaryCoreAnim"
    )

    val primaryCoreHighlightColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFFFFFDF8),
        animationSpec = tweenSpec,
        label = "primaryCoreHighlightAnim"
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
    val speedScale = if (mode == AmbientUniverseMode.LOGIN_REGISTER) 0.55f else 1.0f
    val depthAlphaScale = if (mode == AmbientUniverseMode.LOGIN_REGISTER) 0.82f else 1.0f
    val starAlphaScale = if (mode == AmbientUniverseMode.LOGIN_REGISTER) 0.85f else 1.0f

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

        // 2. ATMOSPHERIC DEPTH LIGHT LAYERS (Behind star field)
        AmbientUniverseData.depthLights.forEachIndexed { index, light ->
            val color = when (index % 3) {
                0 -> depthLightColor1
                1 -> depthLightColor2
                else -> depthLightColor3
            }

            val dPhase = (timeSeconds * light.pulseSpeed + light.phaseOffset) % (2 * PI.toFloat())
            val dPulse = (sin(dPhase) + 1f) / 2f // 0.0f .. 1.0f

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

            // 4-stop radial gradient with smooth quadratic falloff for zero visible banding
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

        // 3. SUBTLE FLOATING COSMIC PARTICLES (Drifting celestial dust)
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
            val particleAlpha = (particle.baseAlpha * pPulse * initialAlpha * (if (mode == AmbientUniverseMode.LOGIN_REGISTER) 0.65f else 1.0f))
                .coerceIn(0.02f, 0.22f)
            val pRadius = particle.radiusDp.dp.toPx()

            drawCircle(
                color = particleColor.copy(alpha = particleAlpha),
                radius = pRadius,
                center = Offset(px, py)
            )
        }

        // 4. SECONDARY STAR FIELD (Art-directed asymmetric distribution)
        AmbientUniverseData.secondaryStars.forEach { star ->
            val phase = (timeSeconds * star.pulseSpeed + star.phaseOffset) % (2 * PI.toFloat())

            // Individual twinkle dynamics
            val pulseFactor = when (star.twinkleType) {
                StarTwinkleType.STEADY -> {
                    0.93f + 0.07f * sin(phase)
                }
                StarTwinkleType.BREATHING -> {
                    val rawSine = (sin(phase) + 1f) / 2f
                    val harmonic = (sin(timeSeconds * star.twinkleFreq * 2.5f + star.phaseOffset * 1.5f) + 1f) / 2f
                    0.65f + 0.35f * (rawSine * 0.82f + harmonic * 0.18f)
                }
                StarTwinkleType.RARE_LUMINOUS -> {
                    val rawSine = (sin(phase) + 1f) / 2f
                    val flare = rawSine.pow(3.5f)
                    0.68f + 0.52f * flare
                }
            }

            // Layer-based subtle parallax drift
            val driftFactor = when (star.layer) {
                0 -> 0.045f
                1 -> 0.080f
                else -> 0.115f
            }
            val driftX = if (reduceMotion) 0f else {
                sin(timeSeconds * 0.12f + star.phaseOffset) * driftFactor * density.density * 10f
            }
            val driftY = if (reduceMotion) 0f else {
                cos(timeSeconds * 0.09f + star.phaseOffset) * driftFactor * density.density * 8f
            }

            val pointX = canvasWidth * star.relX + driftX
            val pointY = canvasHeight * star.relY + driftY
            val centerOffset = Offset(pointX, pointY)

            // Extra dampening in center card area during Login/Register for flawless form legibility
            val isInCentralFormArea = mode == AmbientUniverseMode.LOGIN_REGISTER &&
                    star.relX in 0.16f..0.84f && star.relY in 0.34f..0.72f
            val formDampening = if (isInCentralFormArea) 0.60f else 1.0f

            val currentAlpha = ((star.baseAlpha * pulseFactor) * initialAlpha * starAlphaScale * formDampening)
                .coerceIn(0.06f, 0.92f)
            val starRadiusPx = star.radiusDp.dp.toPx()

            // Subtle luminous halo around near stars
            if (star.layer == 2 && starRadiusPx > 1.2f) {
                val haloRadius = starRadiusPx * 2.7f
                val haloBrush = Brush.radialGradient(
                    colors = listOf(
                        secondaryStarGlowColor.copy(alpha = currentAlpha * 0.42f),
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

        // 5. EXACTLY FIVE PRIMARY LIGHTS (With subtle micro-bloom enhancement)
        if (showPrimaryLights) {
            AmbientUniverseData.primaryStars.forEach { star ->
                val phase = (timeSeconds * star.pulseSpeed + star.phaseOffset) % (2 * PI.toFloat())
                val rawSine = (sin(phase) + 1f) / 2f
                val twinkle = (sin(timeSeconds * star.twinkleFreq * 2.8f + star.phaseOffset) + 1f) / 2f
                val combinedPulse = rawSine * 0.78f + twinkle * 0.22f

                // Subtle optical lock drift
                val driftX = if (reduceMotion) 0f else {
                    sin(timeSeconds * 0.08f + star.phaseOffset) * 0.4f * density.density
                }
                val driftY = if (reduceMotion) 0f else {
                    cos(timeSeconds * 0.06f + star.phaseOffset) * 0.3f * density.density
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
    }
}
