package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class PrayerMomentItem(
    val name: String,
    val time: String,
    val isKey: Boolean = false
)

@Composable
fun Page1PrayerScene(
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

    val timelineProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val lightPointX = remember { Animatable(if (reduceMotion) 0.5f else 0f) }

    val prayerAlphas = remember {
        List(5) { Animatable(if (reduceMotion) 1f else 0f) }
    }

    var activeMilestoneIndex by remember { mutableIntStateOf(2) } // default Asr (0.5)
    var lastHapticIndex by remember { mutableIntStateOf(-1) }

    val prayers = remember {
        listOf(
            PrayerMomentItem("Fajr", "05:12 AM"),
            PrayerMomentItem("Dhuhr", "12:30 PM"),
            PrayerMomentItem("Asr", "03:45 PM", isKey = true),
            PrayerMomentItem("Maghrib", "06:50 PM"),
            PrayerMomentItem("Isha", "08:15 PM")
        )
    }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(100)
            headlineAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            headlineOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            delay(120)
            subtitleAlpha.animateTo(0.85f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            subtitleOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))

            delay(150)
            timelineProgress.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

            for (i in prayers.indices) {
                val targetFraction = (i * 0.25f).coerceIn(0f, 1f)
                lightPointX.animateTo(targetFraction, animationSpec = tween(220, easing = FastOutSlowInEasing))
                prayerAlphas[i].animateTo(1f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                delay(80)
            }

            lightPointX.animateTo(0.5f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val subtitleColor = if (isDark) Color(0xFFC4BFC9) else Color(0xFF5A585F)
    val cardBg = if (isDark) Color(0xFF110F18).copy(alpha = 0.7f) else Color(0xFFFFFFFF).copy(alpha = 0.8f)
    val cardBorder = if (isDark) Color(0xFF2E2B38) else Color(0xFFE8E2D8)
    val lineBaseColor = if (isDark) Color(0xFF332F42) else Color(0xFFE0D8CB)
    val lineGlowColor = if (isDark) Color(0xFFC2B6EC) else Color(0xFF9E8462)
    val activeDotColor = if (isDark) Color(0xFFEADDFE) else Color(0xFF8A6D3B)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_1")
            .semantics {
                contentDescription = "Prayer timeline scene"
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
                text = "Begin with what matters.",
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
                text = "Your prayers, your day, beautifully in rhythm.",
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

            // Interactive Prayer Rhythm Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .pointerInput(reduceMotion) {
                        if (!reduceMotion) {
                            detectDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        val nearestFraction = activeMilestoneIndex * 0.25f
                                        lightPointX.animateTo(nearestFraction, spring(stiffness = 350f))
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        val newFrac = (lightPointX.value + dragAmount.x / 600f).coerceIn(0f, 1f)
                                        lightPointX.snapTo(newFrac)

                                        val closestIndex = (newFrac / 0.25f).roundToInt().coerceIn(0, 4)
                                        if (closestIndex != activeMilestoneIndex) {
                                            activeMilestoneIndex = closestIndex
                                        }

                                        if (closestIndex != lastHapticIndex) {
                                            lastHapticIndex = closestIndex
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                }
                            )
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .padding(horizontal = 12.dp)
                    ) {
                        val w = size.width
                        val y = size.height / 2f

                        drawLine(
                            color = lineBaseColor,
                            start = Offset(0f, y),
                            end = Offset(w * timelineProgress.value, y),
                            strokeWidth = 2.dp.toPx()
                        )

                        val curX = w * lightPointX.value.coerceIn(0f, 1f)
                        val glowBrush = Brush.radialGradient(
                            colors = listOf(
                                lineGlowColor,
                                lineGlowColor.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(curX, y),
                            radius = 22.dp.toPx()
                        )

                        drawCircle(
                            brush = glowBrush,
                            radius = 22.dp.toPx(),
                            center = Offset(curX, y)
                        )

                        drawCircle(
                            color = activeDotColor,
                            radius = 5.dp.toPx(),
                            center = Offset(curX, y)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prayers.forEachIndexed { idx, item ->
                            val alpha = prayerAlphas[idx].value
                            val isFocused = idx == activeMilestoneIndex

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .graphicsLayer {
                                        this.alpha = if (isFocused) 1f else (alpha * 0.65f)
                                        scaleX = if (isFocused) 1.1f else 1.0f
                                        scaleY = if (isFocused) 1.1f else 1.0f
                                    }
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isFocused) 12.dp else 7.dp)
                                        .clip(CircleShape)
                                        .background(if (isFocused) activeDotColor else lineGlowColor.copy(alpha = 0.6f))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.name,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 13.sp,
                                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isFocused) textColor else textColor.copy(alpha = 0.75f)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.time,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 10.sp,
                                    fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isFocused) textColor else subtitleColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
