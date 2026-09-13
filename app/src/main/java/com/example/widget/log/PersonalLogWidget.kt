package com.example.widget.log

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import com.example.data.model.DhikrPreset
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import com.example.data.repository.AppRepository
import com.example.widget.theme.WidgetTheme
import com.example.widget.util.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * FiveLight Personal Log Widget using Jetpack Glance.
 * Displays daily worship summary (Dhikr, Salah, Quran), 7-day monochrome indicator,
 * recent entry preview, and in-widget quick actions.
 */
class PersonalLogWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 100.dp), // Small
            DpSize(220.dp, 110.dp), // Medium
            DpSize(260.dp, 220.dp)  // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val data = withContext(Dispatchers.IO) {
            loadPersonalLogData(appContext)
        }

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                val isSmall = size.width < 210.dp || size.height < 125.dp
                val isMedium = !isSmall && (size.height < 190.dp || size.width < 250.dp)

                Box(modifier = WidgetTheme.frostedGlassContainer()) {
                    when {
                        isSmall -> SmallPersonalLogLayout(data)
                        isMedium -> MediumPersonalLogLayout(data)
                        else -> LargePersonalLogLayout(data)
                    }
                }
            }
        }
    }

    private suspend fun loadPersonalLogData(context: Context): PersonalLogData {
        val db = AppDatabase.getDatabase(context)
        val repository = AppRepository(db, context)
        val todayStr = repository.getTodayDateString()

        // 1. Salah Progress
        val todayLog = db.prayerLogDao().getPrayerLogForDateDirect(todayStr)
        val fardPrayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        val completedCount = fardPrayers.count { todayLog?.isCompleted(it) == true }
        val salahDisplay = "$completedCount/5"

        // Next uncompleted prayer for quick action
        val todayPrayerTimes = repository.getTodayPrayerTimes()
        val nextUncompleted = fardPrayers.find { todayLog?.isCompleted(it) != true }
        val candidatePrayer = todayPrayerTimes.find { it.name == nextUncompleted } ?: todayPrayerTimes.firstOrNull { it.name != PrayerName.SUNRISE }

        // 2. Dhikr Progress
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis
        val dhikrList = db.dhikrHistoryDao().getAllDhikrHistoryDirect()
        val todayDhikrs = dhikrList.filter { it.timestamp >= startOfToday }
        val dhikrTotal = todayDhikrs.sumOf { it.countCompleted }

        // 3. Quran Progress
        val lastRead = repository.lastReadPosition.value
        val quranDisplay = if (lastRead != null && lastRead.surahNumber > 0) {
            "${lastRead.surahNameEnglish} : ${lastRead.verseNumber}"
        } else {
            "Continue"
        }

        // 4. 7-Day Activity Indicator (Monochrome dots)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val activityDots = StringBuilder()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateKey = sdf.format(cal.time)
            val pastLog = db.prayerLogDao().getPrayerLogForDateDirect(dateKey)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 86400000L

            val hadPrayer = fardPrayers.any { pastLog?.isCompleted(it) == true }
            val hadDhikr = dhikrList.any { it.timestamp in dayStart..dayEnd }
            val hasActivity = hadPrayer || hadDhikr

            if (activityDots.isNotEmpty()) activityDots.append(" ")
            activityDots.append(if (hasActivity) "●" else "○")
        }

        // 5. Last Entry Preview
        var recentNotePreview: String? = null
        if (todayLog != null) {
            for (p in fardPrayers.reversed()) {
                val note = todayLog.getNote(p)
                if (!note.isNullOrBlank()) {
                    recentNotePreview = "${p.displayName}: $note"
                    break
                }
            }
        }
        if (recentNotePreview == null) {
            val recentDhikr = dhikrList.firstOrNull()
            if (recentDhikr != null) {
                recentNotePreview = "${recentDhikr.dhikrName} (${recentDhikr.countCompleted})"
            }
        }

        return PersonalLogData(
            salahCount = salahDisplay,
            dhikrCount = if (dhikrTotal > 0) "$dhikrTotal" else "0",
            quranStatus = quranDisplay,
            sevenDayDots = activityDots.toString(),
            lastEntryPreview = recentNotePreview,
            nextPrayerToPray = candidatePrayer?.name?.displayName,
            nextPrayerEnum = candidatePrayer?.name
        )
    }
}

data class PersonalLogData(
    val salahCount: String,
    val dhikrCount: String,
    val quranStatus: String,
    val sevenDayDots: String,
    val lastEntryPreview: String?,
    val nextPrayerToPray: String?,
    val nextPrayerEnum: PrayerName?
)

@androidx.compose.runtime.Composable
private fun SmallPersonalLogLayout(data: PersonalLogData) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Today",
            style = WidgetTheme.titleStyle
        )
        Spacer(modifier = GlanceModifier.height(4.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Dhikr", style = WidgetTheme.captionStyle)
                Text(text = data.dhikrCount, style = WidgetTheme.bodyStyle)
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Salah", style = WidgetTheme.captionStyle)
                Text(text = data.salahCount, style = WidgetTheme.bodyStyle)
            }
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Text(text = "Quran", style = WidgetTheme.captionStyle)
            Text(text = data.quranStatus, style = WidgetTheme.bodyMutedStyle, maxLines = 1)
        }
    }
}

