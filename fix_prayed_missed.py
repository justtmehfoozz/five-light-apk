import re

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'r') as f:
    content = f.read()

# For prayed:
content = content.replace(
"""                    val prayedBg = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess.copy(alpha = 0.15f) else Color.semanticSuccess.copy(alpha = 0.15f)
                    } else {""",
"""                    val prayedBg = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess.copy(alpha = 0.15f) else Color.semanticPrimaryAccent
                    } else {"""
)
content = content.replace(
"""                    val prayedTextColor = if (isPrayed) {
                        Color.semanticSuccess
                    } else {""",
"""                    val prayedTextColor = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess else Color(0xFFFFFFFF)
                    } else {"""
)
content = content.replace(
"""                    val prayedBorder = if (isPrayed) {
                        BorderStroke(1.dp, Color.semanticSuccess)
                    } else {""",
"""                    val prayedBorder = if (isPrayed) {
                        if (isDarkTheme) BorderStroke(1.dp, Color.semanticSuccess) else BorderStroke(1.dp, Color.semanticPrimaryAccent)
                    } else {"""
)

# For missed:
content = content.replace(
"""                    val missedBg = if (isMissed) {
                        if (isDarkTheme) Color.semanticError.copy(alpha = 0.15f) else Color.semanticError.copy(alpha = 0.15f)
                    } else {""",
"""                    val missedBg = if (isMissed) {
                        if (isDarkTheme) Color.semanticError.copy(alpha = 0.15f) else Color.semanticError
                    } else {"""
)
content = content.replace(
"""                    val missedTextColor = if (isMissed) {
                        Color.semanticError
                    } else {""",
"""                    val missedTextColor = if (isMissed) {
                        if (isDarkTheme) Color.semanticError else Color(0xFFFFFFFF)
                    } else {"""
)

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'w') as f:
    f.write(content)
