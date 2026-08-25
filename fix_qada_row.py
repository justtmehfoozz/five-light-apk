import re

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    color = if (count > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,""",
"""                    color = if (count > 0) MaterialTheme.colorScheme.error else Color.semanticSuccess,"""
)

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'w') as f:
    f.write(content)
