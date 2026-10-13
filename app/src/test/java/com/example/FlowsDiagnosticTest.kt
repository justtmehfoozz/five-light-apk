package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthRepository
import com.example.data.db.AppDatabase
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.repository.AppRepository
import com.example.data.util.HijriCalc
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PreLoginPromptScreen
import com.example.ui.screens.RegisterScreen
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FlowsDiagnosticTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        database = AppDatabase.getDatabase(context)
        authRepository = AuthRepository.getInstance(context)
        repository = AppRepository(database, context)
    }

    @Test
    fun testPreLoginPromptScreen_rendersWithoutCrash() {
        var guestClicked = false
        var loginClicked = false

        composeTestRule.setContent {
            PreLoginPromptScreen(
                onLoginOrRegister = { loginClicked = true },
                onContinueAsGuest = { guestClicked = true }
            )
        }

        composeTestRule.waitForIdle()
        assertNotNull(authRepository)
    }

    @Test
    fun testRegisterScreen_rendersWithoutCrash() {
        composeTestRule.setContent {
            RegisterScreen(
                authRepository = authRepository,
                onBack = {},
                onRegistrationSuccess = {}
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testHomeScreen_emptyPrayerTimes_rendersWithoutCrash() {
        composeTestRule.setContent {
            HomeScreen(
                nextPrayer = null,
                prayerTimes = emptyList(),
                countdownFormatted = androidx.compose.runtime.mutableStateOf("00:00:00"),
                selectedCity = repository.selectedCity.value,
                hijriDate = HijriCalc.getHijriDate(),
                todayLog = null,
                onTogglePrayer = {},
                onQuickAccessNavigate = {},
                onOpenSettings = {}
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testHomeScreen_populatedPrayerTimes_rendersWithoutCrash() {
        val times = repository.getTodayPrayerTimes()
        composeTestRule.setContent {
            HomeScreen(
                nextPrayer = times.firstOrNull { it.isNext },
                prayerTimes = times,
                countdownFormatted = androidx.compose.runtime.mutableStateOf("01:23:45"),
                selectedCity = repository.selectedCity.value,
                hijriDate = HijriCalc.getHijriDate(),
                todayLog = null,
                onTogglePrayer = {},
                onQuickAccessNavigate = {},
                onOpenSettings = {}
            )
        }

        composeTestRule.waitForIdle()
    }
}
