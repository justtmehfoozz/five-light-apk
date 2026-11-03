package com.example.data.reminder

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private var currentNotifId: Int = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            com.example.util.AdhanDebugLogger.log(this, "[SERVICE onStartCommand] intent == null, calling startForeground first and stopping service cleanly")
            val fallbackNotif = buildMinimalForegroundNotification()
            promoteToForeground(99991, fallbackNotif)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent.action ?: ACTION_START_ADHAN
        com.example.util.AdhanDebugLogger.log(this, "[SERVICE onStartCommand] AdhanService onStartCommand action=$action")

        when (action) {
            ACTION_STOP_ADHAN -> {
                PrayerNotificationLogger.logValidationSuccess("adhan_service", "ALL", "today", "Received STOP action in AdhanService.")
                stopPlaybackAndService("notification_stop_action")
                return START_NOT_STICKY
            }

            ACTION_SNOOZE_ADHAN -> {
                val prayerNameRaw = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
                val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
                PrayerNotificationLogger.logValidationSuccess("adhan_service", prayerEnum.name, "today", "Received SNOOZE action in AdhanService.")
                SmartPrayerNotificationManager(this).scheduleSnooze(prayerEnum)
                stopPlaybackAndService("notification_snooze_action")
                return START_NOT_STICKY
            }

            ACTION_START_ADHAN -> {
                handleStartAdhan(intent)
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStartAdhan(intent: Intent) {
        val prayerNameRaw = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: "FAJR"
        val prayerDate = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val eventType = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE) ?: "ENTRY"
        val triggerMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, 0L)
        val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        val notifId = intent.getIntExtra(
            SmartPrayerNotificationManager.EXTRA_NOTIF_ID,
            SmartPrayerNotificationManager.getNotificationId(prayerEnum, prayerDate)
        )
        currentNotifId = notifId

        // 1. Promote to Foreground IMMEDIATELY on EVERY start request
        val foregroundNotif = buildForegroundNotification(prayerEnum, prayerDate, notifId)
        promoteToForeground(notifId, foregroundNotif)

        // 2. Persisted Play-once guard check
        val prefs = getSharedPreferences("adhan_last_played_prefs", Context.MODE_PRIVATE)
        val key = if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_SNOOZE_ADHAN) {
            "SNOOZE|${prayerEnum.name}|$prayerDate|$triggerMillis"
        } else {
            "ENTRY|${prayerEnum.name}|$prayerDate"
        }

        val now = System.currentTimeMillis()
        val lastTime = prefs.getLong(key, 0L)
        if (lastTime > 0 && (now - lastTime < 10 * 60 * 1000L)) {
            com.example.util.AdhanDebugLogger.log(
                this,
                "[DUPLICATE START IGNORED] Key=$key already played ${now - lastTime}ms ago. Preserving active audio/overlay."
            )
            if (AzaanAudioPlayer.isPlaying()) {
                return
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return
            }
        }
        prefs.edit().putLong(key, now).apply()

        com.example.util.AdhanDebugLogger.log(
            this,
            "[SERVICE HANDLING START] AdhanService handleStartAdhan (Prayer=${prayerEnum.name}, Date=$prayerDate, Event=$eventType, NotifId=$notifId)"
        )

        // Reset stale playback state to IDLE before triggering new overlay
        AzaanAudioPlayer.resetPlaybackState()
        PrayerReminderManager.triggerAzaanOverlay(prayerEnum)

        // 3. Determine Launch Logic based on Screen / Keyguard state & Overlay permission
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isInteractive = powerManager?.isInteractive == true
        val isKeyguardLocked = keyguardManager?.isKeyguardLocked == true
        val isScreenOnAndUnlocked = isInteractive && !isKeyguardLocked
        val canDrawOverlays = AdhanPermissionHelper.canDrawOverlays(this)

        com.example.util.AdhanDebugLogger.log(
            this,
            "[SERVICE LAUNCH CHECK] isInteractive=$isInteractive, isKeyguardLocked=$isKeyguardLocked, canDrawOverlays=$canDrawOverlays"
        )

        var activityLaunchedDirectly = false

        val activityIntent = Intent(this, AdhanActivity::class.java).apply {
            setAction(SmartPrayerNotificationManager.ACTION_SHOW_ADHAN_OVERLAY)
            setData(Uri.parse("fivelight://adhan/$prayerDate/${prayerEnum.name}"))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerEnum.name)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
            putExtra("LAUNCH_SOURCE", "DIRECT_START_ACTIVITY_SERVICE")
        }

        if (isScreenOnAndUnlocked && canDrawOverlays) {
            try {
                startActivity(activityIntent)
                activityLaunchedDirectly = true
                com.example.util.AdhanDebugLogger.log(
                    this,
                    "[ACTIVITY LAUNCH startActivity ok] Screen on & unlocked; started AdhanActivity directly without fullScreenIntent notification."
                )
            } catch (e: Exception) {
                com.example.util.AdhanDebugLogger.log(
                    this,
                    "[ACTIVITY LAUNCH startActivity exception] Direct startActivity failed: ${e.message}"
                )
            }
        }

        if (!activityLaunchedDirectly) {
            com.example.util.AdhanDebugLogger.log(
                this,
                "[SERVICE POSTING ALERT] Screen off/locked or no draw overlay; posting high-importance fullScreenIntent alert notification."
            )
            postAdhanAlertNotification(prayerEnum, prayerDate, notifId)
        }

        // 4. Start Audio Playback (ONLY AdhanService starts audio)
        AzaanAudioPlayer.playAzaan(this, prayerEnum.name, prayerDate, "service_start")

        // 5. Schedule timeout matching playback state:
        // R.raw.azaan is 204 seconds. Audio length + 60s = 264 seconds (4m 24s).
        // For SKIPPED (silent/vibrate), timeout is 180 seconds (3m).
        val state = AzaanAudioPlayer.playbackState.value
        val timeoutMs = if (state == AdhanPlaybackState.SKIPPED) 180_000L else 264_000L
        acquireWakeLock(timeoutMs)

        cancelTimeout()
        timeoutRunnable = Runnable {
            com.example.util.AdhanDebugLogger.log(this, "[SERVICE TIMEOUT] Timeout reached (${timeoutMs}ms). Stopping service and releasing wakelock.")
            stopPlaybackAndService("service_timeout")
        }
        mainHandler.postDelayed(timeoutRunnable!!, timeoutMs)
    }

    private fun promoteToForeground(notifId: Int, notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(notifId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(notifId, notification)
            }
            com.example.util.AdhanDebugLogger.log(
                this,
                "[FOREGROUND STARTED] AdhanService startForeground on CHANNEL_ADHAN_SERVICE_V2 for notifId=$notifId"
            )
        } catch (e: Exception) {
            com.example.util.AdhanDebugLogger.log(
                this,
                "[SERVICE FOREGROUND ERROR] AdhanService startForeground failed: ${e.message}"
            )
        }
    }

    private fun buildMinimalForegroundNotification(): Notification {
        SmartPrayerNotificationManager.createNotificationChannels(this)
        return NotificationCompat.Builder(this, SmartPrayerNotificationManager.CHANNEL_ADHAN_SERVICE_V2)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle("FiveLight")
            .setContentText("Prayer notification service")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun postAdhanAlertNotification(prayerName: PrayerName, prayerDate: String, notifId: Int) {
        val lang = AppLocaleManager.getPersistedLanguage(this)
        val localizedContext = AppLocaleManager.createLocalizedContext(this, lang)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getLocalizedPrayerName(this, prayerName, isFriday)
        val title = localizedContext.getString(R.string.notif_prayer_title, displayName)
        val bodyText = localizedContext.getString(R.string.notif_prayer_time_entered, displayName)

        val fullScreenIntent = Intent(this, AdhanActivity::class.java).apply {
            action = SmartPrayerNotificationManager.ACTION_SHOW_ADHAN_OVERLAY
            data = Uri.parse("fivelight://adhan/$prayerDate/${prayerName.name}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
            putExtra("LAUNCH_SOURCE", "FULL_SCREEN_ALERT_NOTIFICATION")
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            notifId * 10 + 4,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotif = NotificationCompat.Builder(this, SmartPrayerNotificationManager.CHANNEL_ADHAN_ALERT)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setSubText("FiveLight")
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(getAlertNotifId(notifId), alertNotif)
        com.example.util.AdhanDebugLogger.log(
            this,
            "[FULLSCREEN INTENT POSTED] High importance alert notification posted (notifId=${getAlertNotifId(notifId)})"
        )
    }

    private fun buildForegroundNotification(prayerName: PrayerName, prayerDate: String, notifId: Int): Notification {
        SmartPrayerNotificationManager.createNotificationChannels(this)

        val lang = AppLocaleManager.getPersistedLanguage(this)
        val localizedContext = AppLocaleManager.createLocalizedContext(this, lang)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getLocalizedPrayerName(this, prayerName, isFriday)
        val title = localizedContext.getString(R.string.notif_prayer_title, displayName)
        val bodyText = localizedContext.getString(R.string.notif_prayer_time_entered, displayName)

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

        return NotificationCompat.Builder(this, SmartPrayerNotificationManager.CHANNEL_ADHAN_SERVICE_V2)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setSubText("FiveLight")
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
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

    /**
     * Stops audio playback, cancels notifications, releases wake lock, and stops foreground service.
     * CRITICAL: Does NOT touch PrayerReminderManager.activeAzaanState so the overlay stays visible for Dua card.
     */
    fun stopPlaybackAndService(callerReason: String) {
        cancelTimeout()
        com.example.util.AdhanDebugLogger.log(this, "[STOP PLAYBACK & SERVICE] caller=$callerReason")
        AzaanAudioPlayer.stopAzaan()
        if (currentNotifId > 0) {
            cancelAdhanAlertNotification(this, currentNotifId)
        }
        releaseWakeLock()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}
        stopSelf()
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    private fun acquireWakeLock(timeoutMs: Long = 264_000L) {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "FiveLight:AdhanServiceWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                    acquire(timeoutMs)
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

    override fun onDestroy() {
        cancelTimeout()
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_ADHAN = "com.example.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
        const val ACTION_SNOOZE_ADHAN = "com.example.ACTION_SNOOZE_ADHAN"

        fun getAlertNotifId(baseNotifId: Int): Int = baseNotifId + 5000

        fun cancelAdhanAlertNotification(context: Context, baseNotifId: Int) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(getAlertNotifId(baseNotifId))
            } catch (_: Exception) {}
        }

        fun stopAdhan(context: Context, callerReason: String) {
            com.example.util.AdhanDebugLogger.log(context, "[STOP ADHAN SHARED CALL] caller=$callerReason")
            val intent = Intent(context, AdhanService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
