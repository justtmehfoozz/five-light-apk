package com.example.data.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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

        val lang = com.example.data.util.AppLocaleManager.getPersistedLanguage(context)
        val localizedContext = com.example.data.util.AppLocaleManager.createLocalizedContext(context, lang)
        val prayerEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getLocalizedPrayerName(context, prayerEnum, isFriday)

        if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_FARD) {
            val notifId = SmartPrayerNotificationManager.getNotificationId(prayerEnum, prayerDate)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    PrayerNotificationLogger.logDelivery(
                        eventType = eventType,
                        prayerNameStr = prayerEnum.name,
                        dateStr = prayerDate,
                        scheduledMillis = triggerMillis,
                        deliveryMillis = nowMillis
                    )

                    val valResult = PrayerNotificationValidator.validateFardEntry(
                        context = context,
                        prayerName = prayerEnum,
                        prayerDateStr = prayerDate,
                        triggerMillis = triggerMillis,
                        windowEndMillis = windowEndMillis
                    )

                    when (valResult) {
                        is PrayerNotificationValidator.ValidationResult.Invalid -> {
                            PrayerNotificationLogger.logValidationFailure(
                                eventType = eventType,
                                prayerNameStr = prayerEnum.name,
                                dateStr = prayerDate,
                                reason = valResult.reason
                            )
                            return@launch
                        }
                        is PrayerNotificationValidator.ValidationResult.Valid -> {
                            PrayerNotificationLogger.logValidationSuccess(
                                eventType = eventType,
                                prayerNameStr = prayerEnum.name,
                                dateStr = prayerDate,
                                details = "Entry notification validated successfully."
                            )
                        }
                    }

                    PrayerNotificationValidator.markFardEntryTriggered(context, prayerEnum, prayerDate)

                    val manager = SmartPrayerNotificationManager(context.applicationContext)
                    val title = localizedContext.getString(R.string.notif_prayer_title, displayName)
                    val bodyText = localizedContext.getString(R.string.notif_prayer_time_entered, displayName)

                    val isAzaanMode = manager.prayerAlertMode == PrayerAlertMode.AZAAN_AND_NOTIFICATION
                    val targetChannel = if (isAzaanMode) {
                        SmartPrayerNotificationManager.CHANNEL_ADHAN
                    } else {
                        SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME
                    }

                    showFardPrayerNotification(
                        context = context,
                        title = title,
                        message = bodyText,
                        notificationId = notifId,
                        prayerName = prayerEnum.name,
                        displayName = displayName,
                        prayerDate = prayerDate,
                        channelId = targetChannel,
                        includeMarkAsPrayed = true,
                        triggerMillis = triggerMillis,
                        windowEndMillis = windowEndMillis,
                        stage = SmartPrayerNotificationManager.EVENT_TYPE_FARD,
                        offsetMins = offsetMins
                    )

                    if (isAzaanMode) {
                        PrayerReminderManager.triggerAzaanOverlay(prayerEnum)
                        AzaanAudioPlayer.playAzaan(context)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SmartPrayerNotif", "Error handling Fard prayer event: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_SNOOZE_ADHAN) {
            val notifId = SmartPrayerNotificationManager.getNotificationId(prayerEnum, prayerDate)
            val manager = SmartPrayerNotificationManager(context.applicationContext)
            val isAzaanMode = manager.prayerAlertMode == PrayerAlertMode.AZAAN_AND_NOTIFICATION
            val title = localizedContext.getString(R.string.notif_prayer_title, displayName)
            val bodyText = localizedContext.getString(R.string.notif_prayer_time_entered, displayName)

            showFardPrayerNotification(
                context = context,
                title = title,
                message = bodyText,
                notificationId = notifId,
                prayerName = prayerEnum.name,
                displayName = displayName,
                prayerDate = prayerDate,
                channelId = if (isAzaanMode) SmartPrayerNotificationManager.CHANNEL_ADHAN else SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME,
                includeMarkAsPrayed = true,
                triggerMillis = triggerMillis,
                windowEndMillis = windowEndMillis,
                stage = SmartPrayerNotificationManager.EVENT_TYPE_SNOOZE_ADHAN,
                offsetMins = offsetMins
            )

            if (isAzaanMode) {
                PrayerReminderManager.triggerAzaanOverlay(prayerEnum)
                AzaanAudioPlayer.playAzaan(context)
            }
            return
        }

        if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP) {
            handleFardFollowUp(context, intent)
            return
        }

        if (eventType == SmartPrayerNotificationManager.EVENT_TYPE_PRE_PRAYER) {
            // Approaching notifications have been completely removed.
            return
        }

        val (title, message, notifId, channelId) = when (eventType) {
            SmartPrayerNotificationManager.EVENT_TYPE_TEST -> {
                val testIsFriday = PrayerDisplayUtils.isFriday()
                val testDhuhrName = PrayerDisplayUtils.getPrayerDisplayName(PrayerName.DHUHR, testIsFriday)
                val testNotifId = SmartPrayerNotificationManager.getNotificationId(PrayerName.DHUHR, java.time.LocalDate.now())
                showFardPrayerNotification(
                    context = context,
                    title = "$testDhuhrName Prayer",
                    message = "$testDhuhrName prayer time has entered.",
                    notificationId = testNotifId,
                    prayerName = PrayerName.DHUHR.name,
                    displayName = testDhuhrName,
                    prayerDate = java.time.LocalDate.now().toString(),
                    channelId = SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME,
                    includeMarkAsPrayed = true,
                    stage = SmartPrayerNotificationManager.EVENT_TYPE_FARD
                )
                return
            }

            SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_MORNING -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isContextualRemindersEnabled) return
                Quadruple(
                    localizedContext.getString(R.string.adhkar_morning),
                    localizedContext.getString(R.string.prelude_remembrance_subtitle),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_CONTEXTUAL_MORNING),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_EVENING -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isContextualRemindersEnabled) return
                // Guard: Avoid sending Evening Has Begun at the exact same moment if Maghrib Fard notification is active or enabled
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val maghribNotifId = SmartPrayerNotificationManager.getNotificationId(PrayerName.MAGHRIB, prayerDate)
                val isMaghribActive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    nm.activeNotifications.any { it.id == maghribNotifId }
                } else false

                if ((manager.isPrayerTimeNotificationsEnabled && manager.isPrayerEnabled(PrayerName.MAGHRIB.id)) || isMaghribActive) {
                    return
                }
                Quadruple(
                    localizedContext.getString(R.string.adhkar_evening),
                    localizedContext.getString(R.string.prelude_remembrance_subtitle),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_CONTEXTUAL_EVENING),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_NIGHT -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isContextualRemindersEnabled) return
                // Guard: Avoid colliding with Tahajjud Nafl opportunity notification if Nafl is enabled
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val tahajjudNotifId = SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_TAHAJJUD)
                val isTahajjudActive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    nm.activeNotifications.any { it.id == tahajjudNotifId }
                } else false

                if (manager.isNaflOpportunitiesEnabled || isTahajjudActive) {
                    return
                }
                Quadruple(
                    localizedContext.getString(R.string.home_context_night),
                    localizedContext.getString(R.string.adhkar_before_sleep),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_CONTEXTUAL_NIGHT),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_NAFL_ISHRAQ -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isNaflOpportunitiesEnabled) return
                val ishraqName = localizedContext.getString(R.string.prayer_ishraq)
                Quadruple(
                    ishraqName,
                    localizedContext.getString(R.string.home_feature_nafl_prayers),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_ISHRAQ),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_NAFL_DUHA -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isNaflOpportunitiesEnabled) return
                val duhaName = localizedContext.getString(R.string.prayer_duha)
                Quadruple(
                    duhaName,
                    localizedContext.getString(R.string.home_feature_nafl_prayers),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_DUHA),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_NAFL_TAHAJJUD -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isNaflOpportunitiesEnabled) return
                val tahajjudName = localizedContext.getString(R.string.prayer_tahajjud)
                Quadruple(
                    tahajjudName,
                    localizedContext.getString(R.string.home_feature_nafl_prayers),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_TAHAJJUD),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            SmartPrayerNotificationManager.EVENT_TYPE_NAFL_AWWABIN -> {
                val manager = SmartPrayerNotificationManager(context.applicationContext)
                if (!manager.isSmartNotificationsEnabled || !manager.isNaflOpportunitiesEnabled) return
                val awwabinName = localizedContext.getString(R.string.prayer_awwabin)
                Quadruple(
                    awwabinName,
                    localizedContext.getString(R.string.home_feature_nafl_prayers),
                    SmartPrayerNotificationManager.getSpecialNotificationId(prayerDate, SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_AWWABIN),
                    SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER
                )
            }

            else -> return
        }

        // Idempotency: Avoid delivering the same contextual or nafl notification repeatedly for the same date
        val prefs = context.getSharedPreferences("fivelight_special_delivery", Context.MODE_PRIVATE)
        val deliveryKey = "${prayerDate}_${eventType}"
        if (prefs.getBoolean(deliveryKey, false)) {
            return
        }
        prefs.edit().putBoolean(deliveryKey, true).apply()

        showNotification(context, title, message, notifId, channelId)
    }

    private fun handleFardFollowUp(context: Context, intent: Intent) {
        val prayerNameRaw = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: ""
        val prayerDate = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE)
            ?: java.time.LocalDate.now().toString()
        val lang = com.example.data.util.AppLocaleManager.getPersistedLanguage(context)
        val localizedContext = com.example.data.util.AppLocaleManager.createLocalizedContext(context, lang)
        val prayerNameEnum = PrayerDisplayUtils.parsePrayerName(prayerNameRaw)
        val isFriday = PrayerDisplayUtils.isFriday(prayerDate)
        val displayName = PrayerDisplayUtils.getLocalizedPrayerName(context, prayerNameEnum, isFriday)
        val notifId = SmartPrayerNotificationManager.getNotificationId(prayerNameEnum, prayerDate)
        val triggerMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, 0L)
        val windowEndMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_END_MILLIS, 0L)
        val offsetMins = intent.getIntExtra(SmartPrayerNotificationManager.EXTRA_OFFSET_MINS, 15)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val valResult = PrayerNotificationValidator.validateFardFollowUp(
                    context = context,
                    prayerName = prayerNameEnum,
                    prayerDateStr = prayerDate,
                    triggerMillis = triggerMillis,
                    windowEndMillis = windowEndMillis
                )

                when (valResult) {
                    is PrayerNotificationValidator.ValidationResult.Invalid -> {
                        PrayerNotificationLogger.logValidationFailure(
                            eventType = SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP,
                            prayerNameStr = prayerNameEnum.name,
                            dateStr = prayerDate,
                            reason = valResult.reason
                        )
                        return@launch
                    }
                    is PrayerNotificationValidator.ValidationResult.Valid -> {
                        PrayerNotificationLogger.logValidationSuccess(
                            eventType = SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP,
                            prayerNameStr = prayerNameEnum.name,
                            dateStr = prayerDate,
                            details = "Follow-up notification validated successfully."
                        )
                    }
                }

                PrayerNotificationValidator.markFardFollowUpTriggered(context, prayerNameEnum, prayerDate)

                showFardPrayerNotification(
                    context = context,
                    title = localizedContext.getString(R.string.notif_prayer_reminder_title, displayName),
                    message = localizedContext.getString(R.string.notif_followup_question, displayName),
                    notificationId = notifId,
                    prayerName = prayerNameEnum.name,
                    displayName = displayName,
                    prayerDate = prayerDate,
                    channelId = SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME,
                    includeMarkAsPrayed = true,
                    triggerMillis = triggerMillis,
                    windowEndMillis = windowEndMillis,
                    stage = SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP,
                    offsetMins = offsetMins
                )
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
        val prayerNameEnum = PrayerDisplayUtils.parsePrayerName(prayerNameStr)
        val notifId = intent.getIntExtra(
            SmartPrayerNotificationManager.EXTRA_NOTIF_ID,
            SmartPrayerNotificationManager.getNotificationId(prayerNameEnum, prayerDateStr)
        )
        val stage = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE)
            ?: SmartPrayerNotificationManager.EVENT_TYPE_FARD
        val triggerMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, 0L)
        val windowEndMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_END_MILLIS, 0L)
        val offsetMins = intent.getIntExtra(SmartPrayerNotificationManager.EXTRA_OFFSET_MINS, 15)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context.applicationContext)
                val repository = AppRepository(db, context.applicationContext)
                val success = repository.setPrayerStatus(prayerNameEnum, prayerDateStr, com.example.data.model.PrayerStatus.PRAYED)
                if (success) {
                    // Suppress / cancel follow-up reminder for this exact prayer & date
                    SmartPrayerNotificationManager(context.applicationContext).cancelFollowUpAlarm(prayerNameEnum, prayerDateStr)
                    val isFriday = PrayerDisplayUtils.isFriday(prayerDateStr)
                    val displayName = PrayerDisplayUtils.getPrayerDisplayName(prayerNameEnum, isFriday)
                    showMarkedAsPrayedWithUndo(
                        context = context,
                        prayerName = prayerNameEnum.name,
                        displayName = displayName,
                        prayerDate = prayerDateStr,
                        notificationId = notifId,
                        stage = stage,
                        triggerMillis = triggerMillis,
                        windowEndMillis = windowEndMillis,
                        offsetMins = offsetMins
                    )
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
        val prayerNameEnum = PrayerDisplayUtils.parsePrayerName(prayerNameStr)
        val notifId = intent.getIntExtra(
            SmartPrayerNotificationManager.EXTRA_NOTIF_ID,
            SmartPrayerNotificationManager.getNotificationId(prayerNameEnum, prayerDateStr)
        )
        val stage = intent.getStringExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE)
        val triggerMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, 0L)
        val windowEndMillis = intent.getLongExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_END_MILLIS, 0L)
        val offsetMins = intent.getIntExtra(SmartPrayerNotificationManager.EXTRA_OFFSET_MINS, 15)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context.applicationContext)
                val repository = AppRepository(db, context.applicationContext)
                repository.setPrayerStatus(prayerNameEnum, prayerDateStr, com.example.data.model.PrayerStatus.NEEDS_INPUT)

                val isFriday = PrayerDisplayUtils.isFriday(prayerDateStr)
                val displayName = PrayerDisplayUtils.getPrayerDisplayName(prayerNameEnum, isFriday)

                val nowMillis = System.currentTimeMillis()
                val todayTimes = repository.getTodayPrayerTimes()
                val prayerItem = todayTimes.find { it.name == prayerNameEnum }

                val prayerTriggerMillis = if (triggerMillis > 0L) triggerMillis else (prayerItem?.timeMillis ?: 0L)
                val prayerWindowEnd = if (windowEndMillis > 0L) windowEndMillis else (prayerTriggerMillis + 4 * 3600 * 1000L)

                val prayerStarted = if (prayerTriggerMillis > 0L) {
                    nowMillis >= prayerTriggerMillis
                } else {
                    true
                }

                val followUpMillis = if (prayerTriggerMillis > 0L) prayerTriggerMillis + 45 * 60 * 1000L else 0L
                val isFollowUpStage = (stage == SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP) ||
                        (followUpMillis > 0L && nowMillis >= followUpMillis)

                if (prayerStarted) {
                    if (isFollowUpStage) {
                        showFardPrayerNotification(
                            context = context,
                            title = "$displayName Prayer Reminder",
                            message = "Did you get a chance to pray $displayName?",
                            notificationId = notifId,
                            prayerName = prayerNameEnum.name,
                            displayName = displayName,
                            prayerDate = prayerDateStr,
                            channelId = SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME,
                            includeMarkAsPrayed = true,
                            triggerMillis = prayerTriggerMillis,
                            windowEndMillis = prayerWindowEnd,
                            stage = SmartPrayerNotificationManager.EVENT_TYPE_FARD_FOLLOWUP,
                            offsetMins = offsetMins
                        )
                    } else {
                        showFardPrayerNotification(
                            context = context,
                            title = "$displayName Prayer",
                            message = "$displayName prayer time has entered.",
                            notificationId = notifId,
                            prayerName = prayerNameEnum.name,
                            displayName = displayName,
                            prayerDate = prayerDateStr,
                            channelId = SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME,
                            includeMarkAsPrayed = true,
                            triggerMillis = prayerTriggerMillis,
                            windowEndMillis = prayerWindowEnd,
                            stage = SmartPrayerNotificationManager.EVENT_TYPE_FARD,
                            offsetMins = offsetMins
                        )
                        // Reschedule follow-up alarm if it's in the future and within prayer window
                        if (followUpMillis > nowMillis && followUpMillis < prayerWindowEnd) {
                            val manager = SmartPrayerNotificationManager(context.applicationContext)
                            manager.scheduleFollowUpAlarm(prayerNameEnum, prayerDateStr, followUpMillis, prayerWindowEnd)
                        }
                    }
                } else {
                    // Approaching reminders removed — dismiss notification
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(notifId)
                }
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
        channelId: String,
        includeMarkAsPrayed: Boolean = true,
        triggerMillis: Long = 0L,
        windowEndMillis: Long = 0L,
        stage: String = SmartPrayerNotificationManager.EVENT_TYPE_FARD,
        offsetMins: Int = 15
    ) {
        SmartPrayerNotificationManager.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("fivelight://prayer/$prayerDate/$prayerName/open")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 10 + 3,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            action = SmartPrayerNotificationManager.ACTION_SHOW_ADHAN_OVERLAY
            data = Uri.parse("fivelight://adhan/$prayerDate/$prayerName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 10 + 4,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setSubText("FiveLight")
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        val lang = com.example.data.util.AppLocaleManager.getPersistedLanguage(context)
        val localizedContext = com.example.data.util.AppLocaleManager.createLocalizedContext(context, lang)

        // Action Order:
        // 1. Mark as prayed (Primary action)
        // 2. Open FiveLight (Secondary action)
        if (includeMarkAsPrayed) {
            val markPrayedIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
                action = SmartPrayerNotificationManager.ACTION_MARK_PRAYED
                data = Uri.parse("fivelight://prayer/$prayerDate/$prayerName/mark_prayed")
                putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
                putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
                putExtra(SmartPrayerNotificationManager.EXTRA_NOTIF_ID, notificationId)
                putExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE, stage)
                putExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, triggerMillis)
                putExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_END_MILLIS, windowEndMillis)
                putExtra(SmartPrayerNotificationManager.EXTRA_OFFSET_MINS, offsetMins)
            }
            val markPrayedPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 1,
                markPrayedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                R.drawable.ic_notification_small,
                localizedContext.getString(R.string.notif_action_mark_prayed),
                markPrayedPendingIntent
            )
        }

        builder.addAction(
            R.drawable.ic_notification_small,
            localizedContext.getString(R.string.app_name),
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
        notificationId: Int,
        stage: String = SmartPrayerNotificationManager.EVENT_TYPE_FARD,
        triggerMillis: Long = 0L,
        windowEndMillis: Long = 0L,
        offsetMins: Int = 15
    ) {
        SmartPrayerNotificationManager.createNotificationChannels(context)

        val lang = com.example.data.util.AppLocaleManager.getPersistedLanguage(context)
        val localizedContext = com.example.data.util.AppLocaleManager.createLocalizedContext(context, lang)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("fivelight://prayer/$prayerDate/$prayerName/open")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 10 + 3,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val undoIntent = Intent(context, SmartPrayerNotificationReceiver::class.java).apply {
            action = SmartPrayerNotificationManager.ACTION_UNDO_PRAYED
            data = Uri.parse("fivelight://prayer/$prayerDate/$prayerName/undo_prayed")
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_NAME, prayerName)
            putExtra(SmartPrayerNotificationManager.EXTRA_PRAYER_DATE, prayerDate)
            putExtra(SmartPrayerNotificationManager.EXTRA_NOTIF_ID, notificationId)
            putExtra(SmartPrayerNotificationManager.EXTRA_EVENT_TYPE, stage)
            putExtra(SmartPrayerNotificationManager.EXTRA_TRIGGER_MILLIS, triggerMillis)
            putExtra(SmartPrayerNotificationManager.EXTRA_WINDOW_END_MILLIS, windowEndMillis)
            putExtra(SmartPrayerNotificationManager.EXTRA_OFFSET_MINS, offsetMins)
        }
        val undoPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            undoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "✓ $displayName"
        val message = localizedContext.getString(R.string.home_prayed)

        val builder = NotificationCompat.Builder(context, SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setSubText("FiveLight")
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_notification_small,
                localizedContext.getString(R.string.notif_action_undo),
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

        val priority = if (channelId == SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER ||
            channelId == SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER) {
            NotificationCompat.PRIORITY_LOW
        } else {
            NotificationCompat.PRIORITY_HIGH
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setSubText("FiveLight")
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .apply {
                if (priority == NotificationCompat.PRIORITY_LOW) {
                    setSound(null)
                    setVibrate(null)
                }
            }
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
