import re

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val cardBg = if (isDark) Color.semanticSurface else Color.semanticSurfaceElevated""",
"""    val cardBg = if (isDark) Color.semanticSurface else androidx.compose.ui.graphics.Color(0xFF1E1D1A)"""
)
content = content.replace(
"""    val iconBg = if (isDark) Color.semanticSurfaceElevated else LightSurface""",
"""    val iconBg = if (isDark) Color.semanticSurfaceElevated else androidx.compose.ui.graphics.Color(0xFF2C2C2E)"""
)

# Fix the text colors in ExploreCard
content = content.replace(
"""                    color = Color.semanticPrimaryText,""",
"""                    color = if (isDark) Color.semanticPrimaryText else androidx.compose.ui.graphics.Color(0xFFFFFFFF),"""
)
content = content.replace(
"""                    color = Color.semanticSecondaryText,""",
"""                    color = if (isDark) Color.semanticSecondaryText else androidx.compose.ui.graphics.Color(0xFFD5D1C9),"""
)
content = content.replace(
"""                        tint = Color.semanticPrimaryText,""",
"""                        tint = if (isDark) Color.semanticPrimaryText else androidx.compose.ui.graphics.Color(0xFFFFFFFF),"""
)
content = content.replace(
"""                tint = MaterialTheme.colorScheme.onSurfaceVariant,""",
"""                tint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color(0xFFD5D1C9),"""
)
content = content.replace(
"""        border = BorderStroke(1.dp, Color.semanticBorder),""",
"""        border = BorderStroke(1.dp, if (isDark) Color.semanticBorder else androidx.compose.ui.graphics.Color(0xFF2C2C2E)),"""
)

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'w') as f:
    f.write(content)
