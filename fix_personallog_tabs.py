import re

filename = 'app/src/main/java/com/example/ui/components/HomeFeatureCards.kt'
with open(filename, 'r') as f:
    content = f.read()

# Replace day pill color logic
content = content.replace("isSelected -> MaterialTheme.colorScheme.primary", "isSelected -> Color.semanticPrimaryAccent")
content = content.replace("isSelected -> MaterialTheme.colorScheme.onPrimary", "isSelected -> Color.semanticAccentForeground")

with open(filename, 'w') as f:
    f.write(content)
