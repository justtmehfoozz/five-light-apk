with open("app/src/main/java/com/example/ui/screens/QuranScreen.kt", "r") as f:
    content = f.read()

sig_old = """fun QuranScreen(
    searchQuery: String,"""

sig_new = """fun QuranScreen(
    dailyQuranGoal: Int = 0,
    onSetDailyGoal: (Int) -> Unit = {},
    searchQuery: String,"""

content = content.replace(sig_old, sig_new)

usage_old = """                } else if (selectedTab == 2) {
                    com.example.ui.components.QuranPlannerTabContent()
                }"""

usage_new = """                } else if (selectedTab == 2) {
                    com.example.ui.components.QuranPlannerTabContent(
                        dailyQuranGoal = dailyQuranGoal,
                        onSetDailyGoal = onSetDailyGoal
                    )
                }"""

content = content.replace(usage_old, usage_new)

with open("app/src/main/java/com/example/ui/screens/QuranScreen.kt", "w") as f:
    f.write(content)
