import re

filename = 'app/src/main/java/com/example/ui/screens/CalendarScreen.kt'
with open(filename, 'r') as f:
    content = f.read()

content = content.replace("val selectedFillColor = if (isDark) Color.semanticPrimaryAccent else Color(0xFF1E1D1A)", "val selectedFillColor = Color.semanticPrimaryAccent")
content = content.replace("val selectedFillColor = if (isDark) amberGold else Color(0xFF1E1D1A)", "val selectedFillColor = Color.semanticPrimaryAccent")

with open(filename, 'w') as f:
    f.write(content)
