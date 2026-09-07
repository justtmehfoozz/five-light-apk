package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data representation of an Atmospheric Depth Light sitting behind the star field.
 * Provides soft photographic depth without acting as a primary star.
 */
data class AtmosphericDepthLight(
    val relX: Float,
    val relY: Float,
    val radiusDp: Float,
    val pulseSpeed: Float,
    val phaseOffset: Float
)

/**
 * Data representation of a star or light point in the FiveLight ambient star field.
 */
data class AmbientStar(
    val relX: Float,          // Relative X position (0.0 to 1.0)
    val relY: Float,          // Relative Y position (0.0 to 1.0)
    val radiusDp: Float,      // Core radius in dp
    val baseAlpha: Float,     // Base opacity (0.15 to 0.90)
    val pulseSpeed: Float,    // Animation breathing speed factor
    val twinkleFreq: Float,   // Twinkle harmonic multiplier
    val phaseOffset: Float,   // Phase shift in radians
    val isPrimary: Boolean,   // True for EXACTLY 5 primary lights
    val layer: Int            // 1 = distant dim, 2 = atmospheric secondary, 3 = primary
)

/**
 * Native Jetpack Compose Ambient "Five Lights" Background (Phase 1 Refinement).
 *
 * Visual System:
 * - MANY quiet secondary stars clearly visible in both Light and Dark modes.
 * - EXACTLY FIVE primary lights: small, refined, luminous stars (~1.4-1.8x secondary point size)
 *   with tight atmospheric glow and a subtle 4-point optical diffraction flare.
 * - 2-3 Atmospheric depth lights creating soft photographic depth behind the star field.
 * - Organic, asymmetric composition with negative space over headline and interaction areas.
 * - Independent animation rhythms and VSYNC-driven continuous breathing.
 */
