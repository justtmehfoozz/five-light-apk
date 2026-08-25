package com.example.data.util

import com.example.data.model.MoonPhase
import com.example.data.model.MoonPhaseType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Astronomical Lunar Phase Engine.
 *
 * Implements standard Meeus/Chapront astronomical algorithms for calculating:
 * - Julian Ephemeris Date (JD)
 * - Moon's mean elongation (D)
 * - Sun's and Moon's mean anomalies (M, M')
 * - Phase angle (i) and exact geometric illuminated fraction (k)
 * - Synodic cycle age and 8-phase categorization
 *
 * Distinct and independent from the Hijri calendar month/day methodology.
 */
object MoonPhaseCalculator {

    // Mean synodic month in days (New Moon to New Moon)
    const val SYNODIC_MONTH_DAYS = 29.530588853

    // Known reference New Moon epoch: Jan 6, 2000 18:14 UTC (JD 2451549.759722)
    const val REFERENCE_NEW_MOON_JD = 2451549.759722

    /**
     * Calculates the astronomical moon phase for an epoch timestamp in milliseconds.
     */
    fun calculateMoonPhase(timestampMillis: Long = System.currentTimeMillis()): MoonPhase {
        // Convert timestamp to Julian Date
        // Unix Epoch (1970-01-01 00:00:00 UTC) = JD 2440587.5
        val jd = 2440587.5 + (timestampMillis.toDouble() / 86400000.0)
        return calculateFromJulianDate(jd)
    }

    /**
     * Calculates the astronomical moon phase for a specific LocalDate in a given TimeZone.
     * Evaluates at local noon (12:00 PM) for daily stability.
     */
    fun calculateMoonPhaseForDate(
        date: LocalDate,
        timeZone: TimeZone = TimeZone.getDefault()
    ): MoonPhase {
        val zoneId = try { timeZone.toZoneId() } catch (_: Exception) { ZoneId.systemDefault() }
        val noonMillis = date.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        return calculateMoonPhase(noonMillis)
    }

    /**
     * Calculates the astronomical moon phase for a ZonedDateTime.
     */
    fun calculateMoonPhase(dateTime: ZonedDateTime): MoonPhase {
        return calculateMoonPhase(dateTime.toInstant().toEpochMilli())
    }

    /**
     * Computes the lunar phase parameters from Julian Date using Jean Meeus algorithms.
     */
    fun calculateFromJulianDate(jd: Double): MoonPhase {
        // Julian centuries since J2000.0 (2000-01-01 12:00:00 TT)
        val t = (jd - 2451545.0) / 36525.0

        // Moon's mean elongation D (in degrees)
        var d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t + (t * t * t) / 545868.0 - (t * t * t * t) / 113065000.0
        d = normalizeDegrees(d)

        // Sun's mean anomaly M (in degrees)
        var m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t * t + (t * t * t) / 24490000.0
        m = normalizeDegrees(m)

        // Moon's mean anomaly M' (in degrees)
        var mPrime = 134.9633964 + 477198.8675055 * t + 0.0087414 * t * t + (t * t * t) / 69699.0 - (t * t * t * t) / 14712000.0
        mPrime = normalizeDegrees(mPrime)

        val dRad = Math.toRadians(d)
        val mRad = Math.toRadians(m)
        val mPrimeRad = Math.toRadians(mPrime)

        // Phase angle i (degrees)
        var phaseAngleDeg = 180.0 - d -
                6.289 * sin(mPrimeRad) +
                2.100 * sin(mRad) -
                1.274 * sin(2.0 * dRad - mPrimeRad) -
                0.658 * sin(2.0 * dRad) -
                0.214 * sin(2.0 * mPrimeRad) -
                0.110 * sin(dRad)
        phaseAngleDeg = normalizeDegrees(phaseAngleDeg)

        val iRad = Math.toRadians(phaseAngleDeg)

        // Geometric fraction illuminated k (0.0 to 1.0)
        val illuminationFraction = ((1.0 + cos(iRad)) / 2.0).coerceIn(0.0, 1.0).toFloat()
        val illuminationPercent = (illuminationFraction * 100f).roundToInt()

        // Days since reference New Moon
        val daysSinceNewMoon = jd - REFERENCE_NEW_MOON_JD
        val synodicCycles = daysSinceNewMoon / SYNODIC_MONTH_DAYS
        val phaseProgress = (synodicCycles - floor(synodicCycles)).coerceIn(0.0, 1.0)
        val ageDays = phaseProgress * SYNODIC_MONTH_DAYS

        // Categorize into the 8 phases based on phase progress in synodic cycle
        val phaseType = getPhaseType(phaseProgress)

        val accessibleDescription = "${phaseType.displayName}, approximately $illuminationPercent percent illuminated."

        return MoonPhase(
            phaseType = phaseType,
            phaseName = phaseType.displayName,
            illuminationFraction = illuminationFraction,
            illuminationPercent = illuminationPercent,
            phaseAngle = phaseProgress.toFloat(),
            ageDays = ageDays,
            emoji = phaseType.emoji,
            accessibleDescription = accessibleDescription
        )
    }

    /**
     * Maps the continuous synodic progress [0.0..1.0) into one of the 8 canonical phases.
     */
    fun getPhaseType(progress: Double): MoonPhaseType {
        // 0.00: New Moon -> 0.25: First Quarter -> 0.50: Full Moon -> 0.75: Last Quarter -> 1.00
        val normalized = ((progress % 1.0) + 1.0) % 1.0
        return when {
            normalized < 0.03 || normalized >= 0.97 -> MoonPhaseType.NEW_MOON
            normalized < 0.22 -> MoonPhaseType.WAXING_CRESCENT
            normalized < 0.28 -> MoonPhaseType.FIRST_QUARTER
            normalized < 0.47 -> MoonPhaseType.WAXING_GIBBOUS
            normalized < 0.53 -> MoonPhaseType.FULL_MOON
            normalized < 0.72 -> MoonPhaseType.WANING_GIBBOUS
            normalized < 0.78 -> MoonPhaseType.LAST_QUARTER
            else -> MoonPhaseType.WANING_CRESCENT
        }
    }

    private fun normalizeDegrees(deg: Double): Double {
        var result = deg % 360.0
        if (result < 0.0) {
            result += 360.0
        }
        return result
    }
}
