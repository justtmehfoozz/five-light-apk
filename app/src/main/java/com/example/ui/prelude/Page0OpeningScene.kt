package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

@Composable
fun Page0OpeningScene(
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val scope = rememberCoroutineScope()

    val wordmarkAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val wordmarkOffsetY = remember { Animatable(if (reduceMotion) 0f else 8f) }
    val subtitleAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val subtitleOffsetY = remember { Animatable(if (reduceMotion) 0f else 6f) }
    val glowAlpha = remember { Animatable(if (reduceMotion) 0.6f else 0f) }

    // Interactive ambient touch tracking offset
    val touchOffsetX = remember { Animatable(0f) }
    val touchOffsetY = remember { Animatable(0f) }
    val touchGlowScale = remember { Animatable(1f) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(150)
            glowAlpha.animateTo(0.6f, animationSpec = tween(400, easing = FastOutSlowInEasing))

            wordmarkAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            wordmarkOffsetY.animateTo(0f, animationSpec = tween(500, easing = FastOutSlowInEasing))

            delay(200)
            subtitleAlpha.animateTo(0.85f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            subtitleOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val subtitleColor = if (isDark) Color(0xFFC4BFC9) else Color(0xFF5A585F)
    val ambientLightColor = if (isDark) Color(0xFFC2B6EC) else Color(0xFF8A6D3B)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_0")
            .semantics {
                contentDescription = "FiveLight Opening scene"
            }
            .pointerInput(reduceMotion) {
                if (!reduceMotion) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            scope.launch {
                                touchGlowScale.animateTo(1.25f, tween(300))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                touchOffsetX.animateTo(0f, spring(stiffness = 300f))
                                touchOffsetY.animateTo(0f, spring(stiffness = 300f))
                                touchGlowScale.animateTo(1f, tween(300))
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                touchOffsetX.animateTo(0f, spring(stiffness = 300f))
                                touchOffsetY.animateTo(0f, spring(stiffness = 300f))
                                touchGlowScale.animateTo(1f, tween(300))
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val currentX = touchOffsetX.value + dragAmount.x * 0.25f
                                val currentY = touchOffsetY.value + dragAmount.y * 0.25f
                                touchOffsetX.snapTo(currentX.coerceIn(-120f, 120f))
                                touchOffsetY.snapTo(currentY.coerceIn(-120f, 120f))
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Interactive light follower ambient bloom
        Canvas(
            modifier = Modifier
                .size(320.dp)
                .graphicsLayer {
                    translationX = touchOffsetX.value
                    translationY = touchOffsetY.value
                    scaleX = touchGlowScale.value
                    scaleY = touchGlowScale.value
                    alpha = glowAlpha.value
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    ambientLightColor.copy(alpha = 0.22f),
                    ambientLightColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = center,
                radius = size.width / 2f
            )
            drawCircle(brush = glowBrush, radius = size.width / 2f, center = center)
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
                    modifier = Modifier.graphicsLayer {
                        alpha = wordmarkAlpha.value
                        translationY = wordmarkOffsetY.value.dp.toPx()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
