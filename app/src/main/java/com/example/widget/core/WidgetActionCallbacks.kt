package com.example.widget.core

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.widget.ayah.DailyAyahWidget
import com.example.widget.personal.PersonalLogWidget
import com.example.widget.prayer.PrayerWidget

/**
 * Interactive Glance Action Callbacks that keep widgets functional
 * without automatically launching the host application.
 */
class QuickDhikrIncrementCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            val prefs = context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)
            val current = prefs.getInt("dhikr_count_subhanallah", 0)
            prefs.edit()
                .putInt("dhikr_count_subhanallah", current + 1)
                .putLong("tasbeeh_updated_at", System.currentTimeMillis())
                .apply()

            PersonalLogWidget().update(context, glanceId)
        } catch (_: Exception) {}
    }
}

class RefreshPrayerActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            PrayerWidget().update(context, glanceId)
        } catch (_: Exception) {}
    }
}

class RefreshAyahActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            DailyAyahWidget().update(context, glanceId)
        } catch (_: Exception) {}
    }
}
