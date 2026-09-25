package com.example

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.AppearanceMode
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.ui.screens.QuranScreen
import com.example.ui.theme.FiveLightTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuranReaderHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testQuranReaderHeaderCenteredIdentityAndOverflowMenu() {
        val fatihahSurah = Surah(
            number = 1,
            nameEnglish = "Al-Fatihah",
            nameArabic = "الفاتحة",
            englishTranslation = "The Opener",
            versesCount = 7,
            revelationPlace = "Meccan"
        )

        val verses = listOf(
            Verse(
                surahNumber = 1,
                verseNumber = 1,
                textArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                textEnglish = "In the name of Allah, the Entirely Merciful, the Especially Merciful."
            )
        )

        var translationToggled = false

        composeTestRule.setContent {
            val emptyMapState = androidx.compose.runtime.remember { mutableStateOf(emptyMap<Int, Float>()) }
            val emptyScrollState = androidx.compose.runtime.remember { mutableStateOf(emptyMap<Int, Int>()) }

            FiveLightTheme(appearanceMode = AppearanceMode.LIGHT) {
                QuranScreen(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    selectedSurah = fatihahSurah,
                    verses = verses,
                    onSelectSurah = {},
                    fontSizeSp = 24f,
                    onFontSizeChange = {},
                    showEnglishTranslation = true,
                    onToggleEnglish = { translationToggled = true },
                    isNightReadingMode = false,
                    onToggleNightReading = {},
                    playingSurahNumberProvider = { null },
                    playingVerseNumberProvider = { null },
                    isPlayingAudioProvider = { false },
                    surahPlaybackProgress = emptyMapState,
                    bookmarks = emptyList(),
                    onToggleBookmark = { _, _ -> },
                    surahScrollPositions = emptyScrollState,
                    initialOpenReadingView = true,
                    onPlayVerseAudio = {}
                )
            }
        }

        // 1. Verify Back button is displayed
        composeTestRule.onNodeWithTag("quran_reader_back_btn").assertIsDisplayed()

        // 2. Verify True Centered Surah Title is displayed
        composeTestRule.onNodeWithText("Al-Fatihah").assertIsDisplayed()

        // 3. Verify Metadata format without "Verses" word (e.g. "7 • الفاتحة")
        composeTestRule.onNodeWithText("7 • الفاتحة").assertIsDisplayed()

        // 4. Verify Three-dot Overflow button is displayed
        composeTestRule.onNodeWithTag("quran_reader_overflow_btn").assertIsDisplayed()

        // 5. Open overflow menu
        composeTestRule.onNodeWithTag("quran_reader_overflow_btn").performClick()

        // 6. Verify menu items "Translation" and "Text size" appear
        composeTestRule.onNodeWithText("Translation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Text size").assertIsDisplayed()

        // 7. Click Translation in menu
        composeTestRule.onNodeWithText("Translation").performClick()
        assertTrue(translationToggled)

        // 8. Open overflow menu again and click "Text size"
        composeTestRule.onNodeWithTag("quran_reader_overflow_btn").performClick()
        composeTestRule.onNodeWithText("Text size").performClick()

        // 9. Verify font size slider is now visible
        composeTestRule.onNodeWithTag("font_size_slider").assertIsDisplayed()
    }
}
