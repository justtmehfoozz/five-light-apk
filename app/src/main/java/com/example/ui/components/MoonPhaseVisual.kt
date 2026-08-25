package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoonPhase
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurfaceElevated

/**
 * Geometric Vector Moon Phase visual.
 *
 * Renders an exact, static astronomical terminator curve corresponding to [MoonPhase.phaseAngle].
 * Visible with high contrast across both Light and Dark themes without arbitrary glow or cartoons.
 */
@Composable
fun MoonPhaseVisual(
    moonPhase: MoonPhase,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val isDark = MaterialTheme.colorScheme.background.run {
        (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
    }

    // High contrast theme tokens
    val unlitColor = if (isDark) Color(0xFF1E242B) else Color(0xFFE2E8F0)
    val litColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF334155)
    val borderColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)

    Canvas(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics {
                contentDescription = moonPhase.accessibleDescription
            }
            .testTag("moon_phase_canvas")
    ) {
        val radius = this.size.minDimension / 2f
        val center = Offset(radius, radius)

        // 1. Draw base unlit disc
        drawCircle(
            color = unlitColor,
            radius = radius,
            center = center
        )

        // 2. Draw illuminated terminator path
        val progress = moonPhase.phaseAngle.coerceIn(0f, 1f)

        when {
            // New Moon: completely unlit disc
            progress < 0.02f || progress >= 0.98f -> {
                // No lit path needed
            }
            // Full Moon: completely lit disc
            progress in 0.48f..0.52f -> {
                drawCircle(
                    color = litColor,
                    radius = radius,
                    center = center
                )
            }
            // Waxing: Lit on the right limb (0.0 < progress < 0.5)
            progress < 0.5f -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    // Outer right semi-circle arc
                    arcTo(
                        rect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    // Return along the terminator curve
                    if (progress < 0.25f) {
                        // Crescent: inner terminator curves to the right
                        val k = (1f - (progress / 0.25f)).coerceIn(0f, 1f)
                        val rx = (radius * k).coerceAtLeast(0.1f)
                        arcTo(
                            rect = Rect(center.x - rx, center.y - radius, center.x + rx, center.y + radius),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                    } else {
                        // Gibbous: inner terminator curves to the left
                        val k = ((progress - 0.25f) / 0.25f).coerceIn(0f, 1f)
                        val rx = (radius * k).coerceAtLeast(0.1f)
                        arcTo(
                            rect = Rect(center.x - rx, center.y - radius, center.x + rx, center.y + radius),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -180f,
                            forceMoveTo = false
                        )
                    }
                    close()
                }
                drawPath(path = path, color = litColor)
            }
            // Waning: Lit on the left limb (0.5 <= progress < 1.0)
            else -> {
                val p = progress - 0.5f
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    // Outer left semi-circle arc
                    arcTo(
                        rect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    // Return along the terminator curve
                    if (p < 0.25f) {
                        // Waning Gibbous: inner terminator curves to the right
                        val k = (1f - (p / 0.25f)).coerceIn(0f, 1f)
                        val rx = (radius * k).coerceAtLeast(0.1f)
                        arcTo(
                            rect = Rect(center.x - rx, center.y - radius, center.x + rx, center.y + radius),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                    } else {
                        // Waning Crescent: inner terminator curves to the left
                        val k = ((p - 0.25f) / 0.25f).coerceIn(0f, 1f)
                        val rx = (radius * k).coerceAtLeast(0.1f)
                        arcTo(
                            rect = Rect(center.x - rx, center.y - radius, center.x + rx, center.y + radius),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -180f,
                            forceMoveTo = false
                        )
                    }
                    close()
                }
                drawPath(path = path, color = litColor)
            }
        }

        // 3. Crisp subtle outer disc boundary
        drawCircle(
            color = borderColor.copy(alpha = if (isDark) 0.6f else 0.45f),
            radius = radius - 0.5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Subtle, premium Moon Phase indicator component for the Hijri Calendar.
 *
 * Displays the astronomical lunar phase with subtle state crossfade and
 * clearly distinguishes astronomical computation from official Hijri calendar date.
 */
@Composable
fun MoonPhaseIndicatorRow(
    moonPhase: MoonPhase,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("moon_phase_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Crossfade(
                targetState = moonPhase,
                animationSpec = tween(durationMillis = 280),
                label = "MoonPhaseVisualCrossfade"
            ) { targetPhase ->
                MoonPhaseVisual(
                    moonPhase = targetPhase,
                    size = 28.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "ASTRONOMICAL MOON PHASE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.semanticMutedText
                )

                Spacer(modifier = Modifier.height(1.dp))

                Crossfade(
                    targetState = moonPhase,
                    animationSpec = tween(durationMillis = 280),
                    label = "MoonPhaseNameCrossfade"
                ) { targetPhase ->
                    Text(
                        text = "${targetPhase.emoji} ${targetPhase.phaseName}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.semanticPrimaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Illumination % Pill
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.semanticSurfaceElevated,
            border = BorderStroke(1.dp, Color.semanticBorder)
        ) {
            Crossfade(
                targetState = moonPhase.illuminationPercent,
                animationSpec = tween(durationMillis = 250),
                label = "MoonIlluminationCrossfade"
            ) { percent ->
                Text(
                    text = "$percent% illuminated",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.semanticSecondaryText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
