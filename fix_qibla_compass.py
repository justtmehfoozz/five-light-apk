import re

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""            val ringColor = if (!isDark) {
                Color.semanticStrongBorder.copy(alpha = (baseRingAlpha + pulseBonus).coerceIn(0f, 0.45f))
            } else {
                Color.Black.copy(alpha = (baseRingAlpha + pulseBonus).coerceIn(0f, 0.75f))
            }""",
"""            val ringColor = if (!isDark) {
                Color.semanticStrongBorder.copy(alpha = (baseRingAlpha + pulseBonus).coerceIn(0.5f, 1.0f))
            } else {
                Color.semanticBorder.copy(alpha = (baseRingAlpha + pulseBonus).coerceIn(0f, 0.75f))
            }"""
)

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'w') as f:
    f.write(content)
