package com.example.data.util

import com.example.data.model.NaflPrayerItem
import com.example.data.model.NaflPreferences
import com.example.data.model.NaflType
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.RightNowActionType
import com.example.data.model.RightNowItem

object NaflCalc {

    fun calculateNaflTimes(
        fardPrayers: List<PrayerItem>,
        preferences: NaflPreferences,
        is24Hour: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): List<NaflPrayerItem> {
        if (!preferences.isAnyEnabled) return emptyList()

        val fajr = fardPrayers.find { it.name == PrayerName.FAJR }
        val sunrise = fardPrayers.find { it.name == PrayerName.SUNRISE }
        val dhuhr = fardPrayers.find { it.name == PrayerName.DHUHR }
        val maghrib = fardPrayers.find { it.name == PrayerName.MAGHRIB }
        val isha = fardPrayers.find { it.name == PrayerName.ISHA }

        val items = mutableListOf<NaflPrayerItem>()

        // 1. TAHAJJUD (Last third of the night)
        if (preferences.tahajjudEnabled) {
            val tahajjudWindow = PrayerCalc.calculateTahajjudWindow(fardPrayers, is24Hour, nowMillis)
            if (tahajjudWindow != null) {
                items.add(
                    NaflPrayerItem(
                        type = NaflType.TAHAJJUD,
                        timeFormatted = tahajjudWindow.windowFormatted,
                        isCurrentWindow = tahajjudWindow.isCurrent,
                        startMillis = tahajjudWindow.startMillis,
                        endMillis = tahajjudWindow.endMillis
                    )
                )
            }
        }

        // 2. ISHRAQ (Post-sunrise offset ~18 mins)
        if (preferences.ishraqEnabled && sunrise != null) {
            val ishraqStartMillis = sunrise.timeMillis + 18 * 60 * 1000L
            val ishraqEndMillis = sunrise.timeMillis + 45 * 60 * 1000L

            val formatted = PrayerCalc.formatTime(ishraqStartMillis, is24Hour, PrayerCalc.activeTimeZone)
            val isCurrent = nowMillis in ishraqStartMillis..ishraqEndMillis

            items.add(
                NaflPrayerItem(
                    type = NaflType.ISHRAQ,
                    timeFormatted = formatted,
                    isCurrentWindow = isCurrent,
                    startMillis = ishraqStartMillis,
                    endMillis = ishraqEndMillis
                )
            )
        }

        // 3. DUHA / CHASHT (From after Ishraq until ~15 min before Dhuhr)
        if (preferences.duhaEnabled && sunrise != null && dhuhr != null) {
            val duhaStartMillis = sunrise.timeMillis + 30 * 60 * 1000L
            val duhaEndMillis = dhuhr.timeMillis - 15 * 60 * 1000L

            val startStr = PrayerCalc.formatTime(duhaStartMillis, is24Hour, PrayerCalc.activeTimeZone)
            val endStr = PrayerCalc.formatTime(duhaEndMillis, is24Hour, PrayerCalc.activeTimeZone)
            val formatted = "$startStr – $endStr"

            val isCurrent = nowMillis in duhaStartMillis..duhaEndMillis

            items.add(
                NaflPrayerItem(
                    type = NaflType.DUHA,
                    timeFormatted = formatted,
                    isCurrentWindow = isCurrent,
                    startMillis = duhaStartMillis,
                    endMillis = duhaEndMillis
                )
            )
        }

        // 4. AWWABIN (Post-Maghrib voluntary window)
        if (preferences.awwabinEnabled && maghrib != null && isha != null) {
            val awwabinStartMillis = maghrib.timeMillis + 10 * 60 * 1000L
            val awwabinEndMillis = isha.timeMillis

            val startStr = PrayerCalc.formatTime(awwabinStartMillis, is24Hour, PrayerCalc.activeTimeZone)
            val endStr = PrayerCalc.formatTime(awwabinEndMillis, is24Hour, PrayerCalc.activeTimeZone)
            val formatted = "$startStr – $endStr"

            val isCurrent = nowMillis in awwabinStartMillis..awwabinEndMillis

            items.add(
                NaflPrayerItem(
                    type = NaflType.AWWABIN,
                    timeFormatted = formatted,
                    isCurrentWindow = isCurrent,
                    startMillis = awwabinStartMillis,
                    endMillis = awwabinEndMillis
                )
            )
        }

        val orderMap = preferences.naflOrder.withIndex().associate { it.value.uppercase() to it.index }
        return items.sortedBy { orderMap[it.type.name.uppercase()] ?: 99 }
    }

