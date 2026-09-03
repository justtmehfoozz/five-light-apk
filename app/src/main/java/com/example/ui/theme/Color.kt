package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.data.model.PrayerName

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue

data class SemanticColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val control: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val border: Color,
    val strongBorder: Color,
    val primaryAccent: Color,
    val accentForeground: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
    val dockBackground: Color,
    val dockBorder: Color,
    val dockIconInactive: Color,
    val dockIconActiveBg: Color,
    val dockIconActive: Color,
    val dockActiveIndicator: Color
)

val LightSemanticColors = SemanticColors(
    background = Color(0xFFF4F1EA),
    surface = Color(0xFFFCF9F2),
    surfaceElevated = Color(0xFFF0EBE1),
    control = Color(0xFFFFFFFF),
    primaryText = Color(0xFF1E1D1A),
    secondaryText = Color(0xFF66635E),
    mutedText = Color(0xFF7A7771),
    border = Color(0xFFD6D2C8),
    strongBorder = Color(0xFFC8C3B9),
    primaryAccent = Color(0xFF8D6B1E),
    accentForeground = Color(0xFFFFFFFF),
    success = Color(0xFF248A3D),
    error = Color(0xFFC62828),
    warning = Color(0xFFC93400),
    dockBackground = Color(0xFFFCFBF7),
    dockBorder = Color(0xFFC8C3B9),
    dockIconInactive = Color(0xFF66635E),
    dockIconActiveBg = Color(0xFF302F2B),
    dockIconActive = Color(0xFFFFFFFF),
    dockActiveIndicator = Color(0xFF302F2B)
)

val DarkSemanticColors = SemanticColors(
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surfaceElevated = Color(0xFF1C1C1E),
    control = Color(0xFF2C2C2E),
    primaryText = Color(0xFFF2F2EE),
    secondaryText = Color(0xFFA8A8A2),
    mutedText = Color(0xFF85857F),
    border = Color(255, 255, 255, 36),
    strongBorder = Color(255, 255, 255, 55),
    primaryAccent = Color(0xFF494556),
    accentForeground = Color(0xFFFFFFFF),
    success = Color(0xFF30D158),
    error = Color(0xFFFF453A),
    warning = Color(0xFFFF9F0A),
    dockBackground = Color(60, 60, 64, 235),
    dockBorder = Color(255, 255, 255, 89),
    dockIconInactive = Color(255, 255, 255, 166),
    dockIconActiveBg = Color(255, 255, 255, 46),
    dockIconActive = Color(0xFFFFFFFF),
    dockActiveIndicator = Color(255, 255, 255, 46)
)

val LocalSemanticColors = compositionLocalOf { LightSemanticColors }

@Composable
fun isAppInDarkTheme(): Boolean {
    return androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
}

val Color.Companion.semanticBackgroundLight: Color get() = LightSemanticColors.background
val Color.Companion.semanticSurfaceLight: Color get() = LightSemanticColors.surface
val Color.Companion.semanticSurfaceElevatedLight: Color get() = LightSemanticColors.surfaceElevated
val Color.Companion.semanticControlLight: Color get() = LightSemanticColors.control
val Color.Companion.semanticPrimaryTextLight: Color get() = LightSemanticColors.primaryText
val Color.Companion.semanticSecondaryTextLight: Color get() = LightSemanticColors.secondaryText
val Color.Companion.semanticMutedTextLight: Color get() = LightSemanticColors.mutedText
val Color.Companion.semanticBorderLight: Color get() = LightSemanticColors.border
val Color.Companion.semanticStrongBorderLight: Color get() = LightSemanticColors.strongBorder
val Color.Companion.semanticPrimaryAccentLight: Color get() = LightSemanticColors.primaryAccent
val Color.Companion.semanticAccentForegroundLight: Color get() = LightSemanticColors.accentForeground
val Color.Companion.semanticSuccessLight: Color get() = LightSemanticColors.success
val Color.Companion.semanticErrorLight: Color get() = LightSemanticColors.error
val Color.Companion.semanticWarningLight: Color get() = LightSemanticColors.warning

