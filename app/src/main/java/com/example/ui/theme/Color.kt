package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.PrayerName

// Pure Black Design System Base Palette
val AppBackground = Color(0xFF000000)
val SurfaceDark = Color(0xFF131313)
val BorderDark = Color(0xFF1E1E1E)

val TextPrimary = Color(0xFFF2EFE9) // Warm Off-White
val TextSecondary = Color(0xFF7A7568) // Muted Secondary
val TextWhite = Color(0xFFFFFFFF)
val TextWhiteSubtle = Color(0x9DFFFFFF) // ~62% opacity

// Light Mode Design System Base Palette
val AppBackgroundLight = Color(0xFFF7F4EE) // Warm Off-White
val SurfaceLight = Color(0xFFEAE6DC)
val BorderLight = Color(0xFFDFD9CC)

val TextPrimaryLight = Color(0xFF1A1815) // Near-Black
val TextSecondaryLight = Color(0xFF6B6558) // Muted Secondary

// Pill Row
val PillInactiveBg = Color(0xFF131313)
val PillInactiveBorder = Color(0xFF1E1E1E)
val PillActiveBg = Color(0xFFF2EFE9)
val PillActiveText = Color(0xFF000000)

// Five Daily Prayer Signature Sky Gradients
val FajrGradientStart = Color(0xFF4A6FA5)
val FajrGradientEnd = Color(0xFF253A5E)

val DhuhrGradientStart = Color(0xFFC9A227)
val DhuhrGradientEnd = Color(0xFF7C6115)

val AsrGradientStart = Color(0xFFD9822E)
val AsrGradientEnd = Color(0xFF8C4F17)

val MaghribGradientStart = Color(0xFFB14D6B)
val MaghribGradientEnd = Color(0xFF5E2338)

val IshaGradientStart = Color(0xFF4A4380)
val IshaGradientEnd = Color(0xFF201B3F)

val SunriseGradientStart = Color(0xFF5B80A8)
val SunriseGradientEnd = Color(0xFF344966)

fun getPrayerGradient(prayerName: PrayerName): Brush {
    val (start, end) = when (prayerName) {
        PrayerName.FAJR -> FajrGradientStart to FajrGradientEnd
        PrayerName.SUNRISE -> SunriseGradientStart to SunriseGradientEnd
        PrayerName.DHUHR -> DhuhrGradientStart to DhuhrGradientEnd
        PrayerName.ASR -> AsrGradientStart to AsrGradientEnd
        PrayerName.MAGHRIB -> MaghribGradientStart to MaghribGradientEnd
        PrayerName.ISHA -> IshaGradientStart to IshaGradientEnd
    }
    return Brush.linearGradient(
        colors = listOf(start, end)
    )
}

fun getPrayerPoeticSubtext(prayerName: PrayerName): String {
    return when (prayerName) {
        PrayerName.FAJR -> "First light pierces the quiet dark"
        PrayerName.SUNRISE -> "Dawn opens the sky with morning light"
        PrayerName.DHUHR -> "The sun pauses at its highest peak"
        PrayerName.ASR -> "Shadows begin to stretch and lengthen"
        PrayerName.MAGHRIB -> "Day yields to twilight's glow"
        PrayerName.ISHA -> "Night falls into peaceful stillness"
    }
}