    fun calculateRightNowItem(
        fardPrayers: List<PrayerItem>,
        is24Hour: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): RightNowItem {
        val fajr = fardPrayers.find { it.name == PrayerName.FAJR }
        val sunrise = fardPrayers.find { it.name == PrayerName.SUNRISE }
        val dhuhr = fardPrayers.find { it.name == PrayerName.DHUHR }
        val asr = fardPrayers.find { it.name == PrayerName.ASR }
        val maghrib = fardPrayers.find { it.name == PrayerName.MAGHRIB }
        val isha = fardPrayers.find { it.name == PrayerName.ISHA }

        val fajrMillis = fajr?.timeMillis ?: 0L
        val sunriseMillis = sunrise?.timeMillis ?: 0L
        val dhuhrMillis = dhuhr?.timeMillis ?: 0L
        val asrMillis = asr?.timeMillis ?: 0L
        val maghribMillis = maghrib?.timeMillis ?: 0L
        val ishaMillis = isha?.timeMillis ?: 0L

        // 1. After Fajr to Sunrise (Morning Adhkar)
        if (fajrMillis > 0 && sunriseMillis > 0 && nowMillis in fajrMillis until sunriseMillis) {
            return RightNowItem(
                title = "Morning Adhkar",
                subtitle = "Post-Fajr Remembrance",
                description = "The morning remembrance window is open until sunrise.",
                actionText = "Open Adhkar",
                actionType = RightNowActionType.OPEN_ADHKAR
            )
        }

        // 2. Around Sunrise to Ishraq (Post-sunrise)
        val ishraqStart = sunriseMillis + 18 * 60 * 1000L
        if (sunriseMillis > 0 && nowMillis in sunriseMillis until ishraqStart) {
            return RightNowItem(
                title = "Ishraq Window",
                subtitle = "Shortly After Sunrise",
                description = "Ishraq prayer begins shortly as the sun rises above the horizon (~15–20 mins).",
                actionText = "View Time",
                actionType = RightNowActionType.VIEW_PRAYER,
                naflType = NaflType.ISHRAQ
            )
        }

        // 3. During Duha window (Ishraq to ~15 min before Dhuhr)
        val duhaEnd = dhuhrMillis - 15 * 60 * 1000L
        if (ishraqStart > 0 && duhaEnd > ishraqStart && nowMillis in ishraqStart until duhaEnd) {
            return RightNowItem(
                title = "Duha Prayer Window",
                subtitle = "Voluntary Morning Prayer",
                description = "A voluntary morning prayer (Salat al-Duha) is available right now.",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER,
                naflType = NaflType.DUHA
            )
        }

        // 4. After Asr / Evening Adhkar
        val eveningAdhkarStart = asrMillis
        if (asrMillis > 0 && maghribMillis > 0 && nowMillis in eveningAdhkarStart until maghribMillis) {
            return RightNowItem(
                title = "Evening Adhkar",
                subtitle = "Pre-Sunset Remembrance",
                description = "An opportunity for evening remembrance and dhikr before sunset.",
                actionText = "Open Adhkar",
                actionType = RightNowActionType.OPEN_ADHKAR
            )
        }

        // 5. After Maghrib (Post-Maghrib Evening Remembrance)
        if (maghribMillis > 0 && ishaMillis > 0 && nowMillis in maghribMillis until ishaMillis) {
            return RightNowItem(
                title = "Evening Adhkar & Voluntary Prayer",
                subtitle = "Post-Maghrib Window",
                description = "An opportunity for evening dhikr and voluntary worship between Maghrib and Isha.",
                actionText = "Open Adhkar",
                actionType = RightNowActionType.OPEN_ADHKAR
            )
        }

        // 6. Tahajjud Window (Last third of night)
        val tahajjudWindow = PrayerCalc.calculateTahajjudWindow(fardPrayers, is24Hour, nowMillis)
        if (tahajjudWindow != null && tahajjudWindow.isCurrent) {
            return RightNowItem(
                title = "Tahajjud Window",
                subtitle = "Last Third of the Night",
                description = "The final third of the night has begun. A blessed time for Qiyam al-Layl.",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER,
                naflType = NaflType.TAHAJJUD
            )
        }

        // 7. Default fallback
        return RightNowItem(
            title = "Mindful Remembrance",
            subtitle = "Daily Dhikr & Recitation",
            description = "Engage in short dhikr, tasbeeh, or Quran recitation.",
            actionText = "Open Tasbeeh",
            actionType = RightNowActionType.OPEN_ADHKAR
        )
    }
}
