package com.example.ui.screens

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AmbientUniverseMode
import com.example.ui.components.FiveLightAmbientBackground
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Screen 1: FiveLight Pre-Login Welcome Screen (Phase 2 Refinement).
 *
 * Visual & Interaction Architecture:
 * - Wordmark Awakening: Uses the exact home-screen header typography (Instrument Serif Italic).
 * - Sharp text layer with a soft blurred radial glow layer behind it.
 * - One-time Light Awakening sequence upon entry: wordmark appears, glow reaches a restrained peak,
 *   and smoothly settles without blocking immediate interaction.
 * - Ambient Wordmark Breathing: Barely-perceptible slow 10-second breathing after awakening.
 * - Staggered Content Entrance: Ambient Field -> Primary Lights -> Wordmark -> Headline -> Subtext -> CTA Buttons.
 * - Theme-aware color systems (Warm Paper Light / Deep Quiet Night Dark) with 600ms transitions.
 * - Accessibility / Reduced Motion support.
 */
@Composable
fun PreLoginPromptScreen(
    onLoginOrRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
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

    // --- Staggered Entrance Animation States ---
    val initialLightAlpha = remember { Animatable(0f) }

    val wordmarkAlpha = remember { Animatable(0f) }
    val wordmarkOffsetY = remember { Animatable(if (reduceMotion) 0f else with(density) { 6.dp.toPx() }) }

    val glowAlpha = remember { Animatable(0f) }
    val glowRadiusDp = remember { Animatable(20f) }

    val headlineAlpha = remember { Animatable(0f) }
    val headlineOffsetY = remember { Animatable(if (reduceMotion) 0f else with(density) { 8.dp.toPx() }) }

    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleOffsetY = remember { Animatable(if (reduceMotion) 0f else with(density) { 5.dp.toPx() }) }

    val primaryBtnAlpha = remember { Animatable(0f) }
    val primaryBtnOffsetY = remember { Animatable(if (reduceMotion) 0f else with(density) { 5.dp.toPx() }) }

    val guestBtnAlpha = remember { Animatable(0f) }
    val guestBtnOffsetY = remember { Animatable(if (reduceMotion) 0f else with(density) { 5.dp.toPx() }) }

    // One-time Light Awakening entrance sequence
    LaunchedEffect(Unit) {
        // 0-400ms: Ambient star field and primary lights establish
        launch {
            initialLightAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }

        // 180ms: Wordmark begins fading in with subtle upward glide
        launch {
            kotlinx.coroutines.delay(180)
            launch { wordmarkAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
            if (!reduceMotion) {
                launch { wordmarkOffsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
            }
        }

        // 220ms - 850ms: Wordmark Glow awakening sequence (peak -> settle)
        launch {
            kotlinx.coroutines.delay(220)
            // Soft peak
            launch { glowAlpha.animateTo(0.85f, tween(260, easing = FastOutSlowInEasing)) }
            launch { glowRadiusDp.animateTo(46f, tween(260, easing = FastOutSlowInEasing)) }

            kotlinx.coroutines.delay(260)
            // Settle naturally
            launch { glowAlpha.animateTo(0.38f, tween(400, easing = FastOutSlowInEasing)) }
            launch { glowRadiusDp.animateTo(32f, tween(400, easing = FastOutSlowInEasing)) }
        }

        // 360ms: Headline enters
        launch {
            kotlinx.coroutines.delay(360)
            launch { headlineAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
            if (!reduceMotion) {
                launch { headlineOffsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
            }
        }

        // 460ms: Subtitle enters
        launch {
            kotlinx.coroutines.delay(460)
            launch { subtitleAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
            if (!reduceMotion) {
                launch { subtitleOffsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
            }
        }

        // 540ms: Primary CTA button enters
        launch {
            kotlinx.coroutines.delay(540)
            launch { primaryBtnAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
            if (!reduceMotion) {
                launch { primaryBtnOffsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
            }
        }

        // 620ms: Guest link enters
        launch {
            kotlinx.coroutines.delay(620)
            launch { guestBtnAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
            if (!reduceMotion) {
                launch { guestBtnOffsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
            }
        }
    }

    // --- VSYNC Timer for Barely-Perceptible Ambient Wordmark Breathing (10s Cycle) ---
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

    val wordmarkAmbientBreathMult = if (reduceMotion) 1f else {
        0.97f + 0.03f * ((sin(timeSeconds * (2 * PI / 10.0)) + 1f) / 2f).toFloat()
    }

    // --- Color System Transitions (600ms) ---
    val wordmarkColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFF2F0E8) else Color(0xFF222129),
        animationSpec = tween(600),
        label = "wordmarkColorAnim"
    )

    val wordmarkGlowColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF9B90C2) else Color(0xFFE2D3C0),
        animationSpec = tween(600),
        label = "wordmarkGlowAnim"
    )

    // Button interaction state listeners
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.98f else 1.0f,
        animationSpec = tween(100),
        label = "buttonScale"
    )

    val guestInteractionSource = remember { MutableInteractionSource() }
    val isGuestPressed by guestInteractionSource.collectIsPressedAsState()
    val guestAlphaPressed by animateFloatAsState(
        targetValue = if (isGuestPressed) 0.60f else 1.0f,
        animationSpec = tween(100),
        label = "guestAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("pre_login_prompt_screen")
    ) {
        // Ambient "Five Lights" Background Layer — Living Universe in Calmer Login/Register Mode
        FiveLightAmbientBackground(
            modifier = Modifier.fillMaxSize(),
            initialAlpha = initialLightAlpha.value,
            mode = AmbientUniverseMode.LOGIN_REGISTER
        )

        // Screen Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Area: Integrated FiveLight Wordmark + Soft Blurred Glow Layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.32f),
                contentAlignment = Alignment.Center
            ) {
                // Soft Blurred Glow Layer behind sharp text
                Canvas(
                    modifier = Modifier
                        .size(width = 280.dp, height = 110.dp)
                        .graphicsLayer {
                            alpha = glowAlpha.value * wordmarkAlpha.value * wordmarkAmbientBreathMult
                            translationY = wordmarkOffsetY.value
                        }
                ) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val currentRadiusPx = glowRadiusDp.value.dp.toPx()

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                wordmarkGlowColor.copy(alpha = 0.50f),
                                wordmarkGlowColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = currentRadiusPx
                        ),
                        radius = currentRadiusPx,
                        center = centerOffset
                    )
                }

                // Sharp Wordmark Text
                Text(
                    text = "FiveLight",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = InstrumentSerifItalic,
                        fontStyle = FontStyle.Italic,
                        fontSize = 42.sp,
                        lineHeight = 48.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = wordmarkColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = wordmarkAlpha.value * wordmarkAmbientBreathMult
                            translationY = wordmarkOffsetY.value
                        }
                        .testTag("pre_login_wordmark")
                )
            }

            // Middle Area: Headline + Subtitle
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
                            alpha = headlineAlpha.value
                            translationY = headlineOffsetY.value
                        }
                        .testTag("pre_login_headline")
                )

                Text(
                    text = "Sign in to sync your prayers, dhikr, and reflections across your devices.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .graphicsLayer {
                            alpha = subtitleAlpha.value
                            translationY = subtitleOffsetY.value
                        }
                        .testTag("pre_login_subtext")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lower Area: Primary Button + Guest Action Link
            val primaryBgColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
            val primaryTextColor = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)
            val secondaryTextColor = if (isDark) Color(0xFFF5F5F7) else Color(0xFF141416)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
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
                            alpha = primaryBtnAlpha.value
                            translationY = primaryBtnOffsetY.value
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
                            alpha = guestBtnAlpha.value * guestAlphaPressed
                            translationY = guestBtnOffsetY.value
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
