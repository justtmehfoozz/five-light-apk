import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

# Replace old constants with new semantic tokens
content = content.replace("com.example.ui.theme.DockBackground", "androidx.compose.ui.graphics.Color.semanticDockBackground")
content = content.replace("com.example.ui.theme.DockBorder", "androidx.compose.ui.graphics.Color.semanticDockBorder")
content = content.replace("com.example.ui.theme.DockIconActiveBg", "androidx.compose.ui.graphics.Color.semanticDockIconActiveBg")
content = content.replace("com.example.ui.theme.DockIconActive", "androidx.compose.ui.graphics.Color.semanticDockIconActive")
content = content.replace("com.example.ui.theme.DockIconInactive", "androidx.compose.ui.graphics.Color.semanticDockIconInactive")

# In BottomNavBar.kt, wait: "if (isSearchActive) Color(0xFF2C2C2E) else ..."
content = content.replace("androidx.compose.ui.graphics.Color(0xFF2C2C2E)", "androidx.compose.ui.graphics.Color.semanticControl")

with open(filename, 'w') as f:
    f.write(content)
