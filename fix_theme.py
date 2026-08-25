import re

filename = 'app/src/main/java/com/example/ui/theme/Theme.kt'
with open(filename, 'r') as f:
    content = f.read()

if "semanticAccentForeground" not in content:
    content = content.replace("import com.example.ui.theme.semanticPrimaryAccent",
    "import com.example.ui.theme.semanticPrimaryAccent\nimport com.example.ui.theme.semanticAccentForeground")

with open(filename, 'w') as f:
    f.write(content)
