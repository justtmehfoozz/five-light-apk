package com.example.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

/**
 * State architecture for the single continuous Liquid Glass Dock surface.
 */
enum class DockMorphState {
    DOCK,
    MUSIC,
    SEARCH
}

val LIQUID_GLASS_SPRING_DP = spring<Dp>(
    stiffness = 380f,
    dampingRatio = 0.78f
)

val LIQUID_GLASS_SPRING_FLOAT = spring<Float>(
    stiffness = 380f,
    dampingRatio = 0.78f
)

/**
 * Liquid Glass Surface Material.
 * Renders the translucent glass surface (tint fill, specular highlight, border rim, and shadow)
 * without any embedded shader distortion (which is applied to screen content beneath).
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color,
    cornerRadius: Dp,
    borderWidth: Dp = 1.5.dp,
    elevation: Dp = 16.dp,
    spotShadowColor: Color = Color.Black.copy(alpha = 0.25f),
    ambientShadowColor: Color = Color.Black.copy(alpha = 0.12f),
    isMorphing: Boolean = false,
    morphProgress: Float = 0f,
    isDark: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val tintOpacity = if (isDark) 0.40f else 0.30f
    val baseTint = if (isDark) Color(0xFF1E1C25) else Color(0xFFFFFFFF)
    val blendedBackgroundColor = baseTint.copy(alpha = tintOpacity)

    var sizePx by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { sizePx = it.toSize() }
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = spotShadowColor,
                ambientColor = ambientShadowColor
            )
            .clip(shape)
            .background(blendedBackgroundColor)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.32f),
                        if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = if (sizePx.height > 0f) sizePx.height * 0.25f else 50f
                )
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
