import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

# Make sure active highlight uses the neutral treatment
# Find activeHighlightBg definition
content = re.sub(r'val activeHighlightBg = Color\.semanticDockIconActiveBg',
                 'val activeHighlightBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF7A7585) else androidx.compose.ui.graphics.Color(0xFF1E1D1A)', content)

# Audio player inside BottomNavBar needs to use Accent!
content = re.sub(r'val progressAccent = activeIconColor',
                 'val progressAccent = androidx.compose.ui.graphics.Color.semanticPrimaryAccent', content)

# Play/Pause button in Audio Player
# It's currently using activeHighlightBg and activeIconColor
# We need to change the Play/Pause button to use semanticPrimaryAccent and semanticAccentForeground
content = content.replace("modifier = Modifier\n                                                .size(44.dp)\n                                                .graphicsLayer {\n                                                    alpha = playPauseAlpha.value\n                                                    scaleX = playPauseScale.value\n                                                    scaleY = playPauseScale.value\n                                                    translationY = playPauseYOffset.value.dp.toPx()\n                                                }\n                                                .clip(CircleShape)\n                                                .background(activeHighlightBg)",
"""modifier = Modifier
                                                .size(44.dp)
                                                .graphicsLayer {
                                                    alpha = playPauseAlpha.value
                                                    scaleX = playPauseScale.value
                                                    scaleY = playPauseScale.value
                                                    translationY = playPauseYOffset.value.dp.toPx()
                                                }
                                                .clip(CircleShape)
                                                .background(androidx.compose.ui.graphics.Color.semanticPrimaryAccent)""")

content = content.replace("tint = activeIconColor,\n                                                modifier = Modifier.size(24.dp)",
"tint = androidx.compose.ui.graphics.Color.semanticAccentForeground,\n                                                modifier = Modifier.size(24.dp)")


with open(filename, 'w') as f:
    f.write(content)
