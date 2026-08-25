package com.example.data.util

import com.example.data.db.PrayerLogEntity
import com.example.data.model.CalendarEventMoment
import com.example.data.model.DayWorshipState
import com.example.data.model.FiveLightContextState
import com.example.data.model.FiveLightMoment
import com.example.data.model.HijriDate
import com.example.data.model.NextOpportunityItem
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.PrayerPrepItem
import com.example.data.model.RightNowActionType
import com.example.data.model.TonightSummary
import com.example.data.model.WeeklyWorshipOverview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.model.RightNowItem

object FiveLightContextEngine {

    fun computeContextState(
        fardPrayers: List<PrayerItem>,
        is24Hour: Boolean = false,
        hijriDate: HijriDate,
        weeklyLogsMap: Map<String, PrayerLogEntity> = emptyMap(),
        nowMillis: Long = System.currentTimeMillis(),
        rightNowItem: RightNowItem? = null
    ): FiveLightContextState {
        val prep = computePrayerPrep(fardPrayers, is24Hour, nowMillis)
        val tonight = computeTonightSummary(fardPrayers, is24Hour, nowMillis)
        val nextOpp = computeNextOpportunity(fardPrayers, is24Hour, nowMillis, rightNowItem)
        val moment = computeMoment(fardPrayers, nowMillis)
        val calendarMom = computeCalendarMoment(hijriDate)
        val weeklyOverview = computeWeeklyOverview(fardPrayers, weeklyLogsMap, nowMillis)

        return FiveLightContextState(
            prayerPrep = prep,
            tonight = tonight,
            nextOpportunity = nextOpp,
            moment = moment,
            calendarMoment = calendarMom,
            weeklyOverview = weeklyOverview
        )
    }

    private fun computePrayerPrep(
        fardPrayers: List<PrayerItem>,
        is24Hour: Boolean,
        nowMillis: Long
    ): PrayerPrepItem? {
        val nextPrayer = fardPrayers.find { it.isNext && it.name != PrayerName.SUNRISE } ?: return null
        val diffMillis = nextPrayer.timeMillis - nowMillis
        val diffMinutes = (diffMillis / (1000 * 60)).toInt()

        // Surface preparation guide if prayer is 1 to 30 minutes away
        if (diffMinutes in 1..30) {
            val steps = listOf(
                "Perform wudu (ablution)",
                "Find a clean, quiet place for prayer",
                "Face the Qibla direction",
                "Put away distractions and prepare your heart"
            )
            return PrayerPrepItem(
                prayerName = nextPrayer.name,
                minutesRemaining = diffMinutes,
                formattedTime = nextPrayer.timeFormatted,
                steps = steps
            )
        }
        return null
    }

    private fun computeTonightSummary(
        fardPrayers: List<PrayerItem>,
        is24Hour: Boolean,
        nowMillis: Long
    ): TonightSummary? {
        val maghrib = fardPrayers.find { it.name == PrayerName.MAGHRIB } ?: return null
        val isha = fardPrayers.find { it.name == PrayerName.ISHA } ?: return null
        val fajr = fardPrayers.find { it.name == PrayerName.FAJR } ?: return null

        val tahajjudWindow = PrayerCalc.calculateTahajjudWindow(fardPrayers, is24Hour, nowMillis) ?: return null

        // Handle overnight timeline for Isha/Fajr bounding
        val (nightIshaMillis, nightFajrMillis) = if (nowMillis < fajr.timeMillis) {
            Pair(isha.timeMillis - 24 * 3600 * 1000L, fajr.timeMillis)
        } else {
            val adjustedFajr = if (fajr.timeMillis <= maghrib.timeMillis) fajr.timeMillis + 24 * 3600 * 1000L else fajr.timeMillis
            Pair(isha.timeMillis, adjustedFajr)
        }

        val ishaStr = isha.timeFormatted
        val fajrStr = tahajjudWindow.endFormatted
        val lastThirdStartStr = tahajjudWindow.startFormatted
        val tahajjudWindowStr = tahajjudWindow.windowFormatted

        val isLastThirdActive = tahajjudWindow.isCurrent
        val isIshaActive = nowMillis >= nightIshaMillis && nowMillis < tahajjudWindow.startMillis
        val isFajrActive = nowMillis >= nightFajrMillis && nowMillis < (nightFajrMillis + 90 * 60 * 1000L)
        val isNightActive = nowMillis >= (nightIshaMillis - 2 * 3600 * 1000L) || nowMillis < nightFajrMillis

        val headerTitle = if (isLastThirdActive) "The Last Third Has Begun" else "Night is Coming"
        val subtitleText = if (isLastThirdActive) "A blessed time for Qiyam al-Layl." else "A quiet part of the night is ahead."

        return TonightSummary(
            ishaTimeFormatted = ishaStr,
            fajrTimeFormatted = fajrStr,
            lastThirdStartFormatted = lastThirdStartStr,
            tahajjudWindowFormatted = tahajjudWindowStr,
            isNightActive = isNightActive,
            isLastThirdActive = isLastThirdActive,
            isIshaActive = isIshaActive,
            isTahajjudActive = isLastThirdActive,
            isFajrActive = isFajrActive,
            headerTitle = headerTitle,
            subtitleText = subtitleText,
            ishaTimeMillis = nightIshaMillis,
            fajrTimeMillis = nightFajrMillis
        )
    }

