package com.example.ui.components

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PrayerLogEntity
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import com.example.ui.screens.HandDrawnSkyIcon
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Result model for current prayer window and progress determination.
 */
data class CurrentPrayerProgress(
    val currentPrayer: PrayerName?,
    val progress: Float,
    val windowStartMillis: Long = 0L,
    val windowEndMillis: Long = 0L
)

/**
 * Authoritative function calculating the active prayer window and progress.
 *
 * Windows:
 * - Fajr: Fajr start until Sunrise
 * - Between Sunrise and Dhuhr: no wedge is highlighted
 * - Dhuhr: Dhuhr start until Asr start
 * - Asr: Asr start until Maghrib start
 * - Maghrib: Maghrib start until Isha start
 * - Isha: Isha start until the next Fajr, correctly resolving midnight crossing
 */
fun calculateCurrentPrayerProgress(
    prayerTimes: List<PrayerItem>,
    nowMillis: Long = System.currentTimeMillis()
): CurrentPrayerProgress {
    val fajr = prayerTimes.find { it.name == PrayerName.FAJR }?.timeMillis ?: return CurrentPrayerProgress(null, 0f)
    val sunrise = prayerTimes.find { it.name == PrayerName.SUNRISE }?.timeMillis ?: return CurrentPrayerProgress(null, 0f)
    val dhuhr = prayerTimes.find { it.name == PrayerName.DHUHR }?.timeMillis ?: return CurrentPrayerProgress(null, 0f)
    val asr = prayerTimes.find { it.name == PrayerName.ASR }?.timeMillis ?: return CurrentPrayerProgress(null, 0f)
    val maghrib = prayerTimes.find { it.name == PrayerName.MAGHRIB }?.timeMillis ?: return CurrentPrayerProgress(null, 0f)
    val isha = prayerTimes.find { it.name == PrayerName.ISHA }?.timeMillis ?: return CurrentPrayerProgress(null, 0f)

    return when {
        // 1. Fajr: Fajr start until Sunrise
        nowMillis in fajr until sunrise -> {
            val p = if (sunrise > fajr) ((nowMillis - fajr).toFloat() / (sunrise - fajr).toFloat()).coerceIn(0f, 1f) else 0f
            CurrentPrayerProgress(PrayerName.FAJR, p, fajr, sunrise)
        }
        // 2. Between Sunrise and Dhuhr: NO wedge is highlighted
        nowMillis in sunrise until dhuhr -> {
            CurrentPrayerProgress(null, 0f, sunrise, dhuhr)
        }
        // 3. Dhuhr: Dhuhr start until Asr start
        nowMillis in dhuhr until asr -> {
            val p = if (asr > dhuhr) ((nowMillis - dhuhr).toFloat() / (asr - dhuhr).toFloat()).coerceIn(0f, 1f) else 0f
            CurrentPrayerProgress(PrayerName.DHUHR, p, dhuhr, asr)
        }
        // 4. Asr: Asr start until Maghrib start
        nowMillis in asr until maghrib -> {
            val p = if (maghrib > asr) ((nowMillis - asr).toFloat() / (maghrib - asr).toFloat()).coerceIn(0f, 1f) else 0f
            CurrentPrayerProgress(PrayerName.ASR, p, asr, maghrib)
        }
        // 5. Maghrib: Maghrib start until Isha start
        nowMillis in maghrib until isha -> {
            val p = if (isha > maghrib) ((nowMillis - maghrib).toFloat() / (isha - maghrib).toFloat()).coerceIn(0f, 1f) else 0f
            CurrentPrayerProgress(PrayerName.MAGHRIB, p, maghrib, isha)
        }
        // 6. Isha (evening window, before midnight): Isha start until next day Fajr
        nowMillis >= isha -> {
            val nextFajr = fajr + 24L * 60L * 60L * 1000L
            val p = if (nextFajr > isha) ((nowMillis - isha).toFloat() / (nextFajr - isha).toFloat()).coerceIn(0f, 1f) else 0f
            CurrentPrayerProgress(PrayerName.ISHA, p, isha, nextFajr)
        }
        // 7. Isha (after midnight window, before today's Fajr): Yesterday's Isha until today's Fajr
        else -> {
            val prevIsha = isha - 24L * 60L * 60L * 1000L
            val p = if (fajr > prevIsha) ((nowMillis - prevIsha).toFloat() / (fajr - prevIsha).toFloat()).coerceIn(0f, 1f) else 0f
            CurrentPrayerProgress(PrayerName.ISHA, p, prevIsha, fajr)
        }
    }
}

