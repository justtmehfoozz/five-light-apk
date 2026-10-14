package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EmotionCategoryGroup
import com.example.data.model.EmotionItem
import com.example.data.util.EmotionData
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.rememberPredictiveBackState
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.util.LocalVolumeKeyDispatcher
import com.example.ui.util.VolumeKeyEventListener
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

// =========================================================================
// MATHEMATICALLY CONSISTENT 3-4-3-4 STAGGERED HEXAGONAL LATTICE
// =========================================================================

private data class GridSlot(
    val emotionIndex: Int,
    val row: Int,
    val col: Int,
    val logicalX: Float,
    val logicalY: Float
)

private fun generateDense3434RowGrid(count: Int, pitchPx: Float): List<GridSlot> {
    if (count <= 0) return emptyList()

    val slots = mutableListOf<GridSlot>()
    val rowSpacingPx = pitchPx * 0.8660254f // sqrt(3)/2 vertical row pitch

    // Determine row capacities (alternating 3, 4, 3, 4...) for `count` items
    var remaining = count
    var rowIndex = 0
    val rowCapacities = mutableListOf<Int>()

    while (remaining > 0) {
        val capacity = if (rowIndex % 2 == 0) 3 else 4
        val itemsInRow = minOf(remaining, capacity)
        rowCapacities.add(itemsInRow)
        remaining -= itemsInRow
        rowIndex++
    }

    val totalRows = rowCapacities.size

    // Build each row centered vertically at y = 0 and horizontally at x = 0
    var itemIndex = 0
    for (r in 0 until totalRows) {
        val itemsInThisRow = rowCapacities[r]
        val y = (r.toFloat() - (totalRows - 1) / 2.0f) * rowSpacingPx

        for (c in 0 until itemsInThisRow) {
            val x = (c.toFloat() - (itemsInThisRow - 1) / 2.0f) * pitchPx
            slots.add(GridSlot(itemIndex, r, c, x, y))
            itemIndex++
        }
    }

    return slots
}

// =========================================================================
// HELPER: ADJUST LIGHTNESS FOR SUBTLE APPLE WATCH DEPTH GRADIENT
// =========================================================================

private fun adjustLightness(color: Color, deltaFactor: Float): Color {
    val r = color.red
    val g = color.green
    val b = color.blue
    val a = color.alpha

    return if (deltaFactor > 0f) {
        // Lighten: blend towards white
        Color(
            red = (r + (1f - r) * deltaFactor).coerceIn(0f, 1f),
            green = (g + (1f - g) * deltaFactor).coerceIn(0f, 1f),
            blue = (b + (1f - b) * deltaFactor).coerceIn(0f, 1f),
            alpha = a
        )
    } else {
        // Darken: blend towards black
        val mult = (1f + deltaFactor).coerceIn(0f, 1f)
        Color(
            red = (r * mult).coerceIn(0f, 1f),
            green = (g * mult).coerceIn(0f, 1f),
            blue = (b * mult).coerceIn(0f, 1f),
            alpha = a
        )
    }
}

// =========================================================================
// BOUNDED CONTENT PANNING LIMITS CALCULATION
// =========================================================================

private fun calculatePanLimits(
    slots: List<GridSlot>,
    baseDiameterPx: Float,
    baseScale: Float,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    densityPx: Float
): Pair<Float, Float> {
    if (slots.isEmpty()) return Pair(0f, 0f)

    var minX = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE

    slots.forEach { slot ->
        if (slot.logicalX < minX) minX = slot.logicalX
        if (slot.logicalX > maxX) maxX = slot.logicalX
        if (slot.logicalY < minY) minY = slot.logicalY
        if (slot.logicalY > maxY) maxY = slot.logicalY
    }

    val radiusPadding = (baseDiameterPx / 2f) * baseScale * 1.06f + (24f * densityPx)

    val contentWidthPx = (maxX - minX) * baseScale + 2 * radiusPadding
    val contentHeightPx = (maxY - minY) * baseScale + 2 * radiusPadding

    val maxPanX = if (contentWidthPx <= viewportWidthPx) {
        0f
    } else {
        (contentWidthPx - viewportWidthPx) / 2f
    }

    val maxPanY = if (contentHeightPx <= viewportHeightPx) {
        0f
    } else {
        (contentHeightPx - viewportHeightPx) / 2f
    }

    return Pair(maxPanX, maxPanY)
}

