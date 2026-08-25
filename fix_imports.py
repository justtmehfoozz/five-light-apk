import re

files_to_fix = [
    './app/src/main/java/com/example/ui/components/HomeFeatureCards.kt',
    './app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    './app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt'
]

for filepath in files_to_fix:
    with open(filepath, 'r') as f:
        content = f.read()

    # Change back to Color.semanticPrimaryAccent
    content = content.replace("androidx.compose.ui.graphics.Color.semanticPrimaryAccent", "Color.semanticPrimaryAccent")
    
    # Add import if missing
    if "import com.example.ui.theme.semanticPrimaryAccent" not in content:
        # insert after package
        content = re.sub(r'(package [^\n]+\n)', r'\1\nimport com.example.ui.theme.semanticPrimaryAccent\nimport androidx.compose.ui.graphics.Color\n', content)
        
    with open(filepath, 'w') as f:
        f.write(content)

