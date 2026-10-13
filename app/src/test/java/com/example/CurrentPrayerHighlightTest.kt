package com.example

import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.ui.screens.calculateCurrentPrayerContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CurrentPrayerHighlightTest {

    private fun createSamplePrayerTimes(baseDayMillis: Long): List<PrayerItem> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = baseDayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        fun timeAt(hour: Int, minute: Int): Long {
            return (cal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }.timeInMillis
        }

        return listOf(
            PrayerItem(PrayerName.FAJR, "05:15 AM", timeAt(5, 15)),
            PrayerItem(PrayerName.SUNRISE, "06:30 AM", timeAt(6, 30)),
            PrayerItem(PrayerName.DHUHR, "12:30 PM", timeAt(12, 30)),
            PrayerItem(PrayerName.ASR, "03:45 PM", timeAt(15, 45)),
            PrayerItem(PrayerName.MAGHRIB, "06:15 PM", timeAt(18, 15)),
            PrayerItem(PrayerName.ISHA, "07:45 PM", timeAt(19, 45))
        )
    }

    @Test
    fun testFajrWindow() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)
        val fajr = prayers.first { it.name == PrayerName.FAJR }.timeMillis
        val sunrise = prayers.first { it.name == PrayerName.SUNRISE }.timeMillis

        // At exactly Fajr start
        val contextAtFajr = calculateCurrentPrayerContext(prayers, fajr)
        assertEquals(PrayerName.FAJR, contextAtFajr.currentPrayerName)
        assertEquals(0f, contextAtFajr.progress, 0.001f)
        assertEquals(fajr, contextAtFajr.windowStartMillis)
        assertEquals(sunrise, contextAtFajr.windowEndMillis)

        // Mid-Fajr
        val midFajr = fajr + (sunrise - fajr) / 2
        val contextMidFajr = calculateCurrentPrayerContext(prayers, midFajr)
        assertEquals(PrayerName.FAJR, contextMidFajr.currentPrayerName)
        assertEquals(0.5f, contextMidFajr.progress, 0.01f)

        // Just 1 millisecond before Sunrise
        val justBeforeSunrise = sunrise - 1L
        val contextNearSunrise = calculateCurrentPrayerContext(prayers, justBeforeSunrise)
        assertEquals(PrayerName.FAJR, contextNearSunrise.currentPrayerName)
        assertTrue(contextNearSunrise.progress > 0.99f)
    }

    @Test
    fun testBetweenSunriseAndDhuhr_NoHighlight() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)
        val sunrise = prayers.first { it.name == PrayerName.SUNRISE }.timeMillis
        val dhuhr = prayers.first { it.name == PrayerName.DHUHR }.timeMillis

        // Exactly at Sunrise
        val contextAtSunrise = calculateCurrentPrayerContext(prayers, sunrise)
        assertNull(contextAtSunrise.currentPrayerName)
        assertEquals(0f, contextAtSunrise.progress, 0.001f)

        // Mid-morning (e.g. 09:00 AM)
        val midMorning = (sunrise + dhuhr) / 2
        val contextMidMorning = calculateCurrentPrayerContext(prayers, midMorning)
        assertNull(contextMidMorning.currentPrayerName)
        assertEquals(0f, contextMidMorning.progress, 0.001f)

        // Just before Dhuhr
        val contextBeforeDhuhr = calculateCurrentPrayerContext(prayers, dhuhr - 1L)
        assertNull(contextBeforeDhuhr.currentPrayerName)
    }

    @Test
    fun testDhuhrWindow() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)
        val dhuhr = prayers.first { it.name == PrayerName.DHUHR }.timeMillis
        val asr = prayers.first { it.name == PrayerName.ASR }.timeMillis

        // Exactly at Dhuhr
        val contextAtDhuhr = calculateCurrentPrayerContext(prayers, dhuhr)
        assertEquals(PrayerName.DHUHR, contextAtDhuhr.currentPrayerName)
        assertEquals(0f, contextAtDhuhr.progress, 0.001f)

        // 50% through Dhuhr window
        val midDhuhr = dhuhr + (asr - dhuhr) / 2
        val contextMidDhuhr = calculateCurrentPrayerContext(prayers, midDhuhr)
        assertEquals(PrayerName.DHUHR, contextMidDhuhr.currentPrayerName)
        assertEquals(0.5f, contextMidDhuhr.progress, 0.01f)
    }

    @Test
    fun testAsrWindow() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)
        val asr = prayers.first { it.name == PrayerName.ASR }.timeMillis
        val maghrib = prayers.first { it.name == PrayerName.MAGHRIB }.timeMillis

        val contextAtAsr = calculateCurrentPrayerContext(prayers, asr)
        assertEquals(PrayerName.ASR, contextAtAsr.currentPrayerName)
        assertEquals(0f, contextAtAsr.progress, 0.001f)

        val midAsr = asr + (maghrib - asr) / 2
        val contextMidAsr = calculateCurrentPrayerContext(prayers, midAsr)
        assertEquals(PrayerName.ASR, contextMidAsr.currentPrayerName)
        assertEquals(0.5f, contextMidAsr.progress, 0.01f)
    }

    @Test
    fun testMaghribWindow() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)
        val maghrib = prayers.first { it.name == PrayerName.MAGHRIB }.timeMillis
        val isha = prayers.first { it.name == PrayerName.ISHA }.timeMillis

        val contextAtMaghrib = calculateCurrentPrayerContext(prayers, maghrib)
        assertEquals(PrayerName.MAGHRIB, contextAtMaghrib.currentPrayerName)
        assertEquals(0f, contextAtMaghrib.progress, 0.001f)

        val midMaghrib = maghrib + (isha - maghrib) / 2
        val contextMidMaghrib = calculateCurrentPrayerContext(prayers, midMaghrib)
        assertEquals(PrayerName.MAGHRIB, contextMidMaghrib.currentPrayerName)
        assertEquals(0.5f, contextMidMaghrib.progress, 0.01f)
    }

    @Test
    fun testIshaWindow_EveningAndAfterMidnight() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)
        val isha = prayers.first { it.name == PrayerName.ISHA }.timeMillis
        val fajr = prayers.first { it.name == PrayerName.FAJR }.timeMillis
        val tomorrowFajr = fajr + 24 * 3600 * 1000L

        // 1. Exactly at Isha in the evening
        val contextAtIsha = calculateCurrentPrayerContext(prayers, isha)
        assertEquals(PrayerName.ISHA, contextAtIsha.currentPrayerName)
        assertEquals(0f, contextAtIsha.progress, 0.001f)
        assertEquals(isha, contextAtIsha.windowStartMillis)
        assertEquals(tomorrowFajr, contextAtIsha.windowEndMillis)

        // 2. Late evening (11:00 PM)
        val lateEvening = isha + 3 * 3600 * 1000L
        val contextLateEvening = calculateCurrentPrayerContext(prayers, lateEvening)
        assertEquals(PrayerName.ISHA, contextLateEvening.currentPrayerName)
        assertTrue(contextLateEvening.progress > 0f && contextLateEvening.progress < 1f)

        // 3. After midnight (02:30 AM before today's Fajr)
        val afterMidnightCal = Calendar.getInstance().apply {
            timeInMillis = baseMillis
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 30)
        }
        val afterMidnightMillis = afterMidnightCal.timeInMillis
        val contextAfterMidnight = calculateCurrentPrayerContext(prayers, afterMidnightMillis)
        assertEquals(PrayerName.ISHA, contextAfterMidnight.currentPrayerName)
        assertEquals(fajr, contextAfterMidnight.windowEndMillis)
        val yesterdayIsha = isha - 24 * 3600 * 1000L
        assertEquals(yesterdayIsha, contextAfterMidnight.windowStartMillis)
        assertTrue(contextAfterMidnight.progress > 0.5f && contextAfterMidnight.progress < 1f)

        // 4. Just before next Fajr (05:14 AM)
        val justBeforeFajr = fajr - 60 * 1000L
        val contextBeforeFajr = calculateCurrentPrayerContext(prayers, justBeforeFajr)
        assertEquals(PrayerName.ISHA, contextBeforeFajr.currentPrayerName)
        assertTrue(contextBeforeFajr.progress > 0.95f && contextBeforeFajr.progress <= 1f)
    }

    @Test
    fun testTransitionsAcrossAllBoundaries() {
        val baseMillis = 1700000000000L
        val prayers = createSamplePrayerTimes(baseMillis)

        // Before Fajr -> Isha
        val pBeforeFajr = calculateCurrentPrayerContext(prayers, prayers[0].timeMillis - 1)
        assertEquals(PrayerName.ISHA, pBeforeFajr.currentPrayerName)

        // At Fajr -> Fajr
        val pAtFajr = calculateCurrentPrayerContext(prayers, prayers[0].timeMillis)
        assertEquals(PrayerName.FAJR, pAtFajr.currentPrayerName)

        // At Sunrise -> None
        val pAtSunrise = calculateCurrentPrayerContext(prayers, prayers[1].timeMillis)
        assertNull(pAtSunrise.currentPrayerName)

        // At Dhuhr -> Dhuhr
        val pAtDhuhr = calculateCurrentPrayerContext(prayers, prayers[2].timeMillis)
        assertEquals(PrayerName.DHUHR, pAtDhuhr.currentPrayerName)

        // At Asr -> Asr
        val pAtAsr = calculateCurrentPrayerContext(prayers, prayers[3].timeMillis)
        assertEquals(PrayerName.ASR, pAtAsr.currentPrayerName)

        // At Maghrib -> Maghrib
        val pAtMaghrib = calculateCurrentPrayerContext(prayers, prayers[4].timeMillis)
        assertEquals(PrayerName.MAGHRIB, pAtMaghrib.currentPrayerName)

        // At Isha -> Isha
        val pAtIsha = calculateCurrentPrayerContext(prayers, prayers[5].timeMillis)
        assertEquals(PrayerName.ISHA, pAtIsha.currentPrayerName)
    }
}
