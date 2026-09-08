package com.example.ui.prelude

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FiveLightAmbientBackground
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

@Composable
fun PreludeScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isAppInDarkTheme()

    // Detect system reduced motion setting
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

    val pageCount = 6
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    val textColor = if (isDark) Color(0xFFF5F0EB) else Color(0xFF1C1B1F)
    val headerMutedColor = if (isDark) Color(0xFFA09AA8) else Color(0xFF6B6572)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF000000) else Color(0xFFFAF7F2))
            .testTag("fivelight_prelude_screen")
    ) {
        // Shared Ambient Background System
        FiveLightAmbientBackground(modifier = Modifier.fillMaxSize())

        // Horizontal Pager for Pages 0 to 5
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> Page0OpeningScene(reduceMotion = reduceMotion)
                1 -> Page1PrayerScene(reduceMotion = reduceMotion)
                2 -> Page2RemembranceScene(reduceMotion = reduceMotion)
                3 -> Page3QiblaScene(reduceMotion = reduceMotion)
                4 -> Page4PersonalizationScene(reduceMotion = reduceMotion)
                5 -> Page5ClosingScene(
                    onBeginJourney = onComplete,
                    reduceMotion = reduceMotion
                )
            }
        }

        // Header Controls: Progress Indicator + Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress Indicator (e.g., "01 / 06")
            val formattedProgress = "0${pagerState.currentPage + 1} / 0$pageCount"
            Text(
                text = formattedProgress,
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = headerMutedColor,
                modifier = Modifier.testTag("prelude_progress_indicator")
            )

            // Subtle Skip Button
            Surface(
                onClick = onSkip,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.testTag("prelude_skip_button")
            ) {
                Text(
                    text = "Skip",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = headerMutedColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom Controls: Accessible Next / Continue Button (pages 0..4)
        if (pagerState.currentPage < pageCount - 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF252130) else Color(0xFFEFE8DD),
                    modifier = Modifier.testTag("prelude_next_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next prelude page",
                            tint = textColor,
                            modifier = Modifier.height(16.dp)
                        )
                    }
                }
            }
        }
    }
}
