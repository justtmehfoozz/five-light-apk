import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

# Fix missing import and collapsed imports
content = content.replace("semanticDockIconActiveBgimport", "semanticDockIconActiveBg\nimport")
content = content.replace("semanticDockIconInactiveimport", "semanticDockIconInactive\nimport")

if "import com.example.ui.theme.semanticDockIconActive\n" not in content:
    content = content.replace("import com.example.ui.theme.semanticDockIconActiveBg", "import com.example.ui.theme.semanticDockIconActiveBg\nimport com.example.ui.theme.semanticDockIconActive")

with open(filename, 'w') as f:
    f.write(content)
