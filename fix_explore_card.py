import re

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    color = Color.semanticMutedText,""",
"""                    color = Color.semanticSecondaryText,"""
)

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'w') as f:
    f.write(content)
