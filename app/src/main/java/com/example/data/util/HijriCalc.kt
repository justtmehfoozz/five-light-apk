package com.example.data.util

import com.example.data.model.HijriDate
import com.example.data.model.IslamicEvent
import java.util.Calendar
import java.util.Date
import kotlin.math.floor

object HijriCalc {

    private val MONTH_NAMES_EN = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah"
    )

    private val MONTH_NAMES_AR = arrayOf(
        "المحرّم", "صفر", "ربيع الأوّل", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    val KEY_ISLAMIC_EVENTS = listOf(
        IslamicEvent("Islamic New Year", "رأس السنة الهجرية", 1, "Muharram", "Beginning of the Hijri year 1448 AH."),
        IslamicEvent("Day of Ashura", "يوم عاشوراء", 10, "Muharram", "Day of fasting and commemoration."),
        IslamicEvent("Mawlid al-Nabi", "مولد النبي", 12, "Rabi' al-Awwal", "Commemoration of the birth of Prophet Muhammad (PBUH)."),
        IslamicEvent("Isra and Mi'raj", "الإسراء والمعراج", 27, "Rajab", "The miraculous night journey and ascension."),
        IslamicEvent("Mid-Sha'ban", "ليلة النصف من شعبان", 15, "Sha'ban", "Night of worship and reflection prior to Ramadan."),
        IslamicEvent("Ramadan Begins", "بداية شهر رمضان", 1, "Ramadan", "The holy month of fasting, prayer, and reflection."),
        IslamicEvent("Laylat al-Qadr", "ليلة القدر", 27, "Ramadan", "The Night of Power, better than a thousand months."),
        IslamicEvent("Eid al-Fitr", "عيد الفطر المبارك", 1, "Shawwal", "Festival breaking the fast after Ramadan."),
        IslamicEvent("Day of Arafah", "يوم عرفة", 9, "Dhu al-Hijjah", "The pinnacle day of the Hajj pilgrimage."),
        IslamicEvent("Eid al-Adha", "عيد الأضحى المبارك", 10, "Dhu al-Hijjah", "Festival of Sacrifice honoring Prophet Ibrahim's devotion.")
    )

    fun getHijriDate(date: Date = Date()): HijriDate {
        val cal = Calendar.getInstance().apply { time = date }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        // Julian Day Number calculation
        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jdn = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        // Conversion from JDN to Tabular Hijri
        val l = jdn - 1948440 + 10632
        val n = floor((l - 1) / 10631.0)
        val l1 = l - 10631 * n + 354
        val j = (floor((10985 - l1) / 5316.0)) * (floor((50 * l1) / 17719.0)) + (floor(l1 / 5670.0)) * (floor((43 * l1) / 15238.0))
        val l2 = l1 - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) - (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val hMonth = floor((24 * l2) / 709.0).toInt()
        val hDay = (l2 - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val clampedMonth = hMonth.coerceIn(1, 12)
        val monthIdx = clampedMonth - 1

        return HijriDate(
            day = hDay.coerceIn(1, 30),
            monthName = MONTH_NAMES_EN[monthIdx],
            monthArabic = MONTH_NAMES_AR[monthIdx],
            monthNumber = clampedMonth,
            year = hYear
        )
    }

    fun formatHijriString(hijriDate: HijriDate): String {
        return "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} AH"
    }

    fun formatHijriArabicString(hijriDate: HijriDate): String {
        return "${hijriDate.day} ${hijriDate.monthArabic} ${hijriDate.year} هـ"
    }
}
