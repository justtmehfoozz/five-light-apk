import re

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color(0xFF000000) else androidx.compose.ui.graphics.Color.White""",
"""                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = androidx.compose.ui.graphics.Color.White"""
)

with open('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'w') as f:
    f.write(content)
