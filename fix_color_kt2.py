import re

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

content = content.replace("val Color.Companion.semanticStrongBorderLight: Color get() = Color(0xFFC8C3B9)", 
"""val Color.Companion.semanticStrongBorderLight: Color get() = Color(0xFFC8C3B9)
val Color.Companion.semanticStrongBorder: Color @Composable get() = if (isAppInDarkTheme()) Color(255, 255, 255, 30) else Color(0xFFC8C3B9)
val Color.Companion.semanticControl: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFFEFEBE2)
val Color.Companion.semanticSurfaceElevated: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFFFFFDF8)""")

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
