package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FiveLightHaptics
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.LocalVibrationEnabled
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Page3QiblaScene(
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val isVibrationEnabled = LocalVibrationEnabled.current

    val headlineAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val headlineOffsetY = remember { Animatable(if (reduceMotion) 0f else 8f) }
    val subtitleAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val subtitleOffsetY = remember { Animatable(if (reduceMotion) 0f else 6f) }

    val compassAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val compassRotation = remember { Animatable(if (reduceMotion) 135f else 0f) }
    val targetGlowAlpha = remember { Animatable(if (reduceMotion) 0.8f else 0f) }

    var wasAligned by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(100)
            headlineAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            headlineOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            delay(120)
            subtitleAlpha.animateTo(0.85f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            subtitleOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))

            delay(150)
            compassAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))

            compassRotation.animateTo(135f, animationSpec = tween(1200, easing = FastOutSlowInEasing))

            delay(100)
            targetGlowAlpha.animateTo(0.85f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            wasAligned = true

            // Context-Aware Interaction Cue (runs once if not interacted)
            delay(350)
            if (!hasInteracted) {
                compassRotation.animateTo(141f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                if (!hasInteracted) {
                    compassRotation.animateTo(135f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val subtitleColor = if (isDark) Color(0xFFC4BFC9) else Color(0xFF5A585F)
    val ringColor = if (isDark) Color(0xFF9B90C2).copy(alpha = 0.5f) else Color(0xFFC8B39B).copy(alpha = 0.6f)
    val tickColor = if (isDark) Color(0xFF524B66) else Color(0xFFD4C8B8)
    val needleColor = if (isDark) Color(0xFFE2D8FD) else Color(0xFF8A6D3B)
    val targetGlowColor = if (isDark) Color(0xFFD8CCFE) else Color(0xFF8A6D3B)
    val successColor = if (isDark) Color(0xFFB5E8C5) else Color(0xFF2E7D32)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_3")
            .semantics {
                contentDescription = "Qibla compass scene"
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
                text = "Find your direction.",
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
                text = "Accurate Qibla and prayer guidance, wherever you are.",
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

            Spacer(modifier = Modifier.height(38.dp))

            // Interactive Qibla Compass
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        alpha = compassAlpha.value
                    }
                    .pointerInput(reduceMotion, isVibrationEnabled) {
                        if (!reduceMotion) {
                            detectDragGestures(
                                onDragStart = {
                                    hasInteracted = true
                                },
                                onDragEnd = {
                                    scope.launch {
                                        val diff = abs(compassRotation.value - 135f) % 360f
                                        if (diff < 22f || diff > 338f) {
                                            compassRotation.animateTo(
                                                targetValue = 135f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    hasInteracted = true
                                    scope.launch {
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val pos = change.position
                                        val dx = pos.x - center.x
                                        val dy = pos.y - center.y
                                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                        if (angleDeg < 0) angleDeg += 360f

                                        compassRotation.snapTo(angleDeg)

                                        val angleDiff = abs(angleDeg - 135f) % 360f
                                        val isAligned = angleDiff < 14f || angleDiff > 346f

                                        targetGlowAlpha.snapTo(if (isAligned) 1.0f else (1f - (angleDiff / 180f)).coerceIn(0.2f, 0.7f))

                                        if (isAligned && !wasAligned) {
                                            wasAligned = true
                                            FiveLightHaptics.performMediumTap(view, haptic, isVibrationEnabled)
                                        } else if (!isAligned && wasAligned) {
                                            wasAligned = false
                                        }
                                    }
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.width / 2f - 12.dp.toPx()
                    val innerRadius = outerRadius - 16.dp.toPx()

                    drawCircle(
                        color = ringColor,
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    val numTicks = 36
                    for (i in 0 until numTicks) {
                        val angleRad = (i * (360f / numTicks)) * (PI / 180f)
                        val isCardinal = i % 9 == 0
                        val tLen = if (isCardinal) 8.dp.toPx() else 4.dp.toPx()

                        val p1 = Offset(
                            x = center.x + (outerRadius - tLen) * cos(angleRad).toFloat(),
                            y = center.y + (outerRadius - tLen) * sin(angleRad).toFloat()
                        )
                        val p2 = Offset(
                            x = center.x + outerRadius * cos(angleRad).toFloat(),
                            y = center.y + outerRadius * sin(angleRad).toFloat()
                        )

                        drawLine(
                            color = if (isCardinal) ringColor else tickColor,
                            start = p1,
                            end = p2,
                            strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    val rotRad = (compassRotation.value - 90f) * (PI / 180f)
                    val targetX = center.x + innerRadius * cos(rotRad).toFloat()
                    val targetY = center.y + innerRadius * sin(rotRad).toFloat()
                    val targetOffset = Offset(targetX, targetY)

                    val curGlowColor = if (wasAligned) successColor else targetGlowColor

                    drawLine(
                        color = curGlowColor.copy(alpha = 0.6f),
                        start = center,
                        end = targetOffset,
                        strokeWidth = 2.dp.toPx()
                    )

                    drawCircle(
                        color = textColor,
                        radius = 4.dp.toPx(),
                        center = center
                    )

                    val glowBrush = Brush.radialGradient(
                        colors = listOf(
                            curGlowColor.copy(alpha = targetGlowAlpha.value),
                            curGlowColor.copy(alpha = targetGlowAlpha.value * 0.3f),
                            Color.Transparent
                        ),
                        center = targetOffset,
                        radius = 26.dp.toPx()
                    )

                    drawCircle(
                        brush = glowBrush,
                        radius = 26.dp.toPx(),
                        center = targetOffset
                    )

                    drawCircle(
                        color = curGlowColor,
                        radius = 6.dp.toPx(),
                        center = targetOffset
                    )
                }
            }
        }
    }
}
