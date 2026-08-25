import re

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()",
    "val isDarkTheme = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }"
)

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "w") as f:
    f.write(content)
