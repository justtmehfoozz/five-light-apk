package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppearanceMode
import com.example.data.model.Surah
import com.example.data.util.QuranData
import com.example.ui.components.SurahOverviewContent
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SurahReaderSubNav
import com.example.ui.theme.FiveLightTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SurahReaderSubNavTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSurahOverviewContentRendersAndNavigates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fatihahOverview = QuranData.getSurahOverview(context, 1)
        assertNotNull(fatihahOverview)

        val fatihahSurah = Surah(
            number = 1,
            nameEnglish = "Al-Fatihah",
            nameArabic = "الفاتحة",
            englishTranslation = "The Opener",
            versesCount = 7,
            revelationPlace = "Meccan"
        )

        var navigatedVerse = -1

        composeTestRule.setContent {
            FiveLightTheme(appearanceMode = AppearanceMode.LIGHT) {
                val listState = rememberLazyListState()
                SurahOverviewContent(
                    surah = fatihahSurah,
                    overview = fatihahOverview,
                    listState = listState,
                    onNavigateToVerse = { verse ->
                        navigatedVerse = verse
                    }
                )
            }
        }

        // Verify Overview elements are displayed
        composeTestRule.onNodeWithTag("surah_overview_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surah_overview_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("metric_revelation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("metric_verses").assertIsDisplayed()

        // Verify section card for Al-Fatihah (section 1, verses 1-7)
        composeTestRule.onNodeWithTag("surah_map_section_1").performScrollTo().assertIsDisplayed()

        // Click section card to trigger navigation to verse 1
        composeTestRule.onNodeWithTag("surah_map_section_1").performClick()
        assertEquals(1, navigatedVerse)
    }

    @Test
    fun testSurahOverviewContentForMultiSectionSurah() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val baqarahOverview = QuranData.getSurahOverview(context, 2)
        assertNotNull(baqarahOverview)
        assertTrue((baqarahOverview?.sections?.size ?: 0) >= 3)

        val baqarahSurah = Surah(
            number = 2,
            nameEnglish = "Al-Baqarah",
            nameArabic = "البقرة",
            englishTranslation = "The Cow",
            versesCount = 286,
            revelationPlace = "Medinan"
        )

        var navigatedVerse = -1

        composeTestRule.setContent {
            FiveLightTheme(appearanceMode = AppearanceMode.DARK) {
                val listState = rememberLazyListState()
                SurahOverviewContent(
                    surah = baqarahSurah,
                    overview = baqarahOverview,
                    listState = listState,
                    onNavigateToVerse = { verse ->
                        navigatedVerse = verse
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("surah_overview_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surah_overview_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("metric_revelation").assertIsDisplayed()

        // Check section 1
        composeTestRule.onNodeWithTag("surah_map_section_1").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("surah_map_section_1").performClick()
        assertEquals(1, navigatedVerse)
    }

    @Test
    fun testSurahReaderSubNavInteraction() {
        var selectedSubTab = 0

        composeTestRule.setContent {
            FiveLightTheme(appearanceMode = AppearanceMode.LIGHT) {
                var currentTab by remember { mutableIntStateOf(0) }
                SurahReaderSubNav(
                    selectedTab = currentTab,
                    onTabSelected = { newTab ->
                        currentTab = newTab
                        selectedSubTab = newTab
                    }
                )
            }
        }

        // Initial state: Read tab is selected
        composeTestRule.onNodeWithTag("surah_subtab_read").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithTag("surah_subtab_overview").assertIsDisplayed()

        // Tap Overview tab
        composeTestRule.onNodeWithTag("surah_subtab_overview").performClick()
        assertEquals(1, selectedSubTab)
        composeTestRule.onNodeWithTag("surah_subtab_overview").assertIsSelected()

        // Tap Read tab
        composeTestRule.onNodeWithTag("surah_subtab_read").performClick()
        assertEquals(0, selectedSubTab)
        composeTestRule.onNodeWithTag("surah_subtab_read").assertIsSelected()
    }
}
