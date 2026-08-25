import re

with open('./app/src/main/java/com/example/ui/screens/CalendarScreen.kt', 'r') as f:
    content = f.read()

# Fix selectedFillColor
content = content.replace(
"""                        val selectedFillColor = if (isDark) Color.White else Color.semanticSurfaceElevated""",
"""                        val selectedFillColor = if (isDark) Color.semanticPrimaryAccent else Color(0xFF1E1D1A)"""
)
# For selected text color:
# "if (isSelected) (if (isDark) Color.Black else Color.semanticPrimaryAccent)" -> I need to check how text color is determined.

with open('./app/src/main/java/com/example/ui/screens/CalendarScreen.kt', 'w') as f:
    f.write(content)
