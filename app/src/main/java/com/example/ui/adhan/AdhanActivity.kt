package com.example.ui.adhan

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.reminder.AdhanService
import com.example.data.reminder.PrayerReminderManager
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.util.PrayerDisplayUtils
import com.example.ui.components.AzaanOverlay
import com.example.ui.theme.FiveLightTheme

/**
 * Dedicated full-screen Activity for hosting the Adhan overlay.
 * Configured with lock-screen, screen-on, and edge-to-edge presentation flags.
 */
class AdhanActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.AdhanDebugLogger.log(
            this,
            "[ADHAN ACTIVITY CREATED] AdhanActivity onCreate executed (Intent action=${intent?.action}, prayer=${intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME)})"
        )
        enableEdgeToEdge()

        configureWindowFlags()
        handleAdhanIntent(intent)

        setContent {
            FiveLightTheme {
                val activeState by PrayerReminderManager.activeAzaanState.collectAsStateWithLifecycle()

                // Item 3: If activeAzaanState becomes null (e.g. from manual dismissal), finish activity immediately
                LaunchedEffect(activeState) {
                    if (activeState == null) {
                        finish()
                    }
                }

                AzaanOverlay(
                    activeState = activeState,
                    onDismiss = {
                        finish()
                    },
                    onSnooze = { prayer ->
                        SmartPrayerNotificationManager(this@AdhanActivity).scheduleSnooze(prayer)
                        AdhanService.stopAdhan(this@AdhanActivity, "overlay_snooze")
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.util.AdhanDebugLogger.log(this, "[ADHAN ACTIVITY RESUMED] AdhanActivity onResume executed")
        val prayerNameRaw = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
        val prayerDate = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE) ?: ""
        if (prayerNameRaw.isNotEmpty()) {
            val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
            val notifId = SmartPrayerNotificationManager.getNotificationId(prayerEnum, prayerDate)
            AdhanService.cancelAdhanAlertNotification(this, notifId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        com.example.util.AdhanDebugLogger.log(
            this,
            "[ADHAN ACTIVITY NEW INTENT] AdhanActivity onNewIntent executed (Action=${intent.action}, prayer=${intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME)})"
        )
        setIntent(intent)
        handleAdhanIntent(intent)
    }

    private fun handleAdhanIntent(intent: Intent?) {
        val prayerNameRaw = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
        val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        com.example.util.AdhanDebugLogger.log(
            this,
            "[ADHAN ACTIVITY TRIGGER OVERLAY] Calling PrayerReminderManager.triggerAzaanOverlay(${prayerEnum.name})"
        )
        PrayerReminderManager.triggerAzaanOverlay(prayerEnum)
    }

    private fun configureWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        com.example.util.AdhanDebugLogger.log(this, "[ADHAN ACTIVITY DESTROYED] AdhanActivity onDestroy executed")
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
