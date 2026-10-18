package com.example.ui.adhan

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PrayerName
import com.example.data.reminder.ActiveAzaanState
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
        enableEdgeToEdge()

        configureWindowFlags()
        handleAdhanIntent(intent)

        setContent {
            FiveLightTheme {
                val activeState by PrayerReminderManager.activeAzaanState.collectAsStateWithLifecycle()

                AzaanOverlay(
                    activeState = activeState,
                    onDismiss = {
                        PrayerReminderManager.dismissAzaanOverlay()
                        com.example.data.reminder.AzaanAudioPlayer.stopAzaan()
                        try {
                            val stopIntent = Intent(this@AdhanActivity, com.example.data.reminder.AdhanService::class.java).apply {
                                action = com.example.data.reminder.AdhanService.ACTION_STOP_ADHAN
                            }
                            startService(stopIntent)
                        } catch (_: Exception) {}
                        finish()
                    },
                    onSnooze = { prayer ->
                        SmartPrayerNotificationManager(this@AdhanActivity).scheduleSnooze(prayer)
                        PrayerReminderManager.dismissAzaanOverlay()
                        com.example.data.reminder.AzaanAudioPlayer.stopAzaan()
                        try {
                            val stopIntent = Intent(this@AdhanActivity, com.example.data.reminder.AdhanService::class.java).apply {
                                action = com.example.data.reminder.AdhanService.ACTION_STOP_ADHAN
                            }
                            startService(stopIntent)
                        } catch (_: Exception) {}
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAdhanIntent(intent)
    }

    private fun handleAdhanIntent(intent: Intent?) {
        val prayerNameRaw = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
        val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
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
