package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthRepository
import com.example.data.db.AppDatabase
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.Madhab
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.repository.AppRepository
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
import org.robolectric.shadows.ShadowApplication
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
        val shadowApp = shadowOf(app)
        shadowApp.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

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
        val shadowApp = shadowOf(app)
        shadowApp.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val isGranted = checkLocationPermission(context)
        assertTrue("Location permission should be reported as true when granted", isGranted)
    }

    // ---------------------------------------------------------------------------------------------
    // Test C & D: Notifications Granted & Denied States
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testNotificationPermission_deniedState_functionsNormallyWithoutCrash() {
        val shadowApp = shadowOf(app)
        shadowApp.denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

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
        val shadowApp = shadowOf(app)
        shadowApp.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val isGranted = checkNotificationPermission(context)
        assertTrue("Notification permission should reflect actual granted state", isGranted)

        // User enables smart prayer alerts
        notificationManager.isSmartNotificationsEnabled = true
        notificationManager.isPrayerTimeNotificationsEnabled = true

        assertTrue(notificationManager.isSmartNotificationsEnabled)
        assertTrue(notificationManager.isPrayerTimeNotificationsEnabled)
    }

    // ---------------------------------------------------------------------------------------------
    // Test E & F: Background Battery Optimization Exemption States
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBackgroundOptimization_notEnabledState() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager: ShadowPowerManager = shadowOf(powerManager)
        shadowPowerManager.setIgnoringBatteryOptimizations(context.packageName, false)

        val isExempt = checkBackgroundOptimization(context)
        assertFalse("Background reliability must not report true when app is optimized", isExempt)
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
        val shadowApp = shadowOf(app)
        shadowApp.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        shadowApp.denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager: ShadowPowerManager = shadowOf(powerManager)
        shadowPowerManager.setIgnoringBatteryOptimizations(context.packageName, false)

        // All system capabilities are denied / not enabled
        assertFalse(checkLocationPermission(context))
        assertFalse(checkNotificationPermission(context))
        assertFalse(checkBackgroundOptimization(context))

        // Manual fallback: Default or manually chosen city
        val fallbackCity = repository.selectedCity.value
        assertNotNull(fallbackCity)

        // User finishes onboarding
        val uid = "user_who_denied_permissions"
        authRepository.setSetupCompleted(uid, true)

        assertTrue("User must be able to complete setup even when all permissions are denied", authRepository.isSetupCompleted(uid))
    }
}
