package com.example.data.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerName
import com.example.data.repository.AppRepository
import com.example.data.util.PrayerDisplayUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartPrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == SmartPrayerNotificationManager.ACTION_MARK_PRAYED) {
            handleMarkPrayed(context, intent)
            return
        }

        if (action == SmartPrayerNotificationManager.ACTION_UNDO_PRAYED) {
            handleUndoPrayed(context, intent)
            return
        }

        val eventType = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE) ?: return
        val prayerNameRaw = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
        val prayerTime = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_TIME) ?: ""
        val prayerDate = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val offsetMins = intent.getIntExtra(SmartPrayerNotificationManager.EXTRA_OFFSET_MINS, 0)
        val windowText = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_TEXT) ?: ""

        val nowMillis = System.currentTimeMillis()
        val triggerMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, 0L)
        val windowEndMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_END_MILLIS, 0L)

        // CRITICAL GUARD: Never deliver expired or stale notifications
        if (windowEndMillis > 0L && nowMillis > windowEndMillis) {
            return
        }
        if (triggerMillis > 0L && (nowMillis - triggerMillis > 15 * 60 * 1000L) && windowEndMillis == 0L) {
            return
        }

        val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getPrayerDisplayName(prayerEnum, isFriday)

        if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_FARD) {
            val title = "$displayName Prayer"
            val bodyText = "$displayName prayer time has entered."
            val notifId = 1001 + prayerEnum.hashCode()
            showFardPrayerNotification(
                context = context,
                title = title,
                message = bodyText,
                notificationId = notifId,
                prayerName = prayerEnum.name,
                displayName = displayName,
                prayerDate = prayerDate,
                channelId = SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME
            )
            return
        }

        if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP) {
            handleFardFollowUp(context, intent)
            return
        }

        val (title, message, notifId, channelId) = when (eventType) {
            SmartPrayerNotificationManager.EVENT_TYPE_TEST -> {
                val testIsFriday = PrayerDisplayUtils.isFriday()
                val testDhuhrName = PrayerDisplayUtils.getPrayerDisplayName(PrayerName.DHUHR, testIsFriday)
                Quadruple(
                    "$testDhuhrName Prayer",
                    "$testDhuhrName prayer time has entered.",
                    9999,
                    SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_PRE_PRAYER -> {
                val t = "$displayName is approaching"
                val bodyText = if (prayerEnum == PrayerName.FAJR) {
                    "$prayerTime · $offsetMins min remaining\nPrepare for the first prayer of the day."
                } else if (prayerEnum == PrayerName.DHUHR && isFriday) {
                    "$displayName · $prayerTime\nPrepare for the Friday congregational prayer."
                } else {
                    "$displayName · $prayerTime\nA few moments remain before the prayer time begins."
                }
                Quadruple(t, bodyText, 2001 + prayerEnum.hashCode(), SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME)
            }

            SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_MORNING -> {
                Quadruple(
                    "A New Morning Begins",
                    "Sunrise · $prayerTime\nBegin the morning with remembrance and gratitude.",
                    3001,
                    SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_EVENING -> {
                Quadruple(
                    "Evening Has Begun",
                    "Maghrib · $prayerTime\nA quiet moment for prayer and remembrance.",
                    3002,
                    SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_NIGHT -> {
                Quadruple(
                    "The Last Third of the Night Has Begun",
                    "Tahajjud · $windowText\nA voluntary opportunity is open.",
                    3003,
                    SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_NAFL_DUHA -> {
                Quadruple(
                    "Duha Opportunity",
                    "$windowText\nA voluntary prayer opportunity is available.",
                    4001,
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_NAFL_TAHAJJUD -> {
                Quadruple(
                    "The Last Third of the Night Has Begun",
                    "$windowText\nA voluntary opportunity is open.",
                    4002,
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            else -> return
        }

        showNotification(context, title, message, notifId, channelId)
    }

    private fun handleFardFollowUp(context: Context, intent: Intent) {
        val prayerNameRaw = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
        val prayerDate = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val prayerNameEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getPrayerDisplayName(prayerNameEnum, isFriday)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context.applicationContext)
                val log = db.prayerLogDao().getPrayerLogForDateDirect(prayerDate)
                val isRecorded = log != null && (log.isCompleted(prayerNameEnum) || log.isMissed(prayerNameEnum))
                // Only send follow-up if still unrecorded (NEEDS_INPUT)
                if (!isRecorded) {
                    val notifId = 1501 + prayerNameEnum.hashCode()
                    showFardPrayerNotification(
                        context = context,
                        title = "$displayName Prayer",
                        message = "Did you get a chance to pray?",
                        notificationId = notifId,
                        prayerName = prayerNameEnum.name,
                        displayName = displayName,
                        prayerDate = prayerDate,
                        channelId = SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME
                    )
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMarkPrayed(context: Context, intent: Intent) {
        val prayerNameStr = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: return
        val prayerDateStr = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val notifId = intent.getIntExtra(SmartPrayerNotificationManager.EXTRA_NOTIF_ID, 1001 + prayerNameStr.hashCode())

        val prayerNameEnum = PrayerDisplayUtils.parsePrayerName(prayerNameStr)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context.applicationContext)
                val repository = AppRepository(db, context.applicationContext)
                val success = repository.setPrayerStatus(prayerNameEnum, prayerDateStr, com.example.data.model.PrayerStatus.PRAYED)
                if (success) {
                    // Suppress / cancel follow-up reminder
                    SmartPrayerNotificationManager(context.applicationContext).cancelFollowUp(prayerNameEnum)
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(notifId)
                    nm.cancel(1001 + prayerNameEnum.hashCode())
                    nm.cancel(1501 + prayerNameEnum.hashCode())
                    val isFriday = PrayerDisplayUtils.isFriday(prayerDateStr)
                    val displayName = PrayerDisplayUtils.getPrayerDisplayName(prayerNameEnum, isFriday)
                    showMarkedAsPrayedWithUndo(context, prayerNameEnum.name, displayName, prayerDateStr, notifId)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleUndoPrayed(context: Context, intent: Intent) {
        val prayerNameStr = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: return
        val prayerDateStr = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val notifId = intent.getIntExtra(SmartPrayerNotificationManager.EXTRA_NOTIF_ID, 1001 + prayerNameStr.hashCode())

        val prayerNameEnum = PrayerDisplayUtils.parsePrayerName(prayerNameStr)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context.applicationContext)
                val repository = AppRepository(db, context.applicationContext)
                repository.setPrayerStatus(prayerNameEnum, prayerDateStr, com.example.data.model.PrayerStatus.NEEDS_INPUT)
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(notifId)
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showFardPrayerNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        prayerName: String,
        displayName: String,
        prayerDate: String,
        channelId: String
    ) {
        SmartPrayerNotificationManager.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markPrayedIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
            action = SmartPrayerNotificationManager.ACTION_MARK_PRAYED
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
            putExtra(SmartPrayerNotificationManager.EXTRA_NOTIF_ID, notificationId)
        }
        val markPrayedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            markPrayedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Mark as prayed",
                markPrayedPendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Open FiveLight",
                openAppPendingIntent
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun showMarkedAsPrayedWithUndo(
        context: Context,
        prayerName: String,
        displayName: String,
        prayerDate: String,
        notificationId: Int
    ) {
        SmartPrayerNotificationManager.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val undoIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
            action = SmartPrayerNotificationManager.ACTION_UNDO_PRAYED
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
            putExtra(SmartPrayerNotificationManager.EXTRA_NOTIF_ID, notificationId)
        }
        val undoPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            undoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "✓ $displayName marked as prayed"
        val message = "Recorded in Personal Log."

        val builder = NotificationCompat.Builder(context, SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setTimeoutAfter(30000)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Undo",
                undoPendingIntent
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun showNotification(context: Context, title: String, message: String, notificationId: Int, channelId: String = SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER) {
        SmartPrayerNotificationManager.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
