package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.util.QuranData
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QuranDataTest {

    @Test
    fun testQuranDataLoading() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fatihah = QuranData.getVersesForSurah(context, 1)
        assertNotNull(fatihah)
        assertEquals(7, fatihah.size)
        assertEquals(1, fatihah.first().verseNumber)
        assertTrue(fatihah.first().textArabic.isNotEmpty())
    }

    @Test
    fun testAuthoritativeIdentityLookup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Al-Kafirun 109:1
        val v109_1 = QuranData.getVerseByKey(context, "109:1")
        assertNotNull(v109_1)
        assertEquals(109, v109_1?.surahNumber)
        assertEquals(1, v109_1?.verseNumber)
        assertEquals("109:1", v109_1?.verseKey)
        assertEquals("109:1", v109_1?.identity)
        assertTrue(v109_1?.textEnglish?.contains("disbelievers", ignoreCase = true) == true)

        // 2. An-Nas 114:1
        val v114_1 = QuranData.getVerseByKey(context, "114:1")
        assertNotNull(v114_1)
        assertEquals(114, v114_1?.surahNumber)
        assertEquals(1, v114_1?.verseNumber)
        assertEquals("114:1", v114_1?.verseKey)
        assertTrue(v114_1?.textEnglish?.contains("seek refuge in the Lord of mankind", ignoreCase = true) == true)

        // 3. Al-Jumu'ah 62:1
        val v62_1 = QuranData.getVerseByKey(context, "62:1")
        assertNotNull(v62_1)
        assertEquals(62, v62_1?.surahNumber)
        assertEquals(1, v62_1?.verseNumber)
        assertEquals("62:1", v62_1?.verseKey)
        assertTrue(v62_1?.textEnglish?.contains("exalting Allah", ignoreCase = true) == true)

        // 4. Authoritative translation lookup by key
        val t109_1 = QuranData.getTranslationByKey(context, "109:1")
        assertEquals(v109_1?.textEnglish, t109_1)

        val t62_1 = QuranData.getTranslationByKey(context, "62:1")
        assertEquals(v62_1?.textEnglish, t62_1)
    }

    @Test
    fun testMissingKeyNeverFallsBackToBismillah() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Non-existent verses should resolve to null, NEVER Bismillah
        assertNull(QuranData.getVerseByKey(context, "109:999"))
        assertNull(QuranData.getTranslationByKey(context, "109:999"))
        assertNull(QuranData.getVerseByKey(context, "999:1"))
        assertNull(QuranData.getTranslationByKey(context, "999:1"))
        assertNull(QuranData.getVerseByKey(context, "invalid_key"))
        assertNull(QuranData.getTranslationByKey(context, "invalid_key"))
    }

    @Test
    fun testBismillahIsSeparateMetadata() {
        // Surah 1 (Al-Fatihah) has Ayah 1 as Bismillah
        val bismillahSurah2 = QuranData.getSurahBismillah(2)
        assertNotNull(bismillahSurah2)
        assertEquals(2, bismillahSurah2?.surahNumber)
        assertEquals(0, bismillahSurah2?.verseNumber)
        assertEquals("2:0", bismillahSurah2?.verseKey)
        assertTrue(bismillahSurah2?.isBismillahHeader == true)

        // Surah 9 (At-Tawbah) has NO Bismillah
        val bismillahSurah9 = QuranData.getSurahBismillah(9)
        assertNull(bismillahSurah9)

        // Surah 114 (An-Nas)
        val bismillahSurah114 = QuranData.getSurahBismillah(114)
        assertNotNull(bismillahSurah114)
        assertEquals(114, bismillahSurah114?.surahNumber)
        assertEquals(0, bismillahSurah114?.verseNumber)
    }

    @Test
    fun testGlobalIntegrityValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val report = QuranData.validateIntegrity(context)

        assertTrue(report.summary, report.isValid)
        assertEquals(114, report.totalSurahsChecked)
        assertEquals(6236, report.totalVersesChecked)
        assertTrue("Issues found: ${report.issues}", report.issues.isEmpty())
    }

    @Test
    fun testLegitimateDuplicateTranslationsAreValid() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Surah Ar-Rahman (55) has the repeated refrain:
        // "So which of the favors of your Lord would you deny?"
        val refrainTranslation = QuranData.getTranslation(context, 55, 13)
        assertNotNull(refrainTranslation)
        assertTrue(refrainTranslation!!.contains("which of the favors of your Lord", ignoreCase = true))

        // Check subsequent occurrences in Surah Ar-Rahman
        val refrainVerse16 = QuranData.getTranslation(context, 55, 16)
        val refrainVerse18 = QuranData.getTranslation(context, 55, 18)
        val refrainVerse21 = QuranData.getTranslation(context, 55, 21)

        assertEquals(refrainTranslation, refrainVerse16)
        assertEquals(refrainTranslation, refrainVerse18)
        assertEquals(refrainTranslation, refrainVerse21)

        // Confirm identity keys remain completely distinct
        assertEquals("55:13", QuranData.getVerse(context, 55, 13)?.verseKey)
        assertEquals("55:16", QuranData.getVerse(context, 55, 16)?.verseKey)
        assertEquals("55:18", QuranData.getVerse(context, 55, 18)?.verseKey)
        assertEquals("55:21", QuranData.getVerse(context, 55, 21)?.verseKey)
    }
}