/**
 * Geometric definition of a single cached pentagon wedge.
 */
private data class CachedWedge(
    val prayerName: PrayerName,
    val centreAngleDeg: Float,
    val startAngleDeg: Float,
    val endAngleDeg: Float,
    val path: Path,
    val contentCenter: Offset,
    val badgeCenter: Offset,
    val startColor: Color,
    val endColor: Color,
    val borderTint: Color
)

/**
 * Premium five-wedge pentagon redesign for the Daily Prayers section.
 */
@Composable
fun DailyPrayersPentagon(
    prayerTimes: List<PrayerItem>,
    todayLog: PrayerLogEntity?,
    todayDateString: String,
    onTogglePrayer: (PrayerName) -> Unit,
    onSetPrayerStatus: (PrayerName, PrayerStatus) -> Unit,
    onShowSnackbar: (message: String, actionLabel: String?, onAction: () -> Unit) -> Unit,
    onShowToast: (message: String) -> Unit,
    isFriday: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Reduced motion accessibility check
    val isReducedMotion = remember(context) {
        try {
            val durationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            val transitionScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            durationScale == 0f || transitionScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // 5 daily Fard prayers in chronological canonical order
    val dailyPrayers = remember(prayerTimes) {
        prayerTimes.filter { it.name != PrayerName.SUNRISE }
    }

    // Recompute current prayer progress every 30 seconds (not every frame)
    var tickerTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            tickerTime = System.currentTimeMillis()
        }
    }

    val currentPrayerInfo = remember(prayerTimes, tickerTime) {
        calculateCurrentPrayerProgress(prayerTimes, tickerTime)
    }

    val animatedFillProgress by animateFloatAsState(
        targetValue = currentPrayerInfo.progress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "pentagon_progress_fill"
    )

    // Pulsing outer glow for the active prayer wedge
    val infiniteTransition = rememberInfiniteTransition(label = "currentWedgeGlow")
    val glowAlphaState = if (isReducedMotion || currentPrayerInfo.currentPrayer == null) {
        remember { mutableFloatStateOf(0.85f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "currentGlowPulse"
        )
    }

    // Interactive wedge scales & shakes
    val wedgeScales = remember {
        PrayerName.entries.associateWith { Animatable(1.0f) }
    }
    val wedgeShakes = remember {
        PrayerName.entries.associateWith { Animatable(0.0f) }
    }

    // Long-press popup menu state
    var menuAnchorOffset by remember { mutableStateOf(Offset.Zero) }
    var menuTargetPrayer by remember { mutableStateOf<PrayerName?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val hubSurfaceColor = Color.semanticSurface
    val primaryTextColor = Color.semanticPrimaryText
    val secondaryTextColor = Color.semanticSecondaryText
    val errorColor = Color.semanticError
    val successColor = Color.semanticSuccess

    // Number of prayers marked Prayed today
    val prayedCount = remember(dailyPrayers, todayLog, todayDateString) {
        dailyPrayers.count { prayer ->
            PrayerLogEntity.resolvePrayerStatus(
                todayLog,
                prayer.name,
                prayer.timeMillis,
                todayDateString,
                todayDateString
            ) == PrayerStatus.PRAYED
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_prayers_pentagon_container"),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        // Pentagon geometry formulas:
        // Circumradius R = min(availableWidth - 32dp, 360dp) / 1.902
        val maxDiameterDp = 360.dp
        val usableWidthDp = (availableWidth - 32.dp).coerceAtMost(maxDiameterDp)
        val circumradiusDp = (usableWidthDp / 1.902f).coerceAtLeast(120.dp)
        val circumradiusPx = with(density) { circumradiusDp.toPx() }
        val hubRadiusPx = 0.26f * circumradiusPx
        val gapDp = 5.dp
        val gapPx = with(density) { gapDp.toPx() }

        // Height of the regular pentagon: from top vertex (-R) to bottom edge (+0.809R) = 1.809R
        val pentagonHeightDp = circumradiusDp * 1.85f
        val pentagonWidthDp = circumradiusDp * 1.902f

        val centerPx = Offset(
            x = with(density) { (pentagonWidthDp / 2).toPx() },
            y = with(density) { (circumradiusDp + 4.dp).toPx() }
        )

        // Cache geometry and wedge paths based on circumradius & theme
        val cachedWedges = remember(circumradiusPx, isDark, isFriday, dailyPrayers) {
            val wedgesList = mutableListOf<CachedWedge>()

            // Clockwise wedge centre angles:
            // Fajr: 234°, Dhuhr: 306°, Asr: 18°, Maghrib: 90°, Isha: 162°
            val wedgeConfigs = listOf(
                Triple(PrayerName.FAJR, 234f, 198f to 270f),
                Triple(PrayerName.DHUHR, 306f, 270f to 342f),
                Triple(PrayerName.ASR, 18f, 342f to 414f),
                Triple(PrayerName.MAGHRIB, 90f, 54f to 126f),
                Triple(PrayerName.ISHA, 162f, 126f to 198f)
            )

            val d = gapPx / 2f // Inset per radial edge (2.5dp)
            val sin36 = sin(Math.toRadians(36.0)).toFloat()
            val cos36 = cos(Math.toRadians(36.0)).toFloat()
            val tan36 = tan(Math.toRadians(36.0)).toFloat()

            for ((prayerName, centreAngle, angleRange) in wedgeConfigs) {
                val (startDeg, endDeg) = angleRange
                val startRad = Math.toRadians(startDeg.toDouble())
                val endRad = Math.toRadians(endDeg.toDouble())
                val midRad = Math.toRadians(centreAngle.toDouble())

                val uMidX = cos(midRad).toFloat()
                val uMidY = sin(midRad).toFloat()

                // Apex where shifted radial edges intersect
                val apexDist = d / sin36
                val apex = Offset(
                    centerPx.x + apexDist * uMidX,
                    centerPx.y + apexDist * uMidY
                )

                // Start radial line inset
                val uStart = Offset(cos(startRad).toFloat(), sin(startRad).toFloat())
                val nStart = Offset(-sin(startRad).toFloat(), cos(startRad).toFloat())
                val sOuter = circumradiusPx - d * tan36
                val vStartInset = Offset(
                    centerPx.x + d * nStart.x + sOuter * uStart.x,
                    centerPx.y + d * nStart.y + sOuter * uStart.y
                )

                // End radial line inset
                val uEnd = Offset(cos(endRad).toFloat(), sin(endRad).toFloat())
                val nEnd = Offset(sin(endRad).toFloat(), -cos(endRad).toFloat())
                val vEndInset = Offset(
                    centerPx.x + d * nEnd.x + sOuter * uEnd.x,
                    centerPx.y + d * nEnd.y + sOuter * uEnd.y
                )

                // Build triangular sector with sharp corners
                val trianglePath = Path().apply {
                    moveTo(apex.x, apex.y)
                    lineTo(vStartInset.x, vStartInset.y)
                    lineTo(vEndInset.x, vEndInset.y)
                    close()
                }

                // Cut out central hub with radius hubR = 0.26R
                val hubPath = Path().apply {
                    addOval(Rect(centerPx, hubRadiusPx))
                }
                val finalWedgePath = Path().apply {
                    op(trianglePath, hubPath, PathOperation.Difference)
                }

                // Content block position at 0.58R
                val contentCenter = Offset(
                    centerPx.x + 0.58f * circumradiusPx * uMidX,
                    centerPx.y + 0.58f * circumradiusPx * uMidY
                )

                // Badge position near outer edge at 0.88R
                val badgeCenter = Offset(
                    centerPx.x + 0.87f * circumradiusPx * uMidX,
                    centerPx.y + 0.87f * circumradiusPx * uMidY
                )

                val prayerItem = dailyPrayers.find { it.name == prayerName }
                val (baseStart, baseEnd) = getSeasonalPrayerGradientColors(
                    prayerName,
                    prayerItem?.timeMillis ?: 0L,
                    isFriday
                )
                val borderTint = adjustColorSubtle(baseStart, lightnessDelta = 0.25f, warmthDelta = 0.1f)

                wedgesList.add(
                    CachedWedge(
                        prayerName = prayerName,
                        centreAngleDeg = centreAngle,
                        startAngleDeg = startDeg,
                        endAngleDeg = endDeg,
                        path = finalWedgePath,
                        contentCenter = contentCenter,
                        badgeCenter = badgeCenter,
                        startColor = baseStart,
                        endColor = baseEnd,
                        borderTint = borderTint
                    )
                )
            }
            wedgesList
        }

        // Action dispatcher for user tap
        val handleWedgeTap: (PrayerName) -> Unit = { tappedPrayer ->
            val prayerItem = dailyPrayers.find { it.name == tappedPrayer }
            val isFuture = prayerItem != null && System.currentTimeMillis() < prayerItem.timeMillis

            if (isFuture) {
                // Future prayer restriction: disable status change, shake, toast
                if (!isReducedMotion) {
                    coroutineScope.launch {
                        val shakeAnim = wedgeShakes[tappedPrayer] ?: return@launch
                        shakeAnim.animateTo(6f, tween(50))
                        shakeAnim.animateTo(-6f, tween(60))
                        shakeAnim.animateTo(4f, tween(60))
                        shakeAnim.animateTo(0f, tween(50))
                    }
                }
                onShowToast("${tappedPrayer.displayName} hasn't started yet")
            } else {
                val currentStatus = PrayerLogEntity.resolvePrayerStatus(
                    todayLog,
                    tappedPrayer,
                    prayerItem?.timeMillis,
                    todayDateString,
                    todayDateString
                )

                // Light haptic feedback
                try {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}

                // Wedge scale animation 1.0 -> 0.97 -> 1.0
                if (!isReducedMotion) {
                    coroutineScope.launch {
                        val scaleAnim = wedgeScales[tappedPrayer] ?: return@launch
                        scaleAnim.animateTo(0.97f, tween(110, easing = FastOutSlowInEasing))
                        scaleAnim.animateTo(1.0f, tween(150, easing = FastOutSlowInEasing))
                    }
                }

                // Tap transitions:
                // Prayed -> Pending (unmarked)
                // Pending / Missed -> Prayed
                if (currentStatus == PrayerStatus.PRAYED) {
                    onSetPrayerStatus(tappedPrayer, PrayerStatus.NEEDS_INPUT)
                    onShowSnackbar(
                        "${tappedPrayer.displayName} unmarked",
                        "Undo"
                    ) {
                        onSetPrayerStatus(tappedPrayer, PrayerStatus.PRAYED)
                    }
                } else {
                    onSetPrayerStatus(tappedPrayer, PrayerStatus.PRAYED)
                    onShowSnackbar(
                        "${tappedPrayer.displayName} marked as prayed",
                        "Undo"
                    ) {
                        onSetPrayerStatus(tappedPrayer, currentStatus)
                    }
                }
            }
        }

        // Action dispatcher for user long-press
        val handleWedgeLongPress: (PrayerName, Offset) -> Unit = { pressedPrayer, touchOffset ->
            val prayerItem = dailyPrayers.find { it.name == pressedPrayer }
            val isFuture = prayerItem != null && System.currentTimeMillis() < prayerItem.timeMillis

            if (isFuture) {
                if (!isReducedMotion) {
                    coroutineScope.launch {
                        val shakeAnim = wedgeShakes[pressedPrayer] ?: return@launch
                        shakeAnim.animateTo(6f, tween(50))
                        shakeAnim.animateTo(-6f, tween(60))
                        shakeAnim.animateTo(4f, tween(60))
                        shakeAnim.animateTo(0f, tween(50))
                    }
                }
                onShowToast("${pressedPrayer.displayName} hasn't started yet")
            } else {
                try {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (_: Exception) {}
                menuAnchorOffset = touchOffset
                menuTargetPrayer = pressedPrayer
                showContextMenu = true
            }
        }

        Box(
            modifier = Modifier
                .size(width = pentagonWidthDp, height = pentagonHeightDp)
                .pointerInput(dailyPrayers, todayLog, isFriday) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downPos = down.position

                        val hitPrayer = hitTestPentagon(
                            downPos,
                            centerPx,
                            circumradiusPx,
                            hubRadiusPx,
                            gapPx
                        )

                        if (hitPrayer != null) {
                            var isLongPressed = false
                            try {
                                withTimeout(450L) {
                                    val up = waitForUpOrCancellation()
                                    if (up != null && !isLongPressed) {
                                        handleWedgeTap(hitPrayer)
                                    }
                                }
                            } catch (_: PointerEventTimeoutCancellationException) {
                                isLongPressed = true
                                handleWedgeLongPress(hitPrayer, downPos)
                            }
                        }
                    }
                }
        ) {
            // 1. CANVAS GEOMETRIC RENDERING
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentWedgePrayer = currentPrayerInfo.currentPrayer
                val currentP = animatedFillProgress
                val currentFillRadius = hubRadiusPx + currentP * (circumradiusPx - hubRadiusPx)
                val glowAlpha = glowAlphaState.value

                for (cached in cachedWedges) {
                    val prayerItem = dailyPrayers.find { it.name == cached.prayerName }
                    val status = PrayerLogEntity.resolvePrayerStatus(
                        todayLog,
                        cached.prayerName,
                        prayerItem?.timeMillis,
                        todayDateString,
                        todayDateString
                    )

                    val isCurrent = (cached.prayerName == currentWedgePrayer)
                    val isPrayed = (status == PrayerStatus.PRAYED)
                    val isMissed = (status == PrayerStatus.MISSED)

                    val midRad = Math.toRadians(cached.centreAngleDeg.toDouble())
                    val uX = cos(midRad).toFloat()
                    val uY = sin(midRad).toFloat()

                    val startOffset = Offset(centerPx.x + hubRadiusPx * uX, centerPx.y + hubRadiusPx * uY)
                    val endOffset = Offset(centerPx.x + circumradiusPx * uX, centerPx.y + circumradiusPx * uY)

                    // A. Layer 1: Restrained, soft outer glow (Current wedge only, clipped outside)
                    if (isCurrent) {
                        val glowBaseColor = cached.borderTint
                        drawPath(
                            path = cached.path,
                            color = glowBaseColor.copy(alpha = 0.08f * glowAlpha),
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = cached.path,
                            color = glowBaseColor.copy(alpha = 0.12f * glowAlpha),
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = cached.path,
                            color = glowBaseColor.copy(alpha = 0.18f * glowAlpha),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // B. Layer 2: Wedge Base & Gradient Fill
                    when {
                        isMissed -> {
                            // Missed: desaturated grey + 45-degree hatch lines
                            val desatGrey = if (isDark) Color(0xFF38383A) else Color(0xFF6E6E73)
                            drawPath(cached.path, color = desatGrey)

                            clipPath(cached.path) {
                                val spacing = 8.dp.toPx()
                                val strokeW = 1.5.dp.toPx()
                                val hatchColor = Color.White.copy(alpha = 0.15f)
                                val bounds = cached.path.getBounds()
                                var offset = bounds.left - bounds.height
                                while (offset < bounds.right + bounds.height) {
                                    drawLine(
                                        color = hatchColor,
                                        start = Offset(offset, bounds.top),
                                        end = Offset(offset + bounds.height, bounds.bottom),
                                        strokeWidth = strokeW
                                    )
                                    offset += spacing
                                }
                            }
                        }

                        isCurrent -> {
                            // Current prayer: muted background + progressive fill up to fillRadius
                            val mutedBrush = Brush.linearGradient(
                                colors = listOf(cached.startColor.copy(alpha = 0.35f), cached.endColor.copy(alpha = 0.35f)),
                                start = startOffset,
                                end = endOffset
                            )
                            drawPath(cached.path, brush = mutedBrush)

                            if (currentFillRadius > hubRadiusPx) {
                                clipPath(cached.path) {
                                    val brightBrush = Brush.linearGradient(
                                        colors = listOf(cached.startColor, cached.endColor),
                                        start = startOffset,
                                        end = endOffset
                                    )
                                    drawCircle(
                                        brush = brightBrush,
                                        radius = currentFillRadius,
                                        center = centerPx
                                    )
                                }
                            }
                        }

                        isPrayed -> {
                            // Prayed: ~85% gradient opacity
                            val prayedBrush = Brush.linearGradient(
                                colors = listOf(cached.startColor.copy(alpha = 0.85f), cached.endColor.copy(alpha = 0.85f)),
                                start = startOffset,
                                end = endOffset
                            )
                            drawPath(cached.path, brush = prayedBrush)
                        }

                        else -> {
                            // Upcoming / Past unrecorded: muted gradient at ~35% alpha
                            val upcomingBrush = Brush.linearGradient(
                                colors = listOf(cached.startColor.copy(alpha = 0.35f), cached.endColor.copy(alpha = 0.35f)),
                                start = startOffset,
                                end = endOffset
                            )
                            drawPath(cached.path, brush = upcomingBrush)
                        }
                    }

                    // C. Layer 3: Highlight 2dp border (Current prayer wedge)
                    if (isCurrent) {
                        drawPath(
                            path = cached.path,
                            color = cached.borderTint,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
                        )
                    }
                }

                // D. Layer 4: Centre Hub & Five-Segment Progress Ring
                // Opaque central hub background
                drawCircle(
                    color = hubSurfaceColor,
                    radius = hubRadiusPx,
                    center = centerPx
                )

                // 5-Segment Progress Ring surrounding the hub
                val ringRadius = hubRadiusPx - 4.dp.toPx()
                val ringStrokePx = 3.5.dp.toPx()
                val ringGapDeg = 8f
                val ringSweepDeg = 72f - ringGapDeg

                for (cached in cachedWedges) {
                    val pItem = dailyPrayers.find { it.name == cached.prayerName }
                    val pStatus = PrayerLogEntity.resolvePrayerStatus(
                        todayLog,
                        cached.prayerName,
                        pItem?.timeMillis,
                        todayDateString,
                        todayDateString
                    )
                    val isSegmentPrayed = (pStatus == PrayerStatus.PRAYED)

                    val segColor = if (isSegmentPrayed) {
                        primaryTextColor
                    } else {
                        primaryTextColor.copy(alpha = 0.18f)
                    }

                    // Start angle centered around centreAngleDeg
                    val segStartDeg = cached.centreAngleDeg - 36f + (ringGapDeg / 2f)

                    drawArc(
                        color = segColor,
                        startAngle = segStartDeg,
                        sweepAngle = ringSweepDeg,
                        useCenter = false,
                        topLeft = Offset(centerPx.x - ringRadius, centerPx.y - ringRadius),
                        size = Size(ringRadius * 2f, ringRadius * 2f),
                        style = Stroke(width = ringStrokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // 2. CENTRAL HUB LABELS (Upright Compose Text)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (centerPx.x - hubRadiusPx).roundToInt(),
                            (centerPx.y - hubRadiusPx).roundToInt()
                        )
                    }
                    .size(with(density) { (hubRadiusPx * 2).toDp() }),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$prayedCount/5",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = primaryTextColor
                    )
                    Text(
                        text = "prayed",
                        fontFamily = SpaceGrotesk,
                        fontSize = 10.sp,
                        color = secondaryTextColor
                    )
                }
            }

            // 3. WEDGE UPRIGHT CONTENT BLOCKS & ACCESSIBILITY NODES
            for (cached in cachedWedges) {
                val prayerItem = dailyPrayers.find { it.name == cached.prayerName }
                val status = PrayerLogEntity.resolvePrayerStatus(
                    todayLog,
                    cached.prayerName,
                    prayerItem?.timeMillis,
                    todayDateString,
                    todayDateString
                )
                val isCurrent = (cached.prayerName == currentPrayerInfo.currentPrayer)
                val isFuture = prayerItem != null && System.currentTimeMillis() < prayerItem.timeMillis

                val scaleAnim = wedgeScales[cached.prayerName]?.value ?: 1f
                val shakeOffset = wedgeShakes[cached.prayerName]?.value ?: 0f

                // TalkBack accessibility description: e.g. "Dhuhr, 12:24 PM, current prayer, prayed"
                val accessibilityDesc = buildString {
                    append(cached.prayerName.displayName)
                    if (prayerItem != null) {
                        append(", ${prayerItem.timeFormatted}")
                    }
                    if (isCurrent) {
                        append(", current prayer")
                    }
                    when (status) {
                        PrayerStatus.PRAYED -> append(", prayed")
                        PrayerStatus.MISSED -> append(", missed")
                        PrayerStatus.FUTURE -> append(", upcoming")
                        else -> append(", pending")
                    }
                }

                // Dedicated TalkBack accessibility semantics node
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (cached.contentCenter.x - 48.dp.toPx() / 2 + shakeOffset.dp.toPx()).roundToInt(),
                                (cached.contentCenter.y - 48.dp.toPx() / 2).roundToInt()
                            )
                        }
                        .size(48.dp)
                        .semantics {
                            contentDescription = accessibilityDesc
                            onClick(label = "Toggle ${cached.prayerName.displayName}") {
                                handleWedgeTap(cached.prayerName)
                                true
                            }
                            onLongClick(label = "Options for ${cached.prayerName.displayName}") {
                                handleWedgeLongPress(cached.prayerName, cached.contentCenter)
                                true
                            }
                        }
                )

                // Upright Content Block (Icon, English, Arabic, Time)
                val contentBlockWidthDp = 92.dp
                val contentBlockHeightDp = 76.dp

                val textColor = when {
                    isCurrent -> Color.White
                    status == PrayerStatus.PRAYED -> Color.White
                    status == PrayerStatus.MISSED -> Color.White.copy(alpha = 0.90f)
                    isDark -> Color.White.copy(alpha = 0.88f)
                    else -> primaryTextColor
                }

                val arabicColor = textColor.copy(alpha = 0.75f)
                val iconColor = textColor

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (cached.contentCenter.x - with(density) { contentBlockWidthDp.toPx() / 2 } + with(density) { shakeOffset.dp.toPx() }).roundToInt(),
                                (cached.contentCenter.y - with(density) { contentBlockHeightDp.toPx() / 2 }).roundToInt()
                            )
                        }
                        .size(width = contentBlockWidthDp, height = contentBlockHeightDp)
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // 1. Existing prayer icon
                        HandDrawnSkyIcon(
                            prayerName = cached.prayerName,
                            isFriday = isFriday,
                            modifier = Modifier.size(20.dp),
                            color = iconColor
                        )

                        // 2. English prayer name (min 15sp, semibold, single line, no wrap)
                        Text(
                            text = cached.prayerName.displayName,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )

                        // 3. Arabic prayer name (min 14sp, AmiriFont)
                        Text(
                            text = cached.prayerName.arabicName,
                            fontFamily = AmiriFont,
                            fontSize = 14.sp,
                            color = arabicColor,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )

                        // 4. Existing formatted prayer time (min 14sp, bold, SpaceGrotesk)
                        Text(
                            text = prayerItem?.timeFormatted ?: "",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 4. Status Badges near outer edge
                if (status == PrayerStatus.PRAYED || status == PrayerStatus.MISSED) {
                    val badgeSizeDp = 18.dp
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (cached.badgeCenter.x - with(density) { badgeSizeDp.toPx() / 2 } + with(density) { shakeOffset.dp.toPx() }).roundToInt(),
                                    (cached.badgeCenter.y - with(density) { badgeSizeDp.toPx() / 2 }).roundToInt()
                                )
                            }
                            .size(badgeSizeDp)
                            .clip(CircleShape)
                            .background(if (status == PrayerStatus.PRAYED) Color.White else errorColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (status == PrayerStatus.PRAYED) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Prayed",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Text(
                                text = "!",
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 5. LONG-PRESS CONTEXT MENU
            if (showContextMenu && menuTargetPrayer != null) {
                val targetPrayer = menuTargetPrayer!!
                val anchorX = with(density) { menuAnchorOffset.x.toDp() }
                val anchorY = with(density) { menuAnchorOffset.y.toDp() }

                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = {
                        showContextMenu = false
                        menuTargetPrayer = null
                    },
                    offset = DpOffset(anchorX, anchorY)
                ) {
                    DropdownMenuItem(
                        text = { Text("Prayed") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = successColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showContextMenu = false
                            onSetPrayerStatus(targetPrayer, PrayerStatus.PRAYED)
                            onShowSnackbar(
                                "${targetPrayer.displayName} marked as prayed",
                                "Undo"
                            ) {
                                onSetPrayerStatus(targetPrayer, PrayerStatus.NEEDS_INPUT)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Missed") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = errorColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showContextMenu = false
                            onSetPrayerStatus(targetPrayer, PrayerStatus.MISSED)
                            onShowSnackbar(
                                "${targetPrayer.displayName} marked as missed",
                                "Undo"
                            ) {
                                onSetPrayerStatus(targetPrayer, PrayerStatus.NEEDS_INPUT)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset (Needs input)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showContextMenu = false
                            onSetPrayerStatus(targetPrayer, PrayerStatus.NEEDS_INPUT)
                            onShowSnackbar(
                                "${targetPrayer.displayName} reset",
                                null
                            ) {}
                        }
                    )
                }
            }
        }
    }
}

