package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.skydoves.cloudy.liquidGlass
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassIsolationTestScreen(
    onBack: () -> Unit
) {
    // Mode toggle: True = Official Cloudy direct-content container pattern, False = Floating Haze overlay pattern
    var isDirectContainerMode by remember { mutableStateOf(true) }
    
    // Live adjustable shader parameters
    var refraction by remember { mutableFloatStateOf(1.0f) }
    var curve by remember { mutableFloatStateOf(1.5f) }
    var dispersion by remember { mutableFloatStateOf(0.15f) }
    var saturation by remember { mutableFloatStateOf(1.3f) }
    var contrast by remember { mutableFloatStateOf(1.1f) }
    var edge by remember { mutableFloatStateOf(0.5f) }
    var lensWidthPx by remember { mutableFloatStateOf(650f) }
    var lensHeightPx by remember { mutableFloatStateOf(200f) }
    var lensCornerRadius by remember { mutableFloatStateOf(100f) }
    
    // Interactive draggable lens center
    var lensCenter by remember { mutableStateOf(Offset(500f, 600f)) }
    
    val hazeState = remember { HazeState() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cloudy Shader Isolation Test", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "SDK ${Build.VERSION.SDK_INT} | API33+ = ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        refraction = 1.0f
                        curve = 1.5f
                        dispersion = 0.15f
                        saturation = 1.3f
                        contrast = 1.1f
                        edge = 0.5f
                        lensCenter = Offset(500f, 600f)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode & info header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDirectContainerMode) 
                                "Mode: Direct Container (Cloudy Official Sample)" 
                            else 
                                "Mode: Floating Haze Backdrop Overlay",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Switch(
                            checked = isDirectContainerMode,
                            onCheckedChange = { isDirectContainerMode = it }
                        )
                    }
                    Text(
                        text = if (isDirectContainerMode)
                            "• Content is INSIDE the Box that has Modifier.liquidGlass(). Drag around on the canvas to move the lens."
                        else
                            "• Content has Modifier.haze(), overlay Box has Modifier.hazeChild().liquidGlass().",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Test Canvas Area (Interactive & Touch-Draggable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            lensCenter = Offset(
                                x = (lensCenter.x + dragAmount.x).coerceIn(100f, 900f),
                                y = (lensCenter.y + dragAmount.y).coerceIn(100f, 1200f)
                            )
                        }
                    }
            ) {
                if (isDirectContainerMode) {
                    // OFFICIAL CLOUDY PATTERN: Modifier.liquidGlass() directly on the container of the content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .liquidGlass(
                                lensCenter = lensCenter,
                                lensSize = Size(lensWidthPx, lensHeightPx),
                                cornerRadius = lensCornerRadius,
                                refraction = refraction,
                                curve = curve,
                                dispersion = dispersion,
                                saturation = saturation,
                                contrast = contrast,
                                tint = Color.Transparent,
                                edge = edge
                            )
                    ) {
                        HighContrastTestContent()
                    }
                } else {
                    // FLOATING OVERLAY PATTERN: Haze backdrop + hazeChild + liquidGlass
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .haze(hazeState)
                    ) {
                        HighContrastTestContent()

                        // Floating Glass Capsule positioned at lensCenter
                        val halfW = (lensWidthPx / LocalDensity.current.density).dp / 2
                        val halfH = (lensHeightPx / LocalDensity.current.density).dp / 2
                        val centerXDp = (lensCenter.x / LocalDensity.current.density).dp
                        val centerYDp = (lensCenter.y / LocalDensity.current.density).dp

                        Box(
                            modifier = Modifier
                                .size(
                                    width = (lensWidthPx / LocalDensity.current.density).dp,
                                    height = (lensHeightPx / LocalDensity.current.density).dp
                                )
                                .padding(
                                    start = (centerXDp - halfW).coerceAtLeast(0.dp),
                                    top = (centerYDp - halfH).coerceAtLeast(0.dp)
                                )
                                .shadow(8.dp, RoundedCornerShape((lensCornerRadius / LocalDensity.current.density).dp))
                                .clip(RoundedCornerShape((lensCornerRadius / LocalDensity.current.density).dp))
                                .hazeChild(
                                    state = hazeState,
                                    style = HazeStyle(
                                        backgroundColor = Color.Transparent,
                                        tint = HazeTint(Color.Transparent),
                                        blurRadius = 0.dp
                                    )
                                )
                                .liquidGlass(
                                    lensCenter = Offset(lensWidthPx / 2f, lensHeightPx / 2f),
                                    lensSize = Size(lensWidthPx, lensHeightPx),
                                    cornerRadius = lensCornerRadius,
                                    refraction = refraction,
                                    curve = curve,
                                    dispersion = dispersion,
                                    saturation = saturation,
                                    contrast = contrast,
                                    tint = Color.Transparent,
                                    edge = edge
                                )
                                .border(
                                    1.5.dp,
                                    Color.White.copy(alpha = 0.6f),
                                    RoundedCornerShape((lensCornerRadius / LocalDensity.current.density).dp)
                                )
                        )
                    }
                }
            }

            // Live tuning sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Refraction Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Refraction: ${"%.2f".format(refraction)}", style = MaterialTheme.typography.labelMedium)
                    Text("Curve: ${"%.2f".format(curve)}", style = MaterialTheme.typography.labelMedium)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = refraction,
                        onValueChange = { refraction = it },
                        valueRange = 0.0f..2.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = curve,
                        onValueChange = { curve = it },
                        valueRange = 0.0f..3.0f,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dispersion & Edge Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dispersion: ${"%.2f".format(dispersion)}", style = MaterialTheme.typography.labelMedium)
                    Text("Edge: ${"%.2f".format(edge)}", style = MaterialTheme.typography.labelMedium)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = dispersion,
                        onValueChange = { dispersion = it },
                        valueRange = 0.0f..0.5f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = edge,
                        onValueChange = { edge = it },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Lens Width & Height Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lens Width: ${lensWidthPx.toInt()}px", style = MaterialTheme.typography.labelMedium)
                    Text("Lens Height: ${lensHeightPx.toInt()}px", style = MaterialTheme.typography.labelMedium)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = lensWidthPx,
                        onValueChange = { lensWidthPx = it },
                        valueRange = 200f..1000f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = lensHeightPx,
                        onValueChange = { lensHeightPx = it },
                        valueRange = 100f..500f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HighContrastTestContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF311042)
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // High contrast bold test typography
        Text(
            text = "TAHAJJUD WINDOW",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF38BDF8),
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "02:08 AM",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFDE047),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "05:08 AM",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4ADE80),
                fontFamily = FontFamily.Monospace
            )
        }

        // Geometric high-contrast colored pattern blocks for clear optical bending verification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFEF4444), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFF97316), RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFA855F7), RoundedCornerShape(16.dp))
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF06B6D4), CircleShape)
            )
        }

        // High frequency grid lines & stripes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            if (index % 2 == 0) Color.White.copy(alpha = 0.9f)
                            else Color(0xFF38BDF8)
                        )
                )
            }
        }

        Text(
            text = "BENDING / DISPLACEMENT BENCHMARK GRID",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
    }
}
