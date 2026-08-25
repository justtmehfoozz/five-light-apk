import re

files_to_fix = [
    './app/src/main/java/com/example/ui/components/HomeFeatureCards.kt',
    './app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    './app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt'
]

for filepath in files_to_fix:
    with open(filepath, 'r') as f:
        content = f.read()

    # remove bad imports
    content = content.replace("import Color.semanticPrimaryAccent\n", "")
    content = content.replace("import androidx.compose.ui.graphics.Color\n", "")
    
    # insert good imports at the top
    good_imports = "import com.example.ui.theme.semanticPrimaryAccent\nimport androidx.compose.ui.graphics.Color\n"
    content = re.sub(r'(package [^\n]+\n)', r'\1\n' + good_imports, content)
    
    with open(filepath, 'w') as f:
        f.write(content)

