package com.example.widget.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.example.widget.ayah.DailyAyahWidget
import com.example.widget.log.PersonalLogWidget
import com.example.widget.prayer.PrayerWidget
import com.example.widget.prayer.PrayerWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateHelper {

    private const val TAG = "WidgetUpdateHelper"
    const val ACTION_UPDATE_PRAYER_WIDGET = "com.example.widget.ACTION_UPDATE_PRAYER"
    private const val REQUEST_CODE_PRAYER_TICK = 8101

    fun isSystemDarkMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Schedules the next minute tick for the prayer countdown.
     * Only schedules if there is at least one active prayer widget on the home screen.
     */
    fun scheduleNextMinuteTick(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PrayerWidgetReceiver::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (widgetIds == null || widgetIds.isEmpty()) {
            // No prayer widgets placed; do not schedule background alarms
            cancelMinuteTick(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerWidgetReceiver::class.java).apply {
            action = ACTION_UPDATE_PRAYER_WIDGET
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PRAYER_TICK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val nextMinute = (now / 60000L + 1L) * 60000L + 500L // start of next minute + 500ms

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC, nextMinute, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC, nextMinute, pendingIntent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not set exact minute alarm, falling back to inexact", e)
            alarmManager.set(AlarmManager.RTC, nextMinute, pendingIntent)
        }
    }

    fun cancelMinuteTick(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerWidgetReceiver::class.java).apply {
            action = ACTION_UPDATE_PRAYER_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PRAYER_TICK,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun triggerPrayerWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PrayerWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed updating PrayerWidget", e)
            }
        }
    }

    fun triggerDailyAyahWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DailyAyahWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed updating DailyAyahWidget", e)
            }
        }
    }

    fun triggerPersonalLogWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PersonalLogWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed updating PersonalLogWidget", e)
            }
        }
    }
}
