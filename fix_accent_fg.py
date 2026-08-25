import re

filename = 'app/src/main/java/com/example/ui/theme/Color.kt'
with open(filename, 'r') as f:
    content = f.read()

content = content.replace("val Color.Companion.semanticPrimaryAccentLight: Color get() = Color(0xFF8D6B1E)",
"val Color.Companion.semanticPrimaryAccentLight: Color get() = Color(0xFF8D6B1E)\nval Color.Companion.semanticAccentForegroundLight: Color get() = Color(0xFFFFFFFF)")

content = content.replace("val Color.Companion.semanticPrimaryAccentDark: Color get() = Color(0xFF494556)",
"val Color.Companion.semanticPrimaryAccentDark: Color get() = Color(0xFF494556)\nval Color.Companion.semanticAccentForegroundDark: Color get() = Color(0xFFE6DEF6)")

content = content.replace("val Color.Companion.semanticPrimaryAccent: Color @Composable get() = if (isAppInDarkTheme()) semanticPrimaryAccentDark else semanticPrimaryAccentLight",
"val Color.Companion.semanticPrimaryAccent: Color @Composable get() = if (isAppInDarkTheme()) semanticPrimaryAccentDark else semanticPrimaryAccentLight\nval Color.Companion.semanticAccentForeground: Color @Composable get() = if (isAppInDarkTheme()) semanticAccentForegroundDark else semanticAccentForegroundLight")

with open(filename, 'w') as f:
    f.write(content)
