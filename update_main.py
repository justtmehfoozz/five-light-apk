with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_code = """                                val rightNowItem by viewModel.rightNowItem.collectAsStateWithLifecycle()
                                val contextState by viewModel.contextState.collectAsStateWithLifecycle()
                                val lastReadPosition by viewModel.lastReadPosition.collectAsStateWithLifecycle()"""

new_code = """                                val rightNowItem by viewModel.rightNowItem.collectAsStateWithLifecycle()
                                val contextState by viewModel.contextState.collectAsStateWithLifecycle()
                                val lastReadPosition by viewModel.lastReadPosition.collectAsStateWithLifecycle()
                                val allPrayerLogs by viewModel.allPrayerLogs.collectAsStateWithLifecycle()
                                val qadaCounts by viewModel.qadaCounts.collectAsStateWithLifecycle()"""

content = content.replace(old_code, new_code)

old_home = """                                    HomeScreen(
                                        nextPrayer = nextPrayer,
                                        prayerTimes = prayerTimes,"""

new_home = """                                    HomeScreen(
                                        nextPrayer = nextPrayer,
                                        prayerTimes = prayerTimes,
                                        allPrayerLogs = allPrayerLogs,
                                        qadaCounts = qadaCounts,
                                        onUpdateQadaCount = { p, c -> viewModel.updateQadaCount(p, c) },"""

content = content.replace(old_home, new_home)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
