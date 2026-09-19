package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.data.model.AppearanceMode
import com.example.ui.components.FiveLightEdgeVignette
import com.example.ui.theme.FiveLightTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FiveLightEdgeVignetteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testVignetteRendersTopAndBottomScrimsInDarkMode() {
        composeTestRule.setContent {
            FiveLightTheme(appearanceMode = AppearanceMode.DARK) {
                FiveLightEdgeVignette(
                    topHeight = 88.dp,
                    bottomHeight = 128.dp
                )
            }
        }

        composeTestRule.onNodeWithTag("fivelight_edge_vignette").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_edge_scrim").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_edge_scrim").assertIsDisplayed()
    }

    @Test
    fun testVignetteRendersTopAndBottomScrimsInLightMode() {
        composeTestRule.setContent {
            FiveLightTheme(appearanceMode = AppearanceMode.LIGHT) {
                FiveLightEdgeVignette(
                    topHeight = 88.dp,
                    bottomHeight = 128.dp
                )
            }
        }

        composeTestRule.onNodeWithTag("fivelight_edge_vignette").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_edge_scrim").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_edge_scrim").assertIsDisplayed()
    }

    @Test
    fun testVignetteDoesNotInterceptClicksOnContentUnderneath() {
        var clickedTopContent = false
        var clickedBottomContent = false

        composeTestRule.setContent {
            FiveLightTheme(appearanceMode = AppearanceMode.DARK) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Content underneath at top
                    Text(
                        text = "Top Title",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .testTag("top_content")
                            .clickable { clickedTopContent = true }
                    )

                    // Content underneath at bottom
                    Text(
                        text = "Bottom Action",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .testTag("bottom_content")
                            .clickable { clickedBottomContent = true }
                    )

                    // Persistent vignette layer on top
                    FiveLightEdgeVignette(
                        topHeight = 88.dp,
                        bottomHeight = 128.dp
                    )
                }
            }
        }

        // Click top content through top scrim
        composeTestRule.onNodeWithTag("top_content").performClick()
        assertTrue("Top content must be clickable through the non-interactive vignette", clickedTopContent)

        // Click bottom content through bottom scrim
        composeTestRule.onNodeWithTag("bottom_content").performClick()
        assertTrue("Bottom content must be clickable through the non-interactive vignette", clickedBottomContent)
    }
}
