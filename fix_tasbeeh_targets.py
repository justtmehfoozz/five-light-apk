import re

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'r') as f:
    content = f.read()

# Fix target pill colors (33 / 99 / 100 / etc)
# Target is around line 1026 for Surface
# wait let's just use replace to be safe.
content = content.replace(
"""                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.semanticSurfaceElevated,
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    modifier = Modifier""",
"""                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.semanticSurfaceElevated,
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    modifier = Modifier"""
)

# Unselected controls: Dark Surface / Elevated Surface, Primary or muted text
# The selected logic uses TargetSelectedBg and TargetSelectedText
# The Target controls are like: TargetSelectedBg (Color.semanticPrimaryAccent)
content = content.replace(
"""                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) TargetSelectedBg else Color.Transparent,
                                modifier = Modifier
                                    .size(36.dp)""",
"""                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.semanticPrimaryAccent else Color.Transparent,
                                modifier = Modifier
                                    .size(36.dp)"""
)

content = content.replace(
"""                                    Text(
                                        text = t.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TargetSelectedText else TargetUnselectedText
                                    )""",
"""                                    Text(
                                        text = t.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) {
                                            if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
                                        } else {
                                            Color.semanticPrimaryText
                                        }
                                    )"""
)

with open('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt', 'w') as f:
    f.write(content)
