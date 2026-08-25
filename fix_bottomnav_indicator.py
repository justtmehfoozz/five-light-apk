import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

content = content.replace("val activeHighlightBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF7A7585) else androidx.compose.ui.graphics.Color(0xFF1E1D1A)",
"val activeHighlightBg = if (isDark) androidx.compose.ui.graphics.Color(0xFFD9D6DF) else androidx.compose.ui.graphics.Color(0xFF1E1D1A)")

# The user also wanted:
# "Dark Dock active ball: soft light neutral / subtle lavender-gray... Active icon: #FFFFFF"
# Wait, if ball is #D9D6DF, white icon is invisible? No, I will just give exactly what the user said.
# User's exact words: "dockActiveIndicator = #D9D6DF" ... "Active icon: #FFFFFF"
# But they also said: "Reference appearance: soft light gray / subtle lavender-gray circular ball".
# #D9D6DF is a subtle lavender-gray! 

with open(filename, 'w') as f:
    f.write(content)
