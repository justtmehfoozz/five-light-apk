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
    val targetSemantic = if (darkTheme) DarkSemanticColors else LightSemanticColors

    // Option B: Scope animation strictly to high-impact canvas/surface colors.
    // Borders, accents, icons, and text snap cleanly without triggering massive recomposition loops across the tree.
    val animatedBg by animateColorAsState(targetScheme.background, colorSpec, label = "bg")
    val animatedSurface by animateColorAsState(targetScheme.surface, colorSpec, label = "surf")
    val animatedSurfaceVar by animateColorAsState(targetScheme.surfaceVariant, colorSpec, label = "surfVar")
    val animatedSurfaceElevated by animateColorAsState(targetSemantic.surfaceElevated, colorSpec, label = "surfElev")

    val activeScheme = remember(targetScheme, animatedBg, animatedSurface, animatedSurfaceVar) {
        targetScheme.copy(
            background = animatedBg,
            surface = animatedSurface,
            surfaceVariant = animatedSurfaceVar
        )
    }

    val activeSemanticColors = remember(targetSemantic, animatedBg, animatedSurface, animatedSurfaceElevated) {
        targetSemantic.copy(
            background = animatedBg,
            surface = animatedSurface,
            surfaceElevated = animatedSurfaceElevated
        )
    }

    CompositionLocalProvider(
        LocalSemanticColors provides activeSemanticColors
    ) {
        MaterialTheme(
            colorScheme = activeScheme,
            typography = Typography,
            content = content
        )
    }
}