    fun computePrayerJourney(
        fardPrayers: List<PrayerItem>,
        naflPrayers: List<com.example.data.model.NaflPrayerItem>,
        todayPrayerLog: PrayerLogEntity?,
        is24Hour: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): List<com.example.data.model.PrayerJourneyNode> {
        val fajr = fardPrayers.find { it.name == PrayerName.FAJR }
        val sunrise = fardPrayers.find { it.name == PrayerName.SUNRISE }
        val dhuhr = fardPrayers.find { it.name == PrayerName.DHUHR }
        val asr = fardPrayers.find { it.name == PrayerName.ASR }
        val maghrib = fardPrayers.find { it.name == PrayerName.MAGHRIB }
        val isha = fardPrayers.find { it.name == PrayerName.ISHA }

        val ishraq = naflPrayers.find { it.type == com.example.data.model.NaflType.ISHRAQ }
        val duha = naflPrayers.find { it.type == com.example.data.model.NaflType.DUHA }
        val tahajjud = naflPrayers.find { it.type == com.example.data.model.NaflType.TAHAJJUD }

        val list = mutableListOf<com.example.data.model.PrayerJourneyNode>()

        if (fajr != null) {
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "fajr",
                    title = "Fajr",
                    subtitle = "Obligatory • Dawn Prayer",
                    arabicTitle = "الفجر",
                    timeFormatted = fajr.timeFormatted,
                    type = com.example.data.model.JourneyNodeType.FARD,
                    isCompleted = todayPrayerLog?.isCompleted(PrayerName.FAJR) == true,
                    isMissed = todayPrayerLog?.isMissed(PrayerName.FAJR) == true,
                    timeMillis = fajr.timeMillis,
                    prayerName = PrayerName.FAJR
                )
            )
            val morningAdhkarTime = fajr.timeMillis + 15 * 60 * 1000L
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "morning_adhkar",
                    title = "Morning Adhkar",
                    subtitle = "Remembrance • Post-Fajr",
                    arabicTitle = "أذكار الصباح",
                    timeFormatted = PrayerCalc.formatTime(morningAdhkarTime, is24Hour),
                    type = com.example.data.model.JourneyNodeType.ADHKAR,
                    timeMillis = morningAdhkarTime
                )
            )
        }

        if (sunrise != null) {
            val ishraqStart = sunrise.timeMillis + 18 * 60 * 1000L
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "ishraq",
                    title = "Ishraq",
                    subtitle = "Voluntary • Post-Sunrise",
                    arabicTitle = "الإشراق",
                    timeFormatted = ishraq?.timeFormatted ?: PrayerCalc.formatTime(ishraqStart, is24Hour),
                    type = com.example.data.model.JourneyNodeType.NAFL,
                    timeMillis = ishraqStart,
                    naflType = com.example.data.model.NaflType.ISHRAQ
                )
            )
        }

        if (sunrise != null && dhuhr != null) {
            val duhaStart = sunrise.timeMillis + 30 * 60 * 1000L
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "duha",
                    title = "Duha",
                    subtitle = "Voluntary • Mid-Morning",
                    arabicTitle = "الضحى",
                    timeFormatted = duha?.timeFormatted ?: PrayerCalc.formatTime(duhaStart, is24Hour),
                    type = com.example.data.model.JourneyNodeType.NAFL,
                    timeMillis = duhaStart,
                    naflType = com.example.data.model.NaflType.DUHA
                )
            )
        }

        if (dhuhr != null) {
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "dhuhr",
                    title = "Dhuhr",
                    subtitle = "Obligatory • Noon Prayer",
                    arabicTitle = "الظهر",
                    timeFormatted = dhuhr.timeFormatted,
                    type = com.example.data.model.JourneyNodeType.FARD,
                    isCompleted = todayPrayerLog?.isCompleted(PrayerName.DHUHR) == true,
                    isMissed = todayPrayerLog?.isMissed(PrayerName.DHUHR) == true,
                    timeMillis = dhuhr.timeMillis,
                    prayerName = PrayerName.DHUHR
                )
            )
        }

        if (asr != null) {
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "asr",
                    title = "Asr",
                    subtitle = "Obligatory • Afternoon Prayer",
                    arabicTitle = "العصر",
                    timeFormatted = asr.timeFormatted,
                    type = com.example.data.model.JourneyNodeType.FARD,
                    isCompleted = todayPrayerLog?.isCompleted(PrayerName.ASR) == true,
                    isMissed = todayPrayerLog?.isMissed(PrayerName.ASR) == true,
                    timeMillis = asr.timeMillis,
                    prayerName = PrayerName.ASR
                )
            )
        }

        if (maghrib != null) {
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "maghrib",
                    title = "Maghrib",
                    subtitle = "Obligatory • Sunset Prayer",
                    arabicTitle = "المغرب",
                    timeFormatted = maghrib.timeFormatted,
                    type = com.example.data.model.JourneyNodeType.FARD,
                    isCompleted = todayPrayerLog?.isCompleted(PrayerName.MAGHRIB) == true,
                    isMissed = todayPrayerLog?.isMissed(PrayerName.MAGHRIB) == true,
                    timeMillis = maghrib.timeMillis,
                    prayerName = PrayerName.MAGHRIB
                )
            )
            val eveningAdhkarTime = maghrib.timeMillis + 10 * 60 * 1000L
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "evening_adhkar",
                    title = "Evening Adhkar",
                    subtitle = "Remembrance • Post-Maghrib",
                    arabicTitle = "أذكار المساء",
                    timeFormatted = PrayerCalc.formatTime(eveningAdhkarTime, is24Hour),
                    type = com.example.data.model.JourneyNodeType.ADHKAR,
                    timeMillis = eveningAdhkarTime
                )
            )
        }

        if (isha != null) {
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "isha",
                    title = "Isha",
                    subtitle = "Obligatory • Night Prayer",
                    arabicTitle = "العشاء",
                    timeFormatted = isha.timeFormatted,
                    type = com.example.data.model.JourneyNodeType.FARD,
                    isCompleted = todayPrayerLog?.isCompleted(PrayerName.ISHA) == true,
                    isMissed = todayPrayerLog?.isMissed(PrayerName.ISHA) == true,
                    timeMillis = isha.timeMillis,
                    prayerName = PrayerName.ISHA
                )
            )
        }

        if (maghrib != null && fajr != null) {
            val adjustedFajr = if (fajr.timeMillis <= maghrib.timeMillis) fajr.timeMillis + 24 * 3600 * 1000L else fajr.timeMillis
            val nightDur = (adjustedFajr - maghrib.timeMillis).coerceAtLeast(1000L)
            val tahajjudStart = adjustedFajr - (nightDur / 3)
            list.add(
                com.example.data.model.PrayerJourneyNode(
                    id = "tahajjud",
                    title = "Tahajjud",
                    subtitle = "Voluntary • Night Prayer",
                    arabicTitle = "التهجد",
                    timeFormatted = tahajjud?.timeFormatted ?: PrayerCalc.formatTime(tahajjudStart, is24Hour),
                    type = com.example.data.model.JourneyNodeType.NAFL,
                    timeMillis = tahajjudStart,
                    naflType = com.example.data.model.NaflType.TAHAJJUD
                )
            )
        }

        var currentFound = false
        val sorted = list.sortedBy { it.timeMillis }
        return sorted.mapIndexed { index, node ->
            val nextTime = if (index < sorted.size - 1) sorted[index + 1].timeMillis else Long.MAX_VALUE
            val isNow = !currentFound && (nowMillis >= node.timeMillis && nowMillis < nextTime)
            if (isNow) currentFound = true
            val isUpcoming = nowMillis < node.timeMillis
            node.copy(isCurrentNow = isNow, isUpcoming = isUpcoming)
        }
    }

    private fun computeNextOpportunity(
        fardPrayers: List<PrayerItem>,
        is24Hour: Boolean,
        nowMillis: Long,
        rightNowItem: RightNowItem? = null
    ): NextOpportunityItem? {
        val fajr = fardPrayers.find { it.name == PrayerName.FAJR }
        val sunrise = fardPrayers.find { it.name == PrayerName.SUNRISE }
        val dhuhr = fardPrayers.find { it.name == PrayerName.DHUHR }
        val asr = fardPrayers.find { it.name == PrayerName.ASR }
        val maghrib = fardPrayers.find { it.name == PrayerName.MAGHRIB }
        val isha = fardPrayers.find { it.name == PrayerName.ISHA }

        val sunriseMillis = sunrise?.timeMillis ?: 0L
        val dhuhrMillis = dhuhr?.timeMillis ?: 0L
        val asrMillis = asr?.timeMillis ?: 0L
        val maghribMillis = maghrib?.timeMillis ?: 0L
        val fajrMillis = fajr?.timeMillis ?: 0L

        // 1. Post-Fajr until Sunrise: Morning Adhkar is Right Now -> Next is Duha Prayer
        if (fajrMillis > 0 && sunriseMillis > 0 && nowMillis in fajrMillis until sunriseMillis) {
            val duhaStartMillis = sunriseMillis + 30 * 60 * 1000L
            val mins = ((duhaStartMillis - nowMillis) / 60000).coerceAtLeast(1)
            val timeStr = if (mins >= 60) "Begins in ${mins / 60}h ${mins % 60}m" else "Begins in $mins mins"
            return NextOpportunityItem(
                title = "Duha Prayer",
                subtitle = "Voluntary Morning Prayer",
                timeFormatted = timeStr,
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER
            )
        }

        // 2. Sunrise to Ishraq (~18 min post sunrise): Ishraq Window is Right Now -> Next is Duha Prayer
        val ishraqStart = sunriseMillis + 18 * 60 * 1000L
        if (sunriseMillis > 0 && nowMillis in sunriseMillis until ishraqStart) {
            val duhaStartMillis = sunriseMillis + 30 * 60 * 1000L
            val mins = ((duhaStartMillis - nowMillis) / 60000).coerceAtLeast(1)
            return NextOpportunityItem(
                title = "Duha Prayer",
                subtitle = "Voluntary Morning Prayer",
                timeFormatted = "Begins in $mins mins",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER
            )
        }

        // 3. During Duha window: Duha Prayer Window is Right Now -> Next is Dhuhr Prayer
        val duhaEnd = dhuhrMillis - 15 * 60 * 1000L
        if (ishraqStart > 0 && duhaEnd > ishraqStart && nowMillis in ishraqStart until duhaEnd) {
            return NextOpportunityItem(
                title = "Dhuhr Prayer",
                subtitle = "Obligatory Noon Prayer",
                timeFormatted = dhuhr?.timeFormatted ?: "",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER
            )
        }

        // 4. Pre-Dhuhr & Dhuhr window
        if (dhuhrMillis > 0 && asrMillis > 0 && nowMillis in duhaEnd until asrMillis) {
            return NextOpportunityItem(
                title = "Asr Prayer",
                subtitle = "Obligatory Afternoon Prayer",
                timeFormatted = asr?.timeFormatted ?: "",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER
            )
        }

        // 5. Post-Asr: Evening Adhkar is active in Right Now -> Next is Maghrib Prayer
        if (asrMillis > 0 && maghribMillis > 0 && nowMillis in asrMillis until maghribMillis) {
            return NextOpportunityItem(
                title = "Maghrib Prayer",
                subtitle = "Obligatory Sunset Prayer",
                timeFormatted = maghrib?.timeFormatted ?: "",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER
            )
        }

        // 6. Post-Maghrib: Post-Maghrib Window is active in Right Now -> Next is Isha Prayer
        val ishaMillis = isha?.timeMillis ?: 0L
        if (maghribMillis > 0 && ishaMillis > 0 && nowMillis in maghribMillis until ishaMillis) {
            return NextOpportunityItem(
                title = "Isha Prayer",
                subtitle = "Obligatory Night Prayer",
                timeFormatted = isha?.timeFormatted ?: "",
                actionText = "View Prayer",
                actionType = RightNowActionType.VIEW_PRAYER
            )
        }

        // 7. Night time before & during Tahajjud
        if (maghribMillis > 0 && fajrMillis > 0) {
            val adjustedFajr = if (fajrMillis <= maghribMillis) fajrMillis + 24 * 3600 * 1000L else fajrMillis
            val nightDur = (adjustedFajr - maghribMillis).coerceAtLeast(1000L)
            val tahajjudStart = adjustedFajr - (nightDur / 3)

            if (nowMillis in (if (ishaMillis > 0) ishaMillis else maghribMillis) until tahajjudStart) {
                val mins = ((tahajjudStart - nowMillis) / 60000).coerceAtLeast(1)
                val hours = mins / 60
                val remMins = mins % 60
                val timeStr = if (hours > 0) "Begins in ${hours}h ${remMins}m" else "Begins in ${remMins}m"

                return NextOpportunityItem(
                    title = "Tahajjud Window",
                    subtitle = "Last Third of the Night",
                    timeFormatted = timeStr,
                    actionText = "View Prayer",
                    actionType = RightNowActionType.VIEW_PRAYER
                )
            } else if (nowMillis >= tahajjudStart && nowMillis < adjustedFajr) {
                // 8. During Tahajjud window: Tahajjud is active in Right Now -> Next is Fajr Prayer
                return NextOpportunityItem(
                    title = "Fajr Prayer",
                    subtitle = "Obligatory Dawn Prayer",
                    timeFormatted = fajr?.timeFormatted ?: "",
                    actionText = "View Prayer",
                    actionType = RightNowActionType.VIEW_PRAYER
                )
            }
        }

        return null
    }

    private fun computeMoment(
        fardPrayers: List<PrayerItem>,
        nowMillis: Long
    ): FiveLightMoment? {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        // Friday (Jumu'ah)
        if (dayOfWeek == Calendar.FRIDAY) {
            return FiveLightMoment(
                title = "Jumu'ah Mubarak",
                message = "It's Friday, a day of blessings. Remember to recite Surah Al-Kahf and send Salawat upon the Prophet ﷺ.",
                tag = "Friday",
                actionText = "Open Quran",
                actionType = RightNowActionType.OPEN_QURAN
            )
        }

        val maghrib = fardPrayers.find { it.name == PrayerName.MAGHRIB }
        val isha = fardPrayers.find { it.name == PrayerName.ISHA }
        val fajr = fardPrayers.find { it.name == PrayerName.FAJR }

        val maghribMillis = maghrib?.timeMillis ?: 0L
        val ishaMillis = isha?.timeMillis ?: 0L
        val fajrMillis = fajr?.timeMillis ?: 0L

        // Post-Maghrib quiet moment
        if (maghribMillis > 0 && ishaMillis > 0 && nowMillis in maghribMillis until ishaMillis) {
            return FiveLightMoment(
                title = "A Quiet Evening Moment",
                message = "Maghrib has entered. Take a few peaceful moments for dhikr and reflection before continuing your evening.",
                tag = "Evening Dhikr",
                actionText = "Open Tasbeeh",
                actionType = RightNowActionType.OPEN_ADHKAR
            )
        }

        // Post-Fajr Morning Barakah
        if (fajrMillis > 0 && nowMillis in fajrMillis until fajrMillis + 90 * 60 * 1000L) {
            return FiveLightMoment(
                title = "Morning Barakah",
                message = "The early morning hours carry special barakah. Begin your day with morning adhkar and intention.",
                tag = "Morning Remembrance",
                actionText = "Open Adhkar",
                actionType = RightNowActionType.OPEN_ADHKAR
            )
        }

        return null
    }

    private fun computeCalendarMoment(hijriDate: HijriDate): CalendarEventMoment? {
        val events = HijriCalc.KEY_ISLAMIC_EVENTS
        val todayEvent = events.find {
            (it.hijriMonthNumber == hijriDate.monthNumber || it.hijriMonthName == hijriDate.monthName) && it.hijriDay == hijriDate.day
        }

        if (todayEvent != null) {
            return CalendarEventMoment(
                eventTitle = todayEvent.title,
                arabicTitle = todayEvent.arabicTitle,
                description = todayEvent.description,
                isToday = true
            )
        }

        // Check if Ramadan
        if (hijriDate.monthNumber == 9) {
            val isLastTen = hijriDate.day >= 21
            val desc = if (isLastTen) {
                "The blessed last ten nights of Ramadan. A time to seek Laylat al-Qadr with prayer and recitation."
            } else {
                "Ramadan Mubarak. A month of fasting, Quran recitation, and elevated worship."
            }
            return CalendarEventMoment(
                eventTitle = if (isLastTen) "Last 10 Nights of Ramadan" else "Ramadan Mubarak",
                arabicTitle = "شهر رمضان المبارك",
                description = desc,
                isToday = true
            )
        }

        return null
    }

    private fun computeWeeklyOverview(
        fardPrayers: List<PrayerItem>,
        weeklyLogsMap: Map<String, PrayerLogEntity>,
        nowMillis: Long
    ): WeeklyWorshipOverview {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            firstDayOfWeek = Calendar.MONDAY
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY)
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

        val todayStr = sdf.format(Date(nowMillis))
        val days = mutableListOf<DayWorshipState>()

        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val dayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                Calendar.SUNDAY -> "S"
                else -> "M"
            }
            val log = weeklyLogsMap[dateStr]
            val isToday = dateStr == todayStr

            val fajrTime = fardPrayers.find { it.name == PrayerName.FAJR }?.timeMillis
            val dhuhrTime = fardPrayers.find { it.name == PrayerName.DHUHR }?.timeMillis
            val asrTime = fardPrayers.find { it.name == PrayerName.ASR }?.timeMillis
            val maghribTime = fardPrayers.find { it.name == PrayerName.MAGHRIB }?.timeMillis
            val ishaTime = fardPrayers.find { it.name == PrayerName.ISHA }?.timeMillis

            val fajrStatus = PrayerLogEntity.resolvePrayerStatus(log, PrayerName.FAJR, fajrTime, dateStr, todayStr, nowMillis)
            val dhuhrStatus = PrayerLogEntity.resolvePrayerStatus(log, PrayerName.DHUHR, dhuhrTime, dateStr, todayStr, nowMillis)
            val asrStatus = PrayerLogEntity.resolvePrayerStatus(log, PrayerName.ASR, asrTime, dateStr, todayStr, nowMillis)
            val maghribStatus = PrayerLogEntity.resolvePrayerStatus(log, PrayerName.MAGHRIB, maghribTime, dateStr, todayStr, nowMillis)
            val ishaStatus = PrayerLogEntity.resolvePrayerStatus(log, PrayerName.ISHA, ishaTime, dateStr, todayStr, nowMillis)

            days.add(
                DayWorshipState(
                    dayOfWeekName = dayName,
                    dateString = dateStr,
                    isToday = isToday,
                    fajrStatus = fajrStatus,
                    dhuhrStatus = dhuhrStatus,
                    asrStatus = asrStatus,
                    maghribStatus = maghribStatus,
                    ishaStatus = ishaStatus
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return WeeklyWorshipOverview(days = days)
    }
}
