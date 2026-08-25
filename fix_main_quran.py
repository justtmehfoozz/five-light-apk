with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

sig_old = """                                QuranScreen(
                                    searchQuery = searchQuery,"""

sig_new = """                                val dailyQuranGoal by viewModel.dailyQuranGoal.collectAsStateWithLifecycle()
                                QuranScreen(
                                    dailyQuranGoal = dailyQuranGoal,
                                    onSetDailyGoal = { viewModel.setDailyQuranGoal(it) },
                                    searchQuery = searchQuery,"""

content = content.replace(sig_old, sig_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
