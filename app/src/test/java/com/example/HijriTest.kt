package com.example

import org.junit.Test
import java.util.*
import java.text.SimpleDateFormat
import com.example.data.util.HijriCalc

class HijriTest {
    @Test
    fun testHijri() {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val d1 = sdf.parse("2026-08-15")!!
        val hd = HijriCalc.getHijriDate(d1)
        println("TEST_HIJRI_OUTPUT: " + HijriCalc.formatHijriString(hd))
    }
}
