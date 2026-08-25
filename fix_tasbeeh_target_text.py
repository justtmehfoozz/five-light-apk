import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                                Color.semanticMutedText""",
"""                                Color.semanticSecondaryText"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
