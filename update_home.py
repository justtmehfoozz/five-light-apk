import re
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add qadaCounts to HomeScreen signature if not there, or grab from ViewModel
old_code = """    if (showPersonalLogSheet) {
        com.example.ui.components.PersonalLogSheet(
            overview = contextState.weeklyOverview,
            todayLog = todayLog,
            onSetPrayerStatus = { pName, dStr, pStatus ->
                onSetPrayerStatusWithDate(pName, dStr, pStatus)
            },
            onDismiss = { showPersonalLogSheet = false }
        )
    }"""

new_code = """    val allPrayerLogs by viewModel.allPrayerLogs.collectAsStateWithLifecycle()
    val qadaCounts by viewModel.qadaCounts.collectAsStateWithLifecycle()

    if (showPersonalLogSheet) {
        com.example.ui.components.PersonalLogSheet(
            overview = contextState.weeklyOverview,
            todayLog = todayLog,
            allPrayerLogs = allPrayerLogs,
            qadaCounts = qadaCounts,
            onUpdateQadaCount = { name, count -> viewModel.updateQadaCount(name, count) },
            onSetPrayerStatus = { pName, dStr, pStatus ->
                onSetPrayerStatusWithDate(pName, dStr, pStatus)
            },
            onDismiss = { showPersonalLogSheet = false }
        )
    }"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.write(content)
    print("Updated HomeScreen successfully")
else:
    print("Could not find PersonalLogSheet in HomeScreen")
