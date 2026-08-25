import re

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        tint = MaterialTheme.colorScheme.onSurface,""",
"""                        tint = Color.semanticPrimaryText,"""
)

content = content.replace(
"""                    color = MaterialTheme.colorScheme.onSurface,""",
"""                    color = Color.semanticPrimaryText,"""
)

content = content.replace(
"""                    color = MaterialTheme.colorScheme.onSurfaceVariant,""",
"""                    color = Color.semanticMutedText,"""
)

with open('./app/src/main/java/com/example/ui/screens/ExploreScreen.kt', 'w') as f:
    f.write(content)
