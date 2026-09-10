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
     * FiveLight dock frosted glass palette: obsidian charcoal glass in dark mode,
     * warm milk glass in light mode, with optical diffusion, lens refraction, and specular highlight.
     */
    fun resolveColors(context: Context): FiveLightWidgetColors {
        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        return if (isNight) {
            FiveLightWidgetColors(
                isDark = true,
                background = Color(0xDC1C1C1E),      // FiveLight dock charcoal obsidian glass (86% opacity)
                surfaceSubtle = Color(0x14FFFFFF),   // 8% white subtle glass highlight
                border = Color(0x30FFFFFF),          // 19% subtle frosted glass rim
                textPrimary = Color(0xFFF5F5F8),     // Soft luminous white
                textSecondary = Color(0xFFA1A1AB),   // Refined muted stone
                textTertiary = Color(0xFF71717A),    // Quiet charcoal
                textSubtle = Color(0xFF52525B),      // Subtle boundary
                accentGold = Color(0xFFF5F5F8),
                accentGreen = Color(0xFFF5F5F8)
            )
        } else {
            FiveLightWidgetColors(
                isDark = false,
                background = Color(0xE6F8F7F4),      // FiveLight dock warm off-white glass (90% opacity)
                surfaceSubtle = Color(0x0A000000),   // 4% black subtle surface
                border = Color(0x1E000000),          // 12% black soft glass rim
                textPrimary = Color(0xFF141416),     // Deep obsidian black
                textSecondary = Color(0xFF52525B),   // Slate grey
                textTertiary = Color(0xFF8A8A94),    // Light muted
                textSubtle = Color(0xFFA0A0AB),      // Soft boundary
                accentGold = Color(0xFF141416),
                accentGreen = Color(0xFF141416)
            )
        }
    }

    // FiveLight Celestial Signature Mark ✦
    fun celestialMarkStyle(colors: FiveLightWidgetColors, fontSize: Int = 11): TextStyle = TextStyle(
        color = ColorProvider(colors.textTertiary),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif
    )

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

