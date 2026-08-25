import re

with open('./app/src/main/java/com/example/ui/screens/CalendarScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        val onSelectedTextColor = Color.semanticPrimaryText""",
"""                        val onSelectedTextColor = if (isDark) Color(0xFFE6DEF6) else Color.White"""
)

with open('./app/src/main/java/com/example/ui/screens/CalendarScreen.kt', 'w') as f:
    f.write(content)
