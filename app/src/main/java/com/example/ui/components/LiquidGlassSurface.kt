package com.example.ui.components

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.skydoves.cloudy.liquidGlass
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
    
    val tintOpacity = if (isDark) 0.45f else 0.35f
    val baseTint = if (isDark) Color(0xFF1E1C25) else Color(0xFFFFFFFF)
    val blendedBackgroundColor = baseTint.copy(alpha = tintOpacity)

    var sizePx by remember { mutableStateOf(Size.Zero) }
    val cornerRadiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    val context = LocalContext.current

    LaunchedEffect(sizePx) {
        if (sizePx != Size.Zero) {
            val isApi33 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            Toast.makeText(
                context,
                "Debug: SDK ${Build.VERSION.SDK_INT} (API33+=$isApi33) | Refraction=1.0, Curve=2.0 | Size=${sizePx.width.toInt()}x${sizePx.height.toInt()}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Box(
        modifier = modifier.onSizeChanged { sizePx = it.toSize() }
    ) {
        val glassModifier = if (sizePx != Size.Zero) {
            Modifier.liquidGlass(
                lensCenter = Offset(sizePx.width / 2f, sizePx.height / 2f),
                lensSize = sizePx,
                cornerRadius = cornerRadiusPx,
                refraction = 1.0f,
                curve = 2.0f,
                dispersion = 0.15f,
                saturation = 1.3f,
                contrast = 1.1f,
                tint = blendedBackgroundColor,
                edge = if (isDark) 0.7f else 0.6f
            )
        } else Modifier

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
                        backgroundColor = Color.Transparent, // Let liquidGlass handle the tint
                        tint = HazeTint(Color.Transparent),
                        blurRadius = 0.dp
                    )
                )
                .then(glassModifier)
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
                )
        )

        // Shared Content inside the single continuous Liquid Glass Surface (unclipped for gestures)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

