package com.example.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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

    // 1. Snapped color schemes: instant at the ColorScheme & MaterialTheme level
    // No animateColorAsState feeding into MaterialTheme(colorScheme = ...)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    // 2. Draw-only overlay crossfade:
    // Tracks theme transitions and animates ONLY the overlay's graphicsLayer alpha from 1f to 0f
    var lastDarkTheme by remember { mutableStateOf(darkTheme) }
    var previousOverlayColor by remember { mutableStateOf<Color?>(null) }
    val overlayAlpha = remember { Animatable(0f) }

    LaunchedEffect(darkTheme) {
        if (lastDarkTheme != darkTheme) {
            val oldBg = if (lastDarkTheme) DarkColorScheme.background else LightColorScheme.background
            previousOverlayColor = oldBg
            lastDarkTheme = darkTheme

            if (isReducedMotion) {
                overlayAlpha.snapTo(0f)
                previousOverlayColor = null
            } else {
                overlayAlpha.snapTo(1f)
                overlayAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
                previousOverlayColor = null
            }
        }
    }

    CompositionLocalProvider(
        LocalSemanticColors provides semanticColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Real app content rendered instantly with the new theme
                content()

                // Draw-only overlay fading out the previous theme canvas color
                val currentOverlayColor = previousOverlayColor
                if (currentOverlayColor != null && overlayAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = overlayAlpha.value
                            }
                            .background(currentOverlayColor)
                    )
                }
            }
        }
    }
}



