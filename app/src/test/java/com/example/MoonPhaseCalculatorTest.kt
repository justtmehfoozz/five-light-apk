package com.example

import com.example.data.model.MoonPhaseType
import com.example.data.util.MoonPhaseCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

class MoonPhaseCalculatorTest {

    @Test
    fun testAllEightPhasesMapping() {
        assertEquals(MoonPhaseType.NEW_MOON, MoonPhaseCalculator.getPhaseType(0.00))
        assertEquals(MoonPhaseType.NEW_MOON, MoonPhaseCalculator.getPhaseType(0.02))
        assertEquals(MoonPhaseType.NEW_MOON, MoonPhaseCalculator.getPhaseType(0.98))

        assertEquals(MoonPhaseType.WAXING_CRESCENT, MoonPhaseCalculator.getPhaseType(0.05))
        assertEquals(MoonPhaseType.WAXING_CRESCENT, MoonPhaseCalculator.getPhaseType(0.15))

        assertEquals(MoonPhaseType.FIRST_QUARTER, MoonPhaseCalculator.getPhaseType(0.24))
        assertEquals(MoonPhaseType.FIRST_QUARTER, MoonPhaseCalculator.getPhaseType(0.26))

        assertEquals(MoonPhaseType.WAXING_GIBBOUS, MoonPhaseCalculator.getPhaseType(0.35))
        assertEquals(MoonPhaseType.WAXING_GIBBOUS, MoonPhaseCalculator.getPhaseType(0.45))

        assertEquals(MoonPhaseType.FULL_MOON, MoonPhaseCalculator.getPhaseType(0.49))
        assertEquals(MoonPhaseType.FULL_MOON, MoonPhaseCalculator.getPhaseType(0.51))

        assertEquals(MoonPhaseType.WANING_GIBBOUS, MoonPhaseCalculator.getPhaseType(0.60))
        assertEquals(MoonPhaseType.WANING_GIBBOUS, MoonPhaseCalculator.getPhaseType(0.70))

        assertEquals(MoonPhaseType.LAST_QUARTER, MoonPhaseCalculator.getPhaseType(0.74))
        assertEquals(MoonPhaseType.LAST_QUARTER, MoonPhaseCalculator.getPhaseType(0.76))

        assertEquals(MoonPhaseType.WANING_CRESCENT, MoonPhaseCalculator.getPhaseType(0.85))
        assertEquals(MoonPhaseType.WANING_CRESCENT, MoonPhaseCalculator.getPhaseType(0.95))
    }

    @Test
    fun testReferenceNewMoonCalculations() {
        // Reference New Moon epoch: 2000-01-06 18:14:00 UTC
        val newMoonInstant = Instant.parse("2000-01-06T18:14:00Z")
        val moonPhase = MoonPhaseCalculator.calculateMoonPhase(newMoonInstant.toEpochMilli())

        assertEquals(MoonPhaseType.NEW_MOON, moonPhase.phaseType)
        assertTrue("Illumination should be very close to 0 for New Moon", moonPhase.illuminationPercent in 0..5)
        assertEquals("🌑", moonPhase.emoji)
        assertTrue(moonPhase.accessibleDescription.contains("New Moon"))
    }

    @Test
    fun testKnownFullMoonCalculations() {
        // Known Full Moon epoch: 2000-01-21 04:40:00 UTC (~14.5 days after reference New Moon)
        val fullMoonInstant = Instant.parse("2000-01-21T04:40:00Z")
        val moonPhase = MoonPhaseCalculator.calculateMoonPhase(fullMoonInstant.toEpochMilli())

        assertEquals(MoonPhaseType.FULL_MOON, moonPhase.phaseType)
        assertTrue("Illumination should be > 95% for Full Moon", moonPhase.illuminationPercent >= 95)
        assertEquals("🌕", moonPhase.emoji)
        assertTrue(moonPhase.accessibleDescription.contains("Full Moon"))
    }

    @Test
    fun testIlluminationBoundsAndProgress() {
        // Sample across multiple days in a full lunar cycle (30 steps)
        val startMillis = Instant.parse("2024-01-01T00:00:00Z").toEpochMilli()
        val dayMillis = 86_400_000L

        for (i in 0..60) {
            val currentMillis = startMillis + (i * dayMillis)
            val phase = MoonPhaseCalculator.calculateMoonPhase(currentMillis)

            assertTrue("Fraction should be between 0.0 and 1.0", phase.illuminationFraction in 0.0f..1.0f)
            assertTrue("Percent should be between 0 and 100", phase.illuminationPercent in 0..100)
            assertTrue("Phase progress should be between 0.0 and 1.0", phase.phaseAngle in 0.0f..1.0f)
            assertTrue("Age in days should be within synodic month", phase.ageDays in 0.0..MoonPhaseCalculator.SYNODIC_MONTH_DAYS)
            assertNotNull(phase.phaseName)
            assertNotNull(phase.emoji)
            assertTrue("Accessible description must not be empty", phase.accessibleDescription.isNotEmpty())
        }
    }

    @Test
    fun testTimeZoneAwareDateCalculation() {
        val date = LocalDate.of(2024, 7, 21) // Full moon near July 21, 2024
        val phaseUtc = MoonPhaseCalculator.calculateMoonPhaseForDate(date, TimeZone.getTimeZone("UTC"))
        val phaseTokyo = MoonPhaseCalculator.calculateMoonPhaseForDate(date, TimeZone.getTimeZone("Asia/Tokyo"))

        assertNotNull(phaseUtc)
        assertNotNull(phaseTokyo)
        // Both evaluate noon on July 21 in their respective timezones
        assertTrue(phaseUtc.illuminationPercent in 0..100)
        assertTrue(phaseTokyo.illuminationPercent in 0..100)
    }

    @Test
    fun testZonedDateTimeCalculation() {
        val zdt = ZonedDateTime.of(2024, 4, 8, 18, 20, 0, 0, ZoneId.of("UTC")) // Eclipse / New Moon April 8 2024
        val phase = MoonPhaseCalculator.calculateMoonPhase(zdt)

        assertEquals(MoonPhaseType.NEW_MOON, phase.phaseType)
        assertTrue(phase.illuminationPercent <= 5)
    }
}
