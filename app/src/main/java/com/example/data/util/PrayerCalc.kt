package com.example.data.util

import com.example.data.model.CalcMethod
import com.example.data.model.Madhab
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object PrayerCalc {

    var activeTimeZone: TimeZone = TimeZone.getDefault()

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        method: CalcMethod = CalcMethod.MWL,
        madhab: Madhab = Madhab.STANDARD,
        timeZoneOffsetHours: Double = Math.round(longitude / 15.0).toDouble(),
        is24Hour: Boolean = false,
        updateActiveTimeZone: Boolean = true
    ): List<PrayerItem> {
        val offsetMillis = (timeZoneOffsetHours * 3600000).toInt()
        val offsetHours = offsetMillis / 3600000
        val offsetMins = Math.abs((offsetMillis / 60000) % 60)
        val tzId = String.format(java.util.Locale.US, "GMT%+03d:%02d", offsetHours, offsetMins)
        val cityTz = java.util.SimpleTimeZone(offsetMillis, tzId)
        if (updateActiveTimeZone) {
            activeTimeZone = cityTz
        }
        val calendar = Calendar.getInstance(cityTz).apply { time = date }
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        // Astronomical calculations
        val d = julianDate(year, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)) - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val dec = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqT = q / 15.0 - fixHour(ra)

        // Dhuhr (solar noon)
        val dhuhr = fixHour(12.0 + timeZoneOffsetHours - (longitude / 15.0) - eqT)

        // Sunrise & Sunset
        val alphaSunrise = 0.833
        val tSunrise = hourAngle(-alphaSunrise, latitude, dec)
        val sunrise = fixHour(dhuhr - (tSunrise / 15.0))
        val sunset = fixHour(dhuhr + (tSunrise / 15.0))

        // Fajr (dawn - strictly pre-dawn AM)
        val tFajr = hourAngle(method.fajrAngle, latitude, dec)
        var fajr = fixHour(dhuhr - (tFajr / 15.0))
        if (fajr >= 12.0) {
            fajr %= 12.0
        }

        // Asr
        val shadowFactor = if (madhab == Madhab.HANAFI) 2.0 else 1.0
        val phi = Math.toRadians(latitude)
        val delta = Math.toRadians(dec)
        val angleAsr = Math.toDegrees(atan(1.0 / (shadowFactor + tan(abs(phi - delta)))))
        val tAsr = hourAngleDegrees(angleAsr, latitude, dec)
        val asr = fixHour(dhuhr + (tAsr / 15.0))

        // Maghrib
        val maghrib = sunset

        // Isha
        val isha = if (method == CalcMethod.UMM_AL_QURA) {
            fixHour(maghrib + 1.5) // 90 minutes after Maghrib
        } else {
            val tIsha = hourAngle(method.ishaAngle, latitude, dec)
            fixHour(dhuhr + (tIsha / 15.0))
        }

        // Convert double hours to Epoch Millis for today
        val baseCal = Calendar.getInstance(cityTz).apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nowMillis = System.currentTimeMillis()

        val rawTimes = mapOf(
            PrayerName.FAJR to fajr,
            PrayerName.SUNRISE to sunrise,
            PrayerName.DHUHR to dhuhr,
            PrayerName.ASR to asr,
            PrayerName.MAGHRIB to maghrib,
            PrayerName.ISHA to isha
        )

        var foundNext = false
        val items = mutableListOf<PrayerItem>()

        for ((pName, timeHours) in rawTimes) {
            val pCal = baseCal.clone() as Calendar
            val totalMinutes = (timeHours * 60).toInt()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            pCal.set(Calendar.HOUR_OF_DAY, hours)
            pCal.set(Calendar.MINUTE, minutes)
            pCal.set(Calendar.SECOND, 0)
            pCal.set(Calendar.MILLISECOND, 0)
            val pMillis = pCal.timeInMillis

            val isPassed = nowMillis > pMillis
            val isNext = !isPassed && !foundNext && pName != PrayerName.SUNRISE
            if (isNext) foundNext = true

            items.add(
                PrayerItem(
                    name = pName,
                    timeFormatted = formatPrayerTime(pName, timeHours, is24Hour),
                    timeMillis = pMillis,
                    isNext = isNext,
                    isPassed = isPassed
                )
            )
        }

        return items
    }

    private fun hourAngle(angleDegrees: Double, lat: Double, dec: Double): Double {
        val phi = Math.toRadians(lat)
        val delta = Math.toRadians(dec)
        val alpha = Math.toRadians(angleDegrees)
        val cosH = (-sin(alpha) - sin(phi) * sin(delta)) / (cos(phi) * cos(delta))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH))
    }

    private fun hourAngleDegrees(altitudeAngle: Double, lat: Double, dec: Double): Double {
        val phi = Math.toRadians(lat)
        val delta = Math.toRadians(dec)
        val alt = Math.toRadians(altitudeAngle)
        val cosH = (sin(alt) - sin(phi) * sin(delta)) / (cos(phi) * cos(delta))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH))
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * Math.floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(a: Double): Double {
        var hour = a - 24.0 * Math.floor(a / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    fun formatPrayerTime(prayerName: PrayerName, decimalHours: Double, is24Hour: Boolean = false): String {
        val totalMinutes = (decimalHours * 60).toInt()
        var hour24 = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60

        if (is24Hour) {
            return String.format(Locale.US, "%02d:%02d", hour24, minutes)
        }

        // Strict astronomical check for Fajr and Sunrise: pre-noon morning AM hours
        if (prayerName == PrayerName.FAJR || prayerName == PrayerName.SUNRISE) {
            if (hour24 >= 12) hour24 %= 12
        }

        val displayHour = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val amPm = if (prayerName == PrayerName.FAJR || prayerName == PrayerName.SUNRISE) {
            "AM"
        } else {
            if (hour24 < 12) "AM" else "PM"
        }

        return String.format(Locale.US, "%02d:%02d %s", displayHour, minutes, amPm)
    }

    fun formatTime(timeMillis: Long, is24Hour: Boolean = false, timeZone: TimeZone? = activeTimeZone): String {
        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        if (timeZone != null) {
            sdf.timeZone = timeZone
        }
        return sdf.format(Date(timeMillis))
    }

    fun calculateTahajjudWindow(
        fardPrayers: List<PrayerItem>,
        is24Hour: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): TahajjudWindow? {
        val isha = fardPrayers.find { it.name == PrayerName.ISHA } ?: return null
        val fajr = fardPrayers.find { it.name == PrayerName.FAJR } ?: return null

        // Handle overnight timeline: Isha -> next Fajr
        val (nightIshaMillis, nightFajrMillis) = if (nowMillis < fajr.timeMillis) {
            // Early morning before Fajr (between 00:00 midnight and Fajr): night belongs to yesterday Isha -> today Fajr
            val adjustedIsha = if (isha.timeMillis >= fajr.timeMillis) {
                isha.timeMillis - 24 * 3600 * 1000L
            } else {
                isha.timeMillis
            }
            Pair(adjustedIsha, fajr.timeMillis)
        } else {
            // Daytime or evening: night belongs to today Isha -> tomorrow Fajr
            val adjustedFajr = if (fajr.timeMillis <= isha.timeMillis) {
                fajr.timeMillis + 24 * 3600 * 1000L
            } else {
                fajr.timeMillis
            }
            Pair(isha.timeMillis, adjustedFajr)
        }

        val nightDuration = (nightFajrMillis - nightIshaMillis).coerceAtLeast(1000L)
        val tahajjudStartMillis = nightFajrMillis - (nightDuration / 3)
        val tahajjudEndMillis = nightFajrMillis

        val startStr = formatTime(tahajjudStartMillis, is24Hour, activeTimeZone)
        val fajrStr = formatTime(nightFajrMillis, is24Hour, activeTimeZone)
        val ishaStr = formatTime(nightIshaMillis, is24Hour, activeTimeZone)
        val windowStr = "$startStr – $fajrStr"
        val isCurrent = nowMillis >= tahajjudStartMillis && nowMillis < nightFajrMillis

        return TahajjudWindow(
            startMillis = tahajjudStartMillis,
            endMillis = tahajjudEndMillis,
            startFormatted = startStr,
            endFormatted = fajrStr,
            windowFormatted = windowStr,
            isCurrent = isCurrent,
            ishaMillis = nightIshaMillis,
            fajrMillis = nightFajrMillis,
            ishaFormatted = ishaStr
        )
    }
}

data class TahajjudWindow(
    val startMillis: Long,
    val endMillis: Long,
    val startFormatted: String,
    val endFormatted: String,
    val windowFormatted: String,
    val isCurrent: Boolean,
    val ishaMillis: Long = 0L,
    val fajrMillis: Long = endMillis,
    val ishaFormatted: String = ""
)

