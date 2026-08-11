package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityLocation
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BorderDark
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPrayerGradient
import com.example.data.model.PrayerName
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    cityLocation: CityLocation,
    qiblaAngle: Float,
    compassHeading: Float,
    isSensorAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    var isMapFallbackMode by remember { mutableStateOf(!isSensorAvailable) }

    // Smooth spring rotation for compass
    val targetRotation = if (isMapFallbackMode) -qiblaAngle else compassHeading - qiblaAngle
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "compassRotation"
    )

    // Check alignment precision (within 4 degrees)
    val diff = abs((compassHeading - qiblaAngle + 360) % 360)
    val isAligned = !isMapFallbackMode && (diff < 4f || diff > 356f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title in Instrument Serif
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Qibla Compass",
                fontFamily = InstrumentSerifItalic,
                fontStyle = FontStyle.Italic,
                fontSize = 38.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = cityLocation.fullDisplayName,
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Custom Rendered Compass Face Centerpiece
        Box(
            modifier = Modifier
                .size(280.dp)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            val goldColor = Color(0xFFC9A227)
            val ringColor = if (isAligned) goldColor else BorderDark

            // Outer ring canvas & tick marks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 16.dp.toPx()

                // Background ring circle
                drawCircle(
                    color = ringColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                if (isAligned) {
                    drawCircle(
                        color = goldColor.copy(alpha = 0.2f),
                        radius = radius + 10.dp.toPx(),
                        center = center
                    )
                }

                // 12 Cardinal Tick Marks
                for (i in 0 until 12) {
                    val angleDeg = i * 30f
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val innerR = radius - (if (i % 3 == 0) 14.dp.toPx() else 8.dp.toPx())
                    val outerR = radius - 2.dp.toPx()

                    val startX = center.x + innerR * sin(angleRad).toFloat()
                    val startY = center.y - innerR * cos(angleRad).toFloat()
                    val endX = center.x + outerR * sin(angleRad).toFloat()
                    val endY = center.y - outerR * cos(angleRad).toFloat()

                    drawLine(
                        color = if (i % 3 == 0) goldColor else TextSecondary.copy(alpha = 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }
            }

            // Rotating Pointer Dial (Kaaba Pointer)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (isMapFallbackMode) qiblaAngle else -animatedRotation),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pointerLength = size.width / 2f - 32.dp.toPx()

                    // Top Kaaba Pointer (Gold)
                    val path = Path().apply {
                        moveTo(center.x, center.y - pointerLength)
                        lineTo(center.x - 12.dp.toPx(), center.y)
                        lineTo(center.x, center.y - 12.dp.toPx())
                        lineTo(center.x + 12.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(path = path, color = goldColor)

                    // Bottom Pointer (Muted)
                    val bottomPath = Path().apply {
                        moveTo(center.x, center.y + pointerLength)
                        lineTo(center.x - 10.dp.toPx(), center.y)
                        lineTo(center.x, center.y + 10.dp.toPx())
                        lineTo(center.x + 10.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(path = bottomPath, color = SurfaceDark)

                    // Center Pivot Dot
                    drawCircle(color = goldColor, radius = 8.dp.toPx(), center = center)
                }
            }

            // Center Kaaba Emblem / Icon
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = SurfaceDark,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🕋",
                        fontSize = 24.sp
                    )
                }
            }
        }

        // Qibla Status & Degree Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${qiblaAngle.toInt()}° North-East",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                val statusText = when {
                    isMapFallbackMode -> "Static Map Mode (Sensor Unavailable)"
                    isAligned -> "Aligned with Qibla"
                    else -> "Rotate phone toward Makkah"
                }

                Text(
                    text = statusText,
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = if (isAligned) Color(0xFFC9A227) else TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { isMapFallbackMode = !isMapFallbackMode },
                    shape = CircleShape,
                    modifier = Modifier.testTag("toggle_compass_mode_btn")
                ) {
                    Icon(
                        imageVector = if (isMapFallbackMode) Icons.Outlined.CompassCalibration else Icons.Outlined.Map,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMapFallbackMode) "Use Live Compass" else "Use Static Map View",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

