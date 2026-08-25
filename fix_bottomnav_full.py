import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

# Make it use Color.semantic...
content = content.replace("androidx.compose.ui.graphics.Color.semanticControl", "Color.semanticControl")
content = content.replace("androidx.compose.ui.graphics.Color.semanticDockBackground", "Color.semanticDockBackground")
content = content.replace("androidx.compose.ui.graphics.Color.semanticDockBorder", "Color.semanticDockBorder")
content = content.replace("androidx.compose.ui.graphics.Color.semanticDockIconActiveBg", "Color.semanticDockIconActiveBg")
content = content.replace("androidx.compose.ui.graphics.Color.semanticDockIconActive", "Color.semanticDockIconActive")
content = content.replace("androidx.compose.ui.graphics.Color.semanticDockIconInactive", "Color.semanticDockIconInactive")

# Replace explicit Light Mode colors with semantic tokens where applicable
# Light Mode Background: 0xFFFFFFFF -> Color.semanticDockBackground
content = content.replace("androidx.compose.ui.graphics.Color(0xFFFFFFFF)", "Color.semanticDockBackground")
# Light Mode inactive: 0xFF66635E -> Color.semanticDockIconInactive
content = content.replace("androidx.compose.ui.graphics.Color(0xFF66635E)", "Color.semanticDockIconInactive")
# Light Mode active Bg: 0xFF1E1D1A -> Color.semanticDockIconActiveBg (Wait, it says Color(0xFF1E1D1A). We want Accent for Light Mode, which is 0xFF8D6B1E)
content = content.replace("androidx.compose.ui.graphics.Color(0xFF1E1D1A)", "Color.semanticDockIconActiveBg")

imports = [
    "import com.example.ui.theme.semanticControl",
    "import com.example.ui.theme.semanticDockBackground",
    "import com.example.ui.theme.semanticDockBorder",
    "import com.example.ui.theme.semanticDockIconActiveBg",
    "import com.example.ui.theme.semanticDockIconActive",
    "import com.example.ui.theme.semanticDockIconInactive"
]

for imp in imports:
    if imp not in content:
        content = content.replace("import androidx.compose.runtime.Composable", f"{imp}\nimport androidx.compose.runtime.Composable")

with open(filename, 'w') as f:
    f.write(content)
