with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if line.startswith("fun PersonalLogSheet("):
        start_idx = i
        break

# The new content to append
new_code = """fun PersonalLogSheet(
    overview: com.example.data.model.WeeklyWorshipOverview?,
    todayLog: com.example.data.db.PrayerLogEntity?,
    allPrayerLogs: List<com.example.data.db.PrayerLogEntity>,
    qadaCounts: Map<com.example.data.model.PrayerName, Int>,
    onUpdateQadaCount: (com.example.data.model.PrayerName, Int) -> Unit,
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(PersonalLogTab.LOG) }
    
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("personal_log_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Personal Log",
                        fontFamily = com.example.ui.theme.SerifHeaderFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Unified Fard Prayer Records",
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tabs
            val tabs = listOf(
                com.example.ui.screens.PillItem(PersonalLogTab.LOG, "Log"),
                com.example.ui.screens.PillItem(PersonalLogTab.INSIGHTS, "Insights"),
                com.example.ui.screens.PillItem(PersonalLogTab.QADA, "Qada")
            )
            com.example.ui.screens.SpringPillSelector(
                items = tabs,
                selectedItem = currentTab,
                onItemSelected = { currentTab = it }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            androidx.compose.animation.Crossfade(targetState = currentTab) { tab ->
                when (tab) {
                    PersonalLogTab.LOG -> {
                        PersonalLogTabContent(
                            overview = overview,
                            onSetPrayerStatus = onSetPrayerStatus
                        )
                    }
                    PersonalLogTab.INSIGHTS -> {
                        PersonalLogInsightsContent(allPrayerLogs = allPrayerLogs)
                    }
                    PersonalLogTab.QADA -> {
                        PersonalLogQadaContent(
                            qadaCounts = qadaCounts,
                            onUpdateQadaCount = onUpdateQadaCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalLogTabContent(
    overview: com.example.data.model.WeeklyWorshipOverview?,
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit
) {
    val days = overview?.days ?: emptyList()
    var selectedDateString by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }

    val todayDateString = androidx.compose.runtime.remember(days) { days.find { it.isToday }?.dateString }
    val effectiveDateString = selectedDateString ?: todayDateString ?: days.firstOrNull()?.dateString

    val selectedDay = days.find { it.dateString == effectiveDateString } ?: days.find { it.isToday } ?: days.firstOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (days.isNotEmpty()) {
            val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    val isSelected = day.dateString == effectiveDateString
                    val dayPillColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        day.isToday -> if (isDarkTheme) Color(0xFF2E2B25) else MaterialTheme.colorScheme.primaryContainer
                        isDarkTheme -> Color(0xFF1E1E1E)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    }
                    val contentColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        day.isToday -> if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                        isDarkTheme -> Color(0xFFDCD6CC)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    val pillBorder = when {
                        isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        day.isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        isDarkTheme -> BorderStroke(1.dp, Color(0xFF333333))
                        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    val dayNum = try {
                        day.dateString.substringAfterLast("-")
                    } catch (e: Exception) { "" }

                    Surface(
                        onClick = { selectedDateString = day.dateString },
                        shape = RoundedCornerShape(12.dp),
                        color = dayPillColor,
                        border = pillBorder,
                        modifier = Modifier.width(42.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = day.dayOfWeekName.take(3),
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayNum,
                                fontFamily = com.example.ui.theme.SpaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(contentColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedDay != null) {
            val isFridayDay = selectedDay.dayOfWeekName.startsWith("Fri")
            Text(
                text = if (selectedDay.isToday) "Today's Prayers" else "${selectedDay.dayOfWeekName}, ${selectedDay.dateString}",
                fontFamily = com.example.ui.theme.SerifHeaderFont,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            val dhuhrTitle = com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(com.example.data.model.PrayerName.DHUHR, isFridayDay)
            val prayerItems = listOf(
                Triple(com.example.data.model.PrayerName.FAJR, "Fajr", selectedDay.fajrStatus),
                Triple(com.example.data.model.PrayerName.DHUHR, dhuhrTitle, selectedDay.dhuhrStatus),
                Triple(com.example.data.model.PrayerName.ASR, "Asr", selectedDay.asrStatus),
                Triple(com.example.data.model.PrayerName.MAGHRIB, "Maghrib", selectedDay.maghribStatus),
                Triple(com.example.data.model.PrayerName.ISHA, "Isha", selectedDay.ishaStatus)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                prayerItems.forEach { (pEnum, pDisplayName, status) ->
                    PersonalLogPrayerRow(
                        prayerName = pEnum,
                        displayName = pDisplayName,
                        status = status,
                        onSetStatus = { newStatus ->
                            onSetPrayerStatus(pEnum, selectedDay.dateString, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalLogInsightsContent(allPrayerLogs: List<com.example.data.db.PrayerLogEntity>) {
    if (allPrayerLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Not enough data yet",
                    fontFamily = com.example.ui.theme.SerifHeaderFont,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Keep logging your prayers to see your consistency insights.",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    var currentStreak = 0
    var previousDate = java.time.LocalDate.now()
    
    val sortedLogs = allPrayerLogs.sortedByDescending { it.date }
    for (log in sortedLogs) {
        val logDate = java.time.LocalDate.parse(log.date)
        if (logDate.isAfter(previousDate)) continue
        if (java.time.temporal.ChronoUnit.DAYS.between(logDate, previousDate) > 1 && currentStreak > 0) {
            break
        }
        val allCompleted = log.fajrCompleted && log.dhuhrCompleted && log.asrCompleted && log.maghribCompleted && log.ishaCompleted
        if (allCompleted) {
            currentStreak++
            previousDate = logDate
        } else {
            if (currentStreak > 0 || logDate.isBefore(java.time.LocalDate.now())) break
        }
    }

    val weekAgo = java.time.LocalDate.now().minusDays(7)
    val weekLogs = allPrayerLogs.filter { java.time.LocalDate.parse(it.date).isAfter(weekAgo) }
    var totalPrayersWeek = 0
    var completedPrayersWeek = 0
    var fajrComp = 0; var dhuhrComp = 0; var asrComp = 0; var maghribComp = 0; var ishaComp = 0

    weekLogs.forEach { log ->
        val logDate = java.time.LocalDate.parse(log.date)
        if (logDate.isBefore(java.time.LocalDate.now()) || logDate.isEqual(java.time.LocalDate.now())) {
            totalPrayersWeek += 5
            if (log.fajrCompleted) { completedPrayersWeek++; fajrComp++ }
            if (log.dhuhrCompleted) { completedPrayersWeek++; dhuhrComp++ }
            if (log.asrCompleted) { completedPrayersWeek++; asrComp++ }
            if (log.maghribCompleted) { completedPrayersWeek++; maghribComp++ }
            if (log.ishaCompleted) { completedPrayersWeek++; ishaComp++ }
        }
    }
    
    val weekPercent = if (totalPrayersWeek > 0) (completedPrayersWeek * 100) / totalPrayersWeek else 0

    var af=0; var ad=0; var aa=0; var am=0; var ai=0
    allPrayerLogs.forEach { log ->
        if(log.fajrCompleted) af++
        if(log.dhuhrCompleted) ad++
        if(log.asrCompleted) aa++
        if(log.maghribCompleted) am++
        if(log.ishaCompleted) ai++
    }
    
    val comps = listOf(
        Pair("Fajr", af), Pair("Dhuhr", ad), Pair("Asr", aa), Pair("Maghrib", am), Pair("Isha", ai)
    )
    val mostConsistent = comps.maxByOrNull { it.second }?.first ?: "N/A"
    val leastConsistent = comps.minByOrNull { it.second }?.first ?: "N/A"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InsightCard("Current streak", "$currentStreak days", Modifier.weight(1f))
            InsightCard("This week", "$weekPercent%", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InsightCard("Most consistent", mostConsistent, Modifier.weight(1f))
            InsightCard("Needs attention", leastConsistent, Modifier.weight(1f))
        }
    }
}

@Composable
fun InsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = com.example.ui.theme.SpaceGrotesk,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = com.example.ui.theme.SerifHeaderFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PersonalLogQadaContent(
    qadaCounts: Map<com.example.data.model.PrayerName, Int>,
    onUpdateQadaCount: (com.example.data.model.PrayerName, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Track missed obligatory prayers you intend to make up.",
            fontFamily = com.example.ui.theme.SpaceGrotesk,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        listOf(
            com.example.data.model.PrayerName.FAJR,
            com.example.data.model.PrayerName.DHUHR,
            com.example.data.model.PrayerName.ASR,
            com.example.data.model.PrayerName.MAGHRIB,
            com.example.data.model.PrayerName.ISHA
        ).forEach { prayer ->
            val count = qadaCounts[prayer] ?: 0
            QadaRow(
                name = prayer.name.lowercase().replaceFirstChar { it.uppercase() },
                count = count,
                onIncrement = { onUpdateQadaCount(prayer, count + 1) },
                onDecrement = { if (count > 0) onUpdateQadaCount(prayer, count - 1) },
                onComplete = { if (count > 0) onUpdateQadaCount(prayer, count - 1) }
            )
        }
    }
}

@Composable
fun QadaRow(
    name: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    fontFamily = com.example.ui.theme.SerifHeaderFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (count > 0) "$count remaining" else "Completed",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 12.sp,
                    color = if (count > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, enabled = count > 0) {
                    Icon(androidx.compose.material.icons.Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(20.dp))
                }
                Text(
                    text = count.toString(),
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrement) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onComplete,
                    enabled = count > 0,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Make up", fontFamily = com.example.ui.theme.SpaceGrotesk, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PersonalLogPrayerRow(
    prayerName: com.example.data.model.PrayerName,
    displayName: String,
    status: com.example.data.model.PrayerStatus,
    onSetStatus: (com.example.data.model.PrayerStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFuture = status == com.example.data.model.PrayerStatus.FUTURE
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("personal_log_row_${prayerName.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkTheme) Color(0xFF191919) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2E2E2E) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        fontFamily = com.example.ui.theme.SpaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = prayerName.arabicName,
                        fontFamily = com.example.ui.theme.AmiriFont,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                val statusText = when (status) {
                    com.example.data.model.PrayerStatus.PRAYED -> "✓ Prayed"
                    com.example.data.model.PrayerStatus.MISSED -> "! Missed"
                    com.example.data.model.PrayerStatus.NEEDS_INPUT -> "○ Needs Input"
                    com.example.data.model.PrayerStatus.FUTURE -> "· Future"
                }
                val statusColor = when (status) {
                    com.example.data.model.PrayerStatus.PRAYED -> if (isDarkTheme) Color(0xFF81C784) else MaterialTheme.colorScheme.primary
                    com.example.data.model.PrayerStatus.MISSED -> if (isDarkTheme) Color(0xFFEF5350) else Color(0xFFC62828)
                    com.example.data.model.PrayerStatus.NEEDS_INPUT -> MaterialTheme.colorScheme.onSurfaceVariant
                    com.example.data.model.PrayerStatus.FUTURE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }

                Text(
                    text = statusText,
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }

            if (isFuture) {
                Text(
                    text = "—",
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(end = 12.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isPrayed = status == com.example.data.model.PrayerStatus.PRAYED
                    val prayedBg = if (isPrayed) {
                        if (isDarkTheme) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    } else {
                        if (isDarkTheme) Color(0xFF222222) else MaterialTheme.colorScheme.surface
                    }
                    val prayedTextColor = if (isPrayed) Color.White else MaterialTheme.colorScheme.onSurface
                    val prayedBorder = if (isPrayed) {
                        BorderStroke(1.dp, prayedBg)
                    } else {
                        BorderStroke(1.dp, if (isDarkTheme) Color(0xFF383838) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    Surface(
                        onClick = { onSetStatus(com.example.data.model.PrayerStatus.PRAYED) },
                        shape = RoundedCornerShape(10.dp),
                        color = prayedBg,
                        border = prayedBorder,
                        modifier = Modifier.testTag("btn_prayed_${prayerName.name.lowercase()}")
                    ) {
                        Text(
                            text = "✓ Prayed",
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = if (isPrayed) FontWeight.Bold else FontWeight.Normal,
                            color = prayedTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    val isMissed = status == com.example.data.model.PrayerStatus.MISSED
                    val missedBg = if (isMissed) {
                        if (isDarkTheme) Color(0xFFB71C1C) else Color(0xFFC62828)
                    } else {
                        if (isDarkTheme) Color(0xFF222222) else MaterialTheme.colorScheme.surface
                    }
                    val missedTextColor = if (isMissed) Color.White else MaterialTheme.colorScheme.onSurface
                    val missedBorder = if (isMissed) {
                        BorderStroke(1.dp, missedBg)
                    } else {
                        BorderStroke(1.dp, if (isDarkTheme) Color(0xFF383838) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    Surface(
                        onClick = { onSetStatus(com.example.data.model.PrayerStatus.MISSED) },
                        shape = RoundedCornerShape(10.dp),
                        color = missedBg,
                        border = missedBorder,
                        modifier = Modifier.testTag("btn_missed_${prayerName.name.lowercase()}")
                    ) {
                        Text(
                            text = "! Missed",
                            fontFamily = com.example.ui.theme.SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = if (isMissed) FontWeight.Bold else FontWeight.Normal,
                            color = missedTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "w") as f:
    f.writelines(lines[:start_idx])
    f.write(new_code)
