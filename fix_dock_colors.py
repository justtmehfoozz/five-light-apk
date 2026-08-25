import re

filename = 'app/src/main/java/com/example/ui/theme/Color.kt'
with open(filename, 'r') as f:
    content = f.read()

# I will add semanticDock...
# First, remove the old hardcoded Dock colors
content = re.sub(r'// Bottom Navigation Dock Colors \(Do not change\)\nval DockBackground.*?\nval DockIconInactive = Color\(255, 255, 255, 166\)\n', '', content, flags=re.DOTALL)

# Add new semanticDock tokens where semanticWarning is
semantic_dock = """
val Color.Companion.semanticDockBackgroundLight: Color get() = Color(0xFFFBFAF6)
val Color.Companion.semanticDockBorderLight: Color get() = Color(0xFFC8C3B9)
val Color.Companion.semanticDockIconInactiveLight: Color get() = Color(0xFF66635E)
val Color.Companion.semanticDockIconActiveBgLight: Color get() = Color(0xFF8D6B1E)
val Color.Companion.semanticDockIconActiveLight: Color get() = Color(0xFFFFFFFF)

val Color.Companion.semanticDockBackgroundDark: Color get() = Color(60, 60, 64, 235)
val Color.Companion.semanticDockBorderDark: Color get() = Color(255, 255, 255, 89)
val Color.Companion.semanticDockIconInactiveDark: Color get() = Color(255, 255, 255, 166)
val Color.Companion.semanticDockIconActiveBgDark: Color get() = Color(0xFF494556)
val Color.Companion.semanticDockIconActiveDark: Color get() = Color(0xFFE6DEF6)

val Color.Companion.semanticDockBackground: Color @Composable get() = if (isAppInDarkTheme()) semanticDockBackgroundDark else semanticDockBackgroundLight
val Color.Companion.semanticDockBorder: Color @Composable get() = if (isAppInDarkTheme()) semanticDockBorderDark else semanticDockBorderLight
val Color.Companion.semanticDockIconInactive: Color @Composable get() = if (isAppInDarkTheme()) semanticDockIconInactiveDark else semanticDockIconInactiveLight
val Color.Companion.semanticDockIconActiveBg: Color @Composable get() = if (isAppInDarkTheme()) semanticDockIconActiveBgDark else semanticDockIconActiveBgLight
val Color.Companion.semanticDockIconActive: Color @Composable get() = if (isAppInDarkTheme()) semanticDockIconActiveDark else semanticDockIconActiveLight
"""

content = content.replace("val Color.Companion.semanticWarning: Color @Composable get() = if (isAppInDarkTheme()) semanticWarningDark else semanticWarningLight",
"val Color.Companion.semanticWarning: Color @Composable get() = if (isAppInDarkTheme()) semanticWarningDark else semanticWarningLight\n" + semantic_dock)

with open(filename, 'w') as f:
    f.write(content)
