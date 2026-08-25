import os
import re

files_to_check = [
    'app/src/main/java/com/example/ui/components/AzaanOverlay.kt',
    'app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    'app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt',
    'app/src/main/java/com/example/ui/components/HomeFeatureCards.kt'
]

for filename in files_to_check:
    if os.path.exists(filename):
        with open(filename, 'r') as f:
            content = f.read()

        content = content.replace(
            "if (androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }) Color(0xFFE6DEF6) else Color.White",
            "Color.semanticAccentForeground"
        )
        content = content.replace(
            "if (androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }) androidx.compose.ui.graphics.Color(0xFFE6DEF6) else androidx.compose.ui.graphics.Color.White",
            "androidx.compose.ui.graphics.Color.semanticAccentForeground"
        )

        if "semanticAccentForeground" in content and "import com.example.ui.theme.semanticAccentForeground" not in content:
            if "import com.example.ui.theme.semanticPrimaryAccent" in content:
                content = content.replace("import com.example.ui.theme.semanticPrimaryAccent", "import com.example.ui.theme.semanticPrimaryAccent\nimport com.example.ui.theme.semanticAccentForeground")
            else:
                content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport com.example.ui.theme.semanticAccentForeground")

        with open(filename, 'w') as f:
            f.write(content)
