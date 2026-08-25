import re

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    content = f.read()

# Define the new PersonalLogSheet
new_code = """enum class PersonalLogTab { LOG, INSIGHTS, QADA }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PersonalLogSheet(
    overview: WeeklyWorshipOverview?,
    todayLog: com.example.data.db.PrayerLogEntity?,
    allPrayerLogs: List<com.example.data.db.PrayerLogEntity>,
    qadaCounts: Map<com.example.data.model.PrayerName, Int>,
    onUpdateQadaCount: (com.example.data.model.PrayerName, Int) -> Unit,
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentTab by remember { mutableStateOf(PersonalLogTab.LOG) }
    
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
                        fontFamily = SerifHeaderFont,
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
    overview: WeeklyWorshipOverview?,
    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit
) {
    val days = overview?.days ?: emptyList()
    var selectedDateString by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val todayDateString = remember(days) { days.find { it.isToday }?.dateString }
    val effectiveDateString = selectedDateString ?: todayDateString ?: days.firstOrNull()?.dateString

    val selectedDay = days.find { it.dateString == effectiveDateString } ?: days.find { it.isToday } ?: days.firstOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (days.isNotEmpty()) {
            val isDarkTheme = MaterialTheme.colorScheme.background.androidx.compose.ui.graphics.luminance() < 0.5f

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
                                text = day.dayOfWeekShort,
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
            val isFridayDay = selectedDay.dayOfWeekShort == "Fri"
            Text(
                text = if (selectedDay.isToday) "Today's Prayers" else "${selectedDay.dayOfWeekShort}, ${selectedDay.dateString}",
                fontFamily = SerifHeaderFont,
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
                    fontFamily = SerifHeaderFont,
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

    // Calculate Insights
    // Streak: Consecutive days where all 5 prayers were completed
    var currentStreak = 0
    var previousDate = java.time.LocalDate.now()
    
    val sortedLogs = allPrayerLogs.sortedByDescending { it.date }
    for (log in sortedLogs) {
        val logDate = java.time.LocalDate.parse(log.date)
        // Accept today or yesterday for the start of the streak
        if (logDate.isAfter(previousDate)) continue
        if (java.time.temporal.ChronoUnit.DAYS.between(logDate, previousDate) > 1 && currentStreak > 0) {
            break // Break streak if gap > 1 day
        }
        val allCompleted = log.fajrCompleted && log.dhuhrCompleted && log.asrCompleted && log.maghribCompleted && log.ishaCompleted
        if (allCompleted) {
            currentStreak++
            previousDate = logDate
        } else {
            if (currentStreak > 0 || logDate.isBefore(java.time.LocalDate.now())) break
        }
    }

    // Weekly completion (last 7 days)
    val weekAgo = java.time.LocalDate.now().minusDays(7)
    val weekLogs = allPrayerLogs.filter { java.time.LocalDate.parse(it.date).isAfter(weekAgo) }
    var totalPrayersWeek = 0
    var completedPrayersWeek = 0
    var fajrComp = 0; var dhuhrComp = 0; var asrComp = 0; var maghribComp = 0; var ishaComp = 0
    var fajrTotal = 0; var dhuhrTotal = 0; var asrTotal = 0; var maghribTotal = 0; var ishaTotal = 0

    weekLogs.forEach { log ->
        val logDate = java.time.LocalDate.parse(log.date)
        if (logDate.isBefore(java.time.LocalDate.now()) || logDate.isEqual(java.time.LocalDate.now())) {
            totalPrayersWeek += 5
            if (log.fajrCompleted) { completedPrayersWeek++; fajrComp++ }
            if (log.dhuhrCompleted) { completedPrayersWeek++; dhuhrComp++ }
            if (log.asrCompleted) { completedPrayersWeek++; asrComp++ }
            if (log.maghribCompleted) { completedPrayersWeek++; maghribComp++ }
            if (log.ishaCompleted) { completedPrayersWeek++; ishaComp++ }
            
            fajrTotal++; dhuhrTotal++; asrTotal++; maghribTotal++; ishaTotal++
        }
    }
    
    val weekPercent = if (totalPrayersWeek > 0) (completedPrayersWeek * 100) / totalPrayersWeek else 0

    // All time consistent
    var af=0; var ad=0; var aa=0; var am=0; var ai=0
    var count=0
    allPrayerLogs.forEach { log ->
        count++
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
                fontFamily = SerifHeaderFont,
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
                    fontFamily = SerifHeaderFont,
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
                    Icon(Icons.Default.Close, contentDescription = "Decrease", modifier = Modifier.size(20.dp)) // Using close as minus or we can use another icon, wait better to use Text or just minus
                }
                Text(
                    text = count.toString(),
                    fontFamily = com.example.ui.theme.SpaceGrotesk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Check, contentDescription = "Increase", modifier = Modifier.size(20.dp)) // wait check is tick
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
"""

def extract_body(full_text, signature):
    parts = full_text.split(signature)
    if len(parts) < 2: return None
    body_start = parts[1]
    brace_count = 0
    in_str = False
    for i, char in enumerate(body_start):
        if char == '"' and (i == 0 or body_start[i-1] != '\\'):
            in_str = not in_str
        if not in_str:
            if char == '{': brace_count += 1
            elif char == '}':
                brace_count -= 1
                if brace_count == 0:
                    return signature + body_start[:i+1]
    return None

old_sheet_signature = "fun PersonalLogSheet(\n    overview: WeeklyWorshipOverview?,\n    todayLog: com.example.data.db.PrayerLogEntity?,\n    onSetPrayerStatus: (com.example.data.model.PrayerName, String, com.example.data.model.PrayerStatus) -> Unit,\n    onDismiss: () -> Unit,\n    modifier: Modifier = Modifier\n) {"

old_method = extract_body(content, old_sheet_signature)

if old_method:
    content = content.replace(old_method, new_code)
    
    # Also I need to import luminance, check if it's there
    
    with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "w") as f:
        f.write(content)
    print("Updated PersonalLogSheet in HomeFeatureCards")
else:
    print("Could not find PersonalLogSheet to replace")

