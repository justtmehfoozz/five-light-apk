import re

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val targetDockBg = if (isDark) {
        if (searchMode) androidx.compose.ui.graphics.Color(0xFF2C2C2E) else com.example.ui.theme.DockBackground
    } else MaterialTheme.colorScheme.surface
    val dockBg by androidx.compose.animation.animateColorAsState(targetValue = targetDockBg, animationSpec = DOCK_SPRING_MORPH)
    
    val targetDockBorderColor = if (isDark) {
        if (searchMode) androidx.compose.ui.graphics.Color(255, 255, 255, 20) else com.example.ui.theme.DockBorder
    } else com.example.ui.theme.LightBorder
    val dockBorderColor by androidx.compose.animation.animateColorAsState(targetValue = targetDockBorderColor, animationSpec = DOCK_SPRING_MORPH)""",
"""    val targetDockBg = if (isDark) {
        if (isSearchActive) androidx.compose.ui.graphics.Color(0xFF2C2C2E) else com.example.ui.theme.DockBackground
    } else MaterialTheme.colorScheme.surface
    val dockBg by animateColorAsState(targetValue = targetDockBg, animationSpec = DOCK_SPRING_COLOR, label = "dockBg")
    
    val targetDockBorderColor = if (isDark) {
        if (isSearchActive) androidx.compose.ui.graphics.Color(255, 255, 255, 20) else com.example.ui.theme.DockBorder
    } else com.example.ui.theme.LightBorder
    val dockBorderColor by animateColorAsState(targetValue = targetDockBorderColor, animationSpec = DOCK_SPRING_COLOR, label = "dockBorderColor")"""
)

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'w') as f:
    f.write(content)
