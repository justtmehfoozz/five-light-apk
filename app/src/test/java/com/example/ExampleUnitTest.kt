package com.example

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Unit tests for Qibla compass angle, symmetric kite geometry, and cardinal crossing calculations.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun shortestSignedAngle_handlesWrapping() {
    fun shortestSignedAngle(angle: Float): Float {
      return ((angle + 180f) % 360f + 360f) % 360f - 180f
    }

    assertEquals(2f, shortestSignedAngle(2f), 0.001f)
    assertEquals(-2f, shortestSignedAngle(-2f), 0.001f)
    assertEquals(-10f, shortestSignedAngle(350f), 0.001f)
    assertEquals(10f, shortestSignedAngle(-350f), 0.001f)
    assertEquals(-180f, shortestSignedAngle(180f), 0.001f)
    assertEquals(-180f, shortestSignedAngle(-180f), 0.001f)
  }

  @Test
  fun qiblaDirection_acceptanceTests_allMatchPhysicalOrientation() {
    fun calculateDelta(qibla: Float, heading: Float): Float {
      return ((qibla - heading + 180f) % 360f + 360f) % 360f - 180f
    }

    fun getDirectionInstruction(delta: Float, facingThreshold: Float = 4f): String {
      val absDiff = abs(delta)
      return when {
        absDiff <= facingThreshold -> "Facing Qibla"
        delta > 35f -> "Turn Right"
        delta in facingThreshold..35f -> "Turn Slightly Right"
        delta < -35f -> "Turn Left"
        else -> "Turn Slightly Left"
      }
    }

    // TEST 1: Heading = 161°, Qibla = 280° -> Turn Right, delta = +119°
    val delta1 = calculateDelta(280f, 161f)
    assertEquals(119f, delta1, 0.001f)
    assertEquals("Turn Right", getDirectionInstruction(delta1))

    // TEST 2: Heading = 280°, Qibla = 280° -> Facing Qibla, delta = 0°
    val delta2 = calculateDelta(280f, 280f)
    assertEquals(0f, delta2, 0.001f)
    assertEquals("Facing Qibla", getDirectionInstruction(delta2))

    // TEST 3: Heading = 10°, Qibla = 350° -> Turn Left / Turn Slightly Left, delta = -20°
    val delta3 = calculateDelta(350f, 10f)
    assertEquals(-20f, delta3, 0.001f)
    assertTrue(getDirectionInstruction(delta3).startsWith("Turn"))
    assertTrue(getDirectionInstruction(delta3).contains("Left"))

    // TEST 4: Heading = 350°, Qibla = 10° -> Turn Right / Turn Slightly Right, delta = +20°
    val delta4 = calculateDelta(10f, 350f)
    assertEquals(20f, delta4, 0.001f)
    assertTrue(getDirectionInstruction(delta4).startsWith("Turn"))
    assertTrue(getDirectionInstruction(delta4).contains("Right"))

    // TEST 5: Heading = 100°, Qibla = 190° -> Turn Right, delta = +90°
    val delta5 = calculateDelta(190f, 100f)
    assertEquals(90f, delta5, 0.001f)
    assertEquals("Turn Right", getDirectionInstruction(delta5))

    // TEST 6: Heading = 190°, Qibla = 100° -> Turn Left, delta = -90°
    val delta6 = calculateDelta(100f, 190f)
    assertEquals(-90f, delta6, 0.001f)
    assertEquals("Turn Left", getDirectionInstruction(delta6))
  }

  @Test
  fun symmetricKite_geometrySymmetry_isMathematicallyExact() {
    val topTip = Pair(20f, 4f)
    val rightMid = Pair(30f, 26f)
    val bottomNotch = Pair(20f, 22f)
    val leftMid = Pair(10f, 26f)
    val centerX = 20f

    val rightDistance = rightMid.first - centerX
    val leftDistance = centerX - leftMid.first
    assertEquals("Right and left mid points must be equidistant from centerline", rightDistance, leftDistance, 0.0001f)
    assertEquals("Both mid points must share exact Y coordinate", rightMid.second, leftMid.second, 0.0001f)
    assertEquals("Top tip must sit on vertical centerline", centerX, topTip.first, 0.0001f)
    assertEquals("Bottom notch must sit on vertical centerline", centerX, bottomNotch.first, 0.0001f)

    // Verify distance from top tip to left/right wings
    val distTopToRight = sqrt((rightMid.first - topTip.first) * (rightMid.first - topTip.first) + (rightMid.second - topTip.second) * (rightMid.second - topTip.second))
    val distTopToLeft = sqrt((leftMid.first - topTip.first) * (leftMid.first - topTip.first) + (leftMid.second - topTip.second) * (leftMid.second - topTip.second))
    assertEquals("Distances from tip to wings must be equal", distTopToRight, distTopToLeft, 0.0001f)

    // Verify distance from bottom notch to left/right wings
    val distNotchToRight = sqrt((rightMid.first - bottomNotch.first) * (rightMid.first - bottomNotch.first) + (rightMid.second - bottomNotch.second) * (rightMid.second - bottomNotch.second))
    val distNotchToLeft = sqrt((leftMid.first - bottomNotch.first) * (leftMid.first - bottomNotch.first) + (leftMid.second - bottomNotch.second) * (leftMid.second - bottomNotch.second))
    assertEquals("Distances from notch to wings must be equal", distNotchToRight, distNotchToLeft, 0.0001f)
  }

  @Test
  fun symmetricKite_rotationAtAllCardinalAngles_preservesShapeInvariant() {
    val angles = floatArrayOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)
    // Relative to center (20, 20):
    val top = Pair(0f, -16f)
    val right = Pair(10f, 6f)
    val notch = Pair(0f, 2f)
    val left = Pair(-10f, 6f)

    for (deg in angles) {
      val rad = Math.toRadians(deg.toDouble())
      fun rotate(p: Pair<Float, Float>): Pair<Double, Double> {
        val rx = p.first * cos(rad) - p.second * sin(rad)
        val ry = p.first * sin(rad) + p.second * cos(rad)
        return Pair(rx, ry)
      }

      val rTop = rotate(top)
      val rRight = rotate(right)
      val rNotch = rotate(notch)
      val rLeft = rotate(left)

      fun dist(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        return sqrt((p1.first - p2.first) * (p1.first - p2.first) + (p1.second - p2.second) * (p1.second - p2.second))
      }

      // Check lengths stay identical under rotation (isometry)
      assertEquals(sqrt(10.0 * 10.0 + 22.0 * 22.0), dist(rTop, rRight), 0.001)
      assertEquals(sqrt(10.0 * 10.0 + 22.0 * 22.0), dist(rTop, rLeft), 0.001)
      assertEquals(sqrt(10.0 * 10.0 + 4.0 * 4.0), dist(rNotch, rRight), 0.001)
      assertEquals(sqrt(10.0 * 10.0 + 4.0 * 4.0), dist(rNotch, rLeft), 0.001)
    }
  }

  @Test
  fun cardinalCrossing_detection_worksAccurately() {
    fun shortestSignedAngle(angle: Float): Float {
      var result = angle % 360f
      if (result > 180f) result -= 360f
      if (result < -180f) result += 360f
      return result
    }

    val cardinalAngles = intArrayOf(0, 90, 180, 270)
    fun detectCrossing(prev: Float, curr: Float): Int? {
      for (c in cardinalAngles) {
        val prevDiff = shortestSignedAngle(prev - c.toFloat())
        val currDiff = shortestSignedAngle(curr - c.toFloat())
        val crossed = (prevDiff < 0f && currDiff >= 0f) || (prevDiff > 0f && currDiff <= 0f)
        if (crossed && abs(prevDiff) < 16f && abs(currDiff) < 16f) {
          return c
        }
      }
      return null
    }

    // Crossing North (358° to 2°)
    assertEquals(0, detectCrossing(358f, 2f))
    // Crossing East (88° to 92°)
    assertEquals(90, detectCrossing(88f, 92f))
    // Crossing South (178° to 182°)
    assertEquals(180, detectCrossing(178f, 182f))
    // Crossing West (268° to 272°)
    assertEquals(270, detectCrossing(268f, 272f))
    // No crossing
    assertNull(detectCrossing(45f, 50f))
  }

  @Test
  fun islamicVsGregorianDateRollover_acceptanceTests() {
    val tz = java.util.TimeZone.getTimeZone("GMT")
    val cal = java.util.Calendar.getInstance(tz).apply {
      set(java.util.Calendar.YEAR, 2026)
      set(java.util.Calendar.MONTH, java.util.Calendar.AUGUST)
      set(java.util.Calendar.DAY_OF_MONTH, 15)
      set(java.util.Calendar.HOUR_OF_DAY, 18)
      set(java.util.Calendar.MINUTE, 59)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val maghribAug15 = cal.timeInMillis

    cal.set(java.util.Calendar.DAY_OF_MONTH, 16)
    val maghribAug16 = cal.timeInMillis

    fun getGregorianDay(timeMillis: Long): Int {
      val c = java.util.Calendar.getInstance(tz).apply { timeInMillis = timeMillis }
      return c.get(java.util.Calendar.DAY_OF_MONTH)
    }

    fun getHijri(timeMillis: Long, maghribMillis: Long): com.example.data.model.HijriDate {
      val date = java.util.Date(timeMillis)
      return com.example.data.util.HijriCalc.getHijriDate(date = date, timeZone = tz, maghribTimeMillis = maghribMillis)
    }

    // 1. 15 August at 06:58 PM (1 min before Maghrib)
    val t1 = maghribAug15 - 60000L
    assertEquals(15, getGregorianDay(t1))
    val h1 = getHijri(t1, maghribAug15)

    // 2. 15 August at 06:59 PM (At/after Maghrib)
    val t2 = maghribAug15
    assertEquals(15, getGregorianDay(t2))
    val h2 = getHijri(t2, maghribAug15)
    // Maghrib boundary advances the Islamic day
    assertTrue("Hijri day should advance at Maghrib", h2.day != h1.day || h2.monthNumber != h1.monthNumber)

    // 3. 15 August at 11:59 PM (Before midnight)
    cal.set(java.util.Calendar.DAY_OF_MONTH, 15)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
    cal.set(java.util.Calendar.MINUTE, 59)
    val t3 = cal.timeInMillis
    assertEquals(15, getGregorianDay(t3))
    val h3 = getHijri(t3, maghribAug15)
    assertEquals(h2.day, h3.day)
    assertEquals(h2.monthNumber, h3.monthNumber)

    // 4. 16 August at 12:00 AM (Midnight rollover)
    cal.set(java.util.Calendar.DAY_OF_MONTH, 16)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    val t4 = cal.timeInMillis
    assertEquals(16, getGregorianDay(t4)) // Gregorian date ADVANCES to 16
    val h4 = getHijri(t4, maghribAug16)
    // Hijri date REMAINS same (does not advance at midnight)
    assertEquals(h2.day, h4.day)
    assertEquals(h2.monthNumber, h4.monthNumber)

    // 5. 16 August at 06:58 PM (Before Maghrib)
    val t5 = maghribAug16 - 60000L
    assertEquals(16, getGregorianDay(t5))
    val h5 = getHijri(t5, maghribAug16)
    assertEquals(h2.day, h5.day)
    assertEquals(h2.monthNumber, h5.monthNumber)

    // 6. 16 August at 06:59 PM (Next Maghrib)
    val t6 = maghribAug16
    assertEquals(16, getGregorianDay(t6))
    val h6 = getHijri(t6, maghribAug16)
    assertTrue("Hijri day should advance at next Maghrib", h6.day != h5.day || h6.monthNumber != h5.monthNumber)
  }
}

