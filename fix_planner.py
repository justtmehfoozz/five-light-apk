with open("app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt", "r") as f:
    content = f.read()

sig_old = """fun QuranPlannerTabContent(
    modifier: Modifier = Modifier
) {"""

sig_new = """fun QuranPlannerTabContent(
    dailyQuranGoal: Int,
    onSetDailyGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {"""

content = content.replace(sig_old, sig_new)

usage_old = """    if (showKhatmPlanner) {
        KhatmPlannerSheet(
            dailyGoalPages = 0, // In real app use viewModel.dailyQuranGoal
            onSetDailyGoal = { /* TODO */ },
            onDismiss = { showKhatmPlanner = false }
        )
    }"""

usage_new = """    if (showKhatmPlanner) {
        KhatmPlannerSheet(
            dailyGoalPages = dailyQuranGoal,
            onSetDailyGoal = onSetDailyGoal,
            onDismiss = { showKhatmPlanner = false }
        )
    }"""

content = content.replace(usage_old, usage_new)

# Add goal text in UI
goal_text_old = """                Text(
                    text = "Stay consistent with your daily recitation.",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))"""

goal_text_new = """                Text(
                    text = "Stay consistent with your daily recitation.",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (dailyQuranGoal > 0) {
                    Text(
                        text = "Current Goal: $dailyQuranGoal pages/day",
                        fontFamily = SpaceGrotesk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }"""

content = content.replace(goal_text_old, goal_text_new)

with open("app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt", "w") as f:
    f.write(content)
