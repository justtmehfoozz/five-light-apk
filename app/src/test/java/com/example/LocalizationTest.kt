package com.example

import android.content.Context
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.repository.AppRepository
import com.example.data.util.AppLocaleManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalizationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs before test
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun testAppLanguageModelStructure() {
        // Verify all 4 authoritative languages exist
        val languages = AppLanguage.entries
        assertEquals(4, languages.size)

        // 1. English
        val english = AppLanguage.ENGLISH
        assertEquals("en", english.id)
        assertEquals("en", english.languageTag)
        assertEquals("English", english.displayName)
        assertEquals(LayoutDirection.Ltr, english.layoutDirection)
        assertFalse(english.isRtl)

        // 2. Hindi-script Urdu
        val hindiUrdu = AppLanguage.HINDI_URDU
        assertEquals("ur_Deva", hindiUrdu.id)
        assertEquals("ur-Deva", hindiUrdu.languageTag)
        assertEquals("हिंदी", hindiUrdu.displayName)
        assertEquals(LayoutDirection.Ltr, hindiUrdu.layoutDirection)
        assertFalse(hindiUrdu.isRtl)

        // 3. Urdu (RTL)
        val urdu = AppLanguage.URDU
        assertEquals("ur_Arab", urdu.id)
        assertEquals("ur-Arab", urdu.languageTag)
        assertEquals("اردو", urdu.displayName)
        assertEquals(LayoutDirection.Rtl, urdu.layoutDirection)
        assertTrue(urdu.isRtl)

        // 4. Roman Urdu
        val romanUrdu = AppLanguage.ROMAN_URDU
        assertEquals("ur_Latn", romanUrdu.id)
        assertEquals("ur-Latn", romanUrdu.languageTag)
        assertEquals("Roman Urdu", romanUrdu.displayName)
        assertEquals(LayoutDirection.Ltr, romanUrdu.layoutDirection)
        assertFalse(romanUrdu.isRtl)
    }

    @Test
    fun testAppLanguageLookupAndFallback() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromId("en"))
        assertEquals(AppLanguage.HINDI_URDU, AppLanguage.fromId("ur_Deva"))
        assertEquals(AppLanguage.URDU, AppLanguage.fromId("ur_Arab"))
        assertEquals(AppLanguage.ROMAN_URDU, AppLanguage.fromId("ur-Latn"))

        // Fallback for null/unknown ID is English
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromId(null))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromId(""))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromId("unknown_lang"))
    }

    @Test
    fun testLanguagePersistence() {
        // Default when nothing saved
        assertEquals(AppLanguage.ENGLISH, AppLocaleManager.getPersistedLanguage(context))

        // Set Urdu
        AppLocaleManager.setPersistedLanguage(context, AppLanguage.URDU)
        assertEquals(AppLanguage.URDU, AppLocaleManager.getPersistedLanguage(context))

        // Set Hindi-script Urdu
        AppLocaleManager.setPersistedLanguage(context, AppLanguage.HINDI_URDU)
        assertEquals(AppLanguage.HINDI_URDU, AppLocaleManager.getPersistedLanguage(context))

        // Set Roman Urdu
        AppLocaleManager.setPersistedLanguage(context, AppLanguage.ROMAN_URDU)
        assertEquals(AppLanguage.ROMAN_URDU, AppLocaleManager.getPersistedLanguage(context))

        // Set English
        AppLocaleManager.setPersistedLanguage(context, AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, AppLocaleManager.getPersistedLanguage(context))
    }

    @Test
    fun testRepositoryLanguageIntegration() {
        val db = AppDatabase.getDatabase(context)
        val repository = AppRepository(db, context)

        assertEquals(AppLanguage.ENGLISH, repository.appLanguage.value)

        repository.setAppLanguage(AppLanguage.URDU)
        assertEquals(AppLanguage.URDU, repository.appLanguage.value)
        assertEquals(AppLanguage.URDU, AppLocaleManager.getPersistedLanguage(context))

        // Simulate app restart with new repository instance reading persisted prefs
        val newRepoInstance = AppRepository(db, context)
        assertEquals(AppLanguage.URDU, newRepoInstance.appLanguage.value)
    }

    @Test
    fun testAppLocaleManagerContextCreation() {
        // Test context creation for each language
        AppLanguage.entries.forEach { lang ->
            val localizedCtx = AppLocaleManager.createLocalizedContext(context, lang)
            assertNotNull(localizedCtx)
            val config = localizedCtx.resources.configuration
            assertEquals(lang.toLocale().language, config.locales[0].language)
        }
    }

    @Test
    fun testStringResourcesForAllFourLanguages() {
        // 1. English
        val enCtx = AppLocaleManager.createLocalizedContext(context, AppLanguage.ENGLISH)
        assertEquals("Settings", enCtx.getString(R.string.settings_title))
        assertEquals("Language", enCtx.getString(R.string.settings_language))
        assertEquals("GENERAL", enCtx.getString(R.string.settings_general))
        assertEquals("PRAYER", enCtx.getString(R.string.settings_prayer))

        // 2. Hindi-script Urdu (ur-Deva)
        val hiUrCtx = AppLocaleManager.createLocalizedContext(context, AppLanguage.HINDI_URDU)
        assertEquals("सेटिंग्स", hiUrCtx.getString(R.string.settings_title))
        assertEquals("ज़बान", hiUrCtx.getString(R.string.settings_language))
        assertEquals("आम", hiUrCtx.getString(R.string.settings_general))
        assertEquals("नमाज़", hiUrCtx.getString(R.string.settings_prayer))

        // 3. Urdu (ur-Arab)
        val urCtx = AppLocaleManager.createLocalizedContext(context, AppLanguage.URDU)
        assertEquals("سیٹنگز", urCtx.getString(R.string.settings_title))
        assertEquals("زبان", urCtx.getString(R.string.settings_language))
        assertEquals("عام", urCtx.getString(R.string.settings_general))
        assertEquals("نماز", urCtx.getString(R.string.settings_prayer))

        // 4. Roman Urdu (ur-Latn)
        val roUrCtx = AppLocaleManager.createLocalizedContext(context, AppLanguage.ROMAN_URDU)
        assertEquals("Settings", roUrCtx.getString(R.string.settings_title))
        assertEquals("Zuban", roUrCtx.getString(R.string.settings_language))
        assertEquals("Aam", roUrCtx.getString(R.string.settings_general))
        assertEquals("Namaz", roUrCtx.getString(R.string.settings_prayer))
    }
}
