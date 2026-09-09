package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Page0OpeningScene(
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val density = LocalDensity.current

    val wordmarkAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val wordmarkOffsetY = remember { Animatable(if (reduceMotion) 0f else 8f) }
    val underlineProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val temporaryIlluminationAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val subtitleOffsetY = remember { Animatable(if (reduceMotion) 0f else 6f) }

    var wordmarkWidthPx by remember { mutableFloatStateOf(0f) }

    // FiveLight Logo Reveal Sequence:
    // 1. Stars visible in darkness (0ms - 200ms)
    // 2. Very subtle temporary illumination appears (200ms - 500ms)
    // 3. FiveLight logo fades in (350ms - 750ms)
    // 4. Existing underline animation plays (700ms - 1050ms)
    // 5. Illumination disappears (650ms - 1050ms) -> No permanent glow after reveal!
    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            // Step 1: Initial darkness with ambient stars
            delay(200)

            // Step 2: Very subtle temporary illumination appears
            temporaryIlluminationAlpha.animateTo(
                targetValue = if (isDark) 0.18f else 0.12f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )

            // Step 3: FiveLight logo fades in
            launch {
                wordmarkAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
                wordmarkOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            }

            delay(250)

            // Step 4: Existing golden underline animation writes across
            launch {
                underlineProgress.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
            }

            // Step 5: Temporary illumination disappears completely to 0f
            temporaryIlluminationAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )

            delay(150)
            // Subtitle fades in gently on pure black
            subtitleAlpha.animateTo(0.85f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            subtitleOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val subtitleColor = if (isDark) Color(0xFFC4BFC9) else Color(0xFF5A585F)
    val temporaryLightColor = if (isDark) Color(0xFFC2B6EC) else Color(0xFF8A6D3B)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_0")
            .semantics {
                contentDescription = "FiveLight Opening scene"
            },
        contentAlignment = Alignment.Center
    ) {
        // Temporary reveal illumination:
        // Exists ONLY during initial appearance sequence, completely gone afterward.
        // No permanent radial glow or halo behind the logo.
        if (temporaryIlluminationAlpha.value > 0.001f) {
            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer {
                        alpha = temporaryIlluminationAlpha.value
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val glowBrush = Brush.radialGradient(
                    colors = listOf(
                        temporaryLightColor.copy(alpha = temporaryIlluminationAlpha.value),
                        temporaryLightColor.copy(alpha = temporaryIlluminationAlpha.value * 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width / 2f
                )
                drawCircle(brush = glowBrush, radius = size.width / 2f, center = center)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "FiveLight",
                    fontFamily = InstrumentSerifItalic,
                    fontStyle = FontStyle.Italic,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            wordmarkWidthPx = coordinates.size.width.toFloat()
                        }
                        .graphicsLayer {
                            alpha = wordmarkAlpha.value
                            translationY = wordmarkOffsetY.value.dp.toPx()
                        }
                )
            }

            // Intentional gap between wordmark and underline
            Spacer(modifier = Modifier.height(14.dp))

            // Golden underline matching wordmark width
            val underlineWidthDp = if (wordmarkWidthPx > 0) {
                with(density) { wordmarkWidthPx.toDp() }
            } else {
                180.dp
            }

            Box(
                modifier = Modifier
                    .width(underlineWidthDp)
                    .height(1.5.dp)
            ) {
                if (underlineProgress.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(fraction = underlineProgress.value)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        LightAccentGold.copy(alpha = 0.4f),
                                        LightAccentGold,
                                        LightAccentGold.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your day, illuminated.",
                fontFamily = SpaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor,
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                    translationY = subtitleOffsetY.value.dp.toPx()
                }
            )
        }
    }
}
