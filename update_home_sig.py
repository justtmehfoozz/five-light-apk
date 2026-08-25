with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun HomeScreen(
    nextPrayer: PrayerItem?,
    prayerTimes: List<PrayerItem>,"""

new_sig = """fun HomeScreen(
    nextPrayer: PrayerItem?,
    prayerTimes: List<PrayerItem>,
    allPrayerLogs: List<PrayerLogEntity> = emptyList(),
    qadaCounts: Map<PrayerName, Int> = emptyMap(),
    onUpdateQadaCount: (PrayerName, Int) -> Unit = { _, _ -> },"""

content = content.replace(old_sig, new_sig)

old_body = """    val allPrayerLogs by viewModel.allPrayerLogs.collectAsStateWithLifecycle()
    val qadaCounts by viewModel.qadaCounts.collectAsStateWithLifecycle()

    if (showPersonalLogSheet) {
        com.example.ui.components.PersonalLogSheet(
            overview = contextState.weeklyOverview,
            todayLog = todayLog,
            allPrayerLogs = allPrayerLogs,
            qadaCounts = qadaCounts,
            onUpdateQadaCount = { name, count -> viewModel.updateQadaCount(name, count) },"""

new_body = """    if (showPersonalLogSheet) {
        com.example.ui.components.PersonalLogSheet(
            overview = contextState.weeklyOverview,
            todayLog = todayLog,
            allPrayerLogs = allPrayerLogs,
            qadaCounts = qadaCounts,
            onUpdateQadaCount = onUpdateQadaCount,"""

content = content.replace(old_body, new_body)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