@Composable
fun FiveLightAmbientBackground(
    modifier: Modifier = Modifier,
    initialAlpha: Float = 1.0f
) {
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val density = LocalDensity.current

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

    // 1. Color System Definitions with smooth 600ms transitions
    val durationMs = 600

    val bgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF000000) else Color(0xFFFAF7F2),
        animationSpec = tween(durationMs),
        label = "bgAnim"
    )

    // Depth Lights Color (Soft diffused background glow)
    val depthLightColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF13101C) else Color(0xFFE8DCCB),
        animationSpec = tween(durationMs),
        label = "depthLightAnim"
    )

    // Secondary Stars Colors (Crisp and visible on both warm paper and dark sky)
    val secondaryStarColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFAAA5B8) else Color(0xFF6B6256),
        animationSpec = tween(durationMs),
        label = "secondaryStarAnim"
    )

    val secondaryStarGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF6A637C) else Color(0xFFD4C8B8),
        animationSpec = tween(durationMs),
        label = "secondaryGlowAnim"
    )

    // Primary 5 Lights Palette (Small, luminous, refined)
    val primaryCoreColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF2C2418),
        animationSpec = tween(durationMs),
        label = "primaryCoreAnim"
    )

    val primaryCoreHighlightColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFFFFFDF8),
        animationSpec = tween(durationMs),
        label = "primaryCoreHighlightAnim"
    )

    val primaryInnerGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFE6E3F5) else Color(0xFFBCA68C),
        animationSpec = tween(durationMs),
        label = "primaryInnerGlowAnim"
    )

    val primaryOuterGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF9B90C2) else Color(0xFFE2D3C0),
        animationSpec = tween(durationMs),
        label = "primaryOuterGlowAnim"
    )

    val primaryAuraColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF38304A) else Color(0xFFEFE4D4),
        animationSpec = tween(durationMs),
        label = "primaryAuraAnim"
    )

    val primaryFlareColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFD4CEEE) else Color(0xFF8A765E),
        animationSpec = tween(durationMs),
        label = "primaryFlareAnim"
    )

    // 2. Pre-generate 3 Atmospheric Depth Lights
    val depthLights = remember {
        listOf(
            AtmosphericDepthLight(relX = 0.22f, relY = 0.18f, radiusDp = 58f, pulseSpeed = 0.06f, phaseOffset = 0.0f),
            AtmosphericDepthLight(relX = 0.78f, relY = 0.32f, radiusDp = 64f, pulseSpeed = 0.045f, phaseOffset = 2.1f),
            AtmosphericDepthLight(relX = 0.50f, relY = 0.75f, radiusDp = 52f, pulseSpeed = 0.055f, phaseOffset = 4.2f)
        )
    }

    // 3. Pre-generate star field positions (EXACTLY 5 Primary + 48 Secondary = 53 total)
    val starField = remember {
        val list = mutableListOf<AmbientStar>()

        // EXACTLY 5 PRIMARY LIGHTS - Organically placed in 5 distinct regions around the UI
        val primaryCoords = listOf(
            Pair(0.18f, 0.13f), // Primary 0: Upper Left
            Pair(0.82f, 0.16f), // Primary 1: Upper Right
            Pair(0.50f, 0.26f), // Primary 2: Upper Center (below Wordmark region)
            Pair(0.14f, 0.42f), // Primary 3: Mid Left
            Pair(0.86f, 0.38f)  // Primary 4: Mid Right
        )

        val primarySpeeds = listOf(0.85f, 0.65f, 1.05f, 0.75f, 0.55f)
        val primaryTwinkles = listOf(2.2f, 1.8f, 2.5f, 1.6f, 2.0f)

        primaryCoords.forEachIndexed { idx, (rx, ry) ->
            list.add(
                AmbientStar(
                    relX = rx,
                    relY = ry,
                    radiusDp = 2.4f, // Refined small point (~1.4x of secondary star)
                    baseAlpha = 0.90f,
                    pulseSpeed = primarySpeeds[idx],
                    twinkleFreq = primaryTwinkles[idx],
                    phaseOffset = idx * 1.25f,
                    isPrimary = true,
                    layer = 3
                )
            )
        }

        // ~48 Secondary Lights - Art-directed asymmetric distribution
        val rawSecondary = listOf(
            // Cluster 1: Top-Left sky
            Triple(0.08f, 0.07f, 1.2f), Triple(0.12f, 0.20f, 1.5f), Triple(0.24f, 0.08f, 1.0f),
            Triple(0.28f, 0.18f, 1.7f), Triple(0.34f, 0.11f, 1.1f), Triple(0.06f, 0.26f, 1.4f),
            Triple(0.22f, 0.29f, 1.6f), Triple(0.38f, 0.22f, 1.0f), Triple(0.16f, 0.33f, 1.3f),

            // Cluster 2: Top-Right sky
            Triple(0.64f, 0.09f, 1.3f), Triple(0.72f, 0.06f, 1.0f), Triple(0.78f, 0.22f, 1.6f),
            Triple(0.88f, 0.08f, 1.2f), Triple(0.92f, 0.24f, 1.5f), Triple(0.68f, 0.28f, 1.1f),
            Triple(0.84f, 0.30f, 1.4f), Triple(0.95f, 0.15f, 0.9f), Triple(0.60f, 0.18f, 1.6f),

            // Cluster 3: Upper Center backdrop
            Triple(0.42f, 0.06f, 1.0f), Triple(0.58f, 0.08f, 1.2f), Triple(0.46f, 0.16f, 1.4f),
            Triple(0.54f, 0.19f, 1.1f), Triple(0.36f, 0.25f, 1.5f), Triple(0.62f, 0.24f, 1.0f),

            // Cluster 4: Mid-Left margin
            Triple(0.06f, 0.39f, 1.5f), Triple(0.10f, 0.50f, 1.1f), Triple(0.20f, 0.47f, 1.8f),
            Triple(0.05f, 0.58f, 1.2f), Triple(0.18f, 0.59f, 1.4f), Triple(0.26f, 0.41f, 1.0f),

            // Cluster 5: Mid-Right margin
            Triple(0.94f, 0.44f, 1.6f), Triple(0.78f, 0.46f, 1.1f), Triple(0.90f, 0.52f, 1.7f),
            Triple(0.80f, 0.58f, 1.3f), Triple(0.95f, 0.62f, 1.0f), Triple(0.72f, 0.39f, 1.4f),

            // Cluster 6: Lower subtle backdrop
            Triple(0.12f, 0.72f, 1.1f), Triple(0.28f, 0.80f, 0.9f), Triple(0.75f, 0.76f, 1.2f),
            Triple(0.88f, 0.82f, 1.0f), Triple(0.22f, 0.88f, 1.3f), Triple(0.82f, 0.90f, 0.9f),
            Triple(0.38f, 0.86f, 1.1f), Triple(0.62f, 0.89f, 1.0f), Triple(0.48f, 0.93f, 0.8f)
        )

        rawSecondary.forEachIndexed { i, (rx, ry, rad) ->
            val isLayer1 = i % 3 == 0
            list.add(
                AmbientStar(
                    relX = rx,
                    relY = ry,
                    radiusDp = rad,
                    baseAlpha = if (isLayer1) 0.38f else 0.65f, // High enough to be crisp in Light Mode
                    pulseSpeed = 0.25f + (i * 0.08f) % 0.45f, // Calm 6s - 12s breathing
                    twinkleFreq = 1.0f + (i * 0.15f) % 1.2f,
                    phaseOffset = (i * 1.618f) % (2 * PI.toFloat()),
                    isPrimary = false,
                    layer = if (isLayer1) 1 else 2
                )
            )
        }

        list
    }

    // 4. VSYNC-synchronized continuous animation timer
    var timeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { frameTime ->
                timeNanos = frameTime - startTime
            }
        }
    }
    val timeSeconds = timeNanos / 1_000_000_000f

    // Dimension calculations for primary optical elements
    val primaryAuraRadiusPx = with(density) { 24.dp.toPx() }
    val primaryOuterGlowRadiusPx = with(density) { 11.dp.toPx() }
    val primaryInnerGlowRadiusPx = with(density) { 5.5.dp.toPx() }
    val flareArmLengthPx = with(density) { 8.dp.toPx() }
    val flareStrokePx = with(density) { 1.0.dp.toPx() }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Fill solid background
        drawRect(color = bgColor)

        if (initialAlpha <= 0f) return@Canvas

        // A. RENDER ATMOSPHERIC DEPTH LIGHTS (Behind Star Field)
        depthLights.forEach { depthLight ->
            val dPhase = (timeSeconds * depthLight.pulseSpeed + depthLight.phaseOffset) % (2 * PI.toFloat())
            val dPulse = (sin(dPhase) + 1f) / 2f
            val dAlpha = (0.08f + 0.06f * dPulse) * initialAlpha

            val dPointX = canvasWidth * depthLight.relX
            val dPointY = canvasHeight * depthLight.relY
            val dOffset = Offset(dPointX, dPointY)
            val dRadiusPx = depthLight.radiusDp.dp.toPx() * (0.92f + 0.08f * dPulse)

            val dBrush = Brush.radialGradient(
                colors = listOf(
                    depthLightColor.copy(alpha = dAlpha),
                    depthLightColor.copy(alpha = dAlpha * 0.35f),
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

        // B. RENDER STAR FIELD (Secondary Stars + Exactly 5 Primary Lights)
        starField.forEach { star ->
            val phase = (timeSeconds * star.pulseSpeed + star.phaseOffset) % (2 * PI.toFloat())
            val pulseFactor = (sin(phase) + 1f) / 2f // 0.0f .. 1.0f

            val twinkleVal = (sin(timeSeconds * star.twinkleFreq * 3f + star.phaseOffset) + 1f) / 2f
            val combinedPulse = (pulseFactor * 0.75f + twinkleVal * 0.25f)

            // Tiny sub-pixel drift (disabled under Reduced Motion)
            val driftX = if (reduceMotion) 0f else (sin(timeSeconds * 0.18f + star.phaseOffset) * (if (star.isPrimary) 0.6f else 1.0f) * density.density)
            val driftY = if (reduceMotion) 0f else (cos(timeSeconds * 0.14f + star.phaseOffset) * (if (star.isPrimary) 0.5f else 0.8f) * density.density)

            val pointX = canvasWidth * star.relX + driftX
            val pointY = canvasHeight * star.relY + driftY
            val centerOffset = Offset(pointX, pointY)

            if (star.isPrimary) {
                // PRIMARY LIGHT (EXACTLY 5) - Small, Refined, Luminous Star
                val primaryAlphaMult = (0.78f + 0.22f * combinedPulse) * initialAlpha

                // Layer 4: Soft Atmospheric Aura (Radius ~ 24dp)
                val auraRadius = primaryAuraRadiusPx * (0.90f + 0.10f * combinedPulse)
                val auraBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryAuraColor.copy(alpha = 0.25f * primaryAlphaMult),
                        primaryAuraColor.copy(alpha = 0.08f * primaryAlphaMult),
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

                // Outer Glow (Radius ~ 11dp)
                val outerGlowRadius = primaryOuterGlowRadiusPx * (0.92f + 0.08f * combinedPulse)
                val outerGlowBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryOuterGlowColor.copy(alpha = 0.50f * primaryAlphaMult),
                        primaryOuterGlowColor.copy(alpha = 0.15f * primaryAlphaMult),
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

                // Inner Glow (Radius ~ 5.5dp)
                val innerGlowRadius = primaryInnerGlowRadiusPx * (0.95f + 0.05f * combinedPulse)
                val innerGlowBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryInnerGlowColor.copy(alpha = 0.75f * primaryAlphaMult),
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

                // 4-Point Optical Diffraction Flare (Subtle Thin Cross)
                val currentFlareArm = flareArmLengthPx * (0.88f + 0.12f * combinedPulse)
                val currentFlareAlpha = (0.30f + 0.15f * combinedPulse) * primaryAlphaMult

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

                // Crisp Bright Core (Radius ~ 2.4dp)
                val coreRadiusPx = (star.radiusDp.dp.toPx()) * (0.95f + 0.08f * combinedPulse)
                drawCircle(
                    color = primaryCoreColor.copy(alpha = 0.95f * primaryAlphaMult),
                    radius = coreRadiusPx,
                    center = centerOffset
                )

                // Center Point Highlight for Crisp Luminous Center
                if (!isDark) {
                    drawCircle(
                        color = primaryCoreHighlightColor.copy(alpha = 0.85f * primaryAlphaMult),
                        radius = coreRadiusPx * 0.45f,
                        center = centerOffset
                    )
                }

            } else {
                // SECONDARY STAR (Layer 1 & Layer 2) - Clearly visible, calm
                val currentAlpha = ((star.baseAlpha * (0.70f + 0.30f * combinedPulse)) * initialAlpha)
                    .coerceIn(0.12f, 0.90f)

                val starRadiusPx = star.radiusDp.dp.toPx()

                if (star.layer == 2 && starRadiusPx > 1.3f) {
                    // Soft subtle halo for larger secondary stars
                    val haloRadius = starRadiusPx * 2.8f
                    val haloBrush = Brush.radialGradient(
                        colors = listOf(
                            secondaryStarGlowColor.copy(alpha = currentAlpha * 0.40f),
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
        }
    }
}
