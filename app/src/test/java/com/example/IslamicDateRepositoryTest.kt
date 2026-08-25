package com.example

import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.repository.IslamicDateRepository
import com.example.data.util.HijriCalc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
import java.util.SimpleTimeZone
import java.util.TimeZone

class IslamicDateRepositoryTest {

    private fun parseTimestamp(dateStr: String, timeZone: TimeZone): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            this.timeZone = timeZone
        }
        return sdf.parse(dateStr)!!.time
    }

    @Test
    fun testIndiaConventionOnAugust24Year2026BeforeAndAfterSunset() {
        val repo = IslamicDateRepository()
        // Mumbai timezone is UTC+5.5
        val mumbai = CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI)
        repo.updateLocation(mumbai)
        repo.updateCalcMethod(CalcMethod.KARACHI)
        repo.updateHijriMethod(HijriDateMethod.REGIONAL_INDIA)

        val tz = repo.getTimeZone()

        // 24 August 2026 at 12:00:00 (Noon, before Maghrib)
        val noonAug24 = parseTimestamp("2026-08-24 12:00:00", tz)
        val stateNoon = repo.computeState(noonAug24)

        assertEquals(LocalDate.of(2026, 8, 24), stateNoon.gregorianDate)
        assertEquals(10, stateNoon.hijriDate.day)
        assertEquals("Rabi' al-Awwal", stateNoon.hijriDate.monthName)
        assertEquals(1448, stateNoon.hijriDate.year)
        assertFalse(stateNoon.isAfterMaghrib)

        // Sunset on 24 Aug 2026 in Mumbai is around 19:00 (7:00 PM)
        val sunsetMillis = repo.getCurrentSunset(noonAug24)
        assertNotNull(sunsetMillis)

        // 24 August 2026 at 20:00:00 (After Maghrib)
        val nightAug24 = parseTimestamp("2026-08-24 20:00:00", tz)
        val stateNight = repo.computeState(nightAug24)

        assertEquals(LocalDate.of(2026, 8, 24), stateNight.gregorianDate)
        assertEquals(11, stateNight.hijriDate.day)
        assertEquals("Rabi' al-Awwal", stateNight.hijriDate.monthName)
        assertEquals(1448, stateNight.hijriDate.year)
        assertTrue(stateNight.isAfterMaghrib)
    }

    @Test
    fun testMidnightRolloverDoesNotIncrementHijriDate() {
        val repo = IslamicDateRepository()
        val mumbai = CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI)
        repo.updateLocation(mumbai)
        repo.updateCalcMethod(CalcMethod.KARACHI)
        repo.updateHijriMethod(HijriDateMethod.REGIONAL_INDIA)

        val tz = repo.getTimeZone()

        // 24 August 2026 at 23:59:50 (Late night, after Maghrib)
        val lateNightAug24 = parseTimestamp("2026-08-24 23:59:50", tz)
        val stateBeforeMidnight = repo.computeState(lateNightAug24)

        assertEquals(LocalDate.of(2026, 8, 24), stateBeforeMidnight.gregorianDate)
        assertEquals(11, stateBeforeMidnight.hijriDate.day)
        assertEquals("Rabi' al-Awwal", stateBeforeMidnight.hijriDate.monthName)
        assertTrue(stateBeforeMidnight.isAfterMaghrib)

        // 25 August 2026 at 00:00:10 (Just after midnight)
        val earlyMorningAug25 = parseTimestamp("2026-08-25 00:00:10", tz)
        val stateAfterMidnight = repo.computeState(earlyMorningAug25)

        // Gregorian flips from 24 to 25
        assertEquals(LocalDate.of(2026, 8, 25), stateAfterMidnight.gregorianDate)
        // Hijri date MUST remain 11 Rabi' al-Awwal (it DOES NOT jump at midnight!)
        assertEquals(11, stateAfterMidnight.hijriDate.day)
        assertEquals("Rabi' al-Awwal", stateAfterMidnight.hijriDate.monthName)
        assertFalse(stateAfterMidnight.isAfterMaghrib)

        // 25 August 2026 at 12:00:00 (Noon)
        val noonAug25 = parseTimestamp("2026-08-25 12:00:00", tz)
        val stateNoonAug25 = repo.computeState(noonAug25)
        assertEquals(LocalDate.of(2026, 8, 25), stateNoonAug25.gregorianDate)
        assertEquals(11, stateNoonAug25.hijriDate.day)

        // 25 August 2026 at 20:00:00 (After sunset on 25 Aug)
        val nightAug25 = parseTimestamp("2026-08-25 20:00:00", tz)
        val stateNightAug25 = repo.computeState(nightAug25)
        assertEquals(LocalDate.of(2026, 8, 25), stateNightAug25.gregorianDate)
        assertEquals(12, stateNightAug25.hijriDate.day)
        assertTrue(stateNightAug25.isAfterMaghrib)
    }

    @Test
    fun testLocationAndTimezoneChange() {
        val repo = IslamicDateRepository()
        
        // Setup London (UTC+0 / BST)
        val london = CityLocation("London", "UK", 51.5074, -0.1278, 0.0, CalcMethod.MWL)
        repo.updateLocation(london)
        repo.updateCalcMethod(CalcMethod.MWL)
        repo.updateHijriMethod(HijriDateMethod.GLOBAL_ASTRONOMICAL)

        val tz = repo.getTimeZone()
        val timestamp = parseTimestamp("2026-08-24 14:00:00", tz)
        val state = repo.computeState(timestamp)

        assertEquals("London, UK", state.cityName)
        assertEquals(LocalDate.of(2026, 8, 24), state.gregorianDate)
        assertNotNull(state.sunsetTimeMillis)
    }

    @Test
    fun testPhase2OfflineCacheAndSunsetRollover() {
        HijriCalc.clearCacheForTesting(null)

        // 1. Manually populate cache with a verified sync entry for 24 Aug 2026
        val recordAug24 = com.example.data.util.HijriCalc.HijriCacheRecord(
            gregorianDate = "2026-08-24",
            day = 10,
            monthNumber = 3,
            monthName = "Rabi' al-Awwal",
            monthArabic = "ربيع الأوّل",
            year = 1448,
            method = HijriDateMethod.REGIONAL_INDIA.name,
            syncTimestampMillis = 1756000000000L,
            cityName = "Mumbai, India",
            latitude = 19.0760,
            longitude = 72.8777
        )
        HijriCalc.saveCacheRecord(null, recordAug24)

        val repo = IslamicDateRepository()
        val mumbai = CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI)
        repo.updateLocation(mumbai)
        repo.updateHijriMethod(HijriDateMethod.REGIONAL_INDIA)

        val tz = repo.getTimeZone()

        // 2. Offline daytime query on 24 Aug 2026
        val noonAug24 = parseTimestamp("2026-08-24 12:00:00", tz)
        val stateNoon = repo.computeState(noonAug24)
        assertEquals(10, stateNoon.hijriDate.day)
        assertEquals("Rabi' al-Awwal", stateNoon.hijriDate.monthName)
        assertEquals(1448, stateNoon.hijriDate.year)
        assertTrue(stateNoon.isCachedOffline)

        // 3. Offline sunset query on 24 Aug 2026 (after Maghrib)
        val nightAug24 = parseTimestamp("2026-08-24 20:30:00", tz)
        val stateNight = repo.computeState(nightAug24)
        // Day advances to 11 Rabi' al-Awwal seamlessly from cached baseline + sunset rule
        assertEquals(11, stateNight.hijriDate.day)
        assertEquals("Rabi' al-Awwal", stateNight.hijriDate.monthName)
        assertTrue(stateNight.isAfterMaghrib)

        // 4. Offline next day after midnight on 25 Aug 2026 (before Maghrib)
        val morningAug25 = parseTimestamp("2026-08-25 08:00:00", tz)
        val stateMorningAug25 = repo.computeState(morningAug25)
        assertEquals(11, stateMorningAug25.hijriDate.day)
        assertEquals(LocalDate.of(2026, 8, 25), stateMorningAug25.gregorianDate)

        // 5. Offline next day sunset query on 25 Aug 2026 (after Maghrib)
        val nightAug25 = parseTimestamp("2026-08-25 20:30:00", tz)
        val stateNightAug25 = repo.computeState(nightAug25)
        assertEquals(12, stateNightAug25.hijriDate.day)
        assertEquals(LocalDate.of(2026, 8, 25), stateNightAug25.gregorianDate)
    }

    @Test
    fun testCacheValidationRejectsMalformedRecords() {
        // Invalid month 15
        val badJsonMonth = """{"gregorianDate":"2026-08-24","day":10,"monthNumber":15,"year":1448}"""
        val parsedBadMonth = com.example.data.util.HijriCalc.HijriCacheRecord.fromJsonString(badJsonMonth)
        org.junit.Assert.assertNull(parsedBadMonth)

        // Invalid day 35
        val badJsonDay = """{"gregorianDate":"2026-08-24","day":35,"monthNumber":3,"year":1448}"""
        val parsedBadDay = com.example.data.util.HijriCalc.HijriCacheRecord.fromJsonString(badJsonDay)
        org.junit.Assert.assertNull(parsedBadDay)

        // Valid record
        val goodJson = """{"gregorianDate":"2026-08-24","day":10,"monthNumber":3,"monthName":"Rabi' al-Awwal","monthArabic":"ربيع الأوّل","year":1448,"method":"REGIONAL_INDIA","syncTimestampMillis":12345}"""
        val parsedGood = com.example.data.util.HijriCalc.HijriCacheRecord.fromJsonString(goodJson)
        assertNotNull(parsedGood)
        assertEquals(10, parsedGood?.day)
        assertEquals(3, parsedGood?.monthNumber)
        assertEquals(1448, parsedGood?.year)
    }

    @Test
    fun testPhase3BoundaryCalculationAndTransitions() {
        val repo = IslamicDateRepository()
        val mumbai = CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI)
        repo.updateLocation(mumbai)
        repo.updateHijriMethod(HijriDateMethod.REGIONAL_INDIA)
        val tz = repo.getTimeZone()

        // 1. Before Maghrib at 16:00
        val beforeMaghrib = parseTimestamp("2026-08-24 16:00:00", tz)
        val stateBefore = repo.computeState(beforeMaghrib)
        assertFalse(stateBefore.isAfterMaghrib)
        assertNotNull(stateBefore.sunsetTimeMillis)

        // Next boundary must be today's sunset
        val nextBoundary1 = repo.calculateNextBoundaryMillis(beforeMaghrib)
        assertEquals(stateBefore.sunsetTimeMillis, nextBoundary1)

        // 2. Exactly at Maghrib
        val atMaghrib = stateBefore.sunsetTimeMillis!!
        val stateAt = repo.computeState(atMaghrib)
        assertTrue(stateAt.isAfterMaghrib)

        // 3. After Maghrib at 21:00
        val afterMaghrib = parseTimestamp("2026-08-24 21:00:00", tz)
        val stateAfter = repo.computeState(afterMaghrib)
        assertTrue(stateAfter.isAfterMaghrib)

        // Next boundary after Maghrib must be midnight 00:00:00 of 25 Aug 2026
        val nextBoundary2 = repo.calculateNextBoundaryMillis(afterMaghrib)
        val expectedMidnight = parseTimestamp("2026-08-25 00:00:00", tz)
        assertEquals(expectedMidnight, nextBoundary2)

        // 4. App Resume simulation across backgrounding window
        // Backgrounded at 17:30 (before sunset), resumed at 20:00 (after sunset)
        val backgroundTime = parseTimestamp("2026-08-24 17:30:00", tz)
        val resumeTime = parseTimestamp("2026-08-24 20:00:00", tz)
        val stateBackground = repo.computeState(backgroundTime)
        val stateResumed = repo.computeState(resumeTime)
        assertFalse(stateBackground.isAfterMaghrib)
        assertTrue(stateResumed.isAfterMaghrib)
        assertEquals(stateBackground.hijriDate.day + 1, stateResumed.hijriDate.day)

        // 5. Location change (Travel from Mumbai to London)
        val london = CityLocation("London", "United Kingdom", 51.5074, -0.1278, 1.0, CalcMethod.MWL)
        repo.updateLocation(london)
        val londonTz = repo.getTimeZone()
        val londonTime = parseTimestamp("2026-08-24 12:00:00", londonTz)
        val londonState = repo.computeState(londonTime)
        assertEquals("London, United Kingdom", londonState.cityName)
        assertNotNull(londonState.sunsetTimeMillis)

        // Rescheduled next boundary belongs to London sunset
        val nextLondonBoundary = repo.calculateNextBoundaryMillis(londonTime)
        assertEquals(londonState.sunsetTimeMillis, nextLondonBoundary)
    }

    @Test
    fun testPhase4HomeTodayCardDateDisplay() {
        val repo = IslamicDateRepository()
        val mumbai = CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI)
        repo.updateLocation(mumbai)
        repo.updateCalcMethod(CalcMethod.KARACHI)
        repo.updateHijriMethod(HijriDateMethod.REGIONAL_INDIA)
        val tz = repo.getTimeZone()

        // 1. Before Sunset on 24 August 2026 (e.g. 14:00)
        val beforeSunsetTime = parseTimestamp("2026-08-24 14:00:00", tz)
        val stateBeforeSunset = repo.computeState(beforeSunsetTime)

        assertEquals("Monday, 24 August 2026", stateBeforeSunset.gregorianDateFormatted)
        assertEquals("10 Rabi' al-Awwal 1448 AH", stateBeforeSunset.hijriDateFormatted)
        assertFalse(stateBeforeSunset.isAfterMaghrib)

        // 2. After Sunset / Maghrib on 24 August 2026 (e.g. 20:30)
        val afterSunsetTime = parseTimestamp("2026-08-24 20:30:00", tz)
        val stateAfterSunset = repo.computeState(afterSunsetTime)

        // Gregorian remains Monday, 24 August 2026 until midnight
        assertEquals("Monday, 24 August 2026", stateAfterSunset.gregorianDateFormatted)
        // Hijri date automatically advances to 11 Rabi' al-Awwal 1448 AH
        assertEquals("11 Rabi' al-Awwal 1448 AH", stateAfterSunset.hijriDateFormatted)
        // isAfterMaghrib flag is true -> triggers subtle "After Maghrib" indicator
        assertTrue(stateAfterSunset.isAfterMaghrib)

        // 3. After Midnight on 25 August 2026 (e.g. 00:15)
        val afterMidnightTime = parseTimestamp("2026-08-25 00:15:00", tz)
        val stateAfterMidnight = repo.computeState(afterMidnightTime)

        // Gregorian advances to Tuesday, 25 August 2026
        assertEquals("Tuesday, 25 August 2026", stateAfterMidnight.gregorianDateFormatted)
        // Hijri date remains 11 Rabi' al-Awwal 1448 AH (civil day mapping)
        assertEquals("11 Rabi' al-Awwal 1448 AH", stateAfterMidnight.hijriDateFormatted)
        // isAfterMaghrib flag resets to false
        assertFalse(stateAfterMidnight.isAfterMaghrib)

        // 4. After Next Sunset on 25 August 2026 (e.g. 20:00)
        val nextSunsetTime = parseTimestamp("2026-08-25 20:00:00", tz)
        val stateNextSunset = repo.computeState(nextSunsetTime)

        assertEquals("Tuesday, 25 August 2026", stateNextSunset.gregorianDateFormatted)
        assertEquals("12 Rabi' al-Awwal 1448 AH", stateNextSunset.hijriDateFormatted)
        assertTrue(stateNextSunset.isAfterMaghrib)
    }

    @Test
    fun testPhase5AHijriCacheEntityAndOfflineReconstruction() {
        val entity = com.example.data.db.HijriCacheEntity(
            gregorianDate = "2026-08-24",
            hijriDay = 10,
            hijriMonth = 3,
            hijriMonthName = "Rabi' al-Awwal",
            hijriMonthArabic = "ربيع الأوّل",
            hijriYear = 1448,
            method = "REGIONAL_INDIA",
            cityName = "Mumbai, India",
            latitude = 19.0760,
            longitude = 72.8777,
            timezoneId = "GMT+05:30",
            syncTimestampMillis = 1756000000000L
        )

        assertEquals("2026-08-24", entity.gregorianDate)
        assertEquals(10, entity.hijriDay)
        assertEquals(3, entity.hijriMonth)
        assertEquals(1448, entity.hijriYear)
        assertEquals("REGIONAL_INDIA", entity.method)

        // Verify accurate offline reconstruction 3 days in future from cached baseline
        val cacheRecord = com.example.data.util.HijriCalc.HijriCacheRecord(
            gregorianDate = entity.gregorianDate,
            day = entity.hijriDay,
            monthNumber = entity.hijriMonth,
            monthName = entity.hijriMonthName,
            monthArabic = entity.hijriMonthArabic,
            year = entity.hijriYear,
            method = entity.method,
            syncTimestampMillis = entity.syncTimestampMillis,
            cityName = entity.cityName,
            latitude = entity.latitude,
            longitude = entity.longitude
        )

        val targetDate = LocalDate.of(2026, 8, 27) // +3 days
        val (reconstructedDay, reconstructedMonth, reconstructedYear) =
            HijriCalc.reconstructFromCachedRecord(cacheRecord, targetDate)

        assertEquals(13, reconstructedDay)
        assertEquals(3, reconstructedMonth)
        assertEquals(1448, reconstructedYear)
    }
}


