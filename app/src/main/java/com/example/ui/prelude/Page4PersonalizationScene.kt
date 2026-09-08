package com.example.ui.prelude

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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

private data class PersonalizationFragment(
    val title: String,
    val icon: ImageVector
)

@Composable
fun Page4PersonalizationScene(
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

    val fragments = remember {
        listOf(
            PersonalizationFragment("Prayer Rhythm", Icons.Filled.Schedule),
            PersonalizationFragment("Quran & Verses", Icons.Filled.Book),
            PersonalizationFragment("Dhikr & Dua", Icons.Filled.Favorite),
            PersonalizationFragment("Spiritual Goals", Icons.Filled.AutoAwesome),
            PersonalizationFragment("Personal Log", Icons.Filled.EventNote),
            PersonalizationFragment("Cloud Sync", Icons.Filled.CloudDone)
        )
    }

    val activeFragments = remember { mutableStateListOf(0, 1, 2) } // default 3 active

    val fragmentAlphas = remember {
        List(fragments.size) { Animatable(if (reduceMotion) 1f else 0f) }
    }
    val fragmentOffsets = remember {
        List(fragments.size) { Animatable(if (reduceMotion) 0f else 12f) }
    }
    val fragmentScales = remember {
        List(fragments.size) { Animatable(1f) }
    }

    val containerAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(100)
            headlineAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
            headlineOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            delay(120)
            subtitleAlpha.animateTo(0.85f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            subtitleOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))

            delay(150)
            containerAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))

            for (i in fragments.indices) {
                fragmentAlphas[i].animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                fragmentOffsets[i].animateTo(0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                delay(60)
            }
        }
    }

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val subtitleColor = if (isDark) Color(0xFFC4BFC9) else Color(0xFF5A585F)
    val cardBg = if (isDark) Color(0xFF110F18).copy(alpha = 0.75f) else Color(0xFFFFFFFF).copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color(0xFF2E2B38) else Color(0xFFE8E2D8)
    val badgeBg = if (isDark) Color(0xFF231E2E) else Color(0xFFF2ECE1)
    val activeBadgeBg = if (isDark) Color(0xFF332B45) else Color(0xFFE8DFC8)
    val iconTint = if (isDark) Color(0xFFC2B6EC) else Color(0xFF8A6D3B)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prelude_page_4")
            .semantics {
                contentDescription = "Personalization scene"
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
                text = "Make it yours.",
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
                text = "Your worship, your progress, your FiveLight.",
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

            // Interactive Assembled Surface Composition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .graphicsLayer {
                        alpha = containerAlpha.value
                    }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = fragments.chunked(2)
                    rows.forEachIndexed { rowIndex, rowFragments ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowFragments.forEachIndexed { colIndex, item ->
                                val index = rowIndex * 2 + colIndex
                                val alpha = fragmentAlphas[index].value
                                val offsetY = fragmentOffsets[index].value
                                val isSelected = activeFragments.contains(index)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) activeBadgeBg else badgeBg)
                                        .border(
                                            width = if (isSelected) 1.dp else 0.dp,
                                            color = if (isSelected) iconTint.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            scope.launch {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (isSelected) {
                                                    activeFragments.remove(index)
                                                } else {
                                                    activeFragments.add(index)
                                                }
                                                fragmentScales[index].animateTo(1.06f, tween(100))
                                                fragmentScales[index].animateTo(1.0f, spring(stiffness = 300f))
                                            }
                                        }
                                        .padding(vertical = 14.dp, horizontal = 12.dp)
                                        .graphicsLayer {
                                            this.alpha = alpha
                                            translationY = offsetY.dp.toPx()
                                            scaleX = fragmentScales[index].value
                                            scaleY = fragmentScales[index].value
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(iconTint.copy(alpha = if (isSelected) 0.3f else 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = item.title,
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
