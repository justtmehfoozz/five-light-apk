package com.example.widget.prayer

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.PrayerLogEntity
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import com.example.data.repository.AppRepository
import com.example.widget.theme.WidgetTheme
import com.example.widget.util.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * FiveLight Prayer Widget using Jetpack Glance.
 * Shows current/next prayer, countdown, and astronomical prayer arc in responsive layouts.
 */
class PrayerWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 100.dp), // Small
            DpSize(220.dp, 110.dp), // Medium
            DpSize(260.dp, 220.dp)  // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val widgetData = withContext(Dispatchers.IO) {
            loadPrayerData(appContext)
        }

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                val isSmall = size.width < 210.dp || size.height < 125.dp
                val isMedium = !isSmall && (size.height < 190.dp || size.width < 250.dp)

                Box(modifier = WidgetTheme.frostedGlassContainer()) {
                    when {
                        isSmall -> SmallPrayerLayout(widgetData)
                        isMedium -> MediumPrayerLayout(widgetData)
                        else -> LargePrayerLayout(widgetData)
                    }
                }
            }
        }
    }

    private suspend fun loadPrayerData(context: Context): PrayerWidgetData {
        val db = AppDatabase.getDatabase(context)
        val repository = AppRepository(db, context)
        val prayerTimes = repository.getTodayPrayerTimes()
        val todayStr = repository.getTodayDateString()
        val todayLog = db.prayerLogDao().getPrayerLogForDateDirect(todayStr)

        val fardPrayers = prayerTimes.filter { it.name != PrayerName.SUNRISE }
        val now = System.currentTimeMillis()

        val nextPrayer = fardPrayers.find { it.isNext }
        val activePrayerItem: PrayerItem
        val countdownText: String

        if (nextPrayer != null) {
            activePrayerItem = nextPrayer
            val diffMins = ((nextPrayer.timeMillis - now) / 60000L).coerceAtLeast(0)
            val hours = diffMins / 60
            val mins = diffMins % 60
            countdownText = when {
                diffMins == 0L -> "now"
                hours > 0 -> "in ${hours}h ${mins}m"
                else -> "in ${mins} minutes"
            }
        } else {
            // All today's prayers passed, next is tomorrow Fajr
            val firstFajr = fardPrayers.firstOrNull { it.name == PrayerName.FAJR }
            val tomorrowFajrMillis = (firstFajr?.timeMillis ?: now) + 86400000L
            activePrayerItem = firstFajr?.copy(
                timeMillis = tomorrowFajrMillis,
                isNext = true
            ) ?: PrayerItem(PrayerName.FAJR, "05:00 AM", tomorrowFajrMillis, isNext = true)

            val diffMins = ((tomorrowFajrMillis - now) / 60000L).coerceAtLeast(0)
            val hours = diffMins / 60
            val mins = diffMins % 60
            countdownText = if (hours > 0) "in ${hours}h ${mins}m" else "in ${mins}m"
        }

        // Calculate progress for prayer arc (Fajr to Isha)
        val fajrTime = fardPrayers.firstOrNull { it.name == PrayerName.FAJR }?.timeMillis ?: (now - 3600000)
        val ishaTime = fardPrayers.firstOrNull { it.name == PrayerName.ISHA }?.timeMillis ?: (now + 3600000)
        val isNight = now < fajrTime || now > ishaTime

        val arcProgress = if (!isNight && ishaTime > fajrTime) {
            ((now - fajrTime).toFloat() / (ishaTime - fajrTime).toFloat()).coerceIn(0.0f, 1.0f)
        } else {
            1.0f
        }

        val isDark = WidgetUpdateHelper.isSystemDarkMode(context)
        val arcBitmap = PrayerArcRenderer.generateArcBitmap(
            widthPx = 280,
            heightPx = 70,
            progressFraction = arcProgress,
            isNight = isNight,
            isDarkMode = isDark
        )

        val prayerEntries = fardPrayers.map { p ->
            PrayerRowData(
                name = p.name,
                displayName = p.name.displayName,
                timeFormatted = p.timeFormatted,
                isCurrentOrNext = p.name == activePrayerItem.name,
                isCompleted = todayLog?.isCompleted(p.name) == true
            )
        }

        return PrayerWidgetData(
            targetPrayerName = activePrayerItem.name.displayName,
            targetPrayerEnum = activePrayerItem.name,
            targetPrayerTime = activePrayerItem.timeFormatted,
            countdown = countdownText,
            arcBitmap = arcBitmap,
            prayerList = prayerEntries,
            isTargetCompleted = todayLog?.isCompleted(activePrayerItem.name) == true
        )
    }
}

data class PrayerWidgetData(
    val targetPrayerName: String,
    val targetPrayerEnum: PrayerName,
    val targetPrayerTime: String,
    val countdown: String,
    val arcBitmap: Bitmap,
    val prayerList: List<PrayerRowData>,
    val isTargetCompleted: Boolean
)