// =========================================================================
// FISHEYE TRANSFORM & POSITIONAL EDGE COMPRESSION MODEL
// =========================================================================

private data class TransformedIconState(
    val emotion: EmotionItem,
    val screenX: Float,
    val screenY: Float,
    val scale: Float,
    val renderedDiameterPx: Float,
    val isVisible: Boolean
)

private fun computeTransformedIconState(
    emotion: EmotionItem,
    slot: GridSlot,
    baseDiameterPx: Float,
    pitchPx: Float,
    panX: Float,
    panY: Float,
    baseScale: Float,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    rInnerPx: Float,
    rOuterPx: Float
): TransformedIconState {
    val centerX = viewportWidthPx / 2f
    val centerY = viewportHeightPx / 2f

    // 1. Logical position in grid after panning and base scale magnification
    val pannedX = slot.logicalX * baseScale + panX
    val pannedY = slot.logicalY * baseScale + panY

    // 2. Distance vector relative to viewport focal center
    val dx = pannedX
    val dy = pannedY
    val dist = sqrt(dx * dx + dy * dy)

    // 3. Fisheye Scale falloff calculation:
    // Central zone (dist <= rInnerPx) -> scale = S_MAX (1.06f)
    // Outer zone (dist >= rOuterPx) -> scale = S_MIN (0.48f)
    val S_MAX = 1.06f
    val S_MIN = 0.48f

    val normDist = ((dist - rInnerPx) / (rOuterPx - rInnerPx)).coerceIn(0f, 1f)
    val falloffFraction = 0.5f * (1.0f + cos(Math.PI * normDist).toFloat())
    val scale = S_MIN + (S_MAX - S_MIN) * falloffFraction

    // 4. Positional Edge Compression: max 12% to prevent bubble overlap
    val compressionAlpha = 0.12f
    val compFactor = if (dist > rInnerPx) {
        val u = normDist
        val compRatio = 1.0f - compressionAlpha * u.toDouble().pow(1.2).toFloat()
        val compDist = rInnerPx + (dist - rInnerPx) * compRatio
        if (dist > 0.0001f) compDist / dist else 1.0f
    } else {
        1.0f
    }

    val compressedDx = dx * compFactor
    val compressedDy = dy * compFactor

    // 5. Transformed Screen Center Position
    val screenX = centerX + compressedDx
    val screenY = centerY + compressedDy

    // 6. Rendered Diameter
    val renderedDiameterPx = baseDiameterPx * baseScale * scale

    // 7. Viewport clipping check
    val margin = renderedDiameterPx / 2f + 16f
    val isVisible = screenX >= -margin && screenX <= viewportWidthPx + margin &&
            screenY >= -margin && screenY <= viewportHeightPx + margin

    return TransformedIconState(
        emotion = emotion,
        screenX = screenX,
        screenY = screenY,
        scale = scale,
        renderedDiameterPx = renderedDiameterPx,
        isVisible = isVisible
    )
}

// Exact coordinate-transformed hit-testing
private fun findTappedEmotion(
    tapX: Float,
    tapY: Float,
    emotions: List<EmotionItem>,
    gridSlots: List<GridSlot>,
    baseDiameterPx: Float,
    pitchPx: Float,
    panX: Float,
    panY: Float,
    baseScale: Float,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    rInnerPx: Float,
    rOuterPx: Float
): EmotionItem? {
    var closestEmotion: EmotionItem? = null
    var minDistance = Float.MAX_VALUE

    emotions.forEachIndexed { index, emotion ->
        val slot = gridSlots.getOrNull(index) ?: return@forEachIndexed
        val state = computeTransformedIconState(
            emotion = emotion,
            slot = slot,
            baseDiameterPx = baseDiameterPx,
            pitchPx = pitchPx,
            panX = panX,
            panY = panY,
            baseScale = baseScale,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            rInnerPx = rInnerPx,
            rOuterPx = rOuterPx
        )

        if (state.isVisible) {
            val dx = tapX - state.screenX
            val dy = tapY - state.screenY
            val dist = sqrt(dx * dx + dy * dy)
            val radius = state.renderedDiameterPx / 2f

            if (dist <= radius && dist < minDistance) {
                minDistance = dist
                closestEmotion = emotion
            }
        }
    }
    return closestEmotion
}

