import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    targetValue = if (isSelected) {
                        Color.semanticPrimaryAccent
                    } else {
                        Color.Transparent
                    },""",
"""                    targetValue = if (isSelected) {
                        Color.semanticPrimaryAccent
                    } else {
                        Color.semanticSurface
                    },"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
