package com.example.widget.core

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

data class FiveLightWidgetColors(
    val isDark: Boolean,
    val background: Color,
    val surfaceSubtle: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textSubtle: Color,
    val accentGold: Color = textPrimary,
    val accentGreen: Color = textPrimary
)

object FiveLightWidgetTheme {

    /**
     * Minimal dark glass palette: pure black, white, obsidian, charcoal,
     * and translucent monochrome shades. Zero dynamic colors or loud accents.
     */
    fun resolveColors(context: Context): FiveLightWidgetColors {
        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        return if (isNight) {
            FiveLightWidgetColors(
                isDark = true,
                background = Color(0xF00E0E12),      // Deep obsidian dark glass
                surfaceSubtle = Color(0x14FFFFFF),   // 8% white glass layer
                border = Color(0x1FFFFFFF),          // 12% subtle white border
                textPrimary = Color(0xFFF2F2F5),     // Soft white
                textSecondary = Color(0xFFA1A1AA),   // Refined muted stone
                textTertiary = Color(0xFF71717A),    // Quiet charcoal
                textSubtle = Color(0xFF52525B),      // Subtle boundary
                accentGold = Color(0xFFF2F2F5),
                accentGreen = Color(0xFFF2F2F5)
            )
        } else {
            FiveLightWidgetColors(
                isDark = false,
                background = Color(0xF5F7F7F9),      // Warm off-white glass
                surfaceSubtle = Color(0x0D000000),   // 5% black subtle surface
                border = Color(0x14000000),          // 8% black border
                textPrimary = Color(0xFF141416),     // Deep obsidian black
                textSecondary = Color(0xFF52525B),   // Slate grey
                textTertiary = Color(0xFF8A8A94),    // Light muted
                textSubtle = Color(0xFFA0A0AB),      // Soft boundary
                accentGold = Color(0xFF141416),
                accentGreen = Color(0xFF141416)
            )
        }
    }

    // Header Typography matching FiveLight (Instrument Serif Italic)
    fun headerStyle(colors: FiveLightWidgetColors, fontSize: Int = 12): TextStyle = TextStyle(
        color = ColorProvider(colors.textSecondary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Serif
    )

    // Main Information (Large, calm, confident)
    fun displayLargeStyle(colors: FiveLightWidgetColors, fontSize: Int = 26): TextStyle = TextStyle(
        color = ColorProvider(colors.textPrimary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    )

    fun displaySerifStyle(colors: FiveLightWidgetColors, fontSize: Int = 22): TextStyle = TextStyle(
        color = ColorProvider(colors.textPrimary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Serif
    )

    // Supporting Information
    fun supportingStyle(colors: FiveLightWidgetColors, fontSize: Int = 13): TextStyle = TextStyle(
        color = ColorProvider(colors.textSecondary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )

    // Metadata / Subtle labels
    fun metadataStyle(colors: FiveLightWidgetColors, fontSize: Int = 11): TextStyle = TextStyle(
        color = ColorProvider(colors.textTertiary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )

    // Arabic Scripture typography (Amiri / Serif)
    fun arabicScriptureStyle(colors: FiveLightWidgetColors, fontSize: Int = 16): TextStyle = TextStyle(
        color = ColorProvider(colors.textPrimary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif
    )

    // Legacy & convenience styles
    fun titleStyle(colors: FiveLightWidgetColors): TextStyle = headerStyle(colors, 12)
    fun subtitleStyle(colors: FiveLightWidgetColors): TextStyle = supportingStyle(colors, 12)
    fun brandStyle(colors: FiveLightWidgetColors): TextStyle = headerStyle(colors, 11)
    fun counterStyle(colors: FiveLightWidgetColors, isLarge: Boolean = false): TextStyle = displayLargeStyle(colors, if (isLarge) 28 else 22)
    fun rowLabelStyle(colors: FiveLightWidgetColors): TextStyle = TextStyle(
        color = ColorProvider(colors.textPrimary),
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )
    fun rowValueStyle(colors: FiveLightWidgetColors): TextStyle = TextStyle(
        color = ColorProvider(colors.textPrimary),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    )
    fun reflectionStyle(colors: FiveLightWidgetColors): TextStyle = supportingStyle(colors, 11)
}

