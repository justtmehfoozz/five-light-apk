package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme

/**
 * Screen 1: FiveLight Pre-Login Welcome Screen.
 * Refined architectural representation of "FIVE -> ONE LIGHT":
 * Five subtle geometric points of light in a gentle arc converging into
 * one restrained, serene central focal light with subtle horizon atmosphere.
 */
@Composable
fun PreLoginPromptScreen(
    onLoginOrRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val density = LocalDensity.current

    // Theme-tuned color values for subtle dawn atmosphere
    val baseAccent = if (isDark) Color(0xFF494556) else Color(0xFF8D6B1E)
    val glowColor = if (isDark) Color(0xFF6B607E) else Color(0xFF8D6B1E)
    val centralGlowColor = if (isDark) Color(0xFF8B7FA4) else Color(0xFF9E7B26)

    // Cores with hierarchy
    val outerPointColor = if (isDark) Color(0xFFC5BDD8) else Color(0xFF8D6B1E)
    val midPointColor = if (isDark) Color(0xFFDCD6EB) else Color(0xFF805F15)
    val apexPointColor = if (isDark) Color(0xFFEBE6F7) else Color(0xFF735411)
    val centralPointCore = if (isDark) Color(0xFFFFFFFF) else Color(0xFF573E0B)

    // Entrance animation state
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isStarted = true
    }

    // 1. 0-400ms: Five surrounding lights and horizon atmosphere gently fade into view
    val initialLightAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "initialLightAlpha"
    )
    val horizonAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "horizonAlpha"
    )

    // 2. 350-650ms: Five lights subtly increase luminance toward the center (convergence suggestion)
    val convergenceAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 350, easing = FastOutSlowInEasing),
        label = "convergenceAlpha"
    )

    // 3. 550-800ms: Central focal light emerges and soft wide halo develops
    val centralLightAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 250, delayMillis = 550, easing = FastOutSlowInEasing),
        label = "centralLightAlpha"
    )
    val centralHaloScale by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0.78f,
        animationSpec = tween(durationMillis = 250, delayMillis = 550, easing = FastOutSlowInEasing),
        label = "centralHaloScale"
    )

    // 4. 700-900ms: Headline enters with 8dp upward glide
    val headlineAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "headlineAlpha"
    )
    val headlineOffsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else with(density) { 8.dp.toPx() },
        animationSpec = tween(durationMillis = 200, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "headlineOffsetY"
    )

    // 5. 800-1000ms: Subtitle and CTA area fade in with subtle 5dp upward glide
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = 800, easing = FastOutSlowInEasing),
        label = "subtitleAlpha"
    )
    val ctaAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = 800, easing = FastOutSlowInEasing),
        label = "ctaAlpha"
    )
    val ctaOffsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else with(density) { 5.dp.toPx() },
        animationSpec = tween(durationMillis = 200, delayMillis = 800, easing = FastOutSlowInEasing),
        label = "ctaOffsetY"
    )

    // Primary button press feedback (1.0 -> 0.98 -> 1.0)
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("pre_login_prompt_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Area (approx 34%): Refined Five Lights Composition & Horizon Atmosphere
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.34f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val focalCenterX = canvasWidth * 0.50f
                    val focalCenterY = canvasHeight * 0.60f

                    // 1. Subtle horizontal atmospheric light band (very diffused, first-light feel)
                    val horizonGlowAlpha = if (isDark) 0.22f else 0.12f
                    val horizonRadialBrush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = horizonGlowAlpha * horizonAlpha),
                            baseAccent.copy(alpha = horizonGlowAlpha * 0.40f * horizonAlpha),
                            glowColor.copy(alpha = horizonGlowAlpha * 0.10f * horizonAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalCenterX, focalCenterY - 4.dp.toPx()),
                        radius = canvasWidth * 0.75f
                    )
                    drawRect(brush = horizonRadialBrush)

                    // Soft horizontal dawn gradient band (no visible line, completely diffused)
                    val horizonBandAlpha = if (isDark) 0.14f else 0.08f
                    val horizonBandBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            baseAccent.copy(alpha = horizonBandAlpha * 0.4f * horizonAlpha),
                            baseAccent.copy(alpha = horizonBandAlpha * horizonAlpha),
                            baseAccent.copy(alpha = horizonBandAlpha * 0.3f * horizonAlpha),
                            Color.Transparent
                        ),
                        startY = focalCenterY - 30.dp.toPx(),
                        endY = focalCenterY + 30.dp.toPx()
                    )
                    drawRect(brush = horizonBandBrush)

                    // 2. Wide soft diffused halo around central focal light (restrained, no spotlight)
                    val diffusedHaloRadius = 42.dp.toPx() * centralHaloScale
                    val centralHaloAlpha = if (isDark) 0.24f else 0.15f
                    val centralHaloBrush = Brush.radialGradient(
                        colors = listOf(
                            centralGlowColor.copy(alpha = centralHaloAlpha * centralLightAlpha),
                            glowColor.copy(alpha = centralHaloAlpha * 0.45f * centralLightAlpha),
                            glowColor.copy(alpha = centralHaloAlpha * 0.12f * centralLightAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalCenterX, focalCenterY),
                        radius = diffusedHaloRadius
                    )
                    drawCircle(
                        brush = centralHaloBrush,
                        radius = diffusedHaloRadius,
                        center = Offset(focalCenterX, focalCenterY)
                    )

                    // 3. Five surrounding lights in an architectural arc framing the focal point
                    val arcSpreadX = canvasWidth * 0.26f
                    val arcHeightY = 36.dp.toPx()

                    // Structured arc points with individual weight & subtle depth
                    val lightPoints = listOf(
                        // Outer left
                        Triple(
                            Offset(focalCenterX - arcSpreadX, focalCenterY - arcHeightY * 0.12f),
                            2.1.dp.toPx(),
                            outerPointColor
                        ),
                        // Mid left
                        Triple(
                            Offset(focalCenterX - arcSpreadX * 0.50f, focalCenterY - arcHeightY * 0.68f),
                            2.4.dp.toPx(),
                            midPointColor
                        ),
                        // Apex top
                        Triple(
                            Offset(focalCenterX, focalCenterY - arcHeightY * 0.95f),
                            2.6.dp.toPx(),
                            apexPointColor
                        ),
                        // Mid right
                        Triple(
                            Offset(focalCenterX + arcSpreadX * 0.50f, focalCenterY - arcHeightY * 0.68f),
                            2.4.dp.toPx(),
                            midPointColor
                        ),
                        // Outer right
                        Triple(
                            Offset(focalCenterX + arcSpreadX, focalCenterY - arcHeightY * 0.12f),
                            2.1.dp.toPx(),
                            outerPointColor
                        )
                    )

                    val baseHaloAlpha = if (isDark) 0.20f else 0.14f
                    val basePointAlpha = if (isDark) 0.65f else 0.55f
                    val convergenceBoost = 0.25f * convergenceAlpha

                    lightPoints.forEachIndexed { index, (point, radius, color) ->
                        // Inward luminance gradation suggesting convergence
                        val pointLuminanceMultiplier = when (index) {
                            2 -> 1.15f
                            1, 3 -> 1.05f
                            else -> 0.95f
                        }

                        val currentHaloAlpha = (baseHaloAlpha * pointLuminanceMultiplier + convergenceBoost * 0.4f) * initialLightAlpha
                        val currentPointAlpha = ((basePointAlpha * pointLuminanceMultiplier + convergenceBoost) * initialLightAlpha).coerceAtMost(1f)

                        // Subtle outer halo per point
                        val pointHaloRadius = (radius * 3.8f).coerceAtLeast(8.dp.toPx())
                        val pointHaloBrush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = currentHaloAlpha),
                                Color.Transparent
                            ),
                            center = point,
                            radius = pointHaloRadius
                        )
                        drawCircle(
                            brush = pointHaloBrush,
                            radius = pointHaloRadius,
                            center = point
                        )

                        // Precise inner core per point
                        drawCircle(
                            color = color.copy(alpha = currentPointAlpha),
                            radius = radius,
                            center = point
                        )
                    }

                    // 4. Central Focal Light Core (The ONE focal point - refined, restrained core)
                    val centralCoreRadius = 3.5.dp.toPx() * centralHaloScale
                    val centralInnerHaloRadius = 12.dp.toPx() * centralHaloScale
                    val centralInnerHaloAlpha = if (isDark) 0.35f else 0.24f

                    val centralInnerHaloBrush = Brush.radialGradient(
                        colors = listOf(
                            centralGlowColor.copy(alpha = centralInnerHaloAlpha * centralLightAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalCenterX, focalCenterY),
                        radius = centralInnerHaloRadius
                    )
                    drawCircle(
                        brush = centralInnerHaloBrush,
                        radius = centralInnerHaloRadius,
                        center = Offset(focalCenterX, focalCenterY)
                    )

                    // Compact bright core
                    drawCircle(
                        color = centralPointCore.copy(alpha = 0.96f * centralLightAlpha),
                        radius = centralCoreRadius,
                        center = Offset(focalCenterX, focalCenterY)
                    )
                }
            }

            // Middle Area: Headline + Subtitle (Tighter light-to-text relationship)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Keep your journey with you.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifHeaderFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        lineHeight = 35.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = headlineAlpha
                            translationY = headlineOffsetY
                        }
                        .testTag("pre_login_headline")
                )

                Text(
                    text = "Sign in to sync your prayers, dhikr, and reflections across your devices.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .graphicsLayer {
                            alpha = subtitleAlpha
                        }
                        .testTag("pre_login_subtext")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lower Area: Primary Monochrome Pill Button + Secondary Underlined Action Link
            val primaryBgColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
            val primaryTextColor = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)
            val secondaryTextColor = if (isDark) Color(0xFFF5F5F7) else Color(0xFF141416)

            val guestInteractionSource = remember { MutableInteractionSource() }
            val isGuestPressed by guestInteractionSource.collectIsPressedAsState()
            val guestAlpha by animateFloatAsState(
                targetValue = if (isGuestPressed) 0.60f else 1.0f,
                animationSpec = tween(durationMillis = 100),
                label = "guestAlpha"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = ctaAlpha
                        translationY = ctaOffsetY
                    }
                    .padding(bottom = 12.dp)
            ) {
                Button(
                    onClick = onLoginOrRegister,
                    interactionSource = buttonInteractionSource,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBgColor,
                        contentColor = primaryTextColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .testTag("pre_login_primary_btn")
                ) {
                    Text(
                        text = "Login or Register",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.5.sp,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(
                            interactionSource = guestInteractionSource,
                            indication = null,
                            onClick = onContinueAsGuest
                        )
                        .graphicsLayer {
                            alpha = guestAlpha
                        }
                        .testTag("pre_login_guest_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.5.sp,
                            textDecoration = TextDecoration.Underline,
                            letterSpacing = 0.15.sp
                        ),
                        color = secondaryTextColor
                    )
                }
            }
        }
    }
}