// =========================================================================
// MAIN HONEYCOMB EMOTION EXPLORER SCREEN
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoneycombEmotionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val volumeDispatcher = LocalVolumeKeyDispatcher.current
    val density = LocalDensity.current
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }

    var selectedEmotionForDetail by remember { mutableStateOf<EmotionItem?>(null) }
    var pressedEmotionId by remember { mutableStateOf<String?>(null) }
    var showZoomBadge by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val bookmarkedDuaIds by remember(selectedEmotionForDetail) {
        mutableStateOf(getSavedDuaBookmarks(context))
    }

    // Canvas Pan and Zoom state
    var targetScale by remember { mutableFloatStateOf(1.0f) }
    var targetPanX by remember { mutableFloatStateOf(0f) }
    var targetPanY by remember { mutableFloatStateOf(0f) }

    val triggerZoomStep: (Float) -> Unit = { factor ->
        targetScale = (targetScale * factor).coerceIn(0.70f, 1.80f)
        showZoomBadge = true
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Auto-hide volume zoom badge after 1.1 seconds
    LaunchedEffect(targetScale) {
        if (showZoomBadge) {
            delay(1100L)
            showZoomBadge = false
        }
    }

    // Register Volume Up / Down Key Listener for physical button zooming
    DisposableEffect(volumeDispatcher) {
        val listener = object : VolumeKeyEventListener {
            override fun onVolumeKeyDown(keyCode: Int): Boolean {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    triggerZoomStep(1.12f)
                    return true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    triggerZoomStep(0.88f)
                    return true
                }
                return false
            }

            override fun onVolumeKeyUp(keyCode: Int): Boolean {
                return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
            }
        }
        volumeDispatcher?.setVolumeKeyEventListener(listener)
        onDispose {
            volumeDispatcher?.setVolumeKeyEventListener(null)
        }
    }

    // Predictive back
    val predictiveBackState = rememberPredictiveBackState()
    RegisterPredictiveBackHandler(
        enabled = true,
        backState = predictiveBackState,
        onBack = onBack
    )

    // All 50 emotions rendered in the full-screen immersive honeycomb
    val filteredEmotions = remember { EmotionData.ALL_EMOTIONS }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .testTag("honeycomb_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = Color.semanticPrimaryText
                    )
                }
                Text(
                    text = "I am feeling...",
                    fontFamily = SerifHeaderFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.semanticPrimaryText,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Interactive 2D Honeycomb Canvas Container
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .testTag("honeycomb_canvas")
            ) {
                val viewportWidthPx = constraints.maxWidth.toFloat()
                val bottomNavInsetPx = with(density) { 72.dp.toPx() }
                val viewportHeightPx = (constraints.maxHeight.toFloat() - bottomNavInsetPx).coerceAtLeast(100f)

                val baseDiameterPx = with(density) { 80.dp.toPx() }
                val gapPx = with(density) { 10.dp.toPx() }
                val pitchPx = baseDiameterPx + gapPx // Exactly 1.125x diameter
                val rInnerPx = with(density) { 110.dp.toPx() } * targetScale
                val rOuterPx = kotlin.math.max(viewportWidthPx, viewportHeightPx) * 0.65f * targetScale

                // Precompute Dense 3-4-3-4 Staggered Row Grid Slots
                val gridSlots = remember(filteredEmotions.size, pitchPx) {
                    generateDense3434RowGrid(filteredEmotions.size, pitchPx)
                }

                // Compute Content-Based Clamped Pan Limits
                val (maxPanX, maxPanY) = remember(gridSlots, pitchPx, baseDiameterPx, targetScale, viewportWidthPx, viewportHeightPx) {
                    calculatePanLimits(
                        slots = gridSlots,
                        baseDiameterPx = baseDiameterPx,
                        baseScale = targetScale,
                        viewportWidthPx = viewportWidthPx,
                        viewportHeightPx = viewportHeightPx,
                        densityPx = density.density
                    )
                }

                // Ensure current pan target stays strictly within calculated limits
                LaunchedEffect(maxPanX, maxPanY) {
                    targetPanX = targetPanX.coerceIn(-maxPanX, maxPanX)
                    targetPanY = targetPanY.coerceIn(-maxPanY, maxPanY)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredEmotions, targetScale, maxPanX, maxPanY) {
                            var isDragging = false
                            var totalDrag = Offset.Zero
                            var lastTouchTime = 0L

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                lastTouchTime = System.currentTimeMillis()
                                totalDrag = Offset.Zero
                                isDragging = false

                                do {
                                    val event = awaitPointerEvent()
                                    val zoomFactor = event.calculateZoom()
                                    val panAmount = event.calculatePan()

                                    if (event.changes.size > 1 && zoomFactor != 1.0f) {
                                        targetScale = (targetScale * zoomFactor).coerceIn(0.70f, 1.80f)
                                        isDragging = true
                                        event.changes.forEach { it.consume() }
                                    } else if (panAmount != Offset.Zero) {
                                        totalDrag += panAmount
                                        if (totalDrag.getDistance() > 6.dp.toPx()) {
                                            isDragging = true
                                        }
                                        if (isDragging) {
                                            targetPanX = (targetPanX + panAmount.x).coerceIn(-maxPanX, maxPanX)
                                            targetPanY = (targetPanY + panAmount.y).coerceIn(-maxPanY, maxPanY)

                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                val duration = System.currentTimeMillis() - lastTouchTime
                                if (!isDragging && duration < 300L) {
                                    val tapX = down.position.x
                                    val tapY = down.position.y

                                    val tapped = findTappedEmotion(
                                        tapX = tapX,
                                        tapY = tapY,
                                        emotions = filteredEmotions,
                                        gridSlots = gridSlots,
                                        baseDiameterPx = baseDiameterPx,
                                        pitchPx = pitchPx,
                                        panX = targetPanX,
                                        panY = targetPanY,
                                        baseScale = targetScale,
                                        viewportWidthPx = viewportWidthPx,
                                        viewportHeightPx = viewportHeightPx,
                                        rInnerPx = rInnerPx,
                                        rOuterPx = rOuterPx
                                    )

                                    if (tapped != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        pressedEmotionId = tapped.id
                                        coroutineScope.launch {
                                            delay(90L)
                                            pressedEmotionId = null
                                            selectedEmotionForDetail = tapped
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    if (filteredEmotions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "No emotions found",
                                    fontFamily = SerifHeaderFont,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.semanticPrimaryText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Try adjusting your search or category filter",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 13.sp,
                                    color = Color.semanticSecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Render Spatial Apple Watch 3-4-3-4 Staggered Row Honeycomb
                        filteredEmotions.forEachIndexed { index, emotion ->
                            val slot = gridSlots.getOrNull(index) ?: return@forEachIndexed
                            val state = computeTransformedIconState(
                                emotion = emotion,
                                slot = slot,
                                baseDiameterPx = baseDiameterPx,
                                pitchPx = pitchPx,
                                panX = targetPanX,
                                panY = targetPanY,
                                baseScale = targetScale,
                                viewportWidthPx = viewportWidthPx,
                                viewportHeightPx = viewportHeightPx,
                                rInnerPx = rInnerPx,
                                rOuterPx = rOuterPx
                            )

                            if (state.isVisible) {
                                val leftOffset = (state.screenX - state.renderedDiameterPx / 2f).roundToInt()
                                val topOffset = (state.screenY - state.renderedDiameterPx / 2f).roundToInt()
                                val sizeDp = (state.renderedDiameterPx / density.density).dp

                                val bubbleColor = if (isDark) emotion.colorDark else emotion.colorLight
                                val lightColor = adjustLightness(bubbleColor, +0.08f)
                                val darkColor = adjustLightness(bubbleColor, -0.08f)
                                val diamPx = state.renderedDiameterPx

                                val bubbleGradient = Brush.radialGradient(
                                    colors = listOf(
                                        lightColor,
                                        bubbleColor,
                                        darkColor
                                    ),
                                    center = Offset(diamPx * 0.35f, diamPx * 0.30f),
                                    radius = diamPx * 0.85f
                                )

                                val isPressed = pressedEmotionId == emotion.id
                                val pressScale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.92f else 1.0f,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessHigh,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    label = "pressScale"
                                )

                                val isBookmarked = bookmarkedDuaIds.contains(emotion.duaItem.id)

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(leftOffset, topOffset) }
                                        .size(sizeDp)
                                        .graphicsLayer {
                                            scaleX = pressScale
                                            scaleY = pressScale
                                        }
                                        .shadow(
                                            elevation = if (isDark) (3.0f * state.scale).dp else (1.8f * state.scale).dp,
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .background(bubbleGradient)
                                        .border(
                                            border = BorderStroke(0.75.dp, Color.White.copy(alpha = if (isDark) 0.12f else 0.22f)),
                                            shape = CircleShape
                                        )
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Show label when icon scale is large enough for clear readability
                                    if (state.scale >= 0.50f) {
                                        val baseFontSizeSp = if (emotion.name.length > 10) 9.5f else 11.5f
                                        val fontScaleSp = (baseFontSizeSp * state.scale).coerceIn(8.0f, 14.0f).sp

                                        Text(
                                            text = emotion.name,
                                            fontFamily = SpaceGrotesk,
                                            fontSize = fontScaleSp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (emotion.isDarkText) Color(0xFF1F2937) else Color.White,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = fontScaleSp * 1.12f,
                                            modifier = Modifier.padding(horizontal = 3.dp)
                                        )
                                    }

                                    // Bookmark Indicator Badge
                                    if (isBookmarked && state.scale >= 0.55f) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = "Bookmarked",
                                            tint = if (emotion.isDarkText) Color(0xFF1F2937).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = (4f * state.scale).dp, end = (4f * state.scale).dp)
                                                .size((11f * state.scale).dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Volume Zoom Toast Feedback Badge
                AnimatedVisibility(
                    visible = showZoomBadge,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(250)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.semanticSurfaceElevated.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, Color.semanticBorder),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "${(targetScale * 100).roundToInt()}%",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticPrimaryText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Emotion Detail Bottom Sheet
    if (selectedEmotionForDetail != null) {
        EmotionDetailBottomSheet(
            emotion = selectedEmotionForDetail!!,
            onDismiss = { selectedEmotionForDetail = null }
        )
    }
}

// =========================================================================
// EMOTION DUA DETAIL BOTTOM SHEET
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionDetailBottomSheet(
    emotion: EmotionItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }

    val dua = emotion.duaItem
    var isBookmarked by remember { mutableStateOf(getSavedDuaBookmarks(context).contains(dua.id)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.semanticSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            // Emotion Badge Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) emotion.colorDark.copy(alpha = 0.25f) else emotion.colorLight.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, if (isDark) emotion.colorDark else emotion.colorLight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "FEELING: ${emotion.name.uppercase()}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) emotion.colorDark else emotion.colorLight
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.semanticSecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = dua.title,
                fontFamily = SerifHeaderFont,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

            // Sensitive / Suicidal Compassionate Support Card
            if (emotion.isSensitive) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF991B1B).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF991B1B).copy(alpha = 0.40f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "You are deeply valued, loved, and not alone.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "In Islam, your life is sacred. If you are experiencing overwhelming emotional distress, please reach out for compassionate support immediately.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = Color.semanticPrimaryText,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
                                    val activity = context.findActivity()
                                    if (activity != null) {
                                        activity.startActivity(intent)
                                    } else {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhoneInTalk,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call Crisis Helpline (988)",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic Supplication Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.semanticSurfaceElevated,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = dua.arabic,
                        fontFamily = SerifHeaderFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticPrimaryAccent,
                        textAlign = TextAlign.Right,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = dua.transliteration,
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryText,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"${dua.translation}\"",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 19.sp
                    )

                    if (!dua.reference.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Source: ${dua.reference} (${dua.authenticityGrade})",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryAccent
                        )
                    }

                    if (!dua.benefitOrNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: ${dua.benefitOrNotes}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            color = Color.semanticSecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Bookmark, Copy, Share
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isBookmarked = toggleDuaBookmark(context, dua.id)
                        Toast.makeText(
                            context,
                            if (isBookmarked) "Saved to Bookmarks" else "Removed from Bookmarks",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isBookmarked) Color.semanticPrimaryAccent else Color.semanticPrimaryText
                    ),
                    border = BorderStroke(1.dp, if (isBookmarked) Color.semanticPrimaryAccent else Color.semanticBorder),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBookmarked) "Saved" else "Bookmark",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        clipboardManager.setText(AnnotatedString("${dua.title}\n\n${dua.arabic}\n\n${dua.translation}\n\nReference: ${dua.reference ?: ""}"))
                        Toast.makeText(context, "Dua copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.semanticPrimaryText),
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        shareDuaText(context, dua)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Share",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
