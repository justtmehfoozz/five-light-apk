import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    targetValue = if (isSelected) {
                        androidx.compose.ui.graphics.Color.White
                    } else {""",
"""                    targetValue = if (isSelected) {
                        if (isDarkTheme) Color(0xFFE6DEF6) else Color(0xFFFFFFFF)
                    } else {"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
