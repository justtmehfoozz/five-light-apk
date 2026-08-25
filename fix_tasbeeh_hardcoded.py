import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        color = if (showFeedbackSettings) (if (isDarkTheme) Color(0xFF2B2B2B) else Color.semanticSurfaceElevated) else Color.semanticSurfaceElevated,""",
"""                        color = Color.semanticSurfaceElevated,"""
)

content = content.replace(
"""            val ringTrackColor = if (isDarkTheme) Color(0xFF222222) else LightBorder.copy(alpha = 0.35f)""",
"""            val ringTrackColor = if (isDarkTheme) Color.semanticSurfaceElevated else LightBorder.copy(alpha = 0.35f)"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