val Color.Companion.semanticBackgroundDark: Color get() = DarkSemanticColors.background
val Color.Companion.semanticSurfaceDark: Color get() = DarkSemanticColors.surface
val Color.Companion.semanticSurfaceElevatedDark: Color get() = DarkSemanticColors.surfaceElevated
val Color.Companion.semanticControlDark: Color get() = DarkSemanticColors.control
val Color.Companion.semanticPrimaryTextDark: Color get() = DarkSemanticColors.primaryText
val Color.Companion.semanticSecondaryTextDark: Color get() = DarkSemanticColors.secondaryText
val Color.Companion.semanticMutedTextDark: Color get() = DarkSemanticColors.mutedText
val Color.Companion.semanticBorderDark: Color get() = DarkSemanticColors.border
val Color.Companion.semanticStrongBorderDark: Color get() = DarkSemanticColors.strongBorder
val Color.Companion.semanticPrimaryAccentDark: Color get() = DarkSemanticColors.primaryAccent
val Color.Companion.semanticAccentForegroundDark: Color get() = DarkSemanticColors.accentForeground
val Color.Companion.semanticSuccessDark: Color get() = DarkSemanticColors.success
val Color.Companion.semanticErrorDark: Color get() = DarkSemanticColors.error
val Color.Companion.semanticWarningDark: Color get() = DarkSemanticColors.warning
val Color.Companion.quranVerseActionIconColorDark: Color get() = Color(0xFFFFFFFF)
val Color.Companion.quranChipTextColorDark: Color get() = Color(0xFFFFFFFF)

fun getSemanticColor(isDark: Boolean, lightColor: Color, darkColor: Color): Color {
    return if (isDark) darkColor else lightColor
}

val Color.Companion.semanticBackground: Color @Composable get() = LocalSemanticColors.current.background
val Color.Companion.semanticSurface: Color @Composable get() = LocalSemanticColors.current.surface
val Color.Companion.semanticSurfaceElevated: Color @Composable get() = LocalSemanticColors.current.surfaceElevated
val Color.Companion.semanticControl: Color @Composable get() = LocalSemanticColors.current.control
val Color.Companion.semanticPrimaryText: Color @Composable get() = LocalSemanticColors.current.primaryText
val Color.Companion.semanticSecondaryText: Color @Composable get() = LocalSemanticColors.current.secondaryText
val Color.Companion.semanticMutedText: Color @Composable get() = LocalSemanticColors.current.mutedText
val Color.Companion.semanticBorder: Color @Composable get() = LocalSemanticColors.current.border
val Color.Companion.semanticStrongBorder: Color @Composable get() = LocalSemanticColors.current.strongBorder
val Color.Companion.semanticPrimaryAccent: Color @Composable get() = LocalSemanticColors.current.primaryAccent
val Color.Companion.semanticAccentForeground: Color @Composable get() = LocalSemanticColors.current.accentForeground
val Color.Companion.semanticSuccess: Color @Composable get() = LocalSemanticColors.current.success
val Color.Companion.semanticError: Color @Composable get() = LocalSemanticColors.current.error
val Color.Companion.semanticWarning: Color @Composable get() = LocalSemanticColors.current.warning

val Color.Companion.semanticDockBackgroundLight: Color get() = LightSemanticColors.dockBackground
val Color.Companion.semanticDockBorderLight: Color get() = LightSemanticColors.dockBorder
val Color.Companion.semanticDockIconInactiveLight: Color get() = LightSemanticColors.dockIconInactive
val Color.Companion.semanticDockIconActiveBgLight: Color get() = LightSemanticColors.dockIconActiveBg
val Color.Companion.semanticDockIconActiveLight: Color get() = LightSemanticColors.dockIconActive

val Color.Companion.semanticDockBackgroundDark: Color get() = DarkSemanticColors.dockBackground
val Color.Companion.semanticDockBorderDark: Color get() = DarkSemanticColors.dockBorder
val Color.Companion.semanticDockIconInactiveDark: Color get() = DarkSemanticColors.dockIconInactive
val Color.Companion.dockActiveIndicatorLight: Color get() = LightSemanticColors.dockActiveIndicator
val Color.Companion.dockActiveIndicatorDark: Color get() = DarkSemanticColors.dockActiveIndicator
val Color.Companion.semanticDockIconActiveBgDark: Color get() = DarkSemanticColors.dockIconActiveBg
val Color.Companion.semanticDockIconActiveDark: Color get() = DarkSemanticColors.dockIconActive

