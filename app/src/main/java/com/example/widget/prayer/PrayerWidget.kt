package com.example.widget.prayer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.R
import com.example.widget.core.FiveLightWidgetColors
import com.example.widget.core.FiveLightWidgetTheme
import com.example.widget.core.RefreshPrayerActionCallback
import com.example.widget.core.WidgetConstants
import com.example.widget.core.WidgetSizeClass
import com.example.widget.state.PrayerWidgetState
import com.example.widget.state.PrayerWidgetStateReader

/**
 * FiveLight Current Prayer Widget.
 * Designed as a silent spiritual companion living quietly on the home screen.
 * Floating dark glass panel aesthetics, human typography, zero card-in-card clutter.
 */
class PrayerWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            WidgetConstants.SIZE_SMALL,
            WidgetConstants.SIZE_MEDIUM,
            WidgetConstants.SIZE_LARGE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = PrayerWidgetStateReader.readState(context)

        provideContent {
            val currentContext = LocalContext.current
            val colors = FiveLightWidgetTheme.resolveColors(currentContext)
            PrayerWidgetContent(state = state, colors = colors)
        }
    }
}

@Composable
fun PrayerWidgetContent(
    state: PrayerWidgetState,
    colors: FiveLightWidgetColors
) {
    val size = LocalSize.current
    val sizeClass = WidgetConstants.classifySize(size)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_container_background))
            .clickable(actionRunCallback<RefreshPrayerActionCallback>())
            .padding(if (sizeClass == WidgetSizeClass.SMALL) 12.dp else 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        when (sizeClass) {
            WidgetSizeClass.SMALL -> PrayerSmallLayout(state = state, colors = colors)
            WidgetSizeClass.MEDIUM -> PrayerMediumLayout(state = state, colors = colors)
            WidgetSizeClass.LARGE -> PrayerLargeLayout(state = state, colors = colors)
        }
    }
}

/**
 * SMALL SIZE:
 * ✦ Next Prayer
 *
 * Asr
 *
 * 24 min
 *
 * 03:58 PM
 */
@Composable
private fun PrayerSmallLayout(
    state: PrayerWidgetState,
    colors: FiveLightWidgetColors
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Minimal brand header
        Text(
            text = "✦ Next Prayer",
            style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 11),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Large prayer name
        Text(
            text = state.nextPrayerName.ifEmpty { "Salah" },
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Time remaining
        val remainingText = if (state.nextPrayerRemainingMinutes > 0) {
            "${state.nextPrayerRemainingMinutes} min"
        } else {
            state.nextPrayerRemainingFormatted.ifEmpty { "Due now" }
        }
        Text(
            text = remainingText,
            style = FiveLightWidgetTheme.supportingStyle(colors, fontSize = 12),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Scheduled time
        if (state.nextPrayerTimeFormatted.isNotBlank()) {
            Text(
                text = state.nextPrayerTimeFormatted,
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}

/**
 * MEDIUM SIZE:
 * ✦ Salah
 *
 * Asr
 *
 * in 24 minutes
 *
 * Today's progress
 * ● ● ● ○ ○
 */
@Composable
private fun PrayerMediumLayout(
    state: PrayerWidgetState,
    colors: FiveLightWidgetColors
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✦ Salah",
                style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 12),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            if (state.cityName.isNotBlank()) {
                Text(
                    text = state.cityName,
                    style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Large prayer name with optional subtle Arabic
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.nextPrayerName.ifEmpty { "Salah" },
                style = TextStyle(
                    color = ColorProvider(colors.textPrimary),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            if (state.nextPrayerArabicName.isNotBlank()) {
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = state.nextPrayerArabicName,
                    style = FiveLightWidgetTheme.arabicScriptureStyle(colors, fontSize = 14),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // in 24 minutes
        val timeNotice = if (state.nextPrayerRemainingMinutes > 0) {
            "in ${state.nextPrayerRemainingMinutes} minutes"
        } else {
            state.nextPrayerRemainingFormatted.ifEmpty { "Due now" }
        }
        val fullTimeNotice = if (state.nextPrayerTimeFormatted.isNotBlank()) {
            "$timeNotice • ${state.nextPrayerTimeFormatted}"
        } else {
            timeNotice
        }

        Text(
            text = fullTimeNotice,
            style = FiveLightWidgetTheme.supportingStyle(colors, fontSize = 12),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Today's progress & Dots
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's progress",
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            val dots = buildString {
                for (i in 0 until 5) {
                    val p = state.fardPrayers.getOrNull(i)
                    if (p?.isCompleted == true) {
                        append("● ")
                    } else {
                        append("○ ")
                    }
                }
            }.trim()
            Text(
                text = dots,
                style = TextStyle(
                    color = ColorProvider(colors.textPrimary),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * LARGE SIZE:
 * Today's Salah Journey
 *
 * ✓ Fajr
 * ✓ Dhuhr
 * → Asr
 * ○ Maghrib
 * ○ Isha
 *
 * Next:
 * Maghrib
 * 42 min
 */
@Composable
private fun PrayerLargeLayout(
    state: PrayerWidgetState,
    colors: FiveLightWidgetColors
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✦ Today's Salah Journey",
                style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 12),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${state.completedPrayersCount}/5 completed",
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Content split into Journey (Left) and Next Spotlight (Right)
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: The 5 Prayers
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.fardPrayers.forEach { prayer ->
                    val marker = when {
                        prayer.isCompleted -> "✓"
                        prayer.isNext -> "→"
                        else -> "○"
                    }
                    val textColor = when {
                        prayer.isNext -> colors.textPrimary
                        prayer.isCompleted -> colors.textSecondary
                        else -> colors.textTertiary
                    }
                    val weight = if (prayer.isNext) FontWeight.Medium else FontWeight.Normal

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = marker,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 13.sp,
                                fontWeight = weight,
                                fontFamily = FontFamily.SansSerif
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = prayer.name,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 13.sp,
                                fontWeight = weight,
                                fontFamily = FontFamily.SansSerif
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = prayer.timeFormatted,
                            style = TextStyle(
                                color = ColorProvider(colors.textTertiary),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.width(18.dp))

            // Right Column: Next Spotlight
            Column(
                modifier = GlanceModifier
                    .width(96.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Next",
                    style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 10),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = state.nextPrayerName.ifEmpty { "Salah" },
                    style = TextStyle(
                        color = ColorProvider(colors.textPrimary),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                val remainingShort = if (state.nextPrayerRemainingMinutes > 0) {
                    "${state.nextPrayerRemainingMinutes} min"
                } else {
                    state.nextPrayerRemainingFormatted.ifEmpty { "Due now" }
                }
                Text(
                    text = remainingShort,
                    style = FiveLightWidgetTheme.supportingStyle(colors, fontSize = 12),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = state.nextPrayerTimeFormatted,
                    style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                    maxLines = 1
                )
            }
        }
    }
}
