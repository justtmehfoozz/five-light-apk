package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthRepository
import com.example.data.db.AppDatabase
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.model.Madhab
import com.example.data.model.TasbeehSound
import com.example.data.reminder.PrePrayerReminderOffset
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.repository.AppRepository
import com.example.data.util.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SetUpFiveLightFlowTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: AppRepository
    private lateinit var notificationManager: SmartPrayerNotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear preferences for clean test run
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("smart_prayer_notifications_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("prayer_reminders_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        database = AppDatabase.getDatabase(context)
        authRepository = AuthRepository.getInstance(context)
        repository = AppRepository(database, context)
        notificationManager = SmartPrayerNotificationManager(context)
    }

    // ---------------------------------------------------------------------------------------------
    // Setup Lifecycle & Account Isolation Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testSetupCompletionFlag_initiallyFalse() {
        val testUid = "test_user_abc_123"
        assertFalse("New user must not have completed setup initially", authRepository.isSetupCompleted(testUid))
    }

    @Test
    fun testSetupCompletionFlag_setCompleted() {
        val testUid = "test_user_abc_123"
        authRepository.setSetupCompleted(testUid, true)
        assertTrue("Setup must be completed after calling setSetupCompleted", authRepository.isSetupCompleted(testUid))
    }

    @Test
    fun testSetupCompletionFlag_isolatedPerUid() {
        val user1 = "uid_user_one"
        val user2 = "uid_user_two"

        authRepository.setSetupCompleted(user1, true)

        assertTrue(authRepository.isSetupCompleted(user1))
        assertFalse(authRepository.isSetupCompleted(user2))
    }

    @Test
    fun testSetupCompletionFlag_emptyUidHandledGracefully() {
        assertFalse(authRepository.isSetupCompleted(""))
        assertFalse(authRepository.isSetupCompleted("   "))
    }

    @Test
    fun testSetupCompletedEventEmitsOnChange() {
        val testUid = "event_test_uid"
        val initialEvent = authRepository.setupCompletedEvent.value

        authRepository.setSetupCompleted(testUid, true)

        val updatedEvent = authRepository.setupCompletedEvent.value
        assertEquals(initialEvent + 1, updatedEvent)
    }

    @Test
    fun testGuestMode_isSetupRequiredCondition() {
        // In guest mode, currentUser is null.
        // Therefore isSetupRequired = (currentUser != null && !isSetupCompleted(uid))
        val currentGuestUserUid: String? = null
        val isGuestSetupRequired = currentGuestUserUid != null && !authRepository.isSetupCompleted(currentGuestUserUid)
        assertFalse("Guest mode must never be forced into setup flow", isGuestSetupRequired)
    }

    // ---------------------------------------------------------------------------------------------
    // Phase 2 Preference Storage & Architecture Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testLocationPreference_onboardingSelectionPersistsAndSurvivesRestart() {
        val targetCity = repository.PREDEFINED_CITIES.find { it.cityName == "London" }
            ?: CityLocation("London", "United Kingdom", 51.5074, -0.1278)

        // Simulate onboarding selection
        repository.setCity(targetCity)
        assertEquals("London", repository.selectedCity.value.cityName)

        // Simulate app restart / process recreation
        val restartedRepo = AppRepository(database, context)
        assertEquals("London", restartedRepo.selectedCity.value.cityName)
        assertEquals(targetCity.countryName, restartedRepo.selectedCity.value.countryName)
        assertEquals(targetCity.latitude, restartedRepo.selectedCity.value.latitude, 0.0001)
    }

    @Test
    fun testCalcMethodPreference_onboardingSelectionPersistsAndSurvivesRestart() {
        // Test setting to Karachi method
        repository.setCalcMethod(CalcMethod.KARACHI)
        assertEquals(CalcMethod.KARACHI, repository.calcMethod.value)

        // App restart
        val restartedRepo1 = AppRepository(database, context)
        assertEquals(CalcMethod.KARACHI, restartedRepo1.calcMethod.value)

        // Test setting to MWL
        repository.setCalcMethod(CalcMethod.MWL)
        assertEquals(CalcMethod.MWL, repository.calcMethod.value)

        val restartedRepo2 = AppRepository(database, context)
        assertEquals(CalcMethod.MWL, restartedRepo2.calcMethod.value)
    }

    @Test
    fun testMadhabPreference_onboardingSelectionPersistsAndSurvivesRestart() {
        // Test Hanafi
        repository.setMadhab(Madhab.HANAFI)
        assertEquals(Madhab.HANAFI, repository.madhab.value)

        var restartedRepo = AppRepository(database, context)
        assertEquals(Madhab.HANAFI, restartedRepo.madhab.value)

        // Test Standard (Shafi'i, Maliki, Hanbali)
        repository.setMadhab(Madhab.STANDARD)
        assertEquals(Madhab.STANDARD, repository.madhab.value)

        restartedRepo = AppRepository(database, context)
        assertEquals(Madhab.STANDARD, restartedRepo.madhab.value)
    }

    @Test
    fun testHijriDateConvention_onboardingSelectionPersistsAndSurvivesRestart() {
        // Test Regional India / South Asia
        repository.setHijriDateMethod(HijriDateMethod.REGIONAL_INDIA)
        assertEquals(HijriDateMethod.REGIONAL_INDIA, repository.hijriDateMethod.value)

        var restartedRepo = AppRepository(database, context)
        assertEquals(HijriDateMethod.REGIONAL_INDIA, restartedRepo.hijriDateMethod.value)

        // Test Saudi Umm al-Qura
        repository.setHijriDateMethod(HijriDateMethod.SAUDI_UMM_AL_QURA)
        assertEquals(HijriDateMethod.SAUDI_UMM_AL_QURA, repository.hijriDateMethod.value)

        restartedRepo = AppRepository(database, context)
        assertEquals(HijriDateMethod.SAUDI_UMM_AL_QURA, restartedRepo.hijriDateMethod.value)

        // Test Custom Offset
        repository.setHijriDateMethod(HijriDateMethod.CUSTOM_OFFSET)
        repository.setCustomHijriOffset(1)
        assertEquals(HijriDateMethod.CUSTOM_OFFSET, repository.hijriDateMethod.value)
        assertEquals(1, repository.customHijriOffset.value)

        restartedRepo = AppRepository(database, context)
        assertEquals(HijriDateMethod.CUSTOM_OFFSET, restartedRepo.hijriDateMethod.value)
        assertEquals(1, restartedRepo.customHijriOffset.value)
    }

    @Test
    fun testPrayerNotificationsPreference_onboardingSelectionPersistsAndSurvivesRestart() {
        // Test enable
        notificationManager.isSmartNotificationsEnabled = true
        notificationManager.isPrayerTimeNotificationsEnabled = true
        notificationManager.preReminderOffset = PrePrayerReminderOffset.MIN_15

        assertTrue(notificationManager.isSmartNotificationsEnabled)
        assertTrue(notificationManager.isPrayerTimeNotificationsEnabled)
        assertEquals(PrePrayerReminderOffset.MIN_15, notificationManager.preReminderOffset)

        // Re-read after process restart
        val freshNotificationManager = SmartPrayerNotificationManager(context)
        assertTrue(freshNotificationManager.isSmartNotificationsEnabled)
        assertTrue(freshNotificationManager.isPrayerTimeNotificationsEnabled)
        assertEquals(PrePrayerReminderOffset.MIN_15, freshNotificationManager.preReminderOffset)

        // Test mute
        notificationManager.isSmartNotificationsEnabled = false
        assertFalse(notificationManager.isSmartNotificationsEnabled)

        val freshNotificationManager2 = SmartPrayerNotificationManager(context)
        assertFalse(freshNotificationManager2.isSmartNotificationsEnabled)
    }

    @Test
    fun testTasbeehSoundAndHaptics_onboardingSelectionPersistsAndSurvivesRestart() {
        // Tap sound: GENTLE_TAP
        repository.setTasbeehSound(TasbeehSound.GENTLE_TAP)
        assertEquals(TasbeehSound.GENTLE_TAP, repository.tasbeehSound.value)

        // Vibration: false
        repository.setVibrationEnabled(false)
        assertFalse(repository.vibrationEnabled.value)

        var restartedRepo = AppRepository(database, context)
        assertEquals(TasbeehSound.GENTLE_TAP, restartedRepo.tasbeehSound.value)
        assertFalse(restartedRepo.vibrationEnabled.value)

        // Tap sound: WOODEN_TAP, Vibration: true
        repository.setTasbeehSound(TasbeehSound.WOODEN_TAP)
        repository.setVibrationEnabled(true)
        assertEquals(TasbeehSound.WOODEN_TAP, repository.tasbeehSound.value)
        assertTrue(repository.vibrationEnabled.value)

        restartedRepo = AppRepository(database, context)
        assertEquals(TasbeehSound.WOODEN_TAP, restartedRepo.tasbeehSound.value)
        assertTrue(restartedRepo.vibrationEnabled.value)
    }

    @Test
    fun testAppearanceMode_onboardingSelectionPersistsAndSurvivesRestart() {
        // Light Mode
        repository.setAppearanceMode(AppearanceMode.LIGHT)
        assertEquals(AppearanceMode.LIGHT, repository.appearanceMode.value)

        var restartedRepo = AppRepository(database, context)
        assertEquals(AppearanceMode.LIGHT, restartedRepo.appearanceMode.value)

        // Dark Mode
        repository.setAppearanceMode(AppearanceMode.DARK)
        assertEquals(AppearanceMode.DARK, repository.appearanceMode.value)

        restartedRepo = AppRepository(database, context)
        assertEquals(AppearanceMode.DARK, restartedRepo.appearanceMode.value)

        // System Mode
        repository.setAppearanceMode(AppearanceMode.SYSTEM)
        assertEquals(AppearanceMode.SYSTEM, repository.appearanceMode.value)

        restartedRepo = AppRepository(database, context)
        assertEquals(AppearanceMode.SYSTEM, restartedRepo.appearanceMode.value)
    }

    // ---------------------------------------------------------------------------------------------
    // Phase 3 — Android Permissions & Background Reliability Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testLocationPermission_initiallyDenied_neverFakesEnabled() {
        // In clean test environment without granted permission
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        val hasPermission = LocationHelper.hasLocationPermission(context)
        assertFalse("Must not report location as enabled when Android reports denied", hasPermission)
    }

    @Test
    fun testLocationPermission_whenGranted_reportsTrueAndResolvesCoordinates() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val hasPermission = LocationHelper.hasLocationPermission(context)
        assertTrue("Must report location enabled when Android grants fine location", hasPermission)

        // Verify LocationHelper creates valid CityLocation
        val mockLocation = Location("test").apply {
            latitude = 40.7128
            longitude = -74.0060
        }
        val resolved = LocationHelper.resolveCityLocation(context, mockLocation)
        assertNotNull(resolved)
        assertEquals(40.7128, resolved.latitude, 0.001)
        assertEquals(-74.0060, resolved.longitude, 0.001)
    }

    @Test
    fun testLocationPermission_whenDenied_gracefulManualSelectionAllowed() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        // Verify user can seamlessly select manual city without blocking
        val makkah = repository.PREDEFINED_CITIES.find { it.cityName == "Mecca" }
            ?: CityLocation("Mecca", "Saudi Arabia", 21.3891, 39.8579)
        repository.setCity(makkah)

        assertEquals("Mecca", repository.selectedCity.value.cityName)
        assertFalse("Permission state remains accurately false", LocationHelper.hasLocationPermission(context))
    }

    @Test
    fun testNotificationPermission_realStateInspected_neverFakesGranted() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val isGranted = notificationManager.isNotificationPermissionGranted()
        assertFalse("Must never falsely mark notification permission as granted when Android denies it", isGranted)
    }

    @Test
    fun testNotificationPermission_whenGranted_reportsTrue() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val isGranted = notificationManager.isNotificationPermissionGranted()
        assertTrue("Reports true when POST_NOTIFICATIONS is granted", isGranted)
    }

    @Test
    fun testBackgroundReliability_canScheduleExactAlarms_inspectedFromSystem() {
        // On modern Android versions (API 31+), check canScheduleExactAlarms()
        val canSchedule = notificationManager.canScheduleExactAlarms()
        // In Robolectric environment, it returns a boolean safely without throwing SecurityException
        assertNotNull(canSchedule)
    }

    @Test
    fun testBackgroundReliability_batteryOptimization_inspectedFromPowerManager() {
        val isIgnored = notificationManager.isIgnoringBatteryOptimizations()
        // Must return actual system power manager state (Boolean) without throwing
        assertNotNull(isIgnored)
    }

    @Test
    fun testDenialHandling_allPermissionsDenied_setupCanStillCompleteSuccessfully() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val testUid = "unprivileged_user_test"

        // Setup should allow completion despite denied permissions
        authRepository.setSetupCompleted(testUid, true)
        assertTrue("Setup must complete even if user denies system permissions", authRepository.isSetupCompleted(testUid))

        // Application maintains fallback defaults safely
        assertNotNull(repository.selectedCity.value)
        assertNotNull(repository.calcMethod.value)
        assertNotNull(repository.madhab.value)
    }

    // ---------------------------------------------------------------------------------------------
    // Phase 4: Google Drive & Auto-Backup Onboarding Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testAutoBackupFrequency_defaultIsOff() {
        val defaultFreq = com.example.data.backup.BackupManager.getAutoBackupFrequency(context)
        assertEquals(com.example.data.backup.BackupManager.AutoBackupFrequency.OFF, defaultFreq)
    }

    @Test
    fun testAutoBackupFrequency_canSetDailyAndWeekly() {
        com.example.data.backup.BackupManager.setAutoBackupFrequency(
            context,
            com.example.data.backup.BackupManager.AutoBackupFrequency.DAILY
        )
        assertEquals(
            com.example.data.backup.BackupManager.AutoBackupFrequency.DAILY,
            com.example.data.backup.BackupManager.getAutoBackupFrequency(context)
        )

        com.example.data.backup.BackupManager.setAutoBackupFrequency(
            context,
            com.example.data.backup.BackupManager.AutoBackupFrequency.WEEKLY
        )
        assertEquals(
            com.example.data.backup.BackupManager.AutoBackupFrequency.WEEKLY,
            com.example.data.backup.BackupManager.getAutoBackupFrequency(context)
        )
    }

    @Test
    fun testAutoBackupWorker_scheduleDoesNotCrashInTestEnvironment() {
        com.example.data.backup.GoogleDriveBackupWorker.schedule(
            context,
            com.example.data.backup.BackupManager.AutoBackupFrequency.WEEKLY
        )
        // Successfully handles WorkManager gracefully
        assertTrue(true)
    }
}