@androidx.compose.runtime.Composable
private fun MediumPersonalLogLayout(data: PersonalLogData) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Personal Log · Today",
                style = WidgetTheme.titleStyle,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = data.sevenDayDots,
                style = TextStyle(
                    color = WidgetTheme.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Dhikr", style = WidgetTheme.captionStyle)
                Spacer(modifier = GlanceModifier.height(1.dp))
                Text(text = data.dhikrCount, style = WidgetTheme.heroStyle)
            }

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Salah", style = WidgetTheme.captionStyle)
                Spacer(modifier = GlanceModifier.height(1.dp))
                Text(text = data.salahCount, style = WidgetTheme.heroStyle)
            }

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Quran", style = WidgetTheme.captionStyle)
                Spacer(modifier = GlanceModifier.height(1.dp))
                Text(text = data.quranStatus, style = WidgetTheme.subHeroStyle, maxLines = 1)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LargePersonalLogLayout(data: PersonalLogData) {
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
                text = "Personal Log",
                style = WidgetTheme.titleStyle,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = "Today",
                style = WidgetTheme.captionStyle
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Three Metrics
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Dhikr", style = WidgetTheme.captionStyle)
                Text(text = data.dhikrCount, style = WidgetTheme.heroStyle)
            }

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Salah", style = WidgetTheme.captionStyle)
                Text(text = data.salahCount, style = WidgetTheme.heroStyle)
            }

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Quran", style = WidgetTheme.captionStyle)
                Text(text = data.quranStatus, style = WidgetTheme.bodyStyle, maxLines = 1)
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // 7-day indicator
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity",
                style = WidgetTheme.captionStyle,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = data.sevenDayDots,
                style = TextStyle(
                    color = WidgetTheme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Recent entry preview if available
        if (!data.lastEntryPreview.isNullOrBlank()) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(10.dp)
                    .background(ImageProvider(R.drawable.widget_pill_bg))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "Last Entry", style = WidgetTheme.captionStyle)
                Text(
                    text = data.lastEntryPreview,
                    style = WidgetTheme.bodyMutedStyle,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // In-widget quick-log actions
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = "+33 Dhikr",
                onClick = actionRunCallback<QuickDhikrLogActionCallback>(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = WidgetTheme.pillBackgroundActive,
                    contentColor = WidgetTheme.textPrimary
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            if (data.nextPrayerToPray != null && data.nextPrayerEnum != null) {
                Spacer(modifier = GlanceModifier.width(8.dp))
                Button(
                    text = "✓ ${data.nextPrayerToPray}",
                    onClick = actionRunCallback<QuickPrayerLogActionCallback>(
                        actionParametersOf(
                            QuickPrayerLogActionCallback.PrayerKey to data.nextPrayerEnum.name
                        )
                    ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = WidgetTheme.pillBackgroundActive,
                        contentColor = WidgetTheme.textPrimary
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}

class QuickDhikrLogActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val repository = AppRepository(db, context.applicationContext)
            val defaultPreset = repository.allDhikrs.value.firstOrNull() ?: DhikrPreset(
                id = "subhanallah",
                nameEnglish = "SubhanAllah",
                nameArabic = "سُبْحَانَ اللَّهِ",
                translation = "Glory be to Allah",
                defaultTarget = 33
            )
            repository.recordDhikrCompletion(defaultPreset, count = 33, target = 33)
            PersonalLogWidget().update(context, glanceId)
        } catch (e: Exception) {
            android.util.Log.e("PersonalLogWidget", "Failed logging quick dhikr", e)
        }
    }
}

class QuickPrayerLogActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val prayerNameString = parameters[PrayerKey] ?: return
        try {
            val prayerName = PrayerName.valueOf(prayerNameString)
            val db = AppDatabase.getDatabase(context.applicationContext)
            val repository = AppRepository(db, context.applicationContext)
            val todayStr = repository.getTodayDateString()
            repository.setPrayerStatus(prayerName, todayStr, PrayerStatus.PRAYED)
            PersonalLogWidget().update(context, glanceId)
            // Also refresh prayer widgets
            WidgetUpdateHelper.triggerPrayerWidgetUpdate(context.applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("PersonalLogWidget", "Failed logging quick prayer", e)
        }
    }

    companion object {
        val PrayerKey = ActionParameters.Key<String>("prayer_name")
    }
}

class PersonalLogWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PersonalLogWidget()

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == android.content.Intent.ACTION_DATE_CHANGED ||
            action == android.content.Intent.ACTION_TIMEZONE_CHANGED ||
            action == android.content.Intent.ACTION_TIME_CHANGED) {
            WidgetUpdateHelper.triggerPersonalLogWidgetUpdate(context)
        }
    }
}