val Color.Companion.dockActiveIndicator: Color @Composable get() = LocalSemanticColors.current.dockActiveIndicator
val Color.Companion.semanticDockBackground: Color @Composable get() = LocalSemanticColors.current.dockBackground
val Color.Companion.semanticDockBorder: Color @Composable get() = LocalSemanticColors.current.dockBorder
val Color.Companion.semanticDockIconInactive: Color @Composable get() = LocalSemanticColors.current.dockIconInactive
val Color.Companion.semanticDockIconActiveBg: Color @Composable get() = LocalSemanticColors.current.dockIconActiveBg
val Color.Companion.semanticDockIconActive: Color @Composable get() = LocalSemanticColors.current.dockIconActive


// Dark Mode Layered Design System Palette
val AppBackground = Color(0xFF000000)
val SurfaceDark = Color(0xFF1C1C1E)
val SurfaceVariantDark = Color(0xFF2C2C2E)
val BorderDark = Color(255, 255, 255, 36)
val TextPrimary = Color(0xFFF2F2EE)
val TextSecondary = Color(0xFFA8A8A2)
val TextMuted = Color(0xFF85857F)
val TextWhite = Color(0xFFFFFFFF)
val TextWhiteSubtle = Color(0x9DFFFFFF)

// Light Mode Semantic Tokens (SINGLE SOURCE OF TRUTH)
val LightBackground = Color(0xFFF4F1EA)
val LightSurface = Color(0xFFFDFBF7)
val LightPrimaryText = Color(0xFF1E1D1A)
val LightSecondaryText = Color(0xFF66635E)
val LightMutedText = Color(0xFF7A7771)
val LightSubtleText = Color(0xFF8F8C86)
val LightAccentGold = Color(0xFF8D6B1E)
val LightMissedRed = Color(0xFFC62828)
val LightInactivePillBg = Color(0xFFE8E4DA)
val LightBorder = Color(0xFFD6D2C8)
val LightCurrentBadgeBg = Color(0xFF8D6B1E)
val LightCurrentBadgeText = Color(0xFFFFFFFF)
val LightTodayRing = Color(0xFF8D6B1E)

// Legacy Aliases (to prevent build errors while refactoring)
val AppBackgroundLight = LightBackground
val SurfaceLight = LightSurface
val SurfaceVariantLight = LightInactivePillBg
val BorderLight = LightBorder
val DividerLight = LightBorder
val TextPrimaryLight = LightPrimaryText
val TextSecondaryLight = LightSecondaryText
val TextTertiaryLight = Color(0xFF7A7771)
val PrimaryAccentLight = LightAccentGold
val SuccessLight = Color(0xFF248A3D)
val ErrorLight = LightMissedRed

// Primary Accents (Dark Mode)
val PrimaryAccentDark = Color(0xFF494556)

// Status Feedback Colors (Dark Mode)
val SuccessDark = Color(0xFF30D158)
val ErrorDark = Color(0xFFFF453A)


// Pill Row (Dark Mode)
val PillInactiveBg = Color(0xFF2C2C2E)
val PillInactiveBorder = Color(255, 255, 255, 20)
val PillActiveBg = Color(0xFF494556)
val PillActiveText = Color(0xFFFFFFFF)

// Tasbeeh & Dhikr Specific Design Tokens (Dark Mode)
val DhikrSelectedBg = Color(0xFF494556)
val DhikrSelectedText = Color(0xFFFFFFFF)
val TargetSelectedBg = Color(0xFF494556)
val TargetSelectedText = Color(0xFFFFFFFF)
val TargetUnselectedText = Color(0xFF8E8E93)


// Five Daily Prayer Signature Sky Gradients (DO NOT CHANGE)
val FajrGradientStart = Color(0xFF4A6FA5)
val FajrGradientEnd = Color(0xFF253A5E)

val DhuhrGradientStart = Color(0xFFC9A227)
val DhuhrGradientEnd = Color(0xFF7C6115)

val JummahGradientStart = Color(0xFF1F6B4A)
val JummahGradientEnd = Color(0xFF0F3D2A)

val AsrGradientStart = Color(0xFFD9822E)
val AsrGradientEnd = Color(0xFF8C4F17)

val MaghribGradientStart = Color(0xFFB14D6B)
val MaghribGradientEnd = Color(0xFF5E2338)

