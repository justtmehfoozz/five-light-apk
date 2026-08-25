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
}
