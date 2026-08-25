package com.example.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDate
import com.example.data.model.HijriDateMethod
import com.example.data.model.IslamicDateState
import com.example.data.model.MoonPhase
import com.example.data.model.PrayerName
import com.example.data.util.HijriCalc
import com.example.data.util.MoonPhaseCalculator
import com.example.data.util.PrayerCalc
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * IslamicDateRepository: SINGLE SOURCE OF TRUTH for all Gregorian and Hijri date information.
 *
 * Core Boundaries & Phase 3 Architecture:
 * 1. Gregorian Date follows the user's LOCAL CIVIL DATE:
 *    - Changes strictly at 00:00:00 local civil time.
 *    - Respects the device or configured city timezone.
 *
 * 2. Hijri Date follows the Islamic-day boundary:
 *    - Before local sunset (Maghrib) -> Current Islamic date (Base mapping for today).
 *    - After local sunset (Maghrib) -> Next Islamic date (Base mapping for tomorrow).
 *    - At midnight (00:00), the Gregorian day advances, but before sunset tomorrow the Hijri
 *      date remains the Islamic date that began at previous Maghrib.
 *
 * 3. Automatic Sunset / Location / Timezone Date Transition (Phase 3):
 *    - Calculates precise local sunset (Maghrib) and local civil midnight boundaries.
 *    - Automatically schedules lightweight single-shot timer for the next relevant boundary.
 *    - When sunset or midnight is crossed, previous Islamic/Gregorian state is invalidated,
 *      recomputed off the main thread, and published reactively.
 *    - No continuous per-second polling or repeated astronomical recalculations.
 *    - Dynamically reschedules whenever location, calculation method, or system timezone changes.
 *    - Catches background transitions immediately upon app resume (onAppResume).
 *
 * 4. Offline-First Caching & Network Synchronization (Phase 2):
 *    - Valid API synchronizations are persisted locally.
 *    - When offline: verified cached mappings are utilized with uninterrupted local sunset transitions.
 *    - When network returns: automatic non-blocking background sync occurs without disrupting UI.
 */
