package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.data.model.PrayerName

@Composable
fun isAppInDarkTheme(): Boolean {
    return androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
}

val Color.Companion.semanticBackgroundLight: Color get() = Color(0xFFF4F1EA)
val Color.Companion.semanticSurfaceLight: Color get() = Color(0xFFFCF9F2)
val Color.Companion.semanticSurfaceElevatedLight: Color get() = Color(0xFFF0EBE1)
val Color.Companion.semanticControlLight: Color get() = Color(0xFFFFFFFF)
val Color.Companion.semanticPrimaryTextLight: Color get() = Color(0xFF1E1D1A)
val Color.Companion.semanticSecondaryTextLight: Color get() = Color(0xFF66635E)
val Color.Companion.semanticMutedTextLight: Color get() = Color(0xFF7A7771)
val Color.Companion.semanticBorderLight: Color get() = Color(0xFFD6D2C8)
val Color.Companion.semanticStrongBorderLight: Color get() = Color(0xFFC8C3B9)
val Color.Companion.semanticPrimaryAccentLight: Color get() = Color(0xFF8D6B1E)
val Color.Companion.semanticAccentForegroundLight: Color get() = Color(0xFFFFFFFF)
val Color.Companion.semanticSuccessLight: Color get() = Color(0xFF248A3D)
val Color.Companion.semanticErrorLight: Color get() = Color(0xFFC62828)
val Color.Companion.semanticWarningLight: Color get() = Color(0xFFC93400)

val Color.Companion.semanticBackgroundDark: Color get() = Color(0xFF000000)
val Color.Companion.semanticSurfaceDark: Color get() = Color(0xFF1C1C1E)
val Color.Companion.semanticSurfaceElevatedDark: Color get() = Color(0xFF1C1C1E)
val Color.Companion.semanticControlDark: Color get() = Color(0xFF2C2C2E)
val Color.Companion.semanticPrimaryTextDark: Color get() = Color(0xFFF2F2EE)
val Color.Companion.semanticSecondaryTextDark: Color get() = Color(0xFFA8A8A2)
val Color.Companion.semanticMutedTextDark: Color get() = Color(0xFF85857F)
val Color.Companion.semanticBorderDark: Color get() = Color(255, 255, 255, 36)
val Color.Companion.semanticStrongBorderDark: Color get() = Color(255, 255, 255, 55)
val Color.Companion.semanticPrimaryAccentDark: Color get() = Color(0xFF494556)
val Color.Companion.semanticAccentForegroundDark: Color get() = Color(0xFFFFFFFF)
val Color.Companion.semanticSuccessDark: Color get() = Color(0xFF30D158)
val Color.Companion.semanticErrorDark: Color get() = Color(0xFFFF453A)
val Color.Companion.semanticWarningDark: Color get() = Color(0xFFFF9F0A)
val Color.Companion.quranVerseActionIconColorDark: Color get() = Color(0xFFFFFFFF)
val Color.Companion.quranChipTextColorDark: Color get() = Color(0xFFFFFFFF)

fun getSemanticColor(isDark: Boolean, lightColor: Color, darkColor: Color): Color {
    return if (isDark) darkColor else lightColor
}

val Color.Companion.semanticBackground: Color @Composable get() = if (isAppInDarkTheme()) semanticBackgroundDark else semanticBackgroundLight
val Color.Companion.semanticSurface: Color @Composable get() = if (isAppInDarkTheme()) semanticSurfaceDark else semanticSurfaceLight
val Color.Companion.semanticSurfaceElevated: Color @Composable get() = if (isAppInDarkTheme()) semanticSurfaceElevatedDark else semanticSurfaceElevatedLight
val Color.Companion.semanticControl: Color @Composable get() = if (isAppInDarkTheme()) semanticControlDark else semanticControlLight
val Color.Companion.semanticPrimaryText: Color @Composable get() = if (isAppInDarkTheme()) semanticPrimaryTextDark else semanticPrimaryTextLight
val Color.Companion.semanticSecondaryText: Color @Composable get() = if (isAppInDarkTheme()) semanticSecondaryTextDark else semanticSecondaryTextLight
val Color.Companion.semanticMutedText: Color @Composable get() = if (isAppInDarkTheme()) semanticMutedTextDark else semanticMutedTextLight
val Color.Companion.semanticBorder: Color @Composable get() = if (isAppInDarkTheme()) semanticBorderDark else semanticBorderLight
val Color.Companion.semanticStrongBorder: Color @Composable get() = if (isAppInDarkTheme()) semanticStrongBorderDark else semanticStrongBorderLight
val Color.Companion.semanticPrimaryAccent: Color @Composable get() = if (isAppInDarkTheme()) semanticPrimaryAccentDark else semanticPrimaryAccentLight
val Color.Companion.semanticAccentForeground: Color @Composable get() = if (isAppInDarkTheme()) semanticAccentForegroundDark else semanticAccentForegroundLight
val Color.Companion.semanticSuccess: Color @Composable get() = if (isAppInDarkTheme()) semanticSuccessDark else semanticSuccessLight
val Color.Companion.semanticError: Color @Composable get() = if (isAppInDarkTheme()) semanticErrorDark else semanticErrorLight
val Color.Companion.semanticWarning: Color @Composable get() = if (isAppInDarkTheme()) semanticWarningDark else semanticWarningLight

val Color.Companion.semanticDockBackgroundLight: Color get() = Color(0xFFFCFBF7)
val Color.Companion.semanticDockBorderLight: Color get() = Color(0xFFC8C3B9)
val Color.Companion.semanticDockIconInactiveLight: Color get() = Color(0xFF66635E)
val Color.Companion.semanticDockIconActiveBgLight: Color get() = Color(0xFF302F2B)
val Color.Companion.semanticDockIconActiveLight: Color get() = Color(0xFFFFFFFF)

val Color.Companion.semanticDockBackgroundDark: Color get() = Color(60, 60, 64, 235)
val Color.Companion.semanticDockBorderDark: Color get() = Color(255, 255, 255, 89)
val Color.Companion.semanticDockIconInactiveDark: Color get() = Color(255, 255, 255, 166)
val Color.Companion.dockActiveIndicatorLight: Color get() = Color(0xFF302F2B)
val Color.Companion.dockActiveIndicatorDark: Color get() = Color(255, 255, 255, 46)
val Color.Companion.semanticDockIconActiveBgDark: Color get() = Color(255, 255, 255, 46)
val Color.Companion.semanticDockIconActiveDark: Color get() = Color(0xFFFFFFFF)

val Color.Companion.dockActiveIndicator: Color @Composable get() = if (isAppInDarkTheme()) dockActiveIndicatorDark else dockActiveIndicatorLight
val Color.Companion.semanticDockBackground: Color @Composable get() = if (isAppInDarkTheme()) semanticDockBackgroundDark else semanticDockBackgroundLight
val Color.Companion.semanticDockBorder: Color @Composable get() = if (isAppInDarkTheme()) semanticDockBorderDark else semanticDockBorderLight
val Color.Companion.semanticDockIconInactive: Color @Composable get() = if (isAppInDarkTheme()) semanticDockIconInactiveDark else semanticDockIconInactiveLight
val Color.Companion.semanticDockIconActiveBg: Color @Composable get() = if (isAppInDarkTheme()) semanticDockIconActiveBgDark else semanticDockIconActiveBgLight
val Color.Companion.semanticDockIconActive: Color @Composable get() = if (isAppInDarkTheme()) semanticDockIconActiveDark else semanticDockIconActiveLight


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
