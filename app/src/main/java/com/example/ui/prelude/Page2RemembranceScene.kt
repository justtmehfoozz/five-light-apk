package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Page2RemembranceScene(
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val headlineAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val headlineOffsetY = remember { Animatable(if (reduceMotion) 0f else 8f) }
    val subtitleAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val subtitleOffsetY = remember { Animatable(if (reduceMotion) 0f else 6f) }

    val glowAlpha = remember { Animatable(if (reduceMotion) 0.8f else 0f) }
    val surfaceAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val ringScale = remember { Animatable(if (reduceMotion) 1f else 0.85f) }
    val hintsAlpha = remember { Animatable(if (reduceMotion) 0.9f else 0f) }

    // Interactive reveal progress
    val revealProgress = remember { Animatable(if (reduceMotion) 1f else 0.4f) }
    var hasFiredHaptic by remember { mutableStateOf(false) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(100)
            headlineAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            headlineOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            delay(120)
            subtitleAlpha.animateTo(0.85f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            subtitleOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))

            delay(150)
            glowAlpha.animateTo(0.8f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            surfaceAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            ringScale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

            delay(200)
            hintsAlpha.animateTo(0.9f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            // Gentle initial reveal bounce
            revealProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val subtitleColor = if (isDark) Color(0xFFC4BFC9) else Color(0xFF5A585F)
    val cardBg = if (isDark) Color(0xFF110F18).copy(alpha = 0.75f) else Color(0xFFFFFFFF).copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color(0xFF2E2B38) else Color(0xFFE8E2D8)
    val arabicColor = if (isDark) Color(0xFFE8DFD3) else Color(0xFF8D6B1E)
    val ringColor = if (isDark) Color(0xFF9B90C2).copy(alpha = 0.35f) else Color(0xFFC8B39B).copy(alpha = 0.45f)
    val hintBadgeBg = if (isDark) Color(0xFF252130).copy(alpha = 0.8f) else Color(0xFFF0EAE1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_2")
            .semantics {
                contentDescription = "Remembrance scene"
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
            Text(
                text = "Stay close.",
                fontFamily = InstrumentSerifItalic,
                fontStyle = FontStyle.Italic,
                fontSize = 38.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = headlineAlpha.value
                    translationY = headlineOffsetY.value.dp.toPx()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Small moments of remembrance, throughout your day.",
                fontFamily = SpaceGrotesk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                    translationY = subtitleOffsetY.value.dp.toPx()
                }
            )

            Spacer(modifier = Modifier.height(34.dp))

            // Central Content Surface with Faint Light Ring
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            alpha = glowAlpha.value * (0.8f + 0.2f * revealProgress.value)
                            scaleX = ringScale.value * (0.95f + 0.05f * revealProgress.value)
                            scaleY = ringScale.value * (0.95f + 0.05f * revealProgress.value)
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val r = size.width / 2f - 4.dp.toPx()

                    drawCircle(
                        color = ringColor,
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    val softGlowBrush = Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.25f * revealProgress.value),
                            Color.Transparent
                        ),
                        center = center,
                        radius = r * 1.15f
                    )

                    drawCircle(
                        brush = softGlowBrush,
                        radius = r * 1.15f,
                        center = center
                    )
                }

                // Central Surface Card with Verified Source & Reveal Interaction
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
                        .clickable {
                            scope.launch {
                                val target = if (revealProgress.value < 0.8f) 1f else 0.5f
                                revealProgress.animateTo(target, spring(stiffness = 300f))
                                if (target == 1f && !hasFiredHaptic) {
                                    hasFiredHaptic = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        .pointerInput(reduceMotion) {
                            if (!reduceMotion) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            val target = if (revealProgress.value > 0.6f) 1f else 0.4f
                                            revealProgress.animateTo(target, spring(stiffness = 350f))
                                            if (target == 1f && !hasFiredHaptic) {
                                                hasFiredHaptic = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            val nextVal = (revealProgress.value - dragAmount / 400f).coerceIn(0.3f, 1f)
                                            revealProgress.snapTo(nextVal)
                                            if (nextVal >= 0.95f && !hasFiredHaptic) {
                                                hasFiredHaptic = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        .padding(22.dp)
                        .graphicsLayer {
                            alpha = surfaceAlpha.value
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Surah Ar-Ra'd (13:28)",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtitleColor
                        )

                        Text(
                            text = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                            fontFamily = AmiriFont,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = arabicColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "\"Indeed, in the remembrance of Allah do hearts find rest.\"",
                            fontFamily = SerifHeaderFont,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = revealProgress.value
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = hintsAlpha.value }
            ) {
                listOf("Quran", "Dhikr", "Dua", "Reflection").forEach { label ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(hintBadgeBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtitleColor
                        )
                    }
                }
            }
        }
    }
}
