package com.example.widget.ayah

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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.R
import com.example.widget.core.FiveLightWidgetColors
import com.example.widget.core.FiveLightWidgetTheme
import com.example.widget.core.RefreshAyahActionCallback
import com.example.widget.core.WidgetConstants
import com.example.widget.core.WidgetSizeClass
import com.example.widget.state.DailyAyahStateReader
import com.example.widget.state.DailyAyahWidgetState

/**
 * FiveLight Daily Ayah Widget.
 * Spiritual Quranic reminder with dark translucent glass aesthetics.
 * Pure Arabic & translation presentation without noisy explanation boxes.
 */
class DailyAyahWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            WidgetConstants.SIZE_SMALL,
            WidgetConstants.SIZE_MEDIUM,
            WidgetConstants.SIZE_LARGE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = DailyAyahStateReader.readState(context)

        provideContent {
            val currentContext = LocalContext.current
            val colors = FiveLightWidgetTheme.resolveColors(currentContext)
            DailyAyahWidgetContent(state = state, colors = colors)
        }
    }
}

@Composable
fun DailyAyahWidgetContent(
    state: DailyAyahWidgetState,
    colors: FiveLightWidgetColors
) {
    val size = LocalSize.current
    val sizeClass = WidgetConstants.classifySize(size)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_container_background))
            .clickable(actionRunCallback<RefreshAyahActionCallback>())
            .padding(if (sizeClass == WidgetSizeClass.SMALL) 12.dp else 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        when (sizeClass) {
            WidgetSizeClass.SMALL -> DailyAyahSmallLayout(state = state, colors = colors)
            WidgetSizeClass.MEDIUM -> DailyAyahMediumLayout(state = state, colors = colors)
            WidgetSizeClass.LARGE -> DailyAyahLargeLayout(state = state, colors = colors)
        }
    }
}

/**
 * SMALL SIZE:
 * Arabic ayah only + Verse reference
 * e.g.
 * فَإِنَّ مَعَ الْعُسْرِ يُسْرًا
 * 94:6
 */
@Composable
private fun DailyAyahSmallLayout(
    state: DailyAyahWidgetState,
    colors: FiveLightWidgetColors
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = GlanceModifier.defaultWeight())

        // Arabic Ayah Only
        Text(
            text = state.shortArabicText.ifEmpty { state.arabicText },
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            maxLines = 3
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Reference e.g. 94:6
        Text(
            text = "${state.surahNumber}:${state.verseNumber}",
            style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}

/**
 * MEDIUM SIZE:
 * Daily Ayah
 *
 * Arabic
 *
 * Translation
 *
 * Reference
 */
@Composable
private fun DailyAyahMediumLayout(
    state: DailyAyahWidgetState,
    colors: FiveLightWidgetColors
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Text(
            text = "✦ Daily Ayah",
            style = FiveLightWidgetTheme.headerStyle(colors, fontSize = 12),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Arabic
        Text(
            text = state.shortArabicText.ifEmpty { state.arabicText },
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Translation
        Text(
            text = state.translation,
            style = FiveLightWidgetTheme.supportingStyle(colors, fontSize = 12),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Reference
        Text(
            text = state.reference,
            style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
            maxLines = 1
        )
    }
}

/**
 * LARGE SIZE:
 * Today's Reflection
 *
 * Arabic verse
 *
 * Translation
 *
 * Surah reference
 */
@Composable
private fun DailyAyahLargeLayout(
    state: DailyAyahWidgetState,
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
                text = "✦ Today's Reflection",
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

        // Arabic verse
        Text(
            text = state.arabicText,
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif
            ),
            maxLines = 3
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Translation
        Text(
            text = state.translation,
            style = TextStyle(
                color = ColorProvider(colors.textSecondary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            ),
            maxLines = 3
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Surah reference
        Text(
            text = state.reference,
            style = FiveLightWidgetTheme.metadataStyle(colors, fontSize = 11),
            maxLines = 1
        )
    }
}
