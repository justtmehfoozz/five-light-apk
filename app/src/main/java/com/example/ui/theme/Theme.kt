package com.example.ui.theme

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
    primary = TextPrimary,
    onPrimary = AppBackground,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = AppBackground,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = TextPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SurfaceLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = TextSecondaryLight,
    onSecondary = Color.White,
    background = AppBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
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
    val animatedOnSurface by animateColorAsState(targetScheme.onSurface, tween(400), label = "onSurfaceAnim")
    val animatedOnSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, tween(400), label = "onSurfaceVarAnim")
    val animatedPrimary by animateColorAsState(targetScheme.primary, tween(400), label = "primaryAnim")
    val animatedOutline by animateColorAsState(targetScheme.outline, tween(400), label = "outlineAnim")

    val colorScheme = targetScheme.copy(
        background = animatedBg,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        onSurfaceVariant = animatedOnSurfaceVariant,
        primary = animatedPrimary,
        outline = animatedOutline,
        surfaceVariant = animatedSurface
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


