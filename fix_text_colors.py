import re

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""val LightMutedText = Color(0xFF7A7771)""",
"""val LightSecondaryText = Color(0xFF66635E)
val LightMutedText = Color(0xFF7A7771)
val LightSubtleText = Color(0xFF8F8C86)"""
)

content = content.replace(
"""val TextSecondaryLight = LightMutedText""",
"""val TextSecondaryLight = LightSecondaryText"""
)

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
