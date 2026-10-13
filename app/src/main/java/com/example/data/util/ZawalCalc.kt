package com.example.data.util

import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import kotlin.math.ceil

enum class ZawalDisplayMode {
    HIDDEN,
    APPROACHING,
    ACTIVE
}

data class ZawalInfo(
    val mode: ZawalDisplayMode = ZawalDisplayMode.HIDDEN,
    val zawalStartTimeFormatted: String = "",
    val minutesRemaining: Int = 0,
    val zawalStartMillis: Long = 0L,
    val zawalEndMillis: Long = 0L
)

object ZawalCalc {

    /**
     * Calculates Zawal window and current state based on verified Dhuhr prayer time.
     *
     * Zawal (solar zenith) is the period around solar noon during which voluntary (Nafl)
     * prayer is restricted according to Islamic jurisprudence (Fiqh).
     *
     * Calculation Convention:
     * - Zawal End (`zawalEndMillis`): Exact Dhuhr prayer start time (`dhuhr.timeMillis`), which marks solar zenith exit.
     * - Zawal Start (`zawalStartMillis`): 10 minutes prior to Dhuhr start (`dhuhr.timeMillis - 10 * 60 * 1000L`).
     * - Approaching Window (`approachingStartMillis`): 15 minutes prior to Zawal start (`zawalStartMillis - 15 * 60 * 1000L`).
     */
    fun calculateZawalInfo(
        prayerTimes: List<PrayerItem>,
        is24Hour: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): ZawalInfo {
        val dhuhrItem = prayerTimes.find { it.name == PrayerName.DHUHR } ?: return ZawalInfo()
        val dhuhrMillis = dhuhrItem.timeMillis
        if (dhuhrMillis <= 0L) return ZawalInfo()

        val zawalEndMillis = dhuhrMillis
        val zawalStartMillis = dhuhrMillis - 10 * 60 * 1000L // 10 min before Dhuhr start
        val approachingStartMillis = zawalStartMillis - 15 * 60 * 1000L // 15 min before Zawal start (25 min before Dhuhr)

        return when {
            nowMillis in approachingStartMillis until zawalStartMillis -> {
                val formattedTime = PrayerCalc.formatTime(zawalStartMillis, is24Hour, PrayerCalc.activeTimeZone)
                val diffMillis = (zawalStartMillis - nowMillis).coerceAtLeast(0L)
                val minutes = ceil(diffMillis / 60000.0).toInt().coerceAtLeast(1)
                ZawalInfo(
                    mode = ZawalDisplayMode.APPROACHING,
                    zawalStartTimeFormatted = formattedTime,
                    minutesRemaining = minutes,
                    zawalStartMillis = zawalStartMillis,
                    zawalEndMillis = zawalEndMillis
                )
            }
            nowMillis in zawalStartMillis until zawalEndMillis -> {
                val diffMillis = (zawalEndMillis - nowMillis).coerceAtLeast(0L)
                val minutes = ceil(diffMillis / 60000.0).toInt().coerceAtLeast(1)
                ZawalInfo(
                    mode = ZawalDisplayMode.ACTIVE,
                    zawalStartTimeFormatted = PrayerCalc.formatTime(zawalStartMillis, is24Hour, PrayerCalc.activeTimeZone),
                    minutesRemaining = minutes,
                    zawalStartMillis = zawalStartMillis,
                    zawalEndMillis = zawalEndMillis
                )
            }
            else -> ZawalInfo() // HIDDEN
        }
    }
}
