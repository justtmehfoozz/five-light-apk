import re

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""val Color.Companion.semanticMutedText: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF8E8E93) else Color(0xFF8F8C86)""",
"""val Color.Companion.semanticMutedText: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF8E8E93) else Color(0xFF66635E)
val Color.Companion.semanticVeryMutedText: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF8E8E93) else Color(0xFF8F8C86)"""
)

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
