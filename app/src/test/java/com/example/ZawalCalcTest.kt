package com.example

import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.util.ZawalCalc
import com.example.data.util.ZawalDisplayMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ZawalCalcTest {

    @Test
    fun testZawalStateTransitions() {
        // Dhuhr at 12:30 PM
        val dhuhrMillis = 1700000000000L
        val prayerTimes = listOf(
            PrayerItem(PrayerName.FAJR, "05:00 AM", dhuhrMillis - 7 * 3600 * 1000L),
            PrayerItem(PrayerName.SUNRISE, "06:30 AM", dhuhrMillis - 6 * 3600 * 1000L),
            PrayerItem(PrayerName.DHUHR, "12:30 PM", dhuhrMillis),
            PrayerItem(PrayerName.ASR, "03:45 PM", dhuhrMillis + 3 * 3600 * 1000L),
            PrayerItem(PrayerName.MAGHRIB, "06:15 PM", dhuhrMillis + 5 * 3600 * 1000L),
            PrayerItem(PrayerName.ISHA, "07:30 PM", dhuhrMillis + 7 * 3600 * 1000L)
        )

        val zawalStartMillis = dhuhrMillis - 10 * 60 * 1000L // 12:20 PM
        val approachingStartMillis = zawalStartMillis - 15 * 60 * 1000L // 12:05 PM

        // 1. Far before Zawal -> HIDDEN
        val infoHiddenBefore = ZawalCalc.calculateZawalInfo(prayerTimes, false, approachingStartMillis - 30 * 60 * 1000L)
        assertEquals(ZawalDisplayMode.HIDDEN, infoHiddenBefore.mode)

        // 2. Approaching Zawal -> APPROACHING (8 min remaining)
        val infoApproaching = ZawalCalc.calculateZawalInfo(prayerTimes, false, zawalStartMillis - 8 * 60 * 1000L)
        assertEquals(ZawalDisplayMode.APPROACHING, infoApproaching.mode)
        assertEquals(8, infoApproaching.minutesRemaining)

        // 3. Active Zawal -> ACTIVE (6 min remaining until Dhuhr)
        val infoActive = ZawalCalc.calculateZawalInfo(prayerTimes, false, dhuhrMillis - 6 * 60 * 1000L)
        assertEquals(ZawalDisplayMode.ACTIVE, infoActive.mode)
        assertEquals(6, infoActive.minutesRemaining)

        // 4. After Zawal -> HIDDEN
        val infoHiddenAfter = ZawalCalc.calculateZawalInfo(prayerTimes, false, dhuhrMillis + 60 * 1000L)
        assertEquals(ZawalDisplayMode.HIDDEN, infoHiddenAfter.mode)
    }
}
