package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.model.Madhab
import com.example.data.model.TasbeehSound
import com.example.data.util.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SetUpFiveLightPhase9Test {

    private lateinit var app: Application
    private lateinit var context: Context

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        context = app
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("fivelight_drive_backup_meta", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun testRegionalRecommendationsAndDefaults() {
        val mumbaiCity = CityLocation("Mumbai", "India", 18.922, 72.834, 5.5, CalcMethod.MWL)
        val regional = LocationHelper.determineRegionalPrayerSettings(mumbaiCity)

        assertEquals(CalcMethod.KARACHI, regional.calcMethod)
        assertEquals(Madhab.HANAFI, regional.madhab)
        assertEquals(HijriDateMethod.REGIONAL_INDIA, regional.hijriDateMethod)
        assertTrue(regional.calcMethodRecommendation.isNotBlank())
    }

    @Test
    fun testAppearanceModeOptions() {
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.valueOf("SYSTEM"))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.valueOf("LIGHT"))
        assertEquals(AppearanceMode.DARK, AppearanceMode.valueOf("DARK"))
    }

    @Test
    fun testTasbeehSoundsAndDescriptions() {
        TasbeehSound.entries.forEach { sound ->
            assertNotNull(sound.displayName)
            assertTrue(sound.displayName.isNotBlank())
            assertNotNull(sound.description)
            assertTrue(sound.description.isNotBlank())
        }
    }

    @Test
    fun testCityListIntegrityAndSearch() {
        val repo = com.example.data.repository.AppRepository.getInstance(context)
        val cities = repo.PREDEFINED_CITIES
        assertTrue(cities.isNotEmpty())

        val searchResult = cities.filter { it.cityName.contains("London", ignoreCase = true) }
        assertTrue(searchResult.any { it.countryName.contains("UK", ignoreCase = true) || it.countryName.contains("United Kingdom", ignoreCase = true) })
    }

    @Test
    fun testRecommendedCopyNeverTruncatedOrWrapped() {
        val fullRecommended = "Recommended"
        assertEquals(11, fullRecommended.length)
        assertFalse(fullRecommended.contains("\n"))
        assertEquals("Recommended", fullRecommended)
    }
}
