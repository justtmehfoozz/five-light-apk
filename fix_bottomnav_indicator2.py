import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

content = content.replace("val activeHighlightBg = if (isDark) androidx.compose.ui.graphics.Color(0xFFD9D6DF) else androidx.compose.ui.graphics.Color(0xFF1E1D1A)",
"val activeHighlightBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF6E687A) else androidx.compose.ui.graphics.Color(0xFF1E1D1A)")
# 6E687A is a soft tinted lavender gray that supports white text well

with open(filename, 'w') as f:
    f.write(content)
