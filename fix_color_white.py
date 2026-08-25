import os
import re

files_to_check = [
    'app/src/main/java/com/example/ui/screens/HomeScreen.kt',
    'app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt',
    'app/src/main/java/com/example/ui/screens/CalendarScreen.kt'
]

for filename in files_to_check:
    with open(filename, 'r') as f:
        content = f.read()

    # Replace manual ternary with semanticAccentForeground
    content = re.sub(r'if\s*\(\s*isDark\s*\)\s*androidx\.compose\.ui\.graphics\.Color\(\s*0xFFE6DEF6\s*\)\s*else\s*androidx\.compose\.ui\.graphics\.Color\.White', 'Color.semanticAccentForeground', content)
    content = re.sub(r'if\s*\(\s*isDark\s*\)\s*Color\(\s*0xFFE6DEF6\s*\)\s*else\s*Color\.White', 'Color.semanticAccentForeground', content)
    
    # Ensure import is present if we changed it
    if "semanticAccentForeground" in content and "import com.example.ui.theme.semanticAccentForeground" not in content:
        content = content.replace("import com.example.ui.theme.semanticPrimaryAccent", "import com.example.ui.theme.semanticPrimaryAccent\nimport com.example.ui.theme.semanticAccentForeground")
    
    with open(filename, 'w') as f:
        f.write(content)
