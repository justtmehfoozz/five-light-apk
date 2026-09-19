package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.isAppInDarkTheme

/**
 * Persistent app-wide Top and Bottom Edge Vignette / Edge Scrim for FiveLight.
 *
 * A subtle, theme-aware screen-edge shading effect that makes the interface feel deeper,
 * more immersive, and naturally integrated with the physical display edges.
 *
 * - Top Scrim: Anchored to the top physical edge, seamlessly extending through the system status bar,
 *   fading smoothly toward transparent before the main content area.
 * - Bottom Scrim: Anchored to the bottom physical edge, softly shading behind and around the
 *   floating bottom dock.
 * - Completely non-interactive: never intercepts taps, clicks, swipes, or scroll gestures.
 * - Fixed screen-edge overlay: remains attached to the viewport while content scrolls underneath.
 */
@Composable
fun FiveLightEdgeVignette(
    modifier: Modifier = Modifier,
    topHeight: Dp? = null,
    bottomHeight: Dp? = null,
    topEdgeAlphaOverride: Float? = null,
    bottomEdgeAlphaOverride: Float? = null
) {
    val isDark = isAppInDarkTheme()
    val baseColor = MaterialTheme.colorScheme.background

    // Responsive sizing adapting to device insets
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val calculatedTopHeight = topHeight ?: (topInset + 48.dp).coerceIn(72.dp, 108.dp)
    val calculatedBottomHeight = bottomHeight ?: (bottomInset + 96.dp).coerceIn(120.dp, 160.dp)

    // Subtle edge opacity tuned for dark and light modes
    val topEdgeAlpha = topEdgeAlphaOverride ?: if (isDark) 0.72f else 0.66f
    val bottomEdgeAlpha = bottomEdgeAlphaOverride ?: if (isDark) 0.76f else 0.70f

    // Smooth multi-stop non-linear easing for natural falloff without harsh banding
    val topGradientBrush = remember(baseColor, topEdgeAlpha) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to baseColor.copy(alpha = topEdgeAlpha),
                0.20f to baseColor.copy(alpha = topEdgeAlpha * 0.70f),
                0.40f to baseColor.copy(alpha = topEdgeAlpha * 0.42f),
                0.60f to baseColor.copy(alpha = topEdgeAlpha * 0.20f),
                0.80f to baseColor.copy(alpha = topEdgeAlpha * 0.06f),
                1.00f to Color.Transparent
            )
        )
    }

    val bottomGradientBrush = remember(baseColor, bottomEdgeAlpha) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.20f to baseColor.copy(alpha = bottomEdgeAlpha * 0.06f),
                0.40f to baseColor.copy(alpha = bottomEdgeAlpha * 0.20f),
                0.60f to baseColor.copy(alpha = bottomEdgeAlpha * 0.42f),
                0.80f to baseColor.copy(alpha = bottomEdgeAlpha * 0.70f),
                1.00f to baseColor.copy(alpha = bottomEdgeAlpha)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("fivelight_edge_vignette")
    ) {
        // TOP EDGE SCRIM
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(calculatedTopHeight)
                .align(Alignment.TopCenter)
                .background(topGradientBrush)
                .testTag("top_edge_scrim")
        )

        // BOTTOM EDGE SCRIM
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(calculatedBottomHeight)
                .align(Alignment.BottomCenter)
                .background(bottomGradientBrush)
                .testTag("bottom_edge_scrim")
        )
    }
}
