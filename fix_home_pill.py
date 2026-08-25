import re

with open('./app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val activePillBg = Color.semanticPrimaryText
    val activeContentColor = if (isDark) com.example.ui.theme.TextPrimaryLight else com.example.ui.theme.SurfaceLight
    val inactiveContentColor = Color.semanticMutedText""",
"""    val activePillBg = Color.semanticPrimaryAccent
    val activeContentColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFE6DEF6) else androidx.compose.ui.graphics.Color.White
    val inactiveContentColor = Color.semanticSecondaryText"""
)

with open('./app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
