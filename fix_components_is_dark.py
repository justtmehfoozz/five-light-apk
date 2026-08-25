import re

def replace_in_file(path, old, new):
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace(old, new)
    with open(path, 'w') as f:
        f.write(content)

replace_in_file('./app/src/main/java/com/example/ui/components/AzaanOverlay.kt', 
    'androidx.compose.foundation.isSystemInDarkTheme()', 
    'androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }')

replace_in_file('./app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt', 
    'androidx.compose.foundation.isSystemInDarkTheme()', 
    'androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }')

replace_in_file('./app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt', 
    'androidx.compose.foundation.isSystemInDarkTheme()', 
    'androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }')

replace_in_file('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 
    'androidx.compose.foundation.isSystemInDarkTheme()', 
    'androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }')
