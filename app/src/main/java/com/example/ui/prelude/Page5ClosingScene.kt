package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Page5ClosingScene(
    onBeginJourney: () -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val scope = rememberCoroutineScope()

    val textAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val textOffsetY = remember { Animatable(if (reduceMotion) 0f else 8f) }

    val wordmarkAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val convergenceProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val ctaAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val ctaOffsetY = remember { Animatable(if (reduceMotion) 0f else 12f) }

    val buttonScale = remember { Animatable(1f) }
    val lightPulse = remember { Animatable(1f) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(100)
            textAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            textOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            delay(200)
            // Five lights converge towards center
            convergenceProgress.animateTo(1f, animationSpec = tween(1400, easing = FastOutSlowInEasing))

            delay(100)
            wordmarkAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))

            delay(150)
            ctaAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            ctaOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val wordmarkColor = if (isDark) Color(0xFFEADBFE) else Color(0xFF8A6D3B)
    val lightGlowColor = if (isDark) Color(0xFFC2B6EC) else Color(0xFF9E8462)
    val buttonBg = if (isDark) Color(0xFF2B2538) else Color(0xFFEBE3D8)
    val buttonText = if (isDark) Color(0xFFFAF7F2) else Color(0xFF1C1B1F)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_5")
            .semantics {
                contentDescription = "Five Lights closing scene"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Main Emotion Heading
            Text(
                text = "Made for your journey.",
                fontFamily = InstrumentSerifItalic,
                fontStyle = FontStyle.Italic,
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 46.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffsetY.value.dp.toPx()
                }
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Five Lights Convergence Canvas & Interactive Touch Response
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .pointerInput(reduceMotion) {
                        if (!reduceMotion) {
                            detectTapGestures(
                                onTap = {
                                    scope.launch {
                                        lightPulse.animateTo(1.25f, tween(150))
                                        lightPulse.animateTo(1.0f, spring(stiffness = 250f))
                                    }
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f - 16.dp.toPx()

                    val currentRadius = maxRadius * (1f - convergenceProgress.value * 0.7f)

                    for (i in 0 until 5) {
                        val angle = (i * 72f - 90f) * (PI / 180f)
                        val lightPos = Offset(
                            x = center.x + currentRadius * cos(angle).toFloat(),
                            y = center.y + currentRadius * sin(angle).toFloat()
                        )

                        val glowRadius = 20.dp.toPx() * lightPulse.value

                        val glowBrush = Brush.radialGradient(
                            colors = listOf(
                                lightGlowColor.copy(alpha = 0.85f),
                                lightGlowColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = lightPos,
                            radius = glowRadius
                        )

                        drawCircle(
                            brush = glowBrush,
                            radius = glowRadius,
                            center = lightPos
                        )

                        drawCircle(
                            color = lightGlowColor,
                            radius = 3.5.dp.toPx(),
                            center = lightPos
                        )
                    }

                    val centralGlowBrush = Brush.radialGradient(
                        colors = listOf(
                            lightGlowColor.copy(alpha = 0.25f * convergenceProgress.value * lightPulse.value),
                            Color.Transparent
                        ),
                        center = center,
                        radius = 80.dp.toPx() * lightPulse.value
                    )

                    drawCircle(
                        brush = centralGlowBrush,
                        radius = 80.dp.toPx() * lightPulse.value,
                        center = center
                    )
                }

                Text(
                    text = "FiveLight",
                    fontFamily = InstrumentSerifItalic,
                    fontStyle = FontStyle.Italic,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = wordmarkColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = wordmarkAlpha.value
                        scaleX = lightPulse.value
                        scaleY = lightPulse.value
                    }
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Final Action Button: "Begin your journey"
            Surface(
                onClick = {
                    scope.launch {
                        buttonScale.animateTo(0.96f, tween(80))
                        buttonScale.animateTo(1.0f, spring(stiffness = 300f))
                        onBeginJourney()
                    }
                },
                shape = CircleShape,
                color = buttonBg,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = ctaAlpha.value
                        translationY = ctaOffsetY.value.dp.toPx()
                        scaleX = buttonScale.value
                        scaleY = buttonScale.value
                    }
                    .testTag("prelude_begin_journey_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Begin your journey",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = buttonText
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Begin journey",
                        tint = buttonText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