data class PrayerRowData(
    val name: PrayerName,
    val displayName: String,
    val timeFormatted: String,
    val isCurrentOrNext: Boolean,
    val isCompleted: Boolean
)

@androidx.compose.runtime.Composable
private fun SmallPrayerLayout(data: PrayerWidgetData) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Next Prayer",
            style = WidgetTheme.titleStyle
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = data.targetPrayerName,
            style = WidgetTheme.heroStyle
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = data.targetPrayerTime,
            style = WidgetTheme.subHeroStyle
        )
    }
}

@androidx.compose.runtime.Composable
private fun MediumPrayerLayout(data: PrayerWidgetData) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Next Prayer",
                style = WidgetTheme.titleStyle
            )
            Spacer(modifier = GlanceModifier.height(3.dp))
            Text(
                text = data.targetPrayerName,
                style = WidgetTheme.heroStyle
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = data.targetPrayerTime,
                style = WidgetTheme.subHeroStyle
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = data.countdown,
                style = WidgetTheme.bodyMutedStyle
            )
        }

        Column(
            modifier = GlanceModifier.width(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(data.arcBitmap),
                contentDescription = "Prayer day arc",
                modifier = GlanceModifier.fillMaxWidth().height(48.dp)
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun LargePrayerLayout(data: PrayerWidgetData) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header row with title, countdown, and astronomical arc
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Salah",
                    style = WidgetTheme.titleStyle
                )
                Spacer(modifier = GlanceModifier.height(1.dp))
                Text(
                    text = "${data.targetPrayerName} · ${data.countdown}",
                    style = TextStyle(
                        color = WidgetTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Image(
                provider = ImageProvider(data.arcBitmap),
                contentDescription = "Prayer day arc",
                modifier = GlanceModifier.width(90.dp).height(36.dp)
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Full 5 fard prayer list
        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.prayerList.forEach { prayer ->
                val rowModifier = if (prayer.isCurrentOrNext) {
                    GlanceModifier
                        .fillMaxWidth()
                        .cornerRadius(10.dp)
                        .background(ImageProvider(R.drawable.widget_pill_bg))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                } else {
                    GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                }

                Row(
                    modifier = rowModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicator dot or status mark
                    Text(
                        text = when {
                            prayer.isCompleted -> "✓ "
                            prayer.isCurrentOrNext -> "● "
                            else -> "· "
                        },
                        style = TextStyle(
                            color = if (prayer.isCurrentOrNext) WidgetTheme.textPrimary else WidgetTheme.textMuted,
                            fontSize = 12.sp,
                            fontWeight = if (prayer.isCurrentOrNext) FontWeight.Bold else FontWeight.Normal
                        )
                    )

                    Text(
                        text = prayer.displayName,
                        style = TextStyle(
                            color = if (prayer.isCurrentOrNext) WidgetTheme.textPrimary else WidgetTheme.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (prayer.isCurrentOrNext) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )

                    Text(
                        text = prayer.timeFormatted,
                        style = TextStyle(
                            color = if (prayer.isCurrentOrNext) WidgetTheme.textPrimary else WidgetTheme.textMuted,
                            fontSize = 12.sp,
                            fontWeight = if (prayer.isCurrentOrNext) FontWeight.Medium else FontWeight.Normal
                        )
                    )
                }
            }
        }

        // In-widget action if current prayer is not yet marked prayed
        if (!data.isTargetCompleted) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    text = "Mark ${data.targetPrayerName} Prayed",
                    onClick = actionRunCallback<MarkPrayerPrayedActionCallback>(
                        actionParametersOf(
                            MarkPrayerPrayedActionCallback.PrayerKey to data.targetPrayerEnum.name
                        )
                    ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = WidgetTheme.pillBackgroundActive,
                        contentColor = WidgetTheme.textPrimary
                    )
                )
            }
        }
    }
}

class MarkPrayerPrayedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val prayerNameString = parameters[PrayerKey] ?: return
        try {
            val prayerName = PrayerName.valueOf(prayerNameString)
            val db = AppDatabase.getDatabase(context.applicationContext)
            val repository = AppRepository(db, context.applicationContext)
            val todayStr = repository.getTodayDateString()
            repository.setPrayerStatus(prayerName, todayStr, PrayerStatus.PRAYED)
            PrayerWidget().update(context, glanceId)
            WidgetUpdateHelper.triggerPersonalLogWidgetUpdate(context.applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("PrayerWidget", "Failed marking prayer prayed", e)
        }
    }

    companion object {
        val PrayerKey = ActionParameters.Key<String>("prayer_name")
    }
}

class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerWidget()

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == WidgetUpdateHelper.ACTION_UPDATE_PRAYER_WIDGET ||
            action == android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == android.content.Intent.ACTION_TIMEZONE_CHANGED ||
            action == android.content.Intent.ACTION_TIME_CHANGED) {
            WidgetUpdateHelper.triggerPrayerWidgetUpdate(context)
            WidgetUpdateHelper.scheduleNextMinuteTick(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateHelper.scheduleNextMinuteTick(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateHelper.cancelMinuteTick(context)
    }
}
