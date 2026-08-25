import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                            targetValue = if (isTargetSelected) {
                                if (isDarkTheme) Color(0xFF000000) else Color.White
                            } else {""",
"""                            targetValue = if (isTargetSelected) {
                                Color.White
                            } else {"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
