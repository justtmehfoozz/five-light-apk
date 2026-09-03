package com.example.ui.theme

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AppearanceMode

val ThemeAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 350, easing = FastOutSlowInEasing)

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
private fun ColorScheme.animated(spec: AnimationSpec<Color>): ColorScheme {
    val animatedPrimary by animateColorAsState(primary, spec, label = "primary")
    val animatedOnPrimary by animateColorAsState(onPrimary, spec, label = "onPrimary")
    val animatedPrimaryContainer by animateColorAsState(primaryContainer, spec, label = "primaryContainer")
    val animatedOnPrimaryContainer by animateColorAsState(onPrimaryContainer, spec, label = "onPrimaryContainer")
    val animatedSecondary by animateColorAsState(secondary, spec, label = "secondary")
    val animatedOnSecondary by animateColorAsState(onSecondary, spec, label = "onSecondary")
    val animatedSecondaryContainer by animateColorAsState(secondaryContainer, spec, label = "secondaryContainer")
    val animatedOnSecondaryContainer by animateColorAsState(onSecondaryContainer, spec, label = "onSecondaryContainer")
    val animatedTertiary by animateColorAsState(tertiary, spec, label = "tertiary")
    val animatedOnTertiary by animateColorAsState(onTertiary, spec, label = "onTertiary")
    val animatedTertiaryContainer by animateColorAsState(tertiaryContainer, spec, label = "tertiaryContainer")
    val animatedOnTertiaryContainer by animateColorAsState(onTertiaryContainer, spec, label = "onTertiaryContainer")
    val animatedBackground by animateColorAsState(background, spec, label = "background")
    val animatedOnBackground by animateColorAsState(onBackground, spec, label = "onBackground")
    val animatedSurface by animateColorAsState(surface, spec, label = "surface")
    val animatedOnSurface by animateColorAsState(onSurface, spec, label = "onSurface")
    val animatedSurfaceVariant by animateColorAsState(surfaceVariant, spec, label = "surfaceVariant")
    val animatedOnSurfaceVariant by animateColorAsState(onSurfaceVariant, spec, label = "onSurfaceVariant")
    val animatedOutline by animateColorAsState(outline, spec, label = "outline")
    val animatedOutlineVariant by animateColorAsState(outlineVariant, spec, label = "outlineVariant")
    val animatedError by animateColorAsState(error, spec, label = "error")
    val animatedOnError by animateColorAsState(onError, spec, label = "onError")
    val animatedErrorContainer by animateColorAsState(errorContainer, spec, label = "errorContainer")
    val animatedOnErrorContainer by animateColorAsState(onErrorContainer, spec, label = "onErrorContainer")

    return copy(
        primary = animatedPrimary,
        onPrimary = animatedOnPrimary,
        primaryContainer = animatedPrimaryContainer,
        onPrimaryContainer = animatedOnPrimaryContainer,
        secondary = animatedSecondary,
        onSecondary = animatedOnSecondary,
        secondaryContainer = animatedSecondaryContainer,
        onSecondaryContainer = animatedOnSecondaryContainer,
        tertiary = animatedTertiary,
        onTertiary = animatedOnTertiary,
        tertiaryContainer = animatedTertiaryContainer,
        onTertiaryContainer = animatedOnTertiaryContainer,
        background = animatedBackground,
        onBackground = animatedOnBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant,
        outline = animatedOutline,
        outlineVariant = animatedOutlineVariant,
        error = animatedError,
        onError = animatedOnError,
        errorContainer = animatedErrorContainer,
        onErrorContainer = animatedOnErrorContainer
    )
}

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

    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val durationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            durationScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    val colorSpec: AnimationSpec<Color> = remember(isReducedMotion) {
        if (isReducedMotion) {
            snap()
        } else {
            tween(durationMillis = 350, easing = FastOutSlowInEasing)
        }
    }

    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val animatedScheme = targetScheme.animated(colorSpec)

    val targetSemanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors
    val animatedSemanticColors = targetSemanticColors.animated(colorSpec)

    CompositionLocalProvider(
        LocalSemanticColors provides animatedSemanticColors
    ) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            content = content
        )
    }
}


