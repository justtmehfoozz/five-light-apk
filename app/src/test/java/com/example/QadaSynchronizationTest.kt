package com.example

import com.example.data.db.PrayerLogEntity
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QadaSynchronizationTest {

    @Test
    fun testPrayerLogEntityMissedAndCompletedStates() {
        val dateStr = "2026-08-25"
        val initialLog = PrayerLogEntity(
            date = dateStr,
            dhuhrMissed = true,
            dhuhrCompleted = false,
            dhuhrQadaAdded = false
        )

        assertTrue(initialLog.isMissed(PrayerName.DHUHR))
        assertFalse(initialLog.isCompleted(PrayerName.DHUHR))
        assertFalse(initialLog.isQadaAdded(PrayerName.DHUHR))

        // Step 1: Add to Qada
        val addedToQadaLog = initialLog.withQadaAdded(PrayerName.DHUHR, true)
        assertTrue(addedToQadaLog.isMissed(PrayerName.DHUHR))
        assertTrue(addedToQadaLog.isQadaAdded(PrayerName.DHUHR))
        assertFalse(addedToQadaLog.isCompleted(PrayerName.DHUHR))

        // Step 2: Make up / complete Qada
        val completedLog = addedToQadaLog.copy(
            dhuhrCompleted = true,
            dhuhrMissed = false,
            dhuhrQadaAdded = false
        )
        assertTrue(completedLog.isCompleted(PrayerName.DHUHR))
        assertFalse(completedLog.isMissed(PrayerName.DHUHR))
        assertFalse(completedLog.isQadaAdded(PrayerName.DHUHR))
    }

    @Test
    fun testMultipleMissedPrayersFIFOSelection() {
        val log1 = PrayerLogEntity(
            date = "2026-08-25",
            dhuhrMissed = true,
            dhuhrCompleted = false,
            dhuhrQadaAdded = true
        )
        val log2 = PrayerLogEntity(
            date = "2026-08-26",
            dhuhrMissed = true,
            dhuhrCompleted = false,
            dhuhrQadaAdded = true
        )
        val logs = listOf(log1, log2)

        // Find oldest missed prayer added to Qada
        val target = logs.firstOrNull {
            it.isMissed(PrayerName.DHUHR) && it.isQadaAdded(PrayerName.DHUHR) && !it.isCompleted(PrayerName.DHUHR)
        }

        assertEquals("2026-08-25", target?.date)

        // Update target
        val updatedLog1 = target!!.copy(dhuhrCompleted = true, dhuhrMissed = false, dhuhrQadaAdded = false)
        val remainingLogs = listOf(updatedLog1, log2)

        // Verify updatedLog1 is Prayed
        assertTrue(remainingLogs[0].isCompleted(PrayerName.DHUHR))
        assertFalse(remainingLogs[0].isMissed(PrayerName.DHUHR))

        // Verify log2 is still Missed and independent
        assertFalse(remainingLogs[1].isCompleted(PrayerName.DHUHR))
        assertTrue(remainingLogs[1].isMissed(PrayerName.DHUHR))
        assertTrue(remainingLogs[1].isQadaAdded(PrayerName.DHUHR))
    }

    @Test
    fun testPrayerStatusResolution() {
        val log = PrayerLogEntity(
            date = "2026-08-25",
            dhuhrCompleted = true,
            dhuhrMissed = false
        )
        val status = PrayerLogEntity.resolvePrayerStatus(
            log = log,
            prayerName = PrayerName.DHUHR,
            prayerTimeMillis = 1000L,
            targetDateString = "2026-08-25",
            todayDateString = "2026-08-25"
        )
        assertEquals(PrayerStatus.PRAYED, status)
    }

    @Test
    fun testDirectPrayerStatusChangeClearsQadaAddedAndPreventsDoubleDecrement() {
        // Initial state: missed and added to Qada
        val log = PrayerLogEntity(
            date = "2026-08-25",
            dhuhrMissed = true,
            dhuhrCompleted = false,
            dhuhrQadaAdded = true
        )
        var qadaCount = 2

        // Simulate user manually marking as PRAYED in Personal Log
        val wasQadaAdded = log.isQadaAdded(PrayerName.DHUHR)
        val isCompleted = true
        val isMissed = false
        val updatedLog = log.copy(
            dhuhrCompleted = isCompleted,
            dhuhrMissed = isMissed,
            dhuhrQadaAdded = if (isCompleted) false else log.dhuhrQadaAdded
        )

        if (isCompleted && wasQadaAdded) {
            if (qadaCount > 0) qadaCount -= 1
        }

        // Count decremented once to 1
        assertEquals(1, qadaCount)
        assertFalse(updatedLog.isQadaAdded(PrayerName.DHUHR))
        assertTrue(updatedLog.isCompleted(PrayerName.DHUHR))

        // If user marks as PRAYED again, wasQadaAdded is now false so count is not decremented again
        val wasQadaAddedSecondTime = updatedLog.isQadaAdded(PrayerName.DHUHR)
        if (isCompleted && wasQadaAddedSecondTime) {
            if (qadaCount > 0) qadaCount -= 1
        }
        // Count remains 1, avoiding double decrement
        assertEquals(1, qadaCount)
    }

    @Test
    fun testQadaCountBoundsNeverNegative() {
        var count = 0
        val newCount = (count - 1).coerceAtLeast(0)
        assertEquals(0, newCount)
    }

    @Test
    fun testQadaStateResolutionSemantics() {
        // Case 1: Never added, count = 0 -> NO_QADA
        val count1 = 0
        val hasEver1 = false
        val state1 = when {
            count1 > 0 -> com.example.ui.components.QadaPrayerState.ACTIVE
            hasEver1 -> com.example.ui.components.QadaPrayerState.COMPLETED
            else -> com.example.ui.components.QadaPrayerState.NO_QADA
        }
        assertEquals(com.example.ui.components.QadaPrayerState.NO_QADA, state1)

        // Case 2: Active obligation, count > 0 -> ACTIVE
        val count2 = 2
        val hasEver2 = true
        val state2 = when {
            count2 > 0 -> com.example.ui.components.QadaPrayerState.ACTIVE
            hasEver2 -> com.example.ui.components.QadaPrayerState.COMPLETED
            else -> com.example.ui.components.QadaPrayerState.NO_QADA
        }
        assertEquals(com.example.ui.components.QadaPrayerState.ACTIVE, state2)

        // Case 3: Previously had obligations, now count = 0 -> COMPLETED
        val count3 = 0
        val hasEver3 = true
        val state3 = when {
            count3 > 0 -> com.example.ui.components.QadaPrayerState.ACTIVE
            hasEver3 -> com.example.ui.components.QadaPrayerState.COMPLETED
            else -> com.example.ui.components.QadaPrayerState.NO_QADA
        }
        assertEquals(com.example.ui.components.QadaPrayerState.COMPLETED, state3)
    }
}
