import re

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.semanticSurfaceElevated
    val iconBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightSurface""",
"""    val cardBg = if (isDark) Color.semanticSurface else Color.semanticSurfaceElevated
    val iconBg = if (isDark) Color.semanticSurfaceElevated else LightSurface"""
)

content = content.replace(
"""        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),""",
"""        border = BorderStroke(1.dp, Color.semanticBorder),"""
)

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'w') as f:
    f.write(content)
