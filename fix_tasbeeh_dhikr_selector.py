import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    targetValue = if (isSelected) {
                        if (isDarkTheme) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
                    } else {
                        Color.semanticMutedText
                    },""",
"""                    targetValue = if (isSelected) {
                        if (isDarkTheme) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
                    } else {
                        Color.semanticPrimaryText
                    },"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
