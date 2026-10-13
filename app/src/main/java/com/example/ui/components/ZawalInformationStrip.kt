package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.util.ZawalDisplayMode
import com.example.data.util.ZawalInfo
import com.example.ui.theme.SpaceGrotesk

/**
 * Zawal Information Strip
 *
 * Displays a compact, full-width Zawal (solar zenith restricted prayer interval) strip
 * directly below the Daily Prayers section heading.
 *
 * Color Palette:
 * - Accent Crimson Red: #E5484D
 * - Theme-aware light and dark mode surfaces with subtle red border and background tint.
 */
@Composable
fun ZawalInformationStrip(
    zawalInfo: ZawalInfo,
    modifier: Modifier = Modifier
) {
    val isVisible = zawalInfo.mode != ZawalDisplayMode.HIDDEN
    val isDark = isSystemInDarkTheme()

    // Color definitions using refined red palette (#E5484D)
    val zawalRedAccent = Color(0xFFE5484D)
    val zawalRedTitle = if (isDark) Color(0xFFFF6B6B) else Color(0xFFE5484D)
    val zawalBorderColor = zawalRedAccent.copy(alpha = if (isDark) 0.35f else 0.25f)
    val zawalContainerBg = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val zawalTintOverlay = zawalRedAccent.copy(alpha = if (isDark) 0.12f else 0.06f)
    val zawalChipBg = zawalRedAccent.copy(alpha = if (isDark) 0.20f else 0.12f)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220), expandFrom = Alignment.Top),
        exit = fadeOut(animationSpec = tween(220)) + shrinkVertically(animationSpec = tween(220), shrinkTowards = Alignment.Top),
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp, max = 64.dp)
                .testTag("zawal_information_strip"),
            shape = RoundedCornerShape(16.dp),
            color = zawalContainerBg,
            border = BorderStroke(1.dp, zawalBorderColor),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(zawalTintOverlay)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon + Text Block
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Refined Red Sun Icon
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = stringResource(R.string.zawal_title),
                        tint = zawalRedTitle,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AnimatedContent(
                        targetState = zawalInfo.mode,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        label = "zawal_text_transition"
                    ) { mode ->
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            val titleText = when (mode) {
                                ZawalDisplayMode.APPROACHING -> stringResource(R.string.zawal_approaching_title)
                                ZawalDisplayMode.ACTIVE -> stringResource(R.string.zawal_title)
                                ZawalDisplayMode.HIDDEN -> ""
                            }

                            val subtext = when (mode) {
                                ZawalDisplayMode.APPROACHING -> stringResource(
                                    R.string.zawal_approaching_subtext,
                                    zawalInfo.zawalStartTimeFormatted
                                )
                                ZawalDisplayMode.ACTIVE -> stringResource(R.string.zawal_active_subtext)
                                ZawalDisplayMode.HIDDEN -> ""
                            }

                            Text(
                                text = titleText,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = zawalRedTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (subtext.isNotEmpty()) {
                                Text(
                                    text = subtext,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Countdown Chip
                AnimatedContent(
                    targetState = Pair(zawalInfo.mode, zawalInfo.minutesRemaining),
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "zawal_countdown_transition"
                ) { (mode, mins) ->
                    val countdownText = when (mode) {
                        ZawalDisplayMode.APPROACHING -> stringResource(R.string.zawal_approaching_countdown, mins)
                        ZawalDisplayMode.ACTIVE -> stringResource(R.string.zawal_active_countdown, mins)
                        ZawalDisplayMode.HIDDEN -> ""
                    }

                    if (countdownText.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = zawalChipBg
                        ) {
                            Text(
                                text = countdownText,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = zawalRedTitle,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
