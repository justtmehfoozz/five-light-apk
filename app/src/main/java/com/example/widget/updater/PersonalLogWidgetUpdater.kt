package com.example.widget.updater

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.widget.personal.PersonalLogWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Lightweight, non-intrusive updater for FiveLight's Personal Log widget.
 * Avoids continuous background workers or battery-draining services.
 */
object PersonalLogWidgetUpdater {

    private val updaterScope = CoroutineScope(Dispatchers.IO)

    suspend fun update(context: Context) {
        FiveLightWidgetUpdater.updatePrayerAndLog(context)
    }

    fun updateAsync(context: Context) {
        FiveLightWidgetUpdater.updatePrayerAndLogAsync(context)
    }
}
