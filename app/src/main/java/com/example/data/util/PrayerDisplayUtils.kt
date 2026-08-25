package com.example.data.util

import com.example.data.model.PrayerName
import java.time.DayOfWeek
import java.time.LocalDate

object PrayerDisplayUtils {

    fun isFriday(date: LocalDate = LocalDate.now()): Boolean {
        return date.dayOfWeek == DayOfWeek.FRIDAY
    }

    fun isFriday(dateStr: String?): Boolean {
        if (dateStr.isNullOrBlank()) return isFriday(LocalDate.now())
        return try {
            val date = LocalDate.parse(dateStr)
            date.dayOfWeek == DayOfWeek.FRIDAY
        } catch (_: Exception) {
            isFriday(LocalDate.now())
        }
    }

    fun getPrayerDisplayName(prayerName: PrayerName, isFriday: Boolean = false): String {
        if (prayerName == PrayerName.DHUHR && isFriday) {
            return "Jummah"
        }
        return prayerName.displayName
    }

    fun getPrayerDisplayName(prayerName: PrayerName, dateStr: String?): String {
        return getPrayerDisplayName(prayerName, isFriday(dateStr))
    }

    fun getPrayerDisplayName(prayerName: PrayerName, date: LocalDate): String {
        return getPrayerDisplayName(prayerName, isFriday(date))
    }

    fun getPrayerArabicName(prayerName: PrayerName, isFriday: Boolean = false): String {
        if (prayerName == PrayerName.DHUHR && isFriday) {
            return "الجمعة"
        }
        return prayerName.arabicName
    }

    fun getPrayerArabicName(prayerName: PrayerName, dateStr: String?): String {
        return getPrayerArabicName(prayerName, isFriday(dateStr))
    }

    fun getPrayerArabicName(prayerName: PrayerName, date: LocalDate): String {
        return getPrayerArabicName(prayerName, isFriday(date))
    }

    fun getPrayerPoeticSubtext(prayerName: PrayerName, isFriday: Boolean = false): String {
        if (prayerName == PrayerName.DHUHR && isFriday) {
            return "The best day of the week; the hour of congregation"
        }
        return when (prayerName) {
            PrayerName.FAJR -> "First light pierces the quiet dark"
            PrayerName.SUNRISE -> "Dawn opens the sky with morning light"
            PrayerName.DHUHR -> "The sun pauses at its highest peak"
            PrayerName.ASR -> "Shadows begin to stretch and lengthen"
            PrayerName.MAGHRIB -> "Day yields to twilight's glow"
            PrayerName.ISHA -> "Night falls into peaceful stillness"
        }
    }

    fun getPrayerPoeticSubtext(prayerName: PrayerName, dateStr: String?): String {
        return getPrayerPoeticSubtext(prayerName, isFriday(dateStr))
    }

    fun getPrayerPoeticSubtext(prayerName: PrayerName, date: LocalDate): String {
        return getPrayerPoeticSubtext(prayerName, isFriday(date))
    }

    fun parsePrayerName(prayerNameStr: String?): PrayerName {
        if (prayerNameStr.isNullOrBlank()) return PrayerName.FAJR
        if (prayerNameStr.equals("Jummah", ignoreCase = true) || prayerNameStr.equals("Jumu'ah", ignoreCase = true) || prayerNameStr.equals("Jum'ah", ignoreCase = true)) {
            return PrayerName.DHUHR
        }
        return PrayerName.entries.find {
            it.displayName.equals(prayerNameStr, ignoreCase = true) ||
            it.name.equals(prayerNameStr, ignoreCase = true) ||
            it.id.equals(prayerNameStr, ignoreCase = true)
        } ?: PrayerName.FAJR
    }

    /**
     * Standard human-readable Day / Month / Year formatting
     * e.g., "Thursday, 13 August 2026"
     */
    fun formatFullDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val date = LocalDate.parse(dateStr)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.ENGLISH)
            date.format(formatter)
        } catch (_: Exception) {
            dateStr
        }
    }

    /**
     * Standard human-readable Short Day / Month / Year formatting
     * e.g., "13 Aug 2026"
     */
    fun formatShortDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val date = LocalDate.parse(dateStr)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH)
            date.format(formatter)
        } catch (_: Exception) {
            dateStr
        }
    }

    /**
     * Standard human-readable Medium Day / Month / Year formatting
     * e.g., "13 August 2026"
     */
    fun formatMediumDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val date = LocalDate.parse(dateStr)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH)
            date.format(formatter)
        } catch (_: Exception) {
            dateStr
        }
    }
}

