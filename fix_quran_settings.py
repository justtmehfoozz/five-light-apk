import os

def replace_in_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)

replace_in_file("app/src/main/java/com/example/ui/screens/QuranScreen.kt", [
    ("val baseColor = if (isDark) Color.White else Color.Black", "val baseColor = if (isDark) Color.White else com.example.ui.theme.LightPrimaryText"),
    ("com.example.ui.theme.PrimaryAccentLight", "com.example.ui.theme.LightAccentGold")
])

replace_in_file("app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt", [
    ("Color.Black.copy(alpha = 0.05f)", "com.example.ui.theme.LightPrimaryText.copy(alpha = 0.05f)"),
    ("Color.Black.copy(alpha = 0.55f)", "com.example.ui.theme.LightMutedText")
])
