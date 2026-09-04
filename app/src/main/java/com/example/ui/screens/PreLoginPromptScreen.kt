package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.semanticPrimaryAccent

/**
 * Screen 1: FiveLight Pre-Login Welcome Screen.
 * Atmospheric abstract representation of "FIVE -> ONE LIGHT":
 * Five subtle geometric points of light converging into one serene central focal glow.
 */
@Composable
fun PreLoginPromptScreen(
    onLoginOrRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val density = LocalDensity.current

    // Theme-tuned color values
    val baseAccent = if (isDark) Color(0xFF494556) else Color(0xFF8D6B1E)
    val glowColor = if (isDark) Color(0xFF7A708C) else Color(0xFF8D6B1E)
    val lightPointCore = if (isDark) Color(0xFFE2DCF0) else Color(0xFF8D6B1E)
    val centralPointCore = if (isDark) Color(0xFFFFFFFF) else Color(0xFF634A12)

    // Entrance animation state
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isStarted = true
    }

    // 0-500ms: Atmospheric horizon + 5 subtle lights fade in
    val horizonAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "horizonAlpha"
    )
    val fiveLightsAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "fiveLightsAlpha"
    )
    val atmosphereScale by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0.94f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "atmosphereScale"
    )

    // 300-700ms: Central focal light emerges and blooms
    val centralLightAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "centralLightAlpha"
    )
    val centralLightScale by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0.82f,
        animationSpec = tween(durationMillis = 450, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "centralLightScale"
    )

    // 500-800ms: Headline enters with 10dp upward glide
    val headlineAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "headlineAlpha"
    )
    val headlineOffsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else with(density) { 10.dp.toPx() },
        animationSpec = tween(durationMillis = 450, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "headlineOffsetY"
    )

    // 650-900ms: Subtitle fades in with 8dp upward glide
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = 650, easing = FastOutSlowInEasing),
        label = "subtitleAlpha"
    )
    val subtitleOffsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else with(density) { 8.dp.toPx() },
        animationSpec = tween(durationMillis = 450, delayMillis = 650, easing = FastOutSlowInEasing),
        label = "subtitleOffsetY"
    )

    // 750-1000ms: CTA area fades in with 10dp upward glide
    val ctaAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = 750, easing = FastOutSlowInEasing),
        label = "ctaAlpha"
    )
    val ctaOffsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else with(density) { 10.dp.toPx() },
        animationSpec = tween(durationMillis = 450, delayMillis = 750, easing = FastOutSlowInEasing),
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
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper 36%: Five Lights Composition + Atmospheric Horizon Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
                    .graphicsLayer {
                        scaleX = atmosphereScale
                        scaleY = atmosphereScale
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val focalCenterX = canvasWidth * 0.50f
                    val focalCenterY = canvasHeight * 0.58f

                    // 1. Soft atmospheric dawn/horizon glow
                    val horizonGlowAlpha = if (isDark) 0.32f else 0.16f
                    val horizonGlowBrush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = horizonGlowAlpha * horizonAlpha),
                            baseAccent.copy(alpha = horizonGlowAlpha * 0.40f * horizonAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalCenterX, focalCenterY - 10.dp.toPx()),
                        radius = canvasWidth * 0.68f
                    )
                    drawRect(brush = horizonGlowBrush)

                    // 2. Central focal bloom (ONE LIGHT)
                    val bloomRadius = 48.dp.toPx() * centralLightScale
                    val centralBloomAlpha = if (isDark) 0.38f else 0.22f
                    val centralBloomBrush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = centralBloomAlpha * centralLightAlpha),
                            glowColor.copy(alpha = centralBloomAlpha * 0.30f * centralLightAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalCenterX, focalCenterY),
                        radius = bloomRadius
                    )
                    drawCircle(
                        brush = centralBloomBrush,
                        radius = bloomRadius,
                        center = Offset(focalCenterX, focalCenterY)
                    )

                    // 3. Five subtle lights arranged in a harmonious geometric arc
                    // Symmetrical arc positions framing and converging toward the central light
                    val arcSpreadX = canvasWidth * 0.28f
                    val arcHeightY = 44.dp.toPx()

                    val fiveLights = listOf(
                        Offset(focalCenterX - arcSpreadX, focalCenterY - arcHeightY * 0.15f),
                        Offset(focalCenterX - arcSpreadX * 0.52f, focalCenterY - arcHeightY * 0.72f),
                        Offset(focalCenterX, focalCenterY - arcHeightY * 0.96f),
                        Offset(focalCenterX + arcSpreadX * 0.52f, focalCenterY - arcHeightY * 0.72f),
                        Offset(focalCenterX + arcSpreadX, focalCenterY - arcHeightY * 0.15f)
                    )

                    val haloAlpha = if (isDark) 0.26f else 0.18f
                    val pointAlpha = if (isDark) 0.75f else 0.65f

                    fiveLights.forEach { point ->
                        // Subtle outer halo for each of the 5 points
                        val pointHaloBrush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = haloAlpha * fiveLightsAlpha),
                                Color.Transparent
                            ),
                            center = point,
                            radius = 12.dp.toPx()
                        )
                        drawCircle(
                            brush = pointHaloBrush,
                            radius = 12.dp.toPx(),
                            center = point
                        )

                        // Refined inner core for each of the 5 points
                        drawCircle(
                            color = lightPointCore.copy(alpha = pointAlpha * fiveLightsAlpha),
                            radius = 2.8.dp.toPx(),
                            center = point
                        )
                    }

                    // 4. Central Light Core (The ONE focal light - slightly more prominent)
                    val centralCoreHaloAlpha = if (isDark) 0.50f else 0.35f
                    val centralCoreHaloBrush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = centralCoreHaloAlpha * centralLightAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalCenterX, focalCenterY),
                        radius = 18.dp.toPx() * centralLightScale
                    )
                    drawCircle(
                        brush = centralCoreHaloBrush,
                        radius = 18.dp.toPx() * centralLightScale,
                        center = Offset(focalCenterX, focalCenterY)
                    )

                    drawCircle(
                        color = centralPointCore.copy(alpha = 0.95f * centralLightAlpha),
                        radius = 4.8.dp.toPx() * centralLightScale,
                        center = Offset(focalCenterX, focalCenterY)
                    )
                }
            }

            // Middle Area: Headline + Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        lineHeight = 36.sp
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
                            translationY = subtitleOffsetY
                        }
                        .testTag("pre_login_subtext")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lower Area: Primary Button + Secondary Action Link
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .testTag("pre_login_primary_btn")
                ) {
                    Text(
                        text = "Login or Register",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }

                TextButton(
                    onClick = onContinueAsGuest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pre_login_guest_btn")
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
