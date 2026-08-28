package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import com.example.R


import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.delay

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
        } catch (e: Exception) {
            false
        }
    }

    // 2. Centralized Theme Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val goldAccentColor = LightAccentGold // 0xFF8D6B1E existing Fajr/gold accent token

    // Theme-aware shimmer highlight color
    val shimmerHighlightColor = if (isDark) {
        Color.White.copy(alpha = 0.40f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }

    // 3. Animation State Setup
    var animationTimeMs by remember { mutableFloatStateOf(0f) }
    var isAnimationComplete by remember { mutableStateOf(false) }
    var wordmarkWidthPx by remember { mutableFloatStateOf(0f) }

    // Easing curve: cubic-bezier(0.19, 1, 0.22, 1) - smooth, refined ease-out without bounce
    val cubicEaseOut = remember { CubicBezierEasing(0.19f, 1.0f, 0.22f, 1.0f) }

    // Main Timeline Driver
    LaunchedEffect(isReducedMotion) {
        if (isReducedMotion) {
            animationTimeMs = 1500f
            isAnimationComplete = true
        } else {
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime).toFloat()
                animationTimeMs = elapsed.coerceAtMost(1500f)
                if (elapsed >= 1500f) {
                    isAnimationComplete = true
                    break
                }
                delay(16) // ~60fps
            }
        }
    }

    // Exit condition: BOTH 1500ms reveal complete AND app initialization ready
    val canExit = (isAnimationComplete && isAppReady) || (isReducedMotion && isAppReady)

    // Exit transition animatable (240ms duration cross-fade)
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

    // Calculate shimmer sweep progress (1100ms - 1500ms -> 0f to 1f)
    val shimmerLinearProgress = if (isReducedMotion) {
        0f
    } else {
        ((animationTimeMs - 1100f) / 400f).coerceIn(0f, 1f)
    }

    // Calculate underline reveal progress (900ms - 1100ms -> 0f to 1f)
    val underlineProgress = if (isReducedMotion) {
        1.0f
    } else {
        val rawUnderline = ((animationTimeMs - 900f) / 200f).coerceIn(0f, 1f)
        cubicEaseOut.transform(rawUnderline)
    }

    // Overall container alpha for reduced motion entrance
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { alpha = reducedMotionAlpha }
                .padding(horizontal = 24.dp)
        ) {

            // ----------------------------------------------------
            // 0. APP LOGO WITH FADE IN
            // ----------------------------------------------------
            val logoAlpha = if (isReducedMotion) {
                1.0f
            } else {
                val raw = ((animationTimeMs - 50f) / 300f).coerceIn(0f, 1f)
                cubicEaseOut.transform(raw)
            }
            val iconRes = if (isDark) R.drawable.fivelight_icon_dark else R.drawable.fivelight_icon_light
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "FiveLight Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .graphicsLayer {
                        alpha = logoAlpha
                        translationY = with(density) { ((1f - logoAlpha) * 10.dp.toPx()) }
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // ----------------------------------------------------
            // 1. CENTERED WORDMARK

            // ----------------------------------------------------
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

                        // Single shimmer sweep effect (1100ms - 1500ms)
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
                        // Calculate staggered timing per character
                        // Start at 150ms + index * 55ms, duration 250ms
                        val letterStartTime = 150f + index * 55f
                        val letterProgress = if (isReducedMotion) {
                            1.0f
                        } else {
                            val raw = ((animationTimeMs - letterStartTime) / 250f).coerceIn(0f, 1f)
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

            // ----------------------------------------------------
            // 2. GOLD/AMBER UNDERLINE (MATCHING WORDMARK WIDTH EXACTLY)
            // ----------------------------------------------------
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
