package com.example.widget.personal

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
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.R
import com.example.widget.core.FiveLightWidgetColors
import com.example.widget.core.FiveLightWidgetTheme
import com.example.widget.core.QuickDhikrIncrementCallback
import com.example.widget.core.WidgetConstants
import com.example.widget.core.WidgetSizeClass
import com.example.widget.state.PersonalLogStateReader
import com.example.widget.state.PersonalLogWidgetState

/**
 * FiveLight Personal Log Widget.
 * Floating dark glass panel presenting a personal spiritual snapshot.
 * Supports small Dhikr counter with tap-to-count, and medium/large daily journey views.
 */
class PersonalLogWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            WidgetConstants.SIZE_SMALL,
            WidgetConstants.SIZE_MEDIUM,
            WidgetConstants.SIZE_LARGE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = PersonalLogStateReader.readState(context)

        provideContent {
            val currentContext = LocalContext.current
            val colors = FiveLightWidgetTheme.resolveColors(currentContext)
            PersonalLogWidgetContent(state = state, colors = colors)
        }
    }
}

@Composable
fun PersonalLogWidgetContent(
    state: PersonalLogWidgetState,
    colors: FiveLightWidgetColors
) {
    val size = LocalSize.current
    val sizeClass = WidgetConstants.classifySize(size)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_container_background))
            .padding(if (sizeClass == WidgetSizeClass.SMALL) 12.dp else 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        when (sizeClass) {
            WidgetSizeClass.SMALL -> PersonalLogSmallLayout(state = state, colors = colors)
            WidgetSizeClass.MEDIUM -> PersonalLogMediumLayout(state = state, colors = colors)
            WidgetSizeClass.LARGE -> PersonalLogLargeLayout(state = state, colors = colors)
        }
    }
}

/**
 * SMALL SIZE:
 * Dhikr
 *
 * 342
 */
@Composable
private fun PersonalLogSmallLayout(
    state: PersonalLogWidgetState,
    colors: FiveLightWidgetColors
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<QuickDhikrIncrementCallback>()),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "✦ Dhikr",
            style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 12),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = "${state.todayDhikrCount}",
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(2.dp))

        Text(
            text = "Tap to count",
            style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 10),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}

/**
 * MEDIUM SIZE:
 * Today's Journey
 *
 * Dhikr      342
 *
 * Salah      3/5
 *
 * Quran      Continue
 */
@Composable
private fun PersonalLogMediumLayout(
    state: PersonalLogWidgetState,
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
                text = "✦ Today's Journey",
                style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 12),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = state.dateFormatted,
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 10),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Row 1: Dhikr (Interactive tap-to-increment)
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionRunCallback<QuickDhikrIncrementCallback>())
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dhikr",
                style = TextStyle(
                    color = ColorProvider(colors.textSecondary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${state.todayDhikrCount}",
                style = TextStyle(
                    color = ColorProvider(colors.textPrimary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Row 2: Salah
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salah",
                style = TextStyle(
                    color = ColorProvider(colors.textSecondary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${state.completedPrayersCount}/5",
                style = TextStyle(
                    color = ColorProvider(colors.textPrimary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Row 3: Quran
        val quranStatus = if (state.quranStatusText.isNotBlank()) "Continue" else "Start reading"
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quran",
                style = TextStyle(
                    color = ColorProvider(colors.textSecondary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = quranStatus,
                style = TextStyle(
                    color = ColorProvider(colors.textTertiary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}

/**
 * LARGE SIZE:
 * Today
 *
 * 342 Dhikr
 *
 * 3/5 Salah
 *
 * Surah Al-Kahf
 * Continue reading
 */
@Composable
private fun PersonalLogLargeLayout(
    state: PersonalLogWidgetState,
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
                text = "✦ Today",
                style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 12),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = state.dateFormatted,
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Dhikr item (interactive tap-to-increment)
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionRunCallback<QuickDhikrIncrementCallback>())
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.todayDhikrCount} Dhikr",
                style = TextStyle(
                    color = ColorProvider(colors.textPrimary),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "+1",
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Salah item
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.completedPrayersCount}/5 Salah",
                style = TextStyle(
                    color = ColorProvider(colors.textPrimary),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            val dots = buildString {
                for (i in 0 until 5) {
                    if (i < state.completedPrayersCount) append("● ") else append("○ ")
                }
            }.trim()
            Text(
                text = dots,
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Quran reading position / continue reading
        val surahTitle = if (state.quranStatusText.isNotBlank()) {
            state.quranStatusText
        } else {
            "Surah Al-Kahf"
        }
        val readingAction = if (state.hasQuranActivityToday || state.quranStatusText.isNotBlank()) {
            "Continue reading"
        } else {
            "Start reading"
        }

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = surahTitle,
                style = TextStyle(
                    color = ColorProvider(colors.textSecondary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = readingAction,
                style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}
