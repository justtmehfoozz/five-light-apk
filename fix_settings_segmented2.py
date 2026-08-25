import re

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val activeContentColor = Color.White""",
"""    val activeContentColor = if (isDark) Color(0xFFE6DEF6) else Color.White"""
)

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'w') as f:
    f.write(content)
