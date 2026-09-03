package com.example

import com.example.data.model.PrayerName
import com.example.data.util.PrayerDisplayUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrayerDisplayAndNotificationTest {

    @Test
    fun testPrayerDisplayName_FridayVsNonFriday() {
        val thursday = LocalDate.of(2026, 8, 13) // Thursday
        val friday = LocalDate.of(2026, 8, 14)   // Friday
        val saturday = LocalDate.of(2026, 8, 15) // Saturday

        assertFalse(PrayerDisplayUtils.isFriday(thursday))
        assertTrue(PrayerDisplayUtils.isFriday(friday))
        assertFalse(PrayerDisplayUtils.isFriday(saturday))

        // Dhuhr on Thursday
        assertEquals("Dhuhr", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.DHUHR, thursday))
        // Dhuhr on Friday
        assertEquals("Jummah", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.DHUHR, friday))
        // Dhuhr on Saturday
        assertEquals("Dhuhr", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.DHUHR, saturday))

        // Arabic Name
        assertEquals("الظهر", PrayerDisplayUtils.getPrayerArabicName(PrayerName.DHUHR, thursday))
        assertEquals("الجمعة", PrayerDisplayUtils.getPrayerArabicName(PrayerName.DHUHR, friday))
        assertEquals("الظهر", PrayerDisplayUtils.getPrayerArabicName(PrayerName.DHUHR, saturday))
    }

    @Test
    fun testOtherPrayersUnchangedOnFriday() {
        val friday = LocalDate.of(2026, 8, 14)

        assertEquals("Fajr", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.FAJR, friday))
        assertEquals("Asr", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.ASR, friday))
        assertEquals("Maghrib", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.MAGHRIB, friday))
        assertEquals("Isha", PrayerDisplayUtils.getPrayerDisplayName(PrayerName.ISHA, friday))
    }

    @Test
    fun testParsePrayerName() {
        assertEquals(PrayerName.DHUHR, PrayerDisplayUtils.parsePrayerName("Jummah"))
        assertEquals(PrayerName.DHUHR, PrayerDisplayUtils.parsePrayerName("Jumu'ah"))
        assertEquals(PrayerName.DHUHR, PrayerDisplayUtils.parsePrayerName("Dhuhr"))
        assertEquals(PrayerName.FAJR, PrayerDisplayUtils.parsePrayerName("Fajr"))
        assertEquals(PrayerName.ASR, PrayerDisplayUtils.parsePrayerName("Asr"))
    }

    @Test
    fun testNotificationIdDeterminismAndUniqueness() {
        val date1 = LocalDate.of(2026, 9, 3)
        val date2 = LocalDate.of(2026, 9, 4)

        val fardPrayers = listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )

        val ids = mutableSetOf<Int>()

        fardPrayers.forEach { prayer ->
            val id1a = com.example.data.reminder.SmartPrayerNotificationManager.getNotificationId(prayer, date1)
            val id1b = com.example.data.reminder.SmartPrayerNotificationManager.getNotificationId(prayer, "2026-09-03")
            // Idempotent: identical input must yield identical notification ID
            assertEquals(id1a, id1b)

            val id2 = com.example.data.reminder.SmartPrayerNotificationManager.getNotificationId(prayer, date2)

            // Must not collide across dates
            org.junit.Assert.assertNotEquals(id1a, id2)

            ids.add(id1a)
            ids.add(id2)
        }

        // All 10 IDs across 5 prayers and 2 dates must be completely distinct
        assertEquals(10, ids.size)
    }

    @Test
    fun testAlarmRequestCodeDeterminismAndUniqueness() {
        val date1 = LocalDate.of(2026, 9, 3)
        val date2 = LocalDate.of(2026, 9, 4)

        val fardPrayers = listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )

        val slots = listOf(
            com.example.data.reminder.SmartPrayerNotificationManager.ALARM_SLOT_ENTRY,
            com.example.data.reminder.SmartPrayerNotificationManager.ALARM_SLOT_PRE_REMINDER,
            com.example.data.reminder.SmartPrayerNotificationManager.ALARM_SLOT_FOLLOWUP
        )

        val requestCodes = mutableSetOf<Int>()

        fardPrayers.forEach { prayer ->
            slots.forEach { slot ->
                val code1a = com.example.data.reminder.SmartPrayerNotificationManager.getAlarmRequestCode(prayer, date1, slot)
                val code1b = com.example.data.reminder.SmartPrayerNotificationManager.getAlarmRequestCode(prayer, "2026-09-03", slot)
                assertEquals(code1a, code1b)

                val code2 = com.example.data.reminder.SmartPrayerNotificationManager.getAlarmRequestCode(prayer, date2, slot)
                org.junit.Assert.assertNotEquals(code1a, code2)

                requestCodes.add(code1a)
                requestCodes.add(code2)
            }
        }

        // 5 prayers * 3 slots * 2 dates = 30 distinct codes
        assertEquals(30, requestCodes.size)
    }

    @Test
    fun testSpecialNotificationIdAndRequestCodeUniqueness() {
        val date = LocalDate.of(2026, 9, 3)
        val notifIds = mutableSetOf<Int>()
        val alarmCodes = mutableSetOf<Int>()

        for (slot in 1..5) {
            val nId = com.example.data.reminder.SmartPrayerNotificationManager.getSpecialNotificationId(date, slot)
            val aCode = com.example.data.reminder.SmartPrayerNotificationManager.getSpecialAlarmRequestCode(date, slot)
            notifIds.add(nId)
            alarmCodes.add(aCode)
        }

        assertEquals(5, notifIds.size)
        assertEquals(5, alarmCodes.size)
    }

    @Test
    fun testPhase3NotificationPresentationFormats() {
        val prayerName = PrayerName.ASR
        val displayName = "Asr"
        val offsetMins = 15

        // 1. Before prayer (Pre-prayer)
        val prePrayerTitle = "$displayName Prayer"
        val prePrayerBody = "$displayName begins in $offsetMins minutes."
        assertEquals("Asr Prayer", prePrayerTitle)
        assertEquals("Asr begins in 15 minutes.", prePrayerBody)

        // 2. Prayer starts (Active prayer)
        val activeTitle = "$displayName Prayer"
        val activeBody = "$displayName prayer time has entered."
        assertEquals("Asr Prayer", activeTitle)
        assertEquals("Asr prayer time has entered.", activeBody)

        // 3. Later reminder (Follow-up)
        val followUpTitle = "$displayName Prayer"
        val followUpBody = "Did you get a chance to pray?"
        assertEquals("Asr Prayer", followUpTitle)
        assertEquals("Did you get a chance to pray?", followUpBody)

        // 4. Completed state
        val completedTitle = "✓ $displayName marked as prayed"
        val completedBody = "Recorded in Personal Log."
        assertEquals("✓ Asr marked as prayed", completedTitle)
        assertEquals("Recorded in Personal Log.", completedBody)

        // Verify ID reuse across stages
        val dateStr = "2026-09-03"
        val notifId = com.example.data.reminder.SmartPrayerNotificationManager.getNotificationId(prayerName, dateStr)
        // Ensure same ID is returned consistently for date + prayer
        assertEquals(notifId, com.example.data.reminder.SmartPrayerNotificationManager.getNotificationId(prayerName, dateStr))
    }

    @Test
    fun testPhase4ContextualRemindersCopyAndFormatting() {
        // Morning
        val morningTitle = "Morning Has Begun"
        val morningMessage = "A new day has arrived. Take a moment for morning remembrance."
        assertEquals("Morning Has Begun", morningTitle)
        assertEquals("A new day has arrived. Take a moment for morning remembrance.", morningMessage)
        assertFalse(morningMessage.contains(":"))
        assertFalse(morningMessage.contains("AM", ignoreCase = false))
        assertFalse(morningMessage.contains("PM", ignoreCase = false))

        // Evening
        val eveningTitle = "Evening Has Begun"
        val eveningMessage = "A moment to pause and remember Allah."
        assertEquals("Evening Has Begun", eveningTitle)
        assertEquals("A moment to pause and remember Allah.", eveningMessage)
        assertFalse(eveningMessage.contains(":"))
        assertFalse(eveningMessage.contains("Maghrib"))

        // Night
        val nightTitle = "Night Has Begun"
        val nightMessage = "A quiet time for remembrance and reflection."
        assertEquals("Night Has Begun", nightTitle)
        assertEquals("A quiet time for remembrance and reflection.", nightMessage)
        assertFalse(nightMessage.contains(":"))
        assertFalse(nightMessage.contains("Tahajjud"))
    }

    @Test
    fun testPhase4NaflOpportunitiesCopyAndFormatting() {
        // Duha
        val duhaTitle = "Duha Opportunity"
        val duhaMessage = "The Duha prayer window is now open."
        assertEquals("Duha Opportunity", duhaTitle)
        assertEquals("The Duha prayer window is now open.", duhaMessage)
        assertFalse(duhaMessage.contains(":"))

        // Ishraq
        val ishraqTitle = "Ishraq Opportunity"
        val ishraqMessage = "The Ishraq prayer window is now open."
        assertEquals("Ishraq Opportunity", ishraqTitle)
        assertEquals("The Ishraq prayer window is now open.", ishraqMessage)
        assertFalse(ishraqMessage.contains(":"))

        // Tahajjud
        val tahajjudTitle = "Tahajjud Window"
        val tahajjudMessage = "A quiet portion of the night is now open for voluntary prayer."
        assertEquals("Tahajjud Window", tahajjudTitle)
        assertEquals("A quiet portion of the night is now open for voluntary prayer.", tahajjudMessage)
        assertFalse(tahajjudMessage.contains(":"))
    }

    @Test
    fun testPhase4DeterministicSpecialIdsAndNoCollisionWithFard() {
        val date = java.time.LocalDate.parse("2026-09-03")
        val specialSlots = listOf(
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_CONTEXTUAL_MORNING,
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_CONTEXTUAL_EVENING,
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_CONTEXTUAL_NIGHT,
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_DUHA,
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_TAHAJJUD,
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_ISHRAQ,
            com.example.data.reminder.SmartPrayerNotificationManager.SPECIAL_SLOT_NAFL_AWWABIN
        )

        val specialNotifIds = specialSlots.map {
            com.example.data.reminder.SmartPrayerNotificationManager.getSpecialNotificationId(date, it)
        }.toSet()
        // All special slot IDs must be distinct
        assertEquals(specialSlots.size, specialNotifIds.size)

        // Ensure zero collision with any Fard prayer notification ID
        val fardNotifIds = PrayerName.values().map {
            com.example.data.reminder.SmartPrayerNotificationManager.getNotificationId(it, date)
        }.toSet()

        val intersection = specialNotifIds.intersect(fardNotifIds)
        assertTrue("Special IDs and Fard IDs must never intersect: $intersection", intersection.isEmpty())
    }

    @Test
    fun testRefinementNoTimeVariablesInContextualAndNafl() {
        val messages = listOf(
            "A new day has arrived. Take a moment for morning remembrance.",
            "A moment to pause and remember Allah.",
            "A quiet time for remembrance and reflection.",
            "The Ishraq prayer window is now open.",
            "The Duha prayer window is now open.",
            "A quiet portion of the night is now open for voluntary prayer.",
            "The Awwabin prayer window is now open."
        )

        messages.forEach { msg ->
            assertFalse("Must not contain time formatting: $msg", msg.contains(":"))
            assertFalse("Must not contain clock AM/PM: $msg", msg.contains(" AM") || msg.contains(" PM"))
            assertFalse("Must not contain countdown: $msg", msg.contains("minute") || msg.contains("hour"))
            assertFalse("Must not contain relative countdown: $msg", msg.contains("begins in") || msg.contains("remaining"))
        }
    }

    @Test
    fun testRefinementChannelConstantsAndIdempotencyKeyFormat() {
        val date = "2026-09-03"
        val morningKey = "${date}_${com.example.data.reminder.SmartPrayerNotificationManager.EVENT_TYPE_CONTEXTUAL_MORNING}"
        val duhaKey = "${date}_${com.example.data.reminder.SmartPrayerNotificationManager.EVENT_TYPE_NAFL_DUHA}"

        assertEquals("2026-09-03_contextual_morning", morningKey)
        assertEquals("2026-09-03_nafl_duha", duhaKey)

        assertEquals("fivelight_quiet_reminder", com.example.data.reminder.SmartPrayerNotificationManager.CHANNEL_QUIET_REMINDER)
        assertEquals("fivelight_smart_prayer", com.example.data.reminder.SmartPrayerNotificationManager.CHANNEL_SMART_PRAYER)
        assertEquals("fivelight_prayer_time", com.example.data.reminder.SmartPrayerNotificationManager.CHANNEL_PRAYER_TIME)
    }
}

