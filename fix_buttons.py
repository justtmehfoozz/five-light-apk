import re

def replace_in_file(path, old, new):
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace(old, new)
    with open(path, 'w') as f:
        f.write(content)

replace_in_file('./app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    'contentColor = androidx.compose.ui.graphics.Color.White',
    'contentColor = if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color(0xFFE6DEF6) else androidx.compose.ui.graphics.Color.White')

replace_in_file('./app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt',
    'contentColor = androidx.compose.ui.graphics.Color.White',
    'contentColor = if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color(0xFFE6DEF6) else androidx.compose.ui.graphics.Color.White')

replace_in_file('./app/src/main/java/com/example/ui/components/HomeFeatureCards.kt',
    'contentColor = androidx.compose.ui.graphics.Color.White',
    'contentColor = if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color(0xFFE6DEF6) else androidx.compose.ui.graphics.Color.White')

replace_in_file('./app/src/main/java/com/example/ui/components/AzaanOverlay.kt',
    'contentColor = Color.White',
    'contentColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFE6DEF6) else Color.White')
