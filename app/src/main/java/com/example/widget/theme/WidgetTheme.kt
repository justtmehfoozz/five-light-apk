package com.example.widget.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.example.R

/**
 * FiveLight Glance Widget Design System:
 * - Strictly monochrome: black, white, neutral-grey, and their translucent tints.
 * - Floating frosted-glass container with subtle hairline boundary.
 * - Elegant serif typography for contemplation headers, geometric sans-serif for numbers/times.
 */
object WidgetTheme {

    // Monochromatic Semantic Colors
    val textPrimary = ColorProvider(
        day = Color(0xFF1E1D1A),
        night = Color(0xFFF2F2EE)
    )

    val textSecondary = ColorProvider(
        day = Color(0xFF5E5C58),
        night = Color(0xFFA8A8A2)
    )

    val textMuted = ColorProvider(
        day = Color(0xFF8A8882),
        night = Color(0xFF707074)
    )

    val textSubtle = ColorProvider(
        day = Color(0xFFB5B2AA),
        night = Color(0xFF4A4A50)
    )

    val dividerColor = ColorProvider(
        day = Color(0x18000000),
        night = Color(0x22FFFFFF)
    )

    val pillBackground = ColorProvider(
        day = Color(0x12000000),
        night = Color(0x1EFFFFFF)
    )

    val pillBackgroundActive = ColorProvider(
        day = Color(0x22000000),
        night = Color(0x30FFFFFF)
    )

    // Typography Styles
    val titleStyle = TextStyle(
        color = textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Serif
    )

    val heroStyle = TextStyle(
        color = textPrimary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif
    )

    val subHeroStyle = TextStyle(
        color = textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    )

    val bodyStyle = TextStyle(
        color = textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )

    val bodyMutedStyle = TextStyle(
        color = textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )

    val captionStyle = TextStyle(
        color = textMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )

    val arabicStyle = TextStyle(
        color = textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif
    )

    val arabicLargeStyle = TextStyle(
        color = textPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif
    )

    // Container Modifier for Frosted Glass appearance
    fun frostedGlassContainer(paddingDp: Int = 14): GlanceModifier {
        return GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ImageProvider(R.drawable.widget_frosted_glass_bg))
            .padding(paddingDp.dp)
    }
}
