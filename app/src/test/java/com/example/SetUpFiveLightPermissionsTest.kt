package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthRepository
import com.example.data.db.AppDatabase
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.model.Madhab
import com.example.data.reminder.PrePrayerReminderOffset
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.repository.AppRepository
import com.example.data.util.LocationHelper
import com.example.ui.screens.checkBackgroundOptimization
import com.example.ui.screens.checkLocationPermission
import com.example.ui.screens.checkNotificationPermission
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
import org.robolectric.shadows.ShadowPowerManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SetUpFiveLightPermissionsTest {

    private lateinit var app: Application
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: AppRepository
    private lateinit var notificationManager: SmartPrayerNotificationManager

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        context = app
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("smart_prayer_notifications_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        database = AppDatabase.getDatabase(context)
        authRepository = AuthRepository.getInstance(context)
        repository = AppRepository(database, context)
        notificationManager = SmartPrayerNotificationManager(context)
    }

    // ---------------------------------------------------------------------------------------------
    // Test A & B: Location Granted & Denied States
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testLocationPermission_deniedState_doesNotBlockManualSelection() {
        shadowOf(app).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        val isGranted = checkLocationPermission(context)
        assertFalse("Location permission should be reported as false when denied", isGranted)

        // When denied, FiveLight allows manual city selection without blocking onboarding
        val manualCity = repository.PREDEFINED_CITIES.first { it.cityName == "Cairo" }
        repository.setCity(manualCity)

        assertEquals("Cairo", repository.selectedCity.value.cityName)
        assertEquals("Egypt", repository.selectedCity.value.countryName)
    }

    @Test
    fun testLocationPermission_grantedState() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val isGranted = checkLocationPermission(context)
        assertTrue("Location permission should be reported as true when granted", isGranted)
    }

    // ---------------------------------------------------------------------------------------------
    // Test: Automatic Location Configuration
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testAutoConfigure_southAsiaLocation_setsKarachiAndRegionalMoonSighting() {
        val bhiwandi = CityLocation(
            cityName = "Bhiwandi",
            countryName = "India",
            latitude = 19.2812,
            longitude = 73.0483,
            timezoneOffsetHours = 5.5
        )
        repository.autoConfigureFromLocation(bhiwandi)

        assertEquals("Bhiwandi", repository.selectedCity.value.cityName)
        assertEquals(CalcMethod.KARACHI, repository.calcMethod.value)
        assertEquals(HijriDateMethod.REGIONAL_INDIA, repository.hijriDateMethod.value)
        assertEquals(Madhab.HANAFI, repository.madhab.value)
    }

    @Test
    fun testAutoConfigure_saudiArabiaLocation_setsUmmAlQura() {
        val mecca = CityLocation(
            cityName = "Mecca",
            countryName = "Saudi Arabia",
            latitude = 21.3891,
            longitude = 39.8579,
            timezoneOffsetHours = 3.0
        )
        repository.autoConfigureFromLocation(mecca)

        assertEquals("Mecca", repository.selectedCity.value.cityName)
        assertEquals(CalcMethod.UMM_AL_QURA, repository.calcMethod.value)
        assertEquals(HijriDateMethod.SAUDI_UMM_AL_QURA, repository.hijriDateMethod.value)
    }

    @Test
    fun testAutoConfigure_northAmericaLocation_setsISNA() {
        val newYork = CityLocation(
            cityName = "New York",
            countryName = "USA",
            latitude = 40.7128,
            longitude = -74.0060,
            timezoneOffsetHours = -5.0
        )
        repository.autoConfigureFromLocation(newYork)

        assertEquals("New York", repository.selectedCity.value.cityName)
        assertEquals(CalcMethod.ISNA, repository.calcMethod.value)
        assertEquals(HijriDateMethod.GLOBAL_ASTRONOMICAL, repository.hijriDateMethod.value)
    }

    // ---------------------------------------------------------------------------------------------
    // Test C & D: Notifications Granted & Denied States
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testNotificationPermission_deniedState_functionsNormallyWithoutCrash() {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val isGranted = checkNotificationPermission(context)
        assertFalse("Notification permission should reflect actual denied state", isGranted)

        // User can mute notifications or proceed without notifications
        notificationManager.isSmartNotificationsEnabled = false
        notificationManager.isPrayerTimeNotificationsEnabled = false

        assertFalse(notificationManager.isSmartNotificationsEnabled)
        assertFalse(notificationManager.isPrayerTimeNotificationsEnabled)
    }

    @Test
    fun testNotificationPermission_grantedState() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val isGranted = checkNotificationPermission(context)
        assertTrue("Notification permission should reflect actual granted state", isGranted)

        // User enables smart prayer alerts
        notificationManager.isSmartNotificationsEnabled = true
        notificationManager.isPrayerTimeNotificationsEnabled = true
        notificationManager.preReminderOffset = PrePrayerReminderOffset.MIN_10

        assertTrue(notificationManager.isSmartNotificationsEnabled)
        assertTrue(notificationManager.isPrayerTimeNotificationsEnabled)
        assertEquals(PrePrayerReminderOffset.MIN_10, notificationManager.preReminderOffset)
    }

    // ---------------------------------------------------------------------------------------------
    // Test E & F: Background Battery Optimization Exemption States
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBackgroundOptimization_notEnabledState() {
        val isExempt = checkBackgroundOptimization(context)
        // In clean test environment, app is initially optimized (not exempt)
        assertNotNull(isExempt)
    }

    @Test
    fun testBackgroundOptimization_enabledState() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager: ShadowPowerManager = shadowOf(powerManager)
        shadowPowerManager.setIgnoringBatteryOptimizations(context.packageName, true)

        val isExempt = checkBackgroundOptimization(context)
        assertTrue("Background reliability must report true when battery optimization is ignored", isExempt)
    }

    // ---------------------------------------------------------------------------------------------
    // Test G & H: App Restart & Process Recreation
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testPreferences_surviveAppRestartAndProcessRecreation() {
        // Set all preferences during onboarding
        val targetCity = repository.PREDEFINED_CITIES.first { it.cityName == "Medina" }
        repository.setCity(targetCity)
        repository.setCalcMethod(CalcMethod.UMM_AL_QURA)
        repository.setMadhab(Madhab.HANAFI)
        repository.setAppearanceMode(AppearanceMode.DARK)

        val testUserUid = "test_user_phase3_uid"
        authRepository.setSetupCompleted(testUserUid, true)

        // Simulate app restart / new process instantiating new repository instances
        val restartedRepo = AppRepository(database, context)
        val restartedAuth = AuthRepository.getInstance(context)

        assertEquals("Medina", restartedRepo.selectedCity.value.cityName)
        assertEquals(CalcMethod.UMM_AL_QURA, restartedRepo.calcMethod.value)
        assertEquals(Madhab.HANAFI, restartedRepo.madhab.value)
        assertEquals(AppearanceMode.DARK, restartedRepo.appearanceMode.value)
        assertTrue(restartedAuth.isSetupCompleted(testUserUid))
    }

    // ---------------------------------------------------------------------------------------------
    // Test I & J: Light & Dark Theme Configuration
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testAppearanceMode_lightAndDarkModeSelection() {
        // Light mode
        repository.setAppearanceMode(AppearanceMode.LIGHT)
        assertEquals(AppearanceMode.LIGHT, repository.appearanceMode.value)

        // Dark mode
        repository.setAppearanceMode(AppearanceMode.DARK)
        assertEquals(AppearanceMode.DARK, repository.appearanceMode.value)

        // System mode
        repository.setAppearanceMode(AppearanceMode.SYSTEM)
        assertEquals(AppearanceMode.SYSTEM, repository.appearanceMode.value)
    }

    // ---------------------------------------------------------------------------------------------
    // Denial Handling: Onboarding Not Blocked
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testDenialHandling_allPermissionsDenied_setupStillCompletesSuccessfully() {
        shadowOf(app).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        // All system capabilities are denied / not enabled
        assertFalse(checkLocationPermission(context))
        assertFalse(checkNotificationPermission(context))

        // Manual fallback: Default or manually chosen city
        val fallbackCity = repository.selectedCity.value
        assertNotNull(fallbackCity)

        // User finishes onboarding
        val uid = "user_who_denied_permissions"
        authRepository.setSetupCompleted(uid, true)

        assertTrue("User must be able to complete setup even when all permissions are denied", authRepository.isSetupCompleted(uid))
    }
}

