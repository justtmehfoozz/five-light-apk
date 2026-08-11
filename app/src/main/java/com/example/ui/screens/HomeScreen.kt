package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.ui.theme.PillInactiveBg
import com.example.ui.theme.PillInactiveBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.BorderLight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PrayerLogEntity
import com.example.data.model.CityLocation
import com.example.data.model.HijriDate
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerName
import com.example.data.util.HijriCalc
import com.example.ui.components.NavItem
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BorderDark
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.PillActiveBg
import com.example.ui.theme.PillActiveText
import com.example.ui.theme.PillInactiveBg
import com.example.ui.theme.PillInactiveBorder
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPrayerGradient
import com.example.ui.theme.getPrayerPoeticSubtext
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    nextPrayer: PrayerItem?,
    prayerTimes: List<PrayerItem>,
    countdownFormatted: String,
    selectedCity: CityLocation,
    hijriDate: HijriDate,
    todayLog: PrayerLogEntity?,
    onTogglePrayer: (PrayerName) -> Unit,
    onQuickAccessNavigate: (NavItem) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPill by remember { mutableStateOf("Today") }

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val pillInactiveBg = if (isDark) PillInactiveBg else SurfaceLight
    val pillInactiveBorder = if (isDark) PillInactiveBorder else BorderLight
    val pillInactiveText = MaterialTheme.colorScheme.onSurfaceVariant
    val pillActiveBg = MaterialTheme.colorScheme.primary
    val pillActiveText = MaterialTheme.colorScheme.onPrimary

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FiveLight",
                        fontFamily = InstrumentSerifItalic,
                        fontStyle = FontStyle.Italic,
                        fontSize = 42.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.testTag("app_wordmark")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { onOpenSettings() }
                                .testTag("header_location_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { onOpenSettings() }
                                .testTag("header_search_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${selectedCity.fullDisplayName}  •  ${HijriCalc.formatHijriString(hijriDate)}",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("header_subline")
                )
            }
        }

        // 2. PILL FILTER ROW (LazyRow with end content padding to prevent clipping)
        item {
            val pills = listOf(
                "Today" to Icons.Filled.Home,
                "Qibla" to Icons.Filled.Explore,
                "Quran" to Icons.Filled.AutoStories,
                "Tasbeeh" to Icons.Filled.RadioButtonChecked,
                "Calendar" to Icons.Filled.CalendarMonth
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pills) { (label, icon) ->
                    val isActive = selectedPill == label
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isActive) pillActiveBg else pillInactiveBg)
                            .border(
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) pillActiveBg else pillInactiveBorder
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedPill = label
                                when (label) {
                                    "Qibla" -> onQuickAccessNavigate(NavItem.QIBLA)
                                    "Quran" -> onQuickAccessNavigate(NavItem.QURAN)
                                    "Tasbeeh" -> onQuickAccessNavigate(NavItem.TASBEEH)
                                    "Calendar" -> onQuickAccessNavigate(NavItem.CALENDAR)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isActive) pillActiveText else pillInactiveText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isActive) pillActiveText else pillInactiveText
                            )
                        }
                    }
                }
            }
        }

            // 3. CURRENT / NEXT PRAYER HIGHLIGHT HERO CARD (Spans Full Width)
            val heroPrayer = nextPrayer ?: prayerTimes.firstOrNull()
            if (heroPrayer != null) {
                val isHeroChecked = when (heroPrayer.name) {
                    PrayerName.FAJR -> todayLog?.fajrCompleted ?: false
                    PrayerName.DHUHR -> todayLog?.dhuhrCompleted ?: false
                    PrayerName.ASR -> todayLog?.asrCompleted ?: false
                    PrayerName.MAGHRIB -> todayLog?.maghribCompleted ?: false
                    PrayerName.ISHA -> todayLog?.ishaCompleted ?: false
                    else -> false
                }

                item {
                    FeaturedPrayerHeroCard(
                        prayer = heroPrayer,
                        countdown = countdownFormatted,
                        isChecked = isHeroChecked,
                        onToggle = { onTogglePrayer(heroPrayer.name) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 4. TODAY'S FIVE PRAYERS 2-COLUMN GRID
            item {
                val dailyPrayers = prayerTimes.filter { it.name != PrayerName.SUNRISE }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Prayers",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Tap circle to mark",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 2-Column Grid Pairings
                    val chunkedPrayers = dailyPrayers.chunked(2)
                    chunkedPrayers.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            pair.forEach { prayer ->
                                val isChecked = when (prayer.name) {
                                    PrayerName.FAJR -> todayLog?.fajrCompleted ?: false
                                    PrayerName.DHUHR -> todayLog?.dhuhrCompleted ?: false
                                    PrayerName.ASR -> todayLog?.asrCompleted ?: false
                                    PrayerName.MAGHRIB -> todayLog?.maghribCompleted ?: false
                                    PrayerName.ISHA -> todayLog?.ishaCompleted ?: false
                                    else -> false
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    PrayerGridCard(
                                        prayer = prayer,
                                        isChecked = isChecked,
                                        onToggle = { onTogglePrayer(prayer.name) }
                                    )
                                }
                            }

                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun FeaturedPrayerHeroCard(
    prayer: PrayerItem,
    countdown: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(getPrayerGradient(prayer.name))
            .drawWithContent {
                drawContent()
                // Top-left radial white highlight overlay
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .padding(20.dp)
            .testTag("featured_hero_card")
    ) {
        // Decorative top-right pin dot accent
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
                .align(Alignment.TopEnd)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HandDrawnSkyIcon(prayerName = prayer.name)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = prayer.name.displayName,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prayer.name.arabicName,
                        fontFamily = AmiriFont,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Next Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Next",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = getPrayerPoeticSubtext(prayer.name),
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.68f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Countdown Centerpiece
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "PRAYER IN",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = countdown,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }

                // Completion Toggle Button & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isChecked) Color.White else Color.Transparent)
                            .border(
                                border = if (isChecked) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                                shape = CircleShape
                            )
                            .clickable { onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = prayer.timeFormatted,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerGridCard(
    prayer: PrayerItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(getPrayerGradient(prayer.name))
            .drawWithContent {
                drawContent()
                // Radial highlight overlay top-left
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .clickable { onToggle() }
            .padding(16.dp)
            .testTag("prayer_card_${prayer.name.id}")
    ) {
        // Top-Right Pin Dot Accent
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
                .align(Alignment.TopEnd)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            HandDrawnSkyIcon(prayerName = prayer.name, modifier = Modifier.size(24.dp))

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prayer.name.displayName,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = prayer.name.arabicName,
                    fontFamily = AmiriFont,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = getPrayerPoeticSubtext(prayer.name),
                fontFamily = SpaceGrotesk,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Checkmark Badge + Prayer Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isChecked) Color.White else Color.Transparent)
                        .border(
                            border = if (isChecked) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                            shape = CircleShape
                        )
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = prayer.timeFormatted,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun HandDrawnSkyIcon(
    prayerName: PrayerName,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)

        when (prayerName) {
            PrayerName.FAJR, PrayerName.SUNRISE -> {
                drawLine(color, Offset(w * 0.15f, h * 0.75f), Offset(w * 0.85f, h * 0.75f), strokeWidth = stroke.width, cap = stroke.cap)
                drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.3f, h * 0.4f), size = Size(w * 0.4f, h * 0.4f), style = stroke)
                drawLine(color, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.3f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(color, Offset(w * 0.28f, h * 0.36f), Offset(w * 0.36f, h * 0.42f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(color, Offset(w * 0.72f, h * 0.36f), Offset(w * 0.64f, h * 0.42f), strokeWidth = stroke.width, cap = stroke.cap)
            }
            PrayerName.DHUHR -> {
                drawCircle(color, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                for (i in 0 until 8) {
                    val angle = Math.toRadians(i * 45.0)
                    val x1 = (w * 0.5f + cos(angle) * w * 0.28f).toFloat()
                    val y1 = (h * 0.5f + sin(angle) * h * 0.28f).toFloat()
                    val x2 = (w * 0.5f + cos(angle) * w * 0.38f).toFloat()
                    val y2 = (h * 0.5f + sin(angle) * h * 0.38f).toFloat()
                    drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke.width, cap = stroke.cap)
                }
            }
            PrayerName.ASR -> {
                drawLine(color, Offset(w * 0.15f, h * 0.8f), Offset(w * 0.85f, h * 0.8f), strokeWidth = stroke.width, cap = stroke.cap)
                drawCircle(color, radius = w * 0.18f, center = Offset(w * 0.32f, h * 0.42f), style = stroke)
                drawLine(color, Offset(w * 0.38f, h * 0.8f), Offset(w * 0.82f, h * 0.8f), strokeWidth = stroke.width + 1.dp.toPx(), cap = stroke.cap)
            }
            PrayerName.MAGHRIB -> {
                drawLine(color, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.65f), strokeWidth = stroke.width, cap = stroke.cap)
                drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.32f, h * 0.48f), size = Size(w * 0.36f, h * 0.36f), style = stroke)
                drawLine(color, Offset(w * 0.5f, h * 0.22f), Offset(w * 0.5f, h * 0.38f), strokeWidth = stroke.width, cap = stroke.cap)
            }
            PrayerName.ISHA -> {
                val moonPath = Path().apply {
                    moveTo(w * 0.6f, h * 0.2f)
                    cubicTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.75f, w * 0.6f, h * 0.8f)
                    cubicTo(w * 0.4f, h * 0.7f, w * 0.4f, h * 0.3f, w * 0.6f, h * 0.2f)
                    close()
                }
                drawPath(moonPath, color, style = stroke)
                drawCircle(color, radius = 1.5.dp.toPx(), center = Offset(w * 0.75f, h * 0.32f))
                drawCircle(color, radius = 1.2.dp.toPx(), center = Offset(w * 0.82f, h * 0.52f))
            }
        }
    }
}

