import re

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    val prayedBg = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess.copy(alpha = 0.15f) else Color.semanticPrimaryAccent
                    } else {
                        Color.semanticSurface
                    }
                    val prayedText = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess else Color.White
                    } else {
                        Color.semanticPrimaryText
                    }
                    val prayedBorder = if (isPrayed) {
                        if (isDarkTheme) BorderStroke(1.dp, Color.semanticSuccess) else BorderStroke(1.dp, Color.semanticPrimaryAccent)
                    } else {""",
"""                    val prayedBg = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess.copy(alpha = 0.15f) else Color.semanticSuccess
                    } else {
                        Color.Transparent
                    }
                    val prayedText = if (isPrayed) {
                        if (isDarkTheme) Color.semanticSuccess else Color.White
                    } else {
                        Color.semanticPrimaryText
                    }
                    val prayedBorder = if (isPrayed) {
                        BorderStroke(1.dp, Color.semanticSuccess)
                    } else {"""
)

# And missed state
content = content.replace(
"""                    val isMissed = status == com.example.data.model.PrayerStatus.MISSED
                    val missedBg = if (isMissed) {
                        if (isDarkTheme) Color.semanticError.copy(alpha = 0.15f) else Color.semanticError
                    } else {
                        Color.semanticSurface
                    }""",
"""                    val isMissed = status == com.example.data.model.PrayerStatus.MISSED
                    val missedBg = if (isMissed) {
                        if (isDarkTheme) Color.semanticError.copy(alpha = 0.15f) else Color.semanticError
                    } else {
                        Color.Transparent
                    }"""
)


with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'w') as f:
    f.write(content)
