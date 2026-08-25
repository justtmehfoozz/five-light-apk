import re

files_to_fix = [
    './app/src/main/java/com/example/ui/components/HomeFeatureCards.kt',
    './app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    './app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt'
]

for filepath in files_to_fix:
    with open(filepath, 'r') as f:
        content = f.read()

    # Replace com.example.ui.theme.semanticPrimaryAccent with androidx.compose.ui.graphics.Color.semanticPrimaryAccent
    content = content.replace("com.example.ui.theme.semanticPrimaryAccent", "androidx.compose.ui.graphics.Color.semanticPrimaryAccent")
    
    with open(filepath, 'w') as f:
        f.write(content)

