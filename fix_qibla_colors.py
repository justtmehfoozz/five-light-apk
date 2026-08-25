import re

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val secondaryTextColor = Color.semanticMutedText""",
"""    val secondaryTextColor = Color.semanticSecondaryText"""
)
content = content.replace(
"""                    textColor = secondaryTextColor.copy(alpha = 0.6f)""",
"""                    textColor = Color.semanticMutedText""" # Just checking if this exists
)

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'w') as f:
    f.write(content)
