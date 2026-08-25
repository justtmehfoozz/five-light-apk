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
    content: @Composable () -> Unit
) {
    val darkTheme = when (appearanceMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.DARK -> true
        AppearanceMode.LIGHT -> false
    }
    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val animatedBg by animateColorAsState(targetScheme.background, tween(400), label = "bgAnim")
    val animatedSurface by animateColorAsState(targetScheme.surface, tween(400), label = "surfaceAnim")
    val animatedSurfaceVariant by animateColorAsState(targetScheme.surfaceVariant, tween(400), label = "surfaceVarAnim")
    val animatedOnSurface by animateColorAsState(targetScheme.onSurface, tween(400), label = "onSurfaceAnim")
    val animatedOnSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, tween(400), label = "onSurfaceVarAnim")
    val animatedPrimary by animateColorAsState(targetScheme.primary, tween(400), label = "primaryAnim")
    val animatedSecondary by animateColorAsState(targetScheme.secondary, tween(400), label = "secondaryAnim")
    val animatedTertiary by animateColorAsState(targetScheme.tertiary, tween(400), label = "tertiaryAnim")
    val animatedOutline by animateColorAsState(targetScheme.outline, tween(400), label = "outlineAnim")
    val animatedOutlineVariant by animateColorAsState(targetScheme.outlineVariant, tween(400), label = "outlineVarAnim")

    val colorScheme = targetScheme.copy(
        background = animatedBg,
        surface = animatedSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurface = animatedOnSurface,
        onSurfaceVariant = animatedOnSurfaceVariant,
        primary = animatedPrimary,
        secondary = animatedSecondary,
        tertiary = animatedTertiary,
        outline = animatedOutline,
        outlineVariant = animatedOutlineVariant
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


