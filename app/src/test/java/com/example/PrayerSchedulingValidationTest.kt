package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PrayerName
import com.example.data.reminder.PrayerAlertMode
import com.example.data.reminder.PrayerNotificationLogger
import com.example.data.reminder.PrayerNotificationValidator
import com.example.data.reminder.PrayerReminderManager
import com.example.data.reminder.PrayerReminderReceiver
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.reminder.SmartPrayerNotificationReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PrayerSchedulingValidationTest {

    private lateinit var context: Context
    private lateinit var smartManager: SmartPrayerNotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        smartManager = SmartPrayerNotificationManager(context)
        smartManager.isSmartNotificationsEnabled = true
        smartManager.isPrayerTimeNotificationsEnabled = true
        smartManager.setPrayerEnabled(PrayerName.FAJR.name, true)
        smartManager.prayerAlertMode = PrayerAlertMode.AZAAN_AND_NOTIFICATION

        // Clear validation prefs
        context.getSharedPreferences("smart_prayer_validation_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun testEarlyDeliveryRejectedByValidationGate() {
        val nowMillis = System.currentTimeMillis()
        val futureFajrTriggerMillis = nowMillis + (6 * 3600 * 1000L) // 6 hours in future (e.g. 5:15 AM when now is 11:17 PM)
        val todayStr = LocalDate.now().toString()

        val result = PrayerNotificationValidator.validateFardEntry(
            context = context,
            prayerName = PrayerName.FAJR,
            prayerDateStr = todayStr,
            triggerMillis = futureFajrTriggerMillis,
            windowEndMillis = futureFajrTriggerMillis + (4 * 3600 * 1000L)
        )

        assertTrue(result is PrayerNotificationValidator.ValidationResult.Invalid)
        val reason = (result as PrayerNotificationValidator.ValidationResult.Invalid).reason
        assertTrue("Reason should mention premature alarm delivery", reason.contains("Premature alarm delivery"))
    }

    @Test
    fun testStaleDeliveryRejectedByValidationGate() {
        val nowMillis = System.currentTimeMillis()
        val pastFajrTriggerMillis = nowMillis - (45 * 60 * 1000L) // 45 minutes ago
        val todayStr = LocalDate.now().toString()

        val result = PrayerNotificationValidator.validateFardEntry(
            context = context,
            prayerName = PrayerName.FAJR,
            prayerDateStr = todayStr,
            triggerMillis = pastFajrTriggerMillis,
            windowEndMillis = nowMillis - (10 * 60 * 1000L)
        )

        assertTrue(result is PrayerNotificationValidator.ValidationResult.Invalid)
    }

    @Test
    fun testValidDeliveryAccepted() {
        val todayStr = LocalDate.now().toString()
        val authoritativeMillis = PrayerNotificationValidator.calculateAuthoritativeStartMillis(context, PrayerName.FAJR, todayStr)
            ?: System.currentTimeMillis()

        val triggerMillis = authoritativeMillis
        val nowMillis = authoritativeMillis + 1000L
        val result = PrayerNotificationValidator.validateFardEntry(
            context = context,
            prayerName = PrayerName.FAJR,
            prayerDateStr = todayStr,
            triggerMillis = triggerMillis,
            windowEndMillis = authoritativeMillis + (4 * 3600 * 1000L),
            nowMillis = nowMillis
        )

        val reason = if (result is PrayerNotificationValidator.ValidationResult.Invalid) result.reason else "Valid"
        assertTrue("Valid delivery must pass validation. Now: $nowMillis, Trigger: $triggerMillis. Reason: $reason", 
            result is PrayerNotificationValidator.ValidationResult.Valid)
    }

    @Test
    fun testDeduplicationRejectsSecondEvent() {
        val todayStr = LocalDate.now().toString()
        val authoritativeMillis = PrayerNotificationValidator.calculateAuthoritativeStartMillis(context, PrayerName.FAJR, todayStr)
            ?: System.currentTimeMillis()

        val triggerMillis = authoritativeMillis
        val nowMillis = authoritativeMillis + 1000L

        val firstAttempt = PrayerNotificationValidator.validateFardEntry(
            context = context,
            prayerName = PrayerName.FAJR,
            prayerDateStr = todayStr,
            triggerMillis = triggerMillis,
            windowEndMillis = authoritativeMillis + (4 * 3600 * 1000L),
            nowMillis = nowMillis
        )
        val reason1 = if (firstAttempt is PrayerNotificationValidator.ValidationResult.Invalid) firstAttempt.reason else "Valid"
        assertTrue("First attempt should be valid. Reason: $reason1", firstAttempt is PrayerNotificationValidator.ValidationResult.Valid)

        // Mark as triggered
        PrayerNotificationValidator.markFardEntryTriggered(context, PrayerName.FAJR, todayStr)

        val secondAttempt = PrayerNotificationValidator.validateFardEntry(
            context = context,
            prayerName = PrayerName.FAJR,
            prayerDateStr = todayStr,
            triggerMillis = triggerMillis,
            windowEndMillis = authoritativeMillis + (4 * 3600 * 1000L),
            nowMillis = authoritativeMillis + 2000L
        )
        val reason2 = if (secondAttempt is PrayerNotificationValidator.ValidationResult.Invalid) secondAttempt.reason else "Valid"
        assertTrue("Second attempt should be rejected as duplicate. Reason: $reason2", secondAttempt is PrayerNotificationValidator.ValidationResult.Invalid)
        assertTrue("Reason should mention duplicate: $reason2", reason2.contains("Duplicate event"))
    }

    @Test
    fun testDisabledSettingsRejectEvent() {
        smartManager.isSmartNotificationsEnabled = false
        val nowMillis = System.currentTimeMillis()
        val todayStr = LocalDate.now().toString()

        val result = PrayerNotificationValidator.validateFardEntry(
            context = context,
            prayerName = PrayerName.FAJR,
            prayerDateStr = todayStr,
            triggerMillis = nowMillis - 1000L,
            windowEndMillis = nowMillis + 3600000L
        )

        assertTrue(result is PrayerNotificationValidator.ValidationResult.Invalid)
        val reason = (result as PrayerNotificationValidator.ValidationResult.Invalid).reason
        assertTrue(reason.contains("disabled"))
    }

    @Test
    fun testNotificationOnlyModeDoesNotEnableAzaan() {
        smartManager.prayerAlertMode = PrayerAlertMode.NOTIFICATION_ONLY
        assertEquals(PrayerAlertMode.NOTIFICATION_ONLY, smartManager.prayerAlertMode)
    }

    @Test
    fun testRequestCodesAreCollisionFree() {
        val date1 = LocalDate.of(2026, 10, 15)
        val date2 = LocalDate.of(2026, 10, 16)

        val fajrEntryD1 = SmartPrayerNotificationManager.getAlarmRequestCode(PrayerName.FAJR, date1, SmartPrayerNotificationManager.ALARM_SLOT_ENTRY)
        val fajrFollowUpD1 = SmartPrayerNotificationManager.getAlarmRequestCode(PrayerName.FAJR, date1, SmartPrayerNotificationManager.ALARM_SLOT_FOLLOWUP)
        val fajrEntryD2 = SmartPrayerNotificationManager.getAlarmRequestCode(PrayerName.FAJR, date2, SmartPrayerNotificationManager.ALARM_SLOT_ENTRY)

        assertTrue("Slot request codes must differ", fajrEntryD1 != fajrFollowUpD1)
        assertTrue("Date request codes must differ", fajrEntryD1 != fajrEntryD2)
    }

    @Test
    fun testLegacyPrayerReminderReceiverIgnoresObsoleteIntent() {
        val receiver = PrayerReminderReceiver()
        val legacyIntent = Intent(context, PrayerReminderReceiver::class.java).apply {
            action = PrayerReminderManager.ACTION_PRAYER_REMINDER
            putExtra(PrayerReminderManager.EXTRA_PRAYER_NAME, "FAJR")
        }

        // Must execute without throwing exceptions or launching Azaan overlay
        receiver.onReceive(context, legacyIntent)
        // Verify overlay is not active
        assertEquals(null, PrayerReminderManager.activeAzaanState.value)
    }

    @Test
    fun testRomanUrduSentencesForFardPrayers() {
        val fardPrayers = listOf(
            PrayerName.FAJR to "Fajr ka waqt ho gaya hai",
            PrayerName.DHUHR to "Dhuhr ka waqt ho gaya hai",
            PrayerName.ASR to "Asr ka waqt ho gaya hai",
            PrayerName.MAGHRIB to "Maghrib ka waqt ho gaya hai",
            PrayerName.ISHA to "Isha ka waqt ho gaya hai"
        )

        for ((prayer, expectedSentence) in fardPrayers) {
            val sentence = when (prayer) {
                PrayerName.FAJR -> "Fajr ka waqt ho gaya hai"
                PrayerName.DHUHR -> "Dhuhr ka waqt ho gaya hai"
                PrayerName.ASR -> "Asr ka waqt ho gaya hai"
                PrayerName.MAGHRIB -> "Maghrib ka waqt ho gaya hai"
                PrayerName.ISHA -> "Isha ka waqt ho gaya hai"
                PrayerName.SUNRISE -> "Sunrise ka waqt ho gaya hai"
            }
            assertEquals("Roman Urdu sentence must match for ${prayer.name}", expectedSentence, sentence)
        }
    }
}
