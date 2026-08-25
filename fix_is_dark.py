import re

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

# Replace if (isSystemInDarkTheme()) with if (isAppInDarkTheme())
# And add a helper function isAppInDarkTheme()

helper = """@Composable
fun isAppInDarkTheme(): Boolean {
    return androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
}
"""

content = content.replace("import androidx.compose.foundation.isSystemInDarkTheme", "import androidx.compose.foundation.isSystemInDarkTheme\n" + helper)

content = content.replace("if (isSystemInDarkTheme())", "if (isAppInDarkTheme())")

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
