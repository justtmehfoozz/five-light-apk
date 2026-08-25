import re

def replace_in_file(filepath, old, new):
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace(old, new)
    with open(filepath, "w") as f:
        f.write(content)

replace_in_file("app/src/main/java/com/example/ui/screens/QiblaScreen.kt", 
    "val isSystemDark = isSystemInDarkTheme()", 
    "val isSystemDark = androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }")

replace_in_file("app/src/main/java/com/example/ui/screens/QuranScreen.kt",
    "val isDark = isNightMode || isSystemInDarkTheme()",
    "val isDark = isNightMode || androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }")