class IslamicDateRepository(
    private val context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    companion object {
        private const val TAG = "IslamicDateRepository"

        @Volatile
        private var INSTANCE: IslamicDateRepository? = null

        fun getInstance(context: Context? = null): IslamicDateRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IslamicDateRepository(context?.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Configuration State
    private var currentCity: CityLocation = CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, CalcMethod.KARACHI)
    private var currentCalcMethod: CalcMethod = CalcMethod.KARACHI
    private var currentHijriMethod: HijriDateMethod = HijriDateMethod.REGIONAL_INDIA
    private var currentCustomOffset: Int = 0

    // Single observable state for the entire application
    private val _islamicDateState = MutableStateFlow(computeState(System.currentTimeMillis()))
    val islamicDateState: StateFlow<IslamicDateState> = _islamicDateState.asStateFlow()

    private var boundaryJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var timeChangeReceiver: BroadcastReceiver? = null

    init {
        scheduleNextBoundary()
        registerNetworkMonitoring()
        registerTimeAndZoneMonitoring()
        if (context != null) {
            com.example.data.sync.HijriSyncWorker.enqueuePeriodic(context)
            scope.launch(Dispatchers.IO) {
                HijriCalc.preloadCacheFromRoom(context)
                val updatedState = computeState(System.currentTimeMillis())
                _islamicDateState.value = updatedState
            }
        }
        // Perform initial background sync attempt if internet is available
        syncInBackground(force = false)
    }

    private fun registerTimeAndZoneMonitoring() {
        if (context == null) return
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
            }
            timeChangeReceiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    Log.d(TAG, "System time / timezone / date broadcast received: ${intent?.action}. Refreshing state.")
                    refresh()
                }
            }
            context.registerReceiver(timeChangeReceiver, filter)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register time change receiver: ${e.message}")
        }
    }

    private fun registerNetworkMonitoring() {
        if (context == null) return
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Network connection restored. Triggering background Hijri date synchronization.")
                    syncInBackground(force = false)
                    com.example.data.sync.HijriSyncWorker.enqueueOneTime(context, force = false)
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "Network connection lost. App operating in verified offline cache mode.")
                }
            }
            cm.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register network callback: ${e.message}")
        }
    }

    fun getCurrentGregorianDate(nowMillis: Long = System.currentTimeMillis()): LocalDate {
        val zoneId = getTimeZone().toZoneId()
        return Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    }

    fun getCurrentHijriDate(nowMillis: Long = System.currentTimeMillis()): HijriDate {
        return computeHijriDate(nowMillis)
    }

    fun getHijriDateForLocalDate(localDate: LocalDate): HijriDate {
        val tz = getTimeZone()
        val zoneId = try { tz.toZoneId() } catch (_: Exception) { ZoneId.systemDefault() }
        val noonMillis = localDate.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        return HijriCalc.getHijriDate(
            timestampMillis = noonMillis,
            timeZone = tz,
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            maghribTimeMillis = null,
            method = currentHijriMethod,
            customOffsetDays = currentCustomOffset,
            calcMethod = currentCalcMethod,
            context = context
        )
    }

    fun getHijriDateForTimestamp(timestampMillis: Long): HijriDate {
        return computeHijriDate(timestampMillis)
    }

    fun formatHijriString(hijriDate: HijriDate = getCurrentHijriDate()): String {
        return HijriCalc.formatHijriString(hijriDate)
    }

    fun formatHijriArabicString(hijriDate: HijriDate = getCurrentHijriDate()): String {
        return HijriCalc.formatHijriArabicString(hijriDate)
    }

    fun getCurrentSunset(nowMillis: Long = System.currentTimeMillis()): Long? {
        val date = Date(nowMillis)
        val prayerTimes = PrayerCalc.calculatePrayerTimes(
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            date = date,
            method = currentCalcMethod,
            timeZoneOffsetHours = currentCity.timezoneOffsetHours
        )
        return prayerTimes.find { it.name == PrayerName.MAGHRIB }?.timeMillis
    }

    fun isAfterMaghrib(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val sunset = getCurrentSunset(nowMillis)
        return if (sunset != null && sunset > 0) {
            nowMillis >= sunset
        } else {
            false
        }
    }

    fun getCurrentIslamicDateState(nowMillis: Long = System.currentTimeMillis()): IslamicDateState {
        return computeState(nowMillis)
    }

    fun getCurrentMoonPhase(nowMillis: Long = System.currentTimeMillis()): MoonPhase {
        return MoonPhaseCalculator.calculateMoonPhase(nowMillis)
    }

    fun getTimeZone(): TimeZone {
        val offsetMillis = (currentCity.timezoneOffsetHours * 3600000).toInt()
        val offsetHours = offsetMillis / 3600000
        val offsetMins = Math.abs((offsetMillis / 60000) % 60)
        val tzId = String.format(Locale.US, "GMT%+03d:%02d", offsetHours, offsetMins)
        return SimpleTimeZone(offsetMillis, tzId)
    }

    private fun computeHijriDate(nowMillis: Long): HijriDate {
        val tz = getTimeZone()
        val sunsetMillis = getCurrentSunset(nowMillis)
        return HijriCalc.getHijriDate(
            timestampMillis = nowMillis,
            timeZone = tz,
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            maghribTimeMillis = sunsetMillis,
            method = currentHijriMethod,
            customOffsetDays = currentCustomOffset,
            calcMethod = currentCalcMethod,
            context = context
        )
    }

    fun computeState(nowMillis: Long): IslamicDateState {
        val tz = getTimeZone()
        val zoneId = try { tz.toZoneId() } catch (_: Exception) { ZoneId.systemDefault() }
        val localDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val gregorianFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        val gregorianFormatted = localDate.format(gregorianFormatter)

        val sunsetMillis = getCurrentSunset(nowMillis)
        val afterMaghrib = if (sunsetMillis != null && sunsetMillis > 0) nowMillis >= sunsetMillis else false

        val timeFormatter = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
            timeZone = tz
        }
        val sunsetFormatted = if (sunsetMillis != null && sunsetMillis > 0) timeFormatter.format(Date(sunsetMillis)) else ""

        val hijriDate = HijriCalc.getHijriDate(
            timestampMillis = nowMillis,
            timeZone = tz,
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            maghribTimeMillis = sunsetMillis,
            method = currentHijriMethod,
            customOffsetDays = currentCustomOffset,
            calcMethod = currentCalcMethod,
            context = context
        )

        val targetDate = if (afterMaghrib) localDate.plusDays(1) else localDate
        val cacheRecord = HijriCalc.getCacheRecord(context, targetDate, currentHijriMethod)
            ?: HijriCalc.getLatestCacheRecord(context, currentHijriMethod)

        val sourceDesc = when (currentHijriMethod) {
            HijriDateMethod.REGIONAL_INDIA -> "Regional Hilal Moon-Sighting (India & South Asia)"
            HijriDateMethod.SAUDI_UMM_AL_QURA -> "Saudi Arabia Umm al-Qura Astronomical System"
            HijriDateMethod.GLOBAL_ASTRONOMICAL -> "Global Astronomical Calculation"
            HijriDateMethod.CUSTOM_OFFSET -> "Custom Adjusted (${currentCustomOffset} days)"
        }

        val hijriFormatted = HijriCalc.formatHijriString(hijriDate)

        return IslamicDateState(
            gregorianDate = localDate,
            gregorianDateFormatted = gregorianFormatted,
            hijriDate = hijriDate,
            hijriDateFormatted = hijriFormatted,
            timeZone = tz,
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            cityName = currentCity.fullDisplayName,
            sunsetTimeMillis = sunsetMillis,
            sunsetTimeFormatted = sunsetFormatted,
            isAfterMaghrib = afterMaghrib,
            method = currentHijriMethod,
            isSyncedWithInternet = hijriDate.isSyncedWithInternet,
            isCachedOffline = cacheRecord != null,
            lastSyncTimestampMillis = cacheRecord?.syncTimestampMillis,
            sourceDescription = sourceDesc,
            moonPhase = MoonPhaseCalculator.calculateMoonPhase(nowMillis)
        )
    }

    /**
     * Computes the timestamp (in epoch millis) of the NEXT relevant date boundary:
     * - Today's sunset / Maghrib (if currently before sunset)
     * - Local civil midnight 00:00:00 (Gregorian day transition)
     * - Tomorrow's sunset / Maghrib (if currently after today's sunset)
     */
    fun calculateNextBoundaryMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val tz = getTimeZone()
        val zoneId = try { tz.toZoneId() } catch (_: Exception) { ZoneId.systemDefault() }
        val localDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

        // 1. Check today's sunset
        val todaySunset = getCurrentSunset(nowMillis)
        if (todaySunset != null && todaySunset > nowMillis) {
            return todaySunset
        }

        // 2. Next civil midnight (00:00:00 of tomorrow)
        val nextMidnightMillis = localDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // 3. Tomorrow's sunset
        val tomorrowNoonMillis = localDate.plusDays(1).atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        val tomorrowSunset = getCurrentSunset(tomorrowNoonMillis)

        // Select the earliest future boundary
        val boundaries = mutableListOf<Long>()
        if (nextMidnightMillis > nowMillis) {
            boundaries.add(nextMidnightMillis)
        }
        if (tomorrowSunset != null && tomorrowSunset > nowMillis) {
            boundaries.add(tomorrowSunset)
        }

        return boundaries.minOrNull() ?: (nowMillis + 3600000L)
    }

    fun updateLocation(city: CityLocation) {
        if (currentCity != city) {
            currentCity = city
            Log.d(TAG, "Location updated to ${city.fullDisplayName}. Rescheduling boundary.")
            refresh()
        }
    }

    fun updateCalcMethod(calcMethod: CalcMethod) {
        if (currentCalcMethod != calcMethod) {
            currentCalcMethod = calcMethod
            refresh()
        }
    }

    fun updateHijriMethod(method: HijriDateMethod) {
        if (currentHijriMethod != method) {
            currentHijriMethod = method
            refresh()
        }
    }

    fun updateCustomOffset(offset: Int) {
        if (currentCustomOffset != offset) {
            currentCustomOffset = offset
            refresh()
        }
    }

    fun refresh(nowMillis: Long = System.currentTimeMillis()) {
        val newState = computeState(nowMillis)
        _islamicDateState.value = newState
        scheduleNextBoundary(nowMillis)
        syncInBackground(force = false)
    }

    fun syncInBackground(force: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            try {
                val state = _islamicDateState.value
                val success = HijriCalc.syncWithInternet(
                    context = context,
                    date = state.gregorianDate,
                    method = currentHijriMethod,
                    cityName = currentCity.fullDisplayName,
                    latitude = currentCity.latitude,
                    longitude = currentCity.longitude,
                    force = force
                )
                if (success) {
                    val updatedState = computeState(System.currentTimeMillis())
                    _islamicDateState.value = updatedState
                }
            } catch (e: Exception) {
                Log.w(TAG, "Background sync handled gracefully: ${e.message}")
            }
        }
    }

    /**
     * App Resume handler: immediately recalculates date state off the main thread.
     * Catches any sunset or midnight transitions that occurred while app was in background.
     */
    fun onAppResume() {
        Log.d(TAG, "App resumed. Recalculating state and rescheduling next boundary.")
        refresh()
    }

    /**
     * Lightweight boundary scheduler:
     * - Schedules an exact single-shot timer for the next relevant boundary (sunset or midnight).
     * - Includes a small 1-minute fallback safety tick in case of system clock adjustments.
     * - Never polls every second and runs all computations off the main thread.
     */
    private fun scheduleNextBoundary(nowMillis: Long = System.currentTimeMillis()) {
        boundaryJob?.cancel()
        boundaryJob = scope.launch(Dispatchers.Default) {
            val nextBoundary = calculateNextBoundaryMillis(nowMillis)
            val delayUntilBoundary = (nextBoundary - nowMillis + 500L).coerceAtLeast(1000L)
            Log.d(TAG, "Next date boundary scheduled in ${delayUntilBoundary / 1000}s (at $nextBoundary)")

            // Wait until next boundary or at most 60 seconds (for background clock resilience)
            val sleepDuration = delayUntilBoundary.coerceAtMost(60000L)
            delay(sleepDuration)

            if (isActive) {
                val currentNow = System.currentTimeMillis()
                val newState = computeState(currentNow)
                if (_islamicDateState.value != newState) {
                    Log.d(TAG, "Boundary crossed! New state published: Hijri ${newState.hijriDate.day} ${newState.hijriDate.monthName} (isAfterMaghrib=${newState.isAfterMaghrib})")
                    _islamicDateState.value = newState
                }
                // Reschedule for next boundary
                scheduleNextBoundary(currentNow)
            }
        }
    }
}


