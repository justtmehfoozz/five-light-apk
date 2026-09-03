package com.example.ui.theme

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppearanceMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccentDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = PrimaryAccentDark,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    tertiary = TextMuted,
    onTertiary = TextPrimary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccentLight,
    onPrimary = SurfaceLight,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = TextSecondaryLight,
    onSecondary = SurfaceLight,
    tertiary = TextTertiaryLight,
    onTertiary = SurfaceLight,
    background = AppBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = DividerLight
)

@Composable
fun FiveLightTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    themeTransitionController: ThemeTransitionController? = null,
    content: @Composable () -> Unit
) {
    val controller = themeTransitionController ?: rememberThemeTransitionController()
    val activeMode = controller.effectiveAppearanceMode ?: appearanceMode

    val darkTheme = when (activeMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.DARK -> true
        AppearanceMode.LIGHT -> false
    }
    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeTransitionController provides controller
    ) {
        MaterialTheme(
            colorScheme = targetScheme,
            typography = Typography,
            content = content
        )
    }
}


