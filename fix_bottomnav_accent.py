import os

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

content = content.replace("tint = if (isDark) androidx.compose.ui.graphics.Color(0xFF494556) else androidx.compose.ui.graphics.Color(0xFF8D6B1E),",
"tint = androidx.compose.ui.graphics.Color.semanticPrimaryAccent,")

with open(filename, 'w') as f:
    f.write(content)
