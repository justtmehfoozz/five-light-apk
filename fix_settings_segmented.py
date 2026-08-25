import re

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val activeContentColor = if (isDark) Color(0xFF000000) else Color.White""",
"""    val activeContentColor = Color.White"""
)

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'w') as f:
    f.write(content)
