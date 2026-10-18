package com.example.data.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.model.PrayerName
import com.example.data.util.AppLocaleManager
import com.example.data.util.PrayerDisplayUtils
import com.example.ui.adhan.AdhanActivity

/**
 * Foreground service to guarantee continuous, reliable Azaan audio playback
 * across background states, lock screens, and when the app is swiped from recents.
 */
class AdhanService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ADHAN
        com.example.util.AdhanDebugLogger.log(this, "[SERVICE onStartCommand] AdhanService onStartCommand action=$action")

        when (action) {
            ACTION_STOP_ADHAN -> {
                PrayerNotificationLogger.logValidationSuccess("adhan_service", "ALL", "today", "Received STOP action in AdhanService.")
                AzaanAudioPlayer.stopAzaan()
                PrayerReminderManager.dismissAzaanOverlay()
                stopServiceSafely()
                return START_NOT_STICKY
            }

            ACTION_SNOOZE_ADHAN -> {
                val prayerNameRaw = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
                val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
                PrayerNotificationLogger.logValidationSuccess("adhan_service", prayerEnum.name, "today", "Received SNOOZE action in AdhanService.")
                AzaanAudioPlayer.stopAzaan()
                SmartPrayerNotificationManager(this).scheduleSnooze(prayerEnum)
                PrayerReminderManager.dismissAzaanOverlay()
                stopServiceSafely()
                return START_NOT_STICKY
            }

            ACTION_START_ADHAN -> {
                handleStartAdhan(intent)
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStartAdhan(intent: Intent?) {
        val prayerNameRaw = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: "FAJR"
        val prayerDate = intent?.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        val notifId = intent?.getIntExtra(
            SmartPrayerNotificationManager.EXTRA_NOTIF_ID,
            SmartPrayerNotificationManager.getNotificationId(prayerEnum, prayerDate)
        ) ?: SmartPrayerNotificationManager.getNotificationId(prayerEnum, prayerDate)

        com.example.util.AdhanDebugLogger.log(
            this,
            "[SERVICE HANDLING START] AdhanService handleStartAdhan (Prayer=${prayerEnum.name}, Date=$prayerDate, NotifId=$notifId)"
        )

        PrayerReminderManager.triggerAzaanOverlay(prayerEnum)

        acquireWakeLock()

        val notification = buildForegroundNotification(prayerEnum, prayerDate, notifId)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(notifId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(notifId, notification)
            }
            com.example.util.AdhanDebugLogger.log(
                this,
                "[FOREGROUND STARTED] AdhanService startForeground executed with type MEDIA_PLAYBACK for notifId=$notifId"
            )
        } catch (e: Exception) {
            com.example.util.AdhanDebugLogger.log(
                this,
                "[SERVICE FOREGROUND ERROR] AdhanService startForeground failed: ${e.message}"
            )
        }

        val canDraw = AdhanPermissionHelper.canDrawOverlays(this)
        val canFullScreen = AdhanPermissionHelper.canUseFullScreenIntent(this)
        com.example.util.AdhanDebugLogger.log(
            this,
            "[SERVICE LAUNCH ATTEMPT] canDrawOverlays=$canDraw, canUseFullScreenIntent=$canFullScreen"
        )

        if (canDraw) {
            try {
                val activityIntent = Intent(this, AdhanActivity::class.java).apply {
                    setAction(SmartPrayerNotificationManager.ACTION_SHOW_ADHAN_OVERLAY)
                    setData(Uri.parse("fivelight://adhan/$prayerDate/${prayerEnum.name}"))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerEnum.name)
                    putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
                    putExtra("LAUNCH_SOURCE", "DIRECT_START_ACTIVITY_SERVICE")
                }
                startActivity(activityIntent)
                com.example.util.AdhanDebugLogger.log(
                    this,
                    "[ACTIVITY LAUNCH startActivity ok] Called startActivity(AdhanActivity) directly from AdhanService."
                )
            } catch (e: Exception) {
                com.example.util.AdhanDebugLogger.log(
                    this,
                    "[ACTIVITY LAUNCH startActivity exception] Direct startActivity(AdhanActivity) failed: ${e.message}"
                )
            }
        } else {
            com.example.util.AdhanDebugLogger.log(
                this,
                "[SERVICE LAUNCH SKIPPED] canDrawOverlays is false; relying on fullScreenIntent notification banner/launch."
            )
            try {
                val fsPendingIntent = PendingIntent.getActivity(
                    this,
                    notifId * 10 + 4,
                    Intent(this, AdhanActivity::class.java).apply {
                        action = SmartPrayerNotificationManager.ACTION_SHOW_ADHAN_OVERLAY
                        data = Uri.parse("fivelight://adhan/$prayerDate/${prayerEnum.name}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerEnum.name)
                        putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
                        putExtra("LAUNCH_SOURCE", "FULL_SCREEN_PENDING_INTENT_SEND")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                fsPendingIntent.send()
                com.example.util.AdhanDebugLogger.log(
                    this,
                    "[SERVICE PENDING INTENT SEND SUCCESS] Sent fullScreenPendingIntent.send() from AdhanService."
                )
            } catch (e: Exception) {
                com.example.util.AdhanDebugLogger.log(
                    this,
                    "[SERVICE PENDING INTENT SEND ERROR] fullScreenPendingIntent.send() failed: ${e.message}"
                )
            }
        }

        // Start playback
        AzaanAudioPlayer.playAzaan(this) {
            com.example.util.AdhanDebugLogger.log(this, "[SERVICE AUDIO DONE] Adhan playback finished naturally. Stopping service.")
            stopServiceSafely()
        }
    }

    private fun buildForegroundNotification(prayerName: PrayerName, prayerDate: String, notifId: Int): Notification {
        SmartPrayerNotificationManager.createNotificationChannels(this)

        val lang = AppLocaleManager.getPersistedLanguage(this)
        val localizedContext = AppLocaleManager.createLocalizedContext(this, lang)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getLocalizedPrayerName(this, prayerName, isFriday)
        val title = localizedContext.getString(R.string.notif_prayer_title, displayName)
        val bodyText = localizedContext.getString(R.string.notif_prayer_time_entered, displayName)

        // Target AdhanActivity for full-screen and content intents
        val adhanActivityIntent = Intent(this, AdhanActivity::class.java).apply {
            action = SmartPrayerNotificationManager.ACTION_SHOW_ADHAN_OVERLAY
            data = Uri.parse("fivelight://adhan/$prayerDate/${prayerName.name}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            notifId * 10 + 3,
            adhanActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            notifId * 10 + 4,
            adhanActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action
        val stopIntent = Intent(this, AdhanService::class.java).apply {
            action = ACTION_STOP_ADHAN
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            notifId * 10 + 5,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeIntent = Intent(this, AdhanService::class.java).apply {
            action = ACTION_SNOOZE_ADHAN
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            notifId * 10 + 6,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        com.example.util.AdhanDebugLogger.log(
            this,
            "[FULLSCREEN INTENT POSTED] Built foreground notification with setFullScreenIntent for AdhanActivity (notifId=$notifId)"
        )

        return NotificationCompat.Builder(this, SmartPrayerNotificationManager.CHANNEL_ADHAN)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setSubText("FiveLight")
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(
                R.drawable.ic_notification_small,
                "Stop Adhan",
                stopPendingIntent
            )
            .addAction(
                R.drawable.ic_notification_small,
                "Snooze 5 min",
                snoozePendingIntent
            )
            .build()
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "FiveLight:AdhanServiceWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                    acquire(3 * 60 * 1000L) // 3 minutes timeout
                }
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (_: Exception) {}
    }

    private fun stopServiceSafely() {
        com.example.util.AdhanDebugLogger.log(this, "[SERVICE STOPPED] AdhanService stopped safely.")
        releaseWakeLock()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(false)
            }
        } catch (_: Exception) {}
        stopSelf()
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_ADHAN = "com.example.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
        const val ACTION_SNOOZE_ADHAN = "com.example.ACTION_SNOOZE_ADHAN"
    }
}
