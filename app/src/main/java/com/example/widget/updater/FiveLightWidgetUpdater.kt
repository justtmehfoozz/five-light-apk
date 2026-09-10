package com.example.widget.updater

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.widget.ayah.DailyAyahWidget
import com.example.widget.personal.PersonalLogWidget
import com.example.widget.prayer.PrayerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Unified, efficient updater for the FiveLight widget ecosystem.
 * Coordinates updates across PersonalLogWidget, PrayerWidget, and DailyAyahWidget
 * without background polling or battery drain.
 */
object FiveLightWidgetUpdater {

    private val updaterScope = CoroutineScope(Dispatchers.IO)

    suspend fun updateAll(context: Context) {
        val appContext = context.applicationContext
        try {
            PersonalLogWidget().updateAll(appContext)
        } catch (_: Exception) {}
        try {
            PrayerWidget().updateAll(appContext)
        } catch (_: Exception) {}
        try {
            DailyAyahWidget().updateAll(appContext)
        } catch (_: Exception) {}
    }

    suspend fun updatePrayerAndLog(context: Context) {
        val appContext = context.applicationContext
        try {
            PrayerWidget().updateAll(appContext)
        } catch (_: Exception) {}
        try {
            PersonalLogWidget().updateAll(appContext)
        } catch (_: Exception) {}
    }

    suspend fun updateDailyAyah(context: Context) {
        val appContext = context.applicationContext
        try {
            DailyAyahWidget().updateAll(appContext)
        } catch (_: Exception) {}
    }

    fun updateAllAsync(context: Context) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updateAll(appContext)
        }
    }

    fun updatePrayerAndLogAsync(context: Context) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updatePrayerAndLog(appContext)
        }
    }

    fun updateDailyAyahAsync(context: Context) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updateDailyAyah(appContext)
        }
    }
}
