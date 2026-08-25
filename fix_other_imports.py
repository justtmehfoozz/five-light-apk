import os

files_to_check = [
    'app/src/main/java/com/example/ui/components/AzaanOverlay.kt',
    'app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    'app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt',
    'app/src/main/java/com/example/ui/components/HomeFeatureCards.kt',
    'app/src/main/java/com/example/ui/screens/TasbeehScreen.kt'
]

for filename in files_to_check:
    if os.path.exists(filename):
        with open(filename, 'r') as f:
            content = f.read()

        content = content.replace("semanticPrimaryAccentimport", "semanticPrimaryAccent\nimport")
        content = content.replace("semanticAccentForegroundimport", "semanticAccentForeground\nimport")
        content = content.replace("Composableimport", "Composable\nimport")

        with open(filename, 'w') as f:
            f.write(content)
