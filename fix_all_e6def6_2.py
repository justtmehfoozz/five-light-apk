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

        # Replace all instances of `if (...) ... Color(0xFFE6DEF6) else ... Color.White`
        content = re.sub(
            r'if\s*\([^)]*\)\s*(?:androidx\.compose\.ui\.graphics\.)?Color\(0xFFE6DEF6\)\s*else\s*(?:androidx\.compose\.ui\.graphics\.)?Color\.(?:White|FFFFFF)', 
            'Color.semanticAccentForeground', 
            content
        )

        if "semanticAccentForeground" in content and "import com.example.ui.theme.semanticAccentForeground" not in content:
            if "import com.example.ui.theme.semanticPrimaryAccent" in content:
                content = content.replace("import com.example.ui.theme.semanticPrimaryAccent", "import com.example.ui.theme.semanticPrimaryAccent\nimport com.example.ui.theme.semanticAccentForeground")
            else:
                content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport com.example.ui.theme.semanticAccentForeground")

        with open(filename, 'w') as f:
            f.write(content)