val IshaGradientStart = Color(0xFF4A4380)
val IshaGradientEnd = Color(0xFF201B3F)

val SunriseGradientStart = Color(0xFF5B80A8)
val SunriseGradientEnd = Color(0xFF344966)

fun getPrayerGradientColors(prayerName: PrayerName, isFriday: Boolean = false): Pair<Color, Color> {
    if (prayerName == PrayerName.DHUHR && isFriday) {
        return JummahGradientStart to JummahGradientEnd
    }
    return when (prayerName) {
        PrayerName.FAJR -> FajrGradientStart to FajrGradientEnd
        PrayerName.SUNRISE -> SunriseGradientStart to SunriseGradientEnd
        PrayerName.DHUHR -> DhuhrGradientStart to DhuhrGradientEnd
        PrayerName.ASR -> AsrGradientStart to AsrGradientEnd
        PrayerName.MAGHRIB -> MaghribGradientStart to MaghribGradientEnd
        PrayerName.ISHA -> IshaGradientStart to IshaGradientEnd
    }
}

fun adjustColorSubtle(color: Color, lightnessDelta: Float, warmthDelta: Float = 0f): Color {
    val r = (color.red + lightnessDelta * 0.5f + warmthDelta * 0.3f).coerceIn(0f, 1f)
    val g = (color.green + lightnessDelta * 0.5f).coerceIn(0f, 1f)
    val b = (color.blue + lightnessDelta * 0.5f - warmthDelta * 0.2f).coerceIn(0f, 1f)
    return Color(r, g, b, color.alpha)
}

fun getSeasonalPrayerGradientColors(
    prayerName: PrayerName,
    timeMillis: Long = 0L,
    isFriday: Boolean = false
): Pair<Color, Color> {
    val (baseStart, baseEnd) = getPrayerGradientColors(prayerName, isFriday)
    if (timeMillis <= 0L) return baseStart to baseEnd

    val cal = java.util.Calendar.getInstance().apply { this.timeInMillis = timeMillis }
    val totalMins = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)

    val (lightnessDelta, warmthDelta) = when (prayerName) {
        PrayerName.FAJR -> {
            val delta = ((totalMins - 285) / 75f).coerceIn(-1f, 1f)
            (-delta * 0.05f) to (-delta * 0.02f)
        }
        PrayerName.SUNRISE -> {
            val delta = ((totalMins - 375) / 60f).coerceIn(-1f, 1f)
            (-delta * 0.04f) to (delta * 0.03f)
        }
        PrayerName.DHUHR -> {
            val delta = ((totalMins - 735) / 30f).coerceIn(-1f, 1f)
            (delta * 0.04f) to (delta * 0.03f)
        }
        PrayerName.ASR -> {
            val delta = ((totalMins - 960) / 90f).coerceIn(-1f, 1f)
            (delta * 0.03f) to (delta * 0.05f)
        }
        PrayerName.MAGHRIB -> {
            val delta = ((totalMins - 1125) / 105f).coerceIn(-1f, 1f)
            (delta * 0.06f) to (delta * 0.04f)
        }
        PrayerName.ISHA -> {
            val delta = ((totalMins - 1220) / 115f).coerceIn(-1f, 1f)
            (delta * 0.05f) to (delta * 0.02f)
        }
    }

    val adjStart = adjustColorSubtle(baseStart, lightnessDelta, warmthDelta)
    val adjEnd = adjustColorSubtle(baseEnd, lightnessDelta, warmthDelta)

    return adjStart to adjEnd
}

fun getPrayerGradient(prayerName: PrayerName, isFriday: Boolean = false): Brush {
    val (start, end) = getPrayerGradientColors(prayerName, isFriday)
    return Brush.linearGradient(
        colors = listOf(start, end)
    )
}

fun getPrayerDisplayName(prayerName: PrayerName, isFriday: Boolean = false): String {
    return com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(prayerName, isFriday)
}

fun getPrayerArabicName(prayerName: PrayerName, isFriday: Boolean = false): String {
    return com.example.data.util.PrayerDisplayUtils.getPrayerArabicName(prayerName, isFriday)
}

fun getPrayerPoeticSubtext(prayerName: PrayerName, isFriday: Boolean = false): String {
    return com.example.data.util.PrayerDisplayUtils.getPrayerPoeticSubtext(prayerName, isFriday)
}
