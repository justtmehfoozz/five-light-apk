import re

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

# Fix the import
content = content.replace(
"""import androidx.compose.foundation.isSystemInDarkTheme
@Composable
fun isAppInDarkTheme(): Boolean {
    return androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
}

import androidx.compose.runtime.Composable
import com.example.data.model.PrayerName""",
"""import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.data.model.PrayerName

@Composable
fun isAppInDarkTheme(): Boolean {
    return androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
}"""
)

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/ui/screens/SmartPrayerNotificationsSubScreen.kt', 'r') as f:
    content2 = f.read()

content2 = content2.replace("com.example.ui.theme.semanticPrimaryAccent", "androidx.compose.ui.graphics.Color.semanticPrimaryAccent")
content2 = content2.replace("com.example.ui.theme.semanticBorder", "androidx.compose.ui.graphics.Color.semanticBorder")
content2 = content2.replace("com.example.ui.theme.semanticStrongBorder", "androidx.compose.ui.graphics.Color.semanticStrongBorder")

with open('./app/src/main/java/com/example/ui/screens/SmartPrayerNotificationsSubScreen.kt', 'w') as f:
    f.write(content2)

