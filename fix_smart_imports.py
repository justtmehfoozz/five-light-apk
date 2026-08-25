import re

with open('./app/src/main/java/com/example/ui/screens/SmartPrayerNotificationsSubScreen.kt', 'r') as f:
    content = f.read()

# Fix imports
content = content.replace("import androidx.compose.ui.graphics.Color.semanticPrimaryAccent", "import com.example.ui.theme.semanticPrimaryAccent")
content = content.replace("import androidx.compose.ui.graphics.Color.semanticBorder", "import com.example.ui.theme.semanticBorder\nimport com.example.ui.theme.semanticStrongBorder")

# Fix inline usage
content = content.replace("androidx.compose.ui.graphics.Color.semanticPrimaryAccent", "Color.semanticPrimaryAccent")
content = content.replace("androidx.compose.ui.graphics.Color.semanticBorder", "Color.semanticBorder")
content = content.replace("androidx.compose.ui.graphics.Color.semanticStrongBorder", "Color.semanticStrongBorder")

with open('./app/src/main/java/com/example/ui/screens/SmartPrayerNotificationsSubScreen.kt', 'w') as f:
    f.write(content)

