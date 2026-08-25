import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        tint = if (isDarkTheme) Color(0xFF66635E) else (if (dhikrCount > 0) MaterialTheme.colorScheme.onSurface else LightMutedText.copy(alpha = 0.3f)),""",
"""                        tint = if (dhikrCount > 0) Color.semanticPrimaryText else Color.semanticMutedText.copy(alpha = 0.5f),"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
