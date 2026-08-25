import re

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = activeIconColor,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(activeIconColor),""",
"""                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (isDark) activeIconColor else androidx.compose.ui.graphics.Color(0xFF1E1D1A),
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(if (isDark) activeIconColor else androidx.compose.ui.graphics.Color(0xFF8D6B1E)),"""
)

content = content.replace(
"""                                    color = if (isDark) androidx.compose.ui.graphics.Color(0xFF8E8E93) else inactiveIconColor,""",
"""                                    color = if (isDark) androidx.compose.ui.graphics.Color(0xFF8E8E93) else androidx.compose.ui.graphics.Color(0xFF7A7771),"""
)

content = content.replace(
"""                            tint = if (isDark) androidx.compose.ui.graphics.Color(0xFF494556) else activeIconColor,""",
"""                            tint = if (isDark) androidx.compose.ui.graphics.Color(0xFF494556) else androidx.compose.ui.graphics.Color(0xFF8D6B1E),"""
)

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'w') as f:
    f.write(content)
