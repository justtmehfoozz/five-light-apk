import re

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""val TextTertiaryLight = LightMutedText""",
"""val TextTertiaryLight = Color(0xFF7A7771)"""
)

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
