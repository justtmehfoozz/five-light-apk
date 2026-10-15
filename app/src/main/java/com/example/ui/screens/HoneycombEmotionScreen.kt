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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Job
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
import androidx.compose.ui.input.pointer.util.VelocityTracker
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import kotlin.math.abs
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
// PRECOMPUTED COLOR & GRADIENT STYLING CACHE
// =========================================================================

private data class PrecomputedBubbleStyle(
    val emotion: EmotionItem,
    val baseColor: Color,
    val gradient: Brush,
    val isDarkText: Boolean
)

private fun adjustLightness(color: Color, deltaFactor: Float): Color {
    val r = color.red
    val g = color.green
    val b = color.blue
    val a = color.alpha

    return if (deltaFactor > 0f) {
        Color(
            red = (r + (1f - r) * deltaFactor).coerceIn(0f, 1f),
            green = (g + (1f - g) * deltaFactor).coerceIn(0f, 1f),
            blue = (b + (1f - b) * deltaFactor).coerceIn(0f, 1f),
            alpha = a
        )
    } else {
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
// EXACT COORDINATE-TRANSFORMED HIT-TESTING FOR TAPS
// =========================================================================

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

    val centerX = viewportWidthPx / 2f
    val centerY = viewportHeightPx / 2f

    val S_MAX = 1.06f
    val S_MIN = 0.48f
    val compressionAlpha = 0.12f

    emotions.forEachIndexed { index, emotion ->
        val slot = gridSlots.getOrNull(index) ?: return@forEachIndexed

        val pannedX = slot.logicalX * baseScale + panX
        val pannedY = slot.logicalY * baseScale + panY

        val dx = pannedX
        val dy = pannedY
        val dist = sqrt(dx * dx + dy * dy)

        val normDist = if (rOuterPx > rInnerPx) ((dist - rInnerPx) / (rOuterPx - rInnerPx)).coerceIn(0f, 1f) else 1f
        val falloffFraction = 0.5f * (1.0f + cos(Math.PI * normDist).toFloat())
        val itemScale = S_MIN + (S_MAX - S_MIN) * falloffFraction

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

        val screenX = centerX + compressedDx
        val screenY = centerY + compressedDy

        val renderedDiameterPx = baseDiameterPx * baseScale * itemScale

        val margin = renderedDiameterPx / 2f + 16f
        val isVisible = screenX >= -margin && screenX <= viewportWidthPx + margin &&
                screenY >= -margin && screenY <= viewportHeightPx + margin

        if (isVisible) {
            val dTouchX = tapX - screenX
            val dTouchY = tapY - screenY
            val touchDist = sqrt(dTouchX * dTouchX + dTouchY * dTouchY)
            val radius = renderedDiameterPx / 2f

            if (touchDist <= radius && touchDist < minDistance) {
                minDistance = touchDist
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

    // Predictive back
    val predictiveBackState = rememberPredictiveBackState()
    RegisterPredictiveBackHandler(
        enabled = true,
        backState = predictiveBackState,
        onBack = onBack
    )

    val filteredEmotions = remember { EmotionData.ALL_EMOTIONS }

    // Primitive Float States to drive GPU graphicsLayer transforms with zero recomposition
    val scaleState = remember { mutableFloatStateOf(1.0f) }
    val panXState = remember { mutableFloatStateOf(0f) }
    val panYState = remember { mutableFloatStateOf(0f) }

    var flingJob by remember { mutableStateOf<Job?>(null) }

    val triggerZoomStep: (Float) -> Unit = { factor ->
        val currentScale = scaleState.floatValue
        val newScale = (currentScale * factor).coerceIn(0.70f, 1.80f)
        scaleState.floatValue = newScale
        showZoomBadge = true
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Auto-hide volume zoom badge after 1.1 seconds
    LaunchedEffect(showZoomBadge) {
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
                val rInnerPx = with(density) { 110.dp.toPx() }
                val rOuterPx = kotlin.math.max(viewportWidthPx, viewportHeightPx) * 0.65f

                // Precompute Dense 3-4-3-4 Staggered Row Grid Slots
                val gridSlots = remember(filteredEmotions.size, pitchPx) {
                    generateDense3434RowGrid(filteredEmotions.size, pitchPx)
                }

                // Precompute Bubble Color Styles once per theme to eliminate allocations during drag
                val precomputedStyles = remember(filteredEmotions, isDark) {
                    filteredEmotions.map { emotion ->
                        val bubbleColor = if (isDark) emotion.colorDark else emotion.colorLight
                        val lightColor = adjustLightness(bubbleColor, +0.08f)
                        val darkColor = adjustLightness(bubbleColor, -0.08f)
                        val diamPx = baseDiameterPx

                        val gradient = Brush.radialGradient(
                            colors = listOf(lightColor, bubbleColor, darkColor),
                            center = Offset(diamPx * 0.35f, diamPx * 0.30f),
                            radius = diamPx * 0.85f
                        )
                        PrecomputedBubbleStyle(emotion, bubbleColor, gradient, emotion.isDarkText)
                    }
                }

                val centerX = viewportWidthPx / 2f
                val centerY = viewportHeightPx / 2f
                val boxLeftPx = centerX - baseDiameterPx / 2f
                val boxTopPx = centerY - baseDiameterPx / 2f
                val boxLeftDp = with(density) { boxLeftPx.toDp() }
                val boxTopDp = with(density) { boxTopPx.toDp() }
                val baseDiameterDp = with(density) { baseDiameterPx.toDp() }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            var isDragging = false
                            var totalDrag = Offset.Zero
                            var lastTouchTime = 0L
                            val velocityTracker = VelocityTracker()

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                flingJob?.cancel()

                                lastTouchTime = System.currentTimeMillis()
                                totalDrag = Offset.Zero
                                isDragging = false
                                velocityTracker.resetTracking()
                                velocityTracker.addPosition(down.uptimeMillis, down.position)

                                do {
                                    val event = awaitPointerEvent()
                                    val zoomFactor = event.calculateZoom()
                                    val panAmount = event.calculatePan()

                                    if (event.changes.size > 1 && zoomFactor != 1.0f) {
                                        val currentScale = scaleState.floatValue
                                        val newScale = (currentScale * zoomFactor).coerceIn(0.70f, 1.80f)
                                        scaleState.floatValue = newScale

                                        val (limitX, limitY) = calculatePanLimits(
                                            gridSlots, baseDiameterPx, newScale,
                                            viewportWidthPx, viewportHeightPx, density.density
                                        )
                                        panXState.floatValue = panXState.floatValue.coerceIn(-limitX, limitX)
                                        panYState.floatValue = panYState.floatValue.coerceIn(-limitY, limitY)

                                        isDragging = true
                                        event.changes.forEach { it.consume() }
                                    } else if (panAmount != Offset.Zero) {
                                        totalDrag += panAmount
                                        if (totalDrag.getDistance() > 6.dp.toPx()) {
                                            isDragging = true
                                        }
                                        if (isDragging) {
                                            val currentScale = scaleState.floatValue
                                            val (limitX, limitY) = calculatePanLimits(
                                                gridSlots, baseDiameterPx, currentScale,
                                                viewportWidthPx, viewportHeightPx, density.density
                                            )

                                            val nextPanX = (panXState.floatValue + panAmount.x).coerceIn(-limitX, limitX)
                                            val nextPanY = (panYState.floatValue + panAmount.y).coerceIn(-limitY, limitY)

                                            panXState.floatValue = nextPanX
                                            panYState.floatValue = nextPanY

                                            val change = event.changes.firstOrNull { it.pressed }
                                            if (change != null) {
                                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                            }

                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                val duration = System.currentTimeMillis() - lastTouchTime

                                if (isDragging) {
                                    val velocity = velocityTracker.calculateVelocity()
                                    val vx = velocity.x
                                    val vy = velocity.y
                                    val speed = sqrt(vx * vx + vy * vy)

                                    if (speed > 150f) {
                                        flingJob = coroutineScope.launch {
                                            var currentVx = vx.coerceIn(-4000f, 4000f)
                                            var currentVy = vy.coerceIn(-4000f, 4000f)
                                            var lastNanos = System.nanoTime()

                                            while (abs(currentVx) > 20f || abs(currentVy) > 20f) {
                                                withFrameNanos { frameTime ->
                                                    val dt = (frameTime - lastNanos) / 1_000_000_000f
                                                    lastNanos = frameTime
                                                    if (dt in 0.001f..0.1f) {
                                                        val currentScale = scaleState.floatValue
                                                        val (limitX, limitY) = calculatePanLimits(
                                                            gridSlots, baseDiameterPx, currentScale,
                                                            viewportWidthPx, viewportHeightPx, density.density
                                                        )

                                                        val nextX = (panXState.floatValue + currentVx * dt).coerceIn(-limitX, limitX)
                                                        val nextY = (panYState.floatValue + currentVy * dt).coerceIn(-limitY, limitY)

                                                        if (nextX == -limitX || nextX == limitX) currentVx = 0f
                                                        if (nextY == -limitY || nextY == limitY) currentVy = 0f

                                                        panXState.floatValue = nextX
                                                        panYState.floatValue = nextY

                                                        val decay = 0.88f.pow(dt * 60f)
                                                        currentVx *= decay
                                                        currentVy *= decay
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (duration < 300L) {
                                    val tapX = down.position.x
                                    val tapY = down.position.y

                                    val currentScale = scaleState.floatValue
                                    val currentPanX = panXState.floatValue
                                    val currentPanY = panYState.floatValue

                                    val tapped = findTappedEmotion(
                                        tapX = tapX,
                                        tapY = tapY,
                                        emotions = filteredEmotions,
                                        gridSlots = gridSlots,
                                        baseDiameterPx = baseDiameterPx,
                                        pitchPx = pitchPx,
                                        panX = currentPanX,
                                        panY = currentPanY,
                                        baseScale = currentScale,
                                        viewportWidthPx = viewportWidthPx,
                                        viewportHeightPx = viewportHeightPx,
                                        rInnerPx = rInnerPx * currentScale,
                                        rOuterPx = rOuterPx * currentScale
                                    )

                                    if (tapped != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        pressedEmotionId = tapped.id
                                        coroutineScope.launch {
                                            delay(80L)
                                            pressedEmotionId = null
                                            selectedEmotionForDetail = tapped
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    if (precomputedStyles.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No emotions found",
                                fontFamily = SerifHeaderFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticPrimaryText
                            )
                        }
                    } else {
                        // Render 50 Spatial Honeycomb Bubbles driven by GPU graphicsLayer
                        precomputedStyles.forEachIndexed { index, style ->
                            val slot = gridSlots.getOrNull(index) ?: return@forEachIndexed
                            val emotion = style.emotion
                            val isPressed = pressedEmotionId == emotion.id
                            val isBookmarked = bookmarkedDuaIds.contains(emotion.duaItem.id)

                            Box(
                                modifier = Modifier
                                    .offset(x = boxLeftDp, y = boxTopDp)
                                    .size(baseDiameterDp)
                                    .graphicsLayer {
                                        val px = panXState.floatValue
                                        val py = panYState.floatValue
                                        val sc = scaleState.floatValue

                                        val pannedX = slot.logicalX * sc + px
                                        val pannedY = slot.logicalY * sc + py

                                        val dx = pannedX
                                        val dy = pannedY
                                        val dist = sqrt(dx * dx + dy * dy)

                                        val rInner = rInnerPx * sc
                                        val rOuter = rOuterPx * sc

                                        val S_MAX = 1.06f
                                        val S_MIN = 0.48f

                                        val normDist = if (rOuter > rInner) ((dist - rInner) / (rOuter - rInner)).coerceIn(0f, 1f) else 1f
                                        val falloffFraction = 0.5f * (1.0f + cos(Math.PI * normDist).toFloat())
                                        val itemScale = S_MIN + (S_MAX - S_MIN) * falloffFraction

                                        val compressionAlpha = 0.12f
                                        val compFactor = if (dist > rInner) {
                                            val u = normDist
                                            val compRatio = 1.0f - compressionAlpha * u.toDouble().pow(1.2).toFloat()
                                            val compDist = rInner + (dist - rInner) * compRatio
                                            if (dist > 0.0001f) compDist / dist else 1.0f
                                        } else {
                                            1.0f
                                        }

                                        val compressedDx = dx * compFactor
                                        val compressedDy = dy * compFactor

                                        val screenX = centerX + compressedDx
                                        val screenY = centerY + compressedDy

                                        val renderedDiameter = baseDiameterPx * sc * itemScale
                                        val margin = renderedDiameter / 2f + 16f
                                        val isVisible = screenX >= -margin && screenX <= viewportWidthPx + margin &&
                                                screenY >= -margin && screenY <= viewportHeightPx + margin

                                        if (!isVisible) {
                                            alpha = 0f
                                        } else {
                                            alpha = 1f
                                            translationX = compressedDx
                                            translationY = compressedDy

                                            val totalScale = itemScale * sc * (if (isPressed) 0.92f else 1.0f)
                                            scaleX = totalScale
                                            scaleY = totalScale
                                        }
                                    }
                                    .shadow(
                                        elevation = if (isDark) 3.dp else 2.dp,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .background(style.gradient)
                                    .border(
                                        border = BorderStroke(0.75.dp, Color.White.copy(alpha = if (isDark) 0.12f else 0.22f)),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emotion.name,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = if (emotion.name.length > 10) 9.5.sp else 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (style.isDarkText) Color(0xFF1F2937) else Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )

                                if (isBookmarked) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = "Bookmarked",
                                        tint = if (style.isDarkText) Color(0xFF1F2937).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 4.dp, end = 4.dp)
                                            .size(11.dp)
                                    )
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
                            text = "${(scaleState.floatValue * 100).roundToInt()}%",
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

            Text(
                text = dua.title,
                fontFamily = SerifHeaderFont,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

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
                            fontWeight = FontWeight.Bold,
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
