package com.example.ui.screens

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.model.HijriDate
import com.example.data.model.IslamicDateState
import com.example.data.model.IslamicEvent
import com.example.data.repository.IslamicDateRepository
import com.example.data.util.HijriCalc
import com.example.data.util.MoonPhaseCalculator
import com.example.ui.components.MoonPhaseIndicatorRow
import com.example.ui.components.PageHeader
import com.example.ui.theme.ArabicText
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    hijriDate: HijriDate,
    islamicDateState: IslamicDateState = remember(hijriDate) {
        IslamicDateRepository.getInstance().getCurrentIslamicDateState()
    },
    modifier: Modifier = Modifier
) {
    // Real-time astronomical Moon Phase calculated from current date & time, refreshed periodically
    var liveMoonPhase by remember {
        mutableStateOf(MoonPhaseCalculator.calculateMoonPhase(System.currentTimeMillis()))
    }

    // Continuous real-time periodic update while CalendarScreen is active (refreshes every 60 seconds)
    LaunchedEffect(Unit) {
        liveMoonPhase = MoonPhaseCalculator.calculateMoonPhase(System.currentTimeMillis())
        while (true) {
            delay(60_000L)
            liveMoonPhase = MoonPhaseCalculator.calculateMoonPhase(System.currentTimeMillis())
        }
    }

    val todayGregorian = hijriDate.gregorianDateString.ifEmpty {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
    }

    val cardBg = Color.semanticSurface
    val cardBorder = Color.semanticBorder

    val context = LocalContext.current
    val isReducedMotion = remember {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }

    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }

    var viewedMonth by rememberSaveable { mutableIntStateOf(hijriDate.monthNumber) }
    var viewedYear by rememberSaveable { mutableIntStateOf(hijriDate.year) }
    var slideDirection by rememberSaveable { mutableIntStateOf(1) }

    // Selected date state (independent from device today)
    var selectedDay by rememberSaveable { mutableIntStateOf(hijriDate.day) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(hijriDate.monthNumber) }
    var selectedYear by rememberSaveable { mutableIntStateOf(hijriDate.year) }

    // Precalculate days until for all key Islamic events
    val daysUntilEvents = remember(hijriDate) {
        HijriCalc.getDaysUntilAllEvents(currentHijriDate = hijriDate)
    }

    var popupEvents by remember { mutableStateOf<List<IslamicEvent>>(emptyList()) }
    var showPopup by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var totalDragX by remember { mutableFloatStateOf(0f) }
    val calendarListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    LazyColumn(
        state = calendarListState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Section
        item {
            PageHeader(
                title = "Islamic Calendar",
                subtitle = todayGregorian,
                horizontalPadding = 0.dp
            )
        }

        // Hijri Hero Date Card
        item {
            val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hijri_date_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.semanticSurface),
                border = BorderStroke(1.dp, Color.semanticBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArabicText(
                        text = "${hijriDate.monthArabic} ${hijriDate.year} هـ",
                        fontSize = 30.sp,
                        color = Color.semanticSecondaryText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = HijriCalc.formatHijriString(hijriDate),
                        style = MaterialTheme.typography.headlineLarge.copy(fontFamily = SerifHeaderFont),
                        color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.semanticPrimaryAccent.copy(alpha = 0.15f)
                    ) {
                        val statusText = if (hijriDate.isAfterMaghrib) "Today • After Maghrib" else "Today"
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.semanticPrimaryAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 16.dp, bottom = 14.dp),
                        color = Color.semanticBorder.copy(alpha = 0.6f)
                    )

                    MoonPhaseIndicatorRow(
                        moonPhase = liveMoonPhase,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Clean Minimal Hijri Month Grid View with Synchronized Transitions & Swipe
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(viewedMonth, viewedYear) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDragX += dragAmount
                            },
                            onDragEnd = {
                                if (totalDragX < -45f) {
                                    // Swipe Left -> Next Month
                                    showPopup = false
                                    timerJob?.cancel()
                                    slideDirection = 1
                                    if (viewedMonth == 12) {
                                        viewedMonth = 1
                                        viewedYear += 1
                                    } else {
                                        viewedMonth += 1
                                    }
                                } else if (totalDragX > 45f) {
                                    // Swipe Right -> Previous Month
                                    showPopup = false
                                    timerJob?.cancel()
                                    slideDirection = -1
                                    if (viewedMonth == 1) {
                                        viewedMonth = 12
                                        viewedYear -= 1
                                    } else {
                                        viewedMonth -= 1
                                    }
                                }
                            }
                        )
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Header with Arrows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val interactionSourceLeft = remember { MutableInteractionSource() }
                        val isPressedLeft by interactionSourceLeft.collectIsPressedAsState()
                        val scaleLeft by animateFloatAsState(targetValue = if (isPressedLeft) 0.9f else 1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                        val tintLeft by animateColorAsState(targetValue = if (isPressedLeft) Color.semanticPrimaryAccent else MaterialTheme.colorScheme.onSurface)

                        IconButton(
                            onClick = {
                                showPopup = false
                                timerJob?.cancel()
                                slideDirection = -1
                                if (viewedMonth == 1) {
                                    viewedMonth = 12
                                    viewedYear -= 1
                                } else {
                                    viewedMonth -= 1
                                }
                            },
                            interactionSource = interactionSourceLeft,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scaleLeft
                                scaleY = scaleLeft
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = tintLeft
                            )
                        }

                        // Synchronized month header title
                        AnimatedContent(
                            targetState = Pair(viewedMonth, viewedYear),
                            transitionSpec = {
                                if (isReducedMotion) {
                                    fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                                } else if (slideDirection > 0) {
                                    (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> (width * 0.40f).toInt() } + fadeIn(tween(300))) togetherWith
                                            (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> -(width * 0.40f).toInt() } + fadeOut(tween(260)))
                                } else {
                                    (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> -(width * 0.40f).toInt() } + fadeIn(tween(300))) togetherWith
                                            (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> (width * 0.40f).toInt() } + fadeOut(tween(260)))
                                }
                            },
                            label = "MonthHeaderTransition"
                        ) { (month, year) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${HijriCalc.getMonthNameEn(month)} $year AH",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.semanticPrimaryText
                                )
                                 Text(
                                    text = HijriCalc.getMonthNameAr(month),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.semanticSecondaryText
                                )
                            }
                        }

                        val interactionSourceRight = remember { MutableInteractionSource() }
                        val isPressedRight by interactionSourceRight.collectIsPressedAsState()
                        val scaleRight by animateFloatAsState(targetValue = if (isPressedRight) 0.9f else 1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                        val tintRight by animateColorAsState(targetValue = if (isPressedRight) Color.semanticPrimaryAccent else MaterialTheme.colorScheme.onSurface)

                        IconButton(
                            onClick = {
                                showPopup = false
                                timerJob?.cancel()
                                slideDirection = 1
                                if (viewedMonth == 12) {
                                    viewedMonth = 1
                                    viewedYear += 1
                                } else {
                                    viewedMonth += 1
                                }
                            },
                            interactionSource = interactionSourceRight,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scaleRight
                                scaleY = scaleRight
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Month",
                                tint = tintRight
                            )
                        }
                    }

                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid with Synchronized Slide-Crossfade
                    AnimatedContent(
                        targetState = Pair(viewedMonth, viewedYear),
                        transitionSpec = {
                            if (isReducedMotion) {
                                fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                            } else if (slideDirection > 0) {
                                (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> (width * 0.40f).toInt() } + fadeIn(tween(300))) togetherWith
                                        (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> -(width * 0.40f).toInt() } + fadeOut(tween(260)))
                            } else {
                                (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> -(width * 0.40f).toInt() } + fadeIn(tween(300))) togetherWith
                                        (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { width -> (width * 0.40f).toInt() } + fadeOut(tween(260)))
                            }
                        },
                        label = "CalendarGridTransition"
                    ) { (month, year) ->
                        val currentMonthNameEn = remember(month) { HijriCalc.getMonthNameEn(month) }
                        val daysInMonth = remember(year, month, hijriDate.method) { HijriCalc.getDaysInMonth(year, month, hijriDate.method) }
                        val startWeekday = remember(year, month, hijriDate.method) { HijriCalc.getStartWeekday(year, month, hijriDate.method) }
                        val totalCells = startWeekday + daysInMonth
                        val rows = (totalCells + 6) / 7

                        val monthEventsMap = remember(month, currentMonthNameEn) {
                            (1..30).associateWith { dayNum ->
                                HijriCalc.KEY_ISLAMIC_EVENTS.filter {
                                    (it.hijriMonthNumber == month || it.hijriMonthName.equals(currentMonthNameEn, ignoreCase = true)) && it.hijriDay == dayNum
                                }
                            }
                        }

                        val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
                        val accentColor = Color.semanticPrimaryAccent
                        val selectedFillColor = Color.semanticPrimaryAccent
                        val onSelectedTextColor = Color(0xFFFFFFFF)

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val gridWidth = maxWidth
                            val cellWidth = gridWidth / 7f
                            val cellHeight = 42.dp
                            val rowSpacing = 8.dp

                            val isSelectedInCurrentMonth = month == selectedMonth && year == selectedYear && selectedDay in 1..daysInMonth
                            val selectedCellIndex = if (isSelectedInCurrentMonth) startWeekday + selectedDay - 1 else -1
                            val selectedRow = if (selectedCellIndex >= 0) selectedCellIndex / 7 else 0
                            val selectedCol = if (selectedCellIndex >= 0) selectedCellIndex % 7 else 0

                            Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                                for (row in 0 until rows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        for (col in 0..6) {
                                            val cellIndex = row * 7 + col
                                            val dayNum = cellIndex - startWeekday + 1
                                            if (cellIndex >= startWeekday && dayNum <= daysInMonth) {
                                                val isActualToday = dayNum == hijriDate.day && month == hijriDate.monthNumber && year == hijriDate.year
                                                val isSelected = dayNum == selectedDay && month == selectedMonth && year == selectedYear

                                                val eventsOnDay = monthEventsMap[dayNum] ?: emptyList()
                                                val isKeyDate = eventsOnDay.isNotEmpty()

                                                val isSelectedAnim by animateFloatAsState(
                                                    targetValue = if (isSelected) 1f else 0f, 
                                                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f)
                                                )
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .pointerInput(month, year, dayNum) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    selectedDay = dayNum
                                                                    selectedMonth = month
                                                                    selectedYear = year
                                                                    if (showPopup) {
                                                                        showPopup = false
                                                                        timerJob?.cancel()
                                                                    }
                                                                },
                                                                onLongPress = {
                                                                    selectedDay = dayNum
                                                                    selectedMonth = month
                                                                    selectedYear = year
                                                                    if (eventsOnDay.isNotEmpty()) {
                                                                        timerJob?.cancel()
                                                                        popupEvents = eventsOnDay
                                                                        showPopup = true
                                                                        timerJob = coroutineScope.launch {
                                                                            delay(3500)
                                                                            showPopup = false
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(cellHeight)
                                                            .graphicsLayer {
                                                                val scale = 1f + (isSelectedAnim * 0.05f)
                                                                scaleX = scale
                                                                scaleY = scale
                                                            }
                                                            .drawBehind {
                                                                val outerRadius = (size.minDimension / 2f) - 1.dp.toPx()
                                                                if (isKeyDate && !isSelected) {
                                                                    drawCircle(
                                                                        color = accentColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                                                                        radius = outerRadius,
                                                                        style = Stroke(
                                                                            width = 1.2.dp.toPx(),
                                                                            pathEffect = PathEffect.dashPathEffect(
                                                                                floatArrayOf(4.5.dp.toPx(), 3.5.dp.toPx()),
                                                                                0f
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                
                                                                // Draw Selected Background with animated opacity/scale
                                                                if (isSelectedAnim > 0f) {
                                                                    drawCircle(
                                                                        color = selectedFillColor.copy(alpha = isSelectedAnim),
                                                                        radius = outerRadius
                                                                    )
                                                                }
                                                                
                                                                // Draw Today indicator only if it's not fully selected
                                                                if (isActualToday && isSelectedAnim < 1f) {
                                                                    drawCircle(
                                                                        color = accentColor.copy(alpha = 1f - isSelectedAnim),
                                                                        radius = outerRadius,
                                                                        style = Stroke(width = 1.5.dp.toPx())
                                                                    )
                                                                }
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = "$dayNum",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = when {
                                                                isSelected -> onSelectedTextColor
                                                                isActualToday -> Color.semanticPrimaryText
                                                                isKeyDate -> Color.semanticPrimaryText
                                                                else -> if (isDark) Color.semanticSecondaryText else MaterialTheme.colorScheme.onSurface
                                                            },
                                                            fontWeight = if (isSelected || isActualToday || isKeyDate) FontWeight.Bold else FontWeight.Normal
                                                        )

                                                        if (isKeyDate) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(top = 1.dp)
                                                                    .size(3.5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(
                                                                        when {
                                                                            isSelected -> onSelectedTextColor
                                                                            else -> accentColor
                                                                        }
                                                                    )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }

        // Daily Wisdom & Reflection Card
        item {
            val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.semanticSurface),
                border = BorderStroke(1.dp, Color.semanticBorder)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Daily Reflection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFFFFF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"Unquestionably, by the remembrance of Allah do hearts find rest.\" (Surah Ar-Ra'd 13:28)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SerifHeaderFont),
                            color = if (isDark) Color.semanticSecondaryText else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Key Islamic Events Header
        item {
            val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
            Text(
                text = "Key Islamic Dates",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifHeaderFont),
                color = Color.semanticPrimaryText
            )
        }

        // Part 1 & Part 5: Events List with Countdown Chips & Ramadan Proximity
        items(HijriCalc.KEY_ISLAMIC_EVENTS, key = { it.title }) { event ->
            val daysUntil = daysUntilEvents[event.title] ?: 0
            IslamicEventCard(
                event = event,
                daysUntil = daysUntil
            )
        }

        item {
            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    if (showPopup && popupEvents.isNotEmpty()) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { 
                showPopup = false
                timerJob?.cancel()
            },
            properties = PopupProperties(focusable = true)
        ) {
            var animatedVisible by remember { mutableStateOf(false) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                animatedVisible = true
            }

            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)),
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { 
                                showPopup = false
                                timerJob?.cancel()
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(28.dp)
                            .border(
                                width = 1.dp,
                                color = Color.semanticBorder,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.semanticSurfaceElevated
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            popupEvents.forEachIndexed { index, event ->
                                if (index > 0) {
                                    androidx.compose.material3.HorizontalDivider(
                                        color = Color.semanticBorder
                                    )
                                }
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = event.title,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.semanticPrimaryText,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${event.hijriDay} ${event.hijriMonthName}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.semanticSecondaryText,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        ArabicText(
                                            text = event.arabicTitle,
                                            fontSize = 20.sp,
                                            color = if (isDark) Color.semanticPrimaryText else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.semanticSecondaryText,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicEventCard(
    event: IslamicEvent,
    daysUntil: Int,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val isRamadanBegins = event.title == "Ramadan Begins"
    val isRamadanApproaching = isRamadanBegins && daysUntil in 0..30

    // Continuous proximity intensity for Ramadan: 0f (at 30 days) to 1f (at 0 days)
    val proximityIntensity = if (isRamadanApproaching) {
        ((30 - daysUntil) / 30f).coerceIn(0f, 1f)
    } else {
        0f
    }

    val ramadanGreen = Color.semanticSuccess
    val normalBorderColor = Color.semanticBorder
    val normalCardBg = Color.semanticSurface

    // Part 5.3: Soft green border with continuous proximity intensity
    val cardBorderColor = if (isRamadanApproaching) {
        val borderAlpha = (0.28f + 0.52f * proximityIntensity).coerceIn(0f, 0.85f)
        ramadanGreen.copy(alpha = borderAlpha)
    } else {
        normalBorderColor
    }

    val cardBorderWidth = if (isRamadanApproaching) {
        (1.dp + (0.5.dp * proximityIntensity))
    } else {
        1.dp
    }

    // Part 5.4: Optional subtle background tint
    val bgTintAlpha = if (isRamadanApproaching) {
        (0.02f + 0.05f * proximityIntensity).coerceIn(0f, 0.07f)
    } else {
        0f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = normalCardBg),
        border = BorderStroke(cardBorderWidth, cardBorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isRamadanApproaching) {
                        Modifier.background(ramadanGreen.copy(alpha = bgTintAlpha))
                    } else {
                        Modifier
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRamadanApproaching) {
                            ramadanGreen.copy(alpha = 0.12f + (0.08f * proximityIntensity))
                        } else if (isDark) {
                            Color.semanticSurfaceElevated
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${event.hijriDay}",
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifHeaderFont),
                                color = if (isRamadanApproaching) {
                                    Color.semanticSuccess
                                } else if (isDark) {
                                    Color.semanticPrimaryText
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(
                                text = event.hijriMonthName.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isRamadanApproaching) {
                                    Color.semanticSuccess
                                } else if (isDark) {
                                    Color.semanticSecondaryText
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.semanticPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.semanticSecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Column: Arabic Title + Part 1 & 5 Countdown Chip
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    ArabicText(
                        text = event.arabicTitle,
                        fontSize = 18.sp,
                        color = if (isRamadanApproaching) {
                            Color.semanticSuccess
                        } else if (isDark) {
                            Color(0xFFB0B0AA)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val chipBg = when {
                        isRamadanApproaching -> ramadanGreen.copy(alpha = 0.12f + (0.10f * proximityIntensity))
                        daysUntil == 0 -> if (isDark) Color.semanticPrimaryAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else -> Color.semanticPrimaryAccent.copy(alpha = 0.08f)
                    }

                    val chipTextColor = when {
                        isRamadanApproaching -> Color.semanticSuccess
                        daysUntil == 0 -> Color.semanticPrimaryAccent
                        else -> Color.semanticPrimaryAccent
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = chipBg
                    ) {
                        Text(
                            text = HijriCalc.formatDaysUntil(daysUntil),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = if (daysUntil == 0 || isRamadanApproaching) FontWeight.SemiBold else FontWeight.Medium,
                            color = chipTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