/**
 * Mathematically exact point-in-pentagon hit testing.
 * Excludes central hub (r < hubR), gaps between neighboring wedges (5dp gap),
 * and points outside the outer pentagon boundary.
 */
private fun hitTestPentagon(
    point: Offset,
    center: Offset,
    circumradiusPx: Float,
    hubRadiusPx: Float,
    gapPx: Float
): PrayerName? {
    val dx = point.x - center.x
    val dy = point.y - center.y
    val dist = hypot(dx, dy)

    // Taps inside central hub do nothing
    if (dist < hubRadiusPx) return null

    val d = gapPx / 2f // Inset distance (2.5dp)

    // Clockwise wedge configs:
    val wedgeConfigs = listOf(
        Pair(PrayerName.FAJR, 198.0 to 270.0),
        Pair(PrayerName.DHUHR, 270.0 to 342.0),
        Pair(PrayerName.ASR, 342.0 to 414.0),
        Pair(PrayerName.MAGHRIB, 54.0 to 126.0),
        Pair(PrayerName.ISHA, 126.0 to 198.0)
    )

    val cos36 = cos(Math.toRadians(36.0)).toFloat()

    for ((prayerName, range) in wedgeConfigs) {
        val (startDeg, endDeg) = range
        val startRad = Math.toRadians(startDeg)
        val endRad = Math.toRadians(endDeg)
        val midRad = Math.toRadians((startDeg + endDeg) / 2.0)

        // Inward normals
        val naX = -sin(startRad).toFloat()
        val naY = cos(startRad).toFloat()
        val nbX = sin(endRad).toFloat()
        val nbY = -cos(endRad).toFloat()

        // Perpendicular distance to shifted radial edges
        val distA = dx * naX + dy * naY
        val distB = dx * nbX + dy * nbY

        // If outside inward radial lines, point lies in the gap or adjacent wedge
        if (distA < d || distB < d) continue

        // Check outer edge of pentagon
        val uMidX = cos(midRad).toFloat()
        val uMidY = sin(midRad).toFloat()
        val distAlongMid = dx * uMidX + dy * uMidY
        val outerMaxDist = circumradiusPx * cos36

        if (distAlongMid > outerMaxDist) continue

        return prayerName
    }

    return null
}
