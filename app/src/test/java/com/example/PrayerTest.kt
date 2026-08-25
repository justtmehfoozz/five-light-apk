package com.example

import org.junit.Test
import java.util.*
import java.text.SimpleDateFormat
import com.example.data.util.PrayerCalc
import com.example.data.model.CalcMethod
import com.example.data.model.Madhab

class PrayerTest {
    @Test
    fun testPrayer() {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val d1 = sdf.parse("2026-08-15")!!
        val p1 = PrayerCalc.calculatePrayerTimes(19.0760, 72.8777, d1, CalcMethod.KARACHI, Madhab.STANDARD, 5.5, false)
        println("TEST_PRAYER_OUTPUT_STD: " + p1.map { it.name.name + " " + it.timeFormatted })
        val p2 = PrayerCalc.calculatePrayerTimes(19.0760, 72.8777, d1, CalcMethod.KARACHI, Madhab.HANAFI, 5.5, false)
        println("TEST_PRAYER_OUTPUT_HAN: " + p2.map { it.name.name + " " + it.timeFormatted })
    }
}
