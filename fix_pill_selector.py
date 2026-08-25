import re

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val inactiveContentColor = if (isDark) Color.White.copy(alpha = 0.55f) else com.example.ui.theme.LightMutedText""",
"""    val inactiveContentColor = if (isDark) Color.White.copy(alpha = 0.55f) else Color.semanticSecondaryText"""
)

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'w') as f:
    f.write(content)
