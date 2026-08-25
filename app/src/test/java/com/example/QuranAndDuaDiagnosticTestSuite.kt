package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Surah
import com.example.data.util.DuaData
import com.example.data.util.QuranData
import com.example.ui.screens.allDuas
import com.example.ui.screens.categoryList
import com.example.ui.screens.toDuaEntity
import com.example.ui.screens.toDuaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprehensive Diagnostic Test Suite for Quran (all 114 Surahs) and Dua Library (all categories and items).
 * Programmatically validates that all metadata, verse texts (Arabic & English), audio URLs,
 * and Dua category content fields load successfully without errors.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuranAndDuaDiagnosticTestSuite {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testAll114SurahsMetadataIntegrity() {
        val directory = QuranData.SURAHS_DIRECTORY
        assertEquals("SURAHS_DIRECTORY must contain exactly 114 Surahs", 114, directory.size)

        var totalVersesSum = 0

        directory.forEachIndexed { index, surah ->
            val expectedSurahNumber = index + 1
            assertEquals(
                "Surah number should be sequential (expected $expectedSurahNumber)",
                expectedSurahNumber,
                surah.number
            )

            // Validate English Name
            assertTrue(
                "Surah $expectedSurahNumber (${surah.nameEnglish}) has empty English name",
                surah.nameEnglish.isNotBlank()
            )

            // Validate Arabic Name
            assertTrue(
                "Surah $expectedSurahNumber (${surah.nameEnglish}) has empty Arabic name",
                surah.nameArabic.isNotBlank()
            )

            // Validate English Translation / Meaning
            assertTrue(
                "Surah $expectedSurahNumber (${surah.nameEnglish}) has empty English translation",
                surah.englishTranslation.isNotBlank()
            )

            // Validate Verses Count
            assertTrue(
                "Surah $expectedSurahNumber (${surah.nameEnglish}) must have > 0 verses (was ${surah.versesCount})",
                surah.versesCount > 0
            )

            // Validate Revelation Place (Meccan or Medinan)
            assertTrue(
                "Surah $expectedSurahNumber (${surah.nameEnglish}) has invalid revelation place: ${surah.revelationPlace}",
                surah.revelationPlace == "Meccan" || surah.revelationPlace == "Medinan"
            )

            totalVersesSum += surah.versesCount
        }

        // The Quran contains 6,236 total standard ayat
        assertEquals(
            "Total verses count across all 114 Surahs must equal 6236",
            6236,
            totalVersesSum
        )
    }

    @Test
    fun testAll114SurahsVersesLoadingAndFieldIntegrity() {
        var totalLoadedVerses = 0

        for (surahId in 1..114) {
            val surahMeta = QuranData.SURAHS_DIRECTORY.find { it.number == surahId }
            assertNotNull("Metadata for Surah #$surahId must exist", surahMeta)

            val verses = QuranData.getVersesForSurah(context, surahId)
            assertNotNull("Verses list for Surah #$surahId (${surahMeta?.nameEnglish}) must not be null", verses)
            assertFalse("Verses for Surah #$surahId (${surahMeta?.nameEnglish}) must not be empty", verses.isEmpty())

            assertEquals(
                "Verses count for Surah #$surahId (${surahMeta?.nameEnglish}) must match metadata versesCount",
                surahMeta!!.versesCount,
                verses.size
            )

            verses.forEachIndexed { vIndex, verse ->
                val expectedVerseNumber = vIndex + 1

                assertEquals(
                    "Verse surahNumber mismatch in Surah #$surahId at index $vIndex",
                    surahId,
                    verse.surahNumber
                )
                assertEquals(
                    "Verse number mismatch in Surah #$surahId at index $vIndex",
                    expectedVerseNumber,
                    verse.verseNumber
                )
                assertTrue(
                    "Arabic text missing in Surah #$surahId Verse #$expectedVerseNumber",
                    verse.textArabic.isNotBlank()
                )
                assertTrue(
                    "English translation missing in Surah #$surahId Verse #$expectedVerseNumber",
                    verse.textEnglish.isNotBlank()
                )
                assertTrue(
                    "Audio URL missing in Surah #$surahId Verse #$expectedVerseNumber",
                    verse.audioUrl.isNotBlank()
                )

                // Verify that Bismillah translation is NOT erroneously assigned to non-Bismillah verses
                if (surahId != 1 && surahId != 27) {
                    assertFalse(
                        "Surah $surahId Verse $expectedVerseNumber has incorrect Bismillah translation",
                        verse.textEnglish.startsWith("In the name of Allah, the Entirely Merciful")
                    )
                }
            }

            totalLoadedVerses += verses.size
        }

        assertEquals("Total loaded verses from assets must equal 6236", 6236, totalLoadedVerses)
    }

    @Test
    fun testSpecificSurahsTranslationsIntegrity() {
        // Test Al-Jumu'ah (Surah 62)
        val jumuah = QuranData.getVersesForSurah(context, 62)
        assertEquals(11, jumuah.size)
        assertTrue(jumuah[0].textEnglish.contains("exalting Allah", ignoreCase = true))
        assertTrue(jumuah[1].textEnglish.contains("unlettered", ignoreCase = true))

        // Test Al-Kafirun (Surah 109)
        val kafirun = QuranData.getVersesForSurah(context, 109)
        assertEquals(6, kafirun.size)
        assertTrue(kafirun[0].textEnglish.contains("disbelievers", ignoreCase = true))
        assertTrue(kafirun[1].textEnglish.contains("do not worship", ignoreCase = true))

        // Test An-Nas (Surah 114)
        val nas = QuranData.getVersesForSurah(context, 114)
        assertEquals(6, nas.size)
        assertTrue(nas[0].textEnglish.contains("seek refuge in the Lord of mankind", ignoreCase = true))
        assertTrue(nas[1].textEnglish.contains("Sovereign of mankind", ignoreCase = true))

        // Test Al-Baqarah (Surah 2)
        val baqarah = QuranData.getVersesForSurah(context, 2)
        assertEquals(286, baqarah.size)
        assertEquals("Alif, Lam, Meem.", baqarah[0].textEnglish)
        assertTrue(baqarah[1].textEnglish.contains("no doubt", ignoreCase = true))
    }

    @Test
    fun testComprehensiveBismillahRegressionAcrossMultipleSurahs() {
        val testSurahNumbers = listOf(2, 3, 18, 36, 55, 62, 67, 109, 112, 113, 114)
        val bismillahTranslation = QuranData.BISMILLAH_ENGLISH

        for (sId in testSurahNumbers) {
            val verses = QuranData.getVersesForSurah(context, sId)
            assertTrue("Surah $sId must have verses", verses.isNotEmpty())

            for (v in verses) {
                // Verify Ayah 1, 2, 3, 4 etc. do NOT have Bismillah as translation
                assertNotEquals(
                    "Surah $sId Ayah ${v.verseNumber} must NOT have Bismillah translation",
                    bismillahTranslation,
                    v.textEnglish
                )
                assertFalse(
                    "Surah $sId Ayah ${v.verseNumber} translation must not start with Bismillah prefix",
                    v.textEnglish.startsWith("In the name of Allah, the Entirely Merciful", ignoreCase = true)
                )
            }
        }
    }

    @Test
    fun testRapidSurahSwitchingAndCacheIntegrity() {
        val randomSurahSequence = listOf(114, 1, 109, 62, 2, 18, 114, 62, 109, 1)

        for (sNum in randomSurahSequence) {
            val verses = QuranData.getVersesForSurah(context, sNum)
            val meta = QuranData.SURAHS_DIRECTORY.find { it.number == sNum }
            assertNotNull(meta)
            assertEquals(meta!!.versesCount, verses.size)

            // Verify identity of first and last verse in each retrieved list
            val first = verses.first()
            val last = verses.last()
            assertEquals("$sNum:1", first.verseKey)
            assertEquals("$sNum:${meta.versesCount}", last.verseKey)
            assertEquals(sNum, first.surahNumber)
            assertEquals(sNum, last.surahNumber)
        }
    }

    @Test
    fun testAuthoritativeIdentityMapConsistency() {
        val report = QuranData.validateIntegrity(context)
        assertTrue("Data integrity report must be valid: ${report.summary}", report.isValid)
        assertEquals(114, report.totalSurahsChecked)
        assertEquals(6236, report.totalVersesChecked)
    }

    @Test
    fun testQuranLensAnalysisForVerses() {
        val sampleSurahsToTest = listOf(1, 2, 18, 36, 55, 67, 112, 114)
        for (sId in sampleSurahsToTest) {
            val lensInfo = QuranData.getQuranLensInfoForVerse(context, sId, 1)
            assertNotNull("QuranLensInfo for Surah $sId:1 must not be null", lensInfo)
            assertTrue("QuranLensInfo arabic word should not be blank", lensInfo.arabicWordOrPhrase.isNotBlank())
            assertTrue("QuranLensInfo transliteration should not be blank", lensInfo.transliteration.isNotBlank())
            assertTrue("QuranLensInfo meaning should not be blank", lensInfo.meaning.isNotBlank())
            assertNotNull("Occurrences list must not be null", lensInfo.occurrences)
        }
    }

    @Test
    fun testAllDuaCategoriesIntegrity() {
        assertTrue("Dua categoryList must not be empty", categoryList.isNotEmpty())
        assertEquals("Expected 12 predefined Dua categories", DuaData.CATEGORIES.size, categoryList.size)

        categoryList.forEach { category ->
            assertTrue(
                "Category title must not be blank",
                category.title.isNotBlank()
            )
            assertTrue(
                "Category description for '${category.title}' must not be blank",
                category.description.isNotBlank()
            )
            assertNotNull(
                "Category icon for '${category.title}' must not be null",
                category.icon
            )
        }
    }

    @Test
    fun testAllDuasContentAndCategoryAssociation() {
        assertTrue("allDuas dataset must not be empty", allDuas.isNotEmpty())

        val categoryTitles = categoryList.map { it.title }.toSet()
        val testedCategories = mutableSetOf<String>()

        allDuas.forEachIndexed { index, dua ->
            // Validate ID
            assertTrue(
                "Dua at index $index has blank ID",
                dua.id.isNotBlank()
            )

            // Validate Title
            assertTrue(
                "Dua '${dua.id}' has blank title",
                dua.title.isNotBlank()
            )

            // Validate Arabic Body Text
            assertTrue(
                "Dua '${dua.title}' (${dua.id}) has blank Arabic body text",
                dua.arabic.isNotBlank()
            )

            // Validate Transliteration
            assertTrue(
                "Dua '${dua.title}' (${dua.id}) has blank transliteration",
                dua.transliteration.isNotBlank()
            )

            // Validate English Translation
            assertTrue(
                "Dua '${dua.title}' (${dua.id}) has blank English translation",
                dua.translation.isNotBlank()
            )

            // Validate Category Match
            assertTrue(
                "Dua '${dua.title}' category '${dua.category}' is not recognized in categoryList",
                categoryTitles.contains(dua.category)
            )

            // Validate Reference (if present, must be non-blank)
            dua.reference?.let { ref ->
                assertTrue(
                    "Dua '${dua.title}' reference is present but blank",
                    ref.isNotBlank()
                )
            }

            testedCategories.add(dua.category)

            // Test bidirectional entity conversion
            val entity = dua.toDuaEntity(displayOrder = index)
            assertEquals("Entity id mismatch for dua ${dua.id}", dua.id, entity.id)
            assertEquals("Entity title mismatch for dua ${dua.id}", dua.title, entity.title)
            assertEquals("Entity arabic text mismatch for dua ${dua.id}", dua.arabic, entity.arabic)
            assertEquals("Entity transliteration mismatch for dua ${dua.id}", dua.transliteration, entity.transliteration)
            assertEquals("Entity translation mismatch for dua ${dua.id}", dua.translation, entity.translation)
            assertEquals("Entity categoryTitle mismatch for dua ${dua.id}", dua.category, entity.categoryTitle)
            assertEquals("Entity reference mismatch for dua ${dua.id}", dua.reference, entity.reference)

            val roundTripDua = entity.toDuaItem()
            assertEquals("Roundtrip id mismatch for ${dua.id}", entity.id, roundTripDua.id)
            assertEquals("Roundtrip title mismatch for ${dua.id}", entity.title, roundTripDua.title)
            assertEquals("Roundtrip arabic mismatch for ${dua.id}", entity.arabic, roundTripDua.arabic)
            assertEquals("Roundtrip transliteration mismatch for ${dua.id}", entity.transliteration, roundTripDua.transliteration)
            assertEquals("Roundtrip translation mismatch for ${dua.id}", entity.translation, roundTripDua.translation)
            assertEquals("Roundtrip category mismatch for ${dua.id}", entity.categoryTitle, roundTripDua.category)
            assertEquals("Roundtrip reference mismatch for ${dua.id}", entity.reference, roundTripDua.reference)
        }

        // Verify every category defined in categoryList has at least one Dua
        categoryTitles.forEach { catTitle ->
            assertTrue(
                "Category '$catTitle' has no Duas associated with it in allDuas",
                testedCategories.contains(catTitle)
            )
        }
    }
}
