package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

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
 * Combines backdrop blur, physical lens refraction, specular highlights, and edge dispersion.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    backgroundColor: Color,
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

    // Dynamic morph-aware refraction boost
    val targetRefractionBoost = if (isMorphing) 1.0f else 0.0f
    val refractionAnim by animateFloatAsState(
        targetValue = targetRefractionBoost,
        animationSpec = spring(stiffness = 350f, dampingRatio = 0.8f),
        label = "glassRefractionBoost"
    )

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = spotShadowColor,
                    ambientColor = ambientShadowColor
                )
                .clip(shape)
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(Color.Transparent),
                        blurRadius = 24.dp
                    )
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = shape
                )
        ) {
            // SVG/Canvas Refraction & Specular Edge Simulation Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val radiusPx = cornerRadius.toPx().coerceAtMost(h / 2f).coerceAtMost(w / 2f)

                val refractionStrength = 0.14f + (refractionAnim * 0.18f)
                val specularStrength = if (isDark) {
                    0.28f + (refractionAnim * 0.25f)
                } else {
                    0.40f + (refractionAnim * 0.25f)
                }

                val glassPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = 0f,
                            top = 0f,
                            right = w,
                            bottom = h,
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                    )
                }

                clipPath(glassPath) {
                    // 1. Curved Glass Lens Refraction Gradient (Internal Rim reflection)
                    val rimRefractionBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = refractionStrength * 0.20f) else Color.White.copy(alpha = refractionStrength * 0.45f),
                            if (isDark) Color(0xFFD4AF37).copy(alpha = refractionStrength * 0.15f) else Color(0xFFC5A059).copy(alpha = refractionStrength * 0.25f)
                        ),
                        center = Offset(w * 0.5f, 0f),
                        radius = w * 0.75f
                    )
                    drawRect(brush = rimRefractionBrush)

                    // 2. Chromatic rim separation along horizontal curvature
                    val chromaticBrush = Brush.horizontalGradient(
                        0.0f to if (isDark) Color(0xFF4EE8B0).copy(alpha = refractionStrength * 0.08f) else Color(0xFF3B82F6).copy(alpha = refractionStrength * 0.06f),
                        0.5f to Color.Transparent,
                        1.0f to if (isDark) Color(0xFFD4AF37).copy(alpha = refractionStrength * 0.12f) else Color(0xFFD97706).copy(alpha = refractionStrength * 0.08f)
                    )
                    drawRect(brush = chromaticBrush)

                    // 3. Specular Edge Highlight along Top Arc
                    val specularColor = if (isDark) {
                        Color.White.copy(alpha = specularStrength)
                    } else {
                        Color.White.copy(alpha = specularStrength * 0.85f)
                    }

                    val specularStroke = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = 1f,
                                top = 1f,
                                right = w - 1f,
                                bottom = h - 1f,
                                cornerRadius = CornerRadius((radiusPx - 1f).coerceAtLeast(0f), (radiusPx - 1f).coerceAtLeast(0f))
                            )
                        )
                    }

                    val specularTopBrush = Brush.verticalGradient(
                        0.0f to specularColor,
                        0.35f to specularColor.copy(alpha = specularColor.alpha * 0.4f),
                        0.80f to Color.Transparent,
                        startY = 0f,
                        endY = h * 0.6f
                    )

                    drawPath(
                        path = specularStroke,
                        brush = specularTopBrush,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        // Shared Content inside the single continuous Liquid Glass Surface (unclipped for gestures)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
