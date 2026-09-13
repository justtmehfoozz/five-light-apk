package com.example.widget.ayah

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.data.util.DailyContentProvider
import com.example.data.util.DailyReflection
import com.example.widget.theme.WidgetTheme
import com.example.widget.util.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * FiveLight Daily Ayah Widget using Jetpack Glance.
 * Presents authoritative Quranic verse with prominent RTL Arabic typography,
 * secondary translation, and subtle reference.
 */
class DailyAyahWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(220.dp, 110.dp), // Medium
            DpSize(260.dp, 220.dp)  // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val reflection = withContext(Dispatchers.IO) {
            getDailyAyah(appContext)
        }

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                val isLarge = size.height >= 180.dp && size.width >= 240.dp

                Box(modifier = WidgetTheme.frostedGlassContainer()) {
                    if (isLarge) {
                        LargeDailyAyahLayout(reflection)
                    } else {
                        MediumDailyAyahLayout(reflection)
                    }
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "fivelight_widget_prefs"
        private const val KEY_AYAH_OFFSET = "key_ayah_offset"

        fun getDailyAyah(context: Context): DailyReflection {
            val reflections = DailyContentProvider.getReflections()
            if (reflections.isEmpty()) {
                return DailyReflection(
                    id = 1,
                    arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    translation = "Indeed, in the remembrance of Allah do hearts find rest.",
                    reference = "13:28",
                    surahNumber = 13,
                    verseNumber = 28
                )
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val offset = prefs.getInt(KEY_AYAH_OFFSET, 0)
            val dayEpoch = try {
                LocalDate.now().toEpochDay()
            } catch (_: Exception) {
                System.currentTimeMillis() / (24 * 3600 * 1000L)
            }

            val totalIndex = ((dayEpoch + offset) % reflections.size).let {
                val rem = (it % reflections.size).toInt()
                if (rem < 0) rem + reflections.size else rem
            }

            val item = reflections[totalIndex]
            // Format reference strictly as surah:verse if available
            val refFormatted = if (item.surahNumber > 0 && item.verseNumber > 0) {
                "${item.surahNumber}:${item.verseNumber}"
            } else {
                item.reference
            }

            return item.copy(reference = refFormatted)
        }

        fun cycleNextAyah(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentOffset = prefs.getInt(KEY_AYAH_OFFSET, 0)
            prefs.edit().putInt(KEY_AYAH_OFFSET, currentOffset + 1).apply()
        }
    }
}

@androidx.compose.runtime.Composable
private fun MediumDailyAyahLayout(item: DailyReflection) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        // Subtle Title
        Text(
            text = "Daily Ayah",
            style = WidgetTheme.titleStyle
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Visually dominant Arabic verse (RTL)
        Text(
            text = item.arabic,
            style = TextStyle(
                color = WidgetTheme.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            ),
            modifier = GlanceModifier.fillMaxWidth(),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Secondary Translation
        Text(
            text = item.translation.removeSurrounding("\""),
            style = WidgetTheme.bodyMutedStyle,
            modifier = GlanceModifier.fillMaxWidth(),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(3.dp))

        // Subtle Reference
        Text(
            text = item.reference,
            style = WidgetTheme.captionStyle
        )
    }
}

@androidx.compose.runtime.Composable
private fun LargeDailyAyahLayout(item: DailyReflection) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header row with "Daily Ayah" and subtle shuffle action
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Ayah",
                style = WidgetTheme.titleStyle,
                modifier = GlanceModifier.defaultWeight()
            )

            Button(
                text = "↻ Next",
                onClick = actionRunCallback<ShuffleAyahActionCallback>(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = WidgetTheme.pillBackground,
                    contentColor = WidgetTheme.textMuted
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Prominent Arabic verse with generous spacing
        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.arabic,
                style = TextStyle(
                    color = WidgetTheme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(10.dp))

            Text(
                text = item.translation.removeSurrounding("\""),
                style = TextStyle(
                    color = WidgetTheme.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Minimal reference at bottom
        Text(
            text = item.reference,
            style = WidgetTheme.captionStyle
        )
    }
}

class ShuffleAyahActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        try {
            DailyAyahWidget.cycleNextAyah(context.applicationContext)
            DailyAyahWidget().update(context, glanceId)
        } catch (e: Exception) {
            android.util.Log.e("DailyAyahWidget", "Failed cycling ayah", e)
        }
    }
}

class DailyAyahWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyAyahWidget()

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == android.content.Intent.ACTION_DATE_CHANGED ||
            action == android.content.Intent.ACTION_TIMEZONE_CHANGED ||
            action == android.content.Intent.ACTION_TIME_CHANGED) {
            WidgetUpdateHelper.triggerDailyAyahWidgetUpdate(context)
        }
    }
}
