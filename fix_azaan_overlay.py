import re

with open('./app/src/main/java/com/example/ui/components/AzaanOverlay.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticPrimaryAccent,
                            contentColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF000000) else Color.White
                        )""",
"""                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticPrimaryAccent,
                            contentColor = Color.White
                        )"""
)

with open('./app/src/main/java/com/example/ui/components/AzaanOverlay.kt', 'w') as f:
    f.write(content)
